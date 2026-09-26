package com.runova.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.runova.database.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Read-only task statistics shared by Analytics and Profile.
 * Streak rule (spec 8.3): consecutive Finished days ending today or yesterday.
 */
public class StatsHelper {
    private final DBHelper dbHelper;

    public StatsHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** All-time count of finished tasks (sessions). */
    public int sessions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + DBHelper.TABLE_TASKS
                        + " WHERE status = 'Finished'", null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    /** Consecutive fully-Finished days ending today or yesterday. */
    public int streak() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        int streak = 0;

        for (int i = 0; i < 100; i++) {
            String date = sdf.format(cal.getTime());
            int total = 0;
            int finished = 0;

            try (Cursor c = db.rawQuery(
                    "SELECT COUNT(*), SUM(CASE WHEN status = 'Finished' THEN 1 ELSE 0 END) "
                            + "FROM " + DBHelper.TABLE_TASKS + " WHERE date = ?",
                    new String[]{date})) {
                if (c.moveToFirst()) {
                    total = c.getInt(0);
                    finished = c.getInt(1);
                }
            }

            if (total > 0 && total == finished) {
                streak++;
            } else if (total > 0 && i > 0) {
                // Past day with unfinished tasks ends the streak.
                break;
            }
            // Today may be unfinished (day not over): allowed, keep counting back.
            // Past day with no rows: skip (lazy generation hole).

            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        return streak;
    }
}
