package com.example.smartvenueai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.smartvenueai.ui.alerts.AlertsFragment;
import com.example.smartvenueai.ui.map.MapFragment;
import com.example.smartvenueai.ui.navigation.NavigationFragment;
import com.example.smartvenueai.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, com.example.smartvenueai.ui.auth.LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        bottomNav    = findViewById(R.id.bottomNav);

        // Wire up all drawer click handlers
        setupDrawer();

        // Populate drawer header with real user data
        loadDrawerHeader();

        // Handle back press: close drawer first if open (AndroidX compatible)
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout != null
                                && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);          // let system handle it
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });

        // Load default fragment (Map tab)
        if (savedInstanceState == null) {
            loadFragment(new MapFragment());
            bottomNav.setSelectedItemId(R.id.nav_map);
        }

        // Bottom nav tab switching
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                loadFragment(new MapFragment());
                return true;
            } else if (id == R.id.nav_navigation) {
                loadFragment(new NavigationFragment());
                return true;
            } else if (id == R.id.nav_alerts) {
                loadFragment(new AlertsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    /** Called by Fragments to open the side drawer */
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void setupDrawer() {
        // ── Account ──────────────────────────────────────────────────
        findViewById(R.id.drawerMyProfile).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            startActivity(new Intent(this,
                    com.example.smartvenueai.ui.profile.CompleteProfileActivity.class));
        });



        // ── Preferences ──────────────────────────────────────────────
        SwitchMaterial switchNotif = findViewById(R.id.switchNotifications);
        if (switchNotif != null) {
            switchNotif.setOnCheckedChangeListener((btn, on) ->
                    Toast.makeText(this,
                            on ? "Notifications ON" : "Notifications OFF",
                            Toast.LENGTH_SHORT).show());
        }

        findViewById(R.id.drawerSettings).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            loadFragment(new ProfileFragment());
            bottomNav.setSelectedItemId(R.id.nav_profile);
        });

        findViewById(R.id.drawerHelp).setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("mailto:dhalekartikey@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "SmartVenue AI Support Request");
            startActivity(Intent.createChooser(intent, "Send Support Email"));
        });

        // Logout
        findViewById(R.id.drawerLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            drawerLayout.closeDrawers();
            startActivity(new Intent(this,
                    com.example.smartvenueai.ui.auth.LoginActivity.class));
            finish();
        });
    }

    /** Loads user name and phone from Firebase and updates the drawer header */
    private void loadDrawerHeader() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        android.widget.TextView tvName     = findViewById(R.id.navUserName);
        android.widget.TextView tvPhone    = findViewById(R.id.navUserEmail);
        android.widget.TextView tvInitials = findViewById(R.id.navAvatarInitials);

        // Show phone immediately from FirebaseAuth
        if (tvPhone != null && user.getPhoneNumber() != null) {
            tvPhone.setText(user.getPhoneNumber());
        }

        // Fetch full name from Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("fullName");
                        if (name != null && !name.isEmpty()) {
                            if (tvName != null) tvName.setText("Welcome, "
                                    + name.split(" ")[0] + "!");
                            if (tvInitials != null) tvInitials.setText(getInitials(name));
                        }
                    }
                });
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0))
                + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }

}