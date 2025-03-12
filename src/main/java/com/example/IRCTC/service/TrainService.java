package com.example.IRCTC.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.IRCTC.entity.Train;
import com.example.IRCTC.repository.TrainRepository;

@Service
public class TrainService {
    @Autowired
    private TrainRepository trainRepository;
    
    public List<Train> getAllTrains() { return trainRepository.findAll(); }
    public Train addTrain(Train train) { return trainRepository.save(train); }
}
