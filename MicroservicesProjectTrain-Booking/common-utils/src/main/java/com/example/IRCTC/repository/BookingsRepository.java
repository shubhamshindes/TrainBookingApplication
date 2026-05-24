package com.example.IRCTC.repository;

import com.example.IRCTC.entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<Bookings, Long> {
    List<Bookings> findByUserId(Long userId);
    List<Bookings> findByTrainId(Long trainId);
}
