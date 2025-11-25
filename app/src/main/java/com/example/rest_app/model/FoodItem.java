package com.example.rest_app.model;

public class FoodItem {
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private int quantity;
    private int calories;

    // Constructor with calories
    public FoodItem(String name, String description, double price, String imageUrl, String category, int calories) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.quantity = 0;
        this.calories = calories;
    }

    // Constructor without calories (for backward compatibility)
    public FoodItem(String name, String description, double price, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.quantity = 0;
        this.calories = 0;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
}