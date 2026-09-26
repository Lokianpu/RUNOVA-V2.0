package com.runova.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "runova.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    public static final String TABLE_PROFILE = "profile";
    public static final String TABLE_TASKS = "tasks";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    // Profile columns
    public static final String PROFILE_ID = "id";
    public static final String PROFILE_FIRST_NAME = "firstName";
    public static final String PROFILE_LAST_NAME = "lastName";
    public static final String PROFILE_DOB = "dateOfBirth";
    public static final String PROFILE_GENDER = "gender";
    public static final String PROFILE_HEIGHT = "height";
    public static final String PROFILE_WEIGHT = "weight";
    public static final String PROFILE_EVENT = "event";
    public static final String PROFILE_LEVEL = "level";
    public static final String PROFILE_GOAL = "goal";
    public static final String PROFILE_LEVEL_START = "levelStartDate";
    public static final String PROFILE_SEEN_INTRO = "seenIntro";
    public static final String PROFILE_ACTIVE_TASK_ID = "activeTaskId";
    public static final String PROFILE_ACTIVE_TASK_END = "activeTaskEndTime";
    public static final String PROFILE_ACTIVE_TASK_PAUSED = "activeTaskPaused";

    // Task columns
    public static final String TASK_ID = "id";
    public static final String TASK_DATE = "date";
    public static final String TASK_NAME = "name";
    public static final String TASK_TYPE = "type";
    public static final String TASK_MINUTES = "minutes";
    public static final String TASK_STATUS = "status";
    public static final String TASK_SORT_ORDER = "sortOrder";

    // Notification columns
    public static final String NOTIF_ID = "id";
    public static final String NOTIF_TYPE = "type";
    public static final String NOTIF_MESSAGE = "message";
    public static final String NOTIF_CREATED = "created";
    public static final String NOTIF_RESOLVED = "resolved";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create profile table
        db.execSQL("CREATE TABLE " + TABLE_PROFILE + " (" +
                PROFILE_ID + " INTEGER PRIMARY KEY, " +
                PROFILE_FIRST_NAME + " TEXT NOT NULL, " +
                PROFILE_LAST_NAME + " TEXT NOT NULL, " +
                PROFILE_DOB + " TEXT NOT NULL, " +
                PROFILE_GENDER + " TEXT NOT NULL, " +
                PROFILE_HEIGHT + " REAL NOT NULL, " +
                PROFILE_WEIGHT + " REAL NOT NULL, " +
                PROFILE_EVENT + " TEXT NOT NULL, " +
                PROFILE_LEVEL + " TEXT NOT NULL, " +
                PROFILE_GOAL + " TEXT NOT NULL, " +
                PROFILE_LEVEL_START + " TEXT NOT NULL, " +
                PROFILE_SEEN_INTRO + " INTEGER NOT NULL DEFAULT 0, " +
                PROFILE_ACTIVE_TASK_ID + " INTEGER, " +
                PROFILE_ACTIVE_TASK_END + " INTEGER, " +
                PROFILE_ACTIVE_TASK_PAUSED + " INTEGER NOT NULL DEFAULT 0)");

        // Create tasks table
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TASK_DATE + " TEXT NOT NULL, " +
                TASK_NAME + " TEXT NOT NULL, " +
                TASK_TYPE + " TEXT NOT NULL, " +
                TASK_MINUTES + " INTEGER NOT NULL, " +
                TASK_STATUS + " TEXT NOT NULL DEFAULT 'Pending', " +
                TASK_SORT_ORDER + " INTEGER NOT NULL)");

        // Create index on tasks.date
        db.execSQL("CREATE INDEX idx_tasks_date ON " + TABLE_TASKS + "(" + TASK_DATE + ")");

        // Create notifications table
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NOTIF_TYPE + " TEXT NOT NULL, " +
                NOTIF_MESSAGE + " TEXT NOT NULL, " +
                NOTIF_CREATED + " INTEGER NOT NULL, " +
                NOTIF_RESOLVED + " INTEGER NOT NULL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " +
                    PROFILE_ACTIVE_TASK_PAUSED + " INTEGER NOT NULL DEFAULT 0");
        }
    }
}
