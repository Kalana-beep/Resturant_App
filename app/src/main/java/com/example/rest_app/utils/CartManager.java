package com.example.rest_app.utils;

import android.content.Context;
import android.util.Log;
import com.example.rest_app.model.FoodItem;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private List<FoodItem> cartItems;
    private FirebaseAuthManager authManager;
    private CartUpdateListener cartUpdateListener;
    private FirebaseFirestore firestore;

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    private CartManager() {
        cartItems = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void setAuthManager(FirebaseAuthManager authManager) {
        this.authManager = authManager;
    }

    public void setCartUpdateListener(CartUpdateListener listener) {
        this.cartUpdateListener = listener;
    }

    // Add item to cart
    public void addToCart(FoodItem foodItem) {
        // Check if item already exists in cart
        for (FoodItem item : cartItems) {
            if (item.getName().equals(foodItem.getName())) {
                item.setQuantity(item.getQuantity() + 1);
                notifyCartUpdated();
                saveCartToFirestore();
                return;
            }
        }

        // Add new item
        FoodItem newItem = new FoodItem(
                foodItem.getName(),
                foodItem.getDescription(),
                foodItem.getPrice(),
                foodItem.getImageUrl(),
                foodItem.getCategory(),
                foodItem.getCalories()
        );
        newItem.setQuantity(1);
        cartItems.add(newItem);
        notifyCartUpdated();
        saveCartToFirestore();
    }

    // Remove item from cart
    public void removeFromCart(FoodItem foodItem) {
        cartItems.removeIf(item -> item.getName().equals(foodItem.getName()));
        notifyCartUpdated();
        saveCartToFirestore();
    }

    // Update item quantity
    public void updateQuantity(FoodItem foodItem, int quantity) {
        for (FoodItem item : cartItems) {
            if (item.getName().equals(foodItem.getName())) {
                if (quantity <= 0) {
                    removeFromCart(item);
                } else {
                    item.setQuantity(quantity);
                }
                break;
            }
        }
        notifyCartUpdated();
        saveCartToFirestore();
    }

    // Clear cart
    public void clearCart() {
        cartItems.clear();
        notifyCartUpdated();
        saveCartToFirestore();
    }

    // Get cart items
    public List<FoodItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    // Get total price
    public double getTotalPrice() {
        double total = 0;
        for (FoodItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    // Get item count
    public int getCartItemCount() {
        int count = 0;
        for (FoodItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    // Save cart to Firestore
    public void saveCartToFirestore() {
        if (authManager == null || !authManager.isUserLoggedIn()) {
            return;
        }

        String userId = authManager.getCurrentUserId();
        if (userId == null) return;

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("userId", userId);
        cartData.put("items", convertCartItemsToMap());
        cartData.put("lastUpdated", System.currentTimeMillis());
        cartData.put("userEmail", authManager.getCurrentUserEmail());
        cartData.put("userName", authManager.getCurrentUserName());
        cartData.put("totalItems", getCartItemCount());
        cartData.put("totalPrice", getTotalPrice());

        firestore.collection("userCarts")
                .document(userId)
                .set(cartData)
                .addOnSuccessListener(aVoid -> Log.d("CartManager", "Cart saved to Firestore"))
                .addOnFailureListener(e -> Log.e("CartManager", "Error saving cart: " + e.getMessage()));
    }

    // Load cart from Firestore
    public void loadCartFromFirestore() {
        if (authManager == null || !authManager.isUserLoggedIn()) {
            return;
        }

        String userId = authManager.getCurrentUserId();
        if (userId == null) return;

        firestore.collection("userCarts")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // You can implement cart restoration here if needed
                        Log.d("CartManager", "Cart loaded from Firestore");
                        notifyCartUpdated();
                    }
                })
                .addOnFailureListener(e -> Log.e("CartManager", "Error loading cart: " + e.getMessage()));
    }

    private List<Map<String, Object>> convertCartItemsToMap() {
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (FoodItem item : cartItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("description", item.getDescription());
            itemMap.put("price", item.getPrice());
            itemMap.put("imageUrl", item.getImageUrl());
            itemMap.put("category", item.getCategory());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("calories", item.getCalories());
            itemsList.add(itemMap);
        }
        return itemsList;
    }

    private void notifyCartUpdated() {
        if (cartUpdateListener != null) {
            cartUpdateListener.onCartUpdated();
        }
    }
}