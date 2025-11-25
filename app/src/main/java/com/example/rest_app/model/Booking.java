package com.example.rest_app.model;

import java.io.Serializable;

public class Booking implements Serializable {
    private String name;
    private String phone;
    private String date;
    private String time;
    private String guests;
    private String bookingId;
    private String status;

    public Booking() {
        // Default constructor
    }

    public Booking(String name, String phone, String date, String time, String guests) {
        this.name = name;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.bookingId = String.valueOf(System.currentTimeMillis());
        this.status = "Confirmed";
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getGuests() { return guests; }
    public void setGuests(String guests) { this.guests = guests; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}