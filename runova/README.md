# RUNOVA

> **Offline Android Training Organizer for Runners**

---

## What is RUNOVA?

RUNOVA is an offline Android app that provides structured daily training tasks for runners and regular people who run. Each day it generates a small set of tasks — warm-up, main activities, and cool-down — matched to the user's level and event. It includes an in-app timer for each timed task and tracks how consistently the user follows the plan.

**Core concept:**
```text
Sign Up → Questionnaire (Level, Event, Goal) → Home
    ↓
Each day (generated first time opened, never in advance):
   Rest day → 2 tasks (Easy Walk, Stretch)
   Training day → Warm-up + 1–3 main tasks + Cool-down
    ↓
Start timer or mark done → task = Finished / Missed / Pending
    ↓
Day = Finished only when every task is Finished
    ↓
Weekly progress + Level progress → level-up prompt after 28 days
    → if ignored, prompt stays in Notifications until acted on
```

---

## Who is it for?

- Student athletes
- Beginner and recreational runners
- Anyone who wants a simple structured running plan without internet

---

## What RUNOVA includes

- **Onboarding:** Start-up pop-up, Sign Up, Questionnaire (Event, Level, Goal)
- **Three levels:** Beginner, Intermediate, Pro — each with a fixed 7-day training routine
- **Daily tasks:** Warm-up, main activities (Easy Run, Tempo Run, Interval Run, Strength & Core, Cross-Training, Running Drills, Mobility), Cool-down
- **Per-task timer:** One active timer at a time app-wide
- **Status tracking:** Finished / Missed / Pending per task, rolled up per day
- **Level cycle:** 28-day cycle with level-up prompt
- **Analytics:** Week / Month / Year views with real charts (MPAndroidChart)
- **Profile:** Editable identity and questionnaire fields
- **Notifications:** In-app bell-icon inbox for level-up offers
- **Offline reminders:** OS-level pings to nudge user to do tasks
- **SQLite storage:** All data stored locally
- **Offline operation:** No internet required

---

## What RUNOVA does NOT include

- Firebase or cloud sync
- Online authentication or APIs
- Runtime AI or LLM calls
- GPS, distance, speed, heart-rate tracking
- Wearable integration
- Social features or leaderboards
- Nutrition, hydration, sleep tracking
- Profile pictures
- Complex calendar systems
- Random workout generation
- Runtime-generated workout plans
- Week-to-week progression algorithms

---

## Honest limits

RUNOVA has no GPS, distance, speed or heart-rate tracking. Nothing is verified, so a user can mark a task done without doing it. The start-up pop-up says this plainly.

**Positioning:** a training organizer and educational prototype, **not** medical advice or professional coaching.

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

## Screens

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

---

## Database

| Table | Fields |
|---|---|
| `profile` | Single row: all Profile fields including activeTaskId, activeTaskEndTime |
| `tasks` | id, date, name, type, minutes, status, sortOrder |
| `notifications` | id, type, message, created, resolved |

Exactly 3 tables.

---

## Training Levels

| Level | Daily max | Warm-up | Cool-down | Task count pattern (Mon-Sat) |
|---|---:|---:|---:|---|
| Beginner | 60 min | 10 min | 10 min | 3, 4, 3, 4, 3, 4 |
| Intermediate | 90 min | 15 min | 15 min | 3, 4, 5, 3, 4, 5 |
| Pro | 120 min | 15 min | 15 min | 4, 5, 4, 5, 4, 5 |

Sunday = Rest day (2 tasks: Easy Walk + Stretch)

---

## Events

- Sprint
- Middle Distance
- Long Distance
- Hurdles
- Relay
- General Running

Event selection changes relevant interval/technique task wording and static instruction content only. It does not create new database task types.

---

## Level Cycle

- 28-day cycle
- 85% pass rate required to move up
- In-app dialog at day 28
- If dismissed, level-up offer persists in Notifications until acted on

---

## Status

A new user signs up, picks a level, gets a clear daily set of tasks with timers, completes days, sees their progress with real charts, is offered a level-up (and can still act on it later if they miss it), and can edit their profile so the plan adjusts — all without any internet connection.

---

## Build Philosophy

Time is short (under a week), so every rule was chosen to be the **simplest version that still keeps the agreed concept**: multi-task days, SQLite storage, level-based difficulty, in-app Notifications, and real charts. Nothing fancy, nothing dynamic that doesn't need to be.

---

## Documentation

- **RUNOVA_Project_Overview:Agent_Specs.md** — Authoritative development specification (V8)
- **AGENTS.md** — AI agent control charter
- **PROJECT-ORGANIZATION.md** — Architecture and file structure
- **RUNOVA-AGENT-SPEC.md** — AI agent control specification
- **runova-project-overview.md** — Product specification
- **context/** — Detailed specs (ARCHITECTURE, DESIGN, PRD, ROUTINE-ENGINE, RULES, SCHEMA, TRAINING-PLANS)

---

## Development Control

This is a controlled implementation project. The approved V7 concept, architecture, features, training plan, database design, navigation, UI direction, and scope are preserved. The V8 additions define **how an AI coding agent must work**, how the developer retains control, how changes are proposed, and how limited development time and AI token usage are managed.

**Important:** This is not permission to redesign RUNOVA. It is a stricter execution layer over the already-approved project.

---

## Mission

> **Implement the approved RUNOVA project accurately, simply, deterministically, and
> within the existing architecture — not invent a better version of RUNOVA.**

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
