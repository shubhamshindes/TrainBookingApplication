package com.example.IRCTC.service;

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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID=" + userId));

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with ID=" + trainId));

        if (train.getAvailableSeats() < seats) {
            throw new InvalidBookingException("Not enough seats available on Train ID=" + trainId);
        }

        // Deduct seats
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
    }

    public List<Bookings> getBookingsByUser(Long userId) {
        logger.info("Fetching bookings for userId={}", userId);
        return bookingsRepository.findByUserId(userId);
    }
    @Transactional
    public String deleteBookingsByTrainId(Long trainId) {
        logger.info("Deleting bookings for Train ID={}", trainId);

        List<Bookings> bookings = bookingsRepository.findByTrainId(trainId);
        if (bookings.isEmpty()) {
            throw new ResourceNotFoundException("No bookings found for Train ID=" + trainId);
        }

        bookingsRepository.deleteAll(bookings);
        logger.info("Deleted {} bookings for Train ID={}", bookings.size(), trainId);
        return "Bookings cancelled successfully!";
    }
}
