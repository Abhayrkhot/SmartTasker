package com.smarttasker.app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Mirrors the Django {@code Task} serializer (snake_case JSON keys).
 */
public class Task {
    public String id;
    public String title;
    public String description;

    @SerializedName("is_completed")
    public boolean completed;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;
}
