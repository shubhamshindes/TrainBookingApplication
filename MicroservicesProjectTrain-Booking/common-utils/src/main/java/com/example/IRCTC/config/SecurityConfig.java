package com.example.IRCTC.config;

import com.example.IRCTC.filter.JwtFilter;
import com.example.IRCTC.exceptions.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Unified security configuration for all microservices that depend on common-utils.
 * Replaces the old SpringSecurity.java (deleted) to avoid duplicate bean conflicts.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtFilter jwtFilter, ObjectMapper objectMapper) {
        this.jwtFilter = jwtFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, exception) -> writeSecurityError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized",
                    "Authentication is required to access this resource",
                    request.getRequestURI()))
                .accessDeniedHandler((request, response, exception) -> writeSecurityError(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "Forbidden",
                    "You do not have permission to access this resource",
                    request.getRequestURI())))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                // User endpoints
                .requestMatchers("/user/**", "/USER/**").hasAnyRole("USER", "ADMIN")
                // Train endpoints
                .requestMatchers("/trains/allTrains", "/trains/*/available-seats", "/trains/*/details").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/trains/add", "/trains/deleteTrainById/**").hasRole("ADMIN")
                .requestMatchers("/trains/*/update-seats").hasAnyRole("USER", "ADMIN")
                // Booking endpoints
                .requestMatchers("/bookings/**").hasAnyRole("USER", "ADMIN")
                // Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    private void writeSecurityError(HttpServletResponse response, int status, String error,
            String message, String path) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(message, status, error, path));
    }
}
