package com.example.BankManagement.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class LoanEntity {
	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Personal Information
    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNo;

    @Column(nullable = false, length = 500)
    private String address;

    // Loan Details
    @Column(nullable = false)
    private String loanType;

    @Column(nullable = false)
    private Double loanAmount;

    @Column(nullable = false)
    private Integer loanYears;

    @Column(nullable = false)
    private String employmentType;

    @Column(nullable = false)
    private Double monthlyIncome;

    @Column(nullable = false, length = 500)
    private String purpose;
    
    private boolean isPaid = false; // default false

    
    public boolean isPaid() {
		return isPaid;
	}


	public void setPaid(boolean isPaid) {
		this.isPaid = isPaid;
	}

	// Loan Status
    @Column(nullable = false)
    private String status = "PENDING"; // Default status when loan is created
    
    // You can also use an enum for better type safety:
    // public enum LoanStatus { PENDING, APPROVED, REJECTED, UNDER_REVIEW }
    // @Enumerated(EnumType.STRING)
    // private LoanStatus status = LoanStatus.PENDING;
    public static class LoanStatusUpdateRequest {
        private int loanId;
        private String status;

        public int getLoanId() {
            return loanId;
        }

        public void setLoanId(int loanId) {
            this.loanId = loanId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    
    @Column(name = "application_date")
    private LocalDateTime applicationDate = LocalDateTime.now();

    // Audit trail for the approve/reject decision - null until an admin acts on the loan.
    @Column(length = 500)
    private String rejectionReason;

    @Column(name = "decided_by_admin_email")
    private String decidedByAdminEmail;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

	public LoanEntity() {
		super();
		// TODO Auto-generated constructor stub
	}


	@Override
	public String toString() {
		return "LoanEntity [id=" + id + ", firstname=" + firstname + ", lastname=" + lastname + ", dob=" + dob
				+ ", email=" + email + ", phoneNo=" + phoneNo + ", address=" + address + ", loanType=" + loanType
				+ ", loanAmount=" + loanAmount + ", loanYears=" + loanYears + ", employmentType=" + employmentType
				+ ", monthlyIncome=" + monthlyIncome + ", purpose=" + purpose + ", isPaid=" + isPaid + ", status="
				+ status + ", user=" + user + ", applicationDate=" + applicationDate + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLoanType() {
		return loanType;
	}

	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}

	public Double getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(Double loanAmount) {
		this.loanAmount = loanAmount;
	}

	public Integer getLoanYears() {
		return loanYears;
	}

	public void setLoanYears(Integer loanYears) {
		this.loanYears = loanYears;
	}

	public String getEmploymentType() {
		return employmentType;
	}

	public void setEmploymentType(String employmentType) {
		this.employmentType = employmentType;
	}

	public Double getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(Double monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}
	
	public LocalDateTime getApplicationDate() {
	    return applicationDate;
	}

	public void setApplicationDate(LocalDateTime applicationDate) {
	    this.applicationDate = applicationDate;
	}

	public String getRejectionReason() {
	    return rejectionReason;
	}

	public void setRejectionReason(String rejectionReason) {
	    this.rejectionReason = rejectionReason;
	}

	public String getDecidedByAdminEmail() {
	    return decidedByAdminEmail;
	}

	public void setDecidedByAdminEmail(String decidedByAdminEmail) {
	    this.decidedByAdminEmail = decidedByAdminEmail;
	}

	public LocalDateTime getDecisionDate() {
	    return decisionDate;
	}

	public void setDecisionDate(LocalDateTime decisionDate) {
	    this.decisionDate = decisionDate;
	}

	public LoanEntity(int id, String firstname, String lastname, LocalDate dob, String email, String phoneNo,
			String address, String loanType, Double loanAmount, Integer loanYears, String employmentType,
			Double monthlyIncome, String purpose, String status, UserEntity user, LocalDateTime applicationDate) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.dob = dob;
		this.email = email;
		this.phoneNo = phoneNo;
		this.address = address;
		this.loanType = loanType;
		this.loanAmount = loanAmount;
		this.loanYears = loanYears;
		this.employmentType = employmentType;
		this.monthlyIncome = monthlyIncome;
		this.purpose = purpose;
		this.status = status;
		this.user = user;
		this.applicationDate = applicationDate;
	}
}