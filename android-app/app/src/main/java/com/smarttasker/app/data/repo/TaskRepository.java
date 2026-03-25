package com.smarttasker.app.data.repo;

import android.content.Context;

import com.smarttasker.app.data.model.Task;
import com.smarttasker.app.data.model.TaskRequest;
import com.smarttasker.app.data.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Task CRUD; networking only (MVVM repositories call into this). */
public final class TaskRepository {
    private final Context appContext;

    public TaskRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void getTasks(RepoCallback<List<Task>> cb) {
        ApiClient.get(appContext).api().getTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    cb.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void createTask(TaskRequest request, RepoCallback<Task> cb) {
        ApiClient.get(appContext).api().createTask(request).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    cb.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void updateTask(String id, TaskRequest request, RepoCallback<Task> cb) {
        ApiClient.get(appContext).api().updateTask(id, request).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cb.onSuccess(response.body());
                } else {
                    cb.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void deleteTask(String id, RepoCallback<Void> cb) {
        ApiClient.get(appContext).api().deleteTask(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cb.onSuccess(null);
                } else {
                    cb.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    private static String parseError(Response<?> response) {
        try {
            okhttp3.ResponseBody err = response.errorBody();
            if (err != null) {
                String s = err.string();
                if (s != null && !s.isEmpty()) {
                    return s.length() > 200 ? s.substring(0, 200) + "…" : s;
                }
            }
        } catch (Exception ignored) {
        }
        return "Request failed (" + response.code() + ")";
    }

    public interface RepoCallback<T> {
        void onSuccess(T data);

        void onError(String message);
    }
}
