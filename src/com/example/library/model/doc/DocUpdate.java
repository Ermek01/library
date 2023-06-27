package com.example.library.model.doc;

import com.google.gson.annotations.SerializedName;

public class DocUpdate {
    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
    //
    public static class Data {
        @SerializedName("subscription")
        private boolean subscription;
        @SerializedName("subscriptionData")
        private String subscriptionData;
        @SerializedName("subscriptionDate")
        private String subscriptionDate;
        @SerializedName("statusSelect")
        private int statusSelect;
        @SerializedName("version")
        private int version;
        @SerializedName("id")
        private int id;

        public int getVersion() {
            return version;
        }

        public int getStatusSelect() {
            return statusSelect;
        }

        public void setStatusSelect(int statusSelect) {
            this.statusSelect = statusSelect;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public boolean isSubscription() {
            return subscription;
        }

        public void setSubscription(boolean subscription) {
            this.subscription = subscription;
        }

        public String getSubscriptionData() {
            return subscriptionData;
        }

        public void setSubscriptionData(String subscriptionData) {
            this.subscriptionData = subscriptionData;
        }

        public String getSubscriptionDate() {
            return subscriptionDate;
        }

        public void setSubscriptionDate(String subscriptionDate) {
            this.subscriptionDate = subscriptionDate;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}

