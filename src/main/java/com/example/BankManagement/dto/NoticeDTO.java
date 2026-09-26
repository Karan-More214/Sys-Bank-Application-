package com.example.BankManagement.dto;

import com.example.BankManagement.Entity.NoticeEntity;

public class NoticeDTO {

    private Long id;
    private String title;
    private String message;
    private String createdByName;
    private String createdAt;
    private String updatedAt;
    private boolean active;

    public NoticeDTO() {
    }

    public static NoticeDTO fromEntity(NoticeEntity notice) {
        if (notice == null) return null;
        NoticeDTO dto = new NoticeDTO();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setMessage(notice.getMessage());
        dto.setCreatedByName(notice.getCreatedBy() != null ? notice.getCreatedBy().getFullName() : null);
        dto.setCreatedAt(notice.getCreatedAt() != null ? notice.getCreatedAt().toString() : null);
        dto.setUpdatedAt(notice.getUpdatedAt() != null ? notice.getUpdatedAt().toString() : null);
        dto.setActive(notice.isActive());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
