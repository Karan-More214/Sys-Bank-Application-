package com.example.BankManagement.dto;

import com.example.BankManagement.Entity.TicketEntity;

public class TicketDTO {

    private Long id;
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String adminResponse;
    private String resolvedByName;
    private String userFullName;
    private String userEmail;

    public TicketDTO() {
    }

    public static TicketDTO fromEntity(TicketEntity ticket) {
        if (ticket == null) return null;
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setCategory(ticket.getCategory());
        dto.setPriority(ticket.getPriority());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedAt(ticket.getCreatedAt() != null ? ticket.getCreatedAt().toString() : null);
        dto.setUpdatedAt(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().toString() : null);
        dto.setAdminResponse(ticket.getAdminResponse());
        dto.setResolvedByName(ticket.getResolvedBy() != null ? ticket.getResolvedBy().getFullName() : null);
        if (ticket.getUser() != null) {
            dto.setUserFullName(ticket.getUser().getFullName());
            dto.setUserEmail(ticket.getUser().getEmail());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public String getResolvedByName() { return resolvedByName; }
    public void setResolvedByName(String resolvedByName) { this.resolvedByName = resolvedByName; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
