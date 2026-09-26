package com.example.BankManagement.dto;

import java.util.List;

public class DashboardResponse {

    private UserDTO user;
    private BankAccountDTO bankAccount;
    private boolean hasAccount;
    private List<TransactionDTO> transactions;

    public DashboardResponse() {
    }

    public DashboardResponse(UserDTO user, BankAccountDTO bankAccount, List<TransactionDTO> transactions) {
        this.user = user;
        this.bankAccount = bankAccount;
        this.hasAccount = bankAccount != null;
        this.transactions = transactions;
    }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public BankAccountDTO getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccountDTO bankAccount) { this.bankAccount = bankAccount; }

    public boolean isHasAccount() { return hasAccount; }
    public void setHasAccount(boolean hasAccount) { this.hasAccount = hasAccount; }

    public List<TransactionDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionDTO> transactions) { this.transactions = transactions; }
}
