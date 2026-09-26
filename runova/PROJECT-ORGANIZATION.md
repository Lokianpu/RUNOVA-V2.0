# RUNOVA — Project Organization

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8**

---

## File Structure

```text
RUNOVA/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/runova/
│           │   ├── MainActivity.java
│           │   ├── SignupActivity.java
│           │   ├── QuestionnaireActivity.java
│           │   ├── HomeActivity.java
│           │   ├── TargetActivity.java
│           │   ├── AnalyticsActivity.java
│           │   ├── ProfileActivity.java
│           │   ├── NotificationsActivity.java
│           │   ├── models/
│           │   │   ├── Profile.java
│           │   │   ├── Task.java
│           │   │   ├── LevelPlan.java
│           │   │   └── TrainingContentLibrary.java
│           │   ├── database/
│           │   │   └── DBHelper.java
│           │   ├── helpers/
│           │   │   ├── ReminderHelper.java
│           │   │   └── DateUtils.java
│           │   └── controllers/
│           │       ├── TaskGenerator.java
│           │       ├── TimerController.java
│           │       └── LevelCycleController.java
│           ├── res/
│           │   ├── layout/
│           │   │   ├── activity_main.xml
│           │   │   ├── activity_signup.xml
│           │   │   ├── activity_questionnaire.xml
│           │   │   ├── activity_home.xml
│           │   │   ├── activity_target.xml
│           │   │   ├── activity_analytics.xml
│           │   │   ├── activity_profile.xml
│           │   │   └── activity_notifications.xml
│           │   ├── values/
│           │   │   ├── strings.xml
│           │   │   ├── colors.xml
│           │   │   └── styles.xml
│           │   └── drawable/
│           └── AndroidManifest.xml
├── runova/  (documentation)
│   ├── RUNOVA_Project_Overview:Agent_Specs.md  (authoritative)
│   ├── AGENTS.md
│   ├── PROJECT-ORGANIZATION.md
│   ├── README.md
│   ├── RUNOVA-AGENT-SPEC.md
│   ├── runova-project-overview.md
│   └── context/
│       ├── ARCHITECTURE.md
│       ├── DESIGN.md
│       ├── PRD.md
│       ├── ROUTINE-ENGINE.md
│       ├── RULES.md
│       ├── SCHEMA.md
│       └── TRAINING-PLANS.md
└── build.gradle
```

---

## Architecture Pattern

**Simple MVC-style:**

- **Model:** `Profile`, `Task`, `LevelPlan`, `TrainingContentLibrary`, `DBHelper`
- **View:** Activities and layouts (Section 4 of root spec)
- **Controller:** `TaskGenerator`, `TimerController`, `LevelCycleController`, status calculation, validation, reminder scheduling

---

## Data Flow

```text
1. First launch → Pop-up → Sign Up → Questionnaire → SQLite
2. Tasks generated lazily, one day at a time (never in advance)
3. generateDailyTasks() checks if rows exist for date; if not, creates from static weekly template
4. Completing task updates status in SQLite
5. Day status derived: Finished only when every task that date is Finished
6. Editing profile updates saved values; next lazy-generation uses new values automatically
7. Changing Level = wipe everything and start clean immediately
```

---

## Core Components

### Activities (8 total)

| Activity | Purpose |
|---|---|
| `MainActivity` | Welcome screen, start-up pop-up on first launch |
| `SignupActivity` | Identity collection |
| `QuestionnaireActivity` | Body, training, event, level, goal |
| `HomeActivity` | Greeting, date header, notifications bell, task list, level progress |
| `TargetActivity` | Task list with timer/completion controls |
| `AnalyticsActivity` | Weekly progress, level progress, streak, counts, charts |
| `ProfileActivity` | Identity + questionnaire fields, editable |
| `NotificationsActivity` | In-app message list (level-up offer) |

### Models

| Model | Contents |
|---|---|
| `Profile` | firstName, lastName, dateOfBirth, gender, height, weight, event, level, goal, levelStartDate, seenIntro, activeTaskId, activeTaskEndTime |
| `Task` | id, date, name, type, minutes, status, sortOrder |
| `LevelPlan` | Static weekly templates for Beginner, Intermediate, Pro |
| `TrainingContentLibrary` | Static task instructions, drill/exercise groups, event wording |

### Database

| Table | Fields |
|---|---|
| `profile` | Single row: all Profile fields |
| `tasks` | id, date, name, type, minutes, status, sortOrder |
| `notifications` | id, type, message, created, resolved |

Exactly 3 tables. No additions allowed without developer approval.

### Helpers

| Helper | Purpose |
|---|---|
| `ReminderHelper` | OS-level reminders (`NotificationManager` + `AlarmManager`) |
| `DateUtils` | Age calculation, date comparisons, streak calculation |

### Controllers

| Controller | Purpose |
|---|---|
| `TaskGenerator` | `generateDailyTasks()` — lazy task creation from static templates |
| `TimerController` | Single active timer app-wide using `activeTaskId` + `activeTaskEndTime` in `profile` |
| `LevelCycleController` | 28-day cycle check, level-up dialog, `applyLevelChoice()` shared handler |

---

## Navigation

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

Bottom navigation bar shared by: Home, Target, Analytics, Profile.

---

## Tech Stack

| Component | Choice |
|---|---|
| Platform | Android |
| Language | Java |
| IDE | Android Studio |
| Storage | SQLite (Room or SQLiteOpenHelper) |
| Timer | `CountDownTimer` (one active app-wide) |
| OS Reminders | `NotificationManager` + `AlarmManager` |
| Charts | MPAndroidChart (approved offline dependency) |
| Dependencies | Default Android Studio + MPAndroidChart only |

---

## Development Checkpoints

```text
CHECKPOINT 1 — Database foundation
CHECKPOINT 2 — Training generation
CHECKPOINT 3 — Onboarding
CHECKPOINT 4 — Home/Target/timer
CHECKPOINT 5 — Notifications/level cycle
CHECKPOINT 6 — Analytics
CHECKPOINT 7 — Profile/edit
CHECKPOINT 8 — Final offline/demo validation
```

---

## Implementation Priority

If deadline becomes tight, protect in this order:

```text
1. App builds
2. Onboarding works
3. Correct daily task generation
4. SQLite persistence
5. Task completion/status
6. Single timer
7. Profile editing
8. Level cycle
9. Notifications
10. Analytics
11. UI polish
```

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
