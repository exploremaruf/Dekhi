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

        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_playlists) {
                selectedFragment = new com.dekhi.dekhi.ui.playlist.PlaylistsFragment(); 
            } else if (itemId == R.id.nav_favorites) {
                selectedFragment = new FavoritesFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new HomeFragment())
                    .commit();
        }
    }
}
