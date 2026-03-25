package com.smarttasker.app.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smarttasker.app.data.model.UserResponse;
import com.smarttasker.app.data.repo.AuthRepository;
import com.smarttasker.app.ui.common.AuthUiState;

/**
 * Register screen ViewModel: {@link #getRegisterState()} for unified loading/error/success.
 */
public class RegisterViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<AuthUiState> registerState = new MutableLiveData<>(AuthUiState.idle());

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public void register(String username, String password, String email) {
        registerState.postValue(AuthUiState.loading());
        String em = email == null ? "" : email.trim();
        authRepository.register(username.trim(), password, em, new AuthRepository.RepoCallback<UserResponse>() {
            @Override
            public void onSuccess(UserResponse data) {
                registerState.postValue(AuthUiState.success());
            }

            @Override
            public void onError(String message) {
                registerState.postValue(AuthUiState.error(message));
            }
        });
    }

    public LiveData<AuthUiState> getRegisterState() {
        return registerState;
    }
}
