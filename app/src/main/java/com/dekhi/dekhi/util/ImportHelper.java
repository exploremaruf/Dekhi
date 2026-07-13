package com.dekhi.dekhi.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.ui.home.HomeViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImportHelper {

    public interface ImportListener {
        void onImportStarted();
        void onImportFinished(boolean success, String message);
    }

    public static void showImportDialog(Fragment fragment, HomeViewModel viewModel, ActivityResultLauncher<Intent> filePickerLauncher, ImportListener listener) {
        Context context = fragment.requireContext();
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_import_playlist, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_playlist_name);
        TextInputEditText etUrl = dialogView.findViewById(R.id.et_playlist_url);
        MaterialButton btnPickFile = dialogView.findViewById(R.id.btn_pick_file);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.import_playlist)
                .setView(dialogView)
                .setPositiveButton(R.string.import_action, (d, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    String url = etUrl.getText() != null ? etUrl.getText().toString().trim() : "";
                    if (!url.isEmpty()) {
                        String lowerUrl = url.toLowerCase();
                        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
                            Toast.makeText(context, "Strictly http:// or https:// URLs only for manual input", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (name.isEmpty()) name = getNameFromUrl(url);
                        startImport(fragment, viewModel, name, url, false, listener);
                    } else {
                        Toast.makeText(context, "URL cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        btnPickFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = {"application/x-mpegurl", "application/vnd.apple.mpegurl", "audio/x-mpegurl", "audio/mpegurl", "text/plain", "application/octet-stream"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            try {
                filePickerLauncher.launch(intent);
                dialog.dismiss();
            } catch (Exception e) {
                Toast.makeText(context, "No file picker found", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                dialog.getWindow().getAttributes().setBlurBehindRadius(30);
            }
        }
    }

    public static void startImport(Fragment fragment, HomeViewModel viewModel, String name, String url, boolean isLocalFile, ImportListener listener) {
        if (!fragment.isAdded()) return;
        
        View loadingView = LayoutInflater.from(fragment.requireContext()).inflate(R.layout.dialog_loading, null);
        AlertDialog loadingDialog = new MaterialAlertDialogBuilder(fragment.requireContext())
                .setView(loadingView)
                .setCancelable(false)
                .create();
        loadingDialog.show();

        if (listener != null) listener.onImportStarted();

        viewModel.importPlaylist(name, url, isLocalFile, new com.dekhi.dekhi.data.PlaylistRepository.ImportCallback() {
            @Override
            public void onSuccess() {
                if (fragment.isAdded()) {
                    loadingDialog.dismiss();
                    Toast.makeText(fragment.requireContext(), "Import successful!", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onImportFinished(true, null);
                }
            }

            @Override
            public void onError(String message) {
                if (fragment.isAdded()) {
                    loadingDialog.dismiss();
                    new MaterialAlertDialogBuilder(fragment.requireContext())
                            .setTitle("Import Failed")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();
                    if (listener != null) listener.onImportFinished(false, message);
                }
            }
        });
    }

    public static String getFileNameFromUri(Context context, Uri uri) {
        String result = "";
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            } catch (Exception e) {
                e.printStackTrace();
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

    public static String getNameFromUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String lastSegment = uri.getLastPathSegment();
            if (lastSegment != null && !lastSegment.isEmpty()) {
                if (lastSegment.contains(".")) {
                    lastSegment = lastSegment.substring(0, lastSegment.lastIndexOf('.'));
                }
                return lastSegment;
            }
        } catch (Exception ignored) {}
        return "Remote Playlist " + new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date());
    }
}
