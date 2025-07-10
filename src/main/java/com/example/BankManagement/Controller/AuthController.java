package com.example.BankManagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.UserRepo;
import com.example.BankManagement.Service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/main")
public class AuthController {
    
    @Autowired
    private UserRepo repo;
    
    @GetMapping("/")
    public String landing() {
        return "Bank_Management_System";
    }
    
    @GetMapping("/login")
    public String Login(Model model) {
        model.addAttribute("user", new UserEntity());
        return "Bank_login";
    }
    
    @GetMapping("/create")
    public String AccCreation(Model model) {
        model.addAttribute("user", new UserEntity());
        return "Bank_Account";
    }
    
    @GetMapping("/register")
    public String Register(Model model) {
        model.addAttribute("user", new UserEntity());
        return "Bank_register";
    }
    
//    @PostMapping("/register")
//    public String AddUser(@Valid @ModelAttribute("user") UserEntity user, 
//                         BindingResult bindingResult, Model model) {
//        
//        // Check for validation errors
//        if (bindingResult.hasErrors()) {
//            return "Bank_register";
//        }
//        
//        // Check if email already exists
//        if (repo.findByEmail(user.getEmail()) != null) {
//            model.addAttribute("emailError", "Email already exists");
//            return "Bank_register";
//        }
//        
//        // Check if username already exists
//        if (repo.findByUsername(user.getUsername()) != null) {
//            model.addAttribute("usernameError", "Username already exists");
//            return "Bank_register";
//        }
//        
//        try {
//            user.setRole("User");
//            repo.save(user);
//            model.addAttribute("success", "Account created successfully! Please login.");
//            return "redirect:/main/login?success=true";
//        } catch (DataIntegrityViolationException e) {
//            model.addAttribute("error", "Email or username already exists");
//            return "Bank_register";
//        } catch (Exception e) {
//            model.addAttribute("error", "An error occurred while creating your account");
//            return "Bank_register";
//        }
//    }
    
    
    
    
    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public String AddUser(@Valid @ModelAttribute("user") UserEntity user, 
                         BindingResult bindingResult, Model model) {

        // Validation errors from @Valid
        if (bindingResult.hasErrors()) {
            return "Bank_register";
        }

        // Check for duplicate email
        if (repo.findByEmail(user.getEmail()) != null) {
            model.addAttribute("emailError", "Email already exists");
            return "Bank_register";
        }

        // Check for duplicate username
        if (repo.findByUsername(user.getUsername()) != null) {
            model.addAttribute("usernameError", "Username already exists");
            return "Bank_register";
        }

        try {
            user.setRole("User");
            repo.save(user);

            // ✅ Send welcome email here
            try {
                emailService.sendEmail(
                    user.getEmail(),
                    "Registration successfully!",
                    "Welcome to SYS Bank. Continue your bank journey... Thank you!"
                );
            } catch (Exception emailEx) {
                emailEx.printStackTrace();
                model.addAttribute("error", "Account created, but failed to send confirmation email.");
                return "Bank_register"; // Optional: redirect anyway or show success
            }

            return "redirect:/main/login?success=true";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Email or username already exists");
            return "Bank_register";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while creating your account");
            return "Bank_register";
        }
    }

    
    @PostMapping("/login")
    public String Login(@RequestParam String email, @RequestParam String password, 
                       Model model, HttpSession session) {
        
        System.out.println("Email: " + email + " Pass: " + password);
        UserEntity user = repo.findByEmailAndPassword(email, password);
        
        if (user != null) {
            session.setAttribute("loggedUser", user);
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                System.out.println("Admin login");
                return "redirect:/admin/home";
            } else {
                return "redirect:/user/home";
            }
        } else {
            model.addAttribute("Error", "Invalid Credentials");
            return "Bank_login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/main/";
    }
    
    @GetMapping("/form")
    public String form() {
        return "Bank_Account";
    }
}