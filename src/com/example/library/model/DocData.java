package com.example.library.model;

public class DocData {
    private String docId;
    private String createdUserid;

    public String getDocId() {
        return docId;
    }

    public String getCreatedUserid() {
        return createdUserid;
    }

    @Override
    public String toString() {
        return docId + " " + createdUserid;
    }
}
