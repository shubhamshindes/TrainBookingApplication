package com.example.IRCTC.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.IRCTC.entity.User;
import com.example.IRCTC.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findByUserName(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get(); // Extract User from Optional
			return org.springframework.security.core.userdetails.User.builder()
					.username(user.getUserName()) //Sets the username.
					.password(user.getPassword())//Sets the hashed password (stored in DB).
					.roles(user.getRoles().toArray(new String[0])) //Converts user roles (e.g., ROLE_ADMIN) into a String[].
					.build();
		}

		throw new UsernameNotFoundException("User not found with username: " + username);
	}

}