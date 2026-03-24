package com.smarttasker.app.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.smarttasker.app.R;
import com.smarttasker.app.data.model.Task;
import com.smarttasker.app.data.repo.AuthRepository;
import com.smarttasker.app.databinding.ActivityTaskListBinding;
import com.smarttasker.app.ui.auth.LoginActivity;

public class TaskListActivity extends AppCompatActivity implements TaskAdapter.Listener {
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_COMPLETED = "extra_completed";

    private ActivityTaskListBinding binding;
    private TaskListViewModel viewModel;
    private TaskAdapter adapter;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> viewModel.loadTasks());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new AuthRepository(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(TaskListViewModel.class);
        adapter = new TaskAdapter(this);
        binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTasks.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadTasks());

        binding.fabAdd.setOnClickListener(v ->
                editLauncher.launch(TaskEditActivity.intentNew(this)));

        viewModel.getTasks().observe(this, tasks -> {
            adapter.submit(tasks);
            boolean empty = tasks == null || tasks.isEmpty();
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
        viewModel.getLoading().observe(this, loading -> {
            binding.progress.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
            binding.swipeRefresh.setRefreshing(Boolean.TRUE.equals(loading));
        });
        viewModel.getError().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.loadTasks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AuthRepository(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onToggle(Task task, boolean completed) {
        viewModel.setCompleted(task, completed);
    }

    @Override
    public void onDelete(Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete)
                .setMessage("Remove this task?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> viewModel.deleteTask(task.id))
                .show();
    }

    @Override
    public void onOpen(Task task) {
        editLauncher.launch(TaskEditActivity.intentEdit(this, task));
    }
}
