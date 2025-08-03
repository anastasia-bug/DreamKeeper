package com.example.dreamkeeper;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        SwitchPreferenceCompat reminderSwitch = findPreference("reminder_enabled");
        reminderSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (Boolean) newValue;
            if (enabled) {
                if (!notificationsEnabled())
                {
                    return false;
                }
                if (prefs.getString("reminder_time", "-1").equals("-1")) {
                    showTimePicker();
                }
                scheduleReminder(requireContext());
            } else {
                cancelReminder(requireContext());
            }
            return true;
        });

        Preference reminderPreference = findPreference("reminder_time");
        reminderPreference.setOnPreferenceClickListener(preference -> {
            showTimePicker();
            return true;
        });

        Preference realityCheckPreference = findPreference("reality_check_settings");
        if (realityCheckPreference != null) {
            realityCheckPreference.setOnPreferenceClickListener(preference -> {
                showRealityCheckDialog();
                return true;
            });
        }

        Preference tagList = findPreference("tag_list");
        tagList.setOnPreferenceClickListener(preference -> {
            openTagList();
            return true;
        });

        SwitchPreferenceCompat passwordSwitch = findPreference("set_password");
        passwordSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (boolean) newValue;

            if (enabled) {
                showPasswordDialog(passwordSwitch);
                return false; // откладываем включение до подтверждения
            } else {
                prefs.edit().remove("password_hash").apply();
                Toast.makeText(requireContext(), "Пароль удален", Toast.LENGTH_SHORT).show();
            }

            return true;
        });

        ListPreference themePref = findPreference("theme_preference");
        themePref.setOnPreferenceChangeListener((preference, newValue) -> {
            requireActivity().recreate();
            return true;
        });

        Preference about = findPreference("about");
        about.setOnPreferenceClickListener(preference -> {
            showAboutDialog();
            return true;
        });

        updateReminderTimeSummary();

    }

    private void openTagList() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.navigation_tag_list);
    }

    private boolean notificationsEnabled() {

        Context context = requireContext();
        Activity activity = requireActivity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                boolean wasRequested = prefs.getBoolean("notifications_requested", false);

                // Пользователь запретил уведомления или еще не дал разрешение
                if (!wasRequested || !ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.POST_NOTIFICATIONS)) {
                    prefs.edit().putBoolean("notifications_requested", true).apply();
                    new AlertDialog.Builder(context)
                            .setTitle("Разрешение на уведомления")
                            .setMessage("Разрешите приложению отправлять уведомления, чтобы получать напоминания.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                } else {
                    // Пользователь выбрал "Больше не спрашивать"
                    new AlertDialog.Builder(context)
                            .setTitle("Уведомления запрещены")
                            .setMessage("Вы запретили уведомления. Вы можете изменить это в настройках приложения.")
                            .setPositiveButton("Открыть настройки", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                                startActivity(intent);
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                }
                return false;
            }
        }

        // Уведомления разрешены — теперь проверим точные срабатывания
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(context)
                        .setTitle("Уведомления по расписанию")
                        .setMessage("Для работы напоминаний необходимо разрешить запуск действий в определенное время. Открыть настройки?")
                        .setPositiveButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
                return false;
            }
        }

        return true;
    }

    private void showTimePicker() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String timeString = prefs.getString("reminder_time", "08:00");
        String[] parts = timeString.split(":");
        int setHour = Integer.parseInt(parts[0]);
        int setMinute = Integer.parseInt(parts[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            prefs.edit().putString("reminder_time", newTime).apply();

            Preference timePreference = findPreference("reminder_time");
            if (timePreference != null) {
                timePreference.setSummary(newTime);
            }

            scheduleReminder(requireContext());

        }, setHour, setMinute, true);

        timePickerDialog.setOnCancelListener(dialog -> {

            if (prefs.getString("reminder_time", "-1").equals("-1")) {
                Toast.makeText(requireContext(), "Вы не установили время", Toast.LENGTH_SHORT).show();

                SwitchPreferenceCompat reminderSwitch = findPreference("reminder_enabled");
                reminderSwitch.setChecked(false);
            }
        });

        timePickerDialog.show();
    }

    private void showRealityCheckDialog() {
        // Открытие кастомного диалога
    }

    private void showPasswordDialog(SwitchPreferenceCompat passwordSwitch) {
        Context context = requireContext();
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Введите 6-значный PIN");
        input.setMaxLines(1);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        new AlertDialog.Builder(context)
                .setTitle("Установка пароля")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String pin = input.getText().toString();
                    if (pin.length() == 6 && pin.matches("\\d{6}")) {

                        String hashed = PasswordActivity.hashPin(pin);

                        PreferenceManager.getDefaultSharedPreferences(context).edit()
                                .putString("password_hash", hashed)
                                .putBoolean("pref_password_enabled", true)
                                .apply();

                        passwordSwitch.setChecked(true);

                        Toast.makeText(context, "Пароль установлен", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Введите ровно 6 цифр", Toast.LENGTH_SHORT).show();
                        passwordSwitch.setChecked(false);
                    }
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    dialog.cancel();
                    passwordSwitch.setChecked(false);
                })
                .setCancelable(false)
                .show();
    }

    private void showAboutDialog() {
        TextView textView = new TextView(requireContext());
        textView.setText("Версия приложения: v1.0\n\nАвтор: Анастасия Балабкина\n\nПочта для обратной связи: \nanastasia.balabckina@gmail.com");
        textView.setPadding(72, 32, 72, 32); // отступы для красоты
        textView.setTextSize(16);
        textView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        new AlertDialog.Builder(requireContext())
                .setTitle("О приложении")
                .setView(textView)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void scheduleReminder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String time = prefs.getString("reminder_time", "09:00");
        String[] splittedTime = time.split(":");
        int hour = Integer.parseInt(splittedTime[0]);
        int minute = Integer.parseInt(splittedTime[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1); // завтра
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Приложению не разрешено устанавливать точные напоминания", Toast.LENGTH_LONG).show();
                return;
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        Log.d("NOTIFICATION", "Будильник установлен на: " + calendar.getTime().toString());
    }

    public static void cancelReminder(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("NOTIFICATION", "Будильник отменён");
        }
    }

    private void updateReminderTimeSummary() {
        Preference timePreference = findPreference("reminder_time");
        if (timePreference != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String timeString = prefs.getString("reminder_time", "null");

            if (!timeString.equals("null")) {
                timePreference.setSummary(timeString);
            } else {
                timePreference.setSummary("Выберите время");
            }
        }
    }

}