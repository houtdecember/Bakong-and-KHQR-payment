package com.example.spring_boot_project_api.service.impl;

import com.example.spring_boot_project_api.config.BakongConfig;
import com.example.spring_boot_project_api.dto.PaymentCreateDTO;
import com.example.spring_boot_project_api.dto.PaymentResponseDTO;
import com.example.spring_boot_project_api.exception.PaymentNotFoundException;
import com.example.spring_boot_project_api.mapper.PaymentMapper;
import com.example.spring_boot_project_api.model.Payment;
import com.example.spring_boot_project_api.model.PaymentStatus;
import com.example.spring_boot_project_api.model.Product;
import com.example.spring_boot_project_api.repository.PaymentRepository;
import com.example.spring_boot_project_api.repository.ProductRepository;
import com.example.spring_boot_project_api.service.BakongService;
import com.example.spring_boot_project_api.service.KhqrResult;
import com.example.spring_boot_project_api.service.PaymentService;
import com.example.spring_boot_project_api.service.TransactionCheckResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final PaymentMapper paymentMapper;
    private final BakongService bakongService;
//    private static final int EXPIRATION_MINUTES = 7;
    private final BakongConfig bakongConfig;

    @Override
    public PaymentResponseDTO createPayment(PaymentCreateDTO createDTO){
        Product product = productRepository.findById(createDTO.getProductId())
                .orElseThrow(()->new RuntimeException("Product not found id:"+createDTO.getProductId()));

        // Backend determines the amount — NEVER trust a client-sent amount
        var amount = product.getPrice();
        var currency = createDTO.getCurrency();

        // Generate the real KHQR string + MD5 via Bakong SDK
        KhqrResult khqrResult = bakongService.generateIndividualKhqr(amount, currency);

        Payment payment = paymentMapper.toEntity(
          product,
          amount,
          currency,
          createDTO.getPaymentMethod(),
                khqrResult.getQrString(),
                khqrResult.getMd5Hash()
        );

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponseDTO(saved);
    }

    @Override
    public PaymentResponseDTO getPaymentById(Long id){
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(()->new PaymentNotFoundException("Payment not found id: "+id));
        return paymentMapper.toResponseDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getAllPayments(){
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toResponseDTO)
                .toList();
    }

//    STEP 12 — Generate QR Image (ZXing)
    @Override
    public byte[] getPaymentQrImage(Long paymentId) throws Exception{
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(()-> new PaymentNotFoundException("Payment not found id :"+paymentId));

        return bakongService.generateQrImage(payment.getQrString());
    }

//    STEP 14 — Payment Verification
    @Override
    public PaymentResponseDTO verifyPayment(Long id){
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(()->new PaymentNotFoundException("Payment not found id:"+id));

        if (payment.getPaymentStatus() == PaymentStatus.PAID){
            return paymentMapper.toResponseDTO(payment);
        }

        // Check expiration BEFORE calling Bakong — no point verifying a dead payment
        if (payment.getPaymentStatus() == PaymentStatus.PENDING
            && payment.getCreatedAt().plusMinutes(bakongConfig.getExpirationMinutes()).isBefore(LocalDateTime.now())){

            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            return paymentMapper.toResponseDTO(payment);
        }

        TransactionCheckResult result = bakongService.checkTransactionPaid(
                payment.getMd5Hash(),
                payment.getAmount(),
                payment.getCurrency()
        );

        if (result.isPaid()) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionId(result.getTransactionHash());
            paymentRepository.save(payment);
        }

        return paymentMapper.toResponseDTO(payment);
    }




}
