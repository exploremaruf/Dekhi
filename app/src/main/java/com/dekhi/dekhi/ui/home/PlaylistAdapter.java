package com.dekhi.dekhi.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.entity.Playlist;

public class PlaylistAdapter extends ListAdapter<Playlist, PlaylistAdapter.ViewHolder> {

    private final OnPlaylistClickListener listener;

    public PlaylistAdapter(OnPlaylistClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Playlist> DIFF_CALLBACK = new DiffUtil.ItemCallback<Playlist>() {
        @Override
        public boolean areItemsTheSame(@NonNull Playlist oldItem, @NonNull Playlist newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Playlist oldItem, @NonNull Playlist newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getUrl().equals(newItem.getUrl());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = getItem(position);
        holder.tvName.setText(playlist.getName());
        
        String snippet = playlist.getChannelPreviewSnippet();
        if (snippet != null && !snippet.isEmpty()) {
            holder.tvUrl.setText(snippet);
        } else {
            holder.tvUrl.setText(playlist.getUrl());
        }

        holder.itemView.setOnClickListener(v -> listener.onPlaylistClick(playlist));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(playlist));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvUrl;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_playlist_name);
            tvUrl = itemView.findViewById(R.id.tv_playlist_url);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
        void onDeleteClick(Playlist playlist);
    }
}
