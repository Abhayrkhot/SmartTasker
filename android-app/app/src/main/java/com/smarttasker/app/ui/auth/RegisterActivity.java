package com.smarttasker.app.ui.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smarttasker.app.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel.getLoading().observe(this, loading -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.buttonRegister.setEnabled(!Boolean.TRUE.equals(loading));
        });
        viewModel.getError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.getSuccess().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
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
