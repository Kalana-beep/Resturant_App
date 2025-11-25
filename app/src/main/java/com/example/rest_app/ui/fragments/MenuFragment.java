package com.example.rest_app.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rest_app.R;
import com.example.rest_app.model.FoodItem;
import com.example.rest_app.adapter.MenuAdapter;
import com.example.rest_app.ui.activities.SignInActivity;
import com.example.rest_app.utils.CartManager;
import com.example.rest_app.utils.CurrencyManager;
import com.example.rest_app.utils.FirebaseAuthManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

// Implement CurrencyManager.CurrencyUpdateListener
public class MenuFragment extends Fragment implements CurrencyManager.CurrencyUpdateListener {

    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<FoodItem> foodItemList;
    private List<FoodItem> allFoodItems;
    private androidx.appcompat.widget.SearchView searchView;
    private Button buttonLogout;
    private TextView textViewCartCount, textViewCartTotal;

    // NEW: Currency and Quote Views
    private TextView textViewQuote, textViewAuthor;
    private Spinner spinnerCurrency;
    private CurrencyManager currencyManager;
    private String[] currencies = {"USD", "EUR", "GBP", "INR", "JPY", "CAD"}; // Supported currencies

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        initializeViews(view);
        setupCurrencyManager(); // NEW: Initialize Currency Manager
        setupRecyclerView();
        loadFoodItems();
        setupSearchView();
        setupLogoutButton();
        setupCurrencySpinner(); // NEW: Setup the currency selector
        updateCartSummary();

        // NEW: Fetch the quote when the fragment is created
        fetchZenQuote();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewMenu);
        searchView = view.findViewById(R.id.searchView);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        textViewCartCount = view.findViewById(R.id.textViewCartCount);
        textViewCartTotal = view.findViewById(R.id.textViewCartTotal);

        // NEW: Initialize currency and quote views
        spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        textViewQuote = view.findViewById(R.id.textViewQuote);
        textViewAuthor = view.findViewById(R.id.textViewAuthor);
    }

    private void setupCurrencyManager() {
        currencyManager = CurrencyManager.getInstance();
        currencyManager.setCurrencyUpdateListener(this);
        currencyManager.fetchExchangeRates();
    }

    private void setupCurrencySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        // Set default selection based on manager's state
        int defaultIndex = Arrays.asList(currencies).indexOf(currencyManager.getCurrentCurrencyCode());
        spinnerCurrency.setSelection(defaultIndex != -1 ? defaultIndex : 0, false);

        spinnerCurrency.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String newCurrency = currencies[position];
                if (!newCurrency.equals(currencyManager.getCurrentCurrencyCode())) {
                    // Update manager, which triggers onRatesLoaded() to refresh UI
                    currencyManager.setCurrentCurrencyCode(newCurrency);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        foodItemList = new ArrayList<>();
        allFoodItems = new ArrayList<>();
        // The adapter now uses the lambda expression to call updateCartSummary when an item is added
        adapter = new MenuAdapter(getContext(), foodItemList, this::updateCartSummary);
        recyclerView.setAdapter(adapter);
    }

    private void setupLogoutButton() {
        buttonLogout.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
            builder.setTitle("Logout");
            builder.setMessage("Are you sure you want to logout?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Use Firebase Auth Manager for logout
                FirebaseAuthManager authManager = FirebaseAuthManager.getInstance(getContext());
                authManager.signOut();

                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    private void setupSearchView() {
        searchView.setQueryHint("Search burgers, pizza, drinks...");

        // Customize search view
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.text_primary));
        searchEditText.setHintTextColor(getResources().getColor(R.color.text_secondary));

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFoodItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFoodItems(newText);
                return true;
            }
        });
    }

    private void filterFoodItems(String query) {
        if (TextUtils.isEmpty(query)) {
            adapter.updateList(allFoodItems);
            return;
        }

        List<FoodItem> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();

        for (FoodItem item : allFoodItems) {
            if (item.getName().toLowerCase().contains(lowerCaseQuery) ||
                    item.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                    item.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(item);
            }
        }

        adapter.updateList(filteredList);
    }

    public void updateCartSummary() {
        CurrencyManager cm = CurrencyManager.getInstance();
        int itemCount = CartManager.getInstance().getCartItemCount();
        double totalInBase = CartManager.getInstance().getTotalPrice();

        if (itemCount > 0) {
            textViewCartCount.setText(itemCount + " items");
            // Use CurrencyManager for formatting
            textViewCartTotal.setText(cm.formatPrice(totalInBase));
        } else {
            textViewCartCount.setText("0 items");
            // Use CurrencyManager for formatting of $0.00
            textViewCartTotal.setText(cm.formatPrice(0.0));
        }
    }

    @Override
    public void onRatesLoaded() {
        // Refresh the menu list and cart summary when rates are loaded or currency changes
        adapter.notifyDataSetChanged();
        updateCartSummary();
    }

    // NEW: Methods for Zen Quote Implementation

    private void fetchZenQuote() {
        // Execute the background task to fetch the quote
        new FetchQuoteTask().execute("https://zenquotes.io/api/quotes/random");
    }

    private class FetchQuoteTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the JSON (Zen Quotes API returns an array with one object)
                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject quoteObject = jsonArray.getJSONObject(0);
                    String quote = quoteObject.getString("q");
                    String author = quoteObject.getString("a");
                    return new String[]{quote, author};
                }

            } catch (Exception e) {
                // Handle exceptions (e.g., network error, JSON parsing error)
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null && result.length == 2) {
                // Update UI with the fetched quote and author
                textViewQuote.setText(String.format("“%s”", result[0]));
                textViewAuthor.setText(String.format("— %s", result[1]));
            } else {
                // Fallback quote if the API call fails
                textViewQuote.setText("“Food is the most primitive form of comfort.”");
                textViewAuthor.setText("— Sheila Graham");
            }
        }
    }

    // Existing loadFoodItems method remains the same
    private void loadFoodItems() {
        allFoodItems.clear();
        foodItemList.clear();

        // 10 Delicious Food Items with Your Image URLs
        allFoodItems.add(new FoodItem("Spicy Chicken Burger",
                "Crispy fried chicken with spicy mayo, fresh lettuce, tomato, and pickles. Served with golden fries.",
                12.99,
                "https://images.unsplash.com/photo-1690650262031-b01b6e172374?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8U3BpY3klMjBDaGlja2VuJTIwQnVyZ2VyfGVufDB8fDB8fHww",
                "Burgers",
                560));

        allFoodItems.add(new FoodItem("Classic Beef Burger",
                "Juicy beef patty with melted cheese, crisp lettuce, ripe tomato, and our special sauce on a toasted bun.",
                11.99,
                "https://images.unsplash.com/photo-1645024679624-e8351ac98f01?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MTF8fFNwaWN5JTIwQ2hpY2tlbiUyMEJ1cmdlcnxlbnwwfHwwfHx8MA%3D%3D",
                "Burgers",
                620));

        allFoodItems.add(new FoodItem("Margherita Pizza",
                "Classic Italian pizza with fresh tomato sauce, mozzarella, basil, and extra virgin olive oil.",
                14.99,
                "https://images.unsplash.com/photo-1564936281291-294551497d81?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8TWFyZ2hlcml0YSUyMFBpenphfGVufDB8fDB8fHww",
                "Pizza",
                850));

        allFoodItems.add(new FoodItem("Pepperoni Supreme",
                "Loaded with spicy pepperoni, mozzarella cheese, and our signature tomato sauce on thin crust.",
                16.99,
                "https://images.unsplash.com/photo-1595854341625-f33ee10dbf94?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8TWFyZ2hlcml0YSUyMFBpenphfGVufDB8fDB8fHww",
                "Pizza",
                920));

        allFoodItems.add(new FoodItem("Creamy Alfredo Pasta",
                "Fettuccine pasta tossed in rich, creamy Alfredo sauce with parmesan and fresh herbs.",
                11.99,
                "https://media.istockphoto.com/id/1501557025/photo/pasta.webp?a=1&s=612x612&w=0&k=20&c=lINOnT_giw8rfPqhQQ9KZkyL-23pUJmLOCZ5pcY0f-E=",
                "Pasta",
                720));

        allFoodItems.add(new FoodItem("Spaghetti Bolognese",
                "Traditional spaghetti with slow-cooked meat sauce, parmesan, and fresh basil.",
                12.99,
                "https://media.istockphoto.com/id/1357859588/photo/pasta-bolognese.webp?a=1&s=612x612&w=0&k=20&c=dZ2jzLixDHjl4S4lN1nR8NvoQUKDOqW1tAeubjQRtH0=",
                "Pasta",
                680));

        allFoodItems.add(new FoodItem("California Sushi Roll",
                "Fresh avocado, crab meat, cucumber wrapped in sushi rice and nori. Served with soy sauce.",
                8.99,
                "https://media.istockphoto.com/id/1139967411/photo/sushi-rolls-different-taste-rainbow-color-serving-food-background-top-view.webp?a=1&s=612x612&w=0&k=20&c=JTiYeUfIJo8IkECQimtGO1iCYzgmeWSshvuoM_34-9o=",
                "Asian",
                320));

        allFoodItems.add(new FoodItem("Grilled Salmon Steak",
                "Atlantic salmon grilled to perfection with lemon butter sauce and seasonal vegetables.",
                22.99,
                "https://media.istockphoto.com/id/2154123683/photo/fresh-cooked-delicious-salmon-steak-with-spices-and-herbs.webp?a=1&s=612x612&w=0&k=20&c=-Rc0TQl1gDrBp-0XO1Q3swT8KnhXitTSaJ5LgjHCXRA=",
                "Seafood",
                480));

        allFoodItems.add(new FoodItem("Chocolate Lava Cake",
                "Warm chocolate cake with molten center, served with vanilla ice cream and berry compote.",
                7.99,
                "https://images.unsplash.com/photo-1673551490812-eaee2e9bf0ef?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8Q2hvY29sYXRlJTIwTGF2YSUyMENha2V8ZW58MHx8MHx8fDA%3D",
                "Desserts",
                420));

        allFoodItems.add(new FoodItem("Fresh Berry Smoothie",
                "Mixed berries blended with yogurt and honey. Refreshing and packed with antioxidants.",
                5.99,
                "https://media.istockphoto.com/id/970519622/photo/yogurt-and-strawberry-smoothie-in-jar-on-wooden-table.webp?a=1&s=612x612&w=0&k=20&c=EiRyDpiD4x_vaP044o-DtmXfhAMo3ni-gKBhkgE1t1U=",
                "Drinks",
                180));

        // Bonus Items (You can add more if needed)
        allFoodItems.add(new FoodItem("BBQ Chicken Wings",
                "Crispy chicken wings glazed with homemade BBQ sauce. Served with ranch dip and celery sticks.",
                9.99,
                "https://media.istockphoto.com/id/1312295903/photo/glazed-bbq-chicken-wings-cooked-on-the-grill-closeup.webp?a=1&s=612x612&w=0&k=20&c=po1ulv8AcSLH-9KLyaA6p6JfjqPaYlw8WzLlt_stXOU=",
                "Appetizers",
                380));

        allFoodItems.add(new FoodItem("Beef Tacos",
                "Three soft tortillas filled with seasoned beef, lettuce, cheese, and fresh salsa.",
                10.99,
                "https://images.unsplash.com/photo-1687881063470-a78e6ea2590e?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8QmVlZiUyMFRhY29zfGVufDB8fDB8fHww",
                "Mexican",
                450));

        allFoodItems.add(new FoodItem("Gourmet Salad Bowl",
                "Mixed greens with cherry tomatoes, cucumber, olives, feta cheese, and balsamic dressing.",
                8.99,
                "https://images.unsplash.com/photo-1676300186659-030de568e39a?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8M3x8R291cm1ldCUyMFNhbGFkJTIwQm93bHxlbnwwfHwwfHx8MA%3D%3D",
                "Salads",
                280));

        // Add all items to display list
        foodItemList.addAll(allFoodItems);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartSummary();
    }
}