package com.example.BankManagement.Controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.LoanEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.EmailService;
import com.example.BankManagement.Service.LoanService;

import jakarta.servlet.http.HttpSession;

@RequestMapping("/mail")
@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BankService bankService;

    @Autowired
    private LoanService loanService;

    // ================= WELCOME EMAIL =================
    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, String> payload) {

        String email = payload.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is missing");
        }

        try {

            String subject = "🎉 Welcome to SYS Bank – Registration Successful";

            String message =
                    "Dear Customer,\n\n" +

                    "🎉 Congratulations!\n\n" +

                    "Your registration with 🏦 SYS Bank has been completed successfully.\n\n" +

                    "You can now access our banking services:\n" +
                    "💰 Deposit Money\n" +
                    "💳 Withdraw Funds\n" +
                    "📄 Apply for Loans\n" +
                    "👤 Manage your profile\n\n" +

                    "🔐 Please keep your login credentials secure.\n\n" +

                    "Thank you for choosing SYS Bank ❤️\n\n" +

                    "Warm Regards,\n" +
                    "🏦 SYS Bank Team";

            emailService.sendEmail(email, subject, message);

            return ResponseEntity.ok("Email sent to " + email);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.internalServerError().body("Failed to send email");

        }
    }

    // ================= DEPOSIT =================
    @PostMapping("/deposit")
    public ResponseEntity<?> confirmDeposit(@RequestBody Map<String, Object> payload) {

        try {

            String email = payload.get("email").toString();
            double amount = Double.parseDouble(payload.get("amount").toString());

            BankEntity bankAccount = bankService.getBankAccountByEmail(email);

            if (bankAccount == null) {
                return ResponseEntity.badRequest().body("❌ Bank account not found.");
            }

            double currentBalance = bankAccount.getBalance() != null ? bankAccount.getBalance() : 0.0;

            double newBalance = currentBalance + amount;

            bankAccount.setBalance(
                    BigDecimal.valueOf(newBalance).setScale(2, RoundingMode.HALF_UP).doubleValue()
            );

            bankService.updateBankAccount(bankAccount);

            String subject = "💰 Deposit Successful – SYS Bank";

            String message =
                    "Dear " + bankAccount.getFirstname() + ",\n\n" +

                    "✅ Your deposit has been successfully processed.\n\n" +

                    "💰 Deposit Amount : ₹" + amount + "\n" +
                    "💳 Updated Balance : ₹" + bankAccount.getBalance() + "\n\n" +

                    "Thank you for banking with us 🤝\n\n" +

                    "Warm Regards,\n" +
                    "🏦 SYS Bank Team";

            emailService.sendEmail(email, subject, message);

            return ResponseEntity.ok("✅ Deposit successful. Email sent.");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(500).body("❌ Error processing deposit: " + e.getMessage());

        }
    }

    // ================= LOAN PAYMENT EMAIL =================
    @PostMapping("/loanEmail")
    public ResponseEntity<String> sendLoanEmail(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String loanId = request.get("loanId");
        String amount = request.get("amount");

        String subject = "💳 Loan Payment Received – SYS Bank";

        String message =
                "Dear Customer,\n\n" +

                "✅ We have successfully received your loan payment.\n\n" +

                "💰 Amount Paid : ₹" + amount + "\n" +
                "📄 Loan ID : " + loanId + "\n\n" +

                "Thank you for banking with SYS Bank 🤝\n\n" +

                "Warm Regards,\n" +
                "🏦 SYS Bank Team";

        emailService.sendEmail(email, subject, message);

        return ResponseEntity.ok("Email sent");
    }

    // ================= WITHDRAW =================
    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam("email") String email,
            @RequestParam("amount") double amount,
            RedirectAttributes redirectAttributes) {

        try {

            BankEntity bankAccount = bankService.getBankAccountByEmail(email);

            if (bankAccount == null) {

                redirectAttributes.addFlashAttribute("message", "Bank account not found.");

                redirectAttributes.addFlashAttribute("messageType", "danger");

                return "redirect:/bank/main?email=" + email;
            }

            double currentBalance = bankAccount.getBalance();

            if (amount > currentBalance) {

                redirectAttributes.addFlashAttribute("message", "Insufficient balance.");

                redirectAttributes.addFlashAttribute("messageType", "danger");

                return "redirect:/bank/main?email=" + email;
            }

            bankAccount.setBalance(currentBalance - amount);

            bankService.updateBankAccount(bankAccount);

            String subject = "💳 Withdrawal Successful – SYS Bank";

            String message =
                    "Dear " + bankAccount.getFirstname() + ",\n\n" +

                    "💸 Your withdrawal has been processed successfully.\n\n" +

                    "Withdrawal Amount : ₹" + amount + "\n" +
                    "Remaining Balance : ₹" + bankAccount.getBalance() + "\n\n" +

                    "If you did not authorize this transaction please contact SYS Bank immediately.\n\n" +

                    "Thank you for banking with us 🤝\n\n" +

                    "Warm Regards,\n" +
                    "🏦 SYS Bank Team";

            emailService.sendEmail(email, subject, message);

            redirectAttributes.addFlashAttribute("message", "Withdrawal successful.");

            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute("message", "Error during withdrawal.");

            redirectAttributes.addFlashAttribute("messageType", "danger");

        }

        return "redirect:/bank/main?email=" + email;
    }

    // ================= LOAN APPLICATION =================
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

            LocalDate dob = LocalDate.parse(dobString);

            LoanEntity loan = new LoanEntity();

            loan.setFirstname(firstname);
            loan.setLastname(lastname);
            loan.setDob(dob);
            loan.setEmail(email);
            loan.setPhoneNo(phoneNo);
            loan.setAddress(address);
            loan.setLoanType(loanType);
            loan.setLoanAmount(loanAmount);
            loan.setLoanYears(loanYears);
            loan.setEmploymentType(employmentType);
            loan.setMonthlyIncome(monthlyIncome);
            loan.setPurpose(purpose);
            loan.setUser(user);

            LoanEntity savedLoan = loanService.saveLoan(loan);

            String subject = "📄 Loan Application Submitted – SYS Bank";

            String message =
                    "Dear " + firstname + ",\n\n" +

                    "✅ Your loan application has been successfully submitted.\n\n" +

                    "Loan Details:\n" +
                    "🏦 Loan Type : " + loanType + "\n" +
                    "💰 Amount : ₹" + loanAmount + "\n" +
                    "📅 Duration : " + loanYears + " years\n" +
                    "🆔 Application ID : " + savedLoan.getId() + "\n\n" +

                    "Our team will review your application shortly.\n\n" +

                    "Thank you for choosing SYS Bank ❤️\n\n" +

                    "Warm Regards,\n" +
                    "🏦 SYS Bank Team";

            emailService.sendEmail(email, subject, message);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Loan application submitted successfully! ID: " + savedLoan.getId()
            );

            return "redirect:/loan/myloans";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute("error", "Loan application failed.");

            return "redirect:/loan/apply";
        }
    }

    // ================= LOAN STATUS =================
    public void sendLoanStatusEmail(
            String toEmail,
            String status,
            String name,
            String loanType,
            Double amount) {

        String subject = "Loan Application Update – SYS Bank";

        String body;

        if (status.equalsIgnoreCase("APPROVED")) {

            body =
                    "Dear " + name + ",\n\n" +

                    "🎉 Congratulations!\n\n" +

                    "Your loan application has been APPROVED.\n\n" +

                    "Loan Details:\n" +
                    "🏦 Loan Type : " + loanType + "\n" +
                    "💰 Approved Amount : ₹" + amount + "\n\n" +

                    "Our team will contact you shortly.\n\n" +

                    "Warm Regards,\n" +
                    "🏦 SYS Bank Loan Department";

        } else {

            body =
                    "Dear " + name + ",\n\n" +

                    "⚠️ We regret to inform you that your loan application was not approved.\n\n" +

                    "Loan Details:\n" +
                    "🏦 Loan Type : " + loanType + "\n" +
                    "💰 Requested Amount : ₹" + amount + "\n\n" +

                    "You may contact our support team for other financial options.\n\n" +

                    "Regards,\n" +
                    "🏦 SYS Bank Loan Department";
        }

        emailService.sendEmail(toEmail, subject, body);
    }
}