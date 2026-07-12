package com.dekhi.dekhi.ui.playlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.ui.home.ChannelAdapter;
import com.dekhi.dekhi.ui.home.HomeViewModel;
import com.dekhi.dekhi.ui.player.PlayerActivity;

public class ChannelsFragment extends Fragment {

    private static final String ARG_PLAYLIST_ID = "playlist_id";
    private static final String ARG_PLAYLIST_NAME = "playlist_name";

    private long playlistId;
    private String playlistName;
    private HomeViewModel viewModel;
    private ChannelAdapter adapter;
    private CategoryAdapter categoryAdapter;

    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("All");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public static ChannelsFragment newInstance(long id, String name) {
        ChannelsFragment fragment = new ChannelsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLAYLIST_ID, id);
        args.putString(ARG_PLAYLIST_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playlistId = getArguments().getLong(ARG_PLAYLIST_ID);
            playlistName = getArguments().getString(ARG_PLAYLIST_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_channels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        View btnBack = view.findViewById(R.id.btn_back_simple);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        TextView tvTitle = view.findViewById(R.id.tv_title_simple);
        if (tvTitle != null) {
            tvTitle.setText(playlistName);
        }

        RecyclerView rv = view.findViewById(R.id.rv_channels);
        adapter = new ChannelAdapter(R.layout.item_channel, channel -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_URL, channel.getStreamUrl());
            intent.putExtra(PlayerActivity.EXTRA_NAME, channel.getName());
            intent.putExtra(PlayerActivity.EXTRA_CHANNEL_ID, channel.getId());
            intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_ID, playlistId);
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        categoryAdapter = new CategoryAdapter(category -> {
            currentCategory.setValue(category);
        });
        RecyclerView rvCategories = view.findViewById(R.id.rv_categories);
        rvCategories.setAdapter(categoryAdapter);

        android.widget.EditText etSearch = view.findViewById(R.id.et_search_channels);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery.setValue(s.toString().trim());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        MediatorLiveData<Pair<String, String>> filterState = new MediatorLiveData<>();
        filterState.addSource(currentCategory, cat -> 
            filterState.setValue(new Pair<>(cat, searchQuery.getValue()))
        );
        filterState.addSource(searchQuery, q -> 
            filterState.setValue(new Pair<>(currentCategory.getValue(), q))
        );

        Transformations.switchMap(filterState, pair -> 
            viewModel.getChannelsFiltered(playlistId, pair.first, pair.second)
        ).observe(getViewLifecycleOwner(), channels -> {
            adapter.submitList(channels);
        });

        viewModel.getCategories(playlistId).observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setCategories(categories);
        });
    }
}
