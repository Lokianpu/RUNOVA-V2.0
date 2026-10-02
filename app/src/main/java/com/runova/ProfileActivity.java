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
import com.runova.helpers.AboutDialog;
import com.runova.helpers.DateUtils;
import com.runova.helpers.StatsHelper;
import com.runova.helpers.WindowHelper;

public class ProfileActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etFirstName, etLastName, etHeight, etWeight;
    private Spinner spinnerAge, spinnerGender, spinnerLevel;
    private TextView tvProfileName, tvViewLevel, tvStatHeight, tvStatWeight,
            tvStatSessions, tvStatStreak, tvStatAge, tvStatGender;
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

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        spinnerAge = findViewById(R.id.spinnerAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerLevel = findViewById(R.id.spinnerLevel);

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
        String[] ages = new String[25 - 17 + 1];
        for (int i = 0; i < ages.length; i++) {
            ages[i] = String.valueOf(17 + i);
        }
        ArrayAdapter<String> ageAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ages);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(ageAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(this,
                R.array.level_options, android.R.layout.simple_spinner_item);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
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
            setSpinnerValue(spinnerAge, String.valueOf(age));

            String gender = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_GENDER));
            setSpinnerValue(spinnerGender, gender);

            double height = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_HEIGHT));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_WEIGHT));
            etHeight.setText(String.valueOf(height));
            etWeight.setText(String.valueOf(weight));

            currentLevel = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LEVEL));
            setSpinnerValue(spinnerLevel, currentLevel);

            String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_FIRST_NAME));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.PROFILE_LAST_NAME));
            tvProfileName.setText((firstName + " " + lastName).trim());
            tvViewLevel.setText(currentLevel);
            tvStatHeight.setText(fmtNum(height) + " cm");
            tvStatWeight.setText(fmtNum(weight) + " kg");
            tvStatAge.setText(String.valueOf(age));
            tvStatGender.setText(gender == null ? "--" : gender);
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

        dateOfBirth = DateUtils.dobFromAge(Integer.parseInt(spinnerAge.getSelectedItem().toString()));

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
        values.put(DBHelper.PROFILE_DOB, dateOfBirth);
        values.put(DBHelper.PROFILE_LEVEL, newLevel);
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
        values.put(DBHelper.PROFILE_DOB, dateOfBirth);
        values.put(DBHelper.PROFILE_LEVEL, level);

        db.update(DBHelper.TABLE_PROFILE, values, DBHelper.PROFILE_ID + " = 1", null);
        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        exitEdit();
    }

    private void showAboutPopup() {
        AboutDialog.show(this);
    }

    private void setupNavigation() {
        findViewById(R.id.btnNotifications).setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.settingsb).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.homeb).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.targetb).setOnClickListener(v -> startActivity(new Intent(this, TargetActivity.class)));
        findViewById(R.id.analyticsb).setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        findViewById(R.id.profileb).setOnClickListener(v -> {});
    }
}
