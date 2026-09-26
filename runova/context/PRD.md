# RUNOVA — Product Requirements Document

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8**

---

## Project Overview

**What it is:** RUNOVA is an offline Android app for runners and regular people who run. Each day it gives the user a small set of **tasks** — a warm-up, one or more main activities, and a cool-down — matched to their level and event, provides an in-app **timer** for each timed task, and tracks how consistently the user follows the plan.

**Who it's for:** Student athletes, beginner and recreational runners, and anyone who wants a simple structured running plan without internet.

**Core value proposition:** Structured daily training with timers, progress tracking, and real charts — all offline, no GPS or wearables required.

---

## Success Criteria

A new user signs up, picks a level, gets a clear daily set of tasks with timers, completes days, sees their progress with real charts, is offered a level-up (and can still act on it later if they miss it), and can edit their profile so the plan adjusts — all without any internet connection.

---

## Honest Limits & Positioning

**Limits:** RUNOVA has no GPS, distance, speed or heart-rate tracking. Nothing is verified, so a user can mark a task done without doing it. The start-up pop-up says this plainly.

**Positioning:** A training organizer and educational prototype, **not** medical advice or professional coaching. Use neutral labels: Today's Tasks, Suggested Activity, Recovery Day, Track Your Progress.

---

## Requirements

### 1. Onboarding

#### 1.1 Start-up Pop-up (First Launch)

**When:** Shown once on first launch, before Sign Up. Controlled by `seenIntro` flag.

**Content:**
> **Before you start 👟**
> Hello, runner! RUNOVA is an offline training guide made for a school project. It gives you a daily set of tasks, but it can't track your distance, speed or heart rate. Nothing here is automatic.
> That means you *could* tap "Done" without doing the task, and nobody will stop you. But real results only come from actually doing each task, so follow the plan and give it your best. Listen to your body: if something hurts or you feel unwell, stop and rest. If you have any health concerns, talk to a doctor before you begin.
> It's your choice: the shortcut or the real thing. Good luck on your journey!
> **[ I understand, let's go ]**

**Reopenable:** From Profile screen via "About RUNOVA" button.

#### 1.2 Sign Up (Identity Collection)

**Fields:**
- First name (text input, required, not empty)
- Last name (text input, required, not empty)
- Date of birth (date picker, required, valid date, not in future, age 13–100)
- Gender (selection, required: Male, Female, Other)

**Behavior:**
- Age derived from dateOfBirth using `DateUtils.ageFrom()`, never asked directly
- Show validation errors beside field or as Toast
- Do not advance on validation failure
- Keep entered values visible

#### 1.3 Questionnaire (Training Configuration)

**Fields:**
- Height (decimal input, cm, required, 100–250)
- Weight (decimal input, kg, required, 25–250)
- Event (Spinner, required: Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running)
- Level (Spinner, required, Beginner preselected: Beginner, Intermediate, Pro)
- Goal (Spinner, required: Learn the Basics / Build and Hold Endurance / Run Faster and Race / Build Foundation / Improve Performance / Competition Performance)

**Hint under Level:** *"Not sure? Start with Beginner. You can move up later."*

**What each field affects:**
- Height: display only
- Weight: display only
- Event: changes interval/technique task wording and instructions
- Level: weekly task pattern, warm-up/cool-down length, daily duration
- Goal: display label only — does not affect task generation

**Behavior:**
- Wrap number parsing in try/catch
- Show validation errors
- Do not advance on validation failure

---

### 2. Daily Tasks

#### 2.1 Task Generation (Lazy, One Day at a Time)

**When:** First time Home or Target opens for a given date.

**Logic:**
```text
if no tasks exist in SQLite for today's date:
    level = profile.level
    event = profile.event
    weekday = current weekday
    template = LevelPlan.getTemplate(level, weekday)
    tasks = TrainingContentLibrary.applyEventContent(template, event)
    save tasks to SQLite with status = 'Pending'
show tasks for today's date
```

**Rules:**
- Never generate tasks in advance
- Past and already-generated days are never touched again
- Weekly pattern repeats for all 4 weeks of 28-day cycle (no week-to-week progression)

#### 2.2 Task Structure by Level

| Level | Mon | Tue | Wed | Thu | Fri | Sat | Sun |
|---|---:|---:|---:|---:|---:|---:|---:|
| Beginner | 3 | 4 | 3 | 4 | 3 | 4 | 2 |
| Intermediate | 3 | 4 | 5 | 3 | 4 | 5 | 2 |
| Pro | 4 | 5 | 4 | 5 | 4 | 5 | 2 |

**Task breakdown:**
- Training days: includes warm-up and cool-down in count
- Sunday: 2 recovery tasks (Easy Walk + Stretch), no warm-up/cool-down

#### 2.3 Task Categories

1. Warm-up
2. Running Drills
3. Easy Running
4. Tempo Running
5. Interval Training / Speed Intervals
6. Strength & Core
7. Cross-Training / Mobility
8. Recovery (Sunday)

#### 2.4 Exercise Grouping

Multiple exercises inside a task remain **one database row**. Example: "Strength & Core" with 5 exercises = 1 task row.

---

### 3. Home Screen

**Layout (top to bottom):**
1. Greeting with user's first name
2. Current date header (e.g., "Saturday, September 26")
3. Notifications bell icon (top-right)
4. Today's task list (read-only, links to Target)
5. Level progress indicator (`X / 28` days)

**Task list display:**
- Each task: name, duration, status badge (Finished/Pending/Missed)
- Tap to navigate to Target

**Notifications bell:**
- Badge/indicator if unresolved notifications exist
- Tap to open Notifications screen

---

### 4. Target Screen (Task Execution)

#### 4.1 Display

- Today's task list with interactive controls
- Each task: name, duration, Start/Mark Done button, timer display

#### 4.2 Timer Behavior

**One active timer at a time app-wide:**
- Starting new timer while another active = blocked with message: "Finish your current task first"
- Store single `activeTaskId` + `activeTaskEndTime` in profile table
- Recalculate remaining time from stored endTime when screen reopens
- Keep screen on while timer runs
- Beep and vibrate at zero
- Complete button unlocks at zero; Finish early allowed

#### 4.3 Task Completion

- Completing task saves status = 'Finished' to SQLite
- Can't be counted twice
- Day shows Finished only when every task for that date is Finished

---

### 5. Analytics Screen

**Layout (top to bottom):**

1. **Level progress card**
   - `X / 28` days Finished in current cycle
   - ProgressBar visual
   - Color shift when crosses 85% pass line

2. **Streak card**
   - Current streak (consecutive fully-Finished days)
   - Number with icon

3. **Status counts**
   - Finished / Missed / Pending totals
   - Scoped to current 28-day cycle

4. **Time-range chart**
   - 3-tab selector: `Week | Month | Year`
   - Single BarChart view (MPAndroidChart)

**Chart data:**

| Tab | Bars | Bar value |
|---|---|---|
| Week (default) | 7 (Mon–Sun current week) | Tasks Finished that day (0–5) |
| Month | 4–5 (weeks of current month) | Total tasks Finished that week |
| Year | 12 (months of current year) | % of days fully Finished that month |

**Data source:** Existing `tasks` table only (no new tables).

---

### 6. Profile Screen

**Display/edit fields:**
- All identity fields from Sign Up (first name, last name, date of birth, gender)
- All training configuration from Questionnaire (height, weight, event, level, goal)
- Age shown (derived from dateOfBirth, not editable)
- "About RUNOVA" button to reopen start-up pop-up

**Validation:** Same rules as onboarding.

**Behavior:**
- Editing most fields: save to SQLite; next lazy-generation uses new values
- **Exception: Changing Level** → wipes all tasks and starts clean immediately

---

### 7. Notifications Screen (In-app Inbox)

**Purpose:** In-app bell-icon inbox. Currently used for level-up offer only.

**Level-up offer display:**
- Shown if dialog dismissed at day 28 without choice
- Message depends on pass rate:
  - Passed (≥85%): options to *Move up to [next level]* or *Repeat [level]*
  - Below 85%: option to *Repeat [level]* only
  - Pro: options to *Stay at Pro* or *Repeat*
- Action buttons call `applyLevelChoice(choice)` (same function as dialog)
- Message marked resolved after choice

**Rules:**
- Only one unresolved LEVEL_UP_OFFER at a time
- Separate from OS-level Reminders (NotificationManager/AlarmManager)

---

### 8. Level Cycle & Level-up

#### 8.1 Constants

- `LEVEL_DAYS = 28`
- `PASS_RATE = 0.85` (85%)

#### 8.2 Level-up Flow

At day 28:
1. Calculate pass rate: (Finished days in cycle) / 28
2. Show in-app dialog on Home:
   - Passed (≥85%): *Move up to [next level]* or *Repeat [level]*
   - Below 85%: *Repeat [level]* only
   - Pro: *Stay at Pro* or *Repeat*
3. If dialog dismissed without choice: write one LEVEL_UP_OFFER row to notifications

#### 8.3 Shared Handler

Both dialog buttons and Notifications list call `applyLevelChoice(choice)`:
- Updates level and levelStartDate in profile
- Marks matching notifications row resolved
- One function, one code path

---

### 9. Status & Streak

#### 9.1 Per-Task Status

- **Pending:** today's task, not yet completed
- **Finished:** task has saved completion
- **Missed:** past task with no completion

#### 9.2 Day Status (Derived)

- **Finished:** only when all tasks for that date are Finished
- Otherwise: Pending or Missed

#### 9.3 Streak

Consecutive Finished days ending today or yesterday.

---

### 10. Navigation

**Screens:** 8 Activities
- MainActivity (Welcome/launcher)
- SignupActivity
- QuestionnaireActivity
- HomeActivity
- TargetActivity
- AnalyticsActivity
- ProfileActivity
- NotificationsActivity

**Navigation:**
- Bottom nav bar: Home, Target, Analytics, Profile
- Notifications: bell icon from Home only
- If saved profile exists, START goes straight to Home
- Call `finish()` after Welcome so Back doesn't return

---

### 11. Database

**Tables:** Exactly 3

#### 11.1 profile (single row)

| Field | Type | Notes |
|---|---|---|
| id | INTEGER PRIMARY KEY | Always 1 |
| firstName, lastName | TEXT NOT NULL | Identity |
| dateOfBirth, gender | TEXT NOT NULL | Identity |
| height, weight | REAL NOT NULL | Display only |
| event, level, goal | TEXT NOT NULL | Configuration |
| levelStartDate | TEXT NOT NULL | ISO date |
| seenIntro | INTEGER NOT NULL DEFAULT 0 | Boolean |
| activeTaskId | INTEGER NULLABLE | Single timer reference |
| activeTaskEndTime | INTEGER NULLABLE | Epoch ms |

#### 11.2 tasks

| Field | Type | Notes |
|---|---|---|
| id | INTEGER PRIMARY KEY AUTOINCREMENT | |
| date | TEXT NOT NULL | ISO date, indexed |
| name, type | TEXT NOT NULL | Task info |
| minutes | INTEGER NOT NULL | Duration |
| status | TEXT NOT NULL DEFAULT 'Pending' | Pending/Finished/Missed |
| sortOrder | INTEGER NOT NULL | Display order |

#### 11.3 notifications

| Field | Type | Notes |
|---|---|---|
| id | INTEGER PRIMARY KEY AUTOINCREMENT | |
| type | TEXT NOT NULL | Message type |
| message | TEXT NOT NULL | Content |
| created | INTEGER NOT NULL | Epoch ms |
| resolved | INTEGER NOT NULL DEFAULT 0 | Boolean |

---

### 12. Offline Operation

**Required:**
- All screens work without internet
- All data from local SQLite
- Charts generated from local data
- Reminders use OS-level AlarmManager (no cloud push)

**Not allowed:**
- Network dependencies
- Online authentication
- Cloud sync
- External APIs
- Runtime AI/ML calls

---

### 13. Tech Stack

| Component | Choice |
|---|---|
| Platform | Android |
| Language | Java |
| IDE | Android Studio |
| Storage | SQLite (Room or SQLiteOpenHelper) |
| Timer | CountDownTimer (one active app-wide) |
| OS Reminders | NotificationManager + AlarmManager |
| Charts | MPAndroidChart (approved offline dependency) |
| Pattern | Simple MVC-style |
| Dependencies | Default Android Studio + MPAndroidChart only |

---

### 14. Out of Scope

**Not part of the app:**
- Firebase or cloud database
- Online authentication or APIs
- Runtime AI or LLM calls
- GPS, distance, speed, heart-rate tracking
- Wearable integration
- Social features or leaderboards
- Nutrition, hydration, sleep tracking
- Profile pictures
- Complex calendar systems
- Random workout generation
- Week-to-week progression algorithms

---

### 15. Build Philosophy

Time is short (under a week), so every rule was chosen to be the **simplest version that still keeps the agreed concept**: multi-task days, SQLite storage, level-based difficulty, in-app Notifications, and real charts. Nothing fancy, nothing dynamic that doesn't need to be.

---

### 16. Quality Criteria

**Production-ready means:**
- Builds reliably
- No known blocking crashes in tested flows
- Offline operation works
- Data rules are deterministic
- Approved training routines are preserved
- Validation prevents obvious invalid input
- Navigation is coherent
- Timers and task statuses behave consistently
- Destructive operations are confirmed
- No accidental network dependency exists
- No debug/test shortcuts remain in release flow
- Scope remains aligned with approved project

**Does NOT mean:** Enterprise infrastructure, cloud backup, professional coaching, or production-scale backend systems.

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
