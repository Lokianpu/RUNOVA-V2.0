package com.runova;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.runova.controllers.TaskGenerator;
import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;
import com.runova.helpers.WindowHelper;
import com.runova.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TargetActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private TaskGenerator taskGenerator;
    private RecyclerView rvPendingTasks, rvCompletedTasks, rvMissedTasks;
    private TextView tvLevelChip, tvTargetsSummary, tvPendingEmpty, tvCompletedEmpty, tvMissedEmpty;
    private TextView badgePendingCount, badgeCompletedCount, badgeMissedCount;
    private ImageView arrowPending, arrowCompleted, arrowMissed;
    private boolean pendingExpanded = true;
    private boolean completedExpanded = false;
    private boolean missedExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_target);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));
        WindowHelper.applyBottomInset(findViewById(R.id.navigationBar));

        dbHelper = new DBHelper(this);
        taskGenerator = new TaskGenerator(dbHelper);

        rvPendingTasks = findViewById(R.id.rvPendingTasks);
        rvCompletedTasks = findViewById(R.id.rvCompletedTasks);
        rvMissedTasks = findViewById(R.id.rvMissedTasks);
        tvLevelChip = findViewById(R.id.tvLevelChip);
        tvTargetsSummary = findViewById(R.id.tvTargetsSummary);
        tvPendingEmpty = findViewById(R.id.tvPendingEmpty);
        tvCompletedEmpty = findViewById(R.id.tvCompletedEmpty);
        tvMissedEmpty = findViewById(R.id.tvMissedEmpty);
        badgePendingCount = findViewById(R.id.badgePendingCount);
        badgeCompletedCount = findViewById(R.id.badgeCompletedCount);
        badgeMissedCount = findViewById(R.id.badgeMissedCount);
        arrowPending = findViewById(R.id.arrowPending);
        arrowCompleted = findViewById(R.id.arrowCompleted);
        arrowMissed = findViewById(R.id.arrowMissed);

        rvPendingTasks.setLayoutManager(taskLayout());
        rvCompletedTasks.setLayoutManager(taskLayout());
        rvMissedTasks.setLayoutManager(taskLayout());

        findViewById(R.id.headerPending).setOnClickListener(v -> {
            pendingExpanded = !pendingExpanded;
            setupTasks();
        });
        findViewById(R.id.headerCompleted).setOnClickListener(v -> {
            completedExpanded = !completedExpanded;
            setupTasks();
        });
        findViewById(R.id.headerMissed).setOnClickListener(v -> {
            missedExpanded = !missedExpanded;
            setupTasks();
        });

        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupTasks();
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

        List<Task> missed = loadMissed();

        rvPendingTasks.setAdapter(new TaskControlAdapter(pending));
        rvCompletedTasks.setAdapter(new TaskControlAdapter(completed));
        rvMissedTasks.setAdapter(new TaskControlAdapter(missed));

        badgePendingCount.setText(String.valueOf(pending.size()));
        badgeCompletedCount.setText(String.valueOf(completed.size()));
        badgeMissedCount.setText(String.valueOf(missed.size()));
        tvTargetsSummary.setText(completed.size() + " of " + tasks.size() + " completed");

        tvLevelChip.setText(readLevel());

        rvPendingTasks.setVisibility(pendingExpanded ? View.VISIBLE : View.GONE);
        arrowPending.setRotation(pendingExpanded ? 0 : 180);
        tvPendingEmpty.setVisibility(pendingExpanded && pending.isEmpty() ? View.VISIBLE : View.GONE);

        rvCompletedTasks.setVisibility(completedExpanded ? View.VISIBLE : View.GONE);
        arrowCompleted.setRotation(completedExpanded ? 0 : 180);
        tvCompletedEmpty.setVisibility(completedExpanded && completed.isEmpty() ? View.VISIBLE : View.GONE);

        rvMissedTasks.setVisibility(missedExpanded ? View.VISIBLE : View.GONE);
        arrowMissed.setRotation(missedExpanded ? 0 : 180);
        tvMissedEmpty.setVisibility(missedExpanded && missed.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<Task> loadMissed() {
        List<Task> missed = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT " + DBHelper.TASK_ID + ", " + DBHelper.TASK_DATE + ", name, type, "
                        + DBHelper.TASK_MINUTES + ", " + DBHelper.TASK_STATUS + ", sortOrder FROM "
                        + DBHelper.TABLE_TASKS + " WHERE " + DBHelper.TASK_DATE + " < ? AND "
                        + DBHelper.TASK_STATUS + " != 'Finished' ORDER BY " + DBHelper.TASK_DATE + " DESC",
                new String[]{DateUtils.today()})) {
            while (c.moveToNext()) {
                Task t = new Task();
                t.id = c.getInt(0);
                t.date = c.getString(1);
                t.name = c.getString(2);
                t.type = c.getString(3);
                t.minutes = c.getInt(4);
                t.status = "Missed";
                t.sortOrder = c.getInt(6);
                missed.add(t);
            }
        }
        return missed;
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

    private RecyclerView.LayoutManager taskLayout() {
        return getResources().getConfiguration().screenWidthDp >= 600
            ? new androidx.recyclerview.widget.GridLayoutManager(this, 2)
            : new LinearLayoutManager(this);
    }

    private void setupNavigation() {
        findViewById(R.id.btnNotifications).setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.settingsb).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.homeb).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.targetb).setOnClickListener(v -> {});
        findViewById(R.id.analyticsb).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.profileb).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    class TaskControlAdapter extends RecyclerView.Adapter<TaskControlAdapter.ViewHolder> {
        private List<Task> tasks;

        TaskControlAdapter(List<Task> tasks) {
            this.tasks = tasks;
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
            TextView tvTaskName, tvTaskDuration, tvTaskStatus;
            Button btnStart, btnComplete;
            Task currentTask;

            ViewHolder(View itemView) {
                super(itemView);
                tvTaskName = itemView.findViewById(R.id.tvTaskName);
                tvTaskDuration = itemView.findViewById(R.id.tvTaskDuration);
                tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
                btnStart = itemView.findViewById(R.id.btnStart);
                btnComplete = itemView.findViewById(R.id.btnComplete);
            }

            void bind(Task task) {
                currentTask = task;
                itemView.setOnClickListener(v -> openContent());
                tvTaskName.setText(task.name);
                tvTaskDuration.setText(task.minutes + " minutes");

                boolean finished = task.status.equals("Finished");
                boolean missed = task.status.equals("Missed");
                tvTaskStatus.setText(finished ? "Completed" : (missed ? "Missed" : "Pending"));
                tvTaskStatus.setBackgroundResource(finished ? R.drawable.bg_status_completed
                        : missed ? R.drawable.bg_status_missed
                        : R.drawable.bg_status_pending);

                btnComplete.setVisibility(View.GONE);
                if (finished || missed) {
                    btnStart.setVisibility(View.GONE);
                } else {
                    btnStart.setVisibility(View.VISIBLE);
                    btnStart.setEnabled(true);
                    btnStart.setText("START");
                    btnStart.setOnClickListener(v -> openContent());
                }
            }

            void openContent() {
                Intent intent = new Intent(TargetActivity.this, TrainingContentActivity.class);
                intent.putExtra("TASK_ID", currentTask.id);
                intent.putExtra("TASK_NAME", currentTask.name);
                intent.putExtra("TASK_TYPE", currentTask.type);
                intent.putExtra("DURATION_MINUTES", currentTask.minutes);
                startActivity(intent);
            }
        }
    }
}
