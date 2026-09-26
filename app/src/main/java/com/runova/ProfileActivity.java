package com.runova;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;
import com.runova.helpers.StatsHelper;
import com.runova.helpers.WindowHelper;

public class ProfileActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etFirstName, etLastName, etHeight, etWeight;
    private TextView tvAge;
    private Spinner spinnerGender, spinnerEvent, spinnerLevel, spinnerGoal;
    private TextView tvProfileName, tvViewLevel, tvStatHeight, tvStatWeight,
            tvStatSessions, tvStatStreak, tvStatAge, tvStatGender,
            tvCfgEvent, tvCfgLevel, tvCfgGoal;
    private View profileViewBlock, profileEditBlock;
    private StatsHelper statsHelper;
    private String currentLevel;
    private String dateOfBirth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_profile);
        WindowHelper.applyTopInset(findViewById(R.id.screenScroll));
        WindowHelper.applyBottomInset(findViewById(R.id.navigationBar));

        dbHelper = new DBHelper(this);
        statsHelper = new StatsHelper(dbHelper);

        profileViewBlock = findViewById(R.id.profileViewBlock);
        profileEditBlock = findViewById(R.id.profileEditBlock);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvViewLevel = findViewById(R.id.tvViewLevel);
        tvStatHeight = findViewById(R.id.tvStatHeight);
        tvStatWeight = findViewById(R.id.tvStatWeight);
        tvStatSessions = findViewById(R.id.tvStatSessions);
        tvStatStreak = findViewById(R.id.tvStatStreak);
        tvStatAge = findViewById(R.id.tvStatAge);
        tvStatGender = findViewById(R.id.tvStatGender);
        tvCfgEvent = findViewById(R.id.tvCfgEvent);
        tvCfgLevel = findViewById(R.id.tvCfgLevel);
        tvCfgGoal = findViewById(R.id.tvCfgGoal);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        tvAge = findViewById(R.id.tvAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerEvent = findViewById(R.id.spinnerEvent);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        spinnerGoal = findViewById(R.id.spinnerGoal);

        setupSpinners();
        loadProfile();
        setEditMode(false);

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            loadProfile();
            setEditMode(true);
        });
        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> {
            loadProfile();
            setEditMode(false);
        });
        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btnAbout).setOnClickListener(v -> showAboutPopup());
        setupNavigation();
    }

    private void setEditMode(boolean editing) {
        profileViewBlock.setVisibility(editing ? View.GONE : View.VISIBLE);
        profileEditBlock.setVisibility(editing ? View.VISIBLE : View.GONE);
    }

    private void exitEdit() {
        loadProfile();
        setEditMode(false);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> eventAdapter = ArrayAdapter.createFromResource(this,
                R.array.event_options, android.R.layout.simple_spinner_item);
        eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEvent.setAdapter(eventAdapter);

        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(this,
                R.array.level_options, android.R.layout.simple_spinner_item);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.goal_options, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);
    }

    private void loadProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_PROFILE, null,
                DBHelper.PROFILE_ID + " = 1", null, null, null, null);

        if (cursor.moveToFirst()) {
            etFirstName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_FIRST_NAME)));
            etLastName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LAST_NAME)));
            dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_DOB));
            
            int age = DateUtils.ageFrom(dateOfBirth);
            tvAge.setText("Age: " + age);

            String gender = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_GENDER));
            setSpinnerValue(spinnerGender, gender);

            double height = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_HEIGHT));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_WEIGHT));
            etHeight.setText(String.valueOf(height));
            etWeight.setText(String.valueOf(weight));

            String event = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_EVENT));
            setSpinnerValue(spinnerEvent, event);

            currentLevel = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LEVEL));
            setSpinnerValue(spinnerLevel, currentLevel);

            String goal = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_GOAL));
            setSpinnerValue(spinnerGoal, goal);

            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LAST_NAME));
            tvProfileName.setText((firstName + " " + lastName).trim());
            tvViewLevel.setText(currentLevel);
            tvStatHeight.setText(fmtNum(height) + " cm");
            tvStatWeight.setText(fmtNum(weight) + " kg");
            tvStatAge.setText(String.valueOf(age));
            tvStatGender.setText(gender == null ? "--" : gender);
            tvCfgEvent.setText(event == null ? "--" : event);
            tvCfgLevel.setText(currentLevel == null ? "--" : currentLevel);
            tvCfgGoal.setText(goal == null ? "--" : goal);
        }
        cursor.close();

        tvStatSessions.setText(String.valueOf(statsHelper.sessions()));
        tvStatStreak.setText(statsHelper.streak() + " days");
    }

    private String fmtNum(double value) {
        return value % 1 == 0 ? String.valueOf((int) value) : String.valueOf(value);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        double height, weight;
        try {
            height = Double.parseDouble(heightStr);
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid height or weight", Toast.LENGTH_SHORT).show();
            return;
        }

        if (height < 100 || height > 250 || weight < 25 || weight > 250) {
            Toast.makeText(this, "Height (100-250 cm) or Weight (25-250 kg) out of range", Toast.LENGTH_SHORT).show();
            return;
        }

        String newLevel = spinnerLevel.getSelectedItem().toString();

        if (!newLevel.equals(currentLevel)) {
            new AlertDialog.Builder(this)
                    .setTitle("Level Change")
                    .setMessage("Changing your level will reset your current routine, all logged tasks, " +
                    "your streak, and any notifications. This can't be undone. Continue?")
                    .setPositiveButton("Yes", (dialog, which) -> saveLevelChange(firstName, lastName, height, weight, newLevel))
                    .setNegativeButton("No", null)
                    .show();
        } else {
            saveNormalUpdate(firstName, lastName, height, weight, newLevel);
        }
    }

    private void saveLevelChange(String firstName, String lastName, double height, double weight, String newLevel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBHelper.TABLE_TASKS, null, null);
        db.delete(DBHelper.TABLE_NOTIFICATIONS, null, null);

        ContentValues values = new ContentValues();
        values.put(DBHelper.PROFILE_FIRST_NAME, firstName);
        values.put(DBHelper.PROFILE_LAST_NAME, lastName);
        values.put(DBHelper.PROFILE_HEIGHT, height);
        values.put(DBHelper.PROFILE_WEIGHT, weight);
        values.put(DBHelper.PROFILE_GENDER, spinnerGender.getSelectedItem().toString());
        values.put(DBHelper.PROFILE_EVENT, spinnerEvent.getSelectedItem().toString());
        values.put(DBHelper.PROFILE_LEVEL, newLevel);
        values.put(DBHelper.PROFILE_GOAL, spinnerGoal.getSelectedItem().toString());
        values.put(DBHelper.PROFILE_LEVEL_START, DateUtils.today());

        db.update(DBHelper.TABLE_PROFILE, values, DBHelper.PROFILE_ID + " = 1", null);

        Toast.makeText(this, "Profile saved. Tasks reset.", Toast.LENGTH_SHORT).show();
        currentLevel = newLevel;
    }

    private void saveNormalUpdate(String firstName, String lastName, double height, double weight, String level) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.PROFILE_FIRST_NAME, firstName);
        values.put(DBHelper.PROFILE_LAST_NAME, lastName);
        values.put(DBHelper.PROFILE_HEIGHT, height);
        values.put(DBHelper.PROFILE_WEIGHT, weight);
        values.put(DBHelper.PROFILE_GENDER, spinnerGender.getSelectedItem().toString());
        values.put(DBHelper.PROFILE_EVENT, spinnerEvent.getSelectedItem().toString());
        values.put(DBHelper.PROFILE_LEVEL, level);
        values.put(DBHelper.PROFILE_GOAL, spinnerGoal.getSelectedItem().toString());

        db.update(DBHelper.TABLE_PROFILE, values, DBHelper.PROFILE_ID + " = 1", null);
        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        exitEdit();
    }

    private void showAboutPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Before you start 👟")
                .setMessage("Hello, runner! RUNOVA is an offline training guide made for a school project. " +
                        "It gives you a daily set of tasks, but it can't track your distance, speed or heart rate. " +
                        "Nothing here is automatic.\n\n" +
                        "That means you could tap \"Done\" without doing the task, and nobody will stop you. " +
                        "But real results only come from actually doing each task, so follow the plan and give it your best. " +
                        "Listen to your body: if something hurts or you feel unwell, stop and rest. " +
                        "If you have any health concerns, talk to a doctor before you begin.\n\n" +
                        "It's your choice: the shortcut or the real thing. Good luck on your journey!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupNavigation() {
        findViewById(R.id.settingsb).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.homeb).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.targetb).setOnClickListener(v -> startActivity(new Intent(this, TargetActivity.class)));
        findViewById(R.id.analyticsb).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.profileb).setOnClickListener(v -> {});
    }
}
