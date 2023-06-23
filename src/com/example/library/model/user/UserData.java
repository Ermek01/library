package com.example.library.model.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserData implements Serializable {

    @SerializedName("id")
    private int id;
    @SerializedName("version")
    private int version;

    public UserData(int id, int version) {
        this.id = id;
        this.version = version;
    }

    public int getUserId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
