package com.smarttasker.app.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smarttasker.app.data.model.Task;
import com.smarttasker.app.data.model.TaskRequest;
import com.smarttasker.app.data.repo.TaskRepository;

public class TaskEditViewModel extends AndroidViewModel {
    private final TaskRepository taskRepository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>();

    public TaskEditViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
    }

    /** Pass {@code taskId} null for create. */
    public void save(@Nullable String taskId, String title, String description, boolean completed) {
        loading.postValue(true);
        error.postValue(null);
        TaskRequest req = new TaskRequest(title.trim(), description != null ? description : "", completed);
        if (taskId == null || taskId.isEmpty()) {
            taskRepository.createTask(req, new TaskRepository.RepoCallback<Task>() {
                @Override
                public void onSuccess(Task data) {
                    loading.postValue(false);
                    saved.postValue(true);
                }

                @Override
                public void onError(String message) {
                    loading.postValue(false);
                    error.postValue(message);
                }
            });
        } else {
            taskRepository.updateTask(taskId, req, new TaskRepository.RepoCallback<Task>() {
                @Override
                public void onSuccess(Task data) {
                    loading.postValue(false);
                    saved.postValue(true);
                }

                @Override
                public void onError(String message) {
                    loading.postValue(false);
                    error.postValue(message);
                }
            });
        }
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }
}
