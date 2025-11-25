package com.example.rest_app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rest_app.R;
import com.example.rest_app.model.FoodItem;
import com.example.rest_app.adapter.CartAdapter;
import com.example.rest_app.utils.CartManager;
import com.example.rest_app.utils.CurrencyManager; // NEW IMPORT
import com.example.rest_app.utils.FirebaseAuthManager;
import com.example.rest_app.utils.ImageLoader;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Implement CurrencyManager.CurrencyUpdateListener
public class CartFragment extends Fragment implements CartManager.CartUpdateListener, CurrencyManager.CurrencyUpdateListener {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<FoodItem> cartItems;
    private TextView textViewTotal, textViewSubtotal, textViewTax, textViewItemCount;
    private Button buttonPlaceOrder, buttonBrowseMenu, buttonReturnMenu;
    private LinearLayout emptyCartLayout;
    private ImageView backgroundImage;
    private FirebaseAuthManager authManager;
    private CartManager cartManager;
    private CurrencyManager currencyManager; // NEW FIELD

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        initializeViews(view);
        setupFirebase();
        setupRecyclerView();
        setupCurrencyManager(); // NEW: Initialize Currency Manager
        loadCartItems();
        updateCartSummary();
        loadBackgroundImage();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        textViewTotal = view.findViewById(R.id.textViewTotal);
        textViewSubtotal = view.findViewById(R.id.textViewSubtotal);
        textViewTax = view.findViewById(R.id.textViewTax);
        textViewItemCount = view.findViewById(R.id.textViewItemCount);
        buttonPlaceOrder = view.findViewById(R.id.buttonPlaceOrder);
        buttonBrowseMenu = view.findViewById(R.id.buttonBrowseMenu);
        buttonReturnMenu = view.findViewById(R.id.buttonReturnMenu);
        emptyCartLayout = view.findViewById(R.id.emptyCartLayout);
        backgroundImage = view.findViewById(R.id.backgroundImage);

        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
        buttonBrowseMenu.setOnClickListener(v -> browseMenu());
        buttonReturnMenu.setOnClickListener(v -> returnToMenu());
    }

    private void setupFirebase() {
        authManager = FirebaseAuthManager.getInstance(requireContext());
        cartManager = CartManager.getInstance();
        cartManager.setAuthManager(authManager);
        cartManager.setCartUpdateListener(this);
    }

    private void setupCurrencyManager() { // NEW METHOD
        currencyManager = CurrencyManager.getInstance();
        currencyManager.setCurrencyUpdateListener(this);
        // We only fetch here to ensure rates are loaded if user opens Cart first
        currencyManager.fetchExchangeRates();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        cartItems = cartManager.getCartItems();
        // Adapter calls updateCartSummary when quantity changes
        adapter = new CartAdapter(getContext(), cartItems, this::updateCartSummary);
        recyclerView.setAdapter(adapter);
    }

    private void loadCartItems() {
        cartManager.loadCartFromFirestore();
    }

    private void updateCartVisibility() {
        if (cartItems.isEmpty()) {
            emptyCartLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyCartLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadBackgroundImage() {
        String backgroundUrl = "https://images.unsplash.com/photo-1572802419224-296b0aeee0d9?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2015&q=80";
        ImageLoader.loadBackground(getContext(), backgroundImage, backgroundUrl);
    }

    public void updateCartSummary() {
        CurrencyManager cm = CurrencyManager.getInstance();
        double subtotalInBase = cartManager.getTotalPrice();
        double taxInBase = subtotalInBase * 0.08;
        double totalInBase = subtotalInBase + taxInBase;
        int itemCount = cartManager.getCartItemCount();

        // NEW: Use CurrencyManager for formatting and conversion
        textViewSubtotal.setText(cm.formatPrice(subtotalInBase));
        textViewTax.setText(cm.formatPrice(taxInBase));
        textViewTotal.setText(cm.formatPrice(totalInBase));
        textViewItemCount.setText(String.format("%d %s", itemCount, itemCount == 1 ? "item" : "items"));

        updateCartVisibility();
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Please log in to place an order", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user details
        String userEmail = authManager.getCurrentUserEmail();
        String userName = authManager.getCurrentUserName();

        // Create order data with user information
        String orderId = "ORD_" + System.currentTimeMillis();
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", userId);
        orderData.put("userEmail", userEmail != null ? userEmail : "");
        orderData.put("userName", userName != null ? userName : "");
        orderData.put("items", convertCartItemsToMap());
        // All values saved to Firestore MUST be in BASE CURRENCY (USD) for consistency
        orderData.put("subtotal", cartManager.getTotalPrice());
        orderData.put("tax", cartManager.getTotalPrice() * 0.08);
        orderData.put("total", cartManager.getTotalPrice() * 1.08);
        orderData.put("currencyUsed", CurrencyManager.getInstance().getCurrentCurrencyCode()); // Record the display currency
        orderData.put("status", "confirmed");
        orderData.put("timestamp", System.currentTimeMillis());
        orderData.put("orderDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        // Use set() instead of add() to ensure order is saved
        authManager.getFirestore().collection("orders")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Order placed successfully! 🎉\nOrder ID: " + orderId, Toast.LENGTH_LONG).show();
                    cartManager.clearCart();
                    updateCartSummary();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to place order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private List<Map<String, Object>> convertCartItemsToMap() {
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (FoodItem item : cartItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("imageUrl", item.getImageUrl());
            itemMap.put("description", item.getDescription());
            itemMap.put("category", item.getCategory());
            itemMap.put("calories", item.getCalories());
            itemsList.add(itemMap);
        }
        return itemsList;
    }

    private void browseMenu() {
        navigateToMenu();
    }

    private void returnToMenu() {
        navigateToMenu();
    }

    private void navigateToMenu() {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_menu);
            }
        }
    }

    @Override
    public void onCartUpdated() {
        // Called by CartManager when an item is added/removed/updated
        updateCartSummary();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRatesLoaded() { // NEW METHOD
        // Called by CurrencyManager when currency changes or rates load
        updateCartSummary();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartItems();
        updateCartSummary();
    }
}