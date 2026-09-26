package com.runova;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.runova.controllers.TaskGenerator;
import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;

public class LoadingActivity extends AppCompatActivity {
    private static final long MIN_DISPLAY_MS = 1500;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        dbHelper = new DBHelper(this);
        long start = System.currentTimeMillis();

        new Thread(() -> {
            new TaskGenerator(dbHelper).generateDailyTasks(DateUtils.today());
            long elapsed = System.currentTimeMillis() - start;
            long delay = Math.max(0, MIN_DISPLAY_MS - elapsed);
            new Handler(Looper.getMainLooper()).postDelayed(this::goHome, delay);
        }).start();
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
