package com.example.IRCTC.service;

import java.util.List;
import com.example.IRCTC.entity.Bookings;

public interface BookingService {
    String bookTrain(Long userId, Long trainId, int seatsBooked, String authorization, String principalName, boolean admin);
    List<Bookings> getBookingsByUser(Long userId, String authorization, String principalName, boolean admin);
    String deleteBookingsByTrainId(Long trainId, String authorization);
    String cancelBooking(Long bookingId, String authorization, String principalName, boolean admin);
}
