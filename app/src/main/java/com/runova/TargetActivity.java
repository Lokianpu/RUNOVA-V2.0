package com.runova;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.runova.controllers.TaskGenerator;
import com.runova.controllers.TimerController;
import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;
import com.runova.helpers.WindowHelper;
import com.runova.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TargetActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private TaskGenerator taskGenerator;
    private TimerController timerController;
    private RecyclerView rvPendingTasks, rvCompletedTasks;
    private TextView tvLevelChip, tvTargetsSummary, tvPendingEmpty, tvCompletedEmpty;
    private TextView badgePendingCount, badgeCompletedCount;
    private ImageView arrowPending, arrowCompleted;
    private Integer activeTaskId;
    private boolean pendingExpanded = true;
    private boolean completedExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_target);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));
        WindowHelper.applyBottomInset(findViewById(R.id.navigationBar));

        dbHelper = new DBHelper(this);
        taskGenerator = new TaskGenerator(dbHelper);
        timerController = new TimerController(dbHelper);

        rvPendingTasks = findViewById(R.id.rvPendingTasks);
        rvCompletedTasks = findViewById(R.id.rvCompletedTasks);
        tvLevelChip = findViewById(R.id.tvLevelChip);
        tvTargetsSummary = findViewById(R.id.tvTargetsSummary);
        tvPendingEmpty = findViewById(R.id.tvPendingEmpty);
        tvCompletedEmpty = findViewById(R.id.tvCompletedEmpty);
        badgePendingCount = findViewById(R.id.badgePendingCount);
        badgeCompletedCount = findViewById(R.id.badgeCompletedCount);
        arrowPending = findViewById(R.id.arrowPending);
        arrowCompleted = findViewById(R.id.arrowCompleted);

        rvPendingTasks.setLayoutManager(new LinearLayoutManager(this));
        rvCompletedTasks.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.headerPending).setOnClickListener(v -> {
            pendingExpanded = !pendingExpanded;
            setupTasks();
        });
        findViewById(R.id.headerCompleted).setOnClickListener(v -> {
            completedExpanded = !completedExpanded;
            setupTasks();
        });

        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkActiveTimer();
        setupTasks();
    }

    private void checkActiveTimer() {
        TimerController.ActiveState state = timerController.readState();
        activeTaskId = state != null ? state.taskId : null;
    }

    private void setupTasks() {
        String today = DateUtils.today();
        List<Task> tasks = taskGenerator.generateDailyTasks(today);

        List<Task> pending = new ArrayList<>();
        List<Task> completed = new ArrayList<>();
        for (Task task : tasks) {
            if ("Finished".equals(task.status)) completed.add(task);
            else pending.add(task);
        }

        rvPendingTasks.setAdapter(new TaskControlAdapter(pending, activeTaskId));
        rvCompletedTasks.setAdapter(new TaskControlAdapter(completed, activeTaskId));

        badgePendingCount.setText(String.valueOf(pending.size()));
        badgeCompletedCount.setText(String.valueOf(completed.size()));
        tvTargetsSummary.setText(completed.size() + " of " + tasks.size() + " complete");

        tvLevelChip.setText(readLevel());

        rvPendingTasks.setVisibility(pendingExpanded ? View.VISIBLE : View.GONE);
        arrowPending.setRotation(pendingExpanded ? 0 : 180);
        tvPendingEmpty.setVisibility(pendingExpanded && pending.isEmpty() ? View.VISIBLE : View.GONE);

        rvCompletedTasks.setVisibility(completedExpanded ? View.VISIBLE : View.GONE);
        arrowCompleted.setRotation(completedExpanded ? 0 : 180);
        tvCompletedEmpty.setVisibility(completedExpanded && completed.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String readLevel() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DBHelper.TABLE_PROFILE, new String[]{DBHelper.PROFILE_LEVEL}, null,
                null, null, null, null, "1")) {
            if (c.moveToFirst()) return c.getString(0);
        } catch (Exception ignored) {
        }
        return "Beginner";
    }

    private void setupNavigation() {
        findViewById(R.id.settingsb).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.homeb).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.targetb).setOnClickListener(v -> {});
        findViewById(R.id.analyticsb).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.profileb).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    class TaskControlAdapter extends RecyclerView.Adapter<TaskControlAdapter.ViewHolder> {
        private List<Task> tasks;
        private Integer activeId;

        TaskControlAdapter(List<Task> tasks, Integer activeId) {
            this.tasks = tasks;
            this.activeId = activeId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_control, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Task task = tasks.get(position);
            holder.bind(task);
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTaskName, tvTaskDuration;
            Button btnStart, btnComplete;
            Task currentTask;

            ViewHolder(View itemView) {
                super(itemView);
                tvTaskName = itemView.findViewById(R.id.tvTaskName);
                tvTaskDuration = itemView.findViewById(R.id.tvTaskDuration);
                btnStart = itemView.findViewById(R.id.btnStart);
                btnComplete = itemView.findViewById(R.id.btnComplete);
            }

            void bind(Task task) {
                currentTask = task;
                tvTaskName.setText(task.name);
                tvTaskDuration.setText(task.minutes + " minutes");

                if (task.status.equals("Finished")) {
                    btnStart.setEnabled(false);
                    btnStart.setText("DONE");
                    btnComplete.setVisibility(View.GONE);
                } else if (activeId != null && !activeId.equals(task.id)) {
                    btnStart.setEnabled(false);
                    btnStart.setText("WAIT");
                    btnComplete.setVisibility(View.GONE);
                } else {
                    boolean isActive = activeId != null && activeId.equals(task.id);
                    btnStart.setEnabled(true);
                    btnStart.setText(isActive ? "RESUME" : "START");
                    btnStart.setOnClickListener(v -> openTimer());
                    btnComplete.setVisibility(isActive ? View.VISIBLE : View.GONE);
                    btnComplete.setOnClickListener(v -> completeTask());
                }
            }

            void openTimer() {
                Intent intent = new Intent(TargetActivity.this, TimerActivity.class);
                intent.putExtra("TASK_ID", currentTask.id);
                intent.putExtra("TASK_NAME", currentTask.name);
                intent.putExtra("DURATION_MINUTES", currentTask.minutes);
                startActivity(intent);
            }

            void completeTask() {
                timerController.clearTimer();
                activeId = null;
                activeTaskId = null;

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.TASK_STATUS, "Finished");
                db.update(DBHelper.TABLE_TASKS, values, DBHelper.TASK_ID + " = ?",
                    new String[]{String.valueOf(currentTask.id)});

                currentTask.status = "Finished";

                Toast.makeText(TargetActivity.this, "Task completed!", Toast.LENGTH_SHORT).show();
                setupTasks();
            }
        }
    }
}
