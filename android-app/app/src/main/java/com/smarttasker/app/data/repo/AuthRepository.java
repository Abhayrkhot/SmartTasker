package com.smarttasker.app.data.repo;

import android.content.Context;

import com.smarttasker.app.data.local.TokenManager;
import com.smarttasker.app.data.model.JwtTokens;
import com.smarttasker.app.data.model.LoginBody;
import com.smarttasker.app.data.model.RegisterBody;
import com.smarttasker.app.data.model.UserResponse;
import com.smarttasker.app.data.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class AuthRepository {
    private final TokenManager tokenManager;
    private final Context appContext;

    public AuthRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.tokenManager = new TokenManager(appContext);
    }

    public void register(String username, String password, String email, RepoCallback<UserResponse> cb) {
        RegisterBody body = new RegisterBody(username, password, email == null ? "" : email);
        ApiClient.get(appContext).api().register(body).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    cb.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void login(String username, String password, RepoCallback<JwtTokens> cb) {
        LoginBody body = new LoginBody(username, password);
        ApiClient.get(appContext).api().login(body).enqueue(new Callback<JwtTokens>() {
            @Override
            public void onResponse(Call<JwtTokens> call, Response<JwtTokens> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JwtTokens tokens = response.body();
                    tokenManager.saveTokens(tokens.access, tokens.refresh);
                    cb.onSuccess(tokens);
                } else {
                    cb.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<JwtTokens> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void logout() {
        tokenManager.clear();
        ApiClient.reset();
    }

    public boolean isLoggedIn() {
        return tokenManager.hasAccessToken();
    }

    private static String parseError(retrofit2.Response<?> response) {
        try {
            okhttp3.ResponseBody err = response.errorBody();
            if (err != null) {
                String s = err.string();
                if (s != null && !s.isEmpty()) {
                    return s.length() > 200 ? s.substring(0, 200) + "…" : s;
                }
            }
        } catch (Exception ignored) {
        }
        return "Request failed (" + response.code() + ")";
    }

    public interface RepoCallback<T> {
        void onSuccess(T data);

        void onError(String message);
    }
}
