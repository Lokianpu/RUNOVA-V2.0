# Android Form Validation Skill

**Version:** 1.0  
**Category:** Android UI Development  
**Trigger:** "add validation to [screen]" or "validate [field]"

---

## Purpose

Implement robust input validation for Android forms that prevents crashes, provides clear feedback, and follows RUNOVA validation rules.

---

## When to Use

- Creating Sign Up or Questionnaire screens
- Any form with user input
- Edit Profile functionality
- Need to validate text, numbers, dates, or selections

---

## Validation Rules

### Text Fields
- **Name fields:** not empty, trim whitespace
- **Example:** first name, last name

### Date Fields
- **Date of birth:** valid date, not in future, derived age 13-100
- **Use:** DatePickerDialog with maxDate set to today

### Number Fields
- **Height:** positive decimal, suggested bounds 100-250 cm
- **Weight:** positive decimal, suggested bounds 25-250 kg
- **Weekly minutes:** whole number ≥ 0, sensible cap (e.g., 500)

### Selection Fields
- **Spinner:** must not be default "Select..." option (position 0)
- **Example:** gender, event, level, goal

---

## Implementation Pattern

### Step 1: Create Validation Method
```java
private boolean validateInputs() {
    boolean valid = true;
    
    // Validate text fields
    String firstName = firstNameInput.getText().toString().trim();
    if (firstName.isEmpty()) {
        firstNameInput.setError("First name is required");
        valid = false;
    }
    
    String lastName = lastNameInput.getText().toString().trim();
    if (lastName.isEmpty()) {
        lastNameInput.setError("Last name is required");
        valid = false;
    }
    
    // Validate date
    String dob = dobInput.getText().toString();
    if (dob.isEmpty()) {
        dobInput.setError("Date of birth is required");
        valid = false;
    } else {
        int age = DateUtils.ageFrom(dob);
        if (age < 13 || age > 100) {
            dobInput.setError("Age must be between 13 and 100");
            valid = false;
        }
    }
    
    // Validate numbers with try/catch
    try {
        double height = Double.parseDouble(heightInput.getText().toString());
        if (height <= 0) {
            heightInput.setError("Height must be positive");
            valid = false;
        } else if (height < 100 || height > 250) {
            heightInput.setError("Enter a realistic height (100-250 cm)");
            valid = false;
        }
    } catch (NumberFormatException e) {
        heightInput.setError("Enter a valid number");
        valid = false;
    }
    
    try {
        double weight = Double.parseDouble(weightInput.getText().toString());
        if (weight <= 0) {
            weightInput.setError("Weight must be positive");
            valid = false;
        } else if (weight < 25 || weight > 250) {
            weightInput.setError("Enter a realistic weight (25-250 kg)");
            valid = false;
        }
    } catch (NumberFormatException e) {
        weightInput.setError("Enter a valid number");
        valid = false;
    }
    
    try {
        int weeklyMinutes = Integer.parseInt(weeklyMinutesInput.getText().toString());
        if (weeklyMinutes < 0) {
            weeklyMinutesInput.setError("Cannot be negative");
            valid = false;
        } else if (weeklyMinutes > 500) {
            weeklyMinutesInput.setError("Enter a realistic value");
            valid = false;
        }
    } catch (NumberFormatException e) {
        weeklyMinutesInput.setError("Enter a whole number");
        valid = false;
    }
    
    // Validate spinners
    if (genderSpinner.getSelectedItemPosition() == 0) {
        Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
        valid = false;
    }
    
    if (eventSpinner.getSelectedItemPosition() == 0) {
        Toast.makeText(this, "Please select an event", Toast.LENGTH_SHORT).show();
        valid = false;
    }
    
    if (levelSpinner.getSelectedItemPosition() == 0) {
        Toast.makeText(this, "Please select a level", Toast.LENGTH_SHORT).show();
        valid = false;
    }
    
    return valid;
}
```

### Step 2: Call Before Advancing
```java
nextButton.setOnClickListener(v -> {
    if (validateInputs()) {
        // Save data
        saveProfile();
        
        // Navigate to next screen
        Intent intent = new Intent(SignupActivity.this, QuestionnaireActivity.class);
        startActivity(intent);
    }
    // If validation fails, stay on screen with errors shown
});
```

### Step 3: DatePicker Setup (for DOB)
```java
private void setupDatePicker() {
    dobInput.setOnClickListener(v -> showDatePicker());
}

private void showDatePicker() {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    
    DatePickerDialog dialog = new DatePickerDialog(
        this,
        (view, selectedYear, selectedMonth, selectedDay) -> {
            String dob = selectedYear + "-" + 
                         String.format("%02d", selectedMonth + 1) + "-" + 
                         String.format("%02d", selectedDay);
            dobInput.setText(dob);
            
            // Clear any previous error
            dobInput.setError(null);
        },
        year - 20,  // Default to 20 years ago
        month,
        day
    );
    
    // Prevent future dates
    dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    
    dialog.show();
}
```

### Step 4: Spinner Setup with Validation
```java
// In onCreate
Spinner genderSpinner = findViewById(R.id.gender_spinner);
ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
    this,
    android.R.layout.simple_spinner_item,
    new String[]{"Select Gender", "Male", "Female", "Other"}
);
genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
genderSpinner.setAdapter(genderAdapter);

Spinner eventSpinner = findViewById(R.id.event_spinner);
ArrayAdapter<String> eventAdapter = new ArrayAdapter<>(
    this,
    android.R.layout.simple_spinner_item,
    new String[]{
        "Select Event",
        "Sprint",
        "Middle Distance",
        "Long Distance",
        "Hurdles",
        "Relay",
        "General Running"
    }
);
eventAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
eventSpinner.setAdapter(eventAdapter);

Spinner levelSpinner = findViewById(R.id.level_spinner);
ArrayAdapter<String> levelAdapter = new ArrayAdapter<>(
    this,
    android.R.layout.simple_spinner_item,
    new String[]{"Select Level", "Beginner", "Intermediate", "Pro"}
);
levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
levelSpinner.setAdapter(levelAdapter);

// Preselect Beginner (position 1)
levelSpinner.setSelection(1);
```

---

## Error Display Patterns

### Text Input Errors
```java
// Show error beside field
inputField.setError("Error message");

// Clear error when user starts typing
inputField.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        inputField.setError(null);  // Clear error
    }
    
    @Override
    public void afterTextChanged(Editable s) {}
});
```

### Spinner Errors
```java
// Use Toast for spinners (can't use setError on Spinner)
if (spinner.getSelectedItemPosition() == 0) {
    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
    valid = false;
}
```

### Number Format Errors
```java
try {
    int value = Integer.parseInt(input.getText().toString());
    // Validate value...
} catch (NumberFormatException e) {
    input.setError("Enter a valid number");
    valid = false;
}
```

---

## Validation Behavior Rules

1. **Show errors, don't advance** - keep user on screen with errors visible
2. **Keep entered values** - don't clear fields on validation failure
3. **Multiple errors** - show all errors at once (check all fields)
4. **Clear errors on edit** - remove error when user starts fixing
5. **Never crash** - wrap all parsing in try/catch
6. **Specific messages** - "Enter a valid number" not "Invalid input"

---

## Common Validation Scenarios

### Sign Up Form
```java
// Required: firstName, lastName, dob, gender
// All text fields must not be empty (after trim)
// DOB must give age 13-100
// Gender must be selected (not position 0)
```

### Questionnaire Form
```java
// Required: height, weight, weeklyMinutes, event, level, goal
// height: positive decimal, 100-250 suggested
// weight: positive decimal, 25-250 suggested
// weeklyMinutes: integer 0-500
// event: selected (not position 0)
// level: selected (preselected to Beginner, but still check)
// goal: selected (not position 0)
```

### Edit Profile Form
```java
// Same as Sign Up + Questionnaire
// Additional: level can only move DOWN
if (isEdit) {
    String currentLevel = db.getString("level", "Beginner");
    String newLevel = levelSpinner.getSelectedItem().toString();
    
    if (getLevelRank(newLevel) > getLevelRank(currentLevel)) {
        Toast.makeText(this, 
            "Cannot increase level here. Complete your cycle to level up.", 
            Toast.LENGTH_LONG).show();
        valid = false;
    }
}

private int getLevelRank(String level) {
    switch (level) {
        case "Beginner": return 1;
        case "Intermediate": return 2;
        case "Pro": return 3;
        default: return 0;
    }
}
```

---

## Checklist

- [ ] All required fields validated
- [ ] Empty strings checked (after trim)
- [ ] Number parsing wrapped in try/catch
- [ ] Realistic bounds checked (not just >0)
- [ ] Date not in future
- [ ] Age derived and validated (13-100)
- [ ] Spinners checked for selection (position != 0)
- [ ] Errors shown beside fields or Toast
- [ ] Validation returns boolean
- [ ] Called before advancing to next screen
- [ ] User stays on screen if invalid
- [ ] Entered values kept on error
- [ ] No crashes on any input

---

## DateUtils Helper (reference)

```java
public class DateUtils {
    public static int ageFrom(String dob) {
        // dob format: "yyyy-MM-dd"
        try {
            String[] parts = dob.split("-");
            int birthYear = Integer.parseInt(parts[0]);
            int birthMonth = Integer.parseInt(parts[1]);
            int birthDay = Integer.parseInt(parts[2]);
            
            Calendar birth = Calendar.getInstance();
            birth.set(birthYear, birthMonth - 1, birthDay);
            
            Calendar today = Calendar.getInstance();
            
            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            
            // Adjust if birthday hasn't happened this year
            if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                 today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }
            
            return age;
        } catch (Exception e) {
            return -1;  // Invalid date
        }
    }
    
    public static boolean isFutureDate(String date) {
        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            
            Calendar inputDate = Calendar.getInstance();
            inputDate.set(year, month - 1, day);
            
            return inputDate.after(Calendar.getInstance());
        } catch (Exception e) {
            return true;  // Treat invalid as future (will fail validation)
        }
    }
}
```

---

## Example Output

Given a Sign Up form, produces:
1. Complete `validateInputs()` method
2. Button click listener with validation check
3. DatePicker setup for DOB
4. Spinner setup with proper adapters
5. Error clearing listeners (optional)
6. Try/catch blocks around all number parsing
7. Clear, specific error messages

---

**End of Android Form Validation Skill**

---

## Token Efficiency

**Activation:** Auto-activate when task matches pattern (form validation / screen creation / timer implementation).

**Operation:** Apply patterns silently. Generate code directly. No step-by-step narration.

**Output:** Code only. Brief location confirmation if new file created.

**Budget:** <400 tokens per use.

**Deactivation:** Automatic after code generated.

**Example:**
```
✓ [Generated validation code]
```

Not:
```
I will now apply the form-validation skill...
Step 1: Identifying input fields...
Step 2: Creating validateInputs() method...
[Verbose explanation of each validation rule]
```
