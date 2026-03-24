package com.smarttasker.app.data.model;

import com.google.gson.annotations.SerializedName;

/** Request body for create/update task endpoints. */
public class TaskRequest {
    public String title;
    public String description;

    @SerializedName("is_completed")
    public boolean completed;

    public TaskRequest(String title, String description, boolean completed) {
        this.title = title;
        this.description = description == null ? "" : description;
        this.completed = completed;
    }
}
