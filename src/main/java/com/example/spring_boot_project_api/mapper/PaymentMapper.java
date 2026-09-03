package com.example.spring_boot_project_api.mapper;

import com.example.spring_boot_project_api.dto.PaymentResponseDTO;
import com.example.spring_boot_project_api.model.Payment;
import com.example.spring_boot_project_api.model.PaymentMethod;
import com.example.spring_boot_project_api.model.PaymentStatus;
import com.example.spring_boot_project_api.model.Product;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    /**
     * Builds a new Payment entity from already-validated inputs.
     * The amount is passed in directly because determining it
     * (looking up Product price) is Service-layer business logic,
     * not something the Mapper should decide.
     */

    public Payment toEntity(Product product, java.math.BigDecimal amount
                            , String currency, PaymentMethod paymentMethod,
                            String qrString, String md5Hash ){
        Payment payment = new Payment();
        payment.setProduct(product);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        payment.setQrString(qrString);
        payment.setMd5Hash(md5Hash);

        return payment;
    }

    public PaymentResponseDTO toResponseDTO(Payment payment){
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setProductId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentStatus(payment.getPaymentStatus());

        dto.setProductId(payment.getProduct().getId());

        dto.setQrString(payment.getQrString());
        dto.setMd5Hash(payment.getMd5Hash());

        dto.setTransactionId(payment.getTransactionId());

        return dto;
    }
}
