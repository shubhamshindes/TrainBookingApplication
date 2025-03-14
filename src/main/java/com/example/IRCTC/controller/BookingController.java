package com.example.IRCTC.controller;

import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.repository.BookingsRepository;
import com.example.IRCTC.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingsRepository bookingsRepository;

    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookTrain(@RequestBody Map<String, Object> requestData) {
        logger.info("Received booking request: {}", requestData);

        try {
            Long userId = ((Number) requestData.get("userId")).longValue();
            Long trainId = ((Number) requestData.get("trainId")).longValue();
            int seatsBooked = (int) requestData.get("seatsBooked");

            logger.info("Processing booking for userId={}, trainId={}, seatsBooked={}", userId, trainId, seatsBooked);

            String result = bookingService.bookTrain(userId, trainId, seatsBooked);

            Map<String, Object> response = new HashMap<>();
            response.put("message", result);

            if (result.equals("Booking successful!")) {
                logger.info("Booking successful for userId={}, trainId={}", userId, trainId);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Booking failed for userId={}, trainId={}. Reason: {}", userId, trainId, result);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            logger.error("Error occurred while booking train: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while processing the booking request"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookingsByUser(@PathVariable Long userId) {
        logger.info("Fetching bookings for userId={}", userId);

        List<Bookings> bookings = bookingsRepository.findByUserId(userId);

        if (bookings.isEmpty()) {
            logger.warn("No bookings found for userId={}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No bookings found for this user"));
        }

        List<Map<String, Object>> bookingDetails = new ArrayList<>();

        for (Bookings booking : bookings) {
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("bookingId", booking.getId());
            bookingData.put("trainId", booking.getTrain().getId());
            bookingData.put("trainName", booking.getTrain().getName());
            bookingData.put("username", booking.getUser().getUserName());
            bookingData.put("seatsBooked", booking.getSeatsBooked());

            bookingDetails.add(bookingData);
        }

        logger.info("Returning {} bookings for userId={}", bookingDetails.size(), userId);
        return ResponseEntity.ok(Map.of("bookings", bookingDetails));
    }
    @DeleteMapping("/delete/train/{trainId}")
    public ResponseEntity<Map<String, Object>> deleteBookingsByTrainId(@PathVariable Long trainId) {
        logger.info("Received request to delete bookings for Train ID={}", trainId);

        String result = bookingService.deleteBookingsByTrainId(trainId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", result);

        HttpStatus status = result.equals("Bookings cancelled for train successfully!") ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        logger.info("Delete response: {} for train :{}", result,trainId);
        return ResponseEntity.status(status).body(response);
    }
}
