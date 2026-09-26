package com.example.BankManagement.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketRespondRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String adminResponse;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
}
