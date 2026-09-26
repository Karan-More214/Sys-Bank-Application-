package com.example.BankManagement.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.LoanEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Security.PasswordService;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.LoanService;
import com.example.BankManagement.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BankService bankService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private LoanService loanService;

	@Autowired
	private PasswordService passwordService;

	/**
	 * User Dashboard - Home page
	 */
	@GetMapping("/home")
	public String userHome(HttpSession session, Model model) {
		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
		
		if (user != null && "User".equalsIgnoreCase(user.getRole())) {
			// Get bank account details for this user
			BankEntity bankAccount = userService.getBankAccountByEmail(user.getEmail());
			
			// Add user and bank account data to the model
			model.addAttribute("user", user);
			model.addAttribute("bankAccount", bankAccount);
			model.addAttribute("userEmail", user.getEmail());
			
			return "Bank_main";
		}
		return "redirect:/main/login";
	}
	
	/**
     * Show User Profile Page
     */
    @GetMapping("/profile")
    public String showUserProfile(HttpSession session, Model model) {
        UserEntity user = (UserEntity) session.getAttribute("loggedUser");

        if (user != null && "User".equalsIgnoreCase(user.getRole())) {
            BankEntity bankAccount = userService.getBankAccountByEmail(user.getEmail());
            model.addAttribute("user", user);
            model.addAttribute("bankAccount", bankAccount);
            return "UserProfile"; // Thymeleaf page
        }

        return "redirect:/main/login";
    }

    /**
     * Update User Profile
     */
    @PostMapping("/updateProfile")
    public String updateUserProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam(value = "password", required = false) String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        UserEntity user = (UserEntity) session.getAttribute("loggedUser");

        if (user == null || !"User".equalsIgnoreCase(user.getRole())) {
            return "redirect:/main/login";
        }

        try {
            if (password != null && !password.trim().isEmpty() && !passwordService.isComplexEnough(password)) {
                redirectAttributes.addFlashAttribute("error",
                        "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character");
                return "redirect:/user/profile";
            }

            user.setFullName(fullName);
            user.setEmail(email);
            user.setUsername(username);

            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordService.encode(password));
            }

            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Something went wrong while updating profile.");
        }

        return "redirect:/user/profile";
    }

    /**
     * Serve User Profile Photo (from DB)
     */
    @GetMapping("/profile/photo")
    @ResponseBody
    public ResponseEntity<byte[]> getProfilePhoto(HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("loggedUser");

        if (user != null && "User".equalsIgnoreCase(user.getRole())) {
            BankEntity bankAccount = userService.getBankAccountByEmail(user.getEmail());
            if (bankAccount != null && bankAccount.getProfilePhoto() != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG); // or MediaType.IMAGE_PNG
                return new ResponseEntity<>(bankAccount.getProfilePhoto(), headers, HttpStatus.OK);
            }
        }

        return ResponseEntity.notFound().build();
    }

	
	@GetMapping("/loans")
	public String showUserLoans(HttpSession session, Model model) {
		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
		
		if (user != null && "User".equalsIgnoreCase(user.getRole())) {
			model.addAttribute("loans", loanService.getLoansByUser(user));
			model.addAttribute("user", user);
			
			return "MyLoans";
		}
		return "redirect:/main/login";
	}
	

}