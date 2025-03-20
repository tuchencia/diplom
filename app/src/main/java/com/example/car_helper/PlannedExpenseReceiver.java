package com.example.car_helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class PlannedExpenseReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Создаем уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "planned_expenses_channel")
                .setSmallIcon(R.drawable.carsplash) // Иконка уведомления
                .setContentTitle(title) // Заголовок уведомления
                .setContentText(message) // Текст уведомления
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Приоритет
                .setAutoCancel(true); // Уведомление исчезает после нажатия

        // Показываем уведомление
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

    }
}
