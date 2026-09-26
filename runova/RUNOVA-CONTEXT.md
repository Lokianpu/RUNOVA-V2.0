# RUNOVA Project Context

**Project:** RUNOVA V6 — Offline Android Training & Progress Tracker  
**Purpose:** Context file for AI agents working on RUNOVA  
**Last Updated:** 2026-09-25 (activity-only task model)

---

## Project Identity

**Name:** RUNOVA  
**Category:** Health & Fitness  
**Platform:** Android (Java, Android Studio)  
**Status:** Active development (academic project)

---

## Vision & Goals

### What RUNOVA Is
An offline Android app that gives runners one daily target matched to their level and event, provides an in-app timer for sessions, and tracks consistency without requiring internet. Every target and task is an activity (warm-up, run, workout, cool-down, mobility) — nutrition, food, water/hydration, sleep and meditation tasks do not exist in RUNOVA.

### What RUNOVA Is Not
- Not a GPS/distance/speed tracker
- Not a professional coaching service
- Not medical advice
- Not cloud-based or online
- Not for advanced athletes with complex training needs

### Core User Journey
```
First launch → Start-up popup → Sign Up → Questionnaire (Level + Event)
→ Home shows Today's Target → Start timer → Complete session
→ Track progress (weekly, level cycle, streak)
→ Level-up dialog after 28 days → Move up or Repeat
→ Edit profile to adjust plan
```

### Success Criteria
A user can:
1. Sign up and pick a level without confusion
2. See a clear daily target every day
3. Use the timer for their session
4. Track their progress and streaks
5. Be offered a level-up after 28 days
6. Edit their profile and see the plan adjust
7. Do all of this without internet (airplane mode)

---

## Technical Architecture

### Tech Stack
| Component | Choice | Reason |
|-----------|--------|--------|
| Language | Java | Course requirement |
| IDE | Android Studio | Standard Android development |
| Storage | SQLite via `DbHelper` (tables: profile, routines, tasks, history/meta) | Offline, relational queries for routines/history |
| Navigation | Intent (7 Activities) | Course requirement (5+ Activities) |
| UI Layout | ConstraintLayout + ScrollView | Responsive, flexible |
| Timer | CountDownTimer | Built-in, reliable |
| Charts | MPAndroidChart (only external dependency, offline) | Analytics visualization |
| Dependencies | None besides MPAndroidChart | Offline requirement, simplicity |

### Architecture Pattern: MVC

**Model (Data):**
- `Profile` - user identity, body data, training config (incl. chosen goal)
- `DailyTarget` - target name, type, effort, minutes, activities
- `LevelPlan` - weekly plans for 3 levels × 4 weeks
- `DbHelper` - SQLite storage wrapper (profile, routines, tasks, history/meta)
- `DateUtils` - date/age calculations

**View (UI):**
- 7 Activities + layouts
- Bottom navigation (Home, Target, Analytics, Profile)
- Design system (RUNOVA theme)

**Controller (Logic):**
- `getDailyTarget()` - plan calculation
- Validation methods
- Status calculation (Finished/Missed/Pending)
- Timer management
- Level-cycle check

### Data Flow Philosophy
**Stored in SQLite:**
1. User profile (first/last name, DOB, body, weekly minutes, event, level, chosen goal)
2. Generated routine: `routines` + `tasks` rows with a `profile_snapshot` JSON
3. Completion history (dates + status) and active timer state (end time)

**Regeneration:** written at sign-up and regenerated whenever the user saves an edited profile — old routine, tasks, and history are deleted first (clean start, level cycle resets).

**Calculated from stored data:**
- Status ← completion history vs. calendar
- Streak ← consecutive completed days ending today/yesterday
- Progress ← completed days in current 28-day cycle
- Level-up eligibility ← days completed / 28 ≥ 0.85

**Why:** every questionnaire/sign-up answer actively shapes the stored routine; editing the profile regenerates it, so targets are never stale.

---

## Activities & Navigation

```
MainActivity (Welcome, launcher)
    ↓ (first time)
    Start-up popup (once, seenIntro flag)
    ↓
    START button
    ↓ (if no profile)
SignupActivity (name, DOB, gender)
    ↓
QuestionnaireActivity (body, training, event, level)
    ↓
HomeActivity (greeting, week strip, Today's Target, progress)
    ↔ TargetActivity (timer, complete session)
    ↔ AnalyticsActivity (weekly progress, level progress, streak)
    ↔ ProfileActivity (view/edit profile, About)

Bottom nav: Home ↔ Target ↔ Analytics ↔ Profile
```

### Navigation Rules
1. Welcome → Sign Up: call `finish()` so Back doesn't return
2. If profile exists, Welcome → Home directly
3. Bottom nav uses Intent with no animation (`overridePendingTransition(0, 0)`)
4. Edit Profile → Home (not back to Profile)
5. All Activities declared in `AndroidManifest.xml`

---

## Design System

### Visual Identity
- **Mood:** Focused, motivating, professional (not playful)
- **Primary color:** Bright blue (#1683E8) — energy, clarity
- **Background:** Dark navy (#07152E) — focus, reduce eye strain
- **Success:** Green (#39C84A) — achievement, completion
- **Style:** Rounded cards, clean typography, high contrast

### Color Palette
```java
runova_background  #07152E  // Dark navy (all screens)
runova_primary     #1683E8  // Bright blue (buttons, links, active)
runova_secondary   #0D5DB7  // Secondary blue (borders, inactive)
runova_success     #39C84A  // Green (completed status)
runova_text        #FFFFFF  // White (main text)
runova_text_secondary #B8C4D6 // Light gray (secondary text, hints)
runova_error       #FF4444  // Red (errors, missed days)
```

### Typography Scale
```
Heading:    24sp, bold, white
Subheading: 18sp, medium, white
Body:       16sp, regular, white
Caption:    14sp, regular, light gray
Button:     16sp, bold, white
```

### Spacing Grid
```
Tight:    8dp   (within cards, between related items)
Default:  16dp  (card padding, between sections)
Loose:    24dp  (screen margins, major sections)
```

### Component Library

**Card:**
```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp"
    app:cardBackgroundColor="@color/runova_primary">
    <!-- Content with 16dp padding -->
</androidx.cardview.widget.CardView>
```

**Button (primary):**
```xml
<Button
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/button_rounded"
    android:text="Start Today's Session"
    android:textColor="@color/runova_text"
    android:textSize="16sp"
    android:textStyle="bold"
    android:paddingVertical="12dp"
    android:minHeight="48dp" />
```

**Status badge:**
```
✓ = Finished (green circle)
X = Missed (red circle)
• = Pending (gray circle)
```

**Progress bar:**
```xml
<ProgressBar
    style="?android:attr/progressBarStyleHorizontal"
    android:layout_width="match_parent"
    android:layout_height="8dp"
    android:progressTint="@color/runova_success"
    android:progressBackgroundTint="@color/runova_text_secondary" />
```

---

## Training Plans (Domain Logic)

### Research Basis
- **Beginner:** NHS Couch to 5K (run/walk intervals, 3 days/week)
- **Intermediate:** Active.com, Age UK plans (4 runs/week, tempo + intervals)
- **Pro:** Run247, 80/20 approach (5 runs/week, two quality sessions)
- **Effort:** CDC talk test (no heart rate monitor needed)

### Three Levels

**Beginner — Build Foundation:**
- 3 run/walk days (Mon/Wed/Fri)
- Basic strength (Tue)
- Recovery (Thu)
- Cross-training (Sat)
- Rest (Sun)
- Progresses from 1-min run / 1.5-min walk → 5-min run / 2-min walk over 4 weeks

**Intermediate — Improve Performance:**
- 4 run days (Tue Easy, Wed Tempo, Fri Interval, Sun Long)
- Strength (Thu)
- Cross-training (Sat)
- Rest (Mon)
- Tempo/interval/long run ramps over 4 weeks, lighter week 4

**Pro — Competition Performance:**
- 5 run days (Tue Interval, Wed Easy+Core, Thu Tempo, Sat Easy+Strides, Sun Long)
- Strength (Fri)
- Rest (Mon)
- Higher volume, two quality sessions, most time at Easy effort (80/20)

### Training Goals (user-selected, one merged set)
The Questionnaire Goal spinner offers six options in one list:
```text
Learn the Basics · Build and Hold Endurance · Run Faster and Race
Build Foundation · Improve Performance · Competition Performance
```
The selected goal is stored on the profile, displayed as the user's goal, and shapes the session mix together with event + level. (Level still selects the volume tier; event selects the workout library.)

### Event Workout Library (Event × Level, predefined)
Predefined workouts selected by `Event + Level` — 5 event rows + General Running fallback:
- **Sprint:** Beginner (warm-up, drills, 4×50m easy sprints) · Intermediate (6×100m intervals, strength) · Pro (race-pace intervals, explosive drills)
- **Middle Distance:** Beginner (10–15 min endurance) · Intermediate (20 min + intervals) · Pro (race-pace intervals)
- **Long Distance:** Beginner (15 min easy + walking recovery) · Intermediate (30 min + light intervals) · Pro (long endurance + race pace)
- **Hurdles:** Beginner (mobility + technique drills) · Intermediate (technique + sprint intervals) · Pro (advanced drills, race-pace)
- **Relay:** Beginner (exchange practice, short runs) · Intermediate (acceleration + exchange) · Pro (explosive starts, race-pace)
- **General Running:** fallback to the level's weekly plan with generic running tasks
A fallback workout is mandatory so no combination can crash the app.

### Daily Goal Hierarchy (7-day cycle)
```text
Level → General Goal (user-selected) → Weekly Focus → Today's Goal → Today's Activity
```
7-day rotation: Mon Technique · Tue Endurance · Wed Speed/Quality · Thu Strength & Stability · Fri Event Practice · Sat Weekly Performance · Sun Recovery. Goal text is customized by event (e.g. "Improve Sprint Form") and worded by level (Beginner: Learn/Build · Intermediate: Improve/Control · Pro: Refine/Prepare). Simple Java (`getDailyGoal(event, level, dayOfWeek)` via `Calendar.dayOfWeek`) — no AI, no internet. Sunday is always a recovery goal. Every day (including recovery) has a completion state; a day can only be completed once (`lastCompletedDate` guard).

### Effort Labels (by talk test)
- **Easy:** Can speak full sentences comfortably
- **Tempo:** Can speak short sentences, comfortably hard
- **Hard:** Only a few words, pushing pace

### Duration Bands
Weekly running minutes → adjusts non-running day durations:
- **Low:** <60 min → shorter recovery/strength days
- **Mid:** 60-150 min → default durations
- **High:** >150 min → longer recovery/strength days

---

## Data Model

### SQLite Schema (file: `runova.db`, wrapper: `DbHelper`)
```
profile        single row: seenIntro, firstName, lastName, dob, gender,
               height, weight, weeklyMinutes, event, level, goal,
               levelStartDate, signupDate
routines       generated routine rows with profile_snapshot JSON
tasks          routine task rows (day, orderIndex, title, type, minutes, status) — activity tasks only
history        completion records: yyyy-MM-dd + status (Finished/Missed/Pending),
               lastCompletedDate guard against double counting
meta           timerEndTime (long, Unix ms)
```
`profile` field notes: `event` = Sprint/Middle Distance/Long Distance/Hurdles/Relay/General Running; `level` = Beginner/Intermediate/Pro; `goal` = one of the six merged goal options; `dob` yyyy-MM-dd; age derived, never stored.

### Profile Model
```java
public class Profile {
    public String firstName;
    public String lastName;
    public String dob;         // yyyy-MM-dd
    public String gender;
    public double height;      // cm
    public double weight;      // kg
    public int weeklyMinutes;
    public String event;
    public String level;
    public String goal;          // user-selected, one of six merged options
    public String levelStartDate;
    public String signupDate;
    
    // Derived (not stored)
    public int getAge() {
        return DateUtils.ageFrom(dob);
    }
    
    public String getTrainingGoal() {
        return goal;  // user-selected in Questionnaire; stored on profile
    }
    
    public String getDurationBand() {
        if (weeklyMinutes < 60) return "low";
        if (weeklyMinutes <= 150) return "mid";
        return "high";
    }
}
```

### DailyTarget Model
```java
public class DailyTarget {
    public String name;           // "Run/Walk Intervals", "Tempo Run", etc.
    public String type;           // "run", "strength", "recovery", "rest"
    public String effortLabel;    // "Easy", "Tempo", "Hard", "" (for rest)
    public int minutes;           // Timer duration
    public String[] activityLines; // Bullet list of what to do
    public String effortHint;     // Talk test explanation
}
```

**Task expansion:** each training day's target expands into **exactly 5** checkable activity tasks — `Warm-up`, three main tasks, `Cool-down` — and the rest day expands into **exactly 2** (easy walk, stretching). Tasks are stored as `tasks` rows with an `ordinal`, minutes, effort and a `steps` prescription string. Every task carries its own status and its own timer; the day is Finished only when all of its tasks are Finished. Warm-ups list 3-6 movements with reps or seconds; no nutrition, food, water/hydration, sleep or meditation task type exists (meditation is an open team item).

---

## Key Algorithms

Algorithm detail: see `context/ROUTINE-ENGINE.md` for the full day-plan to task expansion rules, ramps, event wording and status roll-up.

### Calculate Today's Target
```java
public static DailyTarget getDailyTarget(
    String level,           // "Beginner", "Intermediate", "Pro"
    String event,           // "Sprint", "Middle Distance", etc.
    int weekday,            // 1=Mon, 2=Tue, ..., 7=Sun
    int weekInLevel,        // 1-4 (current week in 28-day cycle)
    String durationBand     // "low", "mid", "high"
) {
    // 1. Get base target from LevelPlan[level][weekday]
    // 2. Adjust minutes based on weekInLevel (for ramping targets)
    // 3. Adjust non-running days based on durationBand
    // 4. Adjust wording based on event (Sprint → drills, Hurdles → technique)
    // 5. Add effort label and talk-test hint
    // 6. Return DailyTarget
}
```

### Calculate Week in Level
```java
public static int getWeekInLevel(String levelStartDate) {
    long daysSince = DateUtils.daysBetween(levelStartDate, DateUtils.today());
    int week = (int) (daysSince / 7) + 1;
    return Math.min(week, 4); // Cap at 4
}
```

### Calculate Status
```java
public static String getStatus(String date, String signupDate, Set<String> completedDates) {
    if (completedDates.contains(date)) {
        return "Finished";
    }
    if (date.equals(DateUtils.today())) {
        return "Pending";
    }
    if (DateUtils.isBefore(date, DateUtils.today()) && 
        !DateUtils.isBefore(date, signupDate)) {
        return "Missed";
    }
    return "Future";
}
```

### Calculate Streak
```java
public static int getCurrentStreak(Set<String> completedDates) {
    String today = DateUtils.today();
    String yesterday = DateUtils.addDays(today, -1);
    
    // Streak must include today or yesterday
    if (!completedDates.contains(today) && !completedDates.contains(yesterday)) {
        return 0;
    }
    
    int streak = 0;
    String date = completedDates.contains(today) ? today : yesterday;
    
    while (completedDates.contains(date)) {
        streak++;
        date = DateUtils.addDays(date, -1);
    }
    
    return streak;
}
```

### Check Level Cycle
```java
public static void checkLevelCycle(Context context, Profile profile, Set<String> completedDates) {
    long daysSince = DateUtils.daysBetween(profile.levelStartDate, DateUtils.today());
    
    if (daysSince >= 28) {
        int completed = countCompletedInCycle(profile.levelStartDate, completedDates);
        double passRate = completed / 28.0;
        
        showLevelUpDialog(context, profile.level, completed, passRate >= 0.85);
    }
}
```

---

## Validation Rules

### Sign Up
```java
firstName:  trim, not empty
lastName:   trim, not empty
dob:        valid date, not future, age 13-100
gender:     selected (not "Select Gender")
```

### Questionnaire
```java
height:         positive decimal, 100-250 cm (suggested)
weight:         positive decimal, 25-250 kg (suggested)
weeklyMinutes:  integer ≥ 0, < 1000 (suggested cap)
event:          selected (not "Select Event")
level:          selected (not "Select Level")
goal:           selected (not "Select Goal")
```

### Validation Behavior
- Show error beside field: `inputField.setError("message")`
- Or show Toast for Spinner: `Toast.makeText(...).show()`
- Do NOT advance until valid
- Keep entered values on error
- Wrap number parsing in try/catch to prevent crashes

### Example
```java
try {
    double height = Double.parseDouble(heightInput.getText().toString());
    if (height <= 0 || height > 300) {
        heightInput.setError("Enter a valid height");
        return false;
    }
} catch (NumberFormatException e) {
    heightInput.setError("Enter a number");
    return false;
}
```

---

## Constraints & Limitations

### Hard Constraints (Cannot Change)
1. **Offline only** — no internet, Firebase, cloud, APIs, push/server notifications
2. **No GPS** — no distance, speed, location tracking
3. **Dependencies** — default Android Studio libraries + MPAndroidChart only (offline)
4. **7 Activities** — course requirement (5+ interconnected)
5. **Java + Android Studio** — course requirement
6. **Intent navigation** — course requirement

### Design Constraints
1. **No profile pictures** — simplicity, offline
2. **Charts** — MPAndroidChart approved for Analytics (weekly bar chart, cycle progress)
3. **Local notifications allowed** — device-local scheduled reminders for activity tasks (warm-up, session, workout); no push/server. Implementation approach to be decided in a dedicated session — keep it simple
4. **Background scheduling allowed** — AlarmManager/NotificationManager for scheduled activity-task reminders; exact approach TBD in separate session
5. **Trust-based tracking** — user can mark complete without doing it
6. **No cloud backup** — uninstall loses data
7. **Activity-only tasks** — every task in the routine is an activity (warm-up, run, interval, tempo, long run, strength/workout, cross-training, cool-down, mobility/recovery). Nutrition, food, water/hydration, sleep and meditation tasks are excluded; meditation is an open team item in `context/TRAINING-PLANS.md` §15
8. **Task count** — exactly 5 tasks per training day (warm-up, three main tasks, cool-down) and exactly 2 on the rest day (easy walk, stretching)
9. **Warm-up prescription** — 3-6 movements per warm-up, each with reps or seconds; cool-down is an easy walk plus static stretches of 15-20 s

### Known Limitations (Accepted)
1. Timer beep may not sound if app closed/phone locked
2. Users can cheat (mark complete without doing session)
3. No distance/pace/heart-rate verification
4. No coaching beyond pre-written plans
5. Not suitable for advanced athletes with custom training needs

---

## Build Priority Order

### Phase 1: Foundation (Get Something Working)
1. Create DbHelper, Profile, DateUtils
2. Create LevelPlan, Event workout library, and routine generator with fallback
3. Test getDailyTarget() with hardcoded values
4. Create MainActivity (Welcome) with start-up popup
5. Create SignupActivity with validation
6. Create QuestionnaireActivity with validation

### Phase 2: Core Loop (Daily Target Flow)
7. Create HomeActivity with Today's Target card
8. Create TargetActivity with Start/Complete (no timer yet)
9. Implement status calculation (Finished/Missed/Pending)
10. Test completing a day (save date, check no double-counting)
11. Add week strip to Home (7 badges showing status)

### Phase 3: Timer & Progress
12. Add CountDownTimer to TargetActivity
13. Implement timer pause/resume and saved end time
14. Create AnalyticsActivity (weekly progress, level progress, streak)
15. Test timer survives leaving/returning to screen

### Phase 4: Level Cycle & Profile
16. Implement checkLevelCycle() and level-up dialog
17. Create ProfileActivity (view profile)
18. Add Edit Profile (reuse Sign Up/Questionnaire in EDIT mode)
19. Test level restrictions (can only move down in Edit)
20. Test plan adjusts immediately after editing profile

### Phase 5: Polish & Testing
21. Apply consistent theme (colors.xml, styles.xml)
22. Add rounded corners, proper spacing, icons
23. Test on small phone (wrap forms in ScrollView)
24. **Airplane-mode test** (full flow without internet)
25. Validation test (try to break each form)
26. Navigation test (Back button behavior)

---

## Testing Scenarios

### Critical Path (Airplane Mode)
```
1. Open app (first time) → popup shows
2. Tap "I understand" → popup closes
3. Tap START → Sign Up screen
4. Fill valid data → Next works
5. Invalid data → errors show, stay on screen
6. Complete Sign Up → Questionnaire
7. Select Level (Beginner), Event (Sprint) → Done works
8. Home shows → correct target for Monday + Beginner + Sprint
9. Tap Start Today's Session → Target screen
10. Tap Start → timer counts down
11. Leave app, return → timer still running
12. Wait for zero → beep/vibrate, Complete unlocks
13. Tap Complete → "Today's target complete" shows
14. Return to Home → today's day badge is ✓
15. Open Analytics → shows 1 Finished, progress bar updated
16. Open Profile → shows all saved data
17. Tap Edit Profile → form pre-filled
18. Change Event to Middle Distance → Save
19. Return to Home → target wording changed (no longer Sprint-specific)
```

### Edge Cases
```
1. Complete same day twice → second tap shows "Already complete", no duplicate
2. Level-up at 28 days (use test values: 3 days, 67% pass) → dialog appears
3. Move up → level changes, levelStartDate resets
4. Repeat → level stays, levelStartDate resets
5. Edit profile, change level from Intermediate to Beginner → works (level down)
6. Try to change level from Beginner to Pro → blocked or show message
7. Small phone (320dp width) → Sign Up and Questionnaire scroll
8. Invalid number input → error shows, no crash
9. Future DOB → error shows
10. Age 12 from DOB → error shows
```

### Validation Tests
```
- Empty first name → error
- Empty last name → error
- Future DOB → error
- DOB giving age 12 → error
- DOB giving age 101 → error
- Height "abc" → error, no crash
- Height -5 → error
- Weight 0 → error
- Weekly minutes -10 → error
- Event not selected → error/toast
- Level not selected → error/toast (but preselected, so rare)
```

---

## Common Issues & Solutions

### Issue: Timer stops when app closed
**Solution:** This is a known limitation. Show hint: "Keep the app open during your session." Use `FLAG_KEEP_SCREEN_ON` to reduce accidental closures.

### Issue: Day counted twice
**Solution:** Check if `completedDates.contains(today)` before adding. Show "Already complete" message on second tap.

### Issue: App crashes on invalid number input
**Solution:** Wrap all number parsing in try/catch. Show error, do not advance.

### Issue: Back button returns to Welcome after Sign Up
**Solution:** Call `finish()` in Welcome after starting Sign Up/Home.

### Issue: Layout doesn't fit small phones
**Solution:** Wrap Sign Up and Questionnaire in ScrollView. Test at 320dp width.

### Issue: Level-up dialog never appears
**Solution:** Check `levelStartDate` is saved. Use small test values (e.g., 3 days, 67% pass) for testing.

### Issue: Edit Profile doesn't adjust plan
**Solution:** Plan is calculated from profile on demand. Check `getDailyTarget()` is called in onResume() or onCreate() of HomeActivity.

### Issue: Colors inconsistent
**Solution:** Use `@color/runova_*` references in XML, never raw hex. Define all colors in colors.xml.

---

## Glossary

**Activity:** Android screen class (e.g., `HomeActivity.java`)  
**Intent:** Android navigation mechanism  
**SQLite:** Android built-in relational database (offline, persists across sessions; wrapper `DbHelper`)  
**Activity task:** One checkable activity step inside a day (warm-up, run, workout, cool-down, mobility); a training day holds exactly 5 of them and the rest day holds 2  
**Warm-up Catalogue:** The fixed list of warm-up movements with reps or seconds per movement, capped at 6 (`context/TRAINING-PLANS.md` §7)  
**Cool-down Catalogue:** The fixed end-of-session walk plus static stretches of 15-20 s (`context/TRAINING-PLANS.md` §8)  
**Target:** Daily training session (e.g., "Run/Walk Intervals", "Tempo Run")  
**Effort:** Intensity level (Easy, Tempo, Hard) judged by talk test  
**Status:** Day completion state (Finished, Missed, Pending)  
**Streak:** Consecutive Finished days ending today or yesterday  
**Level cycle:** 28-day training period at one level  
**Pass rate:** 85% (24/28 days) required to be offered level-up  
**Duration band:** low/mid/high, adjusts non-running day lengths based on weekly minutes  
**Event:** Running specialty (Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running — 6 spinner options)  
**Level:** Training difficulty (Beginner, Intermediate, Pro)  
**Training Goal:** User-selected from one merged six-option set (Learn the Basics, Build and Hold Endurance, Run Faster and Race, Build Foundation, Improve Performance, Competition Performance)  

---

**End of RUNOVA Context — Use this as reference for all development decisions**
