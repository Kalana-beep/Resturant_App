package com.example.rest_app.ui.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rest_app.R;
import com.example.rest_app.adapter.BookingAdapter;
import com.example.rest_app.model.Booking;
import com.example.rest_app.utils.FirebaseAuthManager;
import com.example.rest_app.utils.ImageLoader;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationFragment extends Fragment {

    private EditText editTextName, editTextPhone, editTextDate, editTextTime;
    private Spinner spinnerGuests;
    private Button buttonBookTable;
    private TextInputLayout textInputLayoutName, textInputLayoutPhone;

    private RecyclerView recyclerViewBookings;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private TextView textViewNoBookings;
    private ImageView backgroundImage;
    private FirebaseAuthManager authManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);

        initializeViews(view);
        setupFirebase();
        setupSpinner();
        setupRecyclerView();
        setupClickListeners();
        loadBackgroundImage();
        loadBookingsFromFirestore();

        return view;
    }

    private void initializeViews(View view) {
        editTextName = view.findViewById(R.id.editTextName);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextTime = view.findViewById(R.id.editTextTime);
        spinnerGuests = view.findViewById(R.id.spinnerGuests);
        buttonBookTable = view.findViewById(R.id.buttonBookTable);
        textInputLayoutName = view.findViewById(R.id.textInputLayoutName);
        textInputLayoutPhone = view.findViewById(R.id.textInputLayoutPhone);
        backgroundImage = view.findViewById(R.id.backgroundImage);

        recyclerViewBookings = view.findViewById(R.id.recyclerViewBookings);
        textViewNoBookings = view.findViewById(R.id.textViewNoBookings);

        bookingList = new ArrayList<>();
    }

    private void setupFirebase() {
        authManager = FirebaseAuthManager.getInstance(requireContext());

        // Pre-fill name if user is logged in
        if (authManager.isUserLoggedIn() && authManager.getCurrentUserName() != null) {
            editTextName.setText(authManager.getCurrentUserName());
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewBookings.setLayoutManager(layoutManager);

        bookingAdapter = new BookingAdapter(getContext(), bookingList, new BookingAdapter.BookingActionListener() {
            @Override
            public void onDeleteBooking(Booking booking) {
                showDeleteConfirmationDialog(booking);
            }
        });
        recyclerViewBookings.setAdapter(bookingAdapter);
        updateNoBookingsVisibility();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.guests_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuests.setAdapter(adapter);
    }

    private void setupClickListeners() {
        editTextDate.setOnClickListener(v -> showDatePicker());
        editTextTime.setOnClickListener(v -> showTimePicker());
        buttonBookTable.setOnClickListener(v -> bookTable());
    }

    private void loadBackgroundImage() {
        String backgroundUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2070&q=80";
        ImageLoader.loadBackground(getContext(), backgroundImage, backgroundUrl);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                    editTextDate.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute1) -> {
                    String selectedTime = String.format("%02d:%02d", hourOfDay, minute1);
                    editTextTime.setText(selectedTime);
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    private void bookTable() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String time = editTextTime.getText().toString().trim();
        String guests = spinnerGuests.getSelectedItem().toString();

        if (validateInputs(name, phone, date, time)) {
            saveBookingToFirestore(name, phone, date, time, guests);
        }
    }

    private void saveBookingToFirestore(String name, String phone, String date, String time, String guests) {
        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Please log in to book a table", Toast.LENGTH_SHORT).show();
            return;
        }

        String bookingId = "BK_" + System.currentTimeMillis();

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("bookingId", bookingId);
        bookingData.put("userId", userId);
        bookingData.put("name", name);
        bookingData.put("phone", phone);
        bookingData.put("date", date);
        bookingData.put("time", time);
        bookingData.put("guests", guests);
        bookingData.put("status", "Confirmed");
        bookingData.put("timestamp", System.currentTimeMillis());
        bookingData.put("userEmail", authManager.getCurrentUserEmail());

        // Use set() instead of add() to ensure booking is saved
        authManager.getFirestore().collection("bookings")
                .document(bookingId)
                .set(bookingData)
                .addOnSuccessListener(aVoid -> {
                    Booking newBooking = new Booking(name, phone, date, time, guests);
                    newBooking.setBookingId(bookingId);
                    newBooking.setStatus("Confirmed");

                    bookingList.add(0, newBooking);
                    bookingAdapter.notifyItemInserted(0);
                    recyclerViewBookings.smoothScrollToPosition(0);

                    updateNoBookingsVisibility();
                    Toast.makeText(getContext(), "Table booked successfully! ✅", Toast.LENGTH_LONG).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to book table: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadBookingsFromFirestore() {
        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            textViewNoBookings.setText("Please log in to see your bookings");
            return;
        }

        authManager.getFirestore().collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Booking booking = new Booking(
                                    document.getString("name"),
                                    document.getString("phone"),
                                    document.getString("date"),
                                    document.getString("time"),
                                    document.getString("guests")
                            );
                            booking.setBookingId(document.getString("bookingId"));
                            booking.setStatus(document.getString("status"));
                            bookingList.add(booking);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Error loading booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    bookingAdapter.notifyDataSetChanged();
                    updateNoBookingsVisibility();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String name, String phone, String date, String time) {
        boolean isValid = true;

        if (name.isEmpty()) {
            textInputLayoutName.setError("Name is required");
            isValid = false;
        } else {
            textInputLayoutName.setError(null);
        }

        if (phone.isEmpty()) {
            textInputLayoutPhone.setError("Phone is required");
            isValid = false;
        } else {
            textInputLayoutPhone.setError(null);
        }

        if (date.isEmpty()) {
            editTextDate.setError("Date is required");
            isValid = false;
        } else {
            editTextDate.setError(null);
        }

        if (time.isEmpty()) {
            editTextTime.setError("Time is required");
            isValid = false;
        } else {
            editTextTime.setError(null);
        }

        return isValid;
    }

    private void clearForm() {
        editTextPhone.setText("");
        editTextDate.setText("");
        editTextTime.setText("");
        spinnerGuests.setSelection(0);
    }

    private void updateNoBookingsVisibility() {
        if (bookingList.isEmpty()) {
            textViewNoBookings.setVisibility(View.VISIBLE);
            recyclerViewBookings.setVisibility(View.GONE);
        } else {
            textViewNoBookings.setVisibility(View.GONE);
            recyclerViewBookings.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteConfirmationDialog(Booking booking) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Cancel Booking");
        builder.setMessage("Are you sure you want to cancel this booking?");
        builder.setPositiveButton("Yes, Cancel", (dialog, which) -> {
            deleteBookingFromFirestore(booking);
        });
        builder.setNegativeButton("Keep Booking", null);
        builder.show();
    }

    private void deleteBookingFromFirestore(Booking booking) {
        authManager.getFirestore().collection("bookings")
                .document(booking.getBookingId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    int position = bookingList.indexOf(booking);
                    if (position != -1) {
                        bookingList.remove(position);
                        bookingAdapter.notifyItemRemoved(position);
                        updateNoBookingsVisibility();
                        Toast.makeText(getContext(), "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to cancel booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookingsFromFirestore();
    }
}