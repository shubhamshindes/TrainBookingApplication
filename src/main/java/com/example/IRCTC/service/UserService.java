package com.example.IRCTC.service;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public List<User> getAllUsers()
    {
        return userRepository.findAll();
    }
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUserName(String userName){
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + userName));
    }

    public boolean saveNewUser(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));  // Better alternative to Arrays.asList()
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;  // Simply return false in case of an error
        }
    }


}
