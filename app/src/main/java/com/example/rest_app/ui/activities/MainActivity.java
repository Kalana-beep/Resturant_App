package com.example.rest_app.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.rest_app.R;
import com.example.rest_app.databinding.ActivityMainBinding;
import com.example.rest_app.ui.fragments.CartFragment;
import com.example.rest_app.ui.fragments.MenuFragment;
import com.example.rest_app.ui.fragments.ReservationFragment;
import com.example.rest_app.ui.fragments.ReviewsFragment;
import com.example.rest_app.utils.CartManager;
import com.example.rest_app.utils.FirebaseAuthManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase and Cart Manager
        FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(this);
        CartManager cartManager = CartManager.getInstance();
        cartManager.setAuthManager(authManager);

        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new MenuFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_menu) {
                selectedFragment = new MenuFragment();
            } else if (itemId == R.id.navigation_reservation) {
                selectedFragment = new ReservationFragment();
            } else if (itemId == R.id.navigation_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.navigation_reviews) {
                selectedFragment = new ReviewsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}