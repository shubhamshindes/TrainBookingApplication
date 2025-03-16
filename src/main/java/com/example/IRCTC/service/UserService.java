package com.example.IRCTC.service;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.exceptions.UserNotFoundException;
import com.example.IRCTC.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        logger.info("Fetching all users...");
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);
        return userRepository.findById(id);
    }

    public User findByUserName(String userName) {
        logger.info("Fetching user with username: {}", userName);
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", userName);
                    return new UserNotFoundException("User not found with username: " + userName);
                });
    }


    public boolean saveNewUser(User user) {
        try {
            logger.info("Saving new user: {}", user.getUserName());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER"));  // Alternative: Collections.singletonList("USER")
            userRepository.save(user);
            logger.info("User saved successfully: {}", user.getUserName());
            return true;
        } catch (Exception e) {
            logger.error("Error saving user: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteUserById(Long id) {
        logger.info("Attempting to delete user with ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
            logger.info("User deleted successfully with ID: {}", id);
            return true;
        }
        logger.warn("User not found with ID: {}", id);
        throw new UserNotFoundException("User not found with ID: " + id);
    }
}
