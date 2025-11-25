package com.example.rest_app.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rest_app.R;
import com.example.rest_app.model.Review;
import com.example.rest_app.adapter.ReviewsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReviewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private List<Review> reviewList;
    private FloatingActionButton fabAddReview;
    private LinearLayout emptyStateLayout;
    private Button buttonAddFirstReview;
    private Spinner spinnerFilter;
    private TextView textViewReviewsCount;

    // Store the current user who is adding reviews
    private String currentUserName = "";

    private String[] filterOptions = {"All Reviews", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star", "My Reviews"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupFilterSpinner();
        loadSampleReviews();
        updateEmptyState();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewReviews);
        fabAddReview = view.findViewById(R.id.fabAddReview);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        buttonAddFirstReview = view.findViewById(R.id.buttonAddFirstReview);
        spinnerFilter = view.findViewById(R.id.spinnerFilter);
        textViewReviewsCount = view.findViewById(R.id.textViewReviewsCount);

        fabAddReview.setOnClickListener(v -> showAddReviewDialog());
        buttonAddFirstReview.setOnClickListener(v -> showAddReviewDialog());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        reviewList = new ArrayList<>();
        adapter = new ReviewsAdapter(getContext(), reviewList, new ReviewsAdapter.ReviewActionListener() {
            @Override
            public void onEditReview(Review review) {
                // Edit feature - you can implement this later
                Toast.makeText(getContext(), "Edit feature coming soon!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteReview(Review review) {
                // Check if current user can delete this review
                if (currentUserName.equalsIgnoreCase(review.getReviewerName().trim())) {
                    showDeleteConfirmationDialog(review);
                } else {
                    Toast.makeText(getContext(), "You can only delete your own reviews", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public boolean canUserDeleteReview(Review review) {
                // User can only delete their own reviews
                return currentUserName.equalsIgnoreCase(review.getReviewerName().trim());
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                filterOptions
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(filterAdapter);

        spinnerFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilter(filterOptions[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void applyFilter(String filter) {
        List<Review> filteredList = new ArrayList<>();

        switch (filter) {
            case "All Reviews":
                filteredList.addAll(reviewList);
                break;
            case "My Reviews":
                if (!currentUserName.isEmpty()) {
                    for (Review review : reviewList) {
                        if (review.getReviewerName().equalsIgnoreCase(currentUserName.trim())) {
                            filteredList.add(review);
                        }
                    }
                }
                break;
            case "5 Stars":
                filteredList.addAll(getReviewsByRating(5));
                break;
            case "4 Stars":
                filteredList.addAll(getReviewsByRating(4));
                break;
            case "3 Stars":
                filteredList.addAll(getReviewsByRating(3));
                break;
            case "2 Stars":
                filteredList.addAll(getReviewsByRating(2));
                break;
            case "1 Star":
                filteredList.addAll(getReviewsByRating(1));
                break;
        }

        adapter.updateList(filteredList);
        updateReviewsCount(filteredList.size(), filter);
    }

    private List<Review> getReviewsByRating(int rating) {
        List<Review> filtered = new ArrayList<>();
        for (Review review : reviewList) {
            if ((int) review.getRating() == rating) {
                filtered.add(review);
            }
        }
        return filtered;
    }

    private void updateReviewsCount(int count, String filter) {
        String text = filter + " (" + count + ")";
        textViewReviewsCount.setText(text);
    }

    private void loadSampleReviews() {
        reviewList.clear();

        // Sample reviews from different users
        reviewList.add(new Review("John Doe", "Amazing food and great service! The burgers are fantastic and the staff is very friendly.", 5.0f, "2024-01-15", "john_doe"));
        reviewList.add(new Review("Sarah Smith", "Loved the atmosphere. The pasta was delicious and perfectly cooked. Will definitely come back!", 4.5f, "2024-01-14", "sarah_smith"));
        reviewList.add(new Review("Mike Johnson", "Good food but service was a bit slow during peak hours. The pizza was worth the wait though.", 3.5f, "2024-01-13", "mike_johnson"));

        adapter.notifyDataSetChanged();
        updateEmptyState();
        updateReviewsCount(reviewList.size(), "All Reviews");
    }

    private void updateEmptyState() {
        if (reviewList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddReviewDialog() {
        try {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_add_review);
            dialog.setCancelable(true);

            // Make dialog wider
            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }

            EditText editTextName = dialog.findViewById(R.id.editTextName);
            EditText editTextReview = dialog.findViewById(R.id.editTextReview);
            RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
            Button buttonSubmit = dialog.findViewById(R.id.buttonSubmit);
            Button buttonCancel = dialog.findViewById(R.id.buttonCancel);

            // Clear fields for new review
            editTextName.setText("");
            editTextReview.setText("");
            ratingBar.setRating(5); // Default 5 stars
            editTextName.setEnabled(true); // User can enter their name
            buttonSubmit.setText("Submit Review");

            buttonSubmit.setOnClickListener(v -> {
                String name = editTextName.getText().toString().trim();
                String reviewText = editTextReview.getText().toString().trim();
                float rating = ratingBar.getRating();

                if (validateReviewInputs(name, reviewText, rating)) {
                    addNewReview(name, reviewText, rating);
                    currentUserName = name; // Remember this user for delete permissions
                    Toast.makeText(getContext(), "Review added successfully! 🎉", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

            buttonCancel.setOnClickListener(v -> dialog.dismiss());
            dialog.show();

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error opening review dialog", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean validateReviewInputs(String name, String reviewText, float rating) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (reviewText.isEmpty()) {
            Toast.makeText(getContext(), "Please write your review", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (reviewText.length() < 10) {
            Toast.makeText(getContext(), "Review should be at least 10 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rating == 0) {
            Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addNewReview(String name, String reviewText, float rating) {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        String userId = name.toLowerCase().replace(" ", "_");

        Review newReview = new Review(name, reviewText, rating, currentDate, userId);

        // Add to the beginning of the list so it shows at top
        reviewList.add(0, newReview);

        // Update the adapter
        adapter.notifyItemInserted(0);

        // Scroll to top to show the new review
        recyclerView.smoothScrollToPosition(0);

        // Update UI states
        updateEmptyState();
        updateReviewsCount(reviewList.size(), "All Reviews");

        // Show confirmation
        Toast.makeText(getContext(), "Your review has been added! 👍", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog(Review review) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete Review");
        builder.setMessage("Are you sure you want to delete your review? This action cannot be undone.");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteReview(review);
            Toast.makeText(getContext(), "Review deleted successfully", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteReview(Review review) {
        int position = reviewList.indexOf(review);
        if (position != -1) {
            reviewList.remove(position);
            adapter.notifyItemRemoved(position);
            updateEmptyState();
            updateReviewsCount(reviewList.size(), "All Reviews");

            // Show confirmation
            Toast.makeText(getContext(), "Review deleted", Toast.LENGTH_SHORT).show();
        }
    }
}