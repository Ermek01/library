package com.example.library.model.doc;

import com.google.gson.annotations.SerializedName;

public class DocData {
    @SerializedName("docId")
    private int docId;
    @SerializedName("createdUserId")
    private int createdUserId;
    @SerializedName("version")
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
