# RUNOVA — Training Plan Generation Engine

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8**

---

## Core Principle

> **Static approved training data + deterministic generation + SQLite persistence.**

There is no runtime AI training engine. No random workouts. No dynamic algorithms.

---

## Generation Strategy: Lazy, One Day at a Time

**Never generate tasks in advance.** Generate only when needed, for the requested date.

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

That's the entire generator. No date-range math, no per-week caching, nothing to invalidate.

---

## Component Architecture

### LevelPlan (Static Weekly Templates)

Contains approved Monday–Sunday templates for Beginner, Intermediate, and Pro.

**Structure:**
- One template per level
- Seven day templates per level (Monday–Sunday)
- Each day template contains: task count, task names, task types, task durations, sort order
- Templates never change at runtime

**Example conceptual structure:**
```java
public class LevelPlan {
    public static DayTemplate getTemplate(String level, int weekday) {
        // weekday: 1=Monday, 2=Tuesday, ..., 7=Sunday
        // Returns static template for that level and weekday
    }
}

public class DayTemplate {
    public List<TaskTemplate> tasks;
}

public class TaskTemplate {
    public String name;
    public String type;
    public int minutes;
    public int sortOrder;
}
```

**Beginner Monday example:**
```java
// 3 tasks / 60 min
tasks = [
    { name: "Warm-up", type: "WARMUP", minutes: 10, sortOrder: 1 },
    { name: "Easy Run", type: "EASY_RUN", minutes: 40, sortOrder: 2 },
    { name: "Cool-down", type: "COOLDOWN", minutes: 10, sortOrder: 3 }
]
```

### TrainingContentLibrary (Static Content & Event Adaptation)

Contains task instructions, drill/exercise groups, and event-specific wording.

**Responsibilities:**
1. Provide short instruction strings for each task type
2. Apply event-specific wording to interval/technique tasks
3. Return detailed descriptions for Target screen

**Example conceptual structure:**
```java
public class TrainingContentLibrary {
    public static String getInstruction(String taskType, String event) {
        // Returns short instruction for task
        // Applies event-specific wording where relevant
    }
    
    public static String getDetailedContent(String taskType, String event) {
        // Returns detailed exercise groups or instructions
        // E.g., "Strength & Core" returns list of exercises
    }
}
```

**Event adaptation example:**
```java
// General Running event
getInstruction("INTERVAL_RUN", "General Running") 
→ "Controlled faster/easier running repetitions"

// Sprint event
getInstruction("INTERVAL_RUN", "Sprint") 
→ "Short fast efforts with full recovery"
```

---

## Generation Flow

### Step 1: Check if tasks exist

```java
public void generateDailyTasks(String date) {
    // date format: YYYY-MM-DD
    
    List<Task> existing = db.getTasks(date);
    if (!existing.isEmpty()) {
        return; // Already generated, do nothing
    }
    
    // Continue to Step 2
}
```

**Important:** Past and already-generated days are never touched again.

### Step 2: Get profile configuration

```java
Profile profile = db.getProfile();
String level = profile.level;     // Beginner, Intermediate, Pro
String event = profile.event;     // Sprint, Middle Distance, etc.
```

### Step 3: Get weekday

```java
int weekday = DateUtils.getWeekday(date);  // 1=Mon, 2=Tue, ..., 7=Sun
```

### Step 4: Get static template

```java
DayTemplate template = LevelPlan.getTemplate(level, weekday);
```

### Step 5: Apply event content

```java
for (TaskTemplate taskTemplate : template.tasks) {
    Task task = new Task();
    task.date = date;
    task.name = taskTemplate.name;
    task.type = taskTemplate.type;
    task.minutes = taskTemplate.minutes;
    task.sortOrder = taskTemplate.sortOrder;
    task.status = "Pending";
    
    // Event-specific wording applied here if task type is interval/technique
    String instruction = TrainingContentLibrary.getInstruction(
        taskTemplate.type, 
        event
    );
    
    // Store instruction with task or fetch on-demand from library
    tasks.add(task);
}
```

### Step 6: Save to SQLite

```java
db.insertTasks(tasks);
```

### Step 7: Return tasks

```java
return db.getTasks(date);
```

---

## Profile Changes & Regeneration

### Normal profile edits (height, weight, event, goal)

**No regeneration needed.** Next lazy-generation call automatically uses new values.

```text
User edits event from "General Running" to "Sprint":
    UPDATE profile SET event = 'Sprint' WHERE id = 1
    
Tomorrow's tasks not generated yet, so:
    Next generateDailyTasks() call reads profile.event = 'Sprint'
    → applies Sprint-specific wording to interval tasks
    → saves to SQLite
    
No separate regeneration step required.
```

### Level change (special case)

**Wipe everything and start clean.**

```java
public void changeLevel(String newLevel) {
    db.deleteAllTasks();
    db.updateProfile(
        level = newLevel,
        levelStartDate = today
    );
}
```

Reason: Different levels have different task counts and durations. Simplest approach is clean slate.

---

## Weekly Pattern Repetition

The same weekly pattern repeats for all 4 weeks of the 28-day level cycle.

**No week-to-week progression.** Week 1 = Week 2 = Week 3 = Week 4.

```text
Day 1 (Mon, Week 1): Beginner Monday template
Day 2 (Tue, Week 1): Beginner Tuesday template
...
Day 7 (Sun, Week 1): Beginner Sunday template (Rest)
Day 8 (Mon, Week 2): Beginner Monday template (same as Day 1)
...
Day 28 (Sun, Week 4): Beginner Sunday template (same as Day 7)
```

---

## Implementation Rules

### DO implement:
- Static `LevelPlan` with approved Monday–Sunday templates for each level
- Static `TrainingContentLibrary` with task instructions and event wording
- Lazy generation checking date existence in SQLite first
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

## Task Count Pattern by Level & Weekday

| Level | Mon | Tue | Wed | Thu | Fri | Sat | Sun |
|---|---:|---:|---:|---:|---:|---:|---:|
| Beginner | 3 | 4 | 3 | 4 | 3 | 4 | 2 |
| Intermediate | 3 | 4 | 5 | 3 | 4 | 5 | 2 |
| Pro | 4 | 5 | 4 | 5 | 4 | 5 | 2 |

**Task breakdown:**
- Training days: includes warm-up and cool-down in count
- Sunday: 2 recovery tasks (Easy Walk + Stretch), no warm-up/cool-down

---

## Duration Allocation by Level

| Level | Warm-up | Cool-down | Daily max |
|---|---:|---:|---:|
| Beginner | 10 min | 10 min | 60 min |
| Intermediate | 15 min | 15 min | 90 min |
| Pro | 15 min | 15 min | 120 min |

**Important:** Use exact durations from approved routines. Do not recalculate at runtime using even-split formulas.

---

## Event-Specific Task Wording

Event selection changes relevant interval/technique task display names and instructions.

| Event | Interval task wording |
|---|---|
| General Running | Interval Run |
| Middle Distance | Interval Run |
| Long Distance | Interval Run |
| Sprint | Sprint Repeats / Speed Intervals |
| Hurdles | Hurdle Technique + Sprint Repeats |
| Relay | Relay Exchange Practice |

Content emphasis also changes:
- Sprint: shorter fast efforts with full recovery
- Long Distance: longer controlled repetitions with endurance emphasis
- Hurdles: basic hurdle movement concepts (no physical hurdles required)

---

## Exercise Grouping Within Tasks

Multiple exercises inside a task remain **one database row**.

**Example:**
```text
Task row in SQLite:
{
    name: "Strength & Core",
    type: "STRENGTH_CORE",
    minutes: 25,
    status: "Pending"
}

Detailed content from TrainingContentLibrary:
"- Squats
 - Reverse lunges
 - Glute bridges
 - Calf raises
 - Plank"
```

This is **one task**, not five tasks. Do not create separate database rows for each exercise.

---

## Generator Simplicity Goals

**Why lazy generation is better:**
- No future tasks to invalidate when profile changes
- No complex date-range logic
- No caching or staleness issues
- Profile edits automatically reflected in next generation
- Simple: "does row exist for date? if not, create it"

**Why static templates are better:**
- No runtime randomness to debug
- Deterministic behavior every time
- Easy to test and verify
- No AI/ML dependencies
- Approved routines preserved exactly as specified

---

## Data Flow Summary

```text
HomeActivity or TargetActivity opens:
    ↓
generateDailyTasks(today)
    ↓
Check: tasks exist for today?
    YES → return existing tasks
    NO → continue
    ↓
Read profile (level, event)
    ↓
Get weekday from date
    ↓
template = LevelPlan.getTemplate(level, weekday)
    ↓
For each task in template:
    - Create Task row
    - Apply event-specific wording if relevant
    - Set status = "Pending"
    ↓
Insert tasks into SQLite
    ↓
Return tasks for today
```

---

## Testing the Generator

**Test cases:**

1. **First launch:** Generate tasks for today, verify correct level template used
2. **Weekend:** Generate Sunday tasks, verify 2 recovery tasks (no warm-up/cool-down)
3. **Profile edit (event):** Change event, generate tomorrow, verify event-specific wording
4. **Profile edit (level):** Change level, verify all tasks wiped and new level used
5. **Revisit past day:** Verify generator does not regenerate past days
6. **28-day cycle:** Generate all 28 days, verify pattern repeats (Day 8 = Day 1)

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8
