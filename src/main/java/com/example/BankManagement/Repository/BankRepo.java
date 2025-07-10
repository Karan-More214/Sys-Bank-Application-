package com.example.BankManagement.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.BankManagement.Entity.BankEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepo extends JpaRepository<BankEntity, Integer> {

    // ========== BASIC FINDER METHODS ==========
    
    /**
     * Find bank account by email
     */
    BankEntity findByEmail(String email);

    /**
     * Find bank account by email (Optional)
     */
    Optional<BankEntity> findOptionalByEmail(String email);

    /**
     * Find bank accounts by first name
     */
    List<BankEntity> findByFirstname(String firstname);

    /**
     * Find bank accounts by last name
     */
    List<BankEntity> findByLastname(String lastname);

    /**
     * Find bank accounts by first name and last name
     */
    List<BankEntity> findByFirstnameAndLastname(String firstname, String lastname);

    /**
     * Find bank account by phone number
     */
    BankEntity findByPhoneNo(String phoneNo);

    /**
     * Find bank account by phone number (Optional)
     */
    Optional<BankEntity> findOptionalByPhoneNo(String phoneNo);

    // ========== ACCOUNT TYPE & STATUS QUERIES ==========
    
    /**
     * Find bank accounts by account type
     */
    List<BankEntity> findByAccountType(String accountType);

    /**
     * Find bank accounts by account status
     */
    List<BankEntity> findByAccountStatus(String accountStatus);

    /**
     * Find active bank accounts
     */
    @Query("SELECT b FROM BankEntity b WHERE UPPER(b.accountStatus) = 'ACTIVE'")
    List<BankEntity> findActiveAccounts();

    /**
     * Find inactive bank accounts
     */
    @Query("SELECT b FROM BankEntity b WHERE UPPER(b.accountStatus) != 'ACTIVE'")
    List<BankEntity> findInactiveAccounts();

    /**
     * Find accounts by account type and status
     */
    @Query("SELECT b FROM BankEntity b WHERE UPPER(b.accountType) = UPPER(:accountType) AND UPPER(b.accountStatus) = UPPER(:status)")
    List<BankEntity> findByAccountTypeAndStatus(@Param("accountType") String accountType, @Param("status") String status);

    // ========== BALANCE QUERIES ==========
    
    /**
     * Find accounts with balance greater than specified amount
     */
    @Query("SELECT b FROM BankEntity b WHERE b.balance > :amount")
    List<BankEntity> findAccountsWithBalanceGreaterThan(@Param("amount") double amount);

    /**
     * Find accounts with balance less than specified amount
     */
    @Query("SELECT b FROM BankEntity b WHERE b.balance < :amount")
    List<BankEntity> findAccountsWithBalanceLessThan(@Param("amount") double amount);

    /**
     * Find accounts with balance between specified range
     */
    @Query("SELECT b FROM BankEntity b WHERE b.balance BETWEEN :minAmount AND :maxAmount")
    List<BankEntity> findAccountsWithBalanceBetween(@Param("minAmount") double minAmount, @Param("maxAmount") double maxAmount);

    /**
     * Find accounts with zero balance
     */
    @Query("SELECT b FROM BankEntity b WHERE b.balance = 0.0")
    List<BankEntity> findAccountsWithZeroBalance();

    /**
     * Find high value accounts (balance > specified amount and active)
     */
    @Query("SELECT b FROM BankEntity b WHERE b.balance > :amount AND UPPER(b.accountStatus) = 'ACTIVE' ORDER BY b.balance DESC")
    List<BankEntity> findHighValueAccounts(@Param("amount") double amount);

    // ========== EXISTENCE CHECKS ==========
    
    /**
     * Check if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number already exists
     */
    boolean existsByPhoneNo(String phoneNo);

    /**
     * Check if email exists excluding specific ID
     */
    @Query("SELECT COUNT(b) > 0 FROM BankEntity b WHERE b.email = :email AND b.id != :excludeId")
    boolean existsByEmailExcludingId(@Param("email") String email, @Param("excludeId") Integer excludeId);

    /**
     * Check if phone number exists excluding specific ID
     */
    @Query("SELECT COUNT(b) > 0 FROM BankEntity b WHERE b.phoneNo = :phoneNo AND b.id != :excludeId")
    boolean existsByPhoneNoExcludingId(@Param("phoneNo") String phoneNo, @Param("excludeId") Integer excludeId);

    // ========== STATISTICAL QUERIES ==========
    
    /**
     * Count total active accounts
     */
    @Query("SELECT COUNT(b) FROM BankEntity b WHERE UPPER(b.accountStatus) = 'ACTIVE'")
    long countActiveAccounts();

    /**
     * Count total inactive accounts
     */
    @Query("SELECT COUNT(b) FROM BankEntity b WHERE UPPER(b.accountStatus) != 'ACTIVE'")
    long countInactiveAccounts();

    /**
     * Count accounts by type
     */
    @Query("SELECT COUNT(b) FROM BankEntity b WHERE UPPER(b.accountType) = UPPER(:accountType)")
    long countByAccountType(@Param("accountType") String accountType);

    /**
     * Count accounts by status
     */
    @Query("SELECT COUNT(b) FROM BankEntity b WHERE UPPER(b.accountStatus) = UPPER(:status)")
    long countByAccountStatus(@Param("status") String status);

    /**
     * Get total balance of all accounts
     */
    @Query("SELECT COALESCE(SUM(b.balance), 0) FROM BankEntity b WHERE UPPER(b.accountStatus) = 'ACTIVE'")
    Double getTotalBalance();

    /**
     * Get total balance by account type
     */
    @Query("SELECT COALESCE(SUM(b.balance), 0) FROM BankEntity b WHERE UPPER(b.accountType) = UPPER(:accountType) AND UPPER(b.accountStatus) = 'ACTIVE'")
    Double getTotalBalanceByAccountType(@Param("accountType") String accountType);

    /**
     * Get average balance of all active accounts
     */
    @Query("SELECT COALESCE(AVG(b.balance), 0) FROM BankEntity b WHERE UPPER(b.accountStatus) = 'ACTIVE'")
    Double getAverageBalance();

    // ========== SEARCH QUERIES ==========
    
    /**
     * Search accounts by name (first name or last name containing the search term)
     */
    @Query("SELECT b FROM BankEntity b WHERE " +
           "LOWER(b.firstname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.lastname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(b.firstname, ' ', b.lastname)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BankEntity> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Search accounts by email containing search term
     */
    @Query("SELECT b FROM BankEntity b WHERE LOWER(b.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BankEntity> searchByEmail(@Param("searchTerm") String searchTerm);

    /**
     * Search accounts by phone number containing search term
     */
    @Query("SELECT b FROM BankEntity b WHERE b.phoneNo LIKE CONCAT('%', :searchTerm, '%')")
    List<BankEntity> searchByPhoneNo(@Param("searchTerm") String searchTerm);

    /**
     * Global search across multiple fields
     */
    @Query("SELECT b FROM BankEntity b WHERE " +
           "LOWER(b.firstname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.lastname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "b.phoneNo LIKE CONCAT('%', :searchTerm, '%') OR " +
           "CAST(b.id AS string) LIKE CONCAT('%', :searchTerm, '%')")
    List<BankEntity> globalSearch(@Param("searchTerm") String searchTerm);

    // ========== DATE-BASED QUERIES ==========
    
    /**
     * Find accounts created between dates
     */
    @Query("SELECT b FROM BankEntity b WHERE b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
    List<BankEntity> findAccountsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find accounts created today
     */
    @Query("SELECT b FROM BankEntity b WHERE DATE(b.createdAt) = CURRENT_DATE ORDER BY b.createdAt DESC")
    List<BankEntity> findAccountsCreatedToday();

    /**
     * Find accounts created this month
     */
    @Query("SELECT b FROM BankEntity b WHERE YEAR(b.createdAt) = YEAR(CURRENT_DATE) AND MONTH(b.createdAt) = MONTH(CURRENT_DATE) ORDER BY b.createdAt DESC")
    List<BankEntity> findAccountsCreatedThisMonth();

    /**
     * Find accounts updated after specific date
     */
    @Query("SELECT b FROM BankEntity b WHERE b.updatedAt > :date ORDER BY b.updatedAt DESC")
    List<BankEntity> findAccountsUpdatedAfter(@Param("date") LocalDateTime date);

    // ========== SORTING AND PAGINATION SUPPORT ==========
    
    /**
     * Find all accounts ordered by balance descending
     */
    @Query("SELECT b FROM BankEntity b ORDER BY b.balance DESC")
    List<BankEntity> findAllOrderByBalanceDesc();

    /**
     * Find all accounts ordered by creation date descending
     */
    @Query("SELECT b FROM BankEntity b ORDER BY b.createdAt DESC")
    List<BankEntity> findAllOrderByCreatedAtDesc();

    /**
     * Find all accounts ordered by name
     */
    @Query("SELECT b FROM BankEntity b ORDER BY b.firstname ASC, b.lastname ASC")
    List<BankEntity> findAllOrderByName();

    /**
     * Find top N customers by balance
     */
    @Query("SELECT b FROM BankEntity b WHERE UPPER(b.accountStatus) = 'ACTIVE' ORDER BY b.balance DESC")
    List<BankEntity> findTopCustomersByBalance();

    // ========== BULK OPERATIONS ==========
    
    /**
     * Update account status for multiple accounts
     */
    @Modifying
    @Transactional
    @Query("UPDATE BankEntity b SET b.accountStatus = UPPER(:status), b.updatedAt = CURRENT_TIMESTAMP WHERE b.id IN :ids")
    int bulkUpdateAccountStatus(@Param("ids") List<Integer> ids, @Param("status") String status);

    /**
     * Update account type for multiple accounts
     */
    @Modifying
    @Transactional
    @Query("UPDATE BankEntity b SET b.accountType = UPPER(:accountType), b.updatedAt = CURRENT_TIMESTAMP WHERE b.id IN :ids")
    int bulkUpdateAccountType(@Param("ids") List<Integer> ids, @Param("accountType") String accountType);

    // ========== CUSTOM DASHBOARD QUERIES ==========
    
    /**
     * Get account statistics grouped by type
     */
    @Query("SELECT b.accountType, COUNT(b), COALESCE(SUM(b.balance), 0) FROM BankEntity b WHERE UPPER(b.accountStatus) = 'ACTIVE' GROUP BY b.accountType")
    List<Object[]> getAccountStatsByType();

    /**
     * Get account statistics grouped by status
     */
    @Query("SELECT b.accountStatus, COUNT(b), COALESCE(SUM(b.balance), 0) FROM BankEntity b GROUP BY b.accountStatus")
    List<Object[]> getAccountStatsByStatus();

    /**
     * Get monthly registration count for current year
     */
    @Query("SELECT MONTH(b.createdAt), COUNT(b) FROM BankEntity b WHERE YEAR(b.createdAt) = YEAR(CURRENT_DATE) GROUP BY MONTH(b.createdAt) ORDER BY MONTH(b.createdAt)")
    List<Object[]> getMonthlyRegistrationStats();

    /**
     * Find dormant accounts (no transactions/updates in specified days)
     */
    @Query("SELECT b FROM BankEntity b WHERE b.updatedAt < :cutoffDate AND UPPER(b.accountStatus) = 'ACTIVE' ORDER BY b.updatedAt ASC")
    List<BankEntity> findDormantAccounts(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== SPECIALIZED QUERIES FOR ADMIN FEATURES ==========
    
    /**
     * Find customers with complete profiles
     */
    @Query("SELECT b FROM BankEntity b WHERE b.firstname IS NOT NULL AND b.lastname IS NOT NULL AND b.email IS NOT NULL AND b.phoneNo IS NOT NULL AND b.address IS NOT NULL")
    List<BankEntity> findCustomersWithCompleteProfiles();

    /**
     * Find customers with incomplete profiles
     */
    @Query("SELECT b FROM BankEntity b WHERE b.firstname IS NULL OR b.lastname IS NULL OR b.email IS NULL OR b.phoneNo IS NULL OR b.address IS NULL")
    List<BankEntity> findCustomersWithIncompleteProfiles();

    /**
     * Find recently active customers (updated within specified days)
     */
    @Query("SELECT b FROM BankEntity b WHERE b.updatedAt >= :cutoffDate ORDER BY b.updatedAt DESC")
    List<BankEntity> findRecentlyActiveCustomers(@Param("cutoffDate") LocalDateTime cutoffDate);
}