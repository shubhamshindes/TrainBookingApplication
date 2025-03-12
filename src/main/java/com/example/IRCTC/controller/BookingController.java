package com.example.IRCTC.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.service.BookingService;

@RestController
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    
    @PostMapping("/book")
    public Bookings bookTicket(@RequestBody Bookings booking) { return bookingService.bookTrain(booking); }
}
