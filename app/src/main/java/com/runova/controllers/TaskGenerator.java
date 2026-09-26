package com.runova.controllers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;
import com.runova.models.DayTemplate;
import com.runova.models.Profile;
import com.runova.models.Task;
import com.runova.models.TaskTemplate;
import com.runova.training.LevelPlan;

import java.util.ArrayList;
import java.util.List;

public class TaskGenerator {
    private DBHelper dbHelper;

    public TaskGenerator(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Generate tasks for a specific date (lazy generation)
    public List<Task> generateDailyTasks(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Check if tasks already exist for this date
        Cursor cursor = db.query(
            DBHelper.TABLE_TASKS,
            null,
            DBHelper.TASK_DATE + " = ?",
            new String[]{date},
            null, null, null
        );
        
        if (cursor.getCount() > 0) {
            // Tasks already generated, return them
            List<Task> tasks = new ArrayList<>();
            while (cursor.moveToNext()) {
                Task task = new Task();
                task.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TASK_ID));
                task.date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.TASK_DATE));
                task.name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.TASK_NAME));
                task.type = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.TASK_TYPE));
                task.minutes = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TASK_MINUTES));
                task.status = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.TASK_STATUS));
                task.sortOrder = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.TASK_SORT_ORDER));
                tasks.add(task);
            }
            cursor.close();
            return tasks;
        }
        cursor.close();
        
        // Get profile configuration
        Profile profile = getProfile();
        if (profile == null) {
            return new ArrayList<>();
        }
        
        // Get weekday and template
        int weekday = DateUtils.getWeekday(date);
        DayTemplate template = LevelPlan.getTemplate(profile.level, weekday);
        
        // Create and save tasks
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase writeDb = dbHelper.getWritableDatabase();
        
        int sortOrder = 1;
        for (TaskTemplate taskTemplate : template.tasks) {
            ContentValues values = new ContentValues();
            values.put(DBHelper.TASK_DATE, date);
            values.put(DBHelper.TASK_NAME, taskTemplate.name);
            values.put(DBHelper.TASK_TYPE, taskTemplate.type);
            values.put(DBHelper.TASK_MINUTES, taskTemplate.minutes);
            values.put(DBHelper.TASK_STATUS, "Pending");
            values.put(DBHelper.TASK_SORT_ORDER, sortOrder);
            
            long id = writeDb.insert(DBHelper.TABLE_TASKS, null, values);
            
            Task task = new Task(date, taskTemplate.name, taskTemplate.type, 
                               taskTemplate.minutes, sortOrder);
            task.id = (int) id;
            tasks.add(task);
            
            sortOrder++;
        }
        
        return tasks;
    }
    
    // Get profile from database
    private Profile getProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DBHelper.TABLE_PROFILE,
            null,
            DBHelper.PROFILE_ID + " = 1",
            null, null, null, null
        );
        
        if (cursor.moveToFirst()) {
            Profile profile = new Profile();
            profile.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_ID));
            profile.firstName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_FIRST_NAME));
            profile.lastName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LAST_NAME));
            profile.dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_DOB));
            profile.gender = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_GENDER));
            profile.height = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_HEIGHT));
            profile.weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_WEIGHT));
            profile.event = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_EVENT));
            profile.level = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LEVEL));
            profile.goal = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_GOAL));
            profile.levelStartDate = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LEVEL_START));
            profile.seenIntro = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_SEEN_INTRO)) == 1;
            
            int activeTaskIdIndex = cursor.getColumnIndexOrThrow(DBHelper.PROFILE_ACTIVE_TASK_ID);
            if (!cursor.isNull(activeTaskIdIndex)) {
                profile.activeTaskId = cursor.getInt(activeTaskIdIndex);
            }
            
            int activeTaskEndIndex = cursor.getColumnIndexOrThrow(DBHelper.PROFILE_ACTIVE_TASK_END);
            if (!cursor.isNull(activeTaskEndIndex)) {
                profile.activeTaskEndTime = cursor.getLong(activeTaskEndIndex);
            }
            
            cursor.close();
            return profile;
        }
        
        cursor.close();
        return null;
    }
}
