package com.example.dreamkeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "dream_reminders")
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("Доброе утро")
                .setContentText("Пришло время записать свой сон, пока он окончательно не забылся.");

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("NOTIFICATION", "Уведомления выключены");
        }
        else {
            Log.d("NOTIFICATION", "Уведомления работают");
            manager.notify(1, builder.build());
        }

        SettingsFragment.scheduleReminder(context);
    }
}
