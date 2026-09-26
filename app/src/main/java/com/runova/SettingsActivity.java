package com.runova;

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
    private static final String KEY_MEAL = "reminder_meal";
    private static final String KEY_WATER = "reminder_water";
    private static final String KEY_WORKOUT = "reminder_workout";
    private static final String KEY_SLEEP = "alarm_sleep";
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

        setupSwitch((SwitchMaterial) findViewById(R.id.mealSwitch), KEY_MEAL, true);
        setupSwitch((SwitchMaterial) findViewById(R.id.waterSwitch), KEY_WATER, true);
        setupSwitch((SwitchMaterial) findViewById(R.id.workoutSwitch), KEY_WORKOUT, true);
        setupSwitch((SwitchMaterial) findViewById(R.id.sleepSwitch), KEY_SLEEP, false);
        setupDarkMode((SwitchMaterial) findViewById(R.id.darkModeSwitch));
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
