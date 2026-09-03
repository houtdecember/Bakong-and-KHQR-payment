package com.example.spring_boot_project_api.service;

import java.math.BigDecimal;

public interface BakongService {

    KhqrResult generateIndividualKhqr(BigDecimal amount, String currency);

//    STEP 12 — Generate QR Image (ZXing)
    byte[] generateQrImage(String qrString) throws Exception;

//    boolean checkTransactionPaid(String md5Hash , BigDecimal expectedAmount, String expectedCurrency);

    TransactionCheckResult checkTransactionPaid(String md5Hash, BigDecimal expectedAmount, String expectedCurrency);

}
