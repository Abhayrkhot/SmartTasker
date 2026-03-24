package com.smarttasker.app.data.network;

import android.content.Context;

import com.smarttasker.app.BuildConfig;
import com.smarttasker.app.data.local.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Singleton Retrofit client with JWT interceptor. */
public final class ApiClient {
    private static volatile ApiClient instance;
    private final ApiService apiService;

    private ApiClient(Context appContext) {
        TokenManager tokenManager = new TokenManager(appContext);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.apiService = retrofit.create(ApiService.class);
    }

    public static ApiClient get(Context context) {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public ApiService api() {
        return apiService;
    }

    /** Call after logout so a new client picks up cleared tokens (new TokenManager state is same prefs — interceptor reads fresh values each request). */
    public static void reset() {
        instance = null;
    }
}
