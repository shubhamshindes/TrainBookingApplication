package com.example.IRCTC.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        boolean isDeleted = userService.deleteUserById(id);

        if (isDeleted) {
            logger.info("User with ID: {} deleted successfully.", id);
            return ResponseEntity.ok("User deleted successfully");
        } else {
            logger.warn("User with ID: {} not found in the database.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in DB");
        }
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        logger.info("Received request to fetch user with ID: {}", id);
        return userService.getUserById(id)
                .map(user -> {
                    logger.info("User with ID: {} retrieved successfully.", id);
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "User retrieved successfully");
                    response.put("data", user);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("User with ID: {} not found.", id);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "User not found with ID: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @PutMapping("/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        logger.info("Received request to update user: {}", userName);

        User userInDb = userService.findByUserName(userName);
        if (userInDb == null) {
            logger.warn("User with username: {} not found.", userName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {
            logger.info("Updating user details for: {}", userName);
            userInDb.setUserName(user.getUserName());
            userInDb.setPassword(passwordEncoder.encode(user.getPassword())); // Secure password storage
            userService.saveNewUser(userInDb);

            logger.info("User: {} updated successfully.", userName);
            return ResponseEntity.ok("User updated successfully");
        }
    }
}
