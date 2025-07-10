package com.example.BankManagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.BankManagement.Entity.BankEntity;

import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.UserService;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequestMapping("/bank")
public class BankController {
    
    @Autowired
    private BankService bankService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/landing")
	public String mainpage() {
		return "bank_management_System";
	}
	
	@GetMapping("/update")
	public String updatecustomer() {
		return "Customer";
	}
	
	@GetMapping("/add")
	public String AccountOpeningForm(Model model)
	{
		model.addAttribute("Account", new BankEntity());
		return "add";
	}
	
	@PostMapping("/submit-form")
	public String SubmitForm(@ModelAttribute BankEntity bank)
	{
		bankService.SubmitForm(bank);
		return "redirect:/main/";
	}
    
    
    @GetMapping("/main")
    public String bankMain(@RequestParam("email") String email, Model model, HttpSession session) {
        try {
            // Get bank account by email
            BankEntity bankAccount = userService.getBankAccountByEmail(email);
            
            if (bankAccount != null) {
                model.addAttribute("bankAccount", bankAccount);
                model.addAttribute("userEmail", email);
                return "Bank_main";
            } else {
                model.addAttribute("error", "Bank account not found for email: " + email);
                return "redirect:/main/login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading bank account: " + e.getMessage());
            return "redirect:/main/login";
        }
    }
    
   
    @GetMapping("/deposit")
    public String showDepositPage(@RequestParam("email") String email, Model model) {
        try {
            BankEntity bankAccount = userService.getBankAccountByEmail(email);
            
            if (bankAccount != null) {
                model.addAttribute("bankAccount", bankAccount);
                model.addAttribute("userEmail", email);
                return "Bank_Deposite";
            } else {
                model.addAttribute("error", "Bank account not found");
                return "redirect:/bank/main?email=" + email;
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading deposit page: " + e.getMessage());
            return "redirect:/bank/main?email=" + email;
        }
    }
    
    
    @PostMapping("/deposit")
    public String processDeposit(
            @RequestParam("email") String email,
            @RequestParam("amount") double amount,
            @RequestParam(value = "method", required = false) String method,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate amount
            if (amount <= 0) {
                redirectAttributes.addFlashAttribute("message", "Invalid amount. Please enter a positive value.");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            if (amount < 10) {
                redirectAttributes.addFlashAttribute("message", "Minimum deposit amount is ₹10.00");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            if (amount > 100000) {
                redirectAttributes.addFlashAttribute("message", "Maximum deposit amount is ₹1,00,000.00");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            // Get bank account
            BankEntity bankAccount = userService.getBankAccountByEmail(email);
            
            if (bankAccount == null) {
                redirectAttributes.addFlashAttribute("message", "Bank account not found!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            // Process deposit
            double currentBalance = bankAccount.getBalance();
            double newBalance = currentBalance + amount;
            
            // Round to 2 decimal places
            BigDecimal roundedBalance = BigDecimal.valueOf(newBalance).setScale(2, RoundingMode.HALF_UP);
            bankAccount.setBalance(roundedBalance.doubleValue());
            
            // Save updated account
            bankService.updateBankAccount(bankAccount);
            
            // Success message
            redirectAttributes.addFlashAttribute("message", 
                String.format("Successfully deposited ₹%.2f. New balance: ₹%.2f", amount, bankAccount.getBalance()));
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Deposit failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/bank/main?email=" + email;
    }
    
    
    @GetMapping("/withdraw")
    public String showWithdrawPage(@RequestParam("email") String email, Model model) {
        try {
            BankEntity bankAccount = userService.getBankAccountByEmail(email);
            
            if (bankAccount != null) {
                model.addAttribute("bankAccount", bankAccount);
                model.addAttribute("userEmail", email);
                return "Bank_Withdrawl";
            } else {
                model.addAttribute("error", "Bank account not found");
                return "redirect:/bank/main?email=" + email;
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading withdrawal page: " + e.getMessage());
            return "redirect:/bank/main?email=" + email;
        }
    }
    
    
    @PostMapping("/withdraw")
    public String processWithdrawal(
            @RequestParam("email") String email,
            @RequestParam("amount") double amount,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate amount
            if (amount <= 0) {
                redirectAttributes.addFlashAttribute("message", "Invalid amount. Please enter a positive value.");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            if (amount < 1) {
                redirectAttributes.addFlashAttribute("message", "Minimum withdrawal amount is ₹1.00");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            // Get bank account
            BankEntity bankAccount = userService.getBankAccountByEmail(email);
            
            if (bankAccount == null) {
                redirectAttributes.addFlashAttribute("message", "Bank account not found!");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            // Check sufficient balance
            double currentBalance = bankAccount.getBalance();
            
            if (amount > currentBalance) {
                redirectAttributes.addFlashAttribute("message", 
                    String.format("Insufficient balance. Available: ₹%.2f, Requested: ₹%.2f", currentBalance, amount));
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/bank/main?email=" + email;
            }
            
            // Process withdrawal
            double newBalance = currentBalance - amount;
            
            // Round to 2 decimal places
            BigDecimal roundedBalance = BigDecimal.valueOf(newBalance).setScale(2, RoundingMode.HALF_UP);
            bankAccount.setBalance(roundedBalance.doubleValue());
            
            // Save updated account
            bankService.updateBankAccount(bankAccount);
            
            // Success message
            redirectAttributes.addFlashAttribute("message", 
                String.format("Successfully withdrawn ₹%.2f. New balance: ₹%.2f", amount, bankAccount.getBalance()));
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Withdrawal failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/bank/main?email=" + email;
    }
    
   
    @GetMapping("/balance")
    public String checkBalance(@RequestParam("email") String email, Model model) {
        try {
            BankEntity bankAccount = userService.getBankAccountByEmail(email);
            
            if (bankAccount != null) {
                model.addAttribute("bankAccount", bankAccount);
                model.addAttribute("userEmail", email);
                model.addAttribute("message", String.format("Current Balance: ₹%.2f", bankAccount.getBalance()));
                model.addAttribute("messageType", "success");
                return "Bank_main";
            } else {
                model.addAttribute("error", "Bank account not found");
                return "redirect:/bank/main?email=" + email;
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error retrieving balance: " + e.getMessage());
            return "redirect:/bank/main?email=" + email;
        }
    }
    
    
    @GetMapping("/")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/main/login";
    }
    
    @GetMapping("/loan")
    public String loan() {
    	return "Bank_Loan";
        
    }
    @GetMapping("/loanform")
    public String loanform() {
    	return "LoanForm";
        
    }
    


    
    
    

}