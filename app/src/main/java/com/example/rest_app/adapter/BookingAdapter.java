package com.example.rest_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rest_app.R;
import com.example.rest_app.model.Booking;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private Context context;
    private List<Booking> bookingList;
    private BookingActionListener actionListener;

    public interface BookingActionListener {
        void onDeleteBooking(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookingList, BookingActionListener actionListener) {
        this.context = context;
        this.bookingList = bookingList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.textViewName.setText(booking.getName());
        holder.textViewDateTime.setText(booking.getDate() + " at " + booking.getTime());
        holder.textViewGuests.setText(booking.getGuests() + " guests");
        holder.textViewPhone.setText("Phone: " + booking.getPhone());
        holder.textViewStatus.setText(booking.getStatus());

        // Set status color
        if ("Confirmed".equals(booking.getStatus())) {
            holder.textViewStatus.setTextColor(context.getResources().getColor(R.color.primary));
            holder.textViewStatus.setBackgroundResource(R.drawable.status_background);
        } else {
            holder.textViewStatus.setTextColor(context.getResources().getColor(R.color.text_secondary));
            holder.textViewStatus.setBackgroundResource(R.drawable.status_background);
        }

        // Set up delete button
        holder.buttonDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteBooking(booking);
            }
        });

        // Card click listener for potential future actions
        holder.cardView.setOnClickListener(v -> {
            // You can add edit functionality here later
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateList(List<Booking> newList) {
        this.bookingList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewDateTime;
        TextView textViewGuests;
        TextView textViewPhone;
        TextView textViewStatus;
        Button buttonDelete;
        View cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewGuests = itemView.findViewById(R.id.textViewGuests);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}