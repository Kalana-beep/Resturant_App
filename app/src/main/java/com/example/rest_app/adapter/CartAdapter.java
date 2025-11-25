package com.example.rest_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.rest_app.R;
import com.example.rest_app.model.FoodItem;
import com.example.rest_app.utils.CartManager;
import com.example.rest_app.utils.CurrencyManager; // NEW IMPORT
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<FoodItem> cartItems;
    private final UpdateTotalListener updateTotalListener;
    private final CurrencyManager currencyManager; // NEW FIELD

    public interface UpdateTotalListener {
        void onUpdateTotal();
    }

    public CartAdapter(Context context, List<FoodItem> cartItems, UpdateTotalListener updateTotalListener) {
        this.context = context;
        this.cartItems = cartItems;
        this.updateTotalListener = updateTotalListener;
        this.currencyManager = CurrencyManager.getInstance(); // INITIALIZE
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = cartItems.get(position);

        holder.textViewName.setText(foodItem.getName());
        // NEW: Use CurrencyManager to display converted price
        holder.textViewPrice.setText(currencyManager.formatPrice(foodItem.getPrice()));
        holder.textViewQuantity.setText(String.valueOf(foodItem.getQuantity()));

        // Load image
        try {
            Glide.with(context)
                    .load(foodItem.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .into(holder.imageViewFood);
        } catch (Exception e) {
            // Fallback to placeholder
            Glide.with(context)
                    .load(R.drawable.placeholder_food)
                    .into(holder.imageViewFood);
        }

        // Increase quantity
        holder.buttonIncrease.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(foodItem, foodItem.getQuantity() + 1);
            holder.textViewQuantity.setText(String.valueOf(foodItem.getQuantity()));
            updateTotalListener.onUpdateTotal();
        });

        // Decrease quantity
        holder.buttonDecrease.setOnClickListener(v -> {
            if (foodItem.getQuantity() > 1) {
                CartManager.getInstance().updateQuantity(foodItem, foodItem.getQuantity() - 1);
                holder.textViewQuantity.setText(String.valueOf(foodItem.getQuantity()));
                updateTotalListener.onUpdateTotal();
            } else {
                // If quantity is 1 and user clicks decrease, remove item
                removeItem(position);
            }
        });

        // DELETE BUTTON CLICK LISTENER
        holder.buttonDelete.setOnClickListener(v -> {
            removeItem(position);
        });
    }

    // Method to remove item from cart
    private void removeItem(int position) {
        FoodItem foodItem = cartItems.get(position);
        CartManager.getInstance().removeFromCart(foodItem);
        cartItems.remove(position);
        notifyItemRemoved(position);
        updateTotalListener.onUpdateTotal();

        // Show confirmation message
        if (context != null) {
            android.widget.Toast.makeText(context, foodItem.getName() + " removed from cart", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFood;
        TextView textViewName;
        TextView textViewPrice;
        TextView textViewQuantity;
        TextView buttonIncrease;
        TextView buttonDecrease;
        Button buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewFood = itemView.findViewById(R.id.imageViewFood);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}