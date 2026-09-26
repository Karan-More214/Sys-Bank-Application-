package com.example.BankManagement.dto;

public class NoticeUpdateRequest {

    private String title;
    private String message;
    private Boolean active;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
