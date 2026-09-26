package com.example.BankManagement.dto;

import jakarta.validation.constraints.NotBlank;

public class TransferStatusPatchRequest {

    @NotBlank(message = "New status is required")
    private String newStatus;

    private String reason;

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
