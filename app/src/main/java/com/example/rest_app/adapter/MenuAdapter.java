package com.example.rest_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.rest_app.R;
import com.example.rest_app.model.FoodItem;
import com.example.rest_app.utils.CartManager;
import com.example.rest_app.utils.CurrencyManager; // NEW IMPORT
import java.util.List;
import java.util.Random;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private final Context context;
    private List<FoodItem> foodItemList;
    private final Random random;
    private final UpdateCartListener updateCartListener;
    private final CurrencyManager currencyManager; // NEW FIELD

    public interface UpdateCartListener {
        void onUpdateCart();
    }

    public MenuAdapter(Context context, List<FoodItem> foodItemList, UpdateCartListener updateCartListener) {
        this.context = context;
        this.foodItemList = foodItemList;
        this.updateCartListener = updateCartListener;
        this.random = new Random();
        this.currencyManager = CurrencyManager.getInstance(); // INITIALIZE
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = foodItemList.get(position);

        holder.textViewName.setText(foodItem.getName());
        holder.textViewDescription.setText(foodItem.getDescription());

        // NEW: Use CurrencyManager to display converted price
        holder.textViewPrice.setText(currencyManager.formatPrice(foodItem.getPrice()));

        // Set random rating between 4.2 and 5.0
        float rating = 4.2f + random.nextFloat() * 0.8f;
        holder.ratingBar.setRating(rating);
        holder.textViewRating.setText(String.format("%.1f", rating));

        // Set calories if available
        if (foodItem.getCalories() > 0) {
            holder.textViewCalories.setText("🔥 " + foodItem.getCalories() + " cal");
        }

        // Randomly show popular badge for some items
        holder.textViewPopular.setVisibility(random.nextBoolean() ? View.VISIBLE : View.GONE);

        // Set preparation time (15-25 minutes)
        int time = 15 + random.nextInt(11);
        holder.textViewTime.setText(time + " min");

        // Load image from URL with better error handling
        loadFoodImage(holder.imageViewFood, foodItem.getImageUrl());

        // Add to Cart button
        holder.buttonAddToCart.setOnClickListener(v -> {
            CartManager.getInstance().addToCart(foodItem);
            animateAddToCartButton(holder.buttonAddToCart);
            Toast.makeText(context, "✓ " + foodItem.getName() + " added to cart!", Toast.LENGTH_SHORT).show();

            if (updateCartListener != null) {
                updateCartListener.onUpdateCart();
            }
        });

        // Card click animation
        holder.cardView.setOnClickListener(v -> {
            animateCardClick(holder.cardView);
        });
    }

    private void loadFoodImage(ImageView imageView, String imageUrl) {
        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .centerCrop()
                    .into(imageView);
        } catch (Exception e) {
            // Fallback to placeholder on any error
            Glide.with(context)
                    .load(R.drawable.placeholder_food)
                    .into(imageView);
        }
    }

    private void animateAddToCartButton(Button button) {
        button.setText("ADDED ✓");
        button.setBackgroundColor(context.getResources().getColor(R.color.success_green));

        button.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() -> button.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200));

        button.postDelayed(() -> {
            button.setText("ADD TO CART");
            button.setBackground(context.getResources().getDrawable(R.drawable.button_add_to_cart));
        }, 2000);
    }

    private void animateCardClick(CardView cardView) {
        cardView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> cardView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100));
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }

    public void updateList(List<FoodItem> newList) {
        this.foodItemList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageViewFood;
        TextView textViewName;
        TextView textViewDescription;
        TextView textViewPrice;
        TextView textViewPopular;
        TextView textViewRating;
        TextView textViewTime;
        TextView textViewCalories;
        android.widget.RatingBar ratingBar;
        Button buttonAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            imageViewFood = itemView.findViewById(R.id.imageViewFood);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewPopular = itemView.findViewById(R.id.textViewPopular);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewCalories = itemView.findViewById(R.id.textViewCalories);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
        }
    }
}