package com.example.library.model;

public class LibraryResponse {

    private String message;

    private String document;
    private Boolean result;

    public LibraryResponse(String message, String document, Boolean result) {
        this.message = message;
        this.document = document;
        this.result = result;
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

    @Override
    public String toString() {
        return message + document + result.toString();
    }
}
