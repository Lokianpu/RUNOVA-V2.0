package com.runova.controllers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.CountDownTimer;

import com.runova.database.DBHelper;

public class TimerController {
    private DBHelper dbHelper;
    private CountDownTimer timer;
    private TimerCallback callback;

    public interface TimerCallback {
        void onTick(long millisRemaining);
        void onFinish();
    }

    public static class ActiveState {
        public final int taskId;
        public final long remainingMs;
        public final boolean paused;

        ActiveState(int taskId, long remainingMs, boolean paused) {
            this.taskId = taskId;
            this.remainingMs = remainingMs;
            this.paused = paused;
        }
    }

    public TimerController(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void startTimer(int taskId, int minutes, TimerCallback callback) {
        long duration = minutes * 60 * 1000L;
        writeActive(taskId, System.currentTimeMillis() + duration, 0);
        startCountdown(duration, callback);
    }

    public void pauseTimer() {
        ActiveState state = readState();
        cancel();
        if (state == null) {
            return;
        }
        // Paused rows store raw remaining millis in END, not an absolute deadline.
        writeActive(state.taskId, Math.max(0, state.remainingMs), 1);
    }

    public void resumeTimer(TimerCallback callback) {
        ActiveState state = readState();
        if (state == null) {
            return;
        }
        writeActive(state.taskId, System.currentTimeMillis() + state.remainingMs, 0);
        startCountdown(state.remainingMs, callback);
    }

    public ActiveState readState() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE,
            new String[]{DBHelper.PROFILE_ACTIVE_TASK_ID,
                DBHelper.PROFILE_ACTIVE_TASK_END,
                DBHelper.PROFILE_ACTIVE_TASK_PAUSED},
            DBHelper.PROFILE_ID + " = 1", null, null, null, null);

        ActiveState state = null;
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.PROFILE_ACTIVE_TASK_ID);
            int endIndex = cursor.getColumnIndex(DBHelper.PROFILE_ACTIVE_TASK_END);
            int pausedIndex = cursor.getColumnIndex(DBHelper.PROFILE_ACTIVE_TASK_PAUSED);

            if (!cursor.isNull(idIndex)) {
                boolean paused = cursor.getInt(pausedIndex) != 0;
                long end = cursor.getLong(endIndex);
                long remaining = paused ? end : end - System.currentTimeMillis();
                if (paused || remaining > 0) {
                    state = new ActiveState(cursor.getInt(idIndex), Math.max(0, remaining), paused);
                }
            }
        }
        cursor.close();
        return state;
    }

    public void clearTimer() {
        cancel();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(DBHelper.PROFILE_ACTIVE_TASK_ID);
        values.putNull(DBHelper.PROFILE_ACTIVE_TASK_END);
        values.put(DBHelper.PROFILE_ACTIVE_TASK_PAUSED, 0);
        db.update(DBHelper.TABLE_PROFILE, values, DBHelper.PROFILE_ID + " = 1", null);
    }

    public void stopTimer() {
        cancel();
    }

    private void writeActive(int taskId, long endValue, int paused) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.PROFILE_ACTIVE_TASK_ID, taskId);
        values.put(DBHelper.PROFILE_ACTIVE_TASK_END, endValue);
        values.put(DBHelper.PROFILE_ACTIVE_TASK_PAUSED, paused);
        db.update(DBHelper.TABLE_PROFILE, values, DBHelper.PROFILE_ID + " = 1", null);
    }

    private void startCountdown(long duration, TimerCallback callback) {
        cancel();
        this.callback = callback;
        timer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (TimerController.this.callback != null) {
                    TimerController.this.callback.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                clearTimer();
                if (TimerController.this.callback != null) {
                    TimerController.this.callback.onFinish();
                }
            }
        }.start();
    }

    private void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        callback = null;
    }
}
