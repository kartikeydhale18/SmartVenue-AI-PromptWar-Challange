package com.example.smartvenueai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartvenueai.MainActivity;
import com.example.smartvenueai.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String mVerificationId;

    private LinearLayout phoneLayout, otpLayout;
    private TextInputEditText etPhoneNumber, etOtpCode;
    private MaterialButton btnSendOtp, btnVerifyOtp;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Hide action bar for full-screen experience
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Already logged in → skip to main
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        phoneLayout   = findViewById(R.id.phoneLayout);
        otpLayout     = findViewById(R.id.otpLayout);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtpCode     = findViewById(R.id.etOtpCode);
        btnSendOtp    = findViewById(R.id.btnSendOtp);
        btnVerifyOtp  = findViewById(R.id.btnVerifyOtp);
        progressBar   = findViewById(R.id.progressBar);

        // --- HACKATHON JUDGE PREFILL ---
        // Pre-filling test credentials so judges don't have to look for them!
        etPhoneNumber.setText("9999999999");
        etOtpCode.setText("123456");

        btnSendOtp.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Send OTP
    // ─────────────────────────────────────────────────────────────────────────
    private void sendOtp() {
        String rawNumber = etPhoneNumber.getText() != null
                ? etPhoneNumber.getText().toString().trim() : "";

        // ── Client-side validation ────────────────────────────────────────
        if (TextUtils.isEmpty(rawNumber)) {
            etPhoneNumber.setError("Please enter your mobile number");
            etPhoneNumber.requestFocus();
            return;
        }
        if (!rawNumber.matches("[0-9]+")) {
            etPhoneNumber.setError("Use digits only — no spaces or dashes");
            etPhoneNumber.requestFocus();
            return;
        }
        if (rawNumber.length() != 10) {
            etPhoneNumber.setError("Enter a valid 10-digit Indian mobile number");
            etPhoneNumber.requestFocus();
            return;
        }
        if (!rawNumber.matches("[6-9][0-9]{9}")) {
            etPhoneNumber.setError("Indian mobile numbers start with 6, 7, 8, or 9");
            etPhoneNumber.requestFocus();
            return;
        }

        String phoneNumber = "+91" + rawNumber;

        progressBar.setVisibility(View.VISIBLE);
        btnSendOtp.setEnabled(false);
        etPhoneNumber.setError(null);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Auto-retrieval or instant verification (test numbers)
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        btnSendOtp.setEnabled(true);
                        showSendError(e);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                          @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressBar.setVisibility(View.GONE);
                        mVerificationId = verificationId;
                        // Switch to OTP entry panel
                        phoneLayout.setVisibility(View.GONE);
                        otpLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this,
                                "OTP sent to +91" + rawNumber, Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Maps Firebase exceptions to friendly user-facing messages.
     * Firebase never returns a plain "invalid number" message —
     * it throws FirebaseAuthInvalidCredentialsException instead.
     */
    private void showSendError(FirebaseException e) {
        String message;

        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            // This is the "INVALID_PHONE_NUMBER" / format error
            message = "Invalid phone number. Please check and try again.";
            etPhoneNumber.setError(message);
            etPhoneNumber.requestFocus();

        } else if (e instanceof FirebaseTooManyRequestsException) {
            message = "Too many attempts. Please wait a few minutes before trying again.";

        } else if (e instanceof FirebaseNetworkException) {
            message = "No internet connection. Please check your network and try again.";

        } else {
            // Catch-all: log the real cause for debugging but show a clean message
            android.util.Log.e("SmartVenue/Login", "Verification failed", e);
            message = "Could not send OTP. Please check your number and try again.";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verify OTP
    // ─────────────────────────────────────────────────────────────────────────
    private void verifyOtp() {
        String code = etOtpCode.getText() != null
                ? etOtpCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(code)) {
            etOtpCode.setError("Please enter the OTP");
            etOtpCode.requestFocus();
            return;
        }
        if (!code.matches("[0-9]+") || code.length() != 6) {
            etOtpCode.setError("OTP must be 6 digits");
            etOtpCode.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mVerificationId)) {
            Toast.makeText(this, "Session expired. Please request a new OTP.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnVerifyOtp.setEnabled(false);
        etOtpCode.setError(null);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sign In
    // ─────────────────────────────────────────────────────────────────────────
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        btnVerifyOtp.setEnabled(true);
                        showVerifyError(task.getException());
                    }
                });
    }

    private void showVerifyError(Exception e) {
        String message;

        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            // Wrong OTP entered
            message = "Incorrect OTP. Please check and try again.";
            etOtpCode.setError(message);
            etOtpCode.requestFocus();

        } else if (e instanceof FirebaseNetworkException) {
            message = "No internet connection. Please try again.";

        } else {
            android.util.Log.e("SmartVenue/Login", "Sign-in failed", e);
            message = "Verification failed. Please request a new OTP.";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
