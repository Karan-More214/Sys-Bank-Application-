package com.example.BankManagement.Controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.TransferEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.BankRepo;
import com.example.BankManagement.Repository.TransferRepository;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Service.TransferService;
import com.example.BankManagement.Service.TransferService.TransferResult;
import com.example.BankManagement.dto.TransferDTO;
import com.example.BankManagement.dto.TransferInitiateRequest;
import com.example.BankManagement.dto.TransferStatusPatchRequest;

import jakarta.validation.Valid;

/**
 * Money transfer REST API. User endpoints initiate/view own transfers; admin
 * endpoints list/filter all transfers and approve or reverse them. Identity comes
 * from the validated JWT (Authentication), never a client-supplied email - initiate()
 * in particular used to accept a client-supplied "email" naming whose account to debit,
 * which meant anyone could move funds out of any account by naming it there.
 */
@RestController
@RequestMapping("/api/transfers")
public class TransferApiController {

    private static final List<String> VALID_STATUS_FILTERS = List.of("COMPLETED", "FLAGGED", "REVERSED");

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BankRepo bankRepo;

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferRepository transferRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> initiate(@Valid @RequestBody TransferInitiateRequest request, BindingResult bindingResult,
                                       Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        UserEntity user = userRepo.findByEmail(authentication.getName());
        if (user == null) {
            return error(HttpStatus.NOT_FOUND, "User not found");
        }

        String recipientEmail = resolveRecipientEmail(request.getRecipient());
        if (recipientEmail == null) {
            return error(HttpStatus.NOT_FOUND, "No SysBank account found for that recipient.");
        }

        try {
            TransferResult result = transferService.initiate(user.getEmail(), recipientEmail, request.getAmount(), request.getNote());
            if (result.error != null) {
                return error(HttpStatus.BAD_REQUEST, result.error);
            }
            HttpStatus status = "FLAGGED".equals(result.transfer.getStatus()) ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
            return ResponseEntity.status(status).body(TransferDTO.fromEntity(result.transfer));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while processing the transfer.");
        }
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> myTransfers(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userRepo.findByEmail(email);
        if (user == null) {
            return error(HttpStatus.NOT_FOUND, "User not found");
        }

        return ResponseEntity.ok(myTransferList(email));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listAll(@RequestParam(value = "status", required = false) String status) {
        if (status != null && !VALID_STATUS_FILTERS.contains(status)) {
            return error(HttpStatus.BAD_REQUEST, "Invalid status filter");
        }

        return ResponseEntity.ok(listTransfers(status));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id,
                                           @Valid @RequestBody TransferStatusPatchRequest request,
                                           BindingResult bindingResult,
                                           Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        UserEntity admin = userRepo.findByEmail(authentication.getName());
        if (admin == null) {
            return error(HttpStatus.NOT_FOUND, "Admin user not found");
        }

        String target = request.getNewStatus() == null ? "" : request.getNewStatus().toUpperCase();

        try {
            TransferResult result;
            if ("COMPLETED".equals(target)) {
                result = transferService.approve(id);
            } else if ("REVERSED".equals(target)) {
                result = transferService.reverse(id, request.getReason());
            } else {
                return error(HttpStatus.BAD_REQUEST, "newStatus must be COMPLETED (approve) or REVERSED (reverse)");
            }

            if (result.error != null) {
                HttpStatus status = result.error.contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
                return error(status, result.error);
            }

            transferService.setReviewer(result.transfer, admin);
            return ResponseEntity.ok(TransferDTO.fromEntity(result.transfer));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating the transfer.");
        }
    }

    /**
     * Accepts either a direct email or a "SYB{id}"/numeric account number
     * (the same derived account-number format shown on the Profile page).
     */
    private String resolveRecipientEmail(String recipient) {
        if (recipient == null) return null;
        String trimmed = recipient.trim();

        if (trimmed.contains("@")) {
            BankEntity account = bankRepo.findByEmail(trimmed.toLowerCase());
            return account != null ? account.getEmail() : null;
        }

        String digits = trimmed.toUpperCase().startsWith("SYB") ? trimmed.substring(3) : trimmed;
        try {
            Integer accountId = Integer.parseInt(digits.trim());
            return bankRepo.findById(accountId).map(BankEntity::getEmail).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<TransferDTO> myTransferList(String email) {
        return transferRepository.findByFromEmailOrToEmailOrderByCreatedAtDesc(email, email).stream()
                .map(TransferDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private List<TransferDTO> listTransfers(String status) {
        List<TransferEntity> transfers = status != null
                ? transferRepository.findByStatusOrderByCreatedAtDesc(status)
                : transferRepository.findAllByOrderByCreatedAtDesc();
        return transfers.stream().map(TransferDTO::fromEntity).collect(Collectors.toList());
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
