package com.example.IRCTC.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.IRCTC.entity.Bookings;
import com.example.IRCTC.repository.BookingRepository;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    
    public Bookings bookTrain(Bookings booking) { return bookingRepository.save(booking); }
}