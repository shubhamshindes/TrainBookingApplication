package com.example.IRCTC.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.IRCTC.entity.Bookings;

public interface BookingRepository extends JpaRepository<Bookings, Long> {}