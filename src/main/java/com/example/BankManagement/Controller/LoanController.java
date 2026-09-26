package com.example.BankManagement.Controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.LoanEntity;
import com.example.BankManagement.Entity.LoanEntity.LoanStatusUpdateRequest;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.EmailService;
import com.example.BankManagement.Service.LoanService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/loan")
public class LoanController {
    
    @Autowired
    private LoanService loanService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private BankService bankService;

    
    /**
     * Show Loan Application Form
     */
    @GetMapping("/apply")
    public String showLoanForm(HttpSession session, Model model) {
        UserEntity user = (UserEntity) session.getAttribute("loggedUser");
        
        if (user != null && "User".equalsIgnoreCase(user.getRole())) {
            model.addAttribute("user", user);
            return "LoanForm";
        }
        return "redirect:/main/login";
    }
    
    /**
     * Submit Loan Application
     */
    @PostMapping("/submit")
    public String submitLoanApplication(
            @RequestParam("Firstname") String firstname,
            @RequestParam("Lastname") String lastname,
            @RequestParam("dob") String dobString,
            @RequestParam("Email") String email,
            @RequestParam("PhoneNo") String phoneNo,
            @RequestParam("Address") String address,
            @RequestParam("loanType") String loanType,
            @RequestParam("loanAmount") Double loanAmount,
            @RequestParam("loanYears") Integer loanYears,
            @RequestParam("employmentType") String employmentType,
            @RequestParam("monthlyIncome") Double monthlyIncome,
            @RequestParam("purpose") String purpose,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        UserEntity user = (UserEntity) session.getAttribute("loggedUser");
        
        if (user == null || !"User".equalsIgnoreCase(user.getRole())) {
            return "redirect:/main/login";
        }
        
        try {
            // Debug logging
            System.out.println("=== LOAN APPLICATION DEBUG ===");
            System.out.println("User: " + user.getUsername());
            System.out.println("Firstname: " + firstname);
            System.out.println("Lastname: " + lastname);
            System.out.println("DOB String: " + dobString);
            System.out.println("Email: " + email);
            System.out.println("Phone: " + phoneNo);
            System.out.println("Address: " + address);
            System.out.println("Loan Type: " + loanType);
            System.out.println("Loan Amount: " + loanAmount);
            System.out.println("Loan Years: " + loanYears);
            System.out.println("Employment Type: " + employmentType);
            System.out.println("Monthly Income: " + monthlyIncome);
            System.out.println("Purpose: " + purpose);
            
            // Validate required fields
            if (firstname == null || firstname.trim().isEmpty() ||
                lastname == null || lastname.trim().isEmpty() ||
                dobString == null || dobString.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                phoneNo == null || phoneNo.trim().isEmpty() ||
                address == null || address.trim().isEmpty() ||
                loanType == null || loanType.trim().isEmpty() ||
                loanAmount == null || loanAmount <= 0 ||
                loanYears == null || loanYears <= 0 ||
                employmentType == null || employmentType.trim().isEmpty() ||
                monthlyIncome == null || monthlyIncome < 0 ||
                purpose == null || purpose.trim().isEmpty()) {
                
                redirectAttributes.addFlashAttribute("error", 
                    "All fields are required and must be valid");
                return "redirect:/loan/apply";
            }
            
            // Convert date string to LocalDate
            LocalDate dob = LocalDate.parse(dobString);
            System.out.println("Parsed DOB: " + dob);
            
            // Create new loan entity
            LoanEntity loan = new LoanEntity();
            loan.setFirstname(firstname.trim());
            loan.setLastname(lastname.trim());
            loan.setDob(dob);
            loan.setEmail(email.trim().toLowerCase());
            loan.setPhoneNo(phoneNo.trim());
            loan.setAddress(address.trim());
            loan.setLoanType(loanType.trim());
            loan.setLoanAmount(loanAmount);
            loan.setLoanYears(loanYears);
            loan.setEmploymentType(employmentType.trim());
            loan.setMonthlyIncome(monthlyIncome);
            loan.setPurpose(purpose.trim());
            loan.setUser(user);
            
            System.out.println("Created Loan Entity: " + loan.toString());
            
            // Save loan application
            LoanEntity savedLoan = loanService.saveLoan(loan);
            System.out.println("Saved Loan ID: " + savedLoan.getId());

            // Send email confirmation
            String subject = "Loan Application Submitted - Sys Bank";
            String body = "Dear " + firstname + ",<br><br>" +
                          "Your loan application has been submitted successfully!<br><br>" +
                          "<strong>Loan Details:</strong><br>" +
                          "Loan Type: " + loanType + "<br>" +
                          "Loan Amount: ₹" + loanAmount + "<br>" +
                          "Loan Duration: " + loanYears + " years<br>" +
                          "Application ID: " + savedLoan.getId() + "<br><br>" +
                          "We will review your application and contact you soon.<br><br>" +
                          "Regards,<br>Sys Bank";

            emailService.sendEmail(email, subject, body);

            System.out.println("=== END DEBUG ===");

            redirectAttributes.addFlashAttribute("success", 
                "Loan application submitted successfully! Application ID: " + savedLoan.getId());
            
            return "redirect:/loan/myloans";
            
        } catch (Exception e) {
            System.out.println("=== ERROR OCCURRED ===");
            e.printStackTrace();
            System.out.println("=== END ERROR ===");
            
            redirectAttributes.addFlashAttribute("error", 
                "Error submitting loan application: " + e.getMessage());
            
            return "redirect:/loan/apply";
        }
    }
    
    /**
     * View User's Loan Applications
     */
    @GetMapping("/myloans")
    public String viewMyLoans(HttpSession session, Model model) {
        UserEntity user = (UserEntity) session.getAttribute("loggedUser");
        
        if (user != null && "User".equalsIgnoreCase(user.getRole())) {
            model.addAttribute("loans", loanService.getLoansByUser(user));
            model.addAttribute("user", user);
            return "MyLoans";
        }
        return "redirect:/main/login";
    }
    
    @GetMapping("/bank/loan")
    public String showLoanPage(@RequestParam("email") String email, Model model) {
        BankEntity bankAccount = bankService.getBankAccountByEmail(email);

        if (bankAccount == null) {
            model.addAttribute("message", "Please open a bank account before applying for a loan.");
            model.addAttribute("messageType", "danger");
            return "redirect:/bank/main?email=" + email;
        }

        model.addAttribute("userEmail", email);
        model.addAttribute("bankAccount", bankAccount);
        return "loan-form"; // Your loan form page
    }
    
	@GetMapping("/user/loans")
	public String getUserLoans(Model model, Principal principal) {
	    String email = principal.getName();
	    List<LoanEntity> loans = loanService.getLoansByEmail(email);
	    model.addAttribute("loans", loans);
	    return "user-loans";  // HTML page name
	}
	
	@PostMapping("/update-loan-status")
	public ResponseEntity<String> updateLoanStatus(@RequestBody LoanStatusUpdateRequest request) {
	    loanService.updateLoanStatus(request.getLoanId(), request.getStatus());
	    return ResponseEntity.ok("Loan status updated successfully");
	}


    
    
}