package com.runova;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.runova.helpers.WindowHelper;

import java.util.Locale;

public class TimerActivity extends AppCompatActivity {
    public static final String EXTRA_NAME = "TASK_NAME";
    public static final String EXTRA_SECONDS = "DURATION_SECONDS";
    public static final String EXTRA_COMPLETED = "TIMER_COMPLETED";

    private TextView tvTimerDisplay, tvTimerState;
    private Button btnPrimary;
    private int totalSeconds;
    private long remainingMs;
    private CountDownTimer timer;
    private boolean running;
    private boolean ready = true;
    private boolean completed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_timer);

        String name = getIntent().getStringExtra(EXTRA_NAME);
        totalSeconds = getIntent().getIntExtra(EXTRA_SECONDS, 0);
        if (name == null || totalSeconds <= 0) {
            finish();
            return;
        }

        TextView tvTaskName = findViewById(R.id.tvTaskName);
        TextView tvDurationLabel = findViewById(R.id.tvDurationLabel);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        tvTimerState = findViewById(R.id.tvTimerState);
        btnPrimary = findViewById(R.id.btnPrimary);
        findViewById(R.id.btnBack).setOnClickListener(v -> showExitConfirmation());

        tvTaskName.setText(name);
        tvDurationLabel.setText("Duration: " + totalSeconds + " seconds");
        remainingMs = totalSeconds * 1000L;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        btnPrimary.setOnClickListener(v -> onPrimary());
        showReady();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmation();
            }
        });
    }

    // Single confirmation used by both the BACK TO TARGET button and the
    // system back gesture. A running timer pauses while the dialog is open
    // and resumes on CANCEL; EXIT performs the normal exit (goBack).
    private void showExitConfirmation() {
        if (completed) {
            goBack();
            return;
        }
        final boolean wasRunning = running;
        if (wasRunning) {
            pauseCountdown();
        }
        new AlertDialog.Builder(this)
                .setTitle("Exit Training?")
                .setMessage("Are you sure you want to exit? Your current training progress may be lost.")
                .setNegativeButton("CANCEL", (dialog, which) -> {
                    if (wasRunning) resumeCountdown();
                })
                .setPositiveButton("EXIT", (dialog, which) -> goBack())
                .setOnCancelListener(dialog -> {
                    if (wasRunning) resumeCountdown();
                })
                .show();
    }

    private void onPrimary() {
        if (completed) {
            goBack();
            return;
        }
        if (ready) {
            startCountdown();
        } else if (running) {
            pauseCountdown();
        } else {
            resumeCountdown();
        }
    }

    private void startCountdown() {
        remainingMs = totalSeconds * 1000L;
        startTimer();
        setRunningState("Running", "PAUSE", R.color.status_missed_icon);
    }

    private void pauseCountdown() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        ready = false;
        btnPrimary.setText("RESUME");
        btnPrimary.setBackgroundTintList(getResources().getColorStateList(R.color.status_success, null));
        tvTimerState.setText("Paused");
    }

    private void resumeCountdown() {
        startTimer();
        setRunningState("Running", "PAUSE", R.color.status_missed_icon);
    }

    private void startTimer() {
        running = true;
        ready = false;
        timer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                setDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timer = null;
                running = false;
                completed = true;
                remainingMs = 0;
                setDisplay(0);
                tvTimerState.setText("Completed");
                btnPrimary.setText("DONE");
                btnPrimary.setBackgroundTintList(getResources().getColorStateList(R.color.status_success, null));
                btnPrimary.setEnabled(true);
                setResult(Activity.RESULT_OK,
                    new Intent().putExtra(EXTRA_COMPLETED, true));
            }
        }.start();
    }

    private void setRunningState(String state, String buttonText, int colorRes) {
        running = true;
        ready = false;
        tvTimerState.setText(state);
        btnPrimary.setText(buttonText);
        btnPrimary.setBackgroundTintList(getResources().getColorStateList(colorRes, null));
    }

    private void showReady() {
        ready = true;
        running = false;
        btnPrimary.setText("START");
        btnPrimary.setBackgroundTintList(getResources().getColorStateList(R.color.button_primary, null));
        tvTimerState.setText("Ready to start");
        setDisplay(totalSeconds * 1000L);
    }

    private void setDisplay(long millis) {
        int totalSec = (int) (millis / 1000);
        tvTimerDisplay.setText(String.format(Locale.US, "%02d:%02d", totalSec / 60, totalSec % 60));
    }

    private void goBack() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!completed) {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
