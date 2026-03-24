package com.smarttasker.app.data.network;

import com.smarttasker.app.data.model.JwtTokens;
import com.smarttasker.app.data.model.LoginBody;
import com.smarttasker.app.data.model.RegisterBody;
import com.smarttasker.app.data.model.Task;
import com.smarttasker.app.data.model.TaskRequest;
import com.smarttasker.app.data.model.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("register/")
    Call<UserResponse> register(@Body RegisterBody body);

    @POST("login/")
    Call<JwtTokens> login(@Body LoginBody body);

    @GET("tasks/")
    Call<List<Task>> listTasks();

    @POST("tasks/")
    Call<Task> createTask(@Body TaskRequest body);

    @PUT("tasks/{id}/")
    Call<Task> updateTask(@Path("id") String id, @Body TaskRequest body);

    @DELETE("tasks/{id}/")
    Call<Void> deleteTask(@Path("id") String id);
}
