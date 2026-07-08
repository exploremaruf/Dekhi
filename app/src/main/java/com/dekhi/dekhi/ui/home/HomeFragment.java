package com.dekhi.dekhi.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.entity.Channel;
import com.dekhi.dekhi.ui.player.PlayerActivity;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private ChannelAdapter recentAdapter;
    private ChannelAdapter favoritesAdapter;
    private com.dekhi.dekhi.ui.playlist.CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupRecyclerViews(view);
        setupObservers(view);
        
        view.findViewById(R.id.btn_import_empty).setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                if (nav != null) nav.setSelectedItemId(R.id.nav_playlists);
            }
        });
    }

    private void setupRecyclerViews(View view) {
        recentAdapter = new ChannelAdapter(R.layout.item_channel_compact, this::openPlayer);
        RecyclerView rvRecent = view.findViewById(R.id.rv_recent);
        if (rvRecent != null) rvRecent.setAdapter(recentAdapter);

        favoritesAdapter = new ChannelAdapter(R.layout.item_channel_compact, this::openPlayer);
        RecyclerView rvFavorites = view.findViewById(R.id.rv_favorites);
        if (rvFavorites != null) rvFavorites.setAdapter(favoritesAdapter);

        categoryAdapter = new com.dekhi.dekhi.ui.playlist.CategoryAdapter(category -> {
            viewModel.setSearchQuery(category);
            // SearchFragment will be opened by MainActivity's search listener if I trigger it properly
            // Or I can open it manually
            if (getActivity() != null) {
                EditText etSearch = getActivity().findViewById(R.id.et_search);
                if (etSearch != null) etSearch.setText(category);
            }
        });
        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        if (rvCategories != null) rvCategories.setAdapter(categoryAdapter);
    }

    private void openPlayer(Channel channel) {
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_URL, channel.getStreamUrl());
        intent.putExtra(PlayerActivity.EXTRA_NAME, channel.getName());
        intent.putExtra(PlayerActivity.EXTRA_CHANNEL_ID, channel.getId());
        intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_ID, channel.getPlaylistId());
        startActivity(intent);
    }

    private void setupObservers(View view) {
        viewModel.getRecentChannels().observe(getViewLifecycleOwner(), channels -> {
            recentAdapter.submitList(channels);
            updateHero(view, channels);
            view.findViewById(R.id.section_recent).setVisibility(channels != null && !channels.isEmpty() ? View.VISIBLE : View.GONE);
            checkEmptyState(view);
        });

        viewModel.getFavorites().observe(getViewLifecycleOwner(), channels -> {
            favoritesAdapter.submitList(channels);
            view.findViewById(R.id.section_favorites).setVisibility(channels != null && !channels.isEmpty() ? View.VISIBLE : View.GONE);
            checkEmptyState(view);
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setCategories(categories);
            view.findViewById(R.id.section_categories).setVisibility(categories != null && !categories.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlists -> checkEmptyState(view));
    }

    private void checkEmptyState(View view) {
        viewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            boolean noPlaylists = playlists == null || playlists.isEmpty();
            view.findViewById(R.id.empty_state).setVisibility(noPlaylists ? View.VISIBLE : View.GONE);
        });
    }

    private void updateHero(View view, List<Channel> recent) {
        TextView tvLabel = view.findViewById(R.id.tv_hero_label);
        TextView tvTitle = view.findViewById(R.id.tv_hero_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_hero_subtitle);
        MaterialButton btnPrimary = view.findViewById(R.id.btn_hero_primary);
        MaterialButton btnSecondary = view.findViewById(R.id.btn_hero_secondary);
        ImageView ivPoster = view.findViewById(R.id.iv_hero_poster);

        if (recent != null && !recent.isEmpty()) {
            Channel mostRecent = recent.get(0);
            tvLabel.setText("Recently Visited");
            tvTitle.setText(mostRecent.getName());
            tvSubtitle.setText("Continue where you left off");
            btnPrimary.setText("Continue Watching");
            btnPrimary.setOnClickListener(v -> openPlayer(mostRecent));
            
            btnSecondary.setVisibility(View.VISIBLE);
            btnSecondary.setText("Open Channel");
            btnSecondary.setOnClickListener(v -> openPlayer(mostRecent));

            if (mostRecent.getLogoUrl() != null && !mostRecent.getLogoUrl().isEmpty()) {
                Glide.with(this).load(mostRecent.getLogoUrl()).placeholder(R.drawable.ic_launcher_background).into(ivPoster);
            }
        } else {
            tvLabel.setText("Explore");
            tvTitle.setText("Explore IPTV Channels");
            tvSubtitle.setText("Discover channels and playlists to start watching.");
            btnPrimary.setText("Browse Channels");
            btnPrimary.setOnClickListener(v -> {
                if (getActivity() != null) {
                    com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                    if (nav != null) nav.setSelectedItemId(R.id.nav_playlists);
                }
            });
            btnSecondary.setVisibility(View.GONE);
            ivPoster.setImageResource(R.drawable.ic_launcher_background);
        }
    }
}
