package com.dekhi.dekhi.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.entity.Channel;

public class ChannelAdapter extends ListAdapter<Channel, ChannelAdapter.ViewHolder> {

    private final OnChannelClickListener listener;
    private final int layoutId;

    public ChannelAdapter(int layoutId, OnChannelClickListener listener) {
        super(DIFF_CALLBACK);
        this.layoutId = layoutId;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Channel> DIFF_CALLBACK = new DiffUtil.ItemCallback<Channel>() {
        @Override
        public boolean areItemsTheSame(@NonNull Channel oldItem, @NonNull Channel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Channel oldItem, @NonNull Channel newItem) {
            return oldItem.getName().equals(newItem.getName()) && 
                   oldItem.getStreamUrl().equals(newItem.getStreamUrl()) &&
                   oldItem.isFavorite() == newItem.isFavorite() &&
                   (oldItem.getLogoUrl() == null ? "" : oldItem.getLogoUrl()).equals(newItem.getLogoUrl() == null ? "" : newItem.getLogoUrl());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Channel channel = getItem(position);
        holder.tvName.setText(channel.getName());
        
        if (holder.ivLogo != null) {
            Glide.with(holder.itemView.getContext())
                .load(channel.getLogoUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.ivLogo);
        }

        holder.itemView.setOnClickListener(v -> listener.onChannelClick(channel));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivLogo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_channel_name);
            ivLogo = itemView.findViewById(R.id.iv_channel_logo);
        }
    }

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
    }
}
