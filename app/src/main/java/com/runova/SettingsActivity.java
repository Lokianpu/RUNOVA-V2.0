package com.runova;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.runova.helpers.WindowHelper;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS = "settings";
    private static final String KEY_REMINDERS = "reminders_enabled";
    private static final String KEY_DARK = "appearance_dark";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_settings);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));
        WindowHelper.applyBottomInset(findViewById(R.id.navigationBar));

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupSwitch((SwitchMaterial) findViewById(R.id.remindersSwitch), KEY_REMINDERS, true);
        setupDarkMode((SwitchMaterial) findViewById(R.id.darkModeSwitch));
        setupNavigation();
    }

    private void setupNavigation() {
        findViewById(R.id.homeb).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.targetb).setOnClickListener(v -> {
            startActivity(new Intent(this, TargetActivity.class));
            finish();
        });
        findViewById(R.id.analyticsb).setOnClickListener(v -> {
            startActivity(new Intent(this, AnalyticsActivity.class));
            finish();
        });
        findViewById(R.id.profileb).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void setupSwitch(SwitchMaterial toggle, String key, boolean def) {
        toggle.setChecked(prefs.getBoolean(key, def));
        toggle.setOnCheckedChangeListener((button, isChecked) ->
            prefs.edit().putBoolean(key, isChecked).apply());
    }

    private void setupDarkMode(SwitchMaterial toggle) {
        int nightMask = getResources().getConfiguration().uiMode
            & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNight = nightMask == Configuration.UI_MODE_NIGHT_YES;
        toggle.setChecked(prefs.getBoolean(KEY_DARK, isNight));
        toggle.setOnCheckedChangeListener((button, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                          : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }
}
