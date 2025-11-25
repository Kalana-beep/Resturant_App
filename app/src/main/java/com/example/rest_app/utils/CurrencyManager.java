package com.example.rest_app.utils;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CurrencyManager {
    private static final String TAG = "CurrencyManager";
    private static CurrencyManager instance;

    // Use your specific API Key for ExchangeRate-API
    private static final String API_KEY = "97a4dcdc46f82f4dffe77771";
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/USD";
    private static final String BASE_CURRENCY = "USD";

    // Cache the exchange rates
    private final Map<String, Double> exchangeRates = new HashMap<>();
    private String currentCurrencyCode = BASE_CURRENCY;
    private CurrencyUpdateListener listener;

    public interface CurrencyUpdateListener {
        void onRatesLoaded();
    }

    private CurrencyManager() {
        // Default conversion: 1.0 for the base currency
        exchangeRates.put(BASE_CURRENCY, 1.0);
    }

    public static synchronized CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }

    public void setCurrencyUpdateListener(CurrencyUpdateListener listener) {
        this.listener = listener;
    }

    public void fetchExchangeRates() {
        // Only fetch if rates are empty or on a regular interval
        if (exchangeRates.size() <= 1) {
            new FetchRatesTask().execute(API_BASE_URL);
        }
    }

    /**
     * Converts price from BASE_CURRENCY (USD) to the currently selected currency.
     */
    public double convertPrice(double priceInBase) {
        if (currentCurrencyCode.equals(BASE_CURRENCY) || !exchangeRates.containsKey(currentCurrencyCode)) {
            return priceInBase;
        }
        Double rate = exchangeRates.get(currentCurrencyCode);
        return rate != null ? priceInBase * rate : priceInBase;
    }

    /**
     * Formats the price with the currency symbol and conversion applied.
     */
    public String formatPrice(double priceInBase) {
        double convertedPrice = convertPrice(priceInBase);

        switch (currentCurrencyCode) {
            case "EUR":
                return String.format(Locale.US, "€%.2f", convertedPrice);
            case "GBP":
                return String.format(Locale.US, "£%.2f", convertedPrice);
            case "JPY":
                return String.format(Locale.US, "¥%.0f", convertedPrice); // No decimals for JPY
            case "INR":
                return String.format(Locale.US, "₹%.2f", convertedPrice);
            case "CAD":
                return String.format(Locale.US, "C$%.2f", convertedPrice);
            case BASE_CURRENCY: // USD
            default:
                return String.format(Locale.US, "$%.2f", convertedPrice);
        }
    }

    public String getCurrentCurrencyCode() {
        return currentCurrencyCode;
    }

    /**
     * Sets the new currency code and triggers a UI update.
     */
    public void setCurrentCurrencyCode(String currencyCode) {
        this.currentCurrencyCode = currencyCode;
        if (listener != null) {
            listener.onRatesLoaded(); // Trigger UI refresh in fragments
        }
    }

    // AsyncTask for fetching rates from the API
    private class FetchRatesTask extends AsyncTask<String, Void, Map<String, Double>> {
        @Override
        protected Map<String, Double> doInBackground(String... urls) {
            Map<String, Double> rates = new HashMap<>();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String resultStatus = jsonResponse.getString("result");
                    if (resultStatus.equals("success")) {
                        JSONObject ratesObject = jsonResponse.getJSONObject("conversion_rates");

                        // Extract specific currency codes. Add more as needed.
                        rates.put("USD", ratesObject.getDouble("USD"));
                        rates.put("EUR", ratesObject.getDouble("EUR"));
                        rates.put("GBP", ratesObject.getDouble("GBP"));
                        rates.put("JPY", ratesObject.getDouble("JPY"));
                        rates.put("INR", ratesObject.getDouble("INR"));
                        rates.put("CAD", ratesObject.getDouble("CAD"));
                    } else {
                        Log.e(TAG, "API call failed. Error: " + jsonResponse.optString("error-type"));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching exchange rates: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return rates;
        }

        @Override
        protected void onPostExecute(Map<String, Double> result) {
            if (result != null && !result.isEmpty()) {
                exchangeRates.putAll(result);
                Log.d(TAG, "Exchange rates loaded successfully: " + exchangeRates.keySet());
            } else {
                Log.e(TAG, "Failed to load exchange rates. Using default USD.");
                // Retain default USD rate if API fails
            }

            if (listener != null) {
                listener.onRatesLoaded();
            }
        }
    }
}