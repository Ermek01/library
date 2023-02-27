package com.example.library.model;

public class DocData {
    private String docId;
    private int createdUserid;

    public String getDocId() {
        return docId;
    }

    public int getCreatedUserid() {
        return createdUserid;
    }

    @Override
    public String toString() {
        return docId + " " + createdUserid;
    }
}
