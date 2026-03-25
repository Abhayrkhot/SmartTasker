package com.smarttasker.app.data.model;

/** Request body for POST /api/login/. */
public class LoginRequest {
    public String username;
    public String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
