package com.example.spring_boot_project_api.service.impl;

import com.example.spring_boot_project_api.config.BakongConfig;
import com.example.spring_boot_project_api.exception.BakongPaymentException;
import com.example.spring_boot_project_api.service.BakongService;
import com.example.spring_boot_project_api.service.KhqrResult;

import com.example.spring_boot_project_api.service.TransactionCheckResult;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.IndividualInfo;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class BakongServiceImpl implements BakongService {

    private final BakongConfig bakongConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BAKONG_BASE_URL = "https://api-bakong.nbc.gov.kh";

    @Override
    public KhqrResult generateIndividualKhqr(BigDecimal amount, String currency) {

        IndividualInfo individualInfo = new IndividualInfo();
        individualInfo.setBakongAccountId(bakongConfig.getAccountId());
        individualInfo.setMerchantName(bakongConfig.getMerchantName());
        individualInfo.setMerchantCity(bakongConfig.getMerchantCity());
        individualInfo.setAmount(amount.doubleValue());
        individualInfo.setCurrency(
                "KHR".equalsIgnoreCase(currency) ? KHQRCurrency.KHR : KHQRCurrency.USD
        );

        // FIX: Set expiration time for dynamic KHQR (e.g., valid for 10 minutes)
        // Most Bakong SDK versions expect epoch milliseconds
        // Because an amount was provided (making this a dynamic KHQR transaction), the Bakong KHQR SDK requires an expiration timestamp to be set on the IndivitualInfo object.
        long expirationTimestamp = System.currentTimeMillis() + (bakongConfig.getExpirationMinutes() * 60L * 1000); // 7 minutes from now
        individualInfo.setExpirationTimestamp(expirationTimestamp);

        // Explicitly parameterize KHQRResponse with KHQRData
        KHQRResponse<KHQRData> response = BakongKHQR.generateIndividual(individualInfo);

        if (response.getKHQRStatus().getCode() != 0) {
            throw new BakongPaymentException(
                    "KHQR generation failed: " + response.getKHQRStatus().getMessage());
        }

        // Now getData() returns KHQRData, which contains getQr() and getMd5()
        return new KhqrResult(response.getData().getQr(), response.getData().getMd5());
    }

//    STEP 12 — Generate QR Image (ZXing)
    @Override
    public byte[] generateQrImage(String qrString) throws Exception{
        int width = 300;
        int height = 300;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrString, BarcodeFormat.QR_CODE,width,height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix,"PNG",outputStream);

        return outputStream.toByteArray();
    }

//    STEP 14 — Payment Verification
@Override
public TransactionCheckResult checkTransactionPaid(String md5Hash, BigDecimal expectedAmount, String expectedCurrency) {

    String url = BAKONG_BASE_URL + "/v1/check_transaction_by_md5";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bakongConfig.getApiToken());
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> body = Map.of("md5", md5Hash);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response;
    try {
        response = restTemplate.postForEntity(url, request, Map.class);
    } catch (Exception e) {
        return new TransactionCheckResult(false, null);
    }

    Map<String, Object> responseBody = response.getBody();
    if (responseBody == null) return new TransactionCheckResult(false, null);

    Number responseCode = (Number) responseBody.get("responseCode");
    if (responseCode == null || responseCode.intValue() != 0) {
        return new TransactionCheckResult(false, null);
    }

    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
    if (data == null) return new TransactionCheckResult(false, null);

    double paidAmount = ((Number) data.get("amount")).doubleValue();
    String paidCurrency = (String) data.get("currency");
    String hash = (String) data.get("hash");

    boolean amountMatches = BigDecimal.valueOf(paidAmount).compareTo(expectedAmount) == 0;
    boolean currencyMatches = expectedCurrency.equalsIgnoreCase(paidCurrency);

    return new TransactionCheckResult(amountMatches && currencyMatches, hash);
}

}