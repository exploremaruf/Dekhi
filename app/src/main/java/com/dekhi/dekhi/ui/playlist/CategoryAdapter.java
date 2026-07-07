package com.dekhi.dekhi.ui.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dekhi.dekhi.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<String> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private String selectedCategory = "All";

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<String> newCategories) {
        categories.clear();
        categories.add("All");
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.chip.setText(category);
        holder.chip.setChecked(category.equals(selectedCategory));
        holder.chip.setOnClickListener(v -> {
            selectedCategory = category;
            listener.onCategoryClick(category);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = (Chip) itemView;
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }
}
