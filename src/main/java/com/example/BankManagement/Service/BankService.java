package com.example.BankManagement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BankManagement.Entity.BankEntity;
import com.example.BankManagement.Entity.UserEntity;
import com.example.BankManagement.Repository.BankRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BankService {

    @Autowired
    private BankRepo bankRepo;

    public void SubmitForm(BankEntity bank) {
    	bankRepo.save(bank);
		
	}
    
    /**
     * Get bank account by email
     */
    public BankEntity getBankAccountByEmail(String email) {
        return bankRepo.findByEmail(email);
    }

    /**
     * Get bank account by ID
     */
    public BankEntity getBankAccountById(Integer id) {
        Optional<BankEntity> account = bankRepo.findById(id);
        return account.orElse(null);
    }

    /**
     * Get all bank accounts
     */
    public List<BankEntity> getAllBankAccounts() {
        return bankRepo.findAll();
    }

    /**
     * Create new bank account
     */
    public BankEntity createBankAccount(BankEntity bankAccount) {
        // Set initial balance to 0 if not provided
        if (bankAccount.getBalance() == 0) {
            bankAccount.setBalance(0.0);
        }
        return bankRepo.save(bankAccount);
    }

    /**
     * Update existing bank account
     */
    public BankEntity updateBankAccount(BankEntity bankAccount) {
        return bankRepo.save(bankAccount);
    }

    /**
     * Delete bank account
     */
    public void deleteBankAccount(Integer id) {
        bankRepo.deleteById(id);
    }

    /**
     * Process deposit transaction
     */
    @Transactional
    public boolean processDeposit(String email, double amount) {
        try {
            // Validate amount
            if (amount <= 0) {
                throw new IllegalArgumentException("Deposit amount must be positive");
            }

            // Get account
            BankEntity account = getBankAccountByEmail(email);
            if (account == null) {
                throw new IllegalArgumentException("Bank account not found");
            }

            // Calculate new balance
            double currentBalance = account.getBalance();
            double newBalance = currentBalance + amount;

            // Round to 2 decimal places
            BigDecimal roundedBalance = BigDecimal.valueOf(newBalance)
                    .setScale(2, RoundingMode.HALF_UP);

            // Update balance
            account.setBalance(roundedBalance.doubleValue());
            bankRepo.save(account);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Deposit failed: " + e.getMessage());
        }
    }

    /**
     * Process withdrawal transaction
     */
    @Transactional
    public boolean processWithdrawal(String email, double amount) {
        try {
            // Validate amount
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be positive");
            }

            // Get account
            BankEntity account = getBankAccountByEmail(email);
            if (account == null) {
                throw new IllegalArgumentException("Bank account not found");
            }

            // Check sufficient balance
            double currentBalance = account.getBalance();
            if (amount > currentBalance) {
                throw new IllegalArgumentException("Insufficient balance");
            }

            // Calculate new balance
            double newBalance = currentBalance - amount;

            // Round to 2 decimal places
            BigDecimal roundedBalance = BigDecimal.valueOf(newBalance)
                    .setScale(2, RoundingMode.HALF_UP);

            // Update balance
            account.setBalance(roundedBalance.doubleValue());
            bankRepo.save(account);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Withdrawal failed: " + e.getMessage());
        }
    }

    /**
     * Check account balance
     */
    public double getBalance(String email) {
        BankEntity account = getBankAccountByEmail(email);
        if (account != null) {
            return account.getBalance();
        }
        throw new IllegalArgumentException("Bank account not found");
    }

    /**
     * Transfer money between accounts
     */
    @Transactional
    public boolean transferMoney(String fromEmail, String toEmail, double amount) {
        try {
            // Validate amount
            if (amount <= 0) {
                throw new IllegalArgumentException("Transfer amount must be positive");
            }

            // Get both accounts
            BankEntity fromAccount = getBankAccountByEmail(fromEmail);
            BankEntity toAccount = getBankAccountByEmail(toEmail);

            if (fromAccount == null || toAccount == null) {
                throw new IllegalArgumentException("One or both accounts not found");
            }

            // Check sufficient balance
            if (amount > fromAccount.getBalance()) {
                throw new IllegalArgumentException("Insufficient balance");
            }

            // Process transfer
            double fromNewBalance = fromAccount.getBalance() - amount;
            double toNewBalance = toAccount.getBalance() + amount;

            // Round to 2 decimal places
            BigDecimal fromRoundedBalance = BigDecimal.valueOf(fromNewBalance)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal toRoundedBalance = BigDecimal.valueOf(toNewBalance)
                    .setScale(2, RoundingMode.HALF_UP);

            // Update balances
            fromAccount.setBalance(fromRoundedBalance.doubleValue());
            toAccount.setBalance(toRoundedBalance.doubleValue());

            // Save both accounts
            bankRepo.save(fromAccount);
            bankRepo.save(toAccount);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    /**
     * Check if account exists
     */
    public boolean accountExists(String email) {
        return getBankAccountByEmail(email) != null;
    }

    /**
     * Get account summary
     */
    public String getAccountSummary(String email) {
        BankEntity account = getBankAccountByEmail(email);
        if (account != null) {
            return String.format("Account: %s %s | Email: %s | Balance: ₹%.2f",
                    account.getFirstname(), account.getLastname(),
                    account.getEmail(), account.getBalance());
        }
        return "Account not found";
    }
    
 // Add these methods to your BankService class

    /**
     * Get all bank accounts
     */
    public List<BankEntity> getAllBankAccount() {
        return bankRepo.findAll();
    }

    /**
     * Get bank account by ID
     */
    public BankEntity getBankAccountsById(Integer id) {
        Optional<BankEntity> account = bankRepo.findById(id);
        return account.orElse(null);
    }

    /**
     * Delete bank account by ID
     */
    public void deleteBankAccounts(Integer id) {
        bankRepo.deleteById(id);
    }

    /**
     * Search customers by name
     */
    public List<BankEntity> searchCustomersByName(String searchTerm) {
        return bankRepo.searchByName(searchTerm);
    }

    /**
     * Get active customers count
     */
    public long getActiveCustomersCount() {
        return bankRepo.countActiveAccounts();
    }

    /**
     * Get total balance of all accounts
     */
    public double getTotalBalance() {
        return bankRepo.getTotalBalance();
    }
    public List<BankEntity> getAllCustomers() {
        return bankRepo.findAll(); // Assumes BankRepo extends JpaRepository<Customer, Integer>
    }
 // Add these methods to your BankService class

    /**
     * Get customers by account status
     */
    public List<BankEntity> getCustomersByStatus(String status) {
        try {
            return bankRepo.findByAccountStatus(status.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching customers by status: " + e.getMessage());
        }
    }

    /**
     * Get customers by account type
     */
    public List<BankEntity> getCustomersByAccountType(String accountType) {
        try {
            return bankRepo.findByAccountType(accountType.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching customers by account type: " + e.getMessage());
        }
    }

    /**
     * Get customers with balance greater than amount
     */
    public List<BankEntity> getCustomersWithBalanceGreaterThan(double amount) {
        try {
            return bankRepo.findAccountsWithBalanceGreaterThan(amount);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching customers by balance: " + e.getMessage());
        }
    }

    /**
     * Get customers with balance less than amount
     */
    public List<BankEntity> getCustomersWithBalanceLessThan(double amount) {
        try {
            return bankRepo.findAccountsWithBalanceLessThan(amount);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching customers by balance: " + e.getMessage());
        }
    }

    /**
     * Get customers with balance in range
     */
    public List<BankEntity> getCustomersWithBalanceBetween(double minAmount, double maxAmount) {
        try {
            return bankRepo.findAccountsWithBalanceBetween(minAmount, maxAmount);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching customers by balance range: " + e.getMessage());
        }
    }

    /**
     * Validate customer data before saving
     */
    public boolean validateCustomerData(BankEntity customer) {
        if (customer == null) return false;
        if (customer.getFirstname() == null || customer.getFirstname().trim().isEmpty()) return false;
        if (customer.getLastname() == null || customer.getLastname().trim().isEmpty()) return false;
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) return false;
        if (!customer.getEmail().contains("@")) return false;
        return true;
    }

    /**
     * Check if email is already taken (excluding current customer)
     */
    public boolean isEmailTaken(String email, Integer excludeCustomerId) {
        try {
            BankEntity existingCustomer = bankRepo.findByEmail(email);
            if (existingCustomer == null) return false;
            return !existingCustomer.getId().equals(excludeCustomerId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if phone number is already taken (excluding current customer)
     */
    public boolean isPhoneNoTaken(String phoneNo, Integer excludeCustomerId) {
        try {
            BankEntity existingCustomer = bankRepo.findByPhoneNo(phoneNo);
            if (existingCustomer == null) return false;
            return !existingCustomer.getId().equals(excludeCustomerId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            List<BankEntity> allCustomers = bankRepo.findAll();
            long activeCustomers = bankRepo.countActiveAccounts();
            double totalBalance = bankRepo.getTotalBalance();
            
            // Calculate additional stats
            long inactiveCustomers = allCustomers.size() - activeCustomers;
            long savingsAccounts = allCustomers.stream()
                .filter(c -> "SAVINGS".equalsIgnoreCase(c.getAccountType()))
                .count();
            long currentAccounts = allCustomers.stream()
                .filter(c -> "CURRENT".equalsIgnoreCase(c.getAccountType()))
                .count();
            
            stats.put("totalCustomers", allCustomers.size());
            stats.put("activeCustomers", activeCustomers);
            stats.put("inactiveCustomers", inactiveCustomers);
            stats.put("totalBalance", totalBalance);
            stats.put("savingsAccounts", savingsAccounts);
            stats.put("currentAccounts", currentAccounts);
            
            return stats;
        } catch (Exception e) {
            throw new RuntimeException("Error calculating dashboard stats: " + e.getMessage());
        }
    }

    /**
     * Bulk update customer status
     */
    @Transactional
    public int bulkUpdateCustomerStatus(List<Integer> customerIds, String newStatus) {
        try {
            int updatedCount = 0;
            for (Integer id : customerIds) {
                Optional<BankEntity> customerOpt = bankRepo.findById(id);
                if (customerOpt.isPresent()) {
                    BankEntity customer = customerOpt.get();
                    customer.setAccountStatus(newStatus.toUpperCase());
                    bankRepo.save(customer);
                    updatedCount++;
                }
            }
            return updatedCount;
        } catch (Exception e) {
            throw new RuntimeException("Error in bulk update: " + e.getMessage());
        }
    }

	public BankEntity getCustomerbyId(int id) {
		// TODO Auto-generated method stub
		return bankRepo.findById(id).orElse(null);
	}

	public void UpdateCustomer(BankEntity bank) {
		bankRepo.save(bank);
		
	}

	public void DeleteCustomer(int id) {
		// TODO Auto-generated method stub
		bankRepo.deleteById(id);
	}

	public BankEntity getBankByCategory(int id) {
		return bankRepo.findById(id).orElse(null);
	}

}