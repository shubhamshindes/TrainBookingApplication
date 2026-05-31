package com.example.IRCTC.service;

import java.util.List;
import com.example.IRCTC.entity.Train;

public interface TrainService {
    List<Train> getAllTrains();
    Train addTrain(Train train);
    Train getTrainById(Long trainId);
    void deleteTrainById(Long id);
    void updateSeats(Long trainId, int seats);
}
