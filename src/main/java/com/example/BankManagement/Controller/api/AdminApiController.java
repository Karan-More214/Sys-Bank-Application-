package com.example.BankManagement.Controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.LoanService;
import com.example.BankManagement.dto.AdminCustomerUpdateRequest;
import com.example.BankManagement.dto.BankAccountDTO;
import com.example.BankManagement.dto.KycStatusPatchRequest;
import com.example.BankManagement.dto.LoanDTO;

import jakarta.validation.Valid;

/**
 * JSON equivalent of AdminController's customer/loan management, for the React frontend.
 * Reuses BankService/LoanService without duplicating their logic.
 *
 * Access control: every method here requires a validated JWT with the ADMIN role
 * (enforced class-wide by @PreAuthorize below, checked against the role claim in the
 * token via SecurityConfig/JwtAuthenticationFilter) - not a client-supplied email.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    @Autowired
    private BankService bankService;

    @Autowired
    private LoanService loanService;

    @GetMapping("/customers")
    public ResponseEntity<?> listCustomers() {
        List<BankAccountDTO> customers = bankService.getAllBankAccounts().stream()
                .map(BankAccountDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable("id") Integer id) {
        BankEntity customer = bankService.getBankAccountById(id);
        if (customer == null) {
            return error(HttpStatus.NOT_FOUND, "Customer not found");
        }
        return ResponseEntity.ok(BankAccountDTO.fromEntity(customer));
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable("id") Integer id,
                                             @Valid @RequestBody AdminCustomerUpdateRequest request,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        BankEntity customer = bankService.getCustomerbyId(id);
        if (customer == null) {
            return error(HttpStatus.NOT_FOUND, "Customer not found");
        }

        customer.setFirstname(request.getFirstname());
        customer.setLastname(request.getLastname());
        customer.setEmail(request.getEmail());
        if (request.getPhoneNo() != null) customer.setPhoneNo(request.getPhoneNo());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getAccountType() != null) customer.setAccountType(request.getAccountType());
        if (request.getAccountStatus() != null) customer.setAccountStatus(request.getAccountStatus());
        if (request.getBalance() != null) customer.setBalance(request.getBalance());

        bankService.UpdateCustomer(customer);
        return ResponseEntity.ok(BankAccountDTO.fromEntity(customer));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable("id") int id) {
        BankEntity customer = bankService.getCustomerbyId(id);
        if (customer == null) {
            return error(HttpStatus.NOT_FOUND, "Customer not found");
        }
        bankService.DeleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/loans")
    public ResponseEntity<?> listLoans() {
        List<LoanDTO> loans = loanService.getAllLoans().stream()
                .map(LoanDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loans);
    }

    // Loan status changes (approve/reject/mark-paid) now live at
    // PATCH /api/loans/{id}/status in LoanApiController, with proper state-transition
    // guards - see that controller for the single source of truth on loan state changes.

    private static final List<String> ASSIGNABLE_KYC_STATUSES = List.of("VERIFIED", "REJECTED");

    @GetMapping("/kyc/pending")
    public ResponseEntity<?> listPendingKyc() {
        List<BankAccountDTO> pending = bankService.getAllBankAccounts().stream()
                .filter(a -> "PENDING".equalsIgnoreCase(a.getKycStatus()))
                .map(BankAccountDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pending);
    }

    @PatchMapping("/kyc/{id}/status")
    public ResponseEntity<?> updateKycStatus(@PathVariable("id") Integer id,
                                              @Valid @RequestBody KycStatusPatchRequest request,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        String targetStatus = request.getNewStatus() == null ? "" : request.getNewStatus().toUpperCase();
        if (!ASSIGNABLE_KYC_STATUSES.contains(targetStatus)) {
            return error(HttpStatus.BAD_REQUEST, "Invalid status: must be one of " + ASSIGNABLE_KYC_STATUSES);
        }

        BankEntity customer = bankService.getCustomerbyId(id);
        if (customer == null) {
            return error(HttpStatus.NOT_FOUND, "Customer not found");
        }
        if (!"PENDING".equalsIgnoreCase(customer.getKycStatus())) {
            return error(HttpStatus.BAD_REQUEST,
                    "This account's KYC is already " + customer.getKycStatus() + ", not pending review.");
        }

        customer.setKycStatus(targetStatus);
        customer.setKycRejectionReason("REJECTED".equals(targetStatus) && request.getReason() != null && !request.getReason().isBlank()
                ? request.getReason().trim() : null);

        bankService.UpdateCustomer(customer);
        return ResponseEntity.ok(BankAccountDTO.fromEntity(customer));
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
