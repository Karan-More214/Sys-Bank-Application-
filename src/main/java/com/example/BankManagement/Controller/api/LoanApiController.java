package com.example.BankManagement.Controller.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Controller.EmailController;
import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.LoanEntity;
import com.example.BankManagement.Entity.TransactionEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.TransactionRepository;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.EmailService;
import com.example.BankManagement.Service.LoanService;
import com.example.BankManagement.dto.LoanApplicationRequest;
import com.example.BankManagement.dto.LoanDTO;
import com.example.BankManagement.dto.LoanStatusPatchRequest;

import jakarta.validation.Valid;

/**
 * JSON equivalent of LoanController's apply/myloans flow, for the React frontend.
 * Reuses the existing LoanService/UserRepo without modifying them.
 */
@RestController
@RequestMapping("/api/loans")
public class LoanApiController {

    private static final List<String> ASSIGNABLE_STATUSES = List.of("APPROVED", "REJECTED", "REPAID");

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BankService bankService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EmailController emailController;

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> history(Authentication authentication) {
        try {
            UserEntity user = userRepo.findByEmail(authentication.getName());

            if (user == null) {
                return error(HttpStatus.NOT_FOUND, "User not found");
            }

            List<LoanDTO> loans = loanService.getLoansByUser(user).stream()
                    .map(LoanDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while loading loan history.");
        }
    }

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> apply(@Valid @RequestBody LoanApplicationRequest request, BindingResult bindingResult,
                                    Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        try {
            String email = authentication.getName();
            UserEntity user = userRepo.findByEmail(email);

            if (user == null) {
                return error(HttpStatus.NOT_FOUND, "User not found");
            }

            BankEntity bankAccount = bankService.getBankAccountByEmail(email);
            if (bankAccount != null && !"VERIFIED".equalsIgnoreCase(bankAccount.getKycStatus())) {
                if ("REJECTED".equalsIgnoreCase(bankAccount.getKycStatus())) {
                    String reason = bankAccount.getKycRejectionReason();
                    return error(HttpStatus.FORBIDDEN, "Your account verification was rejected"
                            + (reason != null && !reason.isBlank() ? " (" + reason + ")" : "")
                            + ". Please contact support or resubmit your documents before applying for a loan.");
                }
                return error(HttpStatus.FORBIDDEN,
                        "Your account verification is still pending review. Please wait for an admin to verify your documents before applying for a loan.");
            }

            LocalDate dob;
            try {
                dob = LocalDate.parse(request.getDob());
            } catch (Exception e) {
                return error(HttpStatus.BAD_REQUEST, "Invalid date of birth format, expected YYYY-MM-DD");
            }

            LoanEntity loan = new LoanEntity();
            loan.setFirstname(request.getFirstname().trim());
            loan.setLastname(request.getLastname().trim());
            loan.setDob(dob);
            loan.setEmail(email.trim().toLowerCase());
            loan.setPhoneNo(request.getPhoneNo().trim());
            loan.setAddress(request.getAddress().trim());
            loan.setLoanType(request.getLoanType().trim());
            loan.setLoanAmount(request.getLoanAmount());
            loan.setLoanYears(request.getLoanYears());
            loan.setEmploymentType(request.getEmploymentType().trim());
            loan.setMonthlyIncome(request.getMonthlyIncome());
            loan.setPurpose(request.getPurpose().trim());
            loan.setUser(user);

            LoanEntity savedLoan = loanService.saveLoan(loan);

            try {
                String subject = "Loan Application Submitted - Sys Bank";
                String body = "Dear " + loan.getFirstname() + ",<br><br>" +
                        "Your loan application has been submitted successfully!<br><br>" +
                        "<strong>Loan Details:</strong><br>" +
                        "Loan Type: " + loan.getLoanType() + "<br>" +
                        "Loan Amount: ₹" + loan.getLoanAmount() + "<br>" +
                        "Loan Duration: " + loan.getLoanYears() + " years<br>" +
                        "Application ID: " + savedLoan.getId() + "<br><br>" +
                        "We will review your application and contact you soon.<br><br>" +
                        "Regards,<br>Sys Bank";
                emailService.sendEmail(loan.getEmail(), subject, body);
            } catch (Exception emailEx) {
                emailEx.printStackTrace();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(LoanDTO.fromEntity(savedLoan));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while submitting your loan application.");
        }
    }

    /**
     * Single, guarded state-transition endpoint for admin loan actions.
     * Valid transitions: PENDING -> APPROVED | REJECTED, APPROVED -> REPAID.
     * Anything else (re-approving a rejected loan, rejecting an already-repaid
     * loan, etc.) is refused. "REPAID" is not a separate LoanEntity.status value -
     * it maps to status=APPROVED with paid=true, matching the existing schema
     * and what LoanDTO/the frontend already treat as "Repaid".
     *
     * Money movement (confirmed, not guessed): approving a loan credits the loan
     * amount into the borrower's BankEntity balance; marking a loan repaid debits
     * that same amount back out and records a "Loan Repayment" transaction.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable("id") int id,
                                           @Valid @RequestBody LoanStatusPatchRequest request,
                                           BindingResult bindingResult,
                                           Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        String targetStatus = request.getNewStatus() == null ? "" : request.getNewStatus().toUpperCase();
        if (!ASSIGNABLE_STATUSES.contains(targetStatus)) {
            return error(HttpStatus.BAD_REQUEST, "Invalid status: must be one of " + ASSIGNABLE_STATUSES);
        }

        LoanEntity loan = loanService.getLoanById(id);
        if (loan == null) {
            return error(HttpStatus.NOT_FOUND, "Loan not found");
        }

        String currentEffective = effectiveStatus(loan);
        String loanOwnerEmail = loan.getUser() != null ? loan.getUser().getEmail() : loan.getEmail();

        try {
            if ("PENDING".equals(currentEffective) && "APPROVED".equals(targetStatus)) {
                loan.setStatus("APPROVED");
                loan.setDecidedByAdminEmail(authentication.getName());
                loan.setDecisionDate(LocalDateTime.now());
                BankEntity bankAccount = bankService.getBankAccountByEmail(loanOwnerEmail);
                if (bankAccount != null) {
                    bankAccount.setBalance(bankAccount.getBalance() + loan.getLoanAmount());
                    bankService.updateBankAccount(bankAccount);
                }
                loanService.updateLoan(loan);
                safeSendStatusEmail(loan, "APPROVED");
            } else if ("PENDING".equals(currentEffective) && "REJECTED".equals(targetStatus)) {
                loan.setStatus("REJECTED");
                loan.setDecidedByAdminEmail(authentication.getName());
                loan.setDecisionDate(LocalDateTime.now());
                loan.setRejectionReason(request.getReason() != null && !request.getReason().isBlank() ? request.getReason().trim() : null);
                loanService.updateLoan(loan);
                safeSendStatusEmail(loan, "REJECTED");
            } else if ("APPROVED".equals(currentEffective) && "REPAID".equals(targetStatus)) {
                BankEntity bankAccount = bankService.getBankAccountByEmail(loanOwnerEmail);
                if (bankAccount == null) {
                    return error(HttpStatus.NOT_FOUND, "Bank account not found for this borrower");
                }
                if (loan.getLoanAmount() > bankAccount.getBalance()) {
                    return error(HttpStatus.BAD_REQUEST, "Insufficient balance to mark this loan as repaid");
                }
                bankAccount.setBalance(bankAccount.getBalance() - loan.getLoanAmount());
                bankService.updateBankAccount(bankAccount);

                TransactionEntity txn = new TransactionEntity();
                txn.setEmail(loanOwnerEmail);
                txn.setType("Loan Repayment");
                txn.setAmount(loan.getLoanAmount());
                transactionRepository.save(txn);

                loan.setPaid(true);
                loanService.updateLoan(loan);
            } else {
                return error(HttpStatus.BAD_REQUEST,
                        "Cannot change a " + currentEffective + " loan to " + targetStatus);
            }

            return ResponseEntity.ok(LoanDTO.fromEntity(loan));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating the loan.");
        }
    }

    private String effectiveStatus(LoanEntity loan) {
        if ("APPROVED".equalsIgnoreCase(loan.getStatus())) {
            return loan.isPaid() ? "REPAID" : "APPROVED";
        }
        return loan.getStatus() == null ? "" : loan.getStatus().toUpperCase();
    }

    private void safeSendStatusEmail(LoanEntity loan, String status) {
        try {
            emailController.sendLoanStatusEmail(loan.getEmail(), status, loan.getFirstname(), loan.getLoanType(), loan.getLoanAmount());
        } catch (Exception emailEx) {
            emailEx.printStackTrace();
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
