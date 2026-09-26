package com.example.BankManagement.Controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.BankManagement.Entity.Role;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Security.PasswordService;
import com.example.BankManagement.Service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/main")
public class AuthController {

    @Autowired
    private UserRepo repo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordService passwordService;

    @Value("${FRONTEND_URL:}")
    private String frontendUrl;

    private String reactUrl(String path) {
        return (frontendUrl == null || frontendUrl.isBlank() ? "https://sys-bank-application.vercel.app" : frontendUrl) + path;
    }

    // ================= LANDING PAGE =================
    @GetMapping("/")
    public String landing() {
        return "Bank_Management_System";
    }

    // ================= LOGIN PAGE =================
    // Superseded by the React /login page - Bank_login.html's static CSS (Bank_login.css)
    // was never updated when the React auth card's dark-navy/cyan-glow theme shipped
    // (see Login.css), so this template had visibly drifted to the old look. Redirecting,
    // same as /admin/home and /bank/loan, leaves exactly one auth UI to keep in sync
    // instead of two stylesheets that can (and did) diverge again.
    @GetMapping("/login")
    public String loginPage() {
        return "redirect:" + reactUrl("/login");
    }

    // ================= ACCOUNT FORM =================
    @GetMapping("/create")
    public String accountCreation(Model model) {
        model.addAttribute("user", new UserEntity());
        return "Bank_Account";
    }

    // ================= REGISTER PAGE =================
    // Superseded by the React /register page - same stale-CSS reasoning as /login above.
    @GetMapping("/register")
    public String registerPage() {
        return "redirect:" + reactUrl("/register");
    }

    // ================= REGISTER USER =================
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserEntity user,
                               BindingResult bindingResult,
                               Model model) {

        if (bindingResult.hasErrors()) {
            return "Bank_register";
        }

        // UserEntity.password no longer declares this (it stores a hash, not a raw
        // password), so it's checked here instead - same rule, same field-scoped
        // error the template already knows how to render via #fields.hasErrors('password').
        if (!passwordService.isComplexEnough(user.getPassword())) {
            bindingResult.rejectValue("password", "password.complexity",
                    "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character");
            return "Bank_register";
        }

        if (repo.findByEmail(user.getEmail()) != null) {
            model.addAttribute("emailError", "Email already exists");
            return "Bank_register";
        }

        if (repo.findByUsername(user.getUsername()) != null) {
            model.addAttribute("usernameError", "Username already exists");
            return "Bank_register";
        }

        try {

            // Server-computed only - never trust a role value bound from the submitted form.
            user.setRole(Role.forEmail(user.getEmail()).name());
            // user.getPassword() is still the raw value bound directly from the form.
            user.setPassword(passwordService.encode(user.getPassword()));
            repo.save(user);

            // ================= SEND WELCOME EMAIL =================
            try {

                String subject = "🎉 Welcome to SYS Bank – Account Registration Successful";

                String message =
                        "Dear " + user.getFullName() + ",\n\n" +

                        "🎉 Congratulations!\n\n" +

                        "Your account has been successfully registered with 🏦 SYS Bank.\n\n" +

                        "You can now access our online banking services:\n\n" +

                        "💰 Deposit money into your account\n" +
                        "💳 Withdraw funds anytime\n" +
                        "📄 Apply for personal or business loans\n" +
                        "👤 Manage your banking profile\n\n" +

                        "⚠️ Important:\n" +
                        "Before performing any banking operations please create your bank account in the system.\n\n" +

                        "🔐 Security Tip:\n" +
                        "Never share your login credentials with anyone.\n\n" +

                        "If you need assistance our support team is always here to help.\n\n" +

                        "Thank you for choosing SYS Bank ❤️\n\n" +

                        "Warm Regards,\n" +
                        "🏦 SYS Bank Team";

                emailService.sendEmail(user.getEmail(), subject, message);

            } catch (Exception emailEx) {

                emailEx.printStackTrace();
                model.addAttribute("error",
                        "Account created but confirmation email could not be sent.");

                return "Bank_register";
            }

            return "redirect:" + reactUrl("/login?success=true");

        } catch (DataIntegrityViolationException e) {

            model.addAttribute("error", "Email or username already exists");
            return "Bank_register";

        } catch (Exception e) {

            model.addAttribute("error",
                    "An error occurred while creating your account.");

            return "Bank_register";
        }
    }

    // ================= LOGIN SYSTEM =================
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {

        UserEntity user = repo.findByEmail(email);

        if (user == null) {
            model.addAttribute("Error", "Invalid Credentials");
            return "Bank_login";
        }

        // ================= CHECK ACCOUNT LOCK =================
        if (!user.isAccountNonLocked()) {

            LocalDateTime lockTime = user.getLockTime();

            if (lockTime != null &&
                    lockTime.plusHours(24).isBefore(LocalDateTime.now())) {

                user.setAccountNonLocked(true);
                user.setFailedAttempts(0);
                user.setLockTime(null);

                repo.save(user);

            } else {

                model.addAttribute("Error",
                        "Your account is locked. Please try again after 24 hours.");

                return "Bank_login";
            }
        }

        // ================= PASSWORD MATCH =================
        boolean passwordOk;
        if (passwordService.isBcryptHash(user.getPassword())) {
            passwordOk = passwordService.matches(password, user.getPassword());
        } else {
            // Legacy plaintext row (predates BCrypt migration). If it matches, upgrade
            // it to a hash now so this account never compares in plaintext again.
            passwordOk = user.getPassword().equals(password);
            if (passwordOk) {
                user.setPassword(passwordService.encode(password));
            }
        }

        if (passwordOk) {

            user.setFailedAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);

            repo.save(user);

            session.setAttribute("loggedUser", user);

            if ("Admin".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin/home";
            } else {
                return "redirect:/user/home";
            }

        } else {

            int attempts = user.getFailedAttempts() + 1;

            user.setFailedAttempts(attempts);

            // ================= LOCK ACCOUNT =================
            if (attempts >= 3) {

                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());

                String subject =
                        "⚠️ Security Alert – Your SYS Bank Account Has Been Locked";

                String message =
                        "Dear " + user.getFullName() + ",\n\n" +

                        "⚠️ Security Alert!\n\n" +

                        "We detected multiple failed login attempts on your SYS Bank account.\n\n" +

                        "For your security your account has been temporarily locked.\n\n" +

                        "🔒 Lock Duration: 24 Hours\n\n" +

                        "If this was you please try logging in again after the lock period.\n\n" +

                        "If you did NOT attempt to login please contact SYS Bank support immediately.\n\n" +

                        "Your account security is extremely important to us.\n\n" +

                        "Warm Regards,\n" +
                        "🏦 SYS Bank Security Team";

                try {

                    emailService.sendEmail(user.getEmail(), subject, message);

                } catch (Exception emailEx) {

                    emailEx.printStackTrace();
                }
            }

            repo.save(user);

            model.addAttribute("Error", "Invalid Credentials");

            return "Bank_login";
        }
    }

    // ================= LOGOUT =================
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/main/";
    }

    // ================= ACCOUNT FORM =================
    @GetMapping("/form")
    public String form() {
        return "Bank_Account";
    }

    // ================= FORGOT PASSWORD =================
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {

        model.addAttribute("email", "");

        return "Forgetpass";
    }

    // ================= VERIFY EMAIL =================
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        Model model) {

        UserEntity user = repo.findByEmail(email);

        if (user != null) {

            model.addAttribute("email", email);
            model.addAttribute("showResetForm", true);

        } else {

            model.addAttribute("error",
                    "No account found with that email.");
        }

        return "Forgetpass";
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/reset-password-direct")
    public String resetPasswordDirect(@RequestParam("email") String email,
                                      @RequestParam("password") String password,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {

        UserEntity user = repo.findByEmail(email);

        if (user == null) {

            model.addAttribute("error", "Invalid email.");

            return "Forgetpass";
        }

        String regex =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";

        if (!password.matches(regex)) {

            model.addAttribute("email", email);
            model.addAttribute("showResetForm", true);

            model.addAttribute("error",
                    "Password must contain uppercase, lowercase, digit and special character.");

            return "Forgetpass";
        }

        user.setPassword(passwordService.encode(password));

        repo.save(user);

        redirectAttributes.addFlashAttribute("message",
                "Password reset successfully. Please login.");

        return "redirect:/main/login";
    }
}