package com.example.spring_boot_project_api.service;

public class TransactionCheckResult {
    private final boolean paid;
    private final String transactionHash;

    public TransactionCheckResult(boolean paid ,String transactionHash){
        this.transactionHash = transactionHash;
        this.paid = paid;

    }

    public boolean isPaid() {
        return paid;
    }

    public String getTransactionHash() {
        return transactionHash;
    }
}
