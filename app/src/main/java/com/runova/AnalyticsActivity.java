package com.runova;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;
import com.runova.helpers.WindowHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private TextView tvLevelProgress, tvStreak, tvFinishedCount, tvMissedCount, tvPendingCount;
    private ProgressBar progressLevel;
    private BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_analytics);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));
        WindowHelper.applyBottomInset(findViewById(R.id.navigationBar));

        dbHelper = new DBHelper(this);

        tvLevelProgress = findViewById(R.id.tvLevelProgress);
        tvStreak = findViewById(R.id.tvStreak);
        tvFinishedCount = findViewById(R.id.tvFinishedCount);
        tvMissedCount = findViewById(R.id.tvMissedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        progressLevel = findViewById(R.id.progressLevel);
        chart = findViewById(R.id.chart);

        loadLevelProgress();
        loadStreak();
        loadStatusCounts();
        loadWeeklyChart();
        setupNavigation();
    }

    private void loadLevelProgress() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE, 
            new String[]{DBHelper.PROFILE_LEVEL_START},
            DBHelper.PROFILE_ID + " = 1", null, null, null, null);
        
        if (cursor.moveToFirst()) {
            String levelStart = cursor.getString(0);
            long days = DateUtils.daysBetween(levelStart, DateUtils.today()) + 1;
            
            Cursor taskCursor = db.rawQuery(
                "SELECT COUNT(DISTINCT date) FROM " + DBHelper.TABLE_TASKS + 
                " WHERE date >= ? AND date <= ? AND status = 'Finished' " +
                "AND date IN (SELECT date FROM " + DBHelper.TABLE_TASKS + 
                " GROUP BY date HAVING COUNT(*) = SUM(CASE WHEN status = 'Finished' THEN 1 ELSE 0 END))",
                new String[]{levelStart, DateUtils.today()}
            );
            
            int finishedDays = 0;
            if (taskCursor.moveToFirst()) {
                finishedDays = taskCursor.getInt(0);
            }
            taskCursor.close();
            
            tvLevelProgress.setText(finishedDays + " / 28 days");
            progressLevel.setProgress(finishedDays);
            
            if (finishedDays >= 24) {
                progressLevel.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(android.R.color.holo_green_light, null)));
            }
        }
        cursor.close();
    }

    private void loadStreak() {
        tvStreak.setText(new com.runova.helpers.StatsHelper(dbHelper).streak() + " days");
    }

    private void loadStatusCounts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE,
            new String[]{DBHelper.PROFILE_LEVEL_START},
            DBHelper.PROFILE_ID + " = 1", null, null, null, null);
        
        String levelStart = "";
        if (cursor.moveToFirst()) {
            levelStart = cursor.getString(0);
        }
        cursor.close();
        
        Cursor statsCursor = db.rawQuery(
            "SELECT status, COUNT(*) FROM " + DBHelper.TABLE_TASKS + 
            " WHERE date >= ? GROUP BY status",
            new String[]{levelStart}
        );
        
        int finished = 0, missed = 0, pending = 0;
        
        while (statsCursor.moveToNext()) {
            String status = statsCursor.getString(0);
            int count = statsCursor.getInt(1);
            
            if (status.equals("Finished")) finished = count;
            else if (status.equals("Missed")) missed = count;
            else if (status.equals("Pending")) pending = count;
        }
        statsCursor.close();
        
        tvFinishedCount.setText(String.valueOf(finished));
        tvMissedCount.setText(String.valueOf(missed));
        tvPendingCount.setText(String.valueOf(pending));
    }

    private void loadWeeklyChart() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        List<BarEntry> entries = new ArrayList<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        
        for (int i = 0; i < 7; i++) {
            String date = sdf.format(cal.getTime());
            
            Cursor cursor = db.query(DBHelper.TABLE_TASKS,
                new String[]{"COUNT(*)"},
                DBHelper.TASK_DATE + " = ? AND " + DBHelper.TASK_STATUS + " = 'Finished'",
                new String[]{date}, null, null, null);
            
            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            
            entries.add(new BarEntry(i, count));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Tasks Finished");
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < days.length ? days[index] : "";
            }
        });
        
        chart.invalidate();
    }

    private void setupNavigation() {
        findViewById(R.id.settingsb).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.homeb).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.targetb).setOnClickListener(v -> startActivity(new Intent(this, TargetActivity.class)));
        findViewById(R.id.analyticsb).setOnClickListener(v -> {});
        findViewById(R.id.profileb).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }
}
