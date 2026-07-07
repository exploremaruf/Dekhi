package com.dekhi.dekhi;

import android.os.Bundle;
import android.view.ViewGroup;

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
        // Apply theme before super.onCreate
        ThemeHelper.applyTheme(this);
        
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        
        // Apply Window Insets to the Header Container to prevent clipping
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_container), (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusBarInsets.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // Ensure nav view handles navigation bar insets to float properly
        ViewCompat.setOnApplyWindowInsetsListener(navView, (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            int baseMargin = (int) (28 * getResources().getDisplayMetrics().density);
            marginLayoutParams.bottomMargin = baseMargin + navBarInsets.bottom;
            v.setLayoutParams(marginLayoutParams);
            return insets;
        });

        // Handle FAB insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fab_import), (v, insets) -> {
            Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            int baseMargin = (int) (110 * getResources().getDisplayMetrics().density);
            lp.bottomMargin = baseMargin + navBarInsets.bottom;
            v.setLayoutParams(lp);
            return insets;
        });

        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_playlists) {
                // Playlists are managed in Home for this implementation
                selectedFragment = new HomeFragment(); 
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
