package com.example.BankManagement.dto;

import com.example.BankManagement.Entity.UserEntity;

public class UserDTO {

    private int id;
    private String fullName;
    private String email;
    private String username;
    private String role;

    /** Only set on the login response; null everywhere else (e.g. register). */
    private String token;

    public UserDTO() {
    }

    public UserDTO(int id, String fullName, String email, String username, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public static UserDTO fromEntity(UserEntity user) {
        if (user == null) return null;
        return new UserDTO(user.getId(), user.getFullName(), user.getEmail(), user.getUsername(), user.getRole());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
