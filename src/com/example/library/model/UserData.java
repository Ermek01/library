package com.example.library.model;

public class UserData {

    private int createdUserId;
    private String username;
    private String email;

    public UserData(int createdUserId, String username, String email) {
        this.createdUserId = createdUserId;
        this.username = username;
        this.email = email;
    }

    public int getCreatedUserId() {
        return createdUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return createdUserId + " " + username + " " + email;
    }

}
