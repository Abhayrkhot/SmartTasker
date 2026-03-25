package com.smarttasker.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smarttasker.app.data.model.AuthResponse;
import com.smarttasker.app.data.repo.AuthRepository;
import com.smarttasker.app.ui.common.AuthUiState;

/**
 * Login screen ViewModel: {@link #getLoginState()} carries phase (idle/loading/success/error).
 */
public class LoginViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<AuthUiState> loginState = new MutableLiveData<>(AuthUiState.idle());

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public void login(String username, String password) {
        loginState.postValue(AuthUiState.loading());
        authRepository.login(username.trim(), password, new AuthRepository.RepoCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse data) {
                loginState.postValue(AuthUiState.success());
            }

            @Override
            public void onError(String message) {
                loginState.postValue(AuthUiState.error(message));
            }
        });
    }

    public LiveData<AuthUiState> getLoginState() {
        return loginState;
    }
}
