package com.dekhi.dekhi;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.dekhi.dekhi.ui.home.FavoritesFragment;
import com.dekhi.dekhi.ui.home.HomeFragment;
import com.dekhi.dekhi.ui.home.SettingsFragment;
import com.dekhi.dekhi.util.ThemeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment, playlistsFragment, favoritesFragment, settingsFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        
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
                    .add(R.id.nav_host_fragment, settingsFragment, "4").hide(settingsFragment)
                    .add(R.id.nav_host_fragment, favoritesFragment, "3").hide(favoritesFragment)
                    .add(R.id.nav_host_fragment, playlistsFragment, "2").hide(playlistsFragment)
                    .add(R.id.nav_host_fragment, homeFragment, "1")
                    .commit();
        } else {
            homeFragment = getSupportFragmentManager().findFragmentByTag("1");
            playlistsFragment = getSupportFragmentManager().findFragmentByTag("2");
            favoritesFragment = getSupportFragmentManager().findFragmentByTag("3");
            settingsFragment = getSupportFragmentManager().findFragmentByTag("4");
            
            int selectedId = navView.getSelectedItemId();
            if (selectedId == R.id.nav_playlists) activeFragment = playlistsFragment;
            else if (selectedId == R.id.nav_favorites) activeFragment = favoritesFragment;
            else if (selectedId == R.id.nav_settings) activeFragment = settingsFragment;
            else activeFragment = homeFragment;
        }

        navView.setOnItemSelectedListener(item -> {
            Fragment targetFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                targetFragment = homeFragment;
            } else if (itemId == R.id.nav_playlists) {
                targetFragment = playlistsFragment;
            } else if (itemId == R.id.nav_favorites) {
                targetFragment = favoritesFragment;
            } else if (itemId == R.id.nav_settings) {
                targetFragment = settingsFragment;
            }

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
    }
}
