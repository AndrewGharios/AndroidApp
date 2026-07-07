package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class BookingAlarmManager {

    private static final String TAG = "BookingAlarm";

    public static void scheduleReminder(Context context, String bookingId,
                                        String restaurantName, String tableName,
                                        String dateLabel, String timeLabel,
                                        int startMinutes, Calendar bookingDate) {

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // Compute exact booking start time in millis
            Calendar triggerCal = (Calendar) bookingDate.clone();
            triggerCal.set(Calendar.HOUR_OF_DAY, 0);
            triggerCal.set(Calendar.MINUTE, 0);
            triggerCal.set(Calendar.SECOND, 0);
            triggerCal.set(Calendar.MILLISECOND, 0);
            triggerCal.add(Calendar.MINUTE, startMinutes);
            long bookingStartMillis = triggerCal.getTimeInMillis();

            long triggerAtMillis = bookingStartMillis - 15 * 60 * 1000; // 15 minutes before

            // If already past, don't schedule
            if (triggerAtMillis < System.currentTimeMillis()) {
                Log.d(TAG, "Alarm time is in the past, skipping");
                return;
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("booking_id", bookingId);
            intent.putExtra("restaurant_name", restaurantName);
            intent.putExtra("table_name", tableName);
            intent.putExtra("date", dateLabel);
            intent.putExtra("time", timeLabel);

            int requestCode = bookingId.hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // ----- Permission checks and fallbacks -----
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ – exact alarm permission is required for setExact()
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Fallback: use setWindow (doesn't require exact permission)
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60000, pendingIntent);
                    Log.d(TAG, "Used setWindow fallback (no exact permission)");
                    return;
                }
            }

            // Try setAlarmClock first (most reliable), but catch SecurityException
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(triggerAtMillis, null);
                    alarmManager.setAlarmClock(info, pendingIntent);
                    Log.d(TAG, "AlarmClock set successfully");
                }
            } catch (SecurityException e) {
                Log.w(TAG, "setAlarmClock failed, falling back to setExact", e);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error in setAlarmClock, using setExact", e);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }

        } catch (Exception e) {
            // Catch-all: never let scheduling crash the app
            Log.e(TAG, "Failed to schedule reminder", e);
        }
    }

    public static void cancelReminder(Context context, String bookingId) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ReminderReceiver.class);
            int requestCode = bookingId.hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d(TAG, "Reminder cancelled for " + bookingId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel reminder", e);
        }
    }
}