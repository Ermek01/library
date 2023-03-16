package com.example.library.model;

import com.example.library.model.user.UserData;

public class LibraryResponse {

    private String message;

    private Boolean result;

    private Object anyData;

    private String action;

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
