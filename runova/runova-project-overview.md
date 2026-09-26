# RUNOVA — Product Specification

> **Approved V7 baseline preserved in V8**
> **Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8

---

## Build Philosophy

Time is short (under a week), so every rule below was chosen to be the **simplest version that still keeps the agreed concept**: multi-task days, SQLite storage, level-based difficulty, in-app Notifications, and real charts. Nothing fancy, nothing dynamic that doesn't need to be. Where an earlier draft left something open or two rules pulled against each other, the simplest fix that removes the ambiguity was picked — noted inline as **(simplified)** so the reasoning stays visible.

---

## 1. Project Overview

**What it is:** RUNOVA is an offline Android app for runners and regular people who run. Each day it gives the user a small set of **tasks** — a warm-up, one or more main activities, and a cool-down — matched to their level and event, provides an in-app **timer** for each timed task, and tracks how consistently the user follows the plan.

**Who it's for:** Student athletes, beginner and recreational runners, and anyone who wants a simple structured running plan without internet.

**Core idea:**
```text
Start-up pop-up → Sign Up → Questionnaire (Level, Event, Goal) → Home
        ↓
Each day (generated the first time it's opened, never in advance):
   Rest day → 2 tasks (Easy Walk, Stretch)
   Training day → Warm-up + 1–3 main tasks + Cool-down
        ↓
Start a task's timer or mark it done → task = Finished / Missed / Pending
        ↓
Day = Finished only when every task that day is Finished
        ↓
Weekly progress + Level progress → level-up prompt after 28 days
          → if ignored, the prompt stays available in Notifications until acted on
```

**Success looks like:** A new user signs up, picks a level, gets a clear daily set of tasks with timers, completes days, sees their progress with real charts, is offered a level-up (and can still act on it later if they miss it), and can edit their profile so the plan adjusts — all without any internet connection.

**Honest limits:** RUNOVA has no GPS, distance, speed or heart-rate tracking. Nothing is verified, so a user can mark a task done without doing it. The start-up pop-up says this plainly.

**Positioning:** a training organizer and educational prototype, **not** medical advice or professional coaching. Use neutral labels: Today's Tasks, Suggested Activity, Recovery Day, Track Your Progress.

---

## 2. Scope

### Included
- Start-up pop-up, Sign Up, Questionnaire (Event, Level, Goal), Home (date header + notifications bell), daily Task list with per-task timers, Analytics (with charts), Profile (editable), Notifications
- Three levels (Beginner, Intermediate, Pro), each with a fixed 7-day approved training routine
- Warm-up and Cool-down as fixed bookend tasks every training day, plus 1–3 grouped main-task slots depending on the day; individual exercises stay inside a task and never become separate DB rows
- Per-task Finished / Missed / Pending status, rolling up to a day status, weekly progress, level progress, streak
- Level-up prompt: in-app dialog at day 28, persisted in Notifications if not acted on immediately
- SQLite storage, input validation, Intent navigation, consistent theme, airplane-mode testing

### Not part of the app
Firebase or any online database, cloud sync, online accounts, AI/ML, GPS, wearables, heart-rate or distance tracking, social features, leaderboards, external APIs, nutrition/hydration/sleep tracking, profile pictures, a full calendar grid view, medical advice, week-to-week difficulty ramps, random workout generation, runtime AI-generated workouts, GPS/wearable integrations, or complex progression algorithms.

---

## 3. Tech Stack & Architecture

| Piece | Choice |
|---|---|
| Platform / IDE | Android, Java, Android Studio |
| Storage | SQLite (Room or SQLiteOpenHelper) |
| Timer | One `CountDownTimer` at a time app-wide |
| Notifications | `NotificationManager` + `AlarmManager` for reminders |
| Charts | MPAndroidChart (approved offline dependency) for Analytics |
| Pattern | MVC-style: Model, View, Controller |
| Dependencies | Default Android Studio project + MPAndroidChart only |

**Two different "notification" concepts:**
- **Reminders** = OS-level pings (`NotificationManager`/`AlarmManager`) that nudge the user to do a task.
- **Notifications** = the in-app bell-icon inbox on Home, currently used for exactly one thing: a persistent level-up offer.

**Model:** `Profile`, `Task`, `LevelPlan` (static approved weekly templates per level) + `TrainingContentLibrary` (static task instructions/event wording), `DBHelper`, `ReminderHelper`, `DateUtils`.

**View:** the Activities and layouts.

**Controller:** `generateDailyTasks()`, per-task status calculation, day roll-up, level-cycle check, validation, single active-timer logic, reminder scheduling.

**Data flow:**
1. First launch: pop-up, then Sign Up, then Questionnaire. Everything is saved to SQLite.
2. **Tasks are generated lazily, one day at a time — never in advance.** The first time Home or Target opens for a given date, `generateDailyTasks()` checks if rows already exist for that date; if not, it creates them from the static weekly pattern and writes them to `tasks`. Past and already-generated days are never touched again.
3. Completing a task updates its row's status. The day's status is derived: Finished only when every task for that date is Finished.
4. Editing the profile updates the saved values. Because tomorrow's tasks haven't been generated yet, the very next lazy-generation call automatically uses the new values — **no separate "regenerate" step is needed.** Changing Level is the one exception: it wipes everything and starts clean immediately.

---

## 4. Screens & Navigation

Eight Activities, all navigated with `Intent`.

| Activity | Layout | Purpose |
|---|---|---|
| `MainActivity` | `activity_main.xml` | Welcome screen (launcher): logo, START, start-up pop-up on first launch |
| `SignupActivity` | `activity_signup.xml` | Identity |
| `QuestionnaireActivity` | `activity_questionnaire.xml` | Body, training, event, level, goal |
| `HomeActivity` | `activity_home.xml` | Greeting, current date header, notifications bell, today's task list, level progress |
| `TargetActivity` | `activity_target.xml` | Today's task list — start/complete each task, one timer at a time |
| `AnalyticsActivity` | `activity_analytics.xml` | Weekly progress, level progress, streak, counts, charts |
| `ProfileActivity` | `activity_profile.xml` | Identity + questionnaire fields, editable, no picture |
| `NotificationsActivity` | `activity_notifications.xml` | List of in-app messages (currently just the level-up offer) |

Home, Analytics, Target and Profile share a fixed **bottom navigation bar**. Notifications is reached via the bell icon on Home only.

**Rules:**
- Declare every Activity in `AndroidManifest.xml`; `MainActivity` is the launcher.
- Call `finish()` when moving forward from Welcome so Back does not return to it.
- If a saved profile exists, START goes straight to Home.

---

## 5. Start-up Pop-up

Shown **once on first launch**, before Sign Up (use a saved `seenIntro` flag). Reopenable from Profile ("About RUNOVA").

> **Before you start 👟**
> Hello, runner! RUNOVA is an offline training guide made for a school project. It gives you a daily set of tasks, but it can't track your distance, speed or heart rate. Nothing here is automatic.
> That means you *could* tap "Done" without doing the task, and nobody will stop you. But real results only come from actually doing each task, so follow the plan and give it your best. Listen to your body: if something hurts or you feel unwell, stop and rest. If you have any health concerns, talk to a doctor before you begin.
> It's your choice: the shortcut or the real thing. Good luck on your journey!
> **[ I understand, let's go ]**

---

## 6. Onboarding & Validation

### 6.1 Sign Up (identity)
| Field | Input | Validation |
|---|---|---|
| First name | text | not empty |
| Last name | text | not empty |
| Date of birth | date picker | valid date, not in the future, derived age 13–100 |
| Gender | selection | must be selected |

Age is **derived** from the date of birth with `DateUtils.ageFrom()`, shown on Profile. Never asked directly.

### 6.2 Questionnaire
| Field | Input | Validation | Affects |
|---|---|---|---|
| Height | decimal | positive (100–250 cm) | display only |
| Weight | decimal | positive (25–250 kg) | display only |
| Event | Spinner | must be selected: Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running | changes relevant interval/technique task wording and static instructions only |
| Level | Spinner | must be selected, **Beginner preselected** | weekly task pattern, warm-up/cool-down length, daily duration budget |
| Goal | Spinner | must be selected: Learn the Basics / Build and Hold Endurance / Run Faster and Race / Build Foundation / Improve Performance / Competition Performance | display label only — **does not affect task generation** |

Hint under Level: *"Not sure? Start with Beginner. You can move up later."*

**Goal vs. Level:** Goal is purely a **display label** the user picks for themselves — it shows on Home/Profile but never changes which tasks are generated. Level alone drives the plan, Goal is just how the user describes their ambition.

### 6.3 Validation behavior
- Show the error beside the field (`setError`) or a clear Toast.
- Do **not** advance; keep entered values.
- Wrap number parsing in `try/catch`; invalid input must never crash the app.

---

## 7. Daily Training Plan — Approved Final Structure

RUNOVA's training content is intentionally **structured, realistic, deterministic, and simple to implement**. The app is not a random workout generator and does not create individualized coaching plans at runtime.

### 7.1 Training design principles

The approved daily training flow is:

```text
WARM-UP → RUNNING DRILL / ACTIVATION (when scheduled) → MAIN TRAINING
→ OPTIONAL STRENGTH / CONDITIONING / MOBILITY → COOL-DOWN
```

Important implementation rule: the flow above describes the **content inside the day's task slots**. Do not create one database row for every individual exercise. A task such as `Strength & Core` contains a short group of exercises/instructions inside its task description while remaining one SQLite `tasks` row.

The plan uses eight practical activity categories:

1. **Warm-up** — easy movement and dynamic preparation before training.
2. **Running Drills** — simple running mechanics/activation drills such as marching, high knees, butt kicks, A-skips, and controlled strides.
3. **Easy Running** — relaxed aerobic running at a comfortable, conversational effort.
4. **Tempo Running** — sustained controlled running at a stronger but manageable effort.
5. **Interval Training** — repeated faster running with recovery periods; event wording changes where appropriate.
6. **Strength & Core** — bodyweight strength and trunk stability using simple movements.
7. **Cross-Training / Mobility** — low-impact conditioning or mobility work such as brisk walking, cycling, or light stretching.
8. **Recovery** — Sunday Easy Walk + Stretch.

This is an educational training organizer. The routines are intentionally general rather than individualized medical or professional coaching prescriptions.

### 7.2 Fixed weekly task pattern

| Level | Mon | Tue | Wed | Thu | Fri | Sat | Sun |
|---|---:|---:|---:|---:|---:|---:|---:|
| Beginner | 3 | 4 | 3 | 4 | 3 | 4 | Rest (2) |
| Intermediate | 3 | 4 | 5 | 3 | 4 | 5 | Rest (2) |
| Pro | 4 | 5 | 4 | 5 | 4 | 5 | Rest (2) |

Each training-day number is the **total number of task rows** shown to the user, including one warm-up and one cool-down:

- `3 tasks` = Warm-up + 1 main task + Cool-down
- `4 tasks` = Warm-up + 2 main tasks + Cool-down
- `5 tasks` = Warm-up + 3 main tasks + Cool-down
- Sunday = exactly 2 recovery tasks: Easy Walk + Stretch

This pattern repeats for every week of the 28-day level cycle. There is **no week-to-week ramp** and no random task selection.

### 7.3 Daily duration rules

| Level | Daily maximum | Approved routine range |
|---|---:|---:|
| Beginner | 60 min | 30–60 min |
| Intermediate | 90 min | 30–90 min |
| Pro | 120 min | 40–120 min |

Warm-up and cool-down durations are fixed by level:

| Level | Warm-up | Cool-down |
|---|---:|---:|
| Beginner | 10 min | 10 min |
| Intermediate | 15 min | 15 min |
| Pro | 15 min | 15 min |

**Important:** For the approved weekly examples below, use the listed task durations exactly. Do not recalculate them at runtime using an even-split formula. The approved schedule is the source of truth.

### 7.4 Approved Beginner weekly routine

| Day | Tasks / duration | What the user follows |
|---|---|---|
| **Sunday** | **2 tasks / 30 min** | Easy Walk 15 min + Stretch 15 min. Recovery-focused; no warm-up/cool-down. |
| **Monday** | **3 tasks / 60 min** | Warm-up 10 min → Easy Run 40 min → Cool-down 10 min. Comfortable running; focus on consistency. |
| **Tuesday** | **4 tasks / 60 min** | Warm-up 10 min → Running Drills 15 min → Strength & Core 25 min → Cool-down 10 min. Simple mechanics plus bodyweight conditioning. |
| **Wednesday** | **3 tasks / 60 min** | Warm-up 10 min → Cross-Training 40 min → Cool-down 10 min. Low-impact aerobic work such as brisk walking or cycling. |
| **Thursday** | **4 tasks / 60 min** | Warm-up 10 min → Interval Run 30 min → Mobility & Stretch 10 min → Cool-down 10 min. Controlled faster/easier running repetitions. |
| **Friday** | **3 tasks / 60 min** | Warm-up 10 min → Strength & Core 40 min → Cool-down 10 min. Basic lower-body and core work. |
| **Saturday** | **4 tasks / 60 min** | Warm-up 10 min → Easy Run 30 min → Running Drills 10 min → Cool-down 10 min. Easy aerobic running plus technique practice. |

### 7.5 Approved Intermediate weekly routine

| Day | Tasks / duration | What the user follows |
|---|---|---|
| **Sunday** | **2 tasks / 30 min** | Easy Walk 15 min + Stretch 15 min. Recovery day. |
| **Monday** | **3 tasks / 75 min** | Warm-up 15 min → Easy Run 45 min → Cool-down 15 min. Comfortable aerobic running. |
| **Tuesday** | **4 tasks / 90 min** | Warm-up 15 min → Interval Run 30 min → Strength & Core 30 min → Cool-down 15 min. Controlled speed-endurance work plus strength. |
| **Wednesday** | **5 tasks / 90 min** | Warm-up 15 min → Easy Run 25 min → Tempo Run 20 min → Strength & Core 15 min → Cool-down 15 min. Combines aerobic running, controlled tempo, and short conditioning. |
| **Thursday** | **3 tasks / 75 min** | Warm-up 15 min → Easy Run 45 min → Cool-down 15 min. Aerobic maintenance/recovery-oriented running. |
| **Friday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 10 min → Interval Run 35 min → Cool-down 15 min. Technique followed by structured faster running. |
| **Saturday** | **5 tasks / 90 min** | Warm-up 15 min → Easy/Long Run 40 min → Strides 10 min → Mobility 10 min → Cool-down 15 min. Longer aerobic work with short controlled strides and mobility. |

### 7.6 Approved Pro weekly routine

| Day | Tasks / duration | What the user follows |
|---|---|---|
| **Sunday** | **2 tasks / 40 min** | Easy Walk 20 min + Mobility & Stretch 20 min. Recovery-focused day. |
| **Monday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 15 min → Speed Intervals 30 min → Cool-down 15 min. Technique and controlled speed work. |
| **Tuesday** | **5 tasks / 120 min** | Warm-up 15 min → Easy Run 45 min → Strength & Core 30 min → Mobility 15 min → Cool-down 15 min. Aerobic work plus full-body conditioning and mobility. |
| **Wednesday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 10 min → Tempo Run 35 min → Cool-down 15 min. Running mechanics plus sustained controlled effort. |
| **Thursday** | **5 tasks / 105 min** | Warm-up 15 min → Easy Run 45 min → Technique 15 min → Mobility 15 min → Cool-down 15 min. Aerobic maintenance, technique, and mobility. |
| **Friday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 10 min → Interval Run 35 min → Cool-down 15 min. Structured speed-endurance session. |
| **Saturday** | **5 tasks / 115 min** | Warm-up 15 min → Easy/Long Run 60 min → Strides 10 min → Mobility 15 min → Cool-down 15 min. Long aerobic session with controlled strides and mobility. |

### 7.7 Event-specific adaptation

Event selection remains simple and does not create new database task types. It changes the relevant interval/technique task's display name and its static instruction content.

| Event | Relevant task wording | Example content emphasis |
|---|---|---|
| General Running | Interval Run | Controlled faster/easier running repetitions |
| Middle Distance | Interval Run | Speed endurance and controlled intervals |
| Long Distance | Interval Run | Longer controlled repetitions with endurance emphasis |
| Sprint | Sprint Repeats / Speed Intervals | Shorter fast efforts with full recovery emphasis |
| Hurdles | Hurdle Technique + Sprint Repeats | Basic hurdle movement/technique concepts plus controlled sprint work |
| Relay | Relay Exchange Practice | Basic exchange/coordination practice concepts |

**Scope rule:** event adaptation must remain content-level. Do not add new tables, new Activity classes, or a complex event-specific training engine.

### 7.8 Training plan generation

```text
if no tasks exist in SQLite for today's date:
    level = profile.level
    event = profile.event
    weekday = current weekday
    template = LevelPlan.getTemplate(level, weekday)
    tasks = TrainingContentLibrary.applyEventContent(template, event)
    save tasks to SQLite
show tasks for today's date
```

`LevelPlan` contains the approved Monday–Sunday templates for Beginner, Intermediate, and Pro. `TrainingContentLibrary` contains task names, short instructions, drill/exercise groups, and event wording.

**Do not implement:** random workouts, runtime AI-generated workouts, automatic progression algorithms, per-user difficulty calculations, or a week-to-week ramp.

---

## 8. Daily Flow, Timer, Status, Level-up

### 8.1 Target screen and timers

1. Shows today's task list — name, minutes, Start/Mark Done control.
2. **Only one task's timer can run at a time.** Starting a new task's timer while another is active is blocked in the UI.
3. Store a single `activeTaskId` + `activeTaskEndTime` in the `profile` table. Recalculate remaining time from this single pair when the screen reopens.
4. Keep the screen on while the timer runs. Beep and vibrate at zero.
5. **Complete** unlocks at zero for timed tasks; **Finish early** is allowed.
6. Completing a task saves its status; can't be counted twice.
7. A day shows Finished only once **every** task for that date is Finished.

### 8.2 Status

- **Finished:** task has a saved completion.
- **Pending:** today's task, not yet completed.
- **Missed:** a past task with no completion.
- **Day status:** Finished only when all of that day's tasks are Finished.
- **Streak:** consecutive Finished days ending today or yesterday.

### 8.3 Level cycle and level-up

- Constants: `LEVEL_DAYS = 28`, `PASS_RATE = 0.85`
- At day 28, show an **in-app dialog** on Home.
- **Passed (≥85%):** *Move up to [next level]* or *Repeat [level]*.
- **Below 85%:** only *Repeat [level]*.
- **Pro:** *Stay at Pro* or *Repeat*.
- If the dialog is dismissed without a choice, one `LEVEL_UP_OFFER` row is written to `notifications`.
- **Both** the dialog's buttons **and** the Notifications-list version call the exact same function, `applyLevelChoice(choice)`. That function (a) updates the level and `levelStartDate`, and (b) marks any matching `notifications` row resolved.

---

## 9. Analytics

Everything reads from the existing `tasks` table only (`date`, `status`). No schema change.

**Screen layout:**

1. **Level progress card** — `X / 28` days Finished in the current cycle, as a `ProgressBar`. Color shifts once it crosses the 85% pass line.
2. **Streak card** — current streak (consecutive fully-Finished days).
3. **Status counts** — Finished / Missed / Pending totals, **scoped to the current 28-day cycle**.
4. **Time-range chart** — one chart, fed by whichever of the three views below is selected via a 3-tab row (`Week | Month | Year`).

**Chart data:**

| View | Bars | Bar value |
|---|---|---|
| **Week** (default) | 7, Mon–Sun of current week | Tasks Finished that day (0–5) |
| **Month** | 4–5, one per week of current month | Total tasks Finished that week |
| **Year** | 12, one per month of current year | % of that month's days that were fully Finished |

---

## 10. Database Schema

Three tables exactly.

### 10.1 profile (single row)

| Field | Type | Notes |
|---|---|---|
| id | INTEGER PRIMARY KEY | always 1 |
| firstName | TEXT | not null |
| lastName | TEXT | not null |
| dateOfBirth | TEXT | ISO date |
| gender | TEXT | not null |
| height | REAL | cm |
| weight | REAL | kg |
| event | TEXT | not null |
| level | TEXT | not null |
| goal | TEXT | not null |
| levelStartDate | TEXT | ISO date |
| seenIntro | INTEGER | 0/1 boolean |
| activeTaskId | INTEGER | nullable, single active timer reference |
| activeTaskEndTime | INTEGER | nullable, epoch ms |

### 10.2 tasks

| Field | Type | Notes |
|---|---|---|
| id | INTEGER PRIMARY KEY AUTOINCREMENT | |
| date | TEXT | ISO date, not null |
| name | TEXT | not null |
| type | TEXT | not null |
| minutes | INTEGER | not null |
| status | TEXT | Pending/Finished/Missed |
| sortOrder | INTEGER | display order |

Index on `date`.

### 10.3 notifications

| Field | Type | Notes |
|---|---|---|
| id | INTEGER PRIMARY KEY AUTOINCREMENT | |
| type | TEXT | not null |
| message | TEXT | not null |
| created | INTEGER | epoch ms |
| resolved | INTEGER | 0/1 boolean |

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
