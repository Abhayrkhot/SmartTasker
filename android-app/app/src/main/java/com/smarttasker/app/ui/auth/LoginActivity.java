package com.smarttasker.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smarttasker.app.data.repo.AuthRepository;
import com.smarttasker.app.databinding.ActivityLoginBinding;
import com.smarttasker.app.ui.common.AuthUiState;
import com.smarttasker.app.ui.tasks.TaskListActivity;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (new AuthRepository(this).isLoggedIn()) {
            startActivity(new Intent(this, TaskListActivity.class));
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getLoginState().observe(this, state -> {
            if (state == null) {
                return;
            }
            boolean busy = state.phase == AuthUiState.Phase.LOADING;
            binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            binding.buttonLogin.setEnabled(!busy);

            if (state.phase == AuthUiState.Phase.ERROR && state.errorMessage != null && !state.errorMessage.isEmpty()) {
                Snackbar.make(binding.getRoot(), state.errorMessage, Snackbar.LENGTH_LONG).show();
            }
            if (state.phase == AuthUiState.Phase.SUCCESS) {
                startActivity(new Intent(this, TaskListActivity.class));
                finish();
            }
        });

        binding.buttonLogin.setOnClickListener(v -> {
            String u = binding.editUsername.getText() != null ? binding.editUsername.getText().toString().trim() : "";
            String p = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(u, p);
        });

        binding.linkRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }
}
