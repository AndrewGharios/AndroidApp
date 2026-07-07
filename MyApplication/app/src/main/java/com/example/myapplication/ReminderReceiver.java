package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReminderReceiver", "Получен сигнал будильника!");

        String restaurantName = intent.getStringExtra("restaurant_name");
        String tableName = intent.getStringExtra("table_name");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");

        // Показываем Toast для отладки (убедимся, что приёмник сработал)
        Toast.makeText(context, "Напоминание: " + restaurantName + " " + tableName, Toast.LENGTH_LONG).show();

        String title = "⏰ Напоминание о бронировании";
        String message = "У вас бронь в ресторане \"" + restaurantName + "\", столик " + tableName +
                ", на " + date + " в " + time + ". Ждём вас!";

        NotificationHelper.showReminderNotification(context, title, message);
    }
}