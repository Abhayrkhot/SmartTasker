package com.smarttasker.app.ui.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.smarttasker.app.R;
import com.smarttasker.app.data.model.Task;
import com.smarttasker.app.databinding.ActivityTaskEditBinding;

public class TaskEditActivity extends AppCompatActivity {
    private ActivityTaskEditBinding binding;
    private TaskEditViewModel viewModel;
    @Nullable
    private String taskId;

    public static Intent intentNew(Context context) {
        return new Intent(context, TaskEditActivity.class);
    }

    public static Intent intentEdit(Context context, Task task) {
        Intent i = new Intent(context, TaskEditActivity.class);
        i.putExtra(TaskListActivity.EXTRA_TASK_ID, task.id);
        i.putExtra(TaskListActivity.EXTRA_TITLE, task.title);
        i.putExtra(TaskListActivity.EXTRA_DESCRIPTION, task.description != null ? task.description : "");
        i.putExtra(TaskListActivity.EXTRA_COMPLETED, task.completed);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TaskEditViewModel.class);

        Intent in = getIntent();
        taskId = in.getStringExtra(TaskListActivity.EXTRA_TASK_ID);
        if (taskId != null && !taskId.isEmpty()) {
            binding.toolbar.setTitle(R.string.edit_task);
            binding.editTitle.setText(in.getStringExtra(TaskListActivity.EXTRA_TITLE));
            binding.editDescription.setText(in.getStringExtra(TaskListActivity.EXTRA_DESCRIPTION));
            binding.checkComplete.setChecked(in.getBooleanExtra(TaskListActivity.EXTRA_COMPLETED, false));
        } else {
            binding.toolbar.setTitle(R.string.new_task);
        }

        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(loading) ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.buttonSave.setEnabled(!Boolean.TRUE.equals(loading));
        });
        viewModel.getError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.getSaved().observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                setResult(RESULT_OK);
                finish();
            }
        });

        binding.buttonSave.setOnClickListener(v -> {
            String title = binding.editTitle.getText() != null ? binding.editTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                Snackbar.make(binding.getRoot(), "Title is required", Snackbar.LENGTH_SHORT).show();
                return;
            }
            String desc = binding.editDescription.getText() != null ? binding.editDescription.getText().toString() : "";
            boolean done = binding.checkComplete.isChecked();
            viewModel.save(taskId, title, desc, done);
        });
    }
}
