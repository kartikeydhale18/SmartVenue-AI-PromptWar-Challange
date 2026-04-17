package com.example.smartvenueai.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartvenueai.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CompleteProfileActivity extends AppCompatActivity {

    private TextInputEditText etFullName;
    private android.widget.EditText etPhone;   // plain EditText (etMobile in layout)
    private AutoCompleteTextView actvLanguage;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }
        uid = user.getUid();

        // ── Bind views ──────────────────────────────────────────────
        etFullName    = findViewById(R.id.etFullName);
        etPhone       = findViewById(R.id.etMobile);       // matches activity_complete_profile.xml
        actvLanguage  = findViewById(R.id.actvLanguage);
        progressBar   = findViewById(R.id.progressBarProfile); // may be null if not in XML yet

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Language dropdown
        String[] languages = {"English", "Hindi", "Marathi", "Tamil", "Telugu",
                "Kannada", "Bengali", "Gujarati", "Punjabi", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, languages);
        actvLanguage.setAdapter(adapter);

        // Avatar picker (photo library intent)
        findViewById(R.id.avatarPicker).setOnClickListener(v ->
                Toast.makeText(this, "Photo picker coming soon!", Toast.LENGTH_SHORT).show());

        // Pre-fill from Firestore
        loadExistingProfile(user);

        // Save Details
        findViewById(R.id.btnSaveDetails).setOnClickListener(v -> saveProfile());
    }

    /** Loads existing data from Firestore and pre-fills the form */
    private void loadExistingProfile(FirebaseUser user) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Pre-fill phone from Firebase Auth
        if (!TextUtils.isEmpty(user.getPhoneNumber())) {
            etPhone.setText(user.getPhoneNumber());
            etPhone.setEnabled(false); // phone is verified, not editable
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (doc.exists()) {
                        String name = doc.getString("fullName");
                        String lang = doc.getString("language");
                        if (!TextUtils.isEmpty(name)) etFullName.setText(name);
                        if (!TextUtils.isEmpty(lang))  actvLanguage.setText(lang, false);
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    /** Validates and saves profile data to Firestore */
    private void saveProfile() {
        String name = etFullName.getText() != null
                ? etFullName.getText().toString().trim() : "";
        String lang = actvLanguage.getText() != null
                ? actvLanguage.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSaveDetails).setEnabled(false);

        // Build Firestore document
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("fullName", name);
        profileData.put("language", lang);
        profileData.put("uid", uid);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !TextUtils.isEmpty(user.getPhoneNumber())) {
            profileData.put("phone", user.getPhoneNumber());
        }

        db.collection("users").document(uid)
                .set(profileData)
                .addOnSuccessListener(unused -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "✅ Profile saved!", Toast.LENGTH_SHORT).show();
                    finish(); // returns to ProfileFragment which refreshes on resume
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    findViewById(R.id.btnSaveDetails).setEnabled(true);
                    Toast.makeText(this, "Failed to save: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
