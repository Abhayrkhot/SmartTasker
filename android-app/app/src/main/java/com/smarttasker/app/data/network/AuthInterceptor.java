package com.smarttasker.app.data.network;

import androidx.annotation.NonNull;

import com.smarttasker.app.data.local.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Attaches {@code Authorization: Bearer} for protected routes. */
public final class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();
        if (path.endsWith("register/") || path.endsWith("login/")) {
            return chain.proceed(original);
        }
        String token = tokenManager.getAccessToken();
        if (token != null && !token.isEmpty()) {
            Request.Builder b = original.newBuilder()
                    .header("Authorization", "Bearer " + token);
            return chain.proceed(b.build());
        }
        return chain.proceed(original);
    }
}
