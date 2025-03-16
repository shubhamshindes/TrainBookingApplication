package com.example.IRCTC.service;

import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.entity.Train;
import com.example.IRCTC.entity.User;
import com.example.IRCTC.repository.BookingsRepository;
import com.example.IRCTC.repository.TrainRepository;
import com.example.IRCTC.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public String bookTrain(Long userId, Long trainId, int seats) {
        logger.info("Processing booking request: userId={}, trainId={}, seats={}", userId, trainId, seats);

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Train> trainOpt = trainRepository.findById(trainId);

            if (userOpt.isEmpty()) {
                logger.warn("Booking failed: User not found with ID={}", userId);
                return "User not found.";
            }
            if (trainOpt.isEmpty()) {
                logger.warn("Booking failed: Train not found with ID={}", trainId);
                return "Train not found.";
            }

            User user = userOpt.get(); //extracting User obj from Optional user (userOpt)
            Train train = trainOpt.get();

            if (train.getAvailableSeats() < seats) {
                logger.warn("Booking failed: Not enough seats on train ID={}, Requested={}, Available={}",
                        trainId, seats, train.getAvailableSeats());
                return "Not enough seats available.";
            }

            // Deduct available seats
            train.setAvailableSeats(train.getAvailableSeats() - seats);
            trainRepository.save(train);

            // Save booking
            Bookings booking = new Bookings();
            booking.setUser(user);
            booking.setTrain(train);
            booking.setSeatsBooked(seats);
            bookingsRepository.save(booking);

            logger.info("Booking successful: User ID={}, Train ID={}, Seats Booked={}", userId, trainId, seats);
            return "Booking successful!";
        } catch (Exception e) {
            logger.error("Error processing booking request: userId={}, trainId={}, seats={}. Exception: {}",
                    userId, trainId, seats, e.getMessage(), e);
            return "An error occurred while booking the train.";
        }
    }

    public List<Bookings> getBookingsByUser(Long userId) {
        logger.info("Fetching bookings for userId={}", userId);
        List<Bookings> bookings = bookingsRepository.findByUserId(userId);

        if (bookings.isEmpty()) {
            logger.warn("No bookings found for userId={}", userId);
        } else {
            logger.info("Found {} bookings for userId={}", bookings.size(), userId);
        }

        return bookings;
    }
    @Transactional
    public String deleteBookingsByTrainId(Long trainId) {
        logger.info("Attempting to delete bookings for Train ID={}", trainId);

        List<Bookings> bookings = bookingsRepository.findByTrainId(trainId);
        if (bookings.isEmpty()) {
            logger.warn("No bookings found for Train ID={}", trainId);
            return "No bookings found for the given Train ID.";
        }

        bookingsRepository.deleteAll(bookings);
        logger.info("Deleted {} bookings for Train ID={}", bookings.size(), trainId);
        return "Bookings cancelled successfully!";
    }
}
