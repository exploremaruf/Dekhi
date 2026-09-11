package com.dekhi.dekhi;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dekhi.dekhi.ui.base.NavigableFragment;
import com.dekhi.dekhi.ui.home.FavoritesFragment;
import com.dekhi.dekhi.ui.home.HomeFragment;
import com.dekhi.dekhi.ui.home.SettingsFragment;
import com.dekhi.dekhi.util.ThemeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_HOME = "1";
    private static final String TAG_PLAYLISTS = "2";
    private static final String TAG_FAVORITES = "3";
    private static final String TAG_SETTINGS = "4";

    private Fragment homeFragment, playlistsFragment, favoritesFragment, settingsFragment;
    private Fragment activeFragment;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ThemeHelper.isDynamicColorsEnabled(this)) {
            com.google.android.material.color.DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        }
        ThemeHelper.applyTheme(this);
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (ThemeHelper.isAmoledMode(this)) {
            getWindow().getDecorView().setBackgroundColor(android.graphics.Color.BLACK);
        }

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.bottom_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(navView, (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, navBarInsets.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            playlistsFragment = new com.dekhi.dekhi.ui.playlist.PlaylistsFragment();
            favoritesFragment = new FavoritesFragment();
            settingsFragment = new SettingsFragment();
            activeFragment = homeFragment;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.nav_host_fragment, settingsFragment, TAG_SETTINGS).hide(settingsFragment)
                    .add(R.id.nav_host_fragment, favoritesFragment, TAG_FAVORITES).hide(favoritesFragment)
                    .add(R.id.nav_host_fragment, playlistsFragment, TAG_PLAYLISTS).hide(playlistsFragment)
                    .add(R.id.nav_host_fragment, homeFragment, TAG_HOME)
                    .commit();
        } else {
            homeFragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
            playlistsFragment = getSupportFragmentManager().findFragmentByTag(TAG_PLAYLISTS);
            favoritesFragment = getSupportFragmentManager().findFragmentByTag(TAG_FAVORITES);
            settingsFragment = getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);

            int selectedId = navView.getSelectedItemId();
            if (selectedId == R.id.nav_playlists) activeFragment = playlistsFragment;
            else if (selectedId == R.id.nav_favorites) activeFragment = favoritesFragment;
            else if (selectedId == R.id.nav_settings) activeFragment = settingsFragment;
            else activeFragment = homeFragment;
        }

        setupNavigationListeners();
        setupBackNavigation();
    }

    private void setupNavigationListeners() {
        navView.setOnItemSelectedListener(item -> {
            Fragment targetFragment;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) targetFragment = homeFragment;
            else if (itemId == R.id.nav_playlists) targetFragment = playlistsFragment;
            else if (itemId == R.id.nav_favorites) targetFragment = favoritesFragment;
            else if (itemId == R.id.nav_settings) targetFragment = settingsFragment;
            else return false;

            if (targetFragment != null && targetFragment != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(activeFragment)
                        .show(targetFragment)
                        .commit();
                activeFragment = targetFragment;
                return true;
            }
            return false;
        });

        navView.setOnItemReselectedListener(item -> {
            if (activeFragment instanceof NavigableFragment) {
                ((NavigableFragment) activeFragment).onTabReselected();
            }
        });
    }

    private void setupBackNavigation() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fm = getSupportFragmentManager();

                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                    return;
                }

                if (navView.getSelectedItemId() != R.id.nav_home) {
                    navView.setSelectedItemId(R.id.nav_home);
                    return;
                }

                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
