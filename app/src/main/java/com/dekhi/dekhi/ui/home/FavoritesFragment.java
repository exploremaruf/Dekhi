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
import com.dekhi.dekhi.ui.player.PlayerActivity;

public class FavoritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        view.findViewById(R.id.toolbar).setVisibility(View.GONE);
        view.findViewById(R.id.rv_categories).setVisibility(View.GONE);

        RecyclerView rv = view.findViewById(R.id.rv_channels);
        ChannelAdapter adapter = new ChannelAdapter(R.layout.item_channel, channel -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_URL, channel.getStreamUrl());
            intent.putExtra(PlayerActivity.EXTRA_NAME, channel.getName());
            intent.putExtra(PlayerActivity.EXTRA_CHANNEL_ID, channel.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        viewModel.getFavorites().observe(getViewLifecycleOwner(), channels -> {
            adapter.submitList(channels);
        });
    }
}
