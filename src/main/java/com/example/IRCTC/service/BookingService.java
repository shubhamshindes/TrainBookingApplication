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
    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    public String bookTrain(Long userId, Long trainId, int seats) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Train> trainOpt = trainRepository.findById(trainId);

        if (userOpt.isEmpty()) {
            logger.warn("User not found with ID: {}", userId);
            return "User not found.";
        }
        if (trainOpt.isEmpty()) {
            logger.warn("Train not found with ID: {}", trainId);
            return "Train not found.";
        }

        User user = userOpt.get();
        Train train = trainOpt.get();

        if (train.getAvailableSeats() < seats) {
            logger.warn("Not enough seats available for train {}. Requested: {}, Available: {}", trainId, seats, train.getAvailableSeats());
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
        logger.info("Booking successful for User {} on Train {} for {} seats.", userId, trainId, seats);
        return "Booking successful!";
    }
    public List<Bookings> getBookingsByUser(Long userId) {
        return bookingsRepository.findByUserId(userId);
    }

}
