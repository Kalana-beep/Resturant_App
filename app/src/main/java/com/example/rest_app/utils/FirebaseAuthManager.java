package com.example.rest_app.utils;

import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.rest_app.model.User;

public class FirebaseAuthManager {
    private static final String TAG = "FirebaseAuthManager";
    private static FirebaseAuthManager instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;

    private FirebaseAuthManager(Context context) {
        this.context = context.getApplicationContext();
        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firebase Auth and Firestore initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage());
        }
    }

    public static synchronized FirebaseAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAuthManager(context);
        }
        return instance;
    }

    public void signUpWithEmail(String email, String password, String name, AuthCallback callback) {
        Log.d(TAG, "Attempting to sign up: " + email);

        if (mAuth == null) {
            callback.onError("Firebase not initialized properly");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign up successful for: " + email);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update user profile with name
                            com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                                    new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        Log.d(TAG, "User profile updated");
                                    });

                            // Create user document in Firestore
                            createUserDocument(user.getUid(), email, name, callback);
                        } else {
                            callback.onError("User creation failed");
                        }
                    } else {
                        String errorMessage = "Sign up failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, "Sign up error: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                });
    }

    public void signInWithEmail(String email, String password, AuthCallback callback) {
        Log.d(TAG, "Attempting to sign in: " + email);

        if (mAuth == null) {
            callback.onError("Firebase not initialized properly");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign in successful for: " + email);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            callback.onSuccess();
                        } else {
                            callback.onError("User not found");
                        }
                    } else {
                        String errorMessage = "Sign in failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, "Sign in error: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                });
    }

    private void createUserDocument(String userId, String email, String name, AuthCallback callback) {
        User user = new User(userId, email, System.currentTimeMillis(), name);

        // Create user document
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created successfully");

                    // Initialize user's cart
                    java.util.HashMap<String, Object> cartData = new java.util.HashMap<>();
                    cartData.put("items", new java.util.ArrayList<>());
                    cartData.put("userId", userId);
                    cartData.put("lastUpdated", System.currentTimeMillis());

                    db.collection("carts").document(userId)
                            .set(cartData)
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "User cart initialized");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating user cart: " + e.getMessage());
                                callback.onSuccess(); // Still consider success
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user document: " + e.getMessage());
                    callback.onSuccess(); // Still consider auth successful
                });
    }

    public FirebaseUser getCurrentUser() {
        return mAuth != null ? mAuth.getCurrentUser() : null;
    }

    public void signOut() {
        if (mAuth != null) {
            mAuth.signOut();
            Log.d(TAG, "User signed out");
        }
    }

    public boolean isUserLoggedIn() {
        return mAuth != null && mAuth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public String getCurrentUserName() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getDisplayName() : null;
    }

    public FirebaseFirestore getFirestore() {
        return db;
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}