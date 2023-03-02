package com.example.library.model;

public class DocUpdate {

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private boolean subscription;
        private String subscriptionData;
        private String subscriptionDate;

        private int version;

        public int getVersion() {
            return version;
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
    }
}