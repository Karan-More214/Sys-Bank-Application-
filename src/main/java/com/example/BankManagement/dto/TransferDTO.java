package com.example.BankManagement.dto;

import com.example.BankManagement.Entity.TransferEntity;

public class TransferDTO {

    private Long id;
    private String fromEmail;
    private String toEmail;
    private Double amount;
    private String note;
    private String status;
    private String createdAt;
    private String reviewedByName;
    private String reverseReason;

    public TransferDTO() {
    }

    public static TransferDTO fromEntity(TransferEntity transfer) {
        if (transfer == null) return null;
        TransferDTO dto = new TransferDTO();
        dto.setId(transfer.getId());
        dto.setFromEmail(transfer.getFromEmail());
        dto.setToEmail(transfer.getToEmail());
        dto.setAmount(transfer.getAmount());
        dto.setNote(transfer.getNote());
        dto.setStatus(transfer.getStatus());
        dto.setCreatedAt(transfer.getCreatedAt() != null ? transfer.getCreatedAt().toString() : null);
        dto.setReviewedByName(transfer.getReviewedBy() != null ? transfer.getReviewedBy().getFullName() : null);
        dto.setReverseReason(transfer.getReverseReason());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public String getReverseReason() { return reverseReason; }
    public void setReverseReason(String reverseReason) { this.reverseReason = reverseReason; }
}
