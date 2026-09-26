package com.example.BankManagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.TransactionEntity;
import com.example.BankManagement.Repository.BankRepo;
import com.example.BankManagement.Repository.TransactionRepository;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.EmailService;
import com.example.BankManagement.Service.UserService;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;

import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/bank")
public class BankController {

    @Autowired
    private BankService bankService;

    @Autowired
    private BankRepo bankRepo;

    @Value("${FRONTEND_URL:}")
    private String frontendUrl;

    private String reactLoanProductsUrl() {
        return (frontendUrl == null || frontendUrl.isBlank() ? "https://sys-bank-application.vercel.app" : frontendUrl) + "/loan-products";
    }

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TransactionRepository transactionRepository;

    // ---------------- Landing ----------------
    @GetMapping("/landing")
    public String mainpage() {
        return "Bank_Management_System";
    }

    @GetMapping("/update")
    public String updatecustomer() {
        return "Customer";
    }

    @GetMapping("/add")
    public String AccountOpeningForm(Model model) {
        model.addAttribute("Account", new BankEntity());
        return "add";
    }

    // ✅ Fixed: Added missing /open endpoint
    @GetMapping("/open")
    public String openAccountForm(Model model, @RequestParam(value = "success", required = false) String success) {
        if (success != null) {
            model.addAttribute("message", "Account created successfully!");
            model.addAttribute("messageType", "success");
        }
        model.addAttribute("Account", new BankEntity());
        return "Bank_Account"; // This should match your HTML file name
    }

    // ---------------- Account Creation ----------------
    @PostMapping("/submit-form")
    public String submitAccount(
            @RequestParam("Firstname") String firstName,
            @RequestParam("Lastname") String lastName,
            @RequestParam("profilePhoto") MultipartFile profilePhoto,
            @RequestParam("aadhaarFile") MultipartFile aadhaarFile,
            @RequestParam("panFile") MultipartFile panFile,
            @RequestParam("dob") String dob,
            @RequestParam("Gender") String gender,
            @RequestParam("Email") String email,
            @RequestParam("PhoneNo") String phoneNo,
            @RequestParam("Address") String address,
            @RequestParam("AccountType") String accountType,
            @RequestParam("Balance") double balance,
            // ✅ Fixed: Added missing joint account fields
            @RequestParam(value = "jointFirstName", required = false) String jointFirstName,
            @RequestParam(value = "jointLastName", required = false) String jointLastName,
            @RequestParam(value = "jointEmail", required = false) String jointEmail,
            @RequestParam(value = "jointPhoneNo", required = false) String jointPhoneNo,
            @RequestParam(value = "jointAddress", required = false) String jointAddress,
            @RequestParam(value = "jointDob", required = false) String jointDob,
            @RequestParam(value = "jointGender", required = false) String jointGender,
            @RequestParam(value = "jointProfilePhoto", required = false) MultipartFile jointProfilePhoto,
            @RequestParam(value = "jointAadhaarFile", required = false) MultipartFile jointAadhaarFile,
            @RequestParam(value = "jointPanFile", required = false) MultipartFile jointPanFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            BankEntity account = new BankEntity();
            account.setFirstname(firstName);
            account.setLastname(lastName);
            
            // ✅ Fixed: Proper file handling with null checks
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                account.setProfilePhoto(profilePhoto.getBytes());
            }
            if (aadhaarFile != null && !aadhaarFile.isEmpty()) {
                account.setAadhaar(aadhaarFile.getBytes());
            }
            if (panFile != null && !panFile.isEmpty()) {
                account.setPan(panFile.getBytes());
            }

            account.setDob(dob);
            account.setGender(gender);
            account.setEmail(email);
            account.setPhoneNo(phoneNo);
            account.setAddress(address);
            account.setAccountType(accountType);
            account.setBalance(balance);
            // New accounts start unverified - an admin has to review the uploaded Aadhaar/PAN
            // before the account is treated as fully KYC-verified.
            account.setKycStatus("PENDING");

            // ✅ Fixed: Joint account handling with all fields
            if ("joint".equalsIgnoreCase(accountType)) {
                account.setJointFirstName(jointFirstName);
                account.setJointLastName(jointLastName);
                account.setJointEmail(jointEmail);
                account.setJointPhoneNo(jointPhoneNo);
                account.setJointAddress(jointAddress);
                account.setJointDob(jointDob);
                account.setJointGender(jointGender);

                if (jointProfilePhoto != null && !jointProfilePhoto.isEmpty()) {
                    account.setJointProfilePhoto(jointProfilePhoto.getBytes());
                }
                if (jointAadhaarFile != null && !jointAadhaarFile.isEmpty()) {
                    account.setJointAadhaarFile(jointAadhaarFile.getBytes());
                }
                if (jointPanFile != null && !jointPanFile.isEmpty()) {
                    account.setJointPanFile(jointPanFile.getBytes());
                }
            }

            bankRepo.save(account);
            redirectAttributes.addFlashAttribute("message", "Account created successfully!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "Bank_Management_System";  // "redirect:/bank/open?success=true";
            
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Error processing files: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/bank/open";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Error creating account: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/bank/open";
        }
    }

    // ---------------- Dashboard with Transactions ----------------
    @GetMapping("/main")
    public String bankMain(@RequestParam("email") String email, Model model, HttpSession session) {
        BankEntity bankAccount = bankService.getBankAccountByEmail(email);

        if (bankAccount != null) {
            List<TransactionEntity> transactions = transactionRepository.findTop5ByEmailOrderByTimestampDesc(email);
            model.addAttribute("bankAccount", bankAccount);
            model.addAttribute("transactions", transactions);
            model.addAttribute("userEmail", email);
            return "Bank_main";
        } else {
            model.addAttribute("error", "Bank account not found for email: " + email);
            return "redirect:/main/login";
        }
    }

    // ---------------- Deposit ----------------
    @GetMapping("/deposit")
    public String showDepositPage(@RequestParam("email") String email, Model model) {
        BankEntity bankAccount = userService.getBankAccountByEmail(email);
        if (bankAccount != null) {
            model.addAttribute("bankAccount", bankAccount);
            model.addAttribute("userEmail", email);
            return "Bank_Deposite";
        } else {
            model.addAttribute("error", "Bank account not found");
            return "redirect:/bank/main?email=" + email;
        }
    }

 // Replace ENTIRE confirmDeposit method in BankController.java

  //...
    @PostMapping("/deposit")
    @ResponseBody
    public ResponseEntity<?> depositEmailOnly(@RequestBody Map<String, Object> payload) {
        try {
            String email = payload.get("email").toString();
            double amount = Double.parseDouble(payload.get("amount").toString());
            BankEntity bankAccount = bankService.getBankAccountByEmail(email);
            if (bankAccount == null) {
                return ResponseEntity.badRequest().body("❌ Bank account not found.");
            }
            // *** DO NOT update bank balance here! ***
            String subject = "Deposit Confirmation - SYS Bank";
            String message = "Dear " + bankAccount.getFirstname() + ",\n\n" +
                    "We have successfully received your deposit of ₹" + amount + ".\n" +
                    "Your updated account balance is ₹" + bankAccount.getBalance() + ".\n\n" +
                    "Thank you for banking with us.\n\n" +
                    "Warm regards,\nSYS Bank Team";
            emailService.sendEmail(email, subject, message);
            return ResponseEntity.ok("✅ Email sent.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Error processing deposit email: " + e.getMessage());
        }
    }
    // ---------------- Withdraw ----------------
    @GetMapping("/withdraw")
    public String showWithdrawPage(@RequestParam("email") String email, Model model) {
        BankEntity bankAccount = userService.getBankAccountByEmail(email);
        if (bankAccount != null) {
            model.addAttribute("bankAccount", bankAccount);
            model.addAttribute("userEmail", email);
            return "Bank_Withdrawl";
        } else {
            model.addAttribute("error", "Bank account not found");
            return "redirect:/bank/main?email=" + email;
        }
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam("email") String email,
                           @RequestParam("amount") double amount,
                           RedirectAttributes redirectAttributes) {
        try {
            BankEntity bankAccount = bankService.getBankAccountByEmail(email);
            if (bankAccount == null) {
                redirectAttributes.addFlashAttribute("message", "Bank account not found.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/bank/main?email=" + email;
            }

            if (amount > bankAccount.getBalance()) {
                redirectAttributes.addFlashAttribute("message", "Insufficient balance.");
                redirectAttributes.addFlashAttribute("messageType", "danger");
                return "redirect:/bank/main?email=" + email;
            }

            // ✅ Deduct balance
            bankAccount.setBalance(bankAccount.getBalance() - amount);
            bankService.updateBankAccount(bankAccount);

            // ✅ Record withdrawal transaction
            TransactionEntity txn = new TransactionEntity();
            txn.setEmail(email);
            txn.setType("Withdraw");
            txn.setAmount(amount);
            transactionRepository.save(txn);

            // ✅ Send Email
            emailService.sendEmail(email, "Withdrawal Confirmation - SYS Bank",
                    "Dear " + bankAccount.getFirstname() + ",\n\nYour withdrawal of ₹" + amount +
                            " was successful.\nRemaining balance: ₹" + bankAccount.getBalance());

            redirectAttributes.addFlashAttribute("message", "Withdrawal successful.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "An error occurred during withdrawal.");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/bank/main?email=" + email;
    }

    // ---------------- Balance Check ----------------
    @GetMapping("/balance")
    public String checkBalance(@RequestParam("email") String email, Model model) {
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
    }

    @GetMapping("/")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/main/login";
    }

    // Superseded by the React /loan-products page - redirects rather than rendering
    // Bank_Loan.html, matching how the legacy admin dashboard was retired.
    @GetMapping("/loan")
    public String loan() {
        return "redirect:" + reactLoanProductsUrl();
    }

    @GetMapping("/loanform")
    public String loanform() {
        return "LoanForm";
    }
    
    // ✅ Fixed: Return primary profile photo with proper endpoint
    @GetMapping("/user/profile/photo/{id}")
    public ResponseEntity<byte[]> getPrimaryPhoto(@PathVariable Integer id) throws IOException {
        Optional<BankEntity> optAccount = bankRepo.findById(id);
        if (optAccount.isEmpty() || optAccount.get().getProfilePhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        
        BankEntity account = optAccount.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(account.getProfilePhoto().length);
        return new ResponseEntity<>(account.getProfilePhoto(), headers, HttpStatus.OK);
    }
    
    // ✅ Fixed: Joint holder profile photo endpoint
    @GetMapping("/user/joint/photo/{id}")
    public ResponseEntity<byte[]> getJointPhoto(@PathVariable Integer id) throws IOException {
        Optional<BankEntity> optAccount = bankRepo.findById(id);
        if (optAccount.isEmpty() || optAccount.get().getJointProfilePhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        
        BankEntity account = optAccount.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(account.getJointProfilePhoto().length);
        return new ResponseEntity<>(account.getJointProfilePhoto(), headers, HttpStatus.OK);
    }

 // ✅ Document serving endpoints - VIEW in browser
    //
    // These sit under /bank/**, which SecurityConfig leaves at .anyRequest().permitAll() -
    // but JwtAuthenticationFilter still runs on every request regardless of path, so a
    // request carrying a valid JWT still gets its Authentication populated here even
    // though the route itself isn't gated at the authorizeHttpRequests level. That lets
    // these methods enforce their own check (account owner or admin) without moving the
    // route under /api/** or touching every other caller of this controller's other
    // legacy session-based endpoints. Aadhaar/PAN are sensitive documents, so unlike the
    // profile-photo endpoints above, these must never be reachable by an anonymous request.
    private boolean canAccessDocuments(Authentication authentication, BankEntity account) {
        if (authentication == null) return false;
        boolean isAdmin = false;
        boolean isUser = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) isAdmin = true;
            if ("ROLE_USER".equals(authority.getAuthority())) isUser = true;
        }
        if (!isAdmin && !isUser) return false; // anonymous requests get ROLE_ANONYMOUS, not these
        return isAdmin || authentication.getName().equalsIgnoreCase(account.getEmail());
    }

    @GetMapping("/user/profile/aadhaar/{id}")
    public ResponseEntity<byte[]> getPrimaryAadhaar(@PathVariable Integer id, Authentication authentication) {
        Optional<BankEntity> opt = bankRepo.findById(id);
        if (opt.isEmpty() || opt.get().getAadhaar() == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccessDocuments(authentication, opt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BankEntity account = opt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "inline; filename=primary_aadhaar_" + id + ".pdf");
        return new ResponseEntity<>(account.getAadhaar(), headers, HttpStatus.OK);
    }

    @GetMapping("/user/profile/pan/{id}")
    public ResponseEntity<byte[]> getPrimaryPan(@PathVariable Integer id, Authentication authentication) {
        Optional<BankEntity> opt = bankRepo.findById(id);
        if (opt.isEmpty() || opt.get().getPan() == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccessDocuments(authentication, opt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BankEntity account = opt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "inline; filename=primary_pan_" + id + ".pdf");
        return new ResponseEntity<>(account.getPan(), headers, HttpStatus.OK);
    }

    @GetMapping("/user/joint-aadhaar/{id}")
    public ResponseEntity<byte[]> getJointAadhaar(@PathVariable Integer id, Authentication authentication) {
        Optional<BankEntity> opt = bankRepo.findById(id);
        if (opt.isEmpty() || opt.get().getJointAadhaarFile() == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccessDocuments(authentication, opt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BankEntity account = opt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "inline; filename=joint_aadhaar_" + id + ".pdf");
        return new ResponseEntity<>(account.getJointAadhaarFile(), headers, HttpStatus.OK);
    }

    @GetMapping("/user/joint-pan/{id}")
    public ResponseEntity<byte[]> getJointPan(@PathVariable Integer id, Authentication authentication) {
        Optional<BankEntity> opt = bankRepo.findById(id);
        if (opt.isEmpty() || opt.get().getJointPanFile() == null) {
            return ResponseEntity.notFound().build();
        }
        if (!canAccessDocuments(authentication, opt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BankEntity account = opt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "inline; filename=joint_pan_" + id + ".pdf");
        return new ResponseEntity<>(account.getJointPanFile(), headers, HttpStatus.OK);
    }

    @GetMapping("/customer/file/{type}/{id}")
    public ResponseEntity<byte[]> serveCustomerFile(@PathVariable String type, @PathVariable Integer id, Authentication authentication) {
        Optional<BankEntity> opt = bankRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        if (!canAccessDocuments(authentication, opt.get())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BankEntity customer = opt.get();
        byte[] fileBytes;
        String filename;

        switch (type.toLowerCase()) {
            case "aadhaar":
                fileBytes = customer.getAadhaar();
                filename = "aadhaar_" + id + ".pdf";
                break;
            case "pan":
                fileBytes = customer.getPan();
                filename = "pan_" + id + ".pdf";
                break;
            case "joint-aadhaar":
                fileBytes = customer.getJointAadhaarFile();
                filename = "joint_aadhaar_" + id + ".pdf";
                break;
            case "joint-pan":
                fileBytes = customer.getJointPanFile();
                filename = "joint_pan_" + id + ".pdf";
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        if (fileBytes == null) return ResponseEntity.notFound().build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add("Content-Disposition", "inline; filename=" + filename);
        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}