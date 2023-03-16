package com.example.library.model.doc;

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

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public void setCreatedUserId(int createdUserId) {
        this.createdUserId = createdUserId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return docId + " " + createdUserId;
    }
}
