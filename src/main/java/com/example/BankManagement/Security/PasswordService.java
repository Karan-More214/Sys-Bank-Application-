package com.example.BankManagement.Security;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Supports the transition from plaintext-stored passwords to BCrypt. Every UserEntity
 * row created from now on stores a BCrypt hash (see encode()); rows created before this
 * change still hold plaintext until that account's next successful login, at which point
 * the caller re-hashes and saves it (see AuthApiController/AuthController login()).
 *
 * isBcryptHash() is how a caller tells which compare path a given stored value needs -
 * it is not a security check, just a format sniff.
 */
@Service
public class PasswordService {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    // Same complexity rule RegisterRequest already enforces at the DTO layer for the
    // REST path; used here to give the same guarantee to the paths that don't go
    // through that DTO (Thymeleaf register, profile password change).
    private static final Pattern COMPLEXITY_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean isBcryptHash(String value) {
        return value != null && BCRYPT_PATTERN.matcher(value).matches();
    }

    public boolean isComplexEnough(String rawPassword) {
        return rawPassword != null && COMPLEXITY_PATTERN.matcher(rawPassword).matches();
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }
}
