package com.example.IRCTC.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.IRCTC.entity.User;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
    User findByUserName(String userName);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User saveNewUser(User user);
    boolean deleteUserById(Long id);
    String updateUser(Long id, Map<String, Object> updates);
    boolean resetPasswordByEmail(String email, String newPassword);
}
