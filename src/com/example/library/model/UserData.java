package com.example.library.model;

import java.io.Serializable;

public class UserData implements Serializable {

    private String id;
    private String version;

    public UserData(String id, String version) {
        this.id = id;
        this.version = version;
    }

    public String getCreatedUserId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

}
