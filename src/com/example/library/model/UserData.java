package com.example.library.model;

import java.io.Serializable;

public class UserData implements Serializable {

    private String createdUserId;
    private String username;
    private String email;

    public UserData(String createdUserId, String username, String email) {
        this.createdUserId = createdUserId;
        this.username = username;
        this.email = email;
    }

    public String getCreatedUserId() {
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
