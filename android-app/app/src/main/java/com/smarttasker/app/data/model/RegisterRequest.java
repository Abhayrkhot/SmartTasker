package com.smarttasker.app.data.model;

/** Request body for POST /api/register/. */
public class RegisterRequest {
    public String username;
    public String email;
    public String password;

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
