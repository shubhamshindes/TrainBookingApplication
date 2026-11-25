package com.example.IRCTC.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.IRCTC.dto.UserDto;
import com.example.IRCTC.entity.User;
import com.example.IRCTC.exceptions.UserNotFoundException;
import com.example.IRCTC.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.info("Received request to fetch all users.");
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            logger.warn("No users found in the database.");
            return ResponseEntity.noContent().build();
        } else {
            logger.info("Returning {} users.", users.size());
            return ResponseEntity.ok(users.stream().map(UserDto::from).toList());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new com.example.IRCTC.exceptions.UnauthorizedException("Authentication is required");
        }
        User user = userService.findByUserName(principal.getName());
        return ResponseEntity.ok(UserDto.from(user));
    }

    @DeleteMapping("deleteUserById/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        logger.info("Received request to delete user with ID: {}", id);
        userService.deleteUserById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id,
            Principal principal, Authentication authentication) {
        logger.info("Received request to fetch user with ID: {}", id);
        User user = userService.getUserById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && (principal == null || !principal.getName().equalsIgnoreCase(user.getUserName()))) {
            throw new com.example.IRCTC.exceptions.AccessDeniedException("You cannot view another user's profile");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User retrieved successfully");
        response.put("data", UserDto.from(user));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> request, Principal principal) {
        if (principal == null) {
            throw new com.example.IRCTC.exceptions.UnauthorizedException("Authentication is required");
        }
        userService.changePassword(
                principal.getName(),
                request.get("currentPassword"),
                request.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates,
            Principal principal, Authentication authentication) {
        logger.info("API call: PUT /users/update/{} with updates: {}", id, updates);
        User target = userService.getUserById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && (principal == null || !principal.getName().equalsIgnoreCase(target.getUserName()))) {
            throw new com.example.IRCTC.exceptions.AccessDeniedException("You cannot update another user's profile");
        }
        if (!admin) {
            updates.remove("roles");
        }

        Object requestedUserName = updates.get("userName");
        if (requestedUserName instanceof String userName
                && userService.existsByUserNameAndIdNot(userName.trim(), id)) {
            return ResponseEntity.status(409).body("Username is already taken");
        }

        String response;
        try {
            response = userService.updateUser(id, updates);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(409).body("Username is already taken");
        }
        if (response.equals("User not found")) {
            logger.warn("User update failed: User with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("User update successful for ID: {}", id);
        return ResponseEntity.ok(response);
    }
}
