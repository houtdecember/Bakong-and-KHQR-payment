package com.example.spring_boot_project_api.controller;

import com.example.spring_boot_project_api.dto.PaymentCreateDTO;
import com.example.spring_boot_project_api.dto.PaymentResponseDTO;
import com.example.spring_boot_project_api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentCreateDTO createDTO) {
        PaymentResponseDTO response = paymentService.createPayment(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id){
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments(){
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping(value = "/{id}/qr",produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPaymentQrImage(@PathVariable Long id) throws Exception{

        byte[] qrImage = paymentService.getPaymentQrImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @GetMapping("/{id}/verify")
    public ResponseEntity<PaymentResponseDTO> verifyPayment(@PathVariable Long id){
        return ResponseEntity.ok(paymentService.verifyPayment(id));
    }

}
