package com.example.BankManagement.Controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.TransactionEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.TransactionRepository;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Security.JwtService;
import com.example.BankManagement.Security.PasswordService;
import com.example.BankManagement.Service.UserService;
import com.example.BankManagement.dto.BankAccountDTO;
import com.example.BankManagement.dto.DashboardResponse;
import com.example.BankManagement.dto.ProfileUpdateRequest;
import com.example.BankManagement.dto.TransactionDTO;
import com.example.BankManagement.dto.UserDTO;

import jakarta.validation.Valid;

/**
 * JSON equivalent of UserController's /user/home dashboard, for the React frontend.
 * Reuses the existing UserService/UserRepo/TransactionRepository without modifying them.
 */
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserApiController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordService passwordService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication authentication) {
        try {
            UserEntity user = userRepo.findByEmail(authentication.getName());

            if (user == null) {
                Map<String, String> body = new HashMap<>();
                body.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            }

            BankEntity bankAccount = userService.getBankAccountByEmail(user.getEmail());
            List<TransactionDTO> transactions;

            if (bankAccount != null) {
                List<TransactionEntity> txns = transactionRepository.findTop5ByEmailOrderByTimestampDesc(user.getEmail());
                transactions = txns.stream().map(TransactionDTO::fromEntity).collect(Collectors.toList());
            } else {
                transactions = List.of();
            }

            DashboardResponse response = new DashboardResponse(
                    UserDTO.fromEntity(user),
                    BankAccountDTO.fromEntity(bankAccount),
                    transactions
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> body = new HashMap<>();
            body.put("message", "An unexpected error occurred while loading the dashboard.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request, BindingResult bindingResult,
                                            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError fe : bindingResult.getFieldErrors()) {
                errors.put(fe.getField(), fe.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            UserEntity user = userRepo.findByEmail(authentication.getName());
            if (user == null) {
                Map<String, String> body = new HashMap<>();
                body.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            }

            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                // ProfileUpdateRequest.password has no @Pattern (it's optional - blank
                // means "keep current"), so complexity is checked here when one is given.
                if (!passwordService.isComplexEnough(request.getPassword())) {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("password", "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character");
                    return ResponseEntity.badRequest().body(errors);
                }
                user.setPassword(passwordService.encode(request.getPassword()));
            }

            UserEntity updated = userService.updateUser(user);

            // The JWT's subject is the email at login time; if this update changed the
            // email, the old token would no longer resolve to any row on the next
            // request. Reissue one bound to the (possibly new) email so the session
            // survives an email change.
            UserDTO dto = UserDTO.fromEntity(updated);
            dto.setToken(jwtService.generateToken(updated.getEmail(), updated.getRole()));
            return ResponseEntity.ok(dto);
        } catch (DataIntegrityViolationException e) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "Email or username already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> body = new HashMap<>();
            body.put("message", "An unexpected error occurred while updating your profile.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
}
