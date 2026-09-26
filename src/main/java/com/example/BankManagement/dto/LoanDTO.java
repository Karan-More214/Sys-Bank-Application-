package com.example.BankManagement.dto;

import com.example.BankManagement.Entity.LoanEntity;

public class LoanDTO {

    private int id;
    private String firstname;
    private String lastname;
    private String dob;
    private String email;
    private String phoneNo;
    private String address;
    private String loanType;
    private Double loanAmount;
    private Integer loanYears;
    private String employmentType;
    private Double monthlyIncome;
    private String purpose;
    private String status;
    private boolean paid;
    private String applicationDate;
    private String rejectionReason;
    private String decidedByAdminEmail;
    private String decisionDate;

    public LoanDTO() {
    }

    public static LoanDTO fromEntity(LoanEntity loan) {
        if (loan == null) return null;
        LoanDTO dto = new LoanDTO();
        dto.setId(loan.getId());
        dto.setFirstname(loan.getFirstname());
        dto.setLastname(loan.getLastname());
        dto.setDob(loan.getDob() != null ? loan.getDob().toString() : null);
        dto.setEmail(loan.getEmail());
        dto.setPhoneNo(loan.getPhoneNo());
        dto.setAddress(loan.getAddress());
        dto.setLoanType(loan.getLoanType());
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setLoanYears(loan.getLoanYears());
        dto.setEmploymentType(loan.getEmploymentType());
        dto.setMonthlyIncome(loan.getMonthlyIncome());
        dto.setPurpose(loan.getPurpose());
        dto.setStatus(loan.getStatus());
        dto.setPaid(loan.isPaid());
        dto.setApplicationDate(loan.getApplicationDate() != null ? loan.getApplicationDate().toString() : null);
        dto.setRejectionReason(loan.getRejectionReason());
        dto.setDecidedByAdminEmail(loan.getDecidedByAdminEmail());
        dto.setDecisionDate(loan.getDecisionDate() != null ? loan.getDecisionDate().toString() : null);
        return dto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNo() { return phoneNo; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }

    public Integer getLoanYears() { return loanYears; }
    public void setLoanYears(Integer loanYears) { this.loanYears = loanYears; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getApplicationDate() { return applicationDate; }
    public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getDecidedByAdminEmail() { return decidedByAdminEmail; }
    public void setDecidedByAdminEmail(String decidedByAdminEmail) { this.decidedByAdminEmail = decidedByAdminEmail; }

    public String getDecisionDate() { return decisionDate; }
    public void setDecisionDate(String decisionDate) { this.decisionDate = decisionDate; }
}
