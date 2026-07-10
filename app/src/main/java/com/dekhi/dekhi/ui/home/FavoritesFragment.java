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

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private HomeViewModel viewModel;
    private ChannelAdapter adapter;
    private List<Channel> allFavorites = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_favorites);
        adapter = new ChannelAdapter(R.layout.item_channel_src, channel -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_URL, channel.getStreamUrl());
            intent.putExtra(PlayerActivity.EXTRA_NAME, channel.getName());
            intent.putExtra(PlayerActivity.EXTRA_CHANNEL_ID, channel.getId());
            intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_ID, channel.getPlaylistId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        viewModel.getFavorites().observe(getViewLifecycleOwner(), channels -> {
            allFavorites = channels;
            adapter.submitList(channels);
        });

        android.widget.EditText etSearch = view.findViewById(R.id.et_search_favorites);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterFavorites(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void filterFavorites(String query) {
        if (query.isEmpty()) {
            adapter.submitList(allFavorites);
        } else {
            List<Channel> filtered = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            for (Channel c : allFavorites) {
                if (c.getName().toLowerCase().contains(lowerQuery)) {
                    filtered.add(c);
                }
            }
            adapter.submitList(filtered);
        }
    }
}
