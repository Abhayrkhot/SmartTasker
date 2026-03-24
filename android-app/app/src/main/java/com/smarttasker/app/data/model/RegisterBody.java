package com.smarttasker.app.data.model;

public class RegisterBody {
    public String username;
    public String password;
    public String email;

    public RegisterBody(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
