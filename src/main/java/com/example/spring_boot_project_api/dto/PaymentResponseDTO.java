package com.example.spring_boot_project_api.dto;

import com.example.spring_boot_project_api.model.PaymentMethod;
import com.example.spring_boot_project_api.model.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResponseDTO {

    private Long id;
    private Long productId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String qrString;
    private String md5Hash;
    private String transactionId;
}
