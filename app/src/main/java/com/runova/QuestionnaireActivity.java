package com.runova;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.runova.database.DBHelper;
import com.runova.helpers.DateUtils;
import com.runova.helpers.ValidationHelper;
import com.runova.helpers.WindowHelper;

public class QuestionnaireActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etHeight, etWeight;
    private Spinner spinnerLevel;
    private Button btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_questionnaire);
        WindowHelper.applyVerticalInsets(findViewById(R.id.screenScroll));

        dbHelper = new DBHelper(this);

        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        btnFinish = findViewById(R.id.btnFinish);

        setupSpinners();
        btnFinish.setOnClickListener(v -> handleFinish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        String[] levels = {"Beginner", "Intermediate", "Pro"};
        ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);
        spinnerLevel.setSelection(0);
    }

    private void handleFinish() {
        String height = etHeight.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String level = spinnerLevel.getSelectedItem().toString();

        String error = ValidationHelper.validateHeight(height);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        error = ValidationHelper.validateWeight(weight);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }


        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String dateOfBirth = intent.getStringExtra("dateOfBirth");
        String gender = intent.getStringExtra("gender");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.PROFILE_ID, 1);
        values.put(DBHelper.PROFILE_FIRST_NAME, firstName);
        values.put(DBHelper.PROFILE_LAST_NAME, lastName);
        values.put(DBHelper.PROFILE_DOB, dateOfBirth);
        values.put(DBHelper.PROFILE_GENDER, gender);
        values.put(DBHelper.PROFILE_EVENT, "General Running");
        values.put(DBHelper.PROFILE_GOAL, "Learn the Basics");
        values.put(DBHelper.PROFILE_HEIGHT, Double.parseDouble(height));
        values.put(DBHelper.PROFILE_WEIGHT, Double.parseDouble(weight));
        values.put(DBHelper.PROFILE_LEVEL, level);
        values.put(DBHelper.PROFILE_LEVEL_START, DateUtils.today());
        values.put(DBHelper.PROFILE_SEEN_INTRO, 1);

        db.insertWithOnConflict(DBHelper.TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        Intent homeIntent = new Intent(this, LoadingActivity.class);
        startActivity(homeIntent);
        finish();
    }
}
