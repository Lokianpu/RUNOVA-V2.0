package com.runova;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.runova.database.DBHelper;
import com.runova.helpers.AboutDialog;
import com.runova.helpers.DateUtils;
import com.runova.helpers.ValidationHelper;
import com.runova.helpers.WindowHelper;

import java.util.Calendar;

public class SignupActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etFirstName, etLastName;
    private Button btnDatePicker, btnNext;
    private Spinner spinnerGender;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DBHelper(this);
        if (profileExists()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        WindowHelper.applyEdgeToEdge(this);
        setContentView(R.layout.activity_signup);
        WindowHelper.applyVerticalInsets(findViewById(R.id.screenScroll));

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnNext = findViewById(R.id.btnNext);

        setupGenderSpinner();
        setupDatePicker();

        btnNext.setOnClickListener(v -> handleNext());

        // First-time users only: no profile row yet (fresh install / onboarding not done).
        AboutDialog.show(this);
    }

    private boolean profileExists() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
            DBHelper.TABLE_PROFILE,
            new String[]{DBHelper.PROFILE_ID},
            DBHelper.PROFILE_ID + " = 1",
            null, null, null, null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void setupGenderSpinner() {
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            genders
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        btnDatePicker.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR) - 20;
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d);
                    btnDatePicker.setText(selectedDate);
                },
                year, month, day
            );
            dialog.show();
        });
    }

    private void handleNext() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        // Validate
        String error = ValidationHelper.validateFirstName(firstName);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        error = ValidationHelper.validateLastName(lastName);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        error = ValidationHelper.validateDateOfBirth(selectedDate);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender.equals("Select Gender")) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save partial profile
        Intent intent = new Intent(this, QuestionnaireActivity.class);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("dateOfBirth", selectedDate);
        intent.putExtra("gender", gender);
        startActivity(intent);
        finish();
    }
}
