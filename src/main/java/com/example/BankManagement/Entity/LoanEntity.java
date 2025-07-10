package com.example.BankManagement.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class LoanEntity {
	

    public class LoanStatus {

	}

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
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

	public LoanEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LoanEntity(int id, String firstname, String lastname, LocalDate dob, String email, String phoneNo,
			String address, String loanType, Double loanAmount, Integer loanYears, String employmentType,
			Double monthlyIncome, String purpose, UserEntity user) {
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
		this.user = user;
	}

	@Override
	public String toString() {
		return "LoanEntity [id=" + id + ", firstname=" + firstname + ", lastname=" + lastname + ", dob=" + dob
				+ ", email=" + email + ", phoneNo=" + phoneNo + ", address=" + address + ", loanType=" + loanType
				+ ", loanAmount=" + loanAmount + ", loanYears=" + loanYears + ", employmentType=" + employmentType
				+ ", monthlyIncome=" + monthlyIncome + ", purpose=" + purpose + ", user=" + user + "]";
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

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

}
