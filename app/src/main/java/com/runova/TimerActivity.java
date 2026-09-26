package com.runova;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.runova.controllers.TimerController;
import com.runova.database.DBHelper;
import com.runova.helpers.WindowHelper;

import java.util.Locale;

public class TimerActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private TimerController timerController;
    private TextView tvTimerDisplay;
    private Button btnPrimary, btnResume;
    private int taskId, minutes;
    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_timer);

        taskId = getIntent().getIntExtra("TASK_ID", -1);
        String taskName = getIntent().getStringExtra("TASK_NAME");
        minutes = getIntent().getIntExtra("DURATION_MINUTES", 0);
        if (taskId == -1 || minutes <= 0) {
            finish();
            return;
        }

        dbHelper = new DBHelper(this);
        timerController = new TimerController(dbHelper);
        timerController.stopTimer();

        TextView tvTaskName = findViewById(R.id.tvTaskName);
        TextView tvDurationLabel = findViewById(R.id.tvDurationLabel);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnResume = findViewById(R.id.btnResume);
        Button btnDone = findViewById(R.id.btnDone);
        Button btnBack = findViewById(R.id.btnBack);

        tvTaskName.setText(taskName);
        tvDurationLabel.setText("Duration: " + minutes + " minutes");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnPrimary.setOnClickListener(v -> onPrimary());
        btnResume.setOnClickListener(v -> onPrimary());
        btnDone.setOnClickListener(v -> confirmDone());
        btnBack.setOnClickListener(v -> goBack());

        restoreState();
    }

    private void restoreState() {
        TimerController.ActiveState state = timerController.readState();

        if (state != null && state.taskId != taskId) {
            Toast.makeText(this, "Finish current task first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (state == null) {
            startTimer();
        } else if (state.paused) {
            showPaused(state.remainingMs);
        } else {
            timerController.resumeTimer(callback());
            showRunning();
            setDisplay(state.remainingMs);
        }
    }

    private void onPrimary() {
        if (running) {
            timerController.pauseTimer();
            TimerController.ActiveState state = timerController.readState();
            showPaused(state != null ? state.remainingMs : 0);
        } else {
            timerController.resumeTimer(callback());
            showRunning();
        }
    }

    private void startTimer() {
        timerController.startTimer(taskId, minutes, callback());
        showRunning();
        setDisplay(minutes * 60 * 1000L);
    }

    private void showRunning() {
        running = true;
        btnPrimary.setVisibility(android.view.View.VISIBLE);
        btnPrimary.setText("PAUSE");
        btnResume.setVisibility(android.view.View.GONE);
    }

    private void showPaused(long remaining) {
        running = false;
        btnPrimary.setVisibility(android.view.View.GONE);
        btnResume.setVisibility(android.view.View.VISIBLE);
        setDisplay(remaining);
    }

    private void setDisplay(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int minutesPart = totalSeconds / 60;
        int secondsPart = totalSeconds % 60;
        tvTimerDisplay.setText(String.format(Locale.US, "%02d:%02d", minutesPart, secondsPart));
    }

    private TimerController.TimerCallback callback() {
        return new TimerController.TimerCallback() {
            @Override
            public void onTick(long millisRemaining) {
                setDisplay(millisRemaining);
            }

            @Override
            public void onFinish() {
                setDisplay(0);
                completeTask(true);
            }
        };
    }

    private void confirmDone() {
        new AlertDialog.Builder(this)
            .setTitle("Complete task")
            .setMessage("Are you sure you want to mark this activity as complete?")
            .setPositiveButton("DONE", (dialog, which) -> completeTask(false))
            .setNegativeButton("CANCEL", null)
            .show();
    }

    private void completeTask(boolean finishedByTimer) {
        timerController.clearTimer();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_STATUS, "Finished");
        db.update(DBHelper.TABLE_TASKS, values, DBHelper.TASK_ID + " = ?",
            new String[]{String.valueOf(taskId)});

        if (!finishedByTimer) {
            Toast.makeText(this, "Task completed!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void goBack() {
        TimerController.ActiveState state = timerController.readState();
        if (state != null && !state.paused) {
            timerController.pauseTimer();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerController != null) {
            timerController.stopTimer();
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
