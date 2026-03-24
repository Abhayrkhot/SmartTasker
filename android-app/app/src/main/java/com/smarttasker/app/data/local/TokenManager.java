package com.smarttasker.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists JWT access/refresh tokens using {@link SharedPreferences} (private app storage).
 */
public final class TokenManager {
    private static final String PREFS = "smarttasker_auth";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveTokens(String access, String refresh) {
        prefs.edit()
                .putString(KEY_ACCESS, access)
                .putString(KEY_REFRESH, refresh)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public boolean hasAccessToken() {
        String t = getAccessToken();
        return t != null && !t.isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
