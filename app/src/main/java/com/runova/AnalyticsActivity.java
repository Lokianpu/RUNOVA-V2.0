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
    private TextView tvChartTitle, tvChartCaption, tabWeek, tabMonth, tabYear;
    private String range = "Week";
    private String[] xLabels = new String[0];

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

        tvChartTitle = findViewById(R.id.tvChartTitle);
        tvChartCaption = findViewById(R.id.tvChartCaption);
        tabWeek = findViewById(R.id.tabWeek);
        tabMonth = findViewById(R.id.tabMonth);
        tabYear = findViewById(R.id.tabYear);
        tabWeek.setOnClickListener(v -> setRange("Week"));
        tabMonth.setOnClickListener(v -> setRange("Month"));
        tabYear.setOnClickListener(v -> setRange("Year"));
        setRange("Week");

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

    private void setRange(String newRange) {
        range = newRange;
        tabWeek.setBackgroundResource("Week".equals(range) ? R.drawable.bg_chip : 0);
        tabMonth.setBackgroundResource("Month".equals(range) ? R.drawable.bg_chip : 0);
        tabYear.setBackgroundResource("Year".equals(range) ? R.drawable.bg_chip : 0);
        tabWeek.setTextColor(getColor("Week".equals(range) ? R.color.button_primary : R.color.white));
        tabMonth.setTextColor(getColor("Month".equals(range) ? R.color.button_primary : R.color.white));
        tabYear.setTextColor(getColor("Year".equals(range) ? R.color.button_primary : R.color.white));
        tvChartTitle.setText("This " + range);
        tvChartCaption.setText(range + " shows all activity, even across level changes");
        loadChart();
    }

    private void loadChart() {
        List<BarEntry> entries;
        boolean percent;
        if ("Month".equals(range)) {
            entries = monthEntries();
            percent = false;
        } else if ("Year".equals(range)) {
            entries = yearEntries();
            percent = true;
        } else {
            entries = weekEntries();
            percent = false;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Completed Targets");
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        if (percent) {
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return Math.round(value) + "%";
                }
            });
        }

        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getLegend().setTextSize(12f);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setTextSize(12f);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < xLabels.length ? xLabels[index] : "";
            }
        });

        chart.invalidate();
    }

    private List<BarEntry> weekEntries() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int toMonday = dow == Calendar.SUNDAY ? 6 : dow - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -toMonday);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        List<BarEntry> entries = new ArrayList<>();
        xLabels = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

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
        return entries;
    }

    private List<BarEntry> monthEntries() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String start = sdf.format(cal.getTime());
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        String end = sdf.format(cal.getTime());

        int chunkCount = (daysInMonth + 6) / 7;
        int[] chunkFinished = new int[chunkCount];

        Cursor cursor = db.rawQuery(
            "SELECT " + DBHelper.TASK_DATE + ", SUM(CASE WHEN " + DBHelper.TASK_STATUS
                + " = 'Finished' THEN 1 ELSE 0 END) FROM " + DBHelper.TABLE_TASKS
                + " WHERE " + DBHelper.TASK_DATE + " BETWEEN ? AND ? GROUP BY " + DBHelper.TASK_DATE,
            new String[]{start, end});

        while (cursor.moveToNext()) {
            int dayOfMonth = Integer.parseInt(cursor.getString(0).substring(8, 10));
            int chunk = Math.min((dayOfMonth - 1) / 7, chunkCount - 1);
            chunkFinished[chunk] += cursor.getInt(1);
        }
        cursor.close();

        List<BarEntry> entries = new ArrayList<>();
        xLabels = new String[chunkCount];
        for (int i = 0; i < chunkCount; i++) {
            entries.add(new BarEntry(i, chunkFinished[i]));
            xLabels[i] = "W" + (i + 1);
        }
        return entries;
    }

    private List<BarEntry> yearEntries() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String start = sdf.format(cal.getTime());
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        String end = sdf.format(cal.getTime());

        int[] fullDays = new int[12];
        int[] daysWithTasks = new int[12];

        Cursor cursor = db.rawQuery(
            "SELECT " + DBHelper.TASK_DATE + ", COUNT(*), SUM(CASE WHEN " + DBHelper.TASK_STATUS
                + " = 'Finished' THEN 1 ELSE 0 END) FROM " + DBHelper.TABLE_TASKS
                + " WHERE " + DBHelper.TASK_DATE + " BETWEEN ? AND ? GROUP BY " + DBHelper.TASK_DATE,
            new String[]{start, end});

        while (cursor.moveToNext()) {
            int month = Integer.parseInt(cursor.getString(0).substring(5, 7)) - 1;
            int total = cursor.getInt(1);
            int finished = cursor.getInt(2);
            if (total == finished) {
                fullDays[month]++;
            }
            daysWithTasks[month]++;
        }
        cursor.close();

        List<BarEntry> entries = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        xLabels = months;
        for (int m = 0; m < 12; m++) {
            float pct = daysWithTasks[m] == 0 ? 0f
                : Math.round(100f * fullDays[m] / daysWithTasks[m]);
            entries.add(new BarEntry(m, pct));
        }
        return entries;
    }

    private void setupNavigation() {
        findViewById(R.id.btnNotifications).setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.settingsb).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.homeb).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.targetb).setOnClickListener(v -> startActivity(new Intent(this, TargetActivity.class)));
        findViewById(R.id.analyticsb).setOnClickListener(v -> {});
        findViewById(R.id.profileb).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }
}
