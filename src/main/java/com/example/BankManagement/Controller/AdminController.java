package com.example.BankManagement.Controller;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.LoanEntity;
import com.example.BankManagement.Entity.LoanEntity.LoanStatusUpdateRequest;
import com.example.BankManagement.Repository.LoanRepo;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Service.BankService;
import com.example.BankManagement.Service.LoanService;

import jakarta.servlet.http.HttpSession;

/**
 * DEPRECATED dashboard pages (home/customers/customer/loanlist/edit): the app has
 * migrated to the React admin panel at {FRONTEND_URL}/admin. Those routes below now
 * redirect there instead of rendering the old Thymeleaf views, so this old UI can no
 * longer be landed on by accident. The action endpoints below them (approveLoan,
 * rejectLoan, update-loan-status, mark-loan-paid, update, delete/{id}) and the
 * byte[]-returning photo/file endpoints are untouched - they're either not pages, or
 * are now unreachable through normal navigation since the pages that linked to them
 * are gone, so leaving them doesn't add a real exposure.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private BankService bankService;

	@Autowired
	private LoanService loanService;

    @Autowired
    private LoanRepo loanRepository;

	@Autowired
	private EmailController emailController;

	@Value("${FRONTEND_URL:}")
	private String frontendUrl;

	private String reactAdminUrl() {
		return (frontendUrl == null || frontendUrl.isBlank() ? "https://sys-bank-application.vercel.app" : frontendUrl) + "/admin";
	}

	@GetMapping("/cancel")
	public String cancelcus()
	{
		return "redirect:" + reactAdminUrl();
	}


	// Superseded by the React admin panel's Dashboard view - redirects rather than
	// rendering Adminpage.html.
	@GetMapping("/home")
	public String adminHome() {
		return "redirect:" + reactAdminUrl();
	}

	/**
	 * Superseded by the React admin panel's Customers view - redirects rather than
	 * rendering Customer.html/CustomerList.html.
	 */
	@GetMapping("/customers")
	public String showAllCustomers() {
		return "redirect:" + reactAdminUrl();
	}

	/**
	 * Superseded by the React admin panel's Customers view - redirects rather than
	 * rendering the single-customer Customer.html page.
	 */
	@GetMapping("/customer/{id}")
	public String showCustomer(@PathVariable("id") Integer customerId) {
		return "redirect:" + reactAdminUrl();
	}
	
	@GetMapping("/customer/profile-photo/{id}")
	public ResponseEntity<byte[]> getCustomerProfilePhoto(@PathVariable("id") Integer id) {
	    BankEntity customer = bankService.getBankAccountById(id);

	    if (customer != null && customer.getProfilePhoto() != null) {
	        return ResponseEntity.ok()
	            .header("Content-Type", "image/jpeg") // Use image/png if your uploads are PNG
	            .body(customer.getProfilePhoto());
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}

	
	
    // Superseded by the React admin panel - redirects rather than rendering Update_Form.html.
    @GetMapping("/edit/{id}")
	public String editFood(@PathVariable("id")int id)
	{
		return "redirect:" + reactAdminUrl();
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
	
	// Superseded by the React admin panel's Loan Applications view - redirects rather
	// than rendering LoanList.html.
	@GetMapping("/loanlist")
	public String loanlist()
	{
		return "redirect:" + reactAdminUrl();
	}
	
	
	
	

	@PostMapping("/approveLoan")
	public ResponseEntity<String> approveLoan(@RequestBody LoanStatusUpdateRequest request) {
	    loanService.TrackLoanStatus(request.getLoanId(), "APPROVED");

	    // Get loan details
	    LoanEntity loan = loanService.getLoanById(request.getLoanId());
	    if (loan != null) {
	        emailController.sendLoanStatusEmail(
	            loan.getEmail(),
	            "APPROVED",
	            loan.getFirstname(),
	            loan.getLoanType(),
	            loan.getLoanAmount()
	        );
	    }

	    return ResponseEntity.ok("Loan Approved and email sent.");
	}

	@PostMapping("/rejectLoan")
	public ResponseEntity<String> rejectLoan(@RequestBody LoanStatusUpdateRequest request) {
	    loanService.TrackLoanStatus(request.getLoanId(), "REJECTED");

	    // Get loan details
	    LoanEntity loan = loanService.getLoanById(request.getLoanId());
	    if (loan != null) {
	        emailController.sendLoanStatusEmail(
	            loan.getEmail(),
	            "REJECTED",
	            loan.getFirstname(),
	            loan.getLoanType(),
	            loan.getLoanAmount()
	        );
	    }

	    return ResponseEntity.ok("Loan Rejected and email sent.");
	}



	@PostMapping("/update-loan-status")
	public ResponseEntity<String> updateLoanStatus(@RequestBody LoanStatusUpdateRequest request) {
	    LoanEntity loan = loanService.getLoanById(request.getLoanId());

	    if (loan != null) {
	        loan.setStatus(request.getStatus());

	        if ("APPROVED".equalsIgnoreCase(request.getStatus())) {
	            // ✅ Fetch email from UserEntity to avoid null
	            String userEmail = loan.getUser() != null ? loan.getUser().getEmail() : loan.getEmail();

	            BankEntity bankAccount = bankService.getBankAccountByEmail(userEmail);
	            if (bankAccount != null) {
	                double oldBalance = bankAccount.getBalance();
	                double newBalance = oldBalance + loan.getLoanAmount();

	                bankAccount.setBalance(newBalance);
	                bankService.updateBankAccount(bankAccount); // ✅ correctly saves

	                System.out.println("✅ Balance updated for " + userEmail + " from " + oldBalance + " to " + newBalance);
	            } else {
	                System.out.println("❌ Bank account not found for email: " + userEmail);
	            }
	        }

	        loanService.updateLoan(loan); // ✅ use updateLoan, not save
	        return ResponseEntity.ok("Loan status updated successfully");
	    }

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loan not found");
	}

	
	
	@PostMapping("/mark-loan-paid")
	public ResponseEntity<?> markLoanAsPaid(@RequestBody Map<String, Object> payload) {
	    try {
	        Long loanId = Long.valueOf(payload.get("loanId").toString()); // already done right ✅

	        Optional<LoanEntity> loanOpt = loanRepository.findById(loanId.intValue()); // If repo uses Integer ID

	        if (loanOpt.isPresent()) {
	            LoanEntity loan = loanOpt.get();
	            loan.setPaid(true);
	            loanRepository.save(loan);
	            return ResponseEntity.ok("Marked as paid");
	        } else {
	            return ResponseEntity.badRequest().body("Loan not found for ID: " + loanId);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("Error: " + e.getMessage());
	    }
	}
	
	
	
	
	@GetMapping("/customer/profile/{id}")
	public String showCustomerProfile(@PathVariable("id") Integer customerId, HttpSession session, Model model) {
	    UserEntity user = (UserEntity) session.getAttribute("loggedUser");

	    if (user != null && "Admin".equalsIgnoreCase(user.getRole())) {
	        BankEntity customer = bankService.getBankAccountById(customerId);
	        if (customer != null) {
	            model.addAttribute("customer", customer);
	            return "CustomerProfile"; // ye new HTML file hogi
	        } else {
	            model.addAttribute("error", "Customer not found!");
	            return "redirect:/admin/customers";
	        }
	    }
	    return "redirect:/main/login";
	}
	
	
	@GetMapping("/customer/file/{type}/{id}")
	public ResponseEntity<byte[]> serveCustomerFile(
	        @PathVariable("type") String type, 
	        @PathVariable("id") Integer id) {
	    
	    BankEntity customer = bankService.getBankAccountById(id);

	    if (customer == null) return ResponseEntity.notFound().build();

	    byte[] fileBytes = null;
	    String fileType = "application/pdf"; // adjust if you store other formats

	    switch (type.toLowerCase()) {
	        case "aadhaar":
	            fileBytes = customer.getAadhaar();
	            break;
	        case "pan":
	            fileBytes = customer.getPan();
	            break;
	        default:
	            return ResponseEntity.badRequest().build();
	    }

	    if (fileBytes == null) return ResponseEntity.notFound().build();

	    return ResponseEntity.ok()
	            .header("Content-Disposition", "inline; filename=" + type + "_" + id + ".pdf")
	            .header("Content-Type", fileType)
	            .body(fileBytes);
	}
	
	
	@GetMapping("/customer/joint-photo/{id}")
	public ResponseEntity<byte[]> getJointAccountHolderPhoto(@PathVariable("id") Integer id) {
	    BankEntity customer = bankService.getBankAccountById(id);

	    if (customer != null && customer.getJointProfilePhoto() != null) {
	        return ResponseEntity.ok()
	            .header("Content-Type", "image/jpeg") // Use image/png if your uploads are PNG
	            .body(customer.getJointProfilePhoto());
	    } else {
	        return ResponseEntity.notFound().build();
	    }
	}

	





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

