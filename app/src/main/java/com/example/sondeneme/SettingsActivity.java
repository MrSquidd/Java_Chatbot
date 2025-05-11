package com.example.sondeneme;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // SharedPreferences'ı oku
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        // Geçerli temayı uygula (önce)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchTheme = findViewById(R.id.switchTheme);
        switchTheme.setChecked(isDark);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            // Tema geçişini uygula
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            recreate(); // ayarlar ekranını yeniden oluştur
        });
    }
}
