package com.example.IRCTC.controller;

import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> bookTrain(@RequestBody Map<String, Object> requestData) {
        // Extract values from the JSON request
        Long userId = ((Number) requestData.get("userId")).longValue();
        Long trainId = ((Number) requestData.get("trainId")).longValue();
        int seatsBooked = (int) requestData.get("seatsBooked");

        // Call the service method
        String result = bookingService.bookTrain(userId, trainId, seatsBooked);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("message", result);

        return result.equals("Booking successful!")
                ? ResponseEntity.status(HttpStatus.CREATED).body(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getBookingsByUser(@PathVariable Long userId) {
        List<Bookings> bookings = bookingService.getBookingsByUser(userId);

        if (bookings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No bookings found for this user"));
        }

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Bookings booking : bookings) {
            Map<String, Object> bookingDetails = new HashMap<>();
            bookingDetails.put("bookingId", booking.getId());
            bookingDetails.put("trainId", booking.getTrain().getId());
            bookingDetails.put("trainName", booking.getTrain().getName());
            bookingDetails.put("seatsBooked", booking.getSeatsBooked());

            responseList.add(bookingDetails);
        }

        return ResponseEntity.ok(Map.of("bookings", responseList));
    }


}
