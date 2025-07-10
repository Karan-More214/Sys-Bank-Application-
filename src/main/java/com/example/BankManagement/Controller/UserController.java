package com.example.BankManagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.UserEntity;
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
			// Get bank account details for this user (if exists)
			BankEntity bankAccount = userService.getBankAccountByEmail(user.getEmail());
			
			model.addAttribute("user", user);
			model.addAttribute("bankAccount", bankAccount);
			
			return "UserProfile";
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
	        // Update user details
	        user.setFullName(fullName);
	        user.setEmail(email);
	        user.setUsername(username);

	        // Only update password if provided
	        if (password != null && !password.trim().isEmpty()) {
	            user.setPassword(password);
	        }

	        // Save updated user
	        userService.updateUser(user);

	        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

	    } catch (Exception e) {
	        e.printStackTrace();
	        redirectAttributes.addFlashAttribute("error", "Something went wrong while updating profile.");
	    }

	    return "redirect:/user/profile";
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