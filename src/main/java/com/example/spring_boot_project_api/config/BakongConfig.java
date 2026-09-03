package com.example.spring_boot_project_api.config;

import kh.gov.nbc.bakong_khqr.BakongKHQR;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BakongConfig {

    @Value("${bakong.account-id}")
    private String accountId;

    @Value("${bakong.merchant-name}")
    private String merchantName;

    @Value("${bakong.merchant-city}")
    private String merchantCity;

    @Value("${bakong.base-url}")
    private String baseUrl;

    @Value("${bakong.api-token}")
    private String apiToken;


    @Value("${bakong.expiration-minutes}")
    private int expirationMinutes;

}
