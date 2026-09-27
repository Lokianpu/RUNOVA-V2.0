package com.runova;

import android.app.AlertDialog;
import android.content.ContentValues;
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
    private TextView tvTimerDisplay, tvTimerState;
    private Button btnPrimary;
    private int taskId, minutes;
    private boolean running;
    private boolean ready = true;

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
        tvTimerState = findViewById(R.id.tvTimerState);
        btnPrimary = findViewById(R.id.btnPrimary);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnDone = findViewById(R.id.btnDone);
        findViewById(R.id.btnBack).setOnClickListener(v -> goBack());

        tvTaskName.setText(taskName);
        tvDurationLabel.setText("Duration: " + minutes + " minutes");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnPrimary.setOnClickListener(v -> onPrimary());
        btnReset.setOnClickListener(v -> confirmReset());
        btnDone.setOnClickListener(v -> confirmDone());

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
            showReady();
        } else if (state.paused) {
            showPaused(state.remainingMs);
        } else {
            timerController.resumeTimer(callback());
            showRunning();
            setDisplay(state.remainingMs);
        }
    }

    private void onPrimary() {
        if (ready) {
            startTimer();
        } else if (running) {
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

    private void showReady() {
        ready = true;
        running = false;
        btnPrimary.setText("START");
        btnPrimary.setBackgroundTintList(getResources().getColorStateList(R.color.button_primary, null));
        tvTimerState.setText("Ready to start");
        setDisplay(minutes * 60 * 1000L);
    }

    private void showRunning() {
        ready = false;
        running = true;
        btnPrimary.setText("PAUSE");
        btnPrimary.setBackgroundTintList(getResources().getColorStateList(R.color.status_missed_icon, null));
        tvTimerState.setText("Running");
    }

    private void showPaused(long remaining) {
        ready = false;
        running = false;
        btnPrimary.setText("RESUME");
        btnPrimary.setBackgroundTintList(getResources().getColorStateList(R.color.status_success, null));
        tvTimerState.setText("Paused");
        setDisplay(remaining);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
            .setTitle("Reset timer")
            .setMessage("The timer will start again from the beginning.")
            .setPositiveButton("RESET", (dialog, which) -> {
                timerController.clearTimer();
                showReady();
            })
            .setNegativeButton("CANCEL", null)
            .show();
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
            .setPositiveButton("COMPLETE", (dialog, which) -> completeTask(false))
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
