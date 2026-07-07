package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlarmManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRestaurantList();
        setupBottomNavigation();

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // Проверка разрешения на точные будильники (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Запросим разрешение (можно открыть настройки)
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void setupBottomNavigation() {
        Button btnRestaurants = findViewById(R.id.btnRestaurants);
        Button btnMyBookings = findViewById(R.id.btnMyBookings);

        btnRestaurants.setEnabled(false);

        btnMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyBookingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupRestaurantList() {
        ListView restaurantList = findViewById(R.id.restaurantList);
        List<RestaurantRepository.RestaurantInfo> restaurants = RestaurantRepository.getRestaurants();

        restaurantList.setAdapter(new RestaurantAdapter(restaurants));
        restaurantList.setOnItemClickListener((parent, view, position, id) -> {
            RestaurantRepository.RestaurantInfo restaurant = restaurants.get(position);
            Intent intent = new Intent(MainActivity.this, RestaurantHallActivity.class);
            intent.putExtra(RestaurantHallActivity.EXTRA_RESTAURANT_ID, restaurant.getId());
            startActivity(intent);
        });
    }

    private class RestaurantAdapter extends ArrayAdapter<RestaurantRepository.RestaurantInfo> {
        RestaurantAdapter(List<RestaurantRepository.RestaurantInfo> restaurants) {
            super(MainActivity.this, 0, restaurants);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_restaurant, parent, false);
            }

            RestaurantRepository.RestaurantInfo item = getItem(position);
            if (item != null) {
                ((TextView) convertView.findViewById(R.id.restaurantNameText)).setText(item.getName());
                ((TextView) convertView.findViewById(R.id.restaurantAddressText)).setText(item.getAddress());

                int tablesCount = item.getTables().size();
                String tablesCountText = getResources().getQuantityString(R.plurals.tables_count, tablesCount, tablesCount);

                ((TextView) convertView.findViewById(R.id.restaurantDetailsText)).setText(
                        getString(R.string.restaurant_card_details, tablesCountText, item.getDescription())
                );
            }
            return convertView;
        }
    }
}