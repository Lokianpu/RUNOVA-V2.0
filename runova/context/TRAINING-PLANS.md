# RUNOVA — Approved Training Plans

> **Extracted from RUNOVA_Project_Overview:Agent_Specs.md V8 Section 7**

---

## Design Principles

RUNOVA's training content is intentionally **structured, realistic, deterministic, and simple to implement**. The app is not a random workout generator and does not create individualized coaching plans at runtime.

**Training flow:**
```text
WARM-UP → RUNNING DRILL / ACTIVATION (when scheduled) → MAIN TRAINING
→ OPTIONAL STRENGTH / CONDITIONING / MOBILITY → COOL-DOWN
```

**Important:** The flow above describes the **content inside the day's task slots**. Do not create one database row for every individual exercise. A task such as `Strength & Core` contains a short group of exercises/instructions inside its task description while remaining one SQLite `tasks` row.

---

## Activity Categories

1. **Warm-up** — easy movement and dynamic preparation before training
2. **Running Drills** — simple running mechanics/activation drills (marching, high knees, butt kicks, A-skips, controlled strides)
3. **Easy Running** — relaxed aerobic running at comfortable, conversational effort
4. **Tempo Running** — sustained controlled running at stronger but manageable effort
5. **Interval Training** — repeated faster running with recovery periods; event wording changes where appropriate
6. **Strength & Core** — bodyweight strength and trunk stability using simple movements
7. **Cross-Training / Mobility** — low-impact conditioning or mobility work (brisk walking, cycling, light stretching)
8. **Recovery** — Sunday Easy Walk + Stretch

This is an educational training organizer. The routines are intentionally general rather than individualized medical or professional coaching prescriptions.

---

## Fixed Weekly Task Pattern

| Level | Mon | Tue | Wed | Thu | Fri | Sat | Sun |
|---|---:|---:|---:|---:|---:|---:|---:|
| Beginner | 3 | 4 | 3 | 4 | 3 | 4 | Rest (2) |
| Intermediate | 3 | 4 | 5 | 3 | 4 | 5 | Rest (2) |
| Pro | 4 | 5 | 4 | 5 | 4 | 5 | Rest (2) |

**Task row breakdown:**
- `3 tasks` = Warm-up + 1 main task + Cool-down
- `4 tasks` = Warm-up + 2 main tasks + Cool-down
- `5 tasks` = Warm-up + 3 main tasks + Cool-down
- Sunday = exactly 2 recovery tasks: Easy Walk + Stretch

This pattern repeats for every week of the 28-day level cycle. There is **no week-to-week ramp** and no random task selection.

---

## Duration Rules

| Level | Daily maximum | Approved routine range |
|---|---:|---:|
| Beginner | 60 min | 30–60 min |
| Intermediate | 90 min | 30–90 min |
| Pro | 120 min | 40–120 min |

**Warm-up and cool-down durations (fixed by level):**

| Level | Warm-up | Cool-down |
|---|---:|---:|
| Beginner | 10 min | 10 min |
| Intermediate | 15 min | 15 min |
| Pro | 15 min | 15 min |

**Important:** Use the listed task durations exactly. Do not recalculate them at runtime using an even-split formula. The approved schedule is the source of truth.

---

## Beginner Weekly Routine

Beginner emphasizes basic movement, consistency, easy aerobic work, simple strength, and recovery. Intentionally manageable for a new or recreational runner.

### Sunday — Rest (2 tasks / 30 min)
- Easy Walk 15 min
- Stretch 15 min

Recovery-focused; no warm-up/cool-down.

### Monday (3 tasks / 60 min)
- Warm-up 10 min
- Easy Run 40 min
- Cool-down 10 min

Comfortable running; focus on consistency.

### Tuesday (4 tasks / 60 min)
- Warm-up 10 min
- Running Drills 15 min
- Strength & Core 25 min
- Cool-down 10 min

Simple mechanics plus bodyweight conditioning.

**Strength & Core example:** bodyweight squats, reverse lunges, glute bridges, calf raises, plank. Keep exercise group inside single task; do not create five separate database tasks.

**Running Drills example:** marching, high knees, butt kicks, A-skips, controlled strides. Simple instructions, no special equipment required.

### Wednesday (3 tasks / 60 min)
- Warm-up 10 min
- Cross-Training 40 min
- Cool-down 10 min

Low-impact aerobic work such as brisk walking or cycling.

### Thursday (4 tasks / 60 min)
- Warm-up 10 min
- Interval Run 30 min
- Mobility & Stretch 10 min
- Cool-down 10 min

Controlled faster/easier running repetitions.

### Friday (3 tasks / 60 min)
- Warm-up 10 min
- Strength & Core 40 min
- Cool-down 10 min

Basic lower-body and core work.

### Saturday (4 tasks / 60 min)
- Warm-up 10 min
- Easy Run 30 min
- Running Drills 10 min
- Cool-down 10 min

Easy aerobic running plus technique practice.

---

## Intermediate Weekly Routine

Intermediate adds controlled tempo work, more structured intervals, longer running, and more varied strength/technique work while staying within the existing 90-minute daily limit.

### Sunday — Rest (2 tasks / 30 min)
- Easy Walk 15 min
- Stretch 15 min

Recovery day.

### Monday (3 tasks / 75 min)
- Warm-up 15 min
- Easy Run 45 min
- Cool-down 15 min

Comfortable aerobic running.

### Tuesday (4 tasks / 90 min)
- Warm-up 15 min
- Interval Run 30 min
- Strength & Core 30 min
- Cool-down 15 min

Controlled speed-endurance work plus strength.

**Strength & Core example:** squats, reverse lunges, single-leg glute bridges, calf raises, plank, side plank. Keep grouped inside single task.

### Wednesday (5 tasks / 90 min)
- Warm-up 15 min
- Easy Run 25 min
- Tempo Run 20 min
- Strength & Core 15 min
- Cool-down 15 min

Combines aerobic running, controlled tempo, and short conditioning.

### Thursday (3 tasks / 75 min)
- Warm-up 15 min
- Easy Run 45 min
- Cool-down 15 min

Aerobic maintenance/recovery-oriented running.

### Friday (4 tasks / 75 min)
- Warm-up 15 min
- Running Drills 10 min
- Interval Run 35 min
- Cool-down 15 min

Technique followed by structured faster running.

### Saturday (5 tasks / 90 min)
- Warm-up 15 min
- Easy/Long Run 40 min
- Strides 10 min
- Mobility 10 min
- Cool-down 15 min

Longer aerobic work with short controlled strides and mobility.

---

## Pro Weekly Routine

Pro is the highest fixed level in RUNOVA. It adds more speed work, longer aerobic sessions, technique, strength, and mobility while remaining a simple static schedule rather than a professional individualized program.

### Sunday — Rest (2 tasks / 40 min)
- Easy Walk 20 min
- Mobility & Stretch 20 min

Recovery-focused day.

### Monday (4 tasks / 75 min)
- Warm-up 15 min
- Running Drills 15 min
- Speed Intervals 30 min
- Cool-down 15 min

Technique and controlled speed work.

### Tuesday (5 tasks / 120 min)
- Warm-up 15 min
- Easy Run 45 min
- Strength & Core 30 min
- Mobility 15 min
- Cool-down 15 min

Aerobic work plus full-body conditioning and mobility.

**Strength & Core example:** split squats, controlled single-leg squat variation, single-leg glute bridges, single-leg calf raises, side plank, plank, simple hamstring exercise. Keep exercise group inside single task.

### Wednesday (4 tasks / 75 min)
- Warm-up 15 min
- Running Drills 10 min
- Tempo Run 35 min
- Cool-down 15 min

Running mechanics plus sustained controlled effort.

### Thursday (5 tasks / 105 min)
- Warm-up 15 min
- Easy Run 45 min
- Technique 15 min
- Mobility 15 min
- Cool-down 15 min

Aerobic maintenance, technique, and mobility.

### Friday (4 tasks / 75 min)
- Warm-up 15 min
- Running Drills 10 min
- Interval Run 35 min
- Cool-down 15 min

Structured speed-endurance session.

### Saturday (5 tasks / 115 min)
- Warm-up 15 min
- Easy/Long Run 60 min
- Strides 10 min
- Mobility 15 min
- Cool-down 15 min

Long aerobic session with controlled strides and mobility.

---

## Task Content Library

Database remains intentionally small. Task rows store activity name/type/minutes/status; detailed instructions come from static Java content library.

| Task / category | Example contents shown to user | Purpose |
|---|---|---|
| Warm-up | Easy walk/jog, leg swings, arm circles, marching, dynamic mobility | Prepare body for session |
| Running Drills | Marching, high knees, butt kicks, A-skips, controlled strides | Practice basic running mechanics |
| Easy Run | Relaxed continuous running at comfortable effort | Aerobic base / consistency |
| Tempo Run | Controlled sustained running at stronger but manageable effort | Aerobic strength |
| Interval Run | Easy running + repeated faster efforts + recovery periods | Speed endurance |
| Speed Intervals | Shorter faster repetitions with recovery | Speed-oriented work at Pro level |
| Strength & Core | Squats, lunges/split squats, glute bridges, calf raises, plank/side plank | Basic strength and stability |
| Cross-Training | Brisk walking, cycling, or another simple low-impact option | Aerobic conditioning with reduced running load |
| Mobility & Stretch | Light lower-body mobility and stretching | Recovery and movement quality |
| Technique | Running-form drills and controlled strides | Technique reinforcement |
| Easy/Long Run | Longer easy continuous running | Aerobic endurance |
| Easy Walk | Comfortable walking | Recovery |
| Stretch | Simple recovery stretching | Recovery |

App may show short instruction string under each task. Keep instructions concise enough for Target screen.

---

## Exercise Grouping Rule

Existing task-count pattern must never be broken just because a task contains multiple exercises.

**Example 1:**
```text
Strength & Core — 25 min
- Squats
- Reverse lunges
- Glute bridges
- Calf raises
- Plank
```
This is **one task**, not five tasks.

**Example 2:**
```text
Interval Run — 30 min
- 5 min easy running
- 4 × 2 min faster / 2 min easy
- Easy running to finish
```
This is **one timed task** and uses the existing single-task timer.

---

## Event-Specific Adaptation

Event selection remains simple and does not create new database task types. It changes the relevant interval/technique task's display name and its static instruction content.

| Event | Relevant task wording | Example content emphasis |
|---|---|---|
| General Running | Interval Run | Controlled faster/easier running repetitions |
| Middle Distance | Interval Run | Speed endurance and controlled intervals |
| Long Distance | Interval Run | Longer controlled repetitions with endurance emphasis |
| Sprint | Sprint Repeats / Speed Intervals | Shorter fast efforts with full recovery emphasis |
| Hurdles | Hurdle Technique + Sprint Repeats | Basic hurdle movement/technique concepts plus controlled sprint work; no physical hurdles required |
| Relay | Relay Exchange Practice | Basic exchange/coordination practice concepts; no separate database type |

**Scope rule:** Event adaptation must remain content-level. Do not add new tables, new Activity classes, or complex event-specific training engine.

---

## Goal vs Level vs Event

- **Level** controls actual weekly routine, task count, duration, and difficulty tier
- **Event** changes relevant task wording/content only
- **Goal** remains user-selected display label and does not generate different plan

This separation prevents the same training rule from being implemented in multiple places.

---

## Implementation Rules

**Do NOT implement:**
- Random workouts
- Runtime AI-generated workouts
- Automatic progression algorithms
- Per-user difficulty calculations
- Week-to-week ramp

**DO implement:**
- Static weekly templates in `LevelPlan`
- Task names, instructions, drill/exercise groups in `TrainingContentLibrary`
- Event-specific content variations
- Lazy generation using approved routines exactly as specified

---

## Activity-Only Rule

Tasks are activity/training items only: running, drills, strength, cross-training, warm-up, cool-down, mobility, recovery.

Do NOT add: nutrition, hydration, sleep, meditation, supplement, or medical-advice tasks.

---

**Reference:** RUNOVA_Project_Overview:Agent_Specs.md V8 Section 7
