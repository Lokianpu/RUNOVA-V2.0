# RUNOVA — Architecture

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8**

---

## Pattern

**Simple MVC-style structure**

- **Model:** data classes and business logic
- **View:** Activities and layouts
- **Controller:** task generation, status calculation, timer control, level cycle logic

---

## Core Mental Model

```text
                    ┌──────────────────────┐
                    │     RUNOVA APP       │
                    │ Offline Android/Java  │
                    └──────────┬───────────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
            ▼                  ▼                  ▼
       USER PROFILE        TRAINING PLAN       TASK STATE
            │                  │                  │
            │                  │                  │
     Level/Event/Goal      LevelPlan +        Finished/
     Identity fields       Content Library    Missed/Pending
            │                  │                  │
            └──────────────────┼──────────────────┘
                               ▼
                     generateDailyTasks()
                               │
                               ▼
                         SQLite tasks
                               │
             ┌─────────────────┼─────────────────┐
             ▼                 ▼                 ▼
           Target          Analytics          Streak/
           Timer            Charts            Level Cycle
                               │
                               ▼
                        Notifications
```

Central principle:

> **Static approved training data + deterministic generation + SQLite persistence.**

There is no runtime AI training engine.

---

## Model Layer

### Profile
- Single row in `profile` table
- Fields: firstName, lastName, dateOfBirth, gender, height, weight, event, level, goal, levelStartDate, seenIntro, activeTaskId, activeTaskEndTime
- Manages user identity and training configuration
- Single active timer state stored here

### Task
- Rows in `tasks` table
- Fields: id, date, name, type, minutes, status, sortOrder
- Per-task status: Finished / Missed / Pending
- Day status derived: Finished only when all tasks for that date are Finished

### LevelPlan
- Static approved weekly templates
- One template per level (Beginner, Intermediate, Pro)
- Contains Monday–Sunday task patterns with exact durations
- No runtime generation or modification

### TrainingContentLibrary
- Static task instructions
- Drill/exercise groups
- Event-specific wording
- Short descriptions for Target screen

### DBHelper
- SQLite database management
- Three tables: profile, tasks, notifications
- No additional tables allowed without approval

### ReminderHelper
- OS-level reminders using `NotificationManager` + `AlarmManager`
- Separate from in-app Notifications bell

### DateUtils
- Age calculation from dateOfBirth
- Date comparisons
- Streak calculation

---

## View Layer

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

### Layouts

| Layout | Activity |
|---|---|
| `activity_main.xml` | MainActivity |
| `activity_signup.xml` | SignupActivity |
| `activity_questionnaire.xml` | QuestionnaireActivity |
| `activity_home.xml` | HomeActivity |
| `activity_target.xml` | TargetActivity |
| `activity_analytics.xml` | AnalyticsActivity |
| `activity_profile.xml` | ProfileActivity |
| `activity_notifications.xml` | NotificationsActivity |

---

## Controller Layer

### TaskGenerator
- `generateDailyTasks()` function
- Lazy generation: one day at a time, never in advance
- Checks if tasks exist for date; if not, creates from static template
- Logic:
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

### TimerController
- Single active timer app-wide using `CountDownTimer`
- State stored in `profile`: activeTaskId, activeTaskEndTime
- Starting new timer while another active = blocked in UI
- Recalculate remaining time from stored endTime when screen reopens
- Beep and vibrate at zero

### LevelCycleController
- 28-day cycle check
- Pass rate: 85%
- In-app dialog at day 28
- `applyLevelChoice(choice)` shared handler:
  - Updates level and levelStartDate
  - Marks matching notifications row resolved
- Single code path prevents stale state

### Status Calculation
- Per task: Finished / Missed / Pending
- Per day: Finished only when all tasks for that date are Finished
- Streak: consecutive Finished days ending today or yesterday

### Validation
- Input validation on all forms
- Show error beside field or Toast
- Never advance on invalid input
- Wrap number parsing in try/catch

---

## Data Flow

```text
1. First launch
   Pop-up → Sign Up → Questionnaire → SQLite

2. Task generation (lazy)
   Home/Target opens for date → check if tasks exist
   → if not, generate from static template → write to SQLite
   → past/already-generated days never touched again

3. Task completion
   Complete task → update status in SQLite
   → day status derived from all tasks for that date

4. Profile editing
   Update profile → save to SQLite
   → next lazy-generation uses new values automatically
   → Exception: changing Level wipes everything and starts clean

5. Level cycle
   Day 28 reached → check pass rate → show dialog
   → if dismissed, write LEVEL_UP_OFFER to notifications
   → both dialog and notifications call same applyLevelChoice()
```

---

## Component Ownership

| Requirement | Primary implementation area |
|---|---|
| User identity | `Profile`, `DBHelper`, Sign Up |
| Level/Event/Goal | `Profile`, Questionnaire |
| Approved weekly plan | `LevelPlan` |
| Exercise/instruction content | `TrainingContentLibrary` |
| Daily task creation | `generateDailyTasks()` |
| Existing task persistence | `tasks` table |
| Completion | Task controller / Target |
| Day status | status calculation |
| Timer | single timer controller + `profile` timer fields |
| Level cycle | cycle helper/controller |
| Level-up offer | `notifications` table + shared handler |
| Analytics | task queries + MPAndroidChart |
| Profile changes | Profile controller |
| Offline reminders | `ReminderHelper` |
| Date calculations | `DateUtils` |

Do not create new system when existing area already owns responsibility.

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
| Navigation | Intent-based |
| Dependencies | Default Android Studio + MPAndroidChart only |

---

## Architectural Decisions

| Decision | Approved rule |
|---|---|
| Storage | SQLite |
| Table count | Exactly 3 |
| Platform | Android |
| Language | Java |
| Architecture | Simple MVC-style structure |
| Training source | Static approved weekly templates |
| Generation | Lazy, one day at a time |
| Random generation | Not allowed |
| Runtime AI workouts | Not allowed |
| Levels | Beginner, Intermediate, Pro |
| Events | Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running |
| Goal | Display-only |
| Task timer | One active timer |
| Statuses | Finished, Missed, Pending |
| Recovery day | Sunday |
| Level cycle | 28 days |
| Analytics | Week / Month / Year |
| Chart dependency | MPAndroidChart |
| Internet | Not required |
| New dependencies | Not allowed unless approved |
| New tables | Not allowed |
| New Activities | Not allowed unless approved |

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

Bottom navigation bar shared by: Home, Target, Analytics, Profile.

---

## Simplicity Principles

- Reuse existing code over new abstraction
- Small targeted change over refactor
- Fix root cause over symptom patches
- Single source of truth for each concern
- No new architecture when existing pattern works
- Offline-first, no network dependency
- Deterministic behavior, no runtime randomness
- Static data over dynamic generation

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
