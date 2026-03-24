package com.smarttasker.app.ui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smarttasker.app.data.model.Task;
import com.smarttasker.app.databinding.ItemTaskBinding;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {
    public interface Listener {
        void onToggle(Task task, boolean completed);

        void onDelete(Task task);

        void onOpen(Task task);
    }

    private final List<Task> items = new ArrayList<>();
    private final Listener listener;

    public TaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Task> tasks) {
        items.clear();
        if (tasks != null) {
            items.addAll(tasks);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskBinding b = ItemTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Task t = items.get(position);
        holder.bind(t, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;

        VH(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task t, Listener listener) {
            binding.textTitle.setText(t.title);
            String desc = t.description != null ? t.description.trim() : "";
            if (desc.isEmpty()) {
                binding.textDescription.setVisibility(View.GONE);
            } else {
                binding.textDescription.setVisibility(View.VISIBLE);
                binding.textDescription.setText(desc);
            }

            binding.checkComplete.setOnCheckedChangeListener(null);
            binding.checkComplete.setChecked(t.completed);
            binding.checkComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (t.completed != isChecked) {
                    listener.onToggle(t, isChecked);
                }
            });

            binding.buttonDelete.setOnClickListener(v -> listener.onDelete(t));
            binding.getRoot().setOnClickListener(v -> listener.onOpen(t));
        }
    }
}
