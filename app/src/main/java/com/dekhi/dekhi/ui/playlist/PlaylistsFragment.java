package com.dekhi.dekhi.ui.playlist;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.entity.Playlist;
import com.dekhi.dekhi.ui.base.NavigableFragment;
import com.dekhi.dekhi.ui.home.HomeViewModel;
import com.dekhi.dekhi.ui.home.PlaylistAdapter;
import com.dekhi.dekhi.util.ImportHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class PlaylistsFragment extends Fragment implements NavigableFragment {

    private HomeViewModel viewModel;
    private PlaylistAdapter playlistAdapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private RecyclerView recyclerView;

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
        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        recyclerView = view.findViewById(R.id.rv_playlists);
        setupRecyclerView(view);
        setupObservers(view);
        
        view.findViewById(R.id.btn_import).setOnClickListener(v -> ImportHelper.showImportDialog(this, viewModel, filePickerLauncher, null));

        android.widget.EditText etSearch = view.findViewById(R.id.et_search_playlists);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    viewModel.setSearchQuery(s.toString().trim());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    @Override
    public void onTabReselected() {
        if (recyclerView == null) return;
        if (recyclerView.canScrollVertically(-1)) {
            recyclerView.smoothScrollToPosition(0);
        } else {
            Toast.makeText(getContext(), "Syncing playlists...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView(View view) {
        playlistAdapter = new PlaylistAdapter(new PlaylistAdapter.OnPlaylistClickListener() {
            @Override
            public void onPlaylistClick(Playlist playlist) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.nav_host_fragment, ChannelsFragment.newInstance(playlist.getId(), playlist.getName()))
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onDeleteClick(Playlist playlist) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Playlist")
                        .setMessage("Are you sure you want to delete '" + playlist.getName() + "'? This will remove all channels.")
                        .setPositiveButton("Delete", (dialog, which) -> showUndoDeleteSnackbar(playlist))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        recyclerView.setAdapter(playlistAdapter);
    }

    private void showUndoDeleteSnackbar(Playlist playlist) {
        final boolean[] isDeleted = {true};
        Snackbar snackbar = Snackbar.make(requireView(), "Playlist '" + playlist.getName() + "' deleted", 5000)
                .setAction("UNDO", v -> isDeleted[0] = false)
                .setActionTextColor(requireContext().getColor(R.color.primary));

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (isDeleted[0]) {
                    viewModel.deletePlaylist(playlist);
                }
            }
        });
        snackbar.show();
    }

    private void setupObservers(View view) {
        viewModel.getPlaylistSearchResults().observe(getViewLifecycleOwner(), playlists -> {
            playlistAdapter.submitList(playlists);
            View emptyState = view.findViewById(R.id.empty_state);
            if (emptyState != null) {
                emptyState.setVisibility(playlists == null || playlists.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
}
