package com.example.IRCTC.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.repository.UserRepository;
import com.example.IRCTC.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return users.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(users);
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "User retrieved successfully");
                    response.put("data", user);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "User not found with ID: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }
    @PutMapping("/updateUser")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        User userInDb = userService.findByUserName(userName);
        if (userInDb == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } else {

            userInDb.setUserName(user.getUserName());
            userInDb.setPassword(passwordEncoder.encode(user.getPassword()));  // Secure password storage
            userService.saveNewUser(userInDb);

            return ResponseEntity.ok("User updated successfully");
        }
    }

}
