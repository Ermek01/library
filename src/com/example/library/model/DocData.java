package com.example.library.model;

public class DocData {
    private int docId;
    private int createdUserId;
    private int version;

    public int getVersion() {
        return version;
    }

    public int getDocId() {
        return docId;
    }

    public int getCreatedUserId() {
        return createdUserId;
    }

    @Override
    public String toString() {
        return docId + " " + createdUserId;
    }
}
