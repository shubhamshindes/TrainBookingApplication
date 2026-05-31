package com.example.IRCTC.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing user data returned by the user-service via Feign.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private Long id;
    private String userName;
    private String email;

    public UserDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
