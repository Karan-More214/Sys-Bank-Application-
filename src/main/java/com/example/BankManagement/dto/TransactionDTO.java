package com.example.BankManagement.dto;

import com.example.BankManagement.Entity.TransactionEntity;

public class TransactionDTO {

    private Long id;
    private String type;
    private Double amount;
    private String timestamp;

    public TransactionDTO() {
    }

    public static TransactionDTO fromEntity(TransactionEntity txn) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(txn.getId());
        dto.setType(txn.getType());
        dto.setAmount(txn.getAmount());
        dto.setTimestamp(txn.getTimestamp() != null ? txn.getTimestamp().toString() : null);
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
