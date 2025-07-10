package com.example.BankManagement.Controller;

import java.util.ArrayList;
import java.util.List;

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
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.LoanService;
import com.example.BankManagement.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private BankService bankService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private LoanService loanService;
	@GetMapping("/cancel")
	public String cancelcus()
	{
		return "redirect:/admin/customers";
	}
	
	
	@GetMapping("/home")
	public String adminHome(HttpSession session, Model model) {
		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
		if(user != null && "Admin".equalsIgnoreCase(user.getRole()))
		{
			model.addAttribute("EventList", new ArrayList<>());
			model.addAttribute("category", "All");
			return "Adminpage";
		}
		return "redirect:/main/login";
	}
	
	/**
	 * Display all customers page - MAIN CUSTOMER LIST
	 */
	@GetMapping("/customers")
	public String showAllCustomers(HttpSession session, Model model) {
		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
		if(user != null && "Admin".equalsIgnoreCase(user.getRole())) {
			try {
				// Fetch all bank accounts from database
				List<BankEntity> customers = bankService.getAllBankAccounts();
				System.out.println("Fetched " + customers.size() + " customers from database"); // Debug log
				
				// Add data to model for the template
				model.addAttribute("customer", customers); // This matches your template th:each="cust : ${customer}"
				model.addAttribute("totalCustomers", customers.size());
				
				// Return the CustomerList template
				return "Customer";
			} catch (Exception e) {
				System.err.println("Error loading customers: " + e.getMessage()); // Debug log
				e.printStackTrace();
				model.addAttribute("error", "Error loading customers: " + e.getMessage());
				model.addAttribute("customer", new ArrayList<>()); // Empty list to prevent template errors
				model.addAttribute("totalCustomers", 0);
				return "CustomerList";
			}
		}
		return "redirect:/main/login";
	}
	
	/**
	 * Display single customer details
	 */
	@GetMapping("/customer/{id}")
	public String showCustomer(@PathVariable("id") Integer customerId, HttpSession session, Model model) {
		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
		if(user != null && "Admin".equalsIgnoreCase(user.getRole())) {
			try {
				// Fetch specific customer by ID
				BankEntity customer = bankService.getBankAccountById(customerId);
				if(customer != null) {
					model.addAttribute("customer", customer);
					return "Customer"; // Use the original Customer.html for single customer view
				} else {
					model.addAttribute("error", "Customer not found with ID: " + customerId);
					return "redirect:/admin/customers";
				}
			} catch (Exception e) {
				model.addAttribute("error", "Error loading customer: " + e.getMessage());
				return "redirect:/admin/customers";
			}
		}
		return "redirect:/main/login";
	}
	
	
    @GetMapping("/edit/{id}")
	public String editFood(@PathVariable("id")int id, Model model)
	{
		BankEntity bank = bankService.getCustomerbyId(id);
		model.addAttribute("bank", bank);
		return "Update_Form";
	} 
	
	@PostMapping("/update")
	public String updatefood(@ModelAttribute BankEntity bank) {
		
		System.out.println("Update data: "+bank);
		bankService.UpdateCustomer(bank);
		return "redirect:/admin/customers";
	}
	
	
	@GetMapping("delete/{id}")
	public String deleteCustomer(@PathVariable("id") int id) {
	    BankEntity bank = bankService.getCustomerbyId(id); // fetch event by id
	    if (bank != null) {
//	        String category = event.getCategory();  // get category before deletion
	        bankService.DeleteCustomer(id);                     // delete event
	        return "redirect:/admin/customers"; // redirect with category param
	    }
	    // fallback
	    return "redirect:/admin/customers";
	}
	
	@GetMapping("/loanlist")
	public String loanlist(Model model)
	{
		model.addAttribute("loan",loanService.getAllLoans() );
		return "LoanList";
	}
	
	
	
	
	
	/**
	
//	 * Update customer profile
//	 */
//	@PostMapping("/customer/update")
//	public String updateCustomer(
//			@RequestParam("customerId") Integer customerId,
//			@RequestParam("fullName") String fullName,
//			@RequestParam("email") String email,
//			@RequestParam("phoneNo") String phoneNo,
//			@RequestParam(value = "balance", required = false) Double balance,
//			@RequestParam(value = "status", required = false) String status,
//			HttpSession session,
//			RedirectAttributes redirectAttributes) {
//		
//		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
//		if(user != null && "Admin".equalsIgnoreCase(user.getRole())) {
//			try {
//				BankEntity customer = bankService.getBankAccountById(customerId);
//				if(customer != null) {
//					// Parse full name (assuming format: "FirstName LastName")
//					String[] names = fullName.trim().split("\\s+", 2);
//					customer.setFirstname(names[0]);
//					if(names.length > 1) {
//						customer.setLastname(names[1]);
//					} else {
//						customer.setLastname(""); // Set empty if no last name
//					}
//					
//					customer.setEmail(email);
//					customer.setPhoneNo(phoneNo);
//					
//					if(balance != null) {
//						customer.setBalance(balance);
//					}
//					
//					if(status != null && !status.isEmpty()) {
//						customer.setAccountStatus(status.toUpperCase());
//					}
//					
//					bankService.updateBankAccount(customer);
//					
//					redirectAttributes.addFlashAttribute("success", "Customer updated successfully!");
//					return "redirect:/admin/customer/" + customerId;
//				} else {
//					redirectAttributes.addFlashAttribute("error", "Customer not found!");
//					return "redirect:/admin/customers";
//				}
//			} catch (Exception e) {
//				redirectAttributes.addFlashAttribute("error", "Error updating customer: " + e.getMessage());
//				return "redirect:/admin/customer/" + customerId;
//			}
//		}
//		return "redirect:/main/login";
//	}
//	
//	/**
//	 * Delete customer
//	 */
//	@PostMapping("/customer/delete")
//	public String deleteCustomer(
//			@RequestParam("customerId") Integer customerId,
//			HttpSession session,
//			RedirectAttributes redirectAttributes) {
//		
//		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
//		if(user != null && "Admin".equalsIgnoreCase(user.getRole())) {
//			try {
//				bankService.deleteBankAccount(customerId);
//				redirectAttributes.addFlashAttribute("success", "Customer deleted successfully!");
//				return "redirect:/admin/customers";
//			} catch (Exception e) {
//				redirectAttributes.addFlashAttribute("error", "Error deleting customer: " + e.getMessage());
//				return "redirect:/admin/customers";
//			}
//		}
//		return "redirect:/main/login";
//	}
	
//	/**
//	 * Search customers
//	 */
//	@GetMapping("/customers/search")
//	public String searchCustomers(
//			@RequestParam("searchTerm") String searchTerm,
//			HttpSession session,
//			Model model) {
//		
//		UserEntity user = (UserEntity) session.getAttribute("loggedUser");
//		if(user != null && "Admin".equalsIgnoreCase(user.getRole())) {
//			try {
//				List<BankEntity> customers;
//				
//				if(searchTerm != null && !searchTerm.trim().isEmpty()) {
//					customers = bankService.searchCustomersByName(searchTerm.trim());
//				} else {
//					customers = bankService.getAllBankAccounts(); // Show all if search is empty
//				}
//				
//				model.addAttribute("customer", customers); // Keep same name for consistency
//				model.addAttribute("searchTerm", searchTerm);
//				model.addAttribute("totalCustomers", customers.size());
//				return "CustomerList"; // Use CustomerList template for search results too
//			} catch (Exception e) {
//				model.addAttribute("error", "Error searching customers: " + e.getMessage());
//				model.addAttribute("customer", new ArrayList<>());
//				model.addAttribute("totalCustomers", 0);
//				return "CustomerList";
//			}
//		}
//		return "redirect:/main/login";
//	}
}