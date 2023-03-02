package com.example.library.model;

import java.io.Serializable;

public class UserData implements Serializable {

    private int id;
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

}
