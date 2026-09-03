package com.example.spring_boot_project_api.dto;


import com.example.spring_boot_project_api.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCreateDTO {

    @NotNull
    private Long productId;

    @NotNull
    private  String currency;

    @NotNull
    private PaymentMethod paymentMethod;
}
