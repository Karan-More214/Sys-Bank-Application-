package com.example.BankManagement.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
public class BankEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 50)
    private String firstname;
    
    @Column(nullable = false, length = 50)
    private String lastname;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "phone_no", length = 15)
    private String phoneNo;
    
    @Column(name = "gender", length = 10)
    private String gender;
    
    @Column(name = "dob", length = 10)
    private String dob;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(nullable = false, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private Double balance = 0.0;
    
    @Column(name = "account_type", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'SAVINGS'")
    private String accountType = "SAVINGS";
    
    @Column(name = "account_status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private String accountStatus = "ACTIVE";
    
    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Constructors
    public BankEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public BankEntity(String firstname, String lastname, String email, String phoneNo, String address, Double balance, String dob) {
        this();
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phoneNo = phoneNo;
        this.address = address;
        this.balance = balance != null ? balance : 0.0;
        this.dob = dob;
    }

    public BankEntity(String firstname, String lastname, String email, String phoneNo, String address, Double balance, String dob, String gender) {
        this(firstname, lastname, email, phoneNo, address, balance, dob);
        this.gender = gender;
    }

    // JPA Lifecycle Methods
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        
        // Set default values
        if (this.accountType == null || this.accountType.trim().isEmpty()) {
            this.accountType = "SAVINGS";
        }
        if (this.accountStatus == null || this.accountStatus.trim().isEmpty()) {
            this.accountStatus = "ACTIVE";
        }
        if (this.balance == null) {
            this.balance = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
        this.updatedAt = LocalDateTime.now();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public Double getBalance() {
        return balance != null ? balance : 0.0;
    }

    public void setBalance(Double balance) {
        this.balance = balance != null ? balance : 0.0;
        this.updatedAt = LocalDateTime.now();
    }

    // Overloaded method for backward compatibility
    public void setBalance(double balance) {
        this.balance = balance;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAccountType() {
        return accountType != null ? accountType.toUpperCase() : "SAVINGS";
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType != null ? accountType.toUpperCase() : "SAVINGS";
        this.updatedAt = LocalDateTime.now();
    }

    public String getAccountStatus() {
        return accountStatus != null ? accountStatus.toUpperCase() : "ACTIVE";
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus != null ? accountStatus.toUpperCase() : "ACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public String getFullName() {
        return (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(accountStatus);
    }

    public boolean hasSufficientBalance(double amount) {
        return getBalance() >= amount;
    }

    public String getFormattedBalance() {
        return String.format("₹%.2f", getBalance());
    }

    public String getAccountTypeDisplay() {
        return getAccountType();
    }

    public String getAccountStatusDisplay() {
        return getAccountStatus();
    }

    public String getStatusClass() {
        if (accountStatus == null) return "status-active";
        switch (accountStatus.toUpperCase()) {
            case "ACTIVE":
                return "status-active";
            case "INACTIVE":
            case "SUSPENDED":
                return "status-inactive";
            case "CLOSED":
                return "status-closed";
            default:
                return "status-active";
        }
    }

    // Validation methods
    public boolean isValidEmail() {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public boolean isValidPhoneNo() {
        return phoneNo != null && phoneNo.matches("^[+]?[0-9]{10,15}$");
    }

    public boolean hasCompleteProfile() {
        return firstname != null && !firstname.trim().isEmpty() &&
               lastname != null && !lastname.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               isValidEmail();
    }

    @Override
    public String toString() {
        return "BankEntity [id=" + id + 
               ", firstname=" + firstname + 
               ", lastname=" + lastname + 
               ", email=" + email + 
               ", phoneNo=" + phoneNo + 
               ", gender=" + gender + 
               ", dob=" + dob + 
               ", address=" + address + 
               ", balance=" + balance + 
               ", accountType=" + accountType + 
               ", accountStatus=" + accountStatus + 
               ", createdAt=" + createdAt + 
               ", updatedAt=" + updatedAt + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BankEntity that = (BankEntity) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}