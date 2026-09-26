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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.TransactionEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Entity.orderTransactionDetails;
import com.example.BankManagement.Repository.TransactionRepository;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.EmailService;
import com.example.BankManagement.Service.orderServices;
import com.example.BankManagement.dto.BankAccountDTO;
import com.example.BankManagement.dto.DepositInitiateRequest;
import com.example.BankManagement.dto.DepositVerifyRequest;
import com.example.BankManagement.dto.TransactionDTO;
import com.example.BankManagement.dto.WithdrawRequest;

import jakarta.validation.Valid;

/**
 * JSON equivalent of BankController's deposit/withdraw flow and paymentTransactionController's
 * Razorpay order creation, for the React frontend. Reuses BankService, TransactionRepository and
 * orderServices without duplicating their logic.
 *
 * Identity comes from the validated JWT (Authentication), never a client-supplied email -
 * these endpoints move real money in/out of an account, so deposit/verify/withdraw used to
 * let anyone act on any account just by naming its email in the request body.
 *
 * Unlike the Thymeleaf deposit flow (Bank_Deposite.html), which only creates a Razorpay order and
 * never verifies the payment or credits the balance, this controller also verifies the payment
 * signature and completes the deposit - that piece didn't exist anywhere in the app before.
 */
@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("hasRole('USER')")
public class TransactionApiController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BankService bankService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private orderServices orderServices;

    @Autowired
    private EmailService emailService;

    @GetMapping("/history")
    public ResponseEntity<?> history(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserEntity user = userRepo.findByEmail(email);
            if (user == null) {
                return error(HttpStatus.NOT_FOUND, "User not found");
            }

            List<TransactionDTO> transactions = transactionRepository.findByEmailOrderByTimestampDesc(email).stream()
                    .map(TransactionDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while loading transaction history.");
        }
    }

    @PostMapping("/deposit/initiate")
    public ResponseEntity<?> initiateDeposit(@Valid @RequestBody DepositInitiateRequest request, BindingResult bindingResult,
                                              Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        try {
            BankEntity bankAccount = requireBankAccount(authentication.getName());
            if (bankAccount == null) {
                return error(HttpStatus.NOT_FOUND, "Bank account not found for this user.");
            }
            ResponseEntity<?> kycBlock = kycGate(bankAccount);
            if (kycBlock != null) return kycBlock;

            orderTransactionDetails order = orderServices.orderCreateTransaction(request.getAmount());
            if (order == null) {
                return error(HttpStatus.BAD_GATEWAY, "Failed to create payment order.");
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while initiating the deposit.");
        }
    }

    @PostMapping("/deposit/verify")
    public ResponseEntity<?> verifyDeposit(@Valid @RequestBody DepositVerifyRequest request, BindingResult bindingResult,
                                            Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        try {
            String email = authentication.getName();
            BankEntity bankAccount = requireBankAccount(email);
            if (bankAccount == null) {
                return error(HttpStatus.NOT_FOUND, "Bank account not found for this user.");
            }
            ResponseEntity<?> kycBlock = kycGate(bankAccount);
            if (kycBlock != null) return kycBlock;

            boolean valid = orderServices.verifyPayment(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature());

            if (!valid) {
                return error(HttpStatus.UNAUTHORIZED, "Payment verification failed.");
            }

            bankAccount.setBalance(bankAccount.getBalance() + request.getAmount());
            bankService.updateBankAccount(bankAccount);

            TransactionEntity txn = new TransactionEntity();
            txn.setEmail(email);
            txn.setType("Deposit");
            txn.setAmount(request.getAmount());
            transactionRepository.save(txn);

            try {
                emailService.sendEmail(email, "Deposit Confirmation - SYS Bank",
                        "Dear " + bankAccount.getFirstname() + ",\n\nWe have successfully received your deposit of ₹" + request.getAmount() + ".\n" +
                                "Your updated account balance is ₹" + bankAccount.getBalance() + ".\n\n" +
                                "Thank you for banking with us.\n\nWarm regards,\nSYS Bank Team");
            } catch (Exception emailEx) {
                emailEx.printStackTrace();
            }

            return ResponseEntity.ok(BankAccountDTO.fromEntity(bankAccount));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while verifying the deposit.");
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawRequest request, BindingResult bindingResult,
                                       Authentication authentication) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(fieldErrors(bindingResult));
        }

        try {
            String email = authentication.getName();
            BankEntity bankAccount = requireBankAccount(email);
            if (bankAccount == null) {
                return error(HttpStatus.NOT_FOUND, "Bank account not found for this user.");
            }
            ResponseEntity<?> kycBlock = kycGate(bankAccount);
            if (kycBlock != null) return kycBlock;

            if (request.getAmount() > bankAccount.getBalance()) {
                return error(HttpStatus.BAD_REQUEST, "Insufficient balance.");
            }

            bankAccount.setBalance(bankAccount.getBalance() - request.getAmount());
            bankService.updateBankAccount(bankAccount);

            TransactionEntity txn = new TransactionEntity();
            txn.setEmail(email);
            txn.setType("Withdraw");
            txn.setAmount(request.getAmount());
            transactionRepository.save(txn);

            try {
                emailService.sendEmail(email, "Withdrawal Confirmation - SYS Bank",
                        "Dear " + bankAccount.getFirstname() + ",\n\nYour withdrawal of ₹" + request.getAmount() +
                                " was successful.\nRemaining balance: ₹" + bankAccount.getBalance());
            } catch (Exception emailEx) {
                emailEx.printStackTrace();
            }

            return ResponseEntity.ok(BankAccountDTO.fromEntity(bankAccount));
        } catch (Exception e) {
            e.printStackTrace();
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while processing the withdrawal.");
        }
    }

    private BankEntity requireBankAccount(String email) {
        UserEntity user = userRepo.findByEmail(email);
        if (user == null) {
            return null;
        }
        return bankService.getBankAccountByEmail(email);
    }

    /**
     * Blocks money movement until an admin has verified this account's KYC documents.
     * Returns null (no block) when kycStatus is VERIFIED, otherwise a ready-to-return
     * error response explaining why.
     */
    private ResponseEntity<?> kycGate(BankEntity bankAccount) {
        if ("VERIFIED".equalsIgnoreCase(bankAccount.getKycStatus())) {
            return null;
        }
        if ("REJECTED".equalsIgnoreCase(bankAccount.getKycStatus())) {
            String reason = bankAccount.getKycRejectionReason();
            return error(HttpStatus.FORBIDDEN, "Your account verification was rejected"
                    + (reason != null && !reason.isBlank() ? " (" + reason + ")" : "")
                    + ". Please contact support or resubmit your documents to continue.");
        }
        return error(HttpStatus.FORBIDDEN,
                "Your account verification is still pending review. Please wait for an admin to verify your documents before performing this action.");
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
