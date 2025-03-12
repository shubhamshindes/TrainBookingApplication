package com.example.IRCTC.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.repository.UserRepository;
import com.example.IRCTC.utils.JwtUtil;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public String registerUser(String username, String email, String password, String role) {
        if (userRepository.findByUserName(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        
        User user = new User();
        user.setUserName(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(role);
        userRepository.save(user);
        return "User registered successfully";
    }
}
