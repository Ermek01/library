package com.example.library.model;

import java.io.Serializable;

public class UserData implements Serializable {

    private String id;
    private String username;
    private String email;

    public UserData(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public String getCreatedUserId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return id + " " + username + " " + email;
    }

}
