# RUNOVA — Development Rules & Boundaries

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8**

---

## Non-Negotiable Project Boundaries

**MUST NOT add without explicit developer approval:**

- Firebase
- Cloud/database synchronization
- Online authentication
- Online APIs
- Runtime AI or LLM calls
- GPS
- Distance tracking
- Speed tracking
- Heart-rate tracking
- Wearable integration
- Social features
- Leaderboards
- Nutrition tracking
- Hydration tracking
- Sleep tracking
- Profile pictures
- Complex calendar systems
- Random workout generation
- Runtime-generated workout plans
- Week-to-week progression algorithms
- New training levels
- New event types
- New database tables
- New Activities solely to solve an implementation problem
- New external dependencies except the already-approved MPAndroidChart
- Complex frameworks introduced only for convenience
- Features that were not requested in the approved scope

**Rule:** If a feature sounds useful but is not explicitly required, it is out of scope until the developer approves it.

---

## Approved Decision Register

The following decisions are already made and must not be reopened during normal development:

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
| Developer authority | Final |

---

## Validation Rules

### Sign Up (identity)

| Field | Validation |
|---|---|
| First name | not empty |
| Last name | not empty |
| Date of birth | valid date, not in future, derived age 13–100 |
| Gender | must be selected |

### Questionnaire (training configuration)

| Field | Validation |
|---|---|
| Height | positive (100–250 cm) |
| Weight | positive (25–250 kg) |
| Event | must be selected |
| Level | must be selected, Beginner preselected |
| Goal | must be selected |

### Validation behavior (all forms)

- Show error beside field (`setError`) or clear Toast
- Do not advance on validation failure
- Keep entered values visible
- Wrap number parsing in try/catch
- Invalid input must never crash the app

---

## Database Rules

### Schema constraints

- Exactly 3 tables: profile, tasks, notifications
- No new tables without developer approval
- No new columns without developer approval
- No foreign keys (simple flat structure)
- All queries simple and fast

### Data types

- All dates: ISO 8601 strings (YYYY-MM-DD)
- All timestamps: epoch milliseconds (long)
- All booleans: INTEGER (0/1)
- All enums: TEXT

### Data integrity

- Profile: always exactly one row
- Tasks: generated rows never modified except status field
- Notifications: only one unresolved LEVEL_UP_OFFER at a time

---

## Task Generation Rules

### DO implement:
- Static `LevelPlan` with approved Monday–Sunday templates
- Static `TrainingContentLibrary` with task instructions and event wording
- Lazy generation: check date existence in SQLite first
- Simple weekday calculation to select correct template
- Event-specific content application at generation time

### DO NOT implement:
- Random workout selection
- Runtime AI-generated workouts
- Automatic progression algorithms
- Per-user difficulty calculations
- Week-to-week ramp within 28-day cycle
- Pre-generation of future tasks
- Task regeneration on profile edits (except level change)
- Complex caching or invalidation logic

---

## Task Content Rules

### Task rows vs exercises

Multiple exercises inside a task remain **one database row**.

Example: "Strength & Core" with 5 exercises = 1 row in tasks table.

### Activity-only rule

Tasks are activity/training items only: running, drills, strength, cross-training, warm-up, cool-down, mobility, recovery.

**Do NOT add:** nutrition, hydration, sleep, meditation, supplement, or medical-advice tasks.

### Exercise grouping

The existing task-count pattern must never be broken just because a task contains multiple exercises.

---

## Timer Rules

### One active timer app-wide

- Only one task timer can run at a time
- Starting new timer while another active = blocked in UI
- State stored in profile: activeTaskId, activeTaskEndTime
- Recalculate remaining time from stored endTime when screen reopens
- Keep screen on while timer runs
- Beep and vibrate at zero

### Timer state management

```text
User starts timer:
    UPDATE profile SET activeTaskId = ?, activeTaskEndTime = ? WHERE id = 1

User completes task:
    UPDATE tasks SET status = 'Finished' WHERE id = ?
    UPDATE profile SET activeTaskId = NULL, activeTaskEndTime = NULL WHERE id = 1
```

---

## Status Rules

### Per-task status

- **Pending:** today's task, not yet completed
- **Finished:** task has saved completion
- **Missed:** past task with no completion

### Day status (derived, not stored)

- **Finished:** only when all tasks for that date are Finished
- Otherwise: Pending or Missed

### Streak

Consecutive Finished days ending today or yesterday.

---

## Level Cycle Rules

### Constants

- `LEVEL_DAYS = 28`
- `PASS_RATE = 0.85` (85%)

### Level-up behavior

At day 28:
1. Show in-app dialog on Home
2. If passed (≥85%): options to move up or repeat
3. If below 85%: option to repeat only
4. If Pro: options to stay at Pro or repeat
5. If dialog dismissed: write one LEVEL_UP_OFFER row to notifications

### Shared handler

Both dialog buttons and Notifications list call same `applyLevelChoice(choice)` function:
- Updates level and levelStartDate
- Marks matching notifications row resolved
- One function, one code path

---

## Profile Edit Rules

### Normal edits (height, weight, event, goal)

- Save to SQLite
- Next lazy-generation uses new values automatically
- No separate regeneration step needed

### Level change (special case)

- Wipe all tasks: `DELETE FROM tasks`
- Update level and levelStartDate
- Start clean immediately

---

## Navigation Rules

### Screen access

- MainActivity = launcher
- Bottom nav bar: Home, Target, Analytics, Profile
- Notifications: bell icon from Home only

### Navigation behavior

- Declare all Activities in AndroidManifest.xml
- Call `finish()` when moving forward from Welcome
- If saved profile exists, START goes straight to Home

---

## Offline Operation Rules

**Required:**
- All screens work without internet
- All data from local SQLite
- Charts generated from local data
- Reminders use OS-level AlarmManager (no cloud push)

**Not allowed:**
- Network dependency checks
- Online authentication
- Cloud sync
- External APIs
- Runtime AI/ML calls

---

## UI Rules

### Positioning & labels

Use neutral, educational labels:
- "Today's Tasks" not "Your Workout"
- "Suggested Activity" not "Required Training"
- "Recovery Day" not "Rest Day"
- "Track Your Progress" not "Your Stats"

### Design constraints

- No profile pictures
- No complex animations
- No GPS/distance/speed tracking UI
- Consistent color scheme across all screens
- Clean, simple layouts

---

## Scope Control Rules

### Implementation priority (if deadline tight)

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

### Definition of done

A task is complete when:
- Required behavior is implemented
- Affected code compiles
- Relevant validation exists
- Existing behavior remains intact
- No prohibited dependency/scope change introduced
- Affected flow has been tested
- Obvious edge cases checked
- Developer received concise completion report

---

## Code Quality Rules

### Simplicity principles

- Reuse existing code over new abstraction
- Small targeted change over refactor
- Fix root cause over symptom patches
- Single source of truth for each concern
- No new architecture when existing pattern works

### Token-saving priority

```text
Reuse existing code
    >
Small targeted change
    >
Small helper
    >
Refactor only if necessary
    >
New architecture
```

New architecture is the last option, not the first.

---

## Testing Rules

### Required verification

- Project builds reliably
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

### Testing approach

- Test smallest affected area first
- Verify behavior, not just code existence
- Check edge cases: day 1, day 28, Sunday, level change
- Airplane mode testing required

---

## Production-Readiness Definition

"Production ready" for this project means:
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

It does **not** mean enterprise infrastructure, cloud backup, professional coaching, or production-scale backend systems.

---

## Honest Limits Statement

RUNOVA has no GPS, distance, speed or heart-rate tracking. Nothing is verified, so a user can mark a task done without doing it. The start-up pop-up says this plainly.

**Positioning:** a training organizer and educational prototype, **not** medical advice or professional coaching.

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
