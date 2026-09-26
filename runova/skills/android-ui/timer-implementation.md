# Android Timer Implementation Skill

**Version:** 1.0  
**Category:** Android UI Development  
**Trigger:** "implement countdown timer" or "add session timer"

---

## Purpose

Implement a robust countdown timer that survives screen rotation, app backgrounding, and provides proper user feedback for RUNOVA training sessions.

---

## When to Use

- Target screen session timer
- Any countdown functionality
- Need to persist timer state across lifecycle events
- Want beep/vibrate notification at completion

---

## Implementation

### Step 1: Add Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### Step 2: Activity Setup
```java
public class TargetActivity extends AppCompatActivity {
    
    private TextView timerText;
    private Button startButton;
    private Button pauseButton;
    private Button completeButton;
    
    private CountDownTimer timer;
    private long timerEndTime = 0;
    private long remainingTime = 0;
    private boolean timerRunning = false;
    
    private DbHelper dbHelper;
    private MediaPlayer beepSound;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);
        
        dbHelper = new DbHelper(this);
        
        initViews();
        setupListeners();
        
        // Check if timer was running
        resumeTimerIfRunning();
    }
    
    private void initViews() {
        timerText = findViewById(R.id.timer_text);
        startButton = findViewById(R.id.start_button);
        pauseButton = findViewById(R.id.pause_button);
        completeButton = findViewById(R.id.complete_button);
        
        pauseButton.setVisibility(View.GONE);
        completeButton.setEnabled(false);
    }
    
    private void setupListeners() {
        startButton.setOnClickListener(v -> startTimer());
        pauseButton.setOnClickListener(v -> pauseTimer());
        completeButton.setOnClickListener(v -> completeSession());
    }
    
    private void startTimer() {
        // Get session duration (e.g., 30 minutes)
        int durationMinutes = getDurationForToday(); // From your plan
        long durationMillis = durationMinutes * 60 * 1000;
        
        startTimerWithDuration(durationMillis);
    }
    
    private void startTimerWithDuration(long durationMillis) {
        // Save end time
        timerEndTime = System.currentTimeMillis() + durationMillis;
        dbHelper.saveLong("timerEndTime", timerEndTime);
        
        // Create timer
        timer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }
            
            @Override
            public void onFinish() {
                onTimerComplete();
            }
        }.start();
        
        timerRunning = true;
        
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Update UI
        startButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }
    
    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
        }
        
        timerRunning = false;
        
        // Clear end time (paused state)
        dbHelper.saveLong("timerEndTime", 0);
        
        // Update UI
        pauseButton.setVisibility(View.GONE);
        
        // Create resume button
        Button resumeButton = findViewById(R.id.resume_button);
        resumeButton.setVisibility(View.VISIBLE);
        resumeButton.setOnClickListener(v -> resumeTimer());
        
        // Remove keep screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    private void resumeTimer() {
        startTimerWithDuration(remainingTime);
        
        Button resumeButton = findViewById(R.id.resume_button);
        resumeButton.setVisibility(View.GONE);
    }
    
    private void resumeTimerIfRunning() {
        long endTime = dbHelper.getLong("timerEndTime", 0);
        
        if (endTime > System.currentTimeMillis()) {
            // Timer was running
            long remaining = endTime - System.currentTimeMillis();
            startTimerWithDuration(remaining);
        } else if (endTime > 0) {
            // Timer finished while away
            onTimerComplete();
        }
    }
    
    private void updateTimerDisplay(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 60000);
        int seconds = (int) ((millisUntilFinished % 60000) / 1000);
        
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }
    
    private void onTimerComplete() {
        timerText.setText("00:00");
        
        // Play beep
        playBeep();
        
        // Vibrate
        vibrate();
        
        // Enable complete button
        completeButton.setEnabled(true);
        
        // Clear saved timer
        dbHelper.saveLong("timerEndTime", 0);
        
        // Update UI
        startButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);
        
        // Remove keep screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        timerRunning = false;
    }
    
    private void playBeep() {
        try {
            if (beepSound == null) {
                beepSound = MediaPlayer.create(this, R.raw.beep); // Add beep.mp3 to res/raw/
                // Or use system sound:
                // beepSound = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            if (beepSound != null) {
                beepSound.start();
            }
        } catch (Exception e) {
            // Fallback: use system beep
            android.media.ToneGenerator toneGen = 
                new android.media.ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        }
    }
    
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
    }
    
    private void completeSession() {
        // Check if already completed today
        String today = DateUtils.today(); // yyyy-MM-dd
        Set<String> completedDates = dbHelper.getStringSet("completedDates", new HashSet<>());
        
        if (completedDates.contains(today)) {
            Toast.makeText(this, "Already complete for today", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save completion
        completedDates.add(today);
        dbHelper.saveStringSet("completedDates", completedDates);
        
        // Show success message
        Toast.makeText(this, "✓ Today's target complete", Toast.LENGTH_SHORT).show();
        
        // Disable button
        completeButton.setEnabled(false);
        completeButton.setText("Completed ✓");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Timer continues with saved end time
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (beepSound != null) {
            beepSound.release();
        }
    }
}
```

### Step 3: DbHelper Methods (timer state in `meta` table)
```java
public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(Context context) {
        super(context, "runova.db", null, 1);
    }
    
    public void saveLong(String key, long value) {
        getWritableDatabase().execSQL(
            "INSERT OR REPLACE INTO meta (key, value) VALUES (?, ?)",
            new Object[]{key, String.valueOf(value)});
    }
    
    public long getLong(String key, long defaultValue) {
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT value FROM meta WHERE key = ?", new String[]{key});
        long result = defaultValue;
        if (c.moveToFirst()) result = Long.parseLong(c.getString(0));
        c.close();
        return result;
    }
}
```

### Step 4: Layout XML
```xml
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/runova_background"
    android:padding="24dp">
    
    <!-- Timer Display -->
    <TextView
        android:id="@+id/timer_text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="30:00"
        android:textSize="72sp"
        android:textStyle="bold"
        android:textColor="@color/runova_text"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintVertical_bias="0.4" />
    
    <!-- Hint Text -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Keep the app open during your session"
        android:textSize="14sp"
        android:textColor="@color/runova_text_secondary"
        app:layout_constraintTop_toBottomOf="@id/timer_text"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        android:layout_marginTop="16dp" />
    
    <!-- Start Button -->
    <Button
        android:id="@+id/start_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Start Today's Session"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/runova_text"
        android:background="@drawable/button_rounded"
        android:minHeight="48dp"
        android:paddingVertical="12dp"
        app:layout_constraintBottom_toBottomOf="parent"
        android:layout_marginBottom="80dp" />
    
    <!-- Pause Button -->
    <Button
        android:id="@+id/pause_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Pause"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/runova_text"
        android:background="@drawable/button_rounded"
        android:minHeight="48dp"
        android:paddingVertical="12dp"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        android:layout_marginBottom="80dp" />
    
    <!-- Resume Button -->
    <Button
        android:id="@+id/resume_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Resume"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/runova_text"
        android:background="@drawable/button_rounded"
        android:minHeight="48dp"
        android:paddingVertical="12dp"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        android:layout_marginBottom="80dp" />
    
    <!-- Complete Button -->
    <Button
        android:id="@+id/complete_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Complete Session"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/runova_text"
        android:background="@drawable/button_rounded_success"
        android:minHeight="48dp"
        android:paddingVertical="12dp"
        android:enabled="false"
        app:layout_constraintBottom_toBottomOf="parent"
        android:layout_marginBottom="8dp" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## Key Features

1. **Persists across lifecycle** - saves end time, recalculates remaining
2. **Keep screen on** - uses FLAG_KEEP_SCREEN_ON while running
3. **Beep and vibrate** - notifies user at completion
4. **Pause/Resume** - allows user to take breaks
5. **Finish early** - Complete button can be enabled before timer ends
6. **No double counting** - checks if today already completed

---

## Important Notes

- Timer only works while app is open (known limitation for RUNOVA)
- If app closes, timer stops (show hint: "Keep the app open")
- Saved end time allows resume after screen rotation or brief backgrounding
- Complete button unlocks at 00:00 OR can enable early for "Finish early" option

---

## Checklist

- [ ] Permissions added to manifest (VIBRATE)
- [ ] Timer saves end time on start
- [ ] Timer resumes on activity resume if still running
- [ ] Keep screen on while timer runs
- [ ] Beep and vibrate at zero
- [ ] Complete button unlocks at zero
- [ ] Completion saves today's date
- [ ] No double counting (check if today already in completedDates)
- [ ] UI updates properly (Start → Pause → Complete)
- [ ] Timer display updates every second (MM:SS format)

---

**End of Android Timer Implementation Skill**

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
