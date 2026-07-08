package com.dekhi.dekhi.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.entity.Channel;
import com.dekhi.dekhi.ui.player.PlayerActivity;

public class SearchFragment extends Fragment {

    private HomeViewModel viewModel;
    private ChannelAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_search_results);
        adapter = new ChannelAdapter(R.layout.item_channel, channel -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_URL, channel.getStreamUrl());
            intent.putExtra(PlayerActivity.EXTRA_NAME, channel.getName());
            intent.putExtra(PlayerActivity.EXTRA_CHANNEL_ID, channel.getId());
            intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_ID, channel.getPlaylistId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), channels -> {
            adapter.submitList(channels);
        });
    }
}
