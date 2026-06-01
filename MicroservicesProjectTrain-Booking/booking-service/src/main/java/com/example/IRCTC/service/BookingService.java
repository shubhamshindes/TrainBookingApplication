package com.example.IRCTC.service;

import java.util.List;
import com.example.IRCTC.entity.Bookings;

public interface BookingService {
    String bookTrain(Long userId, Long trainId, int seatsBooked, String authorization);
    List<Bookings> getBookingsByUser(Long userId);
    String deleteBookingsByTrainId(Long trainId, String authorization);
}
