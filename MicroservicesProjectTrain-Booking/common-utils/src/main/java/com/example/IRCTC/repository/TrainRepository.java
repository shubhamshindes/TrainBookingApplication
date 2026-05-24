package com.example.IRCTC.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.IRCTC.entity.Train;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {}
