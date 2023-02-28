package com.example.library.model;

public class LibraryResponse {

    private String message;

    private String document;
    private Boolean result;

    private String anyData;

    private String action;

    private String userHash;

    public LibraryResponse(String message, String document, Boolean result, String anyData, String action, String userHash) {
        this.message = message;
        this.document = document;
        this.result = result;
        this.anyData = anyData;
        this.action = action;
        this.userHash = userHash;
    }

    public LibraryResponse() {
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setAnyData(String anyData) {
        this.anyData = anyData;
    }

    public void setUserHash(String userHash) {
        this.userHash = userHash;
    }

    @Override
    public String toString() {
        return message + document + result.toString();
    }
}
