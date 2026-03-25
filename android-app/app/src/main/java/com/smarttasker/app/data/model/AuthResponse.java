package com.smarttasker.app.data.model;

/** Response from POST /api/login/ (SimpleJWT): access + refresh. */
public class AuthResponse {
    public String access;
    public String refresh;
}
