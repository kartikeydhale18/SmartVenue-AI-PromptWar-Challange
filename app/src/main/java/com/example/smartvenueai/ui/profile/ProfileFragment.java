package com.example.smartvenueai.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import com.example.smartvenueai.MainActivity;
import com.example.smartvenueai.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hamburger menu opens the side drawer
        view.findViewById(R.id.btnOpenDrawerProfile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        // Load real user data from Firestore
        loadUserProfile(view);

        // ── Row click handlers ──────────────────────────────────
        view.findViewById(R.id.rowPaymentMethods).setOnClickListener(v ->
                Toast.makeText(getContext(), "Payment Methods coming soon!", Toast.LENGTH_SHORT).show());

        // Link Tickets → Complete Profile (to add/edit info)
        view.findViewById(R.id.rowLinkTickets).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CompleteProfileActivity.class)));

        view.findViewById(R.id.rowAccessibility).setOnClickListener(v ->
                Toast.makeText(getContext(), "Accessibility Options coming soon!", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowNotifications).setOnClickListener(v ->
                Toast.makeText(getContext(), "Notification Settings coming soon!", Toast.LENGTH_SHORT).show());
                
        // Dark Mode Toggle
        SwitchMaterial switchDarkMode = view.findViewById(R.id.switchDarkMode);
        if (switchDarkMode != null) {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            switchDarkMode.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);
            
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });
        }
    }

    /** Pulls the user's profile from Firestore and populates the UI */
    private void loadUserProfile(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Show phone number while Firestore loads
        TextView tvName    = view.findViewById(R.id.tvProfileName);
        TextView tvPhone   = view.findViewById(R.id.tvProfilePhone);
        TextView tvInitials = view.findViewById(R.id.tvProfileInitials);

        // Pre-fill with phone from Firebase Auth
        if (!TextUtils.isEmpty(user.getPhoneNumber())) {
            tvPhone.setText(user.getPhoneNumber());
        }

        // Fetch full profile from Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && getContext() != null) {
                        String name  = doc.getString("fullName");
                        String phone = doc.getString("phone");

                        if (!TextUtils.isEmpty(name)) {
                            tvName.setText(name);
                            // Update initials badge
                            tvInitials.setText(getInitials(name));
                        }
                        if (!TextUtils.isEmpty(phone)) {
                            tvPhone.setText(phone);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Could not load profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    /** Returns up to 2 capital letters from the name, e.g. "Kartikey Dhale" → "KD" */
    private String getInitials(String name) {
        if (TextUtils.isEmpty(name)) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return String.valueOf(parts[0].charAt(0)).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[parts.length - 1].charAt(0))).toUpperCase();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data every time we come back (e.g. after editing in CompleteProfileActivity)
        if (getView() != null) loadUserProfile(getView());
    }
}
