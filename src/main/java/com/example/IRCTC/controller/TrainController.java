package com.example.IRCTC.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.IRCTC.entity.Train;
import com.example.IRCTC.service.TrainService;

@RestController
@RequestMapping("/trains")
class TrainController {
    @Autowired
    private TrainService trainService;
    
    @GetMapping("/")
    public List<Train> getTrains() { return trainService.getAllTrains(); }
    
    @PostMapping("/add")
    public Train addTrain(@RequestBody Train train) { return trainService.addTrain(train); }
}
