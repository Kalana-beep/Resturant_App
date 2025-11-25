package com.example.rest_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rest_app.R;
import com.example.rest_app.utils.FirebaseAuthManager;
import com.example.rest_app.utils.ImageLoader;
import com.google.android.material.textfield.TextInputLayout;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private TextInputLayout textInputLayoutEmail, textInputLayoutPassword;
    private ImageView backgroundImage;
    private FirebaseAuthManager authManager;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initializeViews();
        setupClickListeners();
        loadBackgroundImage();

        authManager = FirebaseAuthManager.getInstance(this);

        // Check if user is already logged in
        if (authManager.isUserLoggedIn()) {
            navigateToMainActivity();
        }
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        backgroundImage = findViewById(R.id.backgroundImage);

        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        TextView textViewSignUp = findViewById(R.id.textViewSignUp);
        TextView textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        buttonSignIn.setOnClickListener(v -> signIn());
        textViewSignUp.setOnClickListener(v -> navigateToSignUp());
        textViewForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void loadBackgroundImage() {
        String backgroundUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80";
        ImageLoader.loadBackground(this, backgroundImage, backgroundUrl);
    }

    private void setupClickListeners() {
        editTextEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) textInputLayoutEmail.setError(null);
        });

        editTextPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) textInputLayoutPassword.setError(null);
        });
    }

    private void signIn() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            Button buttonSignIn = findViewById(R.id.buttonSignIn);
            buttonSignIn.setText("Signing In...");
            buttonSignIn.setEnabled(false);

            authManager.signInWithEmail(email, password, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(SignInActivity.this, "Welcome back! 👨‍🍳", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        buttonSignIn.setText("Sign In");
                        buttonSignIn.setEnabled(true);
                        Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        textInputLayoutEmail.setError(null);
        textInputLayoutPassword.setError(null);

        if (email.isEmpty()) {
            textInputLayoutEmail.setError("Email is required");
            isValid = false;
        } else if (!isValidEmail(email)) {
            textInputLayoutEmail.setError("Please enter a valid email");
            isValid = false;
        }

        if (password.isEmpty()) {
            textInputLayoutPassword.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void showForgotPasswordDialog() {
        Toast.makeText(this, "Password reset feature coming soon!", Toast.LENGTH_LONG).show();
    }
}