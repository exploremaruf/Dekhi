package com.dekhi.dekhi.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dekhi.dekhi.R;

public class HeroAdapter extends RecyclerView.Adapter<HeroAdapter.HeroViewHolder> {

    private final int[] images;

    public HeroAdapter(int[] images) {
        this.images = images;
    }

    @NonNull
    @Override
    public HeroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hero_image, parent, false);
        return new HeroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HeroViewHolder holder, int position) {
        holder.imageView.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class HeroViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        HeroViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_hero_image);
        }
    }
}
