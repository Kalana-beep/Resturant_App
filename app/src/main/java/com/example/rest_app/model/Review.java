package com.example.rest_app.model;

public class Review {
    private String reviewerName;
    private String reviewText;
    private float rating;
    private String date;
    private String userId; // Add this field

    public Review(String reviewerName, String reviewText, float rating, String date) {
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
        this.userId = reviewerName.toLowerCase().replace(" ", "_");
    }

    public Review(String reviewerName, String reviewText, float rating, String date, String userId) {
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.date = date;
        this.userId = userId;
    }

    // Getters and Setters
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}