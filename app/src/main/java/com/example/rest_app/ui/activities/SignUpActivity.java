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

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextFullName;
    private TextInputLayout textInputLayoutEmail, textInputLayoutPassword, textInputLayoutConfirmPassword, textInputLayoutFullName;
    private ImageView backgroundImage;
    private FirebaseAuthManager authManager;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        loadBackgroundImage();
        authManager = FirebaseAuthManager.getInstance(this);
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        backgroundImage = findViewById(R.id.backgroundImage);

        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);
        textInputLayoutFullName = findViewById(R.id.textInputLayoutFullName);

        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        TextView textViewSignIn = findViewById(R.id.textViewSignIn);

        buttonSignUp.setOnClickListener(v -> signUp());
        textViewSignIn.setOnClickListener(v -> navigateToSignIn());
    }

    private void loadBackgroundImage() {
        String backgroundUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80";
        ImageLoader.loadBackground(this, backgroundImage, backgroundUrl);
    }

    private void signUp() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String name = editTextFullName.getText().toString().trim();

        if (validateInputs(email, password, confirmPassword, name)) {
            Button buttonSignUp = findViewById(R.id.buttonSignUp);
            buttonSignUp.setText("Creating Account...");
            buttonSignUp.setEnabled(false);

            authManager.signUpWithEmail(email, password, name, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpActivity.this, "Account created successfully! 🎉", Toast.LENGTH_LONG).show();
                        // Auto sign in after successful registration
                        autoSignIn(email, password);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        buttonSignUp.setText("Create Account");
                        buttonSignUp.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    private void autoSignIn(String email, String password) {
        authManager.signInWithEmail(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "Welcome to Rest_App! 👨‍🍳", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    // Still navigate to main if auto sign in fails
                    navigateToSignIn();
                });
            }
        });
    }

    private boolean validateInputs(String email, String password, String confirmPassword, String name) {
        boolean isValid = true;

        textInputLayoutFullName.setError(null);
        textInputLayoutEmail.setError(null);
        textInputLayoutPassword.setError(null);
        textInputLayoutConfirmPassword.setError(null);

        if (name.isEmpty()) {
            textInputLayoutFullName.setError("Full name is required");
            isValid = false;
        }

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
        } else if (password.length() < 6) {
            textInputLayoutPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            textInputLayoutConfirmPassword.setError("Please confirm password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            textInputLayoutConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}