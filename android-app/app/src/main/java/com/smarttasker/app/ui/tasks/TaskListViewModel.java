package com.smarttasker.app.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smarttasker.app.data.model.Task;
import com.smarttasker.app.data.model.TaskRequest;
import com.smarttasker.app.data.repo.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class TaskListViewModel extends AndroidViewModel {
    private final TaskRepository taskRepository;
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public TaskListViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
    }

    public void loadTasks() {
        loading.postValue(true);
        error.postValue(null);
        taskRepository.listTasks(new TaskRepository.RepoCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> data) {
                loading.postValue(false);
                tasks.postValue(data != null ? data : new ArrayList<>());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }

    public void deleteTask(String id) {
        taskRepository.deleteTask(id, new TaskRepository.RepoCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loadTasks();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void setCompleted(Task task, boolean completed) {
        TaskRequest req = new TaskRequest(
                task.title,
                task.description != null ? task.description : "",
                completed
        );
        taskRepository.updateTask(task.id, req, new TaskRepository.RepoCallback<Task>() {
            @Override
            public void onSuccess(Task data) {
                loadTasks();
            }

            @Override
            public void onError(String message) {
                error.postValue(message);
                loadTasks();
            }
        });
    }

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }
}
