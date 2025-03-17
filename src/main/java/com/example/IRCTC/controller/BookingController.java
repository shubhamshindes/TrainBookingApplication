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

        if (!requestData.containsKey("userId") || requestData.get("userId") == null ||
                !requestData.containsKey("trainId") || requestData.get("trainId") == null ||
                !requestData.containsKey("seatsBooked") || requestData.get("seatsBooked") == null) {

            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request. userId, trainId, and seatsBooked are required."));
        }

        Long userId = ((Number) requestData.get("userId")).longValue();
        Long trainId = ((Number) requestData.get("trainId")).longValue();
        int seatsBooked = ((Number) requestData.get("seatsBooked")).intValue();  // ✅ Proper type conversion

        String result = bookingService.bookTrain(userId, trainId, seatsBooked);
        logger.info("Booking successful for userId={}, trainId={}", userId, trainId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", result));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookingsByUser(@PathVariable Long userId) {
        logger.info("Fetching bookings for userId={}", userId);

        List<Bookings> bookings = bookingService.getBookingsByUser(userId);
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
