package com.example.BankManagement.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Full name can only contain letters and spaces")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(unique = true)
    private String username;

    /** Valid values: Role.ADMIN.name() / Role.USER.name() ("ADMIN"/"USER"). Assigned only
     *  server-side via Role.forEmail(...) at registration - never trust a client-supplied value. */
    private String role;

    /** Stores a BCrypt hash, not the raw password - complexity is validated against the
     *  raw value at the point of submission (RegisterRequest, PasswordService.isComplexEnough),
     *  not here, since a hash will never match a human-password-shaped pattern. */
    @NotBlank(message = "Password is required")
    private String password;

    private int failedAttempts = 0;
    private boolean accountNonLocked = true;
    private LocalDateTime lockTime;

    public UserEntity() {
        super();
    }

    public UserEntity(int id, String fullName, String email, String username,
                      String role, String password) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.role = role;
        this.password = password;
    }

    public UserEntity(int id,
                      String fullName,
                      String email,
                      String username,
                      String role,
                      String password,
                      int failedAttempts,
                      boolean accountNonLocked,
                      LocalDateTime lockTime) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.role = role;
        this.password = password;
        this.failedAttempts = failedAttempts;
        this.accountNonLocked = accountNonLocked;
        this.lockTime = lockTime;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    @Override
    public String toString() {
        // password deliberately excluded - never let this leak into a log line.
        return "UserEntity [id=" + id + ", fullName=" + fullName + ", email=" + email + ", username=" + username
                + ", role=" + role + ", failedAttempts=" + failedAttempts
                + ", accountNonLocked=" + accountNonLocked + ", lockTime=" + lockTime + "]";
    }
}
