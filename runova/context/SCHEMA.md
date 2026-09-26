# RUNOVA — Database Schema

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8**

---

## Database Overview

- **Storage:** SQLite (Room or SQLiteOpenHelper)
- **Tables:** Exactly 3 (no additions allowed without developer approval)
- **Pattern:** Simple, offline, deterministic

---

## Table 1: profile

**Single row containing all user identity and configuration data.**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | INTEGER | PRIMARY KEY | Always 1 (single row) |
| firstName | TEXT | NOT NULL | From Sign Up |
| lastName | TEXT | NOT NULL | From Sign Up |
| dateOfBirth | TEXT | NOT NULL | ISO date format (YYYY-MM-DD) |
| gender | TEXT | NOT NULL | Male, Female, Other |
| height | REAL | NOT NULL | cm, validated 100–250 |
| weight | REAL | NOT NULL | kg, validated 25–250 |
| event | TEXT | NOT NULL | Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running |
| level | TEXT | NOT NULL | Beginner, Intermediate, Pro |
| goal | TEXT | NOT NULL | Display label only; does not affect task generation |
| levelStartDate | TEXT | NOT NULL | ISO date; marks beginning of current 28-day cycle |
| seenIntro | INTEGER | NOT NULL, DEFAULT 0 | Boolean (0/1); controls start-up pop-up display |
| activeTaskId | INTEGER | NULLABLE | Single active timer reference; NULL when no timer active |
| activeTaskEndTime | INTEGER | NULLABLE | Epoch milliseconds; NULL when no timer active |

**Purpose:**
- Single source of truth for user configuration
- Drives task generation via level and event
- Stores single active timer state (app-wide one-timer limit)
- Age derived from dateOfBirth using `DateUtils.ageFrom()`, never stored directly

**Key behaviors:**
- Editing most fields: save to SQLite; next lazy-generation uses new values
- **Changing level:** wipes all tasks and starts clean immediately
- Single row always exists after onboarding

---

## Table 2: tasks

**Stores all generated training tasks.**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | |
| date | TEXT | NOT NULL | ISO date format (YYYY-MM-DD) |
| name | TEXT | NOT NULL | Task display name (e.g., "Warm-up", "Easy Run", "Strength & Core") |
| type | TEXT | NOT NULL | Task category for logic/filtering |
| minutes | INTEGER | NOT NULL | Task duration |
| status | TEXT | NOT NULL, DEFAULT 'Pending' | Pending, Finished, Missed |
| sortOrder | INTEGER | NOT NULL | Display order (1, 2, 3...) |

**Indexes:**
- Index on `date` for fast date-based queries

**Purpose:**
- Stores all generated tasks from lazy generation
- Each row = one task shown to user
- Multiple exercises inside task remain one row (e.g., "Strength & Core" with 5 exercises = 1 row)

**Status rules:**
- **Pending:** today's task, not yet completed
- **Finished:** task has saved completion
- **Missed:** past task with no completion
- **Day status (derived):** Finished only when all tasks for that date are Finished

**Generation:**
- Lazy: generated one day at a time, never in advance
- `generateDailyTasks()` checks if rows exist for date; if not, creates from static template
- Past and already-generated days never touched again

**Queries:**
- Today's tasks: `SELECT * FROM tasks WHERE date = ? ORDER BY sortOrder`
- Week/Month/Year analytics: `SELECT date, status FROM tasks WHERE date BETWEEN ? AND ?`
- Level progress: `SELECT COUNT(*) FROM tasks WHERE date BETWEEN ? AND ? AND status = 'Finished'`
- Streak: consecutive Finished days ending today or yesterday

---

## Table 3: notifications

**Stores in-app notification messages.**

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | |
| type | TEXT | NOT NULL | Message type (currently: LEVEL_UP_OFFER) |
| message | TEXT | NOT NULL | Message content shown to user |
| created | INTEGER | NOT NULL | Epoch milliseconds |
| resolved | INTEGER | NOT NULL, DEFAULT 0 | Boolean (0/1); 1 = user acted on message |

**Purpose:**
- In-app bell-icon inbox on Home screen
- Currently used for: level-up offer only
- Separate from OS-level Reminders (NotificationManager/AlarmManager)

**Level-up offer behavior:**
- At day 28, if dialog dismissed without choice: insert one LEVEL_UP_OFFER row
- Only one unresolved LEVEL_UP_OFFER at a time (check before inserting)
- Both dialog buttons and Notifications list call same `applyLevelChoice(choice)` function
- After choice: mark row resolved (set `resolved = 1`)

**Queries:**
- Unresolved notifications: `SELECT * FROM notifications WHERE resolved = 0 ORDER BY created DESC`
- Check for existing offer: `SELECT COUNT(*) FROM notifications WHERE type = 'LEVEL_UP_OFFER' AND resolved = 0`

---

## Data Flow & Generation

### Task generation (lazy)

```text
if no tasks exist in SQLite for today's date:
    level = profile.level
    event = profile.event
    weekday = current weekday
    template = LevelPlan.getTemplate(level, weekday)
    tasks = TrainingContentLibrary.applyEventContent(template, event)
    save tasks to SQLite with:
        - date = today
        - name, type, minutes from template
        - status = 'Pending'
        - sortOrder = 1, 2, 3...
show tasks for today's date
```

### Task completion

```text
User completes task in Target:
    UPDATE tasks SET status = 'Finished' WHERE id = ?
    
Day status (derived, not stored):
    SELECT COUNT(*) FROM tasks WHERE date = ? AND status != 'Finished'
    if count = 0: day is Finished
    else: day is Pending or Missed
```

### Timer state

```text
User starts timer:
    UPDATE profile SET activeTaskId = ?, activeTaskEndTime = ? WHERE id = 1
    
Screen reopens:
    SELECT activeTaskId, activeTaskEndTime FROM profile WHERE id = 1
    remainingMs = activeTaskEndTime - currentTimeMs
    if remainingMs > 0: resume timer
    else: timer finished
    
User completes task:
    UPDATE tasks SET status = 'Finished' WHERE id = ?
    UPDATE profile SET activeTaskId = NULL, activeTaskEndTime = NULL WHERE id = 1
```

### Level cycle check

```text
Day 28 reached:
    levelStartDate = SELECT levelStartDate FROM profile WHERE id = 1
    daysSince = (today - levelStartDate)
    if daysSince >= 28:
        finishedDays = COUNT days where all tasks Finished in cycle
        passRate = finishedDays / 28
        if passRate >= 0.85: passed
        else: not passed
        show dialog or create LEVEL_UP_OFFER notification
```

### Profile editing

```text
User edits profile:
    UPDATE profile SET field = value WHERE id = 1
    
    if field = level:
        DELETE FROM tasks  -- wipe all tasks
        UPDATE profile SET levelStartDate = today WHERE id = 1
    
    next generateDailyTasks() uses new values automatically
```

---

## Schema Creation

### SQLite / SQLiteOpenHelper

```sql
CREATE TABLE profile (
    id INTEGER PRIMARY KEY,
    firstName TEXT NOT NULL,
    lastName TEXT NOT NULL,
    dateOfBirth TEXT NOT NULL,
    gender TEXT NOT NULL,
    height REAL NOT NULL,
    weight REAL NOT NULL,
    event TEXT NOT NULL,
    level TEXT NOT NULL,
    goal TEXT NOT NULL,
    levelStartDate TEXT NOT NULL,
    seenIntro INTEGER NOT NULL DEFAULT 0,
    activeTaskId INTEGER,
    activeTaskEndTime INTEGER
);

CREATE TABLE tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    minutes INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'Pending',
    sortOrder INTEGER NOT NULL
);

CREATE INDEX idx_tasks_date ON tasks(date);

CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    message TEXT NOT NULL,
    created INTEGER NOT NULL,
    resolved INTEGER NOT NULL DEFAULT 0
);
```

### Room (if used instead)

```java
@Entity(tableName = "profile")
public class Profile {
    @PrimaryKey
    public int id = 1;
    
    @NonNull
    public String firstName;
    
    @NonNull
    public String lastName;
    
    @NonNull
    public String dateOfBirth;  // ISO date
    
    @NonNull
    public String gender;
    
    public double height;  // cm
    public double weight;  // kg
    
    @NonNull
    public String event;
    
    @NonNull
    public String level;
    
    @NonNull
    public String goal;
    
    @NonNull
    public String levelStartDate;  // ISO date
    
    public boolean seenIntro = false;
    
    @Nullable
    public Integer activeTaskId;
    
    @Nullable
    public Long activeTaskEndTime;  // epoch ms
}

@Entity(tableName = "tasks", indices = {@Index("date")})
public class Task {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @NonNull
    public String date;  // ISO date
    
    @NonNull
    public String name;
    
    @NonNull
    public String type;
    
    public int minutes;
    
    @NonNull
    public String status = "Pending";
    
    public int sortOrder;
}

@Entity(tableName = "notifications")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @NonNull
    public String type;
    
    @NonNull
    public String message;
    
    public long created;  // epoch ms
    
    public boolean resolved = false;
}
```

---

## Schema Rules

**Non-negotiable:**
- Exactly 3 tables
- No new tables without developer approval
- No new columns without developer approval
- No foreign keys (simple flat structure)
- No complex joins
- All queries simple and fast

**Consistency:**
- All dates stored as ISO 8601 strings (YYYY-MM-DD)
- All timestamps stored as epoch milliseconds (long)
- All booleans stored as INTEGER (0/1)
- All enums stored as TEXT

**Performance:**
- Index on `tasks.date` for fast date-based queries
- Single-row `profile` table = instant read
- No N+1 query problems (all queries use WHERE clauses)

---

## Data Integrity

**Profile:**
- Always exactly one row
- All fields NOT NULL except activeTaskId and activeTaskEndTime
- Validation enforced in UI before saving

**Tasks:**
- Generated rows never modified except status field
- sortOrder determines display order
- date field never changed after creation

**Notifications:**
- Only one unresolved LEVEL_UP_OFFER at a time
- resolved flag prevents duplicate actions

---

## Migration Strategy (if schema changes)

**Current version:** V1 (initial)

**If schema must change:**
1. Stop and notify developer (per agent control rules)
2. Create migration plan with backward compatibility
3. Test migration on demo data
4. Never lose user data
5. Provide rollback plan

**Current project scope:** No migrations planned; schema is final.

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
