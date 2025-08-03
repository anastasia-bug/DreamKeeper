package com.example.dreamkeeper;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class DreamKeeperApplication  extends Application {

    private static DatabaseHelper databaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(this);

        NotificationChannel channel = new NotificationChannel(
                "dream_reminders",
                "Напоминания",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Напоминания о снах");
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

    }

    public static DatabaseHelper getDreamDatabaseHelper() {
        return databaseHelper;
    }
}

