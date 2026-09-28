package com.runova.controllers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;

public class LevelCycleController {
    private DBHelper dbHelper;

    public LevelCycleController(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean shouldShowLevelUpPrompt() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE,
                new String[]{DBHelper.PROFILE_LEVEL_START},
                DBHelper.PROFILE_ID + " = 1", null, null, null, null);

        if (cursor.moveToFirst()) {
            String levelStart = cursor.getString(0);
            long days = DateUtils.daysBetween(levelStart, DateUtils.today()) + 1;
            cursor.close();

            if (days >= 28) {
                return !hasUnresolvedLevelUpOffer();
            }
        }
        cursor.close();
        return false;
    }

    public double getPassRate() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE,
                new String[]{DBHelper.PROFILE_LEVEL_START},
                DBHelper.PROFILE_ID + " = 1", null, null, null, null);

        String levelStart = "";
        if (cursor.moveToFirst()) {
            levelStart = cursor.getString(0);
        }
        cursor.close();

        Cursor taskCursor = db.rawQuery(
                "SELECT COUNT(DISTINCT date) FROM " + DBHelper.TABLE_TASKS +
                        " WHERE date >= ? AND date <= ? AND status = 'Finished' " +
                        "AND date IN (SELECT date FROM " + DBHelper.TABLE_TASKS +
                        " GROUP BY date HAVING COUNT(*) = SUM(CASE WHEN status = 'Finished' THEN 1 ELSE 0 END))",
                new String[]{levelStart, DateUtils.today()}
        );

        int finishedDays = 0;
        if (taskCursor.moveToFirst()) {
            finishedDays = taskCursor.getInt(0);
        }
        taskCursor.close();

        return finishedDays / 28.0;
    }

    public String getCurrentLevel() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE,
                new String[]{DBHelper.PROFILE_LEVEL},
                DBHelper.PROFILE_ID + " = 1", null, null, null, null);

        String level = "Beginner";
        if (cursor.moveToFirst()) {
            level = cursor.getString(0);
        }
        cursor.close();
        return level;
    }

    public void applyLevelChoice(String choice) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.PROFILE_LEVEL, choice);
        values.put(DBHelper.PROFILE_LEVEL_START, DateUtils.today());
        db.update(DBHelper.TABLE_PROFILE, values, DBHelper.PROFILE_ID + " = 1", null);

        db.execSQL("UPDATE " + DBHelper.TABLE_NOTIFICATIONS +
                " SET " + DBHelper.NOTIF_RESOLVED + " = 1 WHERE " +
                DBHelper.NOTIF_TYPE + " = 'LEVEL_UP_OFFER'");
    }

    public void createLevelUpOffer(String message) {
        if (hasUnresolvedLevelUpOffer()) {
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.NOTIF_TYPE, "LEVEL_UP_OFFER");
        values.put(DBHelper.NOTIF_MESSAGE, message);
        values.put(DBHelper.NOTIF_CREATED, System.currentTimeMillis());
        values.put(DBHelper.NOTIF_RESOLVED, 0);
        db.insert(DBHelper.TABLE_NOTIFICATIONS, null, values);
    }

    private boolean hasUnresolvedLevelUpOffer() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_NOTIFICATIONS,
                new String[]{DBHelper.NOTIF_ID},
                DBHelper.NOTIF_TYPE + " = 'LEVEL_UP_OFFER' AND " +
                        DBHelper.NOTIF_RESOLVED + " = 0",
                null, null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
