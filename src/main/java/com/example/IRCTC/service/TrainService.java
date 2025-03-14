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
    public boolean deleteTrainById(Long id) {
        if (trainRepository.existsById(id)) {
            trainRepository.deleteById(id);
            return true; // Successfully deleted
        }
        return false; // Train ID not found
    }
}
