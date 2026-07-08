package com.dekhi.dekhi;

import android.os.Bundle;
import android.view.View;
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
        ThemeHelper.applyTheme(this);
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_container), (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusBarInsets.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

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
                
                // We need to update visibility after commit, but commit is async.
                // However, since we're replacing the fragment, we can just call it or wait for backstack if we used it.
                // For bottom nav, we usually don't add to backstack.
                findViewById(R.id.app_bar).setVisibility(
                    (selectedFragment instanceof HomeFragment || 
                     selectedFragment instanceof com.dekhi.dekhi.ui.playlist.PlaylistsFragment) ? View.VISIBLE : View.GONE
                );
            }
            return true;
        });

        setupSearch();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new HomeFragment())
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateAppBarVisibility();
        });
    }

    private void updateAppBarVisibility() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        View appBar = findViewById(R.id.app_bar);
        if (appBar != null) {
            boolean shouldShow = current instanceof HomeFragment || 
                                 current instanceof com.dekhi.dekhi.ui.playlist.PlaylistsFragment ||
                                 current instanceof com.dekhi.dekhi.ui.home.SearchFragment;
            appBar.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    private void setupSearch() {
        android.widget.EditText etSearch = findViewById(R.id.et_search);
        if (etSearch != null) {
            com.dekhi.dekhi.ui.home.HomeViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.dekhi.dekhi.ui.home.HomeViewModel.class);
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    viewModel.setSearchQuery(query);
                    
                    if (!query.isEmpty()) {
                        Fragment current = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                        if (!(current instanceof com.dekhi.dekhi.ui.home.SearchFragment)) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.nav_host_fragment, new com.dekhi.dekhi.ui.home.SearchFragment())
                                    .addToBackStack("search")
                                    .commit();
                        }
                    } else {
                        // If query is empty, pop search from backstack if it's there
                        getSupportFragmentManager().popBackStack("search", androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }
}
