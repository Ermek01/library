package com.example.library.model.user;

import com.google.gson.annotations.SerializedName;

public class UserUpdate {
    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        @SerializedName("publicKey")
        String publicKey;
        @SerializedName("publicKeyName")
        String publicKeyName;
        @SerializedName("publicKeyExpirationDate")
        String publicKeyExpirationDate ;
        @SerializedName("version")
        int version;

        public String getPublicKeyExpirationDate() {
            return publicKeyExpirationDate;
        }

        public void setPublicKeyExpirationDate(String publicKeyExpirationDate) {
            this.publicKeyExpirationDate = publicKeyExpirationDate;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPublicKeyName() {
            return publicKeyName;
        }

        public void setPublicKeyName(String publicKeyName) {
            this.publicKeyName = publicKeyName;
        }

        public String getPeriod() {
            return publicKeyExpirationDate ;
        }

        public void setPeriod(String period) {
            this.publicKeyExpirationDate  = period;
        }
    }

}