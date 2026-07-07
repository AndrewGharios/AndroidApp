package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MyBookingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "restaurant_bookings";

    private EditText nameInput;
    private Button searchButton;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private BookingAdapter adapter;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        nameInput = findViewById(R.id.nameSearchInput);
        searchButton = findViewById(R.id.searchButton);
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(new ArrayList<>(), this::cancelBooking);
        recyclerView.setAdapter(adapter);

        // Show all bookings by default
        loadAllBookings();

        searchButton.setOnClickListener(v -> searchBookings());
        setupBottomNavigation();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupBottomNavigation() {
        Button btnRestaurants = findViewById(R.id.btnRestaurants);
        Button btnMyBookings = findViewById(R.id.btnMyBookings);

        btnMyBookings.setEnabled(false);

        btnRestaurants.setOnClickListener(v -> {
            Intent intent = new Intent(MyBookingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadAllBookings() {
        searchBookings(null);
    }

    private void searchBookings() {
        String name = nameInput.getText().toString().trim();
        searchBookings(name);
    }

    @SuppressWarnings("unchecked")
    private void searchBookings(String filterName) {
        List<BookingItem> bookings = new ArrayList<>();
        Map<String, ?> allPrefs = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();
            // key format: restaurantId:yyyy-MM-dd:tableName
            String[] parts = key.split(":");
            if (parts.length < 3) continue;

            String restaurantId = parts[0];
            String date = parts[1];
            String tableName = parts[2];

            // Получаем название ресторана по ID
            RestaurantRepository.RestaurantInfo restaurant = RestaurantRepository.getRestaurantById(restaurantId);
            String restaurantName = (restaurant != null) ? restaurant.getName() : "Неизвестный ресторан";

            Set<String> bookingSet = (Set<String>) entry.getValue();
            if (bookingSet == null) continue;

            for (String record : bookingSet) {
                String[] fields = record.split("\\|");
                if (fields.length < 4) continue;
                int start = Integer.parseInt(fields[0]);
                int duration = Integer.parseInt(fields[1]);
                String guestName = fields[2];
                String phone = fields[3];

                boolean matches = (filterName == null || filterName.isEmpty()) ||
                        guestName.equalsIgnoreCase(filterName);

                if (matches) {
                    // Генерируем bookingId (для отмены будильника)
                    String bookingId = key + ":" + start + ":" + duration;
                    bookings.add(new BookingItem(key, record, tableName, restaurantName, date, start, duration, phone, bookingId));
                }
            }
        }

        if (bookings.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setBookings(bookings);
        }
    }

    private void cancelBooking(BookingItem item) {
        // Отменяем будильник
        BookingAlarmManager.cancelReminder(this, item.bookingId);

        Set<String> bookingsSet = sharedPreferences.getStringSet(item.key, new HashSet<>());
        if (bookingsSet != null) {
            Set<String> updatedSet = new HashSet<>(bookingsSet);
            updatedSet.remove(item.record);
            sharedPreferences.edit().putStringSet(item.key, updatedSet).apply();

            Toast.makeText(this, R.string.booking_cancelled, Toast.LENGTH_SHORT).show();
            String filter = nameInput.getText().toString().trim();
            searchBookings(filter);
        }
    }

    // ---------- Inner classes ----------

    private static class BookingItem {
        String key;
        String record;
        String tableName;
        String restaurantName;   // <-- новое поле
        String date;
        int start;
        int duration;
        String phone;
        String bookingId;

        BookingItem(String key, String record, String tableName, String restaurantName,
                    String date, int start, int duration, String phone, String bookingId) {
            this.key = key;
            this.record = record;
            this.tableName = tableName;
            this.restaurantName = restaurantName;
            this.date = date;
            this.start = start;
            this.duration = duration;
            this.phone = phone;
            this.bookingId = bookingId;
        }
    }

    private static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
        private List<BookingItem> bookings;
        private final OnCancelListener cancelListener;

        interface OnCancelListener {
            void onCancel(BookingItem item);
        }

        BookingAdapter(List<BookingItem> bookings, OnCancelListener listener) {
            this.bookings = bookings;
            this.cancelListener = listener;
        }

        void setBookings(List<BookingItem> bookings) {
            this.bookings = bookings;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BookingItem item = bookings.get(position);
            SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));

            try {
                String dateStr = displayFormat.format(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(item.date));

                // Показываем ресторан и столик в одной строке
                String restaurantAndTable = holder.itemView.getContext().getString(R.string.restaurant_and_table,
                        item.restaurantName, item.tableName);
                holder.tableText.setText(restaurantAndTable);

                holder.dateText.setText(holder.itemView.getContext().getString(R.string.booking_date, dateStr));
                holder.timeText.setText(holder.itemView.getContext().getString(R.string.booking_time, formatTime(item.start) + "-" + formatTime(item.start + item.duration)));
                holder.durationText.setText(holder.itemView.getContext().getString(R.string.booking_duration, item.duration));
                holder.phoneText.setText(holder.itemView.getContext().getString(R.string.booking_phone, item.phone));
            } catch (Exception e) {
                holder.dateText.setText(item.date);
            }

            holder.cancelButton.setOnClickListener(v -> cancelListener.onCancel(item));
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        private String formatTime(int min) {
            return String.format(Locale.getDefault(), "%02d:%02d", min / 60, min % 60);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tableText, dateText, timeText, durationText, phoneText;
            Button cancelButton;

            ViewHolder(View itemView) {
                super(itemView);
                tableText = itemView.findViewById(R.id.bookingTableText);
                dateText = itemView.findViewById(R.id.bookingDateText);
                timeText = itemView.findViewById(R.id.bookingTimeText);
                durationText = itemView.findViewById(R.id.bookingDurationText);
                phoneText = itemView.findViewById(R.id.bookingPhoneText);
                cancelButton = itemView.findViewById(R.id.cancelButton);
            }
        }
    }
}