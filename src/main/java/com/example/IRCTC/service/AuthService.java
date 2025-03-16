package com.example.IRCTC.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.repository.UserRepository;
import com.example.IRCTC.utils.JwtUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> registerUser(String username, String email, String password, String role) {
        if (userRepository.findByUserName(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setUserName(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role)); // Convert String role to List<String>

        User savedUser = userRepository.save(user); // Save and get the stored user

        // Create response with user ID
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("userId", savedUser.getId()); // Assuming `id` is auto-generated

        return response;
    }
}
