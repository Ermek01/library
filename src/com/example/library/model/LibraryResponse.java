package com.example.library.model;

import com.example.library.model.user.UserData;
import com.google.gson.annotations.SerializedName;

public class LibraryResponse {
    @SerializedName("message")
    private String message;
    @SerializedName("result")
    private Boolean result;
    @SerializedName("anyData")
    private Object anyData;
    @SerializedName("action")
    private String action;
    @SerializedName("httpCode")
    private int httpCode;

    public LibraryResponse(String message, Boolean result, UserData anyData, String action) {
        this.message = message;
        this.result = result;
        this.anyData = anyData;
        this.action = action;
    }

    public LibraryResponse() {
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public void setResult(Boolean result) {
        this.result = result;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setAnyData(Object anyData) {
        this.anyData = anyData;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }
}