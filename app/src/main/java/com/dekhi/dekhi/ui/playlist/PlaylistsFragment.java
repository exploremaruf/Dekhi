package com.dekhi.dekhi.ui.playlist;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.data.entity.Playlist;
import com.dekhi.dekhi.ui.home.HomeViewModel;
import com.dekhi.dekhi.ui.home.PlaylistAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class PlaylistsFragment extends Fragment {

    private HomeViewModel viewModel;
    private PlaylistAdapter playlistAdapter;
    private ActivityResultLauncher<android.content.Intent> filePickerIntentLauncher;
    private String tempPlaylistName = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filePickerIntentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        android.net.Uri uri = result.getData().getData();
                        if (uri != null) {
                            String fileName = getFileNameFromUri(uri);
                            if (tempPlaylistName.isEmpty() && !fileName.isEmpty()) {
                                tempPlaylistName = fileName;
                            }
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(uri, 
                                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                Log.w("IPTV_CLICK", "Could not take persistable permission: " + e.getMessage());
                            }
                            startImport(tempPlaylistName.isEmpty() ? "Local Playlist" : tempPlaylistName, uri.toString());
                        }
                    }
                }
        );
    }

    private String getFileNameFromUri(android.net.Uri uri) {
        String result = "";
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception e) {
                Log.e("IPTV_DEBUG", "Error querying file name", e);
            }
        }
        if (result.isEmpty()) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) result = result.substring(cut + 1);
        }
        if (result != null && result.contains(".")) {
            result = result.substring(0, result.lastIndexOf('.'));
        }
        return result;
    }

    private String getNameFromUrl(String url) {
        try {
            android.net.Uri uri = android.net.Uri.parse(url);
            String lastSegment = uri.getLastPathSegment();
            if (lastSegment != null && !lastSegment.isEmpty()) {
                if (lastSegment.contains(".")) {
                    lastSegment = lastSegment.substring(0, lastSegment.lastIndexOf('.'));
                }
                return lastSegment;
            }
        } catch (Exception ignored) {}
        return "Remote Playlist " + new java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
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

        setupRecyclerView(view);
        setupObservers(view);
        
        view.findViewById(R.id.btn_import).setOnClickListener(v -> showImportDialog());

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
                        .setMessage("Are you sure you want to delete '" + playlist.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> viewModel.deletePlaylist(playlist))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        RecyclerView rv = view.findViewById(R.id.rv_playlists);
        rv.setAdapter(playlistAdapter);
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

    private void showImportDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_import_playlist, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_playlist_name);
        TextInputEditText etUrl = dialogView.findViewById(R.id.et_playlist_url);
        MaterialButton btnPickFile = dialogView.findViewById(R.id.btn_pick_file);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.import_playlist)
                .setView(dialogView)
                .setPositiveButton(R.string.import_action, (d, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String url = etUrl.getText() != null ? etUrl.getText().toString().trim() : "";
                    if (!url.isEmpty()) {
                        if (name.isEmpty()) name = getNameFromUrl(url);
                        startImport(name, url);
                    } else {
                        Toast.makeText(requireContext(), "URL cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        btnPickFile.setOnClickListener(v -> {
            tempPlaylistName = etName.getText() != null ? etName.getText().toString().trim() : "";
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = {"application/x-mpegurl", "application/vnd.apple.mpegurl", "audio/x-mpegurl", "audio/mpegurl", "text/plain", "application/octet-stream"};
            intent.putExtra(android.content.Intent.EXTRA_MIME_TYPES, mimeTypes);
            try {
                filePickerIntentLauncher.launch(intent);
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(requireContext(), "No file picker found", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Implement blur on Android 12+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                dialog.getWindow().getAttributes().setBlurBehindRadius(30);
            }
        }
    }

    private void startImport(String name, String url) {
        View loadingView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null);
        AlertDialog loadingDialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(loadingView)
                .setCancelable(false)
                .create();
        loadingDialog.show();

        viewModel.importPlaylist(name, url, new com.dekhi.dekhi.data.PlaylistRepository.ImportCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    loadingDialog.dismiss();
                    Toast.makeText(requireContext(), "Import successful!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    loadingDialog.dismiss();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Import Failed")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        });
    }
}
