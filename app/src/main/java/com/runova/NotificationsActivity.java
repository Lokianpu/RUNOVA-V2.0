package com.runova;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.runova.controllers.LevelCycleController;
import com.runova.database.DBHelper;
import com.runova.helpers.WindowHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private LevelCycleController levelCycleController;
    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private List<NotificationItem> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_notifications);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));
        WindowHelper.applyBottomInset(findViewById(R.id.navigationBar));

        dbHelper = new DBHelper(this);
        levelCycleController = new LevelCycleController(dbHelper);

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();
        setupNavigation();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        notifications = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBHelper.TABLE_NOTIFICATIONS, null,
                DBHelper.NOTIF_RESOLVED + " = 0", null, null, null,
                DBHelper.NOTIF_CREATED + " DESC");

        while (cursor.moveToNext()) {
            NotificationItem item = new NotificationItem();
            item.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.NOTIF_ID));
            item.type = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NOTIF_TYPE));
            item.message = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NOTIF_MESSAGE));
            notifications.add(item);
        }
        cursor.close();

        if (notifications.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setAdapter(new NotificationAdapter());
        }
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NotificationItem item = notifications.get(position);
            holder.tvMessage.setText(item.message);

            String currentLevel = levelCycleController.getCurrentLevel();
            double passRate = levelCycleController.getPassRate();

            if (passRate >= 0.85) {
                String nextLevel = getNextLevel(currentLevel);
                holder.btnOption1.setText("Move up to " + nextLevel);
                holder.btnOption2.setText("Repeat " + currentLevel);

                holder.btnOption1.setOnClickListener(v -> {
                    levelCycleController.applyLevelChoice(nextLevel);
                    loadNotifications();
                });
                holder.btnOption2.setOnClickListener(v -> {
                    levelCycleController.applyLevelChoice(currentLevel);
                    loadNotifications();
                });
            } else {
                holder.btnOption1.setText("Repeat " + currentLevel);
                holder.btnOption2.setVisibility(View.GONE);

                holder.btnOption1.setOnClickListener(v -> {
                    levelCycleController.applyLevelChoice(currentLevel);
                    loadNotifications();
                });
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            Button btnOption1, btnOption2;

            ViewHolder(View view) {
                super(view);
                tvMessage = view.findViewById(R.id.tvMessage);
                btnOption1 = view.findViewById(R.id.btnOption1);
                btnOption2 = view.findViewById(R.id.btnOption2);
            }
        }
    }

    private String getNextLevel(String current) {
        if (current.equals("Beginner")) return "Intermediate";
        if (current.equals("Intermediate")) return "Pro";
        return "Pro";
    }

    private void setupNavigation() {
        findViewById(R.id.homeb).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.targetb).setOnClickListener(v -> startActivity(new Intent(this, TargetActivity.class)));
        findViewById(R.id.analyticsb).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.profileb).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    static class NotificationItem {
        int id;
        String type;
        String message;
    }
}
