# Android Screen Builder Skill

**Version:** 1.0  
**Category:** Android UI Development  
**Trigger:** "create [Activity name] screen" or "build [screen] layout"

---

## Purpose

Quickly scaffold a complete Android Activity with layout, proper structure, manifest entry, and RUNOVA theme applied.

---

## When to Use

- Creating a new screen for RUNOVA
- Scaffolding Activity + XML layout pair
- Setting up proper navigation structure
- Need to follow RUNOVA design system

---

## Inputs

- Activity name (e.g., "Home", "Signup", "Target")
- Layout type (ConstraintLayout default, optional ScrollView wrapper)
- Navigation type (launcher, standalone, bottom-nav)
- UI components needed (buttons, inputs, cards, etc.)

---

## Process

### Step 1: Create Activity Java File
```java
// [Name]Activity.java
package com.runova.app;

import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class [Name]Activity extends AppCompatActivity {
    
    // UI components
    private TextView titleText;
    private Button actionButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_[name]);
        
        // Initialize UI components
        initViews();
        
        // Set up listeners
        setupListeners();
        
        // Load data
        loadData();
    }
    
    private void initViews() {
        titleText = findViewById(R.id.title_text);
        actionButton = findViewById(R.id.action_button);
    }
    
    private void setupListeners() {
        actionButton.setOnClickListener(v -> {
            // Handle button click
        });
    }
    
    private void loadData() {
        // Load from SQLite via DbHelper if needed
    }
}
```

### Step 2: Create Layout XML
```xml
<!-- activity_[name].xml -->
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/runova_background"
    android:padding="24dp">
    
    <TextView
        android:id="@+id/title_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Screen Title"
        android:textSize="24sp"
        android:textStyle="bold"
        android:textColor="@color/runova_text"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
    
    <Button
        android:id="@+id/action_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Action"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/runova_text"
        android:background="@drawable/button_rounded"
        android:minHeight="48dp"
        android:paddingVertical="12dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

### Step 3: Add to AndroidManifest.xml
```xml
<activity
    android:name=".{Name}Activity"
    android:exported="false"
    android:screenOrientation="portrait" />
```

If launcher:
```xml
<activity
    android:name=".{Name}Activity"
    android:exported="true"
    android:screenOrientation="portrait">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### Step 4: Add Navigation (if needed)
```java
// From another Activity
Intent intent = new Intent(CurrentActivity.this, [Name]Activity.class);
startActivity(intent);
// Optional: finish(); if shouldn't return
```

### Step 5: Apply Bottom Navigation (if needed)
```xml
<!-- Add to layout above -->
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottom_navigation"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/runova_secondary"
    app:itemIconTint="@color/runova_text"
    app:itemTextColor="@color/runova_text"
    app:menu="@menu/bottom_nav_menu"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintStart_toStartOf="parent" />
```

```java
// In onCreate
BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
bottomNav.setSelectedItemId(R.id.nav_[name]);

bottomNav.setOnNavigationItemSelectedListener(item -> {
    switch (item.getItemId()) {
        case R.id.nav_home:
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
            return true;
        case R.id.nav_target:
            startActivity(new Intent(this, TargetActivity.class));
            overridePendingTransition(0, 0);
            return true;
        case R.id.nav_analytics:
            startActivity(new Intent(this, AnalyticsActivity.class));
            overridePendingTransition(0, 0);
            return true;
        case R.id.nav_profile:
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            return true;
    }
    return false;
});
```

---

## Output

Complete set of files:
1. `[Name]Activity.java` - full Activity class
2. `activity_[name].xml` - layout file
3. AndroidManifest.xml entry
4. Navigation code (if applicable)
5. Bottom nav setup (if applicable)

---

## Checklist

- [ ] Activity class created with proper package
- [ ] Layout XML created with RUNOVA colors
- [ ] IDs match between XML and findViewById calls
- [ ] Manifest entry added
- [ ] Navigation implemented (if needed)
- [ ] Bottom nav added (if Home/Target/Analytics/Profile)
- [ ] Consistent spacing (8/16/24dp grid)
- [ ] Text readable (white on dark navy)
- [ ] Touch targets ≥ 48dp
- [ ] ScrollView added if long content (Sign Up, Questionnaire)

---

## Example Usage

**User:** "Create a Welcome screen with a logo and START button"

**AI Response:**
Creates:
- `MainActivity.java` with START button listener
- `activity_main.xml` with centered logo and button
- Manifest entry as launcher
- Intent to SignupActivity on button click

**User:** "Create Analytics screen with bottom navigation"

**AI Response:**
Creates:
- `AnalyticsActivity.java` with bottom nav setup
- `activity_analytics.xml` with progress cards and bottom nav bar
- Manifest entry
- Bottom nav navigation logic to other screens

---

## Common Variations

### With ScrollView (for long forms)
```xml
<ScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">
    
    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="24dp">
        <!-- Content -->
    </androidx.constraintlayout.widget.ConstraintLayout>
    
</ScrollView>
```

### With Card Container
```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp"
    app:cardBackgroundColor="@color/runova_primary">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <!-- Card content -->
    </LinearLayout>
    
</androidx.cardview.widget.CardView>
```

---

## Notes

- Always use ConstraintLayout as root (or inside ScrollView)
- Match XML IDs to Java variable names (camelCase)
- Apply RUNOVA colors from colors.xml (no raw hex)
- Test on small phones (320dp width) - add ScrollView if needed
- Keep screen on for timer screens: `getWindow().addFlags(FLAG_KEEP_SCREEN_ON)`
- Portrait orientation only (specified in manifest)

---

**End of Android Screen Builder Skill**

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
