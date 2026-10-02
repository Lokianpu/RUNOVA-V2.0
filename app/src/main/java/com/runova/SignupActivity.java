package com.runova;

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

public class SignupActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etFirstName, etLastName;
    private Button btnNext;
    private Spinner spinnerGender, spinnerAge;
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
        spinnerAge = findViewById(R.id.spinnerAge);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnNext = findViewById(R.id.btnNext);

        setupGenderSpinner();
        setupAgeSpinner();

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
        String[] genders = {"Select Sex", "Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            genders
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupAgeSpinner() {
        String[] ages = new String[10];
        ages[0] = "Select Age";
        for (int i = 1; i <= 9; i++) {
            ages[i] = String.valueOf(16 + i); // 17 to 25
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            ages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAge.setAdapter(adapter);
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

        String ageStr = spinnerAge.getSelectedItem().toString();
        if (ageStr.equals("Select Age")) {
            Toast.makeText(this, "Please select your age", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedDate = DateUtils.dobFromAge(Integer.parseInt(ageStr));

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Onboarding already completed (user returned here via back from Home):
        // close without re-launching Home so Back keeps exiting the app.
        if (profileExists()) {
            finish();
        }
    }
}
