package com.example.IRCTC.service;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.IRCTC.entity.User;
import com.example.IRCTC.enums.Role;
import com.example.IRCTC.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUserName(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String[] roles = user.getRoles() == null ? new String[0]
                    : user.getRoles().stream()
                            .map(Role::name)
                            .collect(Collectors.toList())
                            .toArray(new String[0]);
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUserName())
                    .password(user.getPassword())
                    .roles(roles)
                    .build();
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
