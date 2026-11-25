package com.example.IRCTC.service.impl;

import com.example.IRCTC.client.TrainServiceClient;
import com.example.IRCTC.client.UserServiceClient;
import com.example.IRCTC.dto.TrainDto;
import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.entity.Train;
import com.example.IRCTC.entity.User;
import com.example.IRCTC.exceptions.ResourceNotFoundException;
import com.example.IRCTC.repository.BookingsRepository;
import com.example.IRCTC.repository.UserRepository;
import com.example.IRCTC.repository.TrainRepository;
import com.example.IRCTC.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private TrainServiceClient trainServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    @Override
    @SuppressWarnings("unchecked")
    public String bookTrain(Long userId, Long trainId, int seatsBooked, String authorization,
                            String principalName, boolean admin) {
        logger.info("Booking request received: userId={}, trainId={}, seatsBooked={}", userId, trainId, seatsBooked);
        if (userId == null || userId <= 0 || trainId == null || trainId <= 0) {
            throw new IllegalArgumentException("Valid userId and trainId are required");
        }
        if (seatsBooked < 1) {
            throw new IllegalArgumentException("At least one seat must be booked");
        }

        // 1. Fetch/Sync User details via Feign Client
        ResponseEntity<Map<String, Object>> userResponse = userServiceClient.getUserById(userId, authorization);
        if (userResponse == null || !userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new ResourceNotFoundException("User not found via user-service with ID: " + userId);
        }
        Map<String, Object> userData = (Map<String, Object>) userResponse.getBody().get("data");
        if (userData == null) {
            throw new ResourceNotFoundException("User data not found for ID: " + userId);
        }
        String requestedUserName = (String) userData.get("userName");
        if (!admin && (principalName == null || !principalName.equalsIgnoreCase(requestedUserName))) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot create bookings for another user");
        }

        // Upsert user locally in booking_db
        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(userId);
            newUser.setUserName((String) userData.get("userName"));
            newUser.setEmail((String) userData.get("email"));
            newUser.setRoles((List<String>) userData.get("roles"));
            return userRepository.save(newUser);
        });

        // 2. Fetch/Sync Train details via Feign Client
        ResponseEntity<TrainDto> trainResponse = trainServiceClient.getTrainById(trainId, authorization);
        if (trainResponse == null || !trainResponse.getStatusCode().is2xxSuccessful() || trainResponse.getBody() == null) {
            throw new ResourceNotFoundException("Train not found via train-service with ID: " + trainId);
        }
        TrainDto trainDto = trainResponse.getBody();

        // Upsert train locally in booking_db
        Train train = trainRepository.findById(trainId).orElseGet(() -> {
            Train newTrain = new Train();
            newTrain.setId(trainId);
            newTrain.setName(trainDto.getName());
            newTrain.setAvailableSeats(trainDto.getAvailableSeats());
            return trainRepository.save(newTrain);
        });

        // Check availability
        if (train.getAvailableSeats() < seatsBooked) {
            throw new IllegalArgumentException("Not enough seats available!");
        }

        // 3. Update seats via Feign Client in train-service
        ResponseEntity<Map<String, Object>> seatUpdateResponse = trainServiceClient.updateAvailableSeats(trainId, -seatsBooked, authorization);
        if (seatUpdateResponse == null || !seatUpdateResponse.getStatusCode().is2xxSuccessful()) {
            logger.warn("Seat update Feign call failed for trainId={}; rollback may be needed", trainId);
            throw new RuntimeException("Failed to update seat count via train-service");
        }

        // Update local cache
        train.setAvailableSeats(train.getAvailableSeats() - seatsBooked);
        trainRepository.save(train);

        // 4. Save Booking
        Bookings booking = new Bookings();
        booking.setUser(user);
        booking.setTrain(train);
        booking.setSeatsBooked(seatsBooked);
        bookingsRepository.save(booking);

        logger.info("Booking successful: User ID={}, Train ID={}, Seats Booked={}", userId, trainId, seatsBooked);
        return "Booking successful!";
    }

    @Override
    public List<Bookings> getBookingsByUser(Long userId, String authorization, String principalName, boolean admin) {
        logger.info("Fetching bookings for userId={}", userId);
        if (!admin) {
            ResponseEntity<Map<String, Object>> userResponse = userServiceClient.getUserById(userId, authorization);
            if (userResponse == null || userResponse.getBody() == null) {
                throw new ResourceNotFoundException("User not found with ID: " + userId);
            }
            Map<String, Object> userData = (Map<String, Object>) userResponse.getBody().get("data");
            String requestedUserName = userData == null ? null : (String) userData.get("userName");
            if (principalName == null || !principalName.equalsIgnoreCase(requestedUserName)) {
                throw new org.springframework.security.access.AccessDeniedException("You cannot view another user's bookings");
            }
        }
        return bookingsRepository.findByUserId(userId);
    }

    @Override
    public String deleteBookingsByTrainId(Long trainId, String authorization) {
        logger.info("Deleting bookings for Train ID={}", trainId);
        List<Bookings> bookings = bookingsRepository.findByTrainId(trainId);
        if (bookings.isEmpty()) {
            throw new ResourceNotFoundException("No bookings found for Train ID: " + trainId);
        }

        int totalSeatsToRestore = bookings.stream().mapToInt(Bookings::getSeatsBooked).sum();
        bookingsRepository.deleteAll(bookings);
        logger.info("Deleted {} bookings for Train ID={}", bookings.size(), trainId);

        // Restore seats via Feign
        trainServiceClient.updateAvailableSeats(trainId, totalSeatsToRestore, authorization);
        logger.info("Restored {} seats to trainId={} via Feign", totalSeatsToRestore, trainId);

        return "Bookings cancelled for train successfully!";
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String cancelBooking(Long bookingId, String authorization, String principalName, boolean admin) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("A valid bookingId is required");
        }
        Bookings booking = bookingsRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!admin && (principalName == null
                || !principalName.equalsIgnoreCase(booking.getUser().getUserName()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot cancel another user's booking");
        }

        Train train = booking.getTrain();
        bookingsRepository.delete(booking);
        trainServiceClient.updateAvailableSeats(
                train.getId(), booking.getSeatsBooked(), authorization);
        logger.info("Cancelled bookingId={} and restored {} seats to trainId={}",
                bookingId, booking.getSeatsBooked(), train.getId());
        return "Booking cancelled successfully!";
    }
}
