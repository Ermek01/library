package com.example.library.model.key;

import com.google.gson.annotations.SerializedName;

public class ExKeyModel {
    @SerializedName("date")
    String date;
    @SerializedName("keyName")
    String keyName;

    public void setDate(String date) {
        this.date = date;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
