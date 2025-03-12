package com.example.IRCTC;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class TrainBookingApplication {
  
    public static void main(String[] args) {
        SpringApplication.run(TrainBookingApplication.class, args);
    }

    

}