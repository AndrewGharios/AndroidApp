package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RestaurantHallActivity extends AppCompatActivity {

    public static final String EXTRA_RESTAURANT_ID = "restaurant_id";

    private static final String PREFS_NAME = "restaurant_bookings";
    private static final int OPEN_MINUTES = 8 * 60;
    private static final int CLOSE_MINUTES = 22 * 60;
    private static final int MIN_BOOKING_MINUTES = 60;
    private static final int CLEANUP_MINUTES = 30;
    private static final int TIME_STEP_MINUTES = 30;
    private static final int DEFAULT_TABLE_SIZE_DP = 60;

    private final Calendar selectedDate = Calendar.getInstance();
    private final SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat dateLabelFormat = new SimpleDateFormat("d MMMM", new Locale("ru"));

    private RestaurantRepository.RestaurantInfo restaurant;
    private SharedPreferences sharedPreferences;

    private TextView titleText, subtitleText, dateValueText;
    private TextView entranceLabel, restroomLabel, kitchenLabel;
    private FrameLayout tableLayer;
    private final List<LinearLayout> tableViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_hall);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnViewMenu = findViewById(R.id.btnViewMenu);
        btnViewMenu.setOnClickListener(v -> showMenuDialog());

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String restaurantId = getIntent().getStringExtra(EXTRA_RESTAURANT_ID);
        restaurant = RestaurantRepository.getRestaurantById(restaurantId);

        if (restaurant == null) {
            finish();
            return;
        }

        bindViews();
        setupRestaurantInfo();
        setupDatePicker();
        setupTables();
        updateDateLabel();
        positionSchemeElements();
        refreshTableStates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTableStates();
    }

    private void showMenuDialog() {
        com.github.chrisbanes.photoview.PhotoView photoView = new com.github.chrisbanes.photoview.PhotoView(this);
        photoView.setImageResource(restaurant.getMenuImageResId());
        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        photoView.setMaximumScale(5.0f);
        photoView.setMediumScale(2.0f);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Меню");
        builder.setView(photoView);
        builder.setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void bindViews() {
        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        dateValueText = findViewById(R.id.dateValueText);
        entranceLabel = findViewById(R.id.entranceLabel);
        restroomLabel = findViewById(R.id.restroomLabel);
        kitchenLabel = findViewById(R.id.kitchenLabel);
        tableLayer = findViewById(R.id.tableLayer);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRestaurantInfo() {
        titleText.setText(restaurant.getName());
        subtitleText.setText(restaurant.getAddress());
        configureRoomView(entranceLabel, restaurant.getEntrance());
        configureRoomView(restroomLabel, restaurant.getRestroom());
        configureRoomView(kitchenLabel, restaurant.getKitchen());
    }

    private void configureRoomView(TextView roomView, RestaurantRepository.RoomInfo roomInfo) {
        roomView.setText(roomInfo.getLabel());
        ViewGroup.LayoutParams params = roomView.getLayoutParams();
        params.width = dpToPx(roomInfo.getWidthDp());
        params.height = dpToPx(roomInfo.getHeightDp());
        roomView.setLayoutParams(params);
    }

    // ===== FIXED: Date picker now prevents past dates =====
    private void setupDatePicker() {
        findViewById(R.id.dateCard).setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate.set(year, month, day);
                updateDateLabel();
                refreshTableStates();
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    private void setupTables() {
        tableLayer.removeAllViews();
        tableViews.clear();
        for (RestaurantRepository.TableInfo table : restaurant.getTables()) {
            LinearLayout tableView = createTableView(table);
            tableLayer.addView(tableView);
            tableViews.add(tableView);
        }
    }

    private LinearLayout createTableView(RestaurantRepository.TableInfo table) {
        LinearLayout tableView = new LinearLayout(this);
        tableView.setLayoutParams(new FrameLayout.LayoutParams(dpToPx(DEFAULT_TABLE_SIZE_DP), dpToPx(DEFAULT_TABLE_SIZE_DP)));
        tableView.setOrientation(LinearLayout.VERTICAL);
        tableView.setGravity(android.view.Gravity.CENTER);
        tableView.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        tableView.setBackgroundResource(R.drawable.bg_date_card);
        tableView.setClickable(true);
        tableView.setFocusable(true);

        TextView titleView = new TextView(this);
        titleView.setText(table.getName());
        titleView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        titleView.setTextSize(14);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView seatsView = new TextView(this);
        seatsView.setText(formatSeats(table.getCapacity()));
        seatsView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        seatsView.setTextSize(11);

        tableView.addView(titleView);
        tableView.addView(seatsView);
        tableView.setOnClickListener(v -> showBookingDialog(table));
        return tableView;
    }

    private void positionSchemeElements() {
        tableLayer.post(() -> {
            positionRoomView(entranceLabel, restaurant.getEntrance());
            positionRoomView(restroomLabel, restaurant.getRestroom());
            positionRoomView(kitchenLabel, restaurant.getKitchen());

            int padL = tableLayer.getPaddingLeft();
            int padT = tableLayer.getPaddingTop();
            int wLimit = tableLayer.getWidth() - padL - tableLayer.getPaddingRight();
            int hLimit = tableLayer.getHeight() - padT - tableLayer.getPaddingBottom();

            for (int i = 0; i < restaurant.getTables().size(); i++) {
                RestaurantRepository.TableInfo table = restaurant.getTables().get(i);
                LinearLayout view = tableViews.get(i);
                int w = view.getWidth() > 0 ? view.getWidth() : dpToPx(DEFAULT_TABLE_SIZE_DP);
                int h = view.getHeight() > 0 ? view.getHeight() : dpToPx(DEFAULT_TABLE_SIZE_DP);

                view.setX(padL + (wLimit - w) * table.getXPercent());
                view.setY(padT + (hLimit - h) * table.getYPercent());
            }
        });
    }

    private void positionRoomView(TextView view, RestaurantRepository.RoomInfo info) {
        ViewGroup parent = (ViewGroup) view.getParent();
        int padL = parent.getPaddingLeft();
        int padT = parent.getPaddingTop();
        int wLimit = parent.getWidth() - padL - parent.getPaddingRight();
        int hLimit = parent.getHeight() - padT - parent.getPaddingBottom();

        int w = view.getWidth() > 0 ? view.getWidth() : dpToPx(info.getWidthDp());
        int h = view.getHeight() > 0 ? view.getHeight() : dpToPx(info.getHeightDp());

        view.setX(padL + (wLimit - w) * info.getXPercent());
        view.setY(padT + (hLimit - h) * info.getYPercent());
    }

    private void updateDateLabel() {
        String dateStr = dateLabelFormat.format(selectedDate.getTime());
        dateValueText.setText(dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1));
    }

    private void refreshTableStates() {
        for (int i = 0; i < restaurant.getTables().size(); i++) {
            boolean isAvailable = !calculateAvailableIntervals(restaurant.getTables().get(i)).isEmpty();
            tintView(tableViews.get(i), isAvailable ? R.color.table_available : R.color.table_busy);
        }
    }

    private void showBookingDialog(RestaurantRepository.TableInfo table) {
        List<TimeInterval> intervals = calculateAvailableIntervals(table);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_booking, null);

        ((TextView) dialogView.findViewById(R.id.dialogDateText)).setText(getString(R.string.dialog_date_template, dateLabelFormat.format(selectedDate.getTime())));
        ((TextView) dialogView.findViewById(R.id.dialogCapacityText)).setText(getString(R.string.dialog_capacity_template, formatSeats(table.getCapacity())));
        ((TextView) dialogView.findViewById(R.id.dialogSlotsText)).setText(formatIntervalsList(intervals));
        ((EditText) dialogView.findViewById(R.id.nameInput)).setKeyListener(TextKeyListener.getInstance(true, TextKeyListener.Capitalize.WORDS));

        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText phoneInput = dialogView.findViewById(R.id.phoneInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_template, table.getName()))
                .setView(dialogView)
                .setNegativeButton(R.string.dialog_close_button, null)
                .setPositiveButton(R.string.dialog_book_button, null)
                .create();

        dialog.setOnShowListener(d -> {
            if (intervals.isEmpty()) {
                Toast.makeText(this, R.string.toast_table_busy, Toast.LENGTH_SHORT).show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                return;
            }

            Spinner startSpinner = dialogView.findViewById(R.id.timeSpinner);
            Spinner durationSpinner = dialogView.findViewById(R.id.durationSpinner);
            List<StartOption> options = buildStartOptions(intervals);
            startSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));

            startSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                    updateDurationSpinner(durationSpinner, options.get(pos));
                }
                @Override public void onNothingSelected(AdapterView<?> p) {}
            });

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    nameInput.setError(getString(R.string.toast_pick_name));
                    return;
                }

                String phone = phoneInput.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    phoneInput.setError(getString(R.string.toast_pick_phone));
                    return;
                }

                StartOption start = (StartOption) startSpinner.getSelectedItem();
                DurationOption duration = (DurationOption) durationSpinner.getSelectedItem();
                saveBooking(table, name, phone, start.minutes, duration.minutes);
                refreshTableStates();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void updateDurationSpinner(Spinner spinner, StartOption start) {
        List<DurationOption> options = new ArrayList<>();
        int max = start.parentInterval.end - start.minutes;
        for (int d = MIN_BOOKING_MINUTES; d <= max; d += TIME_STEP_MINUTES) options.add(new DurationOption(d));
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));
    }

    private List<TimeInterval> calculateAvailableIntervals(RestaurantRepository.TableInfo table) {
        List<TimeInterval> occupied = getMergedOccupied(table);
        List<TimeInterval> available = new ArrayList<>();

        int current = OPEN_MINUTES;
        boolean isToday = isToday(selectedDate);
        if (isToday) {
            current = Math.max(current, getCurrentMinutes());
        }

        for (TimeInterval obs : occupied) {
            int gapEnd = obs.start - CLEANUP_MINUTES;
            if (gapEnd - current >= MIN_BOOKING_MINUTES) {
                available.add(new TimeInterval(current, gapEnd));
            }
            current = Math.max(current, obs.end);
        }

        if (CLOSE_MINUTES - current >= MIN_BOOKING_MINUTES) {
            available.add(new TimeInterval(current, CLOSE_MINUTES));
        }

        return available;
    }

    private List<TimeInterval> getMergedOccupied(RestaurantRepository.TableInfo table) {
        Set<String> saved = sharedPreferences.getStringSet(getPrefsKey(table), new HashSet<>());
        List<TimeInterval> list = new ArrayList<>();
        for (String s : saved) {
            String[] p = s.split("\\|");
            if (p.length >= 2) list.add(new TimeInterval(Integer.parseInt(p[0]), Integer.parseInt(p[0]) + Integer.parseInt(p[1]) + CLEANUP_MINUTES));
        }
        Collections.sort(list, Comparator.comparingInt(i -> i.start));
        if (list.isEmpty()) return list;
        List<TimeInterval> merged = new ArrayList<>();
        TimeInterval curr = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            TimeInterval next = list.get(i);
            if (next.start <= curr.end) curr = new TimeInterval(curr.start, Math.max(curr.end, next.end));
            else { merged.add(curr); curr = next; }
        }
        merged.add(curr);
        return merged;
    }

    // ===== New helper methods for date/time checks =====
    private boolean isToday(Calendar cal) {
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }

    private int getCurrentMinutes() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
    }

    // ===== Updated saveBooking with reminder scheduling =====
    private void saveBooking(RestaurantRepository.TableInfo table, String name, String phone, int start, int duration) {
        String key = getPrefsKey(table);
        Set<String> updated = new HashSet<>(sharedPreferences.getStringSet(key, new HashSet<>()));
        String record = start + "|" + duration + "|" + name.replace("|", " ") + "|" + phone.replace("|", " ");
        updated.add(record);
        sharedPreferences.edit().putStringSet(key, updated).apply();

        // Schedule reminder 15 minutes before
        String bookingId = restaurant.getId() + ":" + dateKeyFormat.format(selectedDate.getTime()) + ":" + table.getName() + ":" + start + ":" + duration;
        String dateStr = dateLabelFormat.format(selectedDate.getTime());
        String timeStr = formatTime(start);


        try {
            BookingAlarmManager.scheduleReminder(
                    this,
                    bookingId,
                    restaurant.getName(),
                    table.getName(),
                    dateStr,
                    timeStr,
                    start,
                    selectedDate
            );
        } catch (Exception e) {
            Log.e("Booking", "Failed to schedule reminder", e);
        }
    }

    private String getPrefsKey(RestaurantRepository.TableInfo table) {
        return restaurant.getId() + ":" + dateKeyFormat.format(selectedDate.getTime()) + ":" + table.getName();
    }

    private String formatSeats(int count) {
        return getResources().getQuantityString(R.plurals.guests_count, count, count);
    }

    private String formatIntervalsList(List<TimeInterval> intervals) {
        if (intervals.isEmpty()) return getString(R.string.fully_booked);
        List<String> lines = new ArrayList<>();
        for (TimeInterval i : intervals) lines.add(getString(R.string.slot_line_template, formatTime(i.start) + "-" + formatTime(i.end)));
        return TextUtils.join(getString(R.string.slot_separator), lines);
    }

    private String formatTime(int min) {
        return String.format(Locale.getDefault(), "%02d:%02d", min / 60, min % 60);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void tintView(View v, @ColorRes int color) {
        Drawable wrapped = DrawableCompat.wrap(v.getBackground().mutate());
        DrawableCompat.setTint(wrapped, ContextCompat.getColor(this, color));
        v.setBackground(wrapped);
    }

    private static class TimeInterval {
        final int start, end;
        TimeInterval(int s, int e) { this.start = s; this.end = e; }
    }

    private class StartOption {
        final int minutes;
        final TimeInterval parentInterval;
        StartOption(int m, TimeInterval i) { this.minutes = m; this.parentInterval = i; }
        @Override public String toString() { return formatTime(minutes); }
    }

    private static class DurationOption {
        final int minutes;
        DurationOption(int m) { this.minutes = m; }
        @Override public String toString() {
            int h = minutes / 60, m = minutes % 60;
            return m == 0 ? h + " ч" : h + " ч " + m + " мин";
        }
    }

    private List<StartOption> buildStartOptions(List<TimeInterval> intervals) {
        List<StartOption> options = new ArrayList<>();
        for (TimeInterval i : intervals) {
            for (int s = i.start; s + MIN_BOOKING_MINUTES <= i.end; s += TIME_STEP_MINUTES) options.add(new StartOption(s, i));
        }
        return options;
    }
}