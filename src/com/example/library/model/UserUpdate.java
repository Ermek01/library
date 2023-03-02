package com.example.library.model;

public class UserUpdate {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        String publicKey;
        String publicKeyName;
        String publicKeyExpirationDate ;

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
