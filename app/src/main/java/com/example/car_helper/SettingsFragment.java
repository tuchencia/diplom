package com.example.car_helper;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Загружаем настройки из XML-файла
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Обработка изменения уведомлений
        Preference notificationsPreference = findPreference("notifications");
        if (notificationsPreference != null) {
            notificationsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isNotificationsEnabled = (boolean) newValue;
                handleNotifications(isNotificationsEnabled); // Включить/отключить уведомления
                return true;
            });
        }

        // Обработка изменения звука
        Preference soundPreference = findPreference("sound");
        if (soundPreference != null) {
            soundPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isSoundEnabled = (boolean) newValue;
                handleSound(isSoundEnabled); // Включить/отключить звук
                return true;
            });
        }

        // Обработка изменения темы
        Preference themePreference = findPreference("theme");
        if (themePreference != null) {
            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String theme = (String) newValue;
                applyTheme(theme); // Применить тему
                return true;
            });
        }
    }

    private void showMessage(String message) {
        // Показываем сообщение пользователю с помощью Toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void handleNotifications(boolean isEnabled) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) requireContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager.isNotificationPolicyAccessGranted()) {
                if (isEnabled) {
                    // Включить уведомления
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                } else {
                    // Отключить уведомления
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                }
            } else {
                // Разрешение не предоставлено, показываем сообщение пользователю
                showNotificationPermissionDialog();
            }
        }

        // Сохраняем состояние уведомлений в SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.edit().putBoolean("notifications", isEnabled).apply();
    }

    private void handleSound(boolean isEnabled) {
        // Сохраняем состояние звука в SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.edit().putBoolean("sound", isEnabled).apply();

        // Применяем настройки звука в приложении
        applySoundSettings(isEnabled);
    }

    private void applySoundSettings(boolean isSoundEnabled) {

        if (isSoundEnabled) {

            showMessage("Звук включен");
        } else {

            showMessage("Звук выключен");
        }
    }

//    private void enableSound() {
//        // Логика для включения звука
//        // Например, включение звука уведомлений
//        NotificationManager notificationManager = (NotificationManager) requireContext()
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
//        }
//    }

//    private void disableSound() {
//        // Логика для отключения звука
//        // Например, отключение звука уведомлений
//        NotificationManager notificationManager = (NotificationManager) requireContext()
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
//        }
//    }

    private void applyTheme(String theme) {
        // Сохраняем выбранную тему в SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        sharedPreferences.edit().putString("theme", theme).apply();

        // Перезапускаем активность для применения новой темы
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    private void showNotificationPermissionDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Доступ к настройкам уведомлений")
                .setMessage("Для управления уведомлениями необходимо предоставить доступ к настройкам уведомлений.")
                .setPositiveButton("Открыть настройки", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}