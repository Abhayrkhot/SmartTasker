package com.smarttasker.app.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smarttasker.app.databinding.ActivityRegisterBinding;
import com.smarttasker.app.ui.common.AuthUiState;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel.getRegisterState().observe(this, state -> {
            if (state == null) {
                return;
            }
            boolean busy = state.phase == AuthUiState.Phase.LOADING;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.buttonRegister.setEnabled(!busy);

            if (state.phase == AuthUiState.Phase.ERROR && state.errorMessage != null && !state.errorMessage.isEmpty()) {
                Snackbar.make(binding.getRoot(), state.errorMessage, Snackbar.LENGTH_LONG).show();
            }
            if (state.phase == AuthUiState.Phase.SUCCESS) {
                Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        binding.buttonRegister.setOnClickListener(v -> {
            String u = binding.editUsername.getText() != null ? binding.editUsername.getText().toString().trim() : "";
            String p = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
            String e = binding.editEmail.getText() != null ? binding.editEmail.getText().toString().trim() : "";
            if (u.isEmpty() || p.length() < 8) {
                Toast.makeText(this, "Username required and password min 8 characters", Toast.LENGTH_LONG).show();
                return;
            }
            viewModel.register(u, p, e);
        });

        binding.linkLogin.setOnClickListener(v -> finish());
    }
}
