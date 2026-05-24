package com.example.IRCTC.service;

import com.example.IRCTC.client.TrainServiceClient;
import com.example.IRCTC.client.UserServiceClient;
import com.example.IRCTC.dto.TrainDto;
import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.entity.Train;
import com.example.IRCTC.entity.User;
import com.example.IRCTC.exceptions.InvalidBookingException;
import com.example.IRCTC.exceptions.ResourceNotFoundException;
import com.example.IRCTC.repository.BookingsRepository;
import com.example.IRCTC.repository.TrainRepository;
import com.example.IRCTC.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private UserRepository userRepository;

    // ───────────────────────── Feign Clients ─────────────────────────
    @Autowired
    private TrainServiceClient trainServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;
    // ─────────────────────────────────────────────────────────────────

    /**
     * Book seats on a train.
     * Uses Feign to verify user/train existence and then delegates seat update to train-service.
     */
    @Transactional
    @CacheEvict(cacheNames = {"trains", "bookings"}, allEntries = true)
    public String bookTrain(Long userId, Long trainId, int seats) {
        logger.info("Processing booking request: userId={}, trainId={}, seats={}", userId, trainId, seats);

        // ── 1. Verify user exists via Feign (user-service) ──
        ResponseEntity<Map<String, Object>> userResponse = userServiceClient.getUserById(userId);
        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new ResourceNotFoundException("User not found with ID=" + userId);
        }
        logger.info("User ID={} verified via user-service", userId);

        // ── 2. Check available seats via Feign (train-service) ──
        ResponseEntity<Map<String, Object>> seatsResponse = trainServiceClient.getTrainAvailableSeats(trainId);
        if (!seatsResponse.getStatusCode().is2xxSuccessful() || seatsResponse.getBody() == null) {
            throw new ResourceNotFoundException("Train not found with ID=" + trainId);
        }
        Object availableSeatsObj = seatsResponse.getBody().get("availableSeats");
        int availableSeats = ((Number) availableSeatsObj).intValue();

        if (availableSeats < seats) {
            throw new InvalidBookingException(
                    "Booking failed! Remaining seats on Train ID=" + trainId + " are " + availableSeats);
        }
        logger.info("Seat availability confirmed: requested={}, available={}", seats, availableSeats);

        // ── 3. Persist booking locally using JPA (booking-service owns this table) ──
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID=" + userId));
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID=" + trainId));

        Bookings booking = new Bookings();
        booking.setUser(user);
        booking.setTrain(train);
        booking.setSeatsBooked(seats);
        bookingsRepository.save(booking);

        // ── 4. Decrement seats on train-service via Feign (delta = -seats) ──
        ResponseEntity<Map<String, Object>> updateResponse =
                trainServiceClient.updateAvailableSeats(trainId, -seats);
        if (!updateResponse.getStatusCode().is2xxSuccessful()) {
            logger.warn("Seat update Feign call failed for trainId={}; rollback may be needed", trainId);
            // Transaction rolls back the booking row automatically on exception
            throw new RuntimeException("Failed to update seat count via train-service");
        }

        logger.info("Booking successful: User ID={}, Train ID={}, Seats Booked={}", userId, trainId, seats);
        return "Booking successful!";
    }

    @Cacheable(cacheNames = "bookings", key = "#userId")
    public List<Bookings> getBookingsByUser(Long userId) {
        logger.info("Fetching bookings for userId={}", userId);
        return bookingsRepository.findByUserId(userId);
    }

    @Transactional
    @CacheEvict(cacheNames = {"bookings", "trains"}, allEntries = true)
    public String deleteBookingsByTrainId(Long trainId) {
        logger.info("Deleting bookings for Train ID={}", trainId);

        List<Bookings> bookings = bookingsRepository.findByTrainId(trainId);
        if (bookings.isEmpty()) {
            throw new ResourceNotFoundException("No bookings found for Train ID=" + trainId);
        }

        // Calculate total seats to restore
        int totalSeatsToRestore = bookings.stream()
                .mapToInt(Bookings::getSeatsBooked)
                .sum();

        bookingsRepository.deleteAll(bookings);
        logger.info("Deleted {} bookings for Train ID={}", bookings.size(), trainId);

        // ── Restore seats on train-service via Feign (delta = +totalSeatsToRestore) ──
        trainServiceClient.updateAvailableSeats(trainId, totalSeatsToRestore);
        logger.info("Restored {} seats to trainId={} via Feign", totalSeatsToRestore, trainId);

        return "Bookings cancelled for train successfully!";
    }
}
