package com.example.library.model;

public class ExQrResult {
    private String action;
    private byte[] signingData;

    private String publicKey;

    private Boolean isDuplicate = false;



    public ExQrResult(String action, byte[] signingData, String publicKey, Boolean isDuplicate) {
        this.action = action;
        this.signingData = signingData;
        this.publicKey = publicKey;
        this.isDuplicate = isDuplicate;
    }

    public ExQrResult() {

    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setSigningData(byte[] signingData) {
        this.signingData = signingData;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setDuplicate(Boolean duplicate) {
        isDuplicate = duplicate;
    }
}
