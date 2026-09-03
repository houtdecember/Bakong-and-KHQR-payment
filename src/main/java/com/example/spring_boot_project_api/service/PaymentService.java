package com.example.spring_boot_project_api.service;

import com.example.spring_boot_project_api.dto.PaymentCreateDTO;
import com.example.spring_boot_project_api.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {

    PaymentResponseDTO createPayment(PaymentCreateDTO createDTO);

    PaymentResponseDTO getPaymentById(Long id);

    List<PaymentResponseDTO> getAllPayments();

    byte[] getPaymentQrImage(Long paymentId) throws Exception;

//    STEP 14 — Payment Verification
    PaymentResponseDTO verifyPayment(Long id);

}
