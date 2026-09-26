package com.example.BankManagement.Controller.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.Role;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Security.JwtService;
import com.example.BankManagement.Security.PasswordService;
import com.example.BankManagement.Service.EmailService;
import com.example.BankManagement.dto.LoginRequest;
import com.example.BankManagement.dto.RegisterRequest;
import com.example.BankManagement.dto.UserDTO;

import jakarta.validation.Valid;

/**
 * JSON equivalent of AuthController's login/register flow, for the React frontend.
 * Mirrors the same business rules (lockout after 3 failed attempts, 24h auto-unlock)
 * without touching the existing Thymeleaf controller. Passwords are BCrypt-hashed on
 * register; login format-detects and lazily migrates any still-plaintext row on its
 * next successful login (see PasswordService).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private UserRepo repo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        try {
            UserEntity user = repo.findByEmail(request.getEmail());

            if (user == null) {
                return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            }

            if (!user.isAccountNonLocked()) {
                LocalDateTime lockTime = user.getLockTime();

                if (lockTime != null && lockTime.plusHours(24).isBefore(LocalDateTime.now())) {
                    user.setAccountNonLocked(true);
                    user.setFailedAttempts(0);
                    user.setLockTime(null);
                    repo.save(user);
                } else {
                    return error(HttpStatus.LOCKED, "Your account is locked. Please try again after 24 hours.");
                }
            }

            boolean passwordOk;
            if (passwordService.isBcryptHash(user.getPassword())) {
                passwordOk = passwordService.matches(request.getPassword(), user.getPassword());
            } else {
                // Legacy plaintext row (predates BCrypt migration). If it matches, upgrade
                // it to a hash now so this account never compares in plaintext again.
                passwordOk = user.getPassword().equals(request.getPassword());
                if (passwordOk) {
                    user.setPassword(passwordService.encode(request.getPassword()));
                }
            }

            if (passwordOk) {
                user.setFailedAttempts(0);
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                repo.save(user);

                UserDTO dto = UserDTO.fromEntity(user);
                dto.setToken(jwtService.generateToken(user.getEmail(), user.getRole()));
                return ResponseEntity.ok(dto);
            }

            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= 3) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());

                try {
                    emailService.sendEmail(user.getEmail(),
                            "⚠️ Security Alert – Your SYS Bank Account Has Been Locked",
                            "Dear " + user.getFullName() + ",\n\nWe detected multiple failed login attempts on your SYS Bank account. "
                                    + "For your security your account has been temporarily locked for 24 hours.\n\nWarm Regards,\n🏦 SYS Bank Security Team");
                } catch (Exception emailEx) {
                    emailEx.printStackTrace();
                }
            }

            repo.save(user);

            return error(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while logging in.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        try {
            if (repo.findByEmail(request.getEmail()) != null) {
                return error(HttpStatus.CONFLICT, "Email already exists");
            }

            if (repo.findByUsername(request.getUsername()) != null) {
                return error(HttpStatus.CONFLICT, "Username already exists");
            }

            UserEntity user = new UserEntity();
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(passwordService.encode(request.getPassword()));
            // Server-computed only - never trust a role value from the client.
            user.setRole(Role.forEmail(user.getEmail()).name());

            repo.save(user);

            try {
                emailService.sendEmail(user.getEmail(),
                        "🎉 Welcome to SYS Bank – Account Registration Successful",
                        "Dear " + user.getFullName() + ",\n\nYour account has been successfully registered with SYS Bank.\n\n"
                                + "Before performing any banking operations please create your bank account in the system.\n\n"
                                + "Warm Regards,\n🏦 SYS Bank Team");
            } catch (Exception emailEx) {
                emailEx.printStackTrace();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(UserDTO.fromEntity(user));
        } catch (DataIntegrityViolationException e) {
            return error(HttpStatus.CONFLICT, "Email or username already exists");
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating your account.");
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, String> fieldErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : bindingResult.getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return errors;
    }
}
