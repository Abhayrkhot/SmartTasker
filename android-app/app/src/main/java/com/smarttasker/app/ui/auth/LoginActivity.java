package com.smarttasker.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smarttasker.app.data.repo.AuthRepository;
import com.smarttasker.app.databinding.ActivityLoginBinding;
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

        viewModel.getLoading().observe(this, loading -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.buttonLogin.setEnabled(!Boolean.TRUE.equals(loading));
        });
        viewModel.getError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.getSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
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
