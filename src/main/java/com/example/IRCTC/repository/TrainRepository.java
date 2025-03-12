package com.example.IRCTC.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.IRCTC.entity.Train;

public interface TrainRepository extends JpaRepository<Train, Long> {}