# RUNOVA — UI & Navigation Design

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8**

---

## Screens Overview

Eight Activities, all navigated with `Intent`.

| Activity | Layout | Purpose | Access |
|---|---|---|---|
| `MainActivity` | `activity_main.xml` | Welcome screen, start-up pop-up on first launch | Launcher |
| `SignupActivity` | `activity_signup.xml` | Identity collection | From Main |
| `QuestionnaireActivity` | `activity_questionnaire.xml` | Body, training, event, level, goal | From Signup |
| `HomeActivity` | `activity_home.xml` | Greeting, date header, notifications bell, task list, level progress | From Questionnaire, bottom nav |
| `TargetActivity` | `activity_target.xml` | Task list with timer/completion controls | Bottom nav |
| `AnalyticsActivity` | `activity_analytics.xml` | Weekly progress, level progress, streak, counts, charts | Bottom nav |
| `ProfileActivity` | `activity_profile.xml` | Identity + questionnaire fields, editable | Bottom nav |
| `NotificationsActivity` | `activity_notifications.xml` | In-app message list (level-up offer) | Bell icon from Home only |

---

## Navigation Flow

```text
MainActivity (START)
    ↓
SignupActivity → QuestionnaireActivity → HomeActivity
                                              ↓
                                    ┌─────────┼─────────┐
                                    ▼         ▼         ▼
                              TargetActivity  AnalyticsActivity  ProfileActivity
                                    ↑
                                    │ (bell icon from Home only)
                                    ▼
                            NotificationsActivity
```

**Bottom navigation bar** shared by: Home, Target, Analytics, Profile

**Navigation rules:**
- Declare every Activity in `AndroidManifest.xml`; `MainActivity` is the launcher
- Call `finish()` when moving forward from Welcome so Back does not return to it
- If saved profile exists, START goes straight to Home

---

## Screen Details

### MainActivity (Welcome)

**Layout:**
- Logo
- START button
- Start-up pop-up on first launch (once only, controlled by `seenIntro` flag)

**Behavior:**
- If no saved profile: show start-up pop-up, then navigate to Signup
- If saved profile exists: navigate directly to Home
- Call `finish()` after navigation so Back does not return here

---

### Start-up Pop-up

**Shown once on first launch**, before Sign Up. Reopenable from Profile ("About RUNOVA").

**Content:**
> **Before you start 👟**
> Hello, runner! RUNOVA is an offline training guide made for a school project. It gives you a daily set of tasks, but it can't track your distance, speed or heart rate. Nothing here is automatic.
> That means you *could* tap "Done" without doing the task, and nobody will stop you. But real results only come from actually doing each task, so follow the plan and give it your best. Listen to your body: if something hurts or you feel unwell, stop and rest. If you have any health concerns, talk to a doctor before you begin.
> It's your choice: the shortcut or the real thing. Good luck on your journey!
> **[ I understand, let's go ]**

---

### SignupActivity (Identity)

**Fields:**
- First name (text input)
- Last name (text input)
- Date of birth (date picker)
- Gender (selection: Male, Female, Other)

**Validation:**
- First name: not empty
- Last name: not empty
- Date of birth: valid date, not in future, derived age 13–100
- Gender: must be selected

**Behavior:**
- Show error beside field (`setError`) or Toast on validation failure
- Do not advance; keep entered values
- Age derived from dateOfBirth using `DateUtils.ageFrom()`, shown on Profile
- Never ask age directly

---

### QuestionnaireActivity (Training Configuration)

**Fields:**
- Height (decimal input, cm)
- Weight (decimal input, kg)
- Event (Spinner: Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running)
- Level (Spinner: Beginner, Intermediate, Pro) — **Beginner preselected**
- Goal (Spinner: Learn the Basics / Build and Hold Endurance / Run Faster and Race / Build Foundation / Improve Performance / Competition Performance)

**Validation:**
- Height: positive (100–250 cm)
- Weight: positive (25–250 kg)
- Event: must be selected
- Level: must be selected
- Goal: must be selected

**Hint under Level:** *"Not sure? Start with Beginner. You can move up later."*

**Behavior:**
- Wrap number parsing in try/catch; invalid input must never crash
- Show validation errors beside field or as Toast
- Do not advance on validation failure

**What each field affects:**
- Height: display only
- Weight: display only
- Event: changes interval/technique task wording and instructions
- Level: weekly task pattern, warm-up/cool-down length, daily duration
- Goal: display label only — does not affect task generation

---

### HomeActivity (Daily Overview)

**Layout (top to bottom):**
1. Greeting with user's first name
2. Current date header (e.g., "Saturday, September 26")
3. Notifications bell icon (top-right)
4. Today's task list (read-only view, links to Target for interaction)
5. Level progress indicator (`X / 28` days in current cycle)

**Task list display:**
- Each task shows: name, duration, status badge (Finished/Pending/Missed)
- Tap task to navigate to Target for timer/completion controls

**Notifications bell:**
- Shows badge/indicator if unresolved notifications exist
- Tap to open NotificationsActivity

**Level progress:**
- Simple progress bar or text: "Day X of 28"
- Visual indicator if near level-up (e.g., at day 28)

---

### TargetActivity (Task Execution)

**Layout:**
- Today's task list with interactive controls
- Each task shows: name, duration, Start/Mark Done button, timer display

**Timer behavior:**
1. **Only one task timer can run at a time app-wide**
2. Starting new timer while another active → blocked in UI with message: "Finish your current task first"
3. Store single `activeTaskId` + `activeTaskEndTime` in `profile` table
4. Recalculate remaining time from stored endTime when screen reopens
5. Keep screen on while timer runs
6. Beep and vibrate at zero
7. **Complete** button unlocks at zero; **Finish early** allowed
8. Short tasks (rest-day Easy Walk/Stretch) can use Mark-as-Done instead of timer

**Task completion:**
- Completing task saves status to SQLite
- Can't be counted twice
- Day shows Finished only when every task for that date is Finished

---

### AnalyticsActivity (Progress Tracking)

**Layout (top to bottom):**

1. **Level progress card**
   - `X / 28` days Finished in current cycle
   - ProgressBar visual
   - Color shift when crosses 85% pass line (e.g., gray → green)

2. **Streak card**
   - Current streak (consecutive fully-Finished days)
   - Number with simple icon

3. **Status counts**
   - Finished / Missed / Pending totals
   - Scoped to current 28-day cycle

4. **Time-range chart**
   - 3-tab selector: `Week | Month | Year`
   - Single BarChart view below tabs
   - Chart updates when tab switched

**Chart data by tab:**

| Tab | Bars | Bar value |
|---|---|---|
| Week (default) | 7 bars (Mon–Sun current week) | Tasks Finished that day (0–5) |
| Month | 4–5 bars (weeks of current month) | Total tasks Finished that week |
| Year | 12 bars (months of current year) | % of days fully Finished that month |

**Chart implementation:**
- Uses MPAndroidChart library
- All data from existing `tasks` table (no new tables)
- Computed on demand when Analytics opens or tab tapped

---

### ProfileActivity (User Settings)

**Layout:**
- Display/edit all identity fields from Signup
- Display/edit all training configuration from Questionnaire
- Age shown (derived from dateOfBirth, not editable directly)
- "About RUNOVA" button to reopen start-up pop-up
- No profile picture

**Editable fields:**
- First name, Last name, Date of birth, Gender
- Height, Weight, Event, Level, Goal

**Validation:**
- Same rules as Signup and Questionnaire
- Show errors beside field or as Toast
- Do not save invalid input

**Behavior:**
- Editing most fields: save to SQLite; next lazy-generation uses new values automatically
- **Exception: Changing Level** → wipes all tasks and starts clean immediately
- Age recalculated from dateOfBirth on save

---

### NotificationsActivity (In-app Inbox)

**Layout:**
- List of in-app messages
- Currently used for: level-up offer only

**Message display:**
- Type icon/badge
- Message text
- Created timestamp
- Action buttons (if applicable)

**Level-up offer message:**
- Shown if dialog dismissed at day 28 without choice
- Message text depends on pass rate:
  - Passed (≥85%): options to *Move up to [next level]* or *Repeat [level]*
  - Below 85%: option to *Repeat [level]* only
  - Pro: options to *Stay at Pro* or *Repeat*
- Action buttons call `applyLevelChoice(choice)` — same function as dialog
- Message marked resolved after choice made

**Behavior:**
- Only one LEVEL_UP_OFFER row at a time (check before inserting)
- Resolved messages removed from list or shown as completed

---

## Design Guidelines

### Positioning & Labels

Use neutral, educational labels:
- "Today's Tasks" not "Your Workout"
- "Suggested Activity" not "Required Training"
- "Recovery Day" not "Rest Day"
- "Track Your Progress" not "Your Stats"

### Validation UI

- Show error beside field using `setError` or clear Toast
- Do not advance on validation failure
- Keep entered values visible
- Never crash on invalid number input (use try/catch)

### Bottom Navigation Bar

**Shared by:** Home, Target, Analytics, Profile

**Icons/Labels:**
- Home: house icon, "Home"
- Target: target icon, "Target"
- Analytics: chart icon, "Analytics"
- Profile: person icon, "Profile"

**Behavior:**
- Fixed position at bottom
- Current screen highlighted
- Tap to switch between screens

### Notifications Bell (Home only)

- Bell icon in top-right of Home screen
- Badge/indicator when unresolved notifications exist
- Tap to open NotificationsActivity
- Separate from OS-level Reminders

---

## Theme & Consistency

- Consistent color scheme across all screens
- Running/fitness theme without being sports-aggressive
- Clean, simple layouts
- No profile pictures
- No complex animations
- No GPS/distance/speed tracking UI

---

## AndroidManifest.xml

**Declare all Activities:**
```xml
<activity android:name=".MainActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name=".SignupActivity" />
<activity android:name=".QuestionnaireActivity" />
<activity android:name=".HomeActivity" />
<activity android:name=".TargetActivity" />
<activity android:name=".AnalyticsActivity" />
<activity android:name=".ProfileActivity" />
<activity android:name=".NotificationsActivity" />
```

---

## Offline Operation

- All screens work without internet
- No network dependency checks
- All data from local SQLite
- Charts generated from local data
- Reminders use OS-level AlarmManager (no cloud push)

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
