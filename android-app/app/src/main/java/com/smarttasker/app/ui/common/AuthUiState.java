package com.smarttasker.app.ui.common;

import androidx.annotation.Nullable;

/**
 * Unified UI state for login/register flows (MVVM): phase + optional error.
 */
public final class AuthUiState {
    public enum Phase {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    public final Phase phase;
    @Nullable
    public final String errorMessage;

    public AuthUiState(Phase phase, @Nullable String errorMessage) {
        this.phase = phase;
        this.errorMessage = errorMessage;
    }

    public static AuthUiState idle() {
        return new AuthUiState(Phase.IDLE, null);
    }

    public static AuthUiState loading() {
        return new AuthUiState(Phase.LOADING, null);
    }

    public static AuthUiState success() {
        return new AuthUiState(Phase.SUCCESS, null);
    }

    public static AuthUiState error(@Nullable String message) {
        return new AuthUiState(Phase.ERROR, message);
    }
}
