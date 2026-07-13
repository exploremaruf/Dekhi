package com.dekhi.dekhi.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dekhi.dekhi.R;
import com.dekhi.dekhi.ui.base.NavigableFragment;
import com.dekhi.dekhi.util.ThemeHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment implements NavigableFragment {
    private HomeViewModel viewModel;
    private View scrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        scrollView = view.findViewById(R.id.settings_scroll_view);
        setupClickListeners(view);
        setupSwitches(view);
    }

    @Override
    public void onTabReselected() {
        if (scrollView != null && scrollView.getScrollY() > 0) {
            scrollView.scrollTo(0, 0);
        } else {
            Toast.makeText(getContext(), "Dekhi Settings v1.0", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSwitches(View view) {
        MaterialSwitch amoledSwitch = view.findViewById(R.id.setting_amoled);
        if (amoledSwitch != null) {
            amoledSwitch.setChecked(ThemeHelper.isAmoledMode(requireContext()));
            amoledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ThemeHelper.setAmoledMode(requireContext(), isChecked);
                if (getActivity() != null) getActivity().recreate();
            });
        }

        MaterialSwitch dynamicSwitch = view.findViewById(R.id.setting_dynamic);
        if (dynamicSwitch != null) {
            dynamicSwitch.setChecked(ThemeHelper.isDynamicColorsEnabled(requireContext()));
            dynamicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ThemeHelper.setDynamicColorsEnabled(requireContext(), isChecked);
                if (getActivity() != null) getActivity().recreate();
            });
        }
    }

    private void showThemeDialog() {
        String[] themes = {"Light", "Dark", "System Default"};
        int currentTheme = ThemeHelper.getSavedTheme(requireContext());
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Choose Theme")
                .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                    ThemeHelper.saveTheme(requireContext(), which);
                    dialog.dismiss();
                    if (getActivity() != null) getActivity().recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupClickListeners(View view) {
        View.OnClickListener listener = v -> Toast.makeText(requireContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();

        view.findViewById(R.id.setting_theme).setOnClickListener(v -> showThemeDialog());
        view.findViewById(R.id.setting_accent).setOnClickListener(listener);
        view.findViewById(R.id.setting_player).setOnClickListener(listener);
        view.findViewById(R.id.setting_resume).setOnClickListener(listener);
        view.findViewById(R.id.setting_hw).setOnClickListener(listener);
        view.findViewById(R.id.setting_buffer).setOnClickListener(listener);
        view.findViewById(R.id.setting_import).setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
                if (nav != null) nav.setSelectedItemId(R.id.nav_playlists);
            }
        });
        view.findViewById(R.id.setting_refresh).setOnClickListener(listener);
        view.findViewById(R.id.setting_auto_update).setOnClickListener(listener);
        view.findViewById(R.id.setting_clear_playlist_cache).setOnClickListener(listener);
        view.findViewById(R.id.setting_continue_watching).setOnClickListener(listener);
        view.findViewById(R.id.setting_recent_opened).setOnClickListener(listener);
        
        view.findViewById(R.id.setting_clear_history).setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear your watch history?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        viewModel.clearHistory();
                        Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        view.findViewById(R.id.setting_privacy).setOnClickListener(listener);
        view.findViewById(R.id.setting_terms).setOnClickListener(listener);
        view.findViewById(R.id.setting_licenses).setOnClickListener(listener);
    }
}
