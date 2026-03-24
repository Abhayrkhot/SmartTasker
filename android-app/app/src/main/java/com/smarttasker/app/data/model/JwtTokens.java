package com.smarttasker.app.data.model;

/** Response from POST /api/login/ (djangorestframework-simplejwt). */
public class JwtTokens {
    public String access;
    public String refresh;
}
