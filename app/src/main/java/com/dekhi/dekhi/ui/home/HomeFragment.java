package com.dekhi.dekhi.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.entity.Channel;
import com.dekhi.dekhi.ui.base.NavigableFragment;
import com.dekhi.dekhi.ui.player.PlayerActivity;
import com.dekhi.dekhi.util.DekhiImageLoader;
import com.dekhi.dekhi.util.ImportHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HomeFragment extends Fragment implements NavigableFragment {

    private HomeViewModel viewModel;
    private ChannelAdapter recentAdapter;
    private ChannelAdapter favoritesAdapter;
    private PlaylistAdapter homePlaylistAdapter;
    private com.dekhi.dekhi.ui.playlist.CategoryAdapter categoryAdapter;
    private ImageView heroBackground;
    private EditText etSearchHome;
    private NestedScrollView nestedScrollView;
    
    private android.content.SharedPreferences prefs;
    private static final String PREF_LAST_CHANNEL_NAME = "last_channel_name";
    private static final String PREF_LAST_CHANNEL_LOGO = "last_channel_logo";
    private static final String PREF_LAST_CHANNEL_ID = "last_channel_id";
    private static final String PREF_LAST_CHANNEL_URL = "last_channel_url";
    private static final String PREF_LAST_PLAYLIST_ID = "last_playlist_id";

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(uri, 
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                Log.w("IPTV_DEBUG", "Permission error: " + e.getMessage());
                            }
                            String name = ImportHelper.getFileNameFromUri(requireContext(), uri);
                            ImportHelper.startImport(this, viewModel, name, uri.toString(), true, null);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        prefs = requireContext().getSharedPreferences("dekhi_prefs", android.content.Context.MODE_PRIVATE);

        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        heroBackground = view.findViewById(R.id.hero_image_background);
        loadLastChannelFromPrefs(view);

        setupRecyclerViews(view);
        setupObservers(view);
        setupHomeTools(view);
    }

    @Override
    public void onTabReselected() {
        if (nestedScrollView == null) return;

        if (nestedScrollView.getScrollY() > 0) {
            nestedScrollView.smoothScrollTo(0, 0);
        } else {
            refreshData();
        }
    }

    private void refreshData() {
        Toast.makeText(getContext(), "Refreshing cinematic feed...", Toast.LENGTH_SHORT).show();
    }

    private void setupHomeTools(View view) {
        View fabImport = view.findViewById(R.id.fab_import);
        if (fabImport != null) {
            fabImport.setOnClickListener(v -> ImportHelper.showImportDialog(this, viewModel, filePickerLauncher, null));
        }

        View btnImportEmpty = view.findViewById(R.id.btn_import_empty);
        if (btnImportEmpty != null) {
            btnImportEmpty.setOnClickListener(v -> ImportHelper.showImportDialog(this, viewModel, filePickerLauncher, null));
        }

        etSearchHome = view.findViewById(R.id.et_search_home);
        if (etSearchHome != null) {
            etSearchHome.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    viewModel.setSearchQuery(query);
                    if (!query.isEmpty()) {
                        Fragment current = getParentFragmentManager().findFragmentById(R.id.nav_host_fragment);
                        if (!(current instanceof SearchFragment)) {
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.nav_host_fragment, new SearchFragment())
                                    .addToBackStack("search")
                                    .commit();
                        }
                    } else {
                        getParentFragmentManager().popBackStack("search", androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void setupRecyclerViews(View view) {
        recentAdapter = new ChannelAdapter(R.layout.item_channel_compact, this::openPlayer);
        RecyclerView rvRecent = view.findViewById(R.id.rv_recent);
        if (rvRecent != null) rvRecent.setAdapter(recentAdapter);

        favoritesAdapter = new ChannelAdapter(R.layout.item_channel_compact, this::openPlayer);
        RecyclerView rvFavorites = view.findViewById(R.id.rv_favorites);
        if (rvFavorites != null) rvFavorites.setAdapter(favoritesAdapter);

        homePlaylistAdapter = new PlaylistAdapter(R.layout.item_playlist_home, new PlaylistAdapter.OnPlaylistClickListener() {
            @Override
            public void onPlaylistClick(com.dekhi.dekhi.data.entity.Playlist playlist) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.nav_host_fragment, com.dekhi.dekhi.ui.playlist.ChannelsFragment.newInstance(playlist.getId(), playlist.getName()))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(com.dekhi.dekhi.data.entity.Playlist playlist) {}
        });
        RecyclerView rvHomePlaylists = view.findViewById(R.id.rv_home_playlists);
        if (rvHomePlaylists != null) rvHomePlaylists.setAdapter(homePlaylistAdapter);

        categoryAdapter = new com.dekhi.dekhi.ui.playlist.CategoryAdapter(category -> {
            viewModel.setSearchQuery(category);
            if (etSearchHome != null) {
                etSearchHome.setText(category);
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
        });

        viewModel.getFavorites().observe(getViewLifecycleOwner(), channels -> {
            favoritesAdapter.submitList(channels);
            view.findViewById(R.id.section_favorites).setVisibility(channels != null && !channels.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setCategories(categories);
            view.findViewById(R.id.section_categories).setVisibility(categories != null && !categories.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            homePlaylistAdapter.submitList(playlists);
            view.findViewById(R.id.section_home_playlists).setVisibility(playlists != null && !playlists.isEmpty() ? View.VISIBLE : View.GONE);

            boolean noPlaylists = playlists == null || playlists.isEmpty();
            view.findViewById(R.id.empty_state).setVisibility(noPlaylists ? View.VISIBLE : View.GONE);
        });
    }

    private void loadLastChannelFromPrefs(View view) {
        String name = prefs.getString(PREF_LAST_CHANNEL_NAME, null);
        String logoUrl = prefs.getString(PREF_LAST_CHANNEL_LOGO, null);
        if (name != null) {
            updateHeroUI(view, name, logoUrl, true);
            
            long id = prefs.getLong(PREF_LAST_CHANNEL_ID, -1);
            String url = prefs.getString(PREF_LAST_CHANNEL_URL, null);
            long playlistId = prefs.getLong(PREF_LAST_PLAYLIST_ID, -1);
            
            if (id != -1 && url != null) {
                Channel minimalChannel = new Channel(playlistId, name, logoUrl, url, "");
                minimalChannel.setId(id);
                
                view.findViewById(R.id.btn_hero_primary).setOnClickListener(v -> openPlayer(minimalChannel));
            }
        }
    }

    private void updateHeroUI(View view, String name, String logoUrl, boolean isRecent) {
        TextView tvLabel = view.findViewById(R.id.tv_hero_label);
        TextView tvTitle = view.findViewById(R.id.tv_hero_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_hero_subtitle);
        com.google.android.material.button.MaterialButton btnPrimary = view.findViewById(R.id.btn_hero_primary);

        if (isRecent) {
            tvLabel.setText("Recently Visited");
            tvLabel.setTextColor(requireContext().getColor(R.color.primary));
            tvTitle.setText(name);
            tvSubtitle.setText("Continue where you left off");
            btnPrimary.setVisibility(View.VISIBLE);
            btnPrimary.setText("Continue Watching");
            
            if (heroBackground != null) {
                DekhiImageLoader.loadThumbnail(requireContext(), logoUrl, heroBackground);
            }
        } else {
            tvLabel.setText("Welcome to Dekhi");
            tvLabel.setTextColor(requireContext().getColor(R.color.text_med_emph));
            tvTitle.setText("Explore IPTV Channels");
            tvSubtitle.setText("Discover channels and playlists to start watching.");
            btnPrimary.setVisibility(View.GONE);

            if (heroBackground != null) {
                heroBackground.setImageResource(R.mipmap.ic_launcher);
                heroBackground.setAlpha(0.2f);
            }
        }
    }

    private void updateHero(View view, List<Channel> recent) {
        if (recent != null && !recent.isEmpty()) {
            Channel mostRecent = recent.get(0);
            
            prefs.edit()
                .putString(PREF_LAST_CHANNEL_NAME, mostRecent.getName())
                .putString(PREF_LAST_CHANNEL_LOGO, mostRecent.getLogoUrl())
                .putLong(PREF_LAST_CHANNEL_ID, mostRecent.getId())
                .putString(PREF_LAST_CHANNEL_URL, mostRecent.getStreamUrl())
                .putLong(PREF_LAST_PLAYLIST_ID, mostRecent.getPlaylistId())
                .apply();

            updateHeroUI(view, mostRecent.getName(), mostRecent.getLogoUrl(), true);
            
            MaterialButton btnPrimary = view.findViewById(R.id.btn_hero_primary);
            btnPrimary.setOnClickListener(v -> openPlayer(mostRecent));
        } else {
            if (prefs != null && prefs.getString(PREF_LAST_CHANNEL_NAME, null) == null) {
                updateHeroUI(view, null, null, false);
            }
        }
    }
}
