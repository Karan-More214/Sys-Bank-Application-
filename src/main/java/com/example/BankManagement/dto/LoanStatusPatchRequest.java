package com.example.BankManagement.dto;

import jakarta.validation.constraints.NotBlank;

public class LoanStatusPatchRequest {

    @NotBlank(message = "New status is required")
    private String newStatus;

    // Optional - only meaningful when newStatus is REJECTED.
    private String reason;

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
