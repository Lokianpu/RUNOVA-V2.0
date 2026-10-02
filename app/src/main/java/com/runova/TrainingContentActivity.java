package com.runova;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.runova.database.DBHelper;
import com.runova.helpers.WindowHelper;
import com.runova.models.Exercise;
import com.runova.training.TrainingContentLibrary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrainingContentActivity extends AppCompatActivity {
    private static final String PREFS = "exercise_progress";

    private DBHelper dbHelper;
    private LinearLayout exerciseContainer;
    private int taskId;
    private int minutes;
    private int totalExercises;
    private int pendingIndex = -1;
    private final Set<Integer> doneIndices = new HashSet<>();
    private List<View> cards = new ArrayList<>();
    private ActivityResultLauncher<Intent> timerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_training_content);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));

        String taskName = getIntent().getStringExtra("TASK_NAME");
        String taskType = getIntent().getStringExtra("TASK_TYPE");
        taskId = getIntent().getIntExtra("TASK_ID", -1);
        minutes = getIntent().getIntExtra("DURATION_MINUTES", 0);
        if (taskType == null || taskName == null || taskId == -1 || minutes <= 0) {
            finish();
            return;
        }

        dbHelper = new DBHelper(this);
        exerciseContainer = findViewById(R.id.exerciseContainer);

        TextView tvScreenTitle = findViewById(R.id.tvScreenTitle);
        TextView tvScreenSubtitle = findViewById(R.id.tvScreenSubtitle);
        TextView tvInstruction = findViewById(R.id.tvInstruction);
        tvScreenTitle.setText(taskName);
        tvScreenSubtitle.setText(minutes + " minutes");
        tvInstruction.setText(TrainingContentLibrary.getInstruction(taskType, readEvent()));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        timerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (pendingIndex < 0) return;
                boolean completed = result.getResultCode() == RESULT_OK
                    && result.getData() != null
                    && result.getData().getBooleanExtra(TimerActivity.EXTRA_COMPLETED, false);
                if (completed) {
                    markDone(pendingIndex);
                }
                pendingIndex = -1;
            });

        if (isTaskFinished()) {
            populateAllDone(taskType);
        } else {
            loadProgress();
            populateExercises(taskType);
        }
    }

    private String readEvent() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DBHelper.TABLE_PROFILE, new String[]{DBHelper.PROFILE_EVENT}, null,
                null, null, null, null, "1")) {
            if (c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {
        }
        return "General Running";
    }

    private boolean isTaskFinished() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DBHelper.TABLE_TASKS, new String[]{DBHelper.TASK_STATUS},
                DBHelper.TASK_ID + " = ?", new String[]{String.valueOf(taskId)}, null, null, null)) {
            if (c.moveToFirst()) return "Finished".equals(c.getString(0));
        } catch (Exception ignored) {
        }
        return false;
    }

    private String prefKey() {
        return "task_" + taskId;
    }

    private void loadProgress() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(prefKey(), "");
        if (raw.isEmpty()) return;
        try {
            for (String part : raw.split(",")) {
                doneIndices.add(Integer.parseInt(part.trim()));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void saveProgress() {
        StringBuilder sb = new StringBuilder();
        for (int index : doneIndices) {
            if (sb.length() > 0) sb.append(',');
            sb.append(index);
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(prefKey(), sb.toString()).apply();
    }

    private void clearProgress() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(prefKey()).apply();
    }

    private void populateAllDone(String taskType) {
        loadProgress();
        if (doneIndices.isEmpty()) {
            List<Exercise> list = TrainingContentLibrary.getExercises(taskType);
            for (int i = 0; i < list.size(); i++) doneIndices.add(i);
        }
        populateExercises(taskType);
    }

    private void populateExercises(String taskType) {
        List<Exercise> exercises = TrainingContentLibrary.getExercises(taskType);
        totalExercises = exercises.size();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < exercises.size(); i++) {
            final int index = i;
            Exercise exercise = exercises.get(i);
            int seconds = exercise.seconds > 0 ? exercise.seconds : minutes * 60;

            View card = inflater.inflate(R.layout.item_exercise_card, exerciseContainer, false);
            TextView tvName = card.findViewById(R.id.tvExerciseName);
            TextView tvDescription = card.findViewById(R.id.tvExerciseDescription);
            TextView tvDuration = card.findViewById(R.id.tvExerciseDuration);
            TextView tvStatus = card.findViewById(R.id.tvExerciseStatus);
            Button btnStart = card.findViewById(R.id.btnExerciseStart);

            tvName.setText(exercise.name);
            tvDescription.setText(exercise.description);
            tvDuration.setText(seconds + " seconds");

            final int cardSeconds = seconds;
            btnStart.setOnClickListener(v -> {
                pendingIndex = index;
                Intent intent = new Intent(TrainingContentActivity.this, TimerActivity.class);
                intent.putExtra(TimerActivity.EXTRA_NAME, exercise.name);
                intent.putExtra(TimerActivity.EXTRA_SECONDS, cardSeconds);
                timerLauncher.launch(intent);
            });

            if (doneIndices.contains(index)) {
                tvStatus.setVisibility(View.VISIBLE);
                btnStart.setText("DONE");
                btnStart.setEnabled(false);
            }

            cards.add(card);
            exerciseContainer.addView(card);
        }
    }

    private void markDone(int index) {
        if (!doneIndices.add(index) || index < 0 || index >= cards.size()) {
            return;
        }
        View card = cards.get(index);
        card.findViewById(R.id.tvExerciseStatus).setVisibility(View.VISIBLE);
        Button btnStart = card.findViewById(R.id.btnExerciseStart);
        btnStart.setText("DONE");
        btnStart.setEnabled(false);
        saveProgress();

        if (doneIndices.size() >= totalExercises) {
            finishTask();
        }
    }

    private void finishTask() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_STATUS, "Finished");
        db.update(DBHelper.TABLE_TASKS, values, DBHelper.TASK_ID + " = ?",
            new String[]{String.valueOf(taskId)});
        clearProgress();
        Toast.makeText(this, "Task completed!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
