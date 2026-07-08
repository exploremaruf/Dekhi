package com.dekhi.dekhi.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dekhi.dekhi.R;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupClickListeners(view);
    }

    private void setupClickListeners(View view) {
        View.OnClickListener listener = v -> Toast.makeText(requireContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();

        view.findViewById(R.id.setting_theme).setOnClickListener(listener);
        view.findViewById(R.id.setting_accent).setOnClickListener(listener);
        view.findViewById(R.id.setting_dynamic).setOnClickListener(listener);
        view.findViewById(R.id.setting_player).setOnClickListener(listener);
        view.findViewById(R.id.setting_resume).setOnClickListener(listener);
        view.findViewById(R.id.setting_hw).setOnClickListener(listener);
        view.findViewById(R.id.setting_buffer).setOnClickListener(listener);
        view.findViewById(R.id.setting_import).setOnClickListener(listener);
        view.findViewById(R.id.setting_refresh).setOnClickListener(listener);
        view.findViewById(R.id.setting_auto_update).setOnClickListener(listener);
        view.findViewById(R.id.setting_clear_playlist_cache).setOnClickListener(listener);
        view.findViewById(R.id.setting_continue_watching).setOnClickListener(listener);
        view.findViewById(R.id.setting_recent_opened).setOnClickListener(listener);
        view.findViewById(R.id.setting_clear_history).setOnClickListener(listener);
        view.findViewById(R.id.setting_privacy).setOnClickListener(listener);
        view.findViewById(R.id.setting_terms).setOnClickListener(listener);
        view.findViewById(R.id.setting_licenses).setOnClickListener(listener);
    }
}
