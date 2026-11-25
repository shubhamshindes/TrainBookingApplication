package com.example.IRCTC.controller;

import com.example.IRCTC.dto.BookingRequestDto;
import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookTrain(
            @RequestBody BookingRequestDto request,
            @RequestHeader("Authorization") String authorization,
            Principal principal,
            Authentication authentication) {
        logger.info("Received booking request: {}", request);

        if (request == null || request.getUserId() == null || request.getTrainId() == null || request.getSeatsBooked() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request. userId, trainId, and seatsBooked are required."));
        }

        Long userId = request.getUserId();
        Long trainId = request.getTrainId();
        int seatsBooked = request.getSeatsBooked();

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        String result = bookingService.bookTrain(userId, trainId, seatsBooked, authorization,
                principal == null ? null : principal.getName(), admin);
        logger.info("Booking successful for userId={}, trainId={}", userId, trainId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", result));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookingsByUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization,
            Principal principal,
            Authentication authentication) {
        logger.info("Fetching bookings for userId={}", userId);

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        List<Bookings> bookings = bookingService.getBookingsByUser(userId, authorization,
                principal == null ? null : principal.getName(), admin);
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBookingsByTrainId(
            @PathVariable Long trainId,
            @RequestHeader("Authorization") String authorization) {
        logger.info("Received request to delete bookings for Train ID={}", trainId);

        String result = bookingService.deleteBookingsByTrainId(trainId, authorization);
        Map<String, Object> response = new HashMap<>();
        response.put("message", result);

        HttpStatus status = result.equals("Bookings cancelled for train successfully!") ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        logger.info("Delete response: {} for train :{}", result, trainId);
        return ResponseEntity.status(status).body(response);
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authorization,
            Principal principal,
            Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        String result = bookingService.cancelBooking(bookingId, authorization,
                principal == null ? null : principal.getName(), admin);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
