package com.example.IRCTC.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.exceptions.UserNotFoundException;
import com.example.IRCTC.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")

public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Received request to fetch all users.");
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            logger.warn("No users found in the database.");
            return ResponseEntity.noContent().build();
        } else {
            logger.info("Returning {} users.", users.size());
            return ResponseEntity.ok(users);
        }
    }

    @DeleteMapping("deleteUserById/{id}")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        logger.info("Received request to delete user with ID: {}", id);
        userService.deleteUserById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        logger.info("Received request to fetch user with ID: {}", id);
        User user = userService.getUserById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User retrieved successfully");
        response.put("data", user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        logger.info("API call: PUT /users/update/{} with updates: {}", id, updates);

        String response = userService.updateUser(id, updates);
        if (response.equals("User not found")) {
            logger.warn("User update failed: User with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("User update successful for ID: {}", id);
        return ResponseEntity.ok(response);
    }

}
