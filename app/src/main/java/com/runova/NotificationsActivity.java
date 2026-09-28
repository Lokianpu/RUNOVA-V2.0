package com.runova;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.runova.database.DBHelper;
import com.runova.helpers.LevelUpDialog;
import com.runova.helpers.WindowHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private DBHelper dbHelper;
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
            holder.itemView.setOnClickListener(v ->
                    LevelUpDialog.show(NotificationsActivity.this, NotificationsActivity.this::loadNotifications));
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;

            ViewHolder(View view) {
                super(view);
                tvMessage = view.findViewById(R.id.tvMessage);
            }
        }
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
