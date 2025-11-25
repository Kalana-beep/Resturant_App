package com.example.rest_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rest_app.R;
import com.example.rest_app.model.Review;

import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private ReviewActionListener actionListener;

    public interface ReviewActionListener {
        void onEditReview(Review review);
        void onDeleteReview(Review review);
        boolean canUserDeleteReview(Review review);
    }

    public ReviewsAdapter(Context context, List<Review> reviewList, ReviewActionListener actionListener) {
        this.context = context;
        this.reviewList = reviewList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        // Set review data
        holder.textViewName.setText(review.getReviewerName());
        holder.textViewReview.setText(review.getReviewText());
        holder.textViewDate.setText(review.getDate());
        holder.ratingBar.setRating(review.getRating());

        // Check if current user can delete this review
        boolean canDelete = actionListener != null && actionListener.canUserDeleteReview(review);

        // Show/hide delete button based on permission
        holder.buttonDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        holder.buttonEdit.setVisibility(View.GONE); // Hide edit for now

        // Set up delete button
        holder.buttonDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteReview(review);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateList(List<Review> newList) {
        this.reviewList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewReview;
        TextView textViewDate;
        RatingBar ratingBar;
        LinearLayout layoutActions;
        Button buttonEdit;
        Button buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewReview = itemView.findViewById(R.id.textViewReview);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}