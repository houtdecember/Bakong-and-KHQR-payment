package com.example.spring_boot_project_api.service;

public class KhqrResult {

    private final String qrString;
    private final String md5Hash;

    public KhqrResult(String qrString , String md5Hash){
        this.md5Hash = md5Hash;
        this.qrString = qrString;
    }

    public String getQrString(){
        return qrString;
    }

    public String getMd5Hash(){
        return md5Hash;
    }
}
