package com.example.spring_boot_project_api.exception;

public class BakongPaymentException extends RuntimeException{
    public BakongPaymentException(String message){
        super(message);
    }
}
