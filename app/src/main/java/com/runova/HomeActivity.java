package com.runova;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.runova.controllers.LevelCycleController;
import com.runova.controllers.TaskGenerator;
import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;
import com.runova.helpers.LevelUpDialog;
import com.runova.helpers.WindowHelper;
import com.runova.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private TaskGenerator taskGenerator;
    private LevelCycleController levelCycleController;
    private RecyclerView rvTasks;
    private TextView tvGreeting, tvDate, tvLevelProgress;
    private TextView tvFinishedCount, tvPendingCount, tvMissedCount;
    private ProgressBar progressLevel;
    private ImageButton btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_home);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));
        WindowHelper.applyBottomInset(findViewById(R.id.navigationBar));

        dbHelper = new DBHelper(this);
        taskGenerator = new TaskGenerator(dbHelper);
        levelCycleController = new LevelCycleController(dbHelper);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);
        tvLevelProgress = findViewById(R.id.tvLevelProgress);
        progressLevel = findViewById(R.id.progressLevel);
        rvTasks = findViewById(R.id.rvTasks);
        btnNotifications = findViewById(R.id.btnNotifications);
        tvFinishedCount = findViewById(R.id.tvFinishedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvMissedCount = findViewById(R.id.tvMissedCount);

        rvTasks.setLayoutManager(getResources().getConfiguration().screenWidthDp >= 600
            ? new androidx.recyclerview.widget.GridLayoutManager(this, 2)
            : new LinearLayoutManager(this));

        setupGreeting();
        setupDate();
        setupLevelProgress();
        List<Task> tasks = setupTasks();
        setupStatusCounts(tasks);
        checkLevelUpPrompt();
        setupNavigation();

        btnNotifications.setOnClickListener(v ->
            startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.btnViewAll).setOnClickListener(v ->
            startActivity(new Intent(this, TargetActivity.class)));
    }

    private void setupGreeting() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE, new String[]{DBHelper.PROFILE_FIRST_NAME},
            DBHelper.PROFILE_ID + " = 1", null, null, null, null);
        if (cursor.moveToFirst()) {
            String firstName = cursor.getString(0);
            tvGreeting.setText("Hello " + firstName + "!");
        }
        cursor.close();
    }

    private void setupDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
        tvDate.setText(sdf.format(new Date()));
    }

    private void setupLevelProgress() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE, new String[]{DBHelper.PROFILE_LEVEL_START},
            DBHelper.PROFILE_ID + " = 1", null, null, null, null);
        if (cursor.moveToFirst()) {
            String levelStart = cursor.getString(0);
            long days = DateUtils.daysBetween(levelStart, DateUtils.today()) + 1;
            tvLevelProgress.setText("Day " + days + " of 28");
            progressLevel.setProgress((int) days);
        }
        cursor.close();
    }

    private List<Task> setupTasks() {
        String today = DateUtils.today();
        List<Task> tasks = taskGenerator.generateDailyTasks(today);

        rvTasks.setAdapter(new TaskAdapter(tasks, v ->
            startActivity(new Intent(this, TargetActivity.class))));
        return tasks;
    }

    private void setupStatusCounts(List<Task> tasks) {
        int finished = 0, pending = 0, missed = 0;
        for (Task task : tasks) {
            if ("Finished".equals(task.status)) finished++;
            else if ("Missed".equals(task.status)) missed++;
            else pending++;
        }
        tvFinishedCount.setText(String.valueOf(finished));
        tvPendingCount.setText(String.valueOf(pending));
        tvMissedCount.setText(String.valueOf(missed));
    }

    private void setupNavigation() {
        findViewById(R.id.settingsb).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.homeb).setOnClickListener(v -> {});
        findViewById(R.id.targetb).setOnClickListener(v -> startActivity(new Intent(this, TargetActivity.class)));
        findViewById(R.id.analyticsb).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.profileb).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void checkLevelUpPrompt() {
        if (levelCycleController.shouldShowLevelUpPrompt()) {
            LevelUpDialog.show(this, this::recreate);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupLevelProgress();
        setupStatusCounts(setupTasks());
    }

    static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
        private List<Task> tasks;
        private View.OnClickListener rowClick;

        TaskAdapter(List<Task> tasks, View.OnClickListener rowClick) {
            this.tasks = tasks;
            this.rowClick = rowClick;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Task task = tasks.get(position);
            holder.tvTaskName.setText(task.name);
            holder.tvTaskDuration.setText(task.minutes + " minutes");
            holder.tvTaskStatus.setText("Finished".equals(task.status) ? "Completed" : task.status);
            holder.itemView.setOnClickListener(rowClick);
            int bg = "Finished".equals(task.status) ? R.drawable.bg_status_completed
                    : "Missed".equals(task.status) ? R.drawable.bg_status_missed
                    : R.drawable.bg_status_pending;
            holder.tvTaskStatus.setBackgroundResource(bg);
        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }

        static class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView tvTaskName, tvTaskDuration, tvTaskStatus;

            TaskViewHolder(View itemView) {
                super(itemView);
                tvTaskName = itemView.findViewById(R.id.tvTaskName);
                tvTaskDuration = itemView.findViewById(R.id.tvTaskDuration);
                tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            }
        }
    }
}
