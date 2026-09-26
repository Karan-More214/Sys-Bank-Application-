package com.example.BankManagement.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.BankManagement.Entity.LoanEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.LoanRepo;
import com.example.BankManagement.Repository.UserRepo;

@Service
@Transactional
public class LoanService {
    
    @Autowired
    private LoanRepo loanRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepo userRepo;

    
    
    /**
     * Save a new loan application
     */
    @Transactional
    public LoanEntity saveLoan(LoanEntity loan) {
        System.out.println("=== LOAN SERVICE DEBUG ===");
        System.out.println("About to save loan: " + loan.toString());
        
        try {
            // Validate the loan entity
            if (loan == null) {
                throw new IllegalArgumentException("Loan entity cannot be null");
            }
            
            if (loan.getUser() == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            
            if (loan.getFirstname() == null || loan.getFirstname().trim().isEmpty()) {
                throw new IllegalArgumentException("First name is required");
            }
            
            if (loan.getLastname() == null || loan.getLastname().trim().isEmpty()) {
                throw new IllegalArgumentException("Last name is required");
            }
            
            if (loan.getEmail() == null || loan.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            
            if (loan.getLoanAmount() == null || loan.getLoanAmount() <= 0) {
                throw new IllegalArgumentException("Valid loan amount is required");
            }
            
            if (loan.getLoanYears() == null || loan.getLoanYears() <= 0) {
                throw new IllegalArgumentException("Valid loan duration is required");
            }
            
            System.out.println("Validation passed. Saving loan...");
            
            LoanEntity savedLoan = loanRepository.save(loan);
            
            if (savedLoan != null && savedLoan.getId() > 0) {
                System.out.println("Successfully saved loan with ID: " + savedLoan.getId());
                System.out.println("=== END LOAN SERVICE DEBUG ===");
                return savedLoan;
            } else {
                throw new RuntimeException("Failed to save loan - no ID generated");
            }
            
        } catch (Exception e) {
            System.out.println("Error saving loan: " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== END LOAN SERVICE ERROR ===");
            throw new RuntimeException("Failed to save loan application: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all loans for a specific user
     */
    @Transactional(readOnly = true)
    public List<LoanEntity> getLoansByUser(UserEntity user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return loanRepository.findByUser(user);
    }
    
    /**
     * Get all loans (for admin)
     */
    @Transactional(readOnly = true)
    public List<LoanEntity> getAllLoans() {
        return loanRepository.findAll();
    }
    
    /**
     * Get loan by ID
     */
    @Transactional(readOnly = true)
    public LoanEntity getLoanById(int id) {
        return loanRepository.findById(id).orElse(null);
    }
    
    /**
     * Update loan details
     */
    @Transactional
    public LoanEntity updateLoan(LoanEntity loan) {
        if (loan == null || loan.getId() <= 0) {
            throw new IllegalArgumentException("Invalid loan entity for update");
        }
        return loanRepository.save(loan);
    }
    
    /**
     * Delete loan by ID
     */
    @Transactional
    public void deleteLoan(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid loan ID");
        }
        loanRepository.deleteById(id);
    }
    
    /**
     * Get loans by email
     */
    @Transactional(readOnly = true)
    public List<LoanEntity> getLoansByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return loanRepository.findByEmail(email.trim().toLowerCase());
    }
    
    public void updateLoanStatus(int loanId, String status) {
        Optional<LoanEntity> optionalLoan = loanRepository.findById(loanId);
        
        if (optionalLoan.isPresent()) {
            LoanEntity loan = optionalLoan.get();
            loan.setStatus(status); // PENDING, APPROVED, REJECTED
            loanRepository.save(loan);
        } else {
            throw new RuntimeException("Loan with ID " + loanId + " not found");
        }
    }
    
    public void TrackLoanStatus(int loanId, String status) {
        LoanEntity loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setStatus(status);
        loanRepository.save(loan);

        String userEmail = loan.getUser().getEmail();
        String subject = "Loan Application " + status;
        String htmlMessage = "<html><body>" +
        	    "<h2>Dear " + loan.getFirstname() + ",</h2>" +
        	    "<p>Your loan application has been <strong style='color:blue'>" + status.toLowerCase() + "</strong>.</p>" +
        	    "<p><b>Loan Details:</b><br>" +
        	    "Loan Type: " + loan.getLoanType() + "<br>" +
        	    "Loan Amount: ₹" + loan.getLoanAmount() + "<br>" +
        	    "Duration: " + loan.getLoanYears() + " years</p>" +
        	    "<br><p>Thank you for choosing <b>SysBank</b>.<br><br>" +
        	    "Best regards,<br>SysBank Team</p>" +
        	    "</body></html>";

        	emailService.sendEmail(userEmail, subject, htmlMessage);

    }
    
    
    
    

    	
	
}