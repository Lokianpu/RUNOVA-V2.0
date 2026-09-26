# RUNOVA — Project Overview V8
## AI Development Specification & Controlled Implementation Plan

> **STATUS: DEVELOPMENT CONTROL DOCUMENT**
>
> This document is the authoritative development specification for the RUNOVA project.
> It preserves the approved RUNOVA V7 concept, architecture, features, training plan,
> database design, navigation, UI direction, and scope. The additions in this V8 version
> define **how an AI coding agent must work**, how the developer retains control, how
> changes are proposed, and how limited development time and AI token usage are managed.
>
> **Important:** This is not permission to redesign RUNOVA. It is a stricter execution
> layer over the already-approved project.

---

# 0. AI AGENT CONTROL CHARTER

## 0.1 Mission

The coding agent's mission is:

> **Implement the approved RUNOVA project accurately, simply, deterministically, and
> within the existing architecture — not invent a better version of RUNOVA.**

The agent is an implementation assistant, not the product owner, architect, UX decision-maker,
or requirements approver.

The agent must optimize for:

1. Correctness
2. Alignment with this document
3. Minimal scope
4. Stable architecture
5. Fast development
6. Low token/context consumption
7. Easy debugging
8. Developer visibility and control
9. Offline reliability
10. Demo readiness

Do **not** optimize for feature count, architectural sophistication, code volume, or visual
complexity.

---

## 0.2 Authority hierarchy

When two instructions appear to conflict, use this order:

```text
LEVEL 1 — Developer's explicit current instruction
             ↓
LEVEL 2 — This V8 Development Specification
             ↓
LEVEL 3 — Approved V7 RUNOVA requirements preserved below
             ↓
LEVEL 4 — Existing project code, only when it does not conflict with Levels 1–3
             ↓
LEVEL 5 — Normal Android/Java best practices
```

A lower level must never silently override a higher level.

If the developer's instruction would change an approved requirement, the agent must stop
before implementing the change and report the conflict.

---

## 0.3 Non-negotiable project boundaries

The agent MUST NOT add any of the following without explicit developer approval:

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

**Rule:** If a feature sounds useful but is not explicitly required, it is out of scope
until the developer approves it.

---

## 0.4 Developer remains the decision-maker

The agent must never silently decide:

- whether a requirement should be changed;
- whether a feature should be removed;
- whether a database schema should change;
- whether a new dependency should be added;
- whether an Activity should be replaced;
- whether an approved training routine should change;
- whether a task duration should change;
- whether a user-flow should change;
- whether a validation rule should change;
- whether a destructive data operation is acceptable;
- whether a deadline trade-off should be made.

The agent may **recommend** an option, but the developer decides.

When approval is needed, the agent should provide:

```text
ISSUE
What was discovered.

WHY IT MATTERS
What could break or conflict.

CURRENT RULE
Which RUNOVA requirement is involved.

OPTIONS
A. ...
B. ...

RECOMMENDATION
The simplest option and why.

WAITING FOR
Developer decision.
```

Then stop that part of the work.

---

## 0.5 Stop-and-ask conditions

The agent MUST stop and notify the developer before continuing when:

1. Two requirements conflict.
2. The requested change would alter the SQLite schema.
3. A new dependency appears necessary.
4. A requested feature is outside the approved scope.
5. The agent discovers that existing code contradicts this specification.
6. An implementation requires deleting or migrating user data unexpectedly.
7. A training task, duration, task count, level, or event rule appears ambiguous.
8. A UI change would alter an approved user flow.
9. A requested shortcut could make the app unreliable or non-offline.
10. The agent is unsure which interpretation is correct.
11. A change would create substantial refactoring.
12. The agent encounters a build/system problem that cannot be fixed locally and safely.
13. A proposed fix could affect multiple unrelated features.
14. The agent believes a requirement itself may contain an error.
15. The remaining development time makes the requested change risky.

**Never guess when a decision belongs to the developer.**

---

## 0.6 Safe autonomy

The agent MAY make small implementation decisions when all of the following are true:

- The decision is internal to implementation.
- It does not change visible requirements.
- It does not change the database contract.
- It does not change navigation.
- It does not change approved training content.
- It does not add dependencies.
- It does not increase scope.
- It can be reversed easily.
- It is consistent with existing code and Android conventions.

Examples:

- choosing a private helper method name;
- extracting duplicated code into a small utility;
- choosing a safe null check;
- improving a local variable name;
- using a standard Android layout technique without changing the design.

Even then, keep the change small.

---

# 0.7 WORKING RULE: READ → PLAN → CHANGE → VERIFY → REPORT

Before modifying code, the agent follows this cycle:

```text
READ
  ↓
Understand the relevant existing files and requirements
  ↓
PLAN
  ↓
State the smallest implementation approach
  ↓
CHANGE
  ↓
Modify only required files
  ↓
VERIFY
  ↓
Build/test the affected behavior
  ↓
REPORT
  ↓
Tell the developer what changed, what was tested,
what remains, and whether any risk/conflict was found
```

Never jump directly from a feature request to broad code changes.

---

# 0.8 Token and AI-cost control

RUNOVA has limited development time and limited AI/coding-agent budget.

The agent MUST treat context and token usage as development resources.

### Use context efficiently

- Read only the files relevant to the current task.
- Do not repeatedly re-read the entire project.
- Use this document as the requirements reference.
- Keep implementation plans short.
- Do not generate large explanations when a short status is enough.
- Do not rewrite working code unnecessarily.
- Do not refactor unrelated classes.
- Do not regenerate complete files when a small patch is sufficient.
- Reuse existing helpers and components.
- Fix the root cause rather than repeatedly patching symptoms.
- Test the smallest affected area first.
- Batch closely related inspections where practical.
- Keep temporary/debug code out of the final project.

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

### AI-agent communication rule

Do not spend tokens explaining obvious implementation details to the developer.
Report only:

- what changed;
- why it changed;
- what was verified;
- what failed;
- what decision is needed.

---

# 0.9 Change budget

For each development task, the agent should estimate:

```text
TASK
Files likely affected
Expected implementation size
Expected testing
Risk: Low / Medium / High
```

If the task unexpectedly expands beyond the initial scope, stop and report it.

Example:

```text
TASK: Add Target timer persistence
EXPECTED: TargetActivity + timer helper + Profile timer fields
RISK: Medium
FOUND: Existing timer state is handled in three unrelated places
ACTION: STOP — this requires architectural cleanup. Developer decision needed.
```

---

# 0.10 No silent scope creep

The agent must not say:

> "While I was here, I also improved..."

unless the improvement was explicitly approved.

The following are examples of scope creep:

- redesigning screens while implementing validation;
- changing colors while fixing a database bug;
- replacing SQLite because another approach is "better";
- adding animations because the UI feels static;
- adding extra statistics to Analytics;
- adding a search feature;
- adding extra workout types;
- changing the training plan because another plan seems more realistic.

Implement the requested scope first.

---

# 0.11 Definition of done

A task is NOT complete merely because code was written.

A task is complete when:

- the required behavior is implemented;
- affected code compiles;
- relevant validation exists;
- existing behavior remains intact;
- no prohibited dependency/scope change was introduced;
- the affected flow has been tested;
- obvious edge cases were checked;
- the developer has received a concise completion report.

---

# 0.12 Developer status report format

At the end of each meaningful development task, report:

```text
RUNOVA DEVELOPMENT UPDATE

Completed:
- ...

Files changed:
- ...

Behavior implemented:
- ...

Verified:
- ...

Not changed:
- ...

Risks/issues:
- None
  OR
- ...

Developer decision needed:
- None
  OR
- ...

Next recommended step:
- ...
```

For a blocking issue, do not continue silently.

---

# 0.13 Checkpoint rule

After every major stage, create a stable checkpoint.

Recommended checkpoints:

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

A checkpoint means:

- project builds;
- the stage's core behavior works;
- no known blocking issue is ignored;
- the developer receives a status report.

Do not stack many unverified changes together.

---

# 0.14 Implementation priority when time becomes critical

If the deadline becomes tight, protect functionality in this order:

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

Do not sacrifice core correctness to add optional polish.

If a requested change threatens the critical path, notify the developer.

---

# 0.15 Production-readiness rule

"Production ready" for this project means:

- builds reliably;
- no known blocking crashes in the tested flows;
- offline operation works;
- data rules are deterministic;
- approved training routines are preserved;
- validation prevents obvious invalid input;
- navigation is coherent;
- timers and task statuses behave consistently;
- destructive operations are confirmed;
- no accidental network dependency exists;
- no debug/test shortcuts remain in the release flow;
- scope remains aligned with the approved project.

It does **not** mean enterprise infrastructure, cloud backup, professional coaching,
or production-scale backend systems.

---

# 0.16 Core mental model for the AI

Before coding, keep this simplified architecture in mind:

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

The central principle is:

> **Static approved training data + deterministic generation + SQLite persistence.**

There is no runtime AI training engine.

---

# 0.17 Requirement-to-code mapping

Use this mapping before deciding where code belongs:

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

Do not create a new system when an existing area already owns the responsibility.

---

# 0.18 Single source of truth rule

If the coding agent finds old comments, old code, or old documentation that conflicts with
this specification, the agent must NOT automatically preserve the old behavior.

Instead:

1. Identify the conflict.
2. Compare the conflicting code with this document.
3. Report the conflict.
4. Implement the approved specification only after the conflict is understood.
5. Avoid unrelated cleanup.

This is especially important for old versions of:

- flat activity cycling;
- dynamic duration calculation;
- event-only display-name logic;
- task generation;
- level changes;
- analytics;
- notification handling.

---

# 0.19 Approved decision register

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

# 0.20 Before touching the code: agent checklist

The agent should mentally verify:

```text
[ ] What exact requirement am I implementing?
[ ] Which section of this document defines it?
[ ] Which files actually need to change?
[ ] Can existing code already do part of it?
[ ] Am I changing architecture unnecessarily?
[ ] Am I changing the approved training plan?
[ ] Am I changing the database contract?
[ ] Am I adding a dependency?
[ ] Am I adding scope?
[ ] What is the smallest safe implementation?
[ ] How will I verify it?
[ ] What will I report to the developer?
```

If any answer is unclear, stop and ask.

---

# RUNOVA — Approved Product Specification (V7 baseline preserved in V8)

*The single reference for building RUNOVA from scratch. Every rule below is final and decided — there are no open items left. Written for a small team building this in under a week, and for an AI coding agent to follow directly.*

---

## 0. Build philosophy for this revision

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
- Three levels (Beginner, Intermediate, Pro), each with a fixed 7-day approved training routine (Section 7)
- Warm-up and Cool-down as fixed bookend tasks every training day, plus 1–3 grouped main-task slots depending on the day; individual exercises stay inside a task and never become separate DB rows
- Per-task Finished / Missed / Pending status, rolling up to a day status, weekly progress, level progress, streak
- Level-up prompt: in-app dialog at day 28, persisted in Notifications if not acted on immediately
- SQLite storage, input validation, Intent navigation, consistent theme, airplane-mode testing

### Not part of the app
Firebase or any online database, cloud sync, online accounts, AI/ML, GPS, wearables, heart-rate or distance tracking, social features, leaderboards, external APIs, nutrition/hydration/sleep tracking, profile pictures, a full calendar grid view, medical advice, week-to-week difficulty ramps, random workout generation, runtime AI-generated workouts, GPS/wearable integrations, or complex progression algorithms *(simplified — see Section 7)*.

---

## 3. Tech Stack & Architecture

| Piece | Choice |
|---|---|
| Platform / IDE | Android, Java, Android Studio |
| Storage | **SQLite** (Room or SQLiteOpenHelper) — see Section 10 |
| Timer | One `CountDownTimer` at a time app-wide **(simplified — see 8.2)** |
| Notifications | `NotificationManager` + `AlarmManager` for reminders, renamed **"Reminders"** to avoid confusion with the in-app **Notifications** bell (see 3a) |
| Charts | **MPAndroidChart** (approved offline dependency) for Analytics |
| Pattern | MVC-style: Model, View, Controller |
| Dependencies | Default Android Studio project + MPAndroidChart only |

**3a. Two different "notification" concepts — kept separate on purpose:**
- **Reminders** = OS-level pings (`NotificationManager`/`AlarmManager`) that nudge the user to do a task.
- **Notifications** = the in-app bell-icon inbox on Home, currently used for exactly one thing: a persistent level-up offer.

**Model:** `Profile`, `Task`, `LevelPlan` (static approved weekly templates per level) + `TrainingContentLibrary` (static task instructions/event wording), `DBHelper`, `ReminderHelper`, `DateUtils`.
**View:** the Activities and layouts in Section 4.
**Controller:** `generateDailyTasks()`, per-task status calculation, day roll-up, level-cycle check, validation, single active-timer logic, reminder scheduling.

**Data flow:**
1. First launch: pop-up, then Sign Up, then Questionnaire. Everything is saved to SQLite.
2. **Tasks are generated lazily, one day at a time — never in advance (simplified).** The first time Home or Target opens for a given date, `generateDailyTasks()` checks if rows already exist for that date; if not, it creates them from the static weekly pattern and writes them to `tasks`. Past and already-generated days are never touched again.
3. Completing a task updates its row's status. The day's status is derived: Finished only when every task for that date is Finished.
4. Editing the profile updates the saved values. Because tomorrow's tasks haven't been generated yet, the very next lazy-generation call automatically uses the new values — **no separate "regenerate" step is needed.** Changing Level is the one exception (Section 9): it wipes everything and starts clean immediately.

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
| Event | Spinner | must be selected: Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running | changes relevant interval/technique task wording and static instructions only (Section 7.9) |
| Level | Spinner | must be selected, **Beginner preselected** | weekly task pattern, warm-up/cool-down length, daily duration budget |
| Goal | Spinner | must be selected: Learn the Basics / Build and Hold Endurance / Run Faster and Race / Build Foundation / Improve Performance / Competition Performance | display label only — **does not affect task generation** |

Hint under Level: *"Not sure? Start with Beginner. You can move up later."*

**Goal vs. Level (resolved):** Goal is purely a **display label** the user picks for themselves — it shows on Home/Profile but never changes which tasks are generated. That fully removes the overlap risk: Level alone drives the plan, Goal is just how the user describes their ambition. Simplest possible fix, and keeps both fields meaningful without them fighting over the same logic.

### 6.3 Validation behavior (every form, including Edit Profile)
- Show the error beside the field (`setError`) or a clear Toast.
- Do **not** advance; keep entered values.
- Wrap number parsing in `try/catch`; invalid input must never crash the app.

---

## 7. Daily Training Plan — Approved Final Structure

RUNOVA's training content is intentionally **structured, realistic, deterministic, and simple to implement**. The app is not a random workout generator and does not create individualized coaching plans at runtime. The developer and AI coding agent should implement the approved routines below as static Java data that the existing lazy task generator reads.

### 7.1 Training design principles

The approved daily training flow is:

```text
WARM-UP → RUNNING DRILL / ACTIVATION (when scheduled) → MAIN TRAINING
→ OPTIONAL STRENGTH / CONDITIONING / MOBILITY → COOL-DOWN
```

Important implementation rule: the flow above describes the **content inside the day's task slots**. Do not create one database row for every individual exercise. The existing task-count limits must remain unchanged. A task such as `Strength & Core` contains a short group of exercises/instructions inside its task description while remaining one SQLite `tasks` row.

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

The existing task-count pattern is **unchanged** and is the structural limit for the approved routines:

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

The total duration is for **all tasks combined on that day**.

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

**Important:** For the approved weekly examples below, use the listed task durations exactly. Do not recalculate them at runtime using an even-split formula. The approved schedule is the source of truth for the demo/project implementation.

### 7.4 Approved Beginner weekly routine

Beginner emphasizes basic movement, consistency, easy aerobic work, simple strength, and recovery. It is intentionally manageable for a new or recreational runner.

| Day | Tasks / duration | What the user follows |
|---|---|---|
| **Sunday** | **2 tasks / 30 min** | Easy Walk 15 min + Stretch 15 min. Recovery-focused; no warm-up/cool-down. |
| **Monday** | **3 tasks / 60 min** | Warm-up 10 min → Easy Run 40 min → Cool-down 10 min. Comfortable running; focus on consistency. |
| **Tuesday** | **4 tasks / 60 min** | Warm-up 10 min → Running Drills 15 min → Strength & Core 25 min → Cool-down 10 min. Simple mechanics plus bodyweight conditioning. |
| **Wednesday** | **3 tasks / 60 min** | Warm-up 10 min → Cross-Training 40 min → Cool-down 10 min. Low-impact aerobic work such as brisk walking or cycling. |
| **Thursday** | **4 tasks / 60 min** | Warm-up 10 min → Interval Run 30 min → Mobility & Stretch 10 min → Cool-down 10 min. Controlled faster/easier running repetitions. |
| **Friday** | **3 tasks / 60 min** | Warm-up 10 min → Strength & Core 40 min → Cool-down 10 min. Basic lower-body and core work. |
| **Saturday** | **4 tasks / 60 min** | Warm-up 10 min → Easy Run 30 min → Running Drills 10 min → Cool-down 10 min. Easy aerobic running plus technique practice. |

**Beginner strength example:** bodyweight squats, reverse lunges, glute bridges, calf raises, and a plank. Keep the exercise group inside the single `Strength & Core` task; do not create five separate database tasks.

**Beginner running-drill example:** marching, high knees, butt kicks, A-skips, and controlled strides. Use simple instructions and avoid requiring special equipment.

### 7.5 Approved Intermediate weekly routine

Intermediate adds controlled tempo work, more structured intervals, longer running, and more varied strength/technique work while staying within the existing 90-minute daily limit.

| Day | Tasks / duration | What the user follows |
|---|---|---|
| **Sunday** | **2 tasks / 30 min** | Easy Walk 15 min + Stretch 15 min. Recovery day. |
| **Monday** | **3 tasks / 75 min** | Warm-up 15 min → Easy Run 45 min → Cool-down 15 min. Comfortable aerobic running. |
| **Tuesday** | **4 tasks / 90 min** | Warm-up 15 min → Interval Run 30 min → Strength & Core 30 min → Cool-down 15 min. Controlled speed-endurance work plus strength. |
| **Wednesday** | **5 tasks / 90 min** | Warm-up 15 min → Easy Run 25 min → Tempo Run 20 min → Strength & Core 15 min → Cool-down 15 min. Combines aerobic running, controlled tempo, and short conditioning. |
| **Thursday** | **3 tasks / 75 min** | Warm-up 15 min → Easy Run 45 min → Cool-down 15 min. Aerobic maintenance/recovery-oriented running. |
| **Friday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 10 min → Interval Run 35 min → Cool-down 15 min. Technique followed by structured faster running. |
| **Saturday** | **5 tasks / 90 min** | Warm-up 15 min → Easy/Long Run 40 min → Strides 10 min → Mobility 10 min → Cool-down 15 min. Longer aerobic work with short controlled strides and mobility. |

**Intermediate strength example:** squats, reverse lunges, single-leg glute bridges, calf raises, plank, and side plank. Keep these grouped inside the `Strength & Core` task.

### 7.6 Approved Pro weekly routine

Pro is the highest fixed level in RUNOVA. It adds more speed work, longer aerobic sessions, technique, strength, and mobility while remaining a simple static schedule rather than a professional individualized program.

| Day | Tasks / duration | What the user follows |
|---|---|---|
| **Sunday** | **2 tasks / 40 min** | Easy Walk 20 min + Mobility & Stretch 20 min. Recovery-focused day. |
| **Monday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 15 min → Speed Intervals 30 min → Cool-down 15 min. Technique and controlled speed work. |
| **Tuesday** | **5 tasks / 120 min** | Warm-up 15 min → Easy Run 45 min → Strength & Core 30 min → Mobility 15 min → Cool-down 15 min. Aerobic work plus full-body conditioning and mobility. |
| **Wednesday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 10 min → Tempo Run 35 min → Cool-down 15 min. Running mechanics plus sustained controlled effort. |
| **Thursday** | **5 tasks / 105 min** | Warm-up 15 min → Easy Run 45 min → Technique 15 min → Mobility 15 min → Cool-down 15 min. Aerobic maintenance, technique, and mobility. |
| **Friday** | **4 tasks / 75 min** | Warm-up 15 min → Running Drills 10 min → Interval Run 35 min → Cool-down 15 min. Structured speed-endurance session. |
| **Saturday** | **5 tasks / 115 min** | Warm-up 15 min → Easy/Long Run 60 min → Strides 10 min → Mobility 15 min → Cool-down 15 min. Long aerobic session with controlled strides and mobility. |

**Pro strength example:** split squats, a controlled single-leg squat variation, single-leg glute bridges, single-leg calf raises, side plank, plank, and a simple hamstring exercise. Keep the exercise group inside the single `Strength & Core` task.

### 7.7 Task content library

The database remains intentionally small. Task rows store the activity name/type/minutes/status; the more detailed instructions come from a static Java content library.

| Task / category | Example contents shown to user | Purpose |
|---|---|---|
| Warm-up | Easy walk/jog, leg swings, arm circles, marching, dynamic mobility | Prepare the body for the session |
| Running Drills | Marching, high knees, butt kicks, A-skips, controlled strides | Practice basic running mechanics |
| Easy Run | Relaxed continuous running at a comfortable effort | Aerobic base / consistency |
| Tempo Run | Controlled sustained running at a stronger but manageable effort | Aerobic strength |
| Interval Run | Easy running + repeated faster efforts + recovery periods | Speed endurance |
| Speed Intervals | Shorter faster repetitions with recovery | Speed-oriented work at Pro level |
| Strength & Core | Squats, lunges/split squats, glute bridges, calf raises, plank/side plank | Basic strength and stability |
| Cross-Training | Brisk walking, cycling, or another simple low-impact option | Aerobic conditioning with reduced running load |
| Mobility & Stretch | Light lower-body mobility and stretching | Recovery and movement quality |
| Technique | Running-form drills and controlled strides | Technique reinforcement |
| Easy/Long Run | Longer easy continuous running | Aerobic endurance |
| Easy Walk | Comfortable walking | Recovery |
| Stretch | Simple recovery stretching | Recovery |

The app may show a short instruction string under each task. Keep instructions concise enough for the Target screen.

### 7.8 Main-task exercise grouping rule

The existing task-count pattern must never be broken just because a task contains multiple exercises. For example:

```text
Strength & Core — 25 min
- Squats
- Reverse lunges
- Glute bridges
- Calf raises
- Plank
```

This is **one task**, not five tasks.

Likewise:

```text
Interval Run — 30 min
- 5 min easy running
- 4 × 2 min faster / 2 min easy
- Easy running to finish
```

This is **one timed task** and uses the existing single-task timer.

### 7.9 Event-specific adaptation

Event selection remains simple and does not create new database task types. It changes the relevant interval/technique task's display name and its static instruction content.

| Event | Relevant task wording | Example content emphasis |
|---|---|---|
| General Running | Interval Run | Controlled faster/easier running repetitions |
| Middle Distance | Interval Run | Speed endurance and controlled intervals |
| Long Distance | Interval Run | Longer controlled repetitions with endurance emphasis |
| Sprint | Sprint Repeats / Speed Intervals | Shorter fast efforts with full recovery emphasis |
| Hurdles | Hurdle Technique + Sprint Repeats | Basic hurdle movement/technique concepts plus controlled sprint work; no physical hurdles required by the app |
| Relay | Relay Exchange Practice | Basic exchange/coordination practice concepts; no separate database type |

**Scope rule:** event adaptation must remain content-level. Do not add new tables, new Activity classes, or a complex event-specific training engine.

### 7.10 Goal and Level remain separate

- **Level** controls the actual weekly routine, task count, duration, and difficulty tier.
- **Event** changes relevant task wording/content only.
- **Goal** remains a user-selected display label and does not generate a different plan.

This separation prevents the same training rule from being implemented in multiple places.

### 7.11 Training plan generation — final implementation rule

Replace the previous idea of cycling through a generic activity list with **static weekly templates**. The implementation should conceptually work like this:

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

`LevelPlan` contains the approved Monday–Sunday templates for Beginner, Intermediate, and Pro. `TrainingContentLibrary` contains task names, short instructions, drill/exercise groups, and event wording. The database continues to store only the final generated task rows.

**Do not implement:** random workouts, runtime AI-generated workouts, automatic progression algorithms, per-user difficulty calculations, or a week-to-week ramp.

### 7.12 Activity-only rule

Tasks are activity/training items only — running, drills, strength, cross-training, warm-up, cool-down, mobility, and recovery. Do not add nutrition, hydration, sleep, meditation, supplement, or medical-advice tasks.

## 8. Daily Flow, Timer, Status, Level-up, Analytics, Notifications

### 8.1 Generating today's tasks
```text
if no tasks exist in SQLite for today's date:
    template = LevelPlan.getTemplate(level, weekday)   // static approved routine, Section 7
    tasks = TrainingContentLibrary.applyEventContent(template, profile.event)
    save tasks to SQLite
show tasks for today's date
```
That's the entire generator. No date-range math, no per-week caching, nothing to invalidate.

### 8.2 Target screen and timers (simplified: one timer at a time, app-wide)
1. Shows today's task list — name, minutes, Start/Mark Done control.
2. **Only one task's timer can run at a time.** Starting a new task's timer while another is active is blocked in the UI (its Start button is disabled with a short message, e.g. "Finish your current task first"). This removes the need for per-task `timerEndTime` rows entirely.
3. Store a single `activeTaskId` + `activeTaskEndTime` in one place (the `profile` table, Section 10) — not per task. Recalculate remaining time from this single pair when the screen reopens.
4. Keep the screen on while the timer runs. Beep and vibrate at zero.
5. **Complete** unlocks at zero for timed tasks; **Finish early** is allowed. Short tasks (rest-day Easy Walk/Stretch) can use Mark-as-Done instead of a timer if simpler to build.
6. Completing a task saves its status; can't be counted twice.
7. A day shows Finished only once **every** task for that date is Finished.

### 8.3 Status (per task, rolled up per day)
- **Finished:** task has a saved completion.
- **Pending:** today's task, not yet completed.
- **Missed:** a past task with no completion.
- **Day status:** Finished only when all of that day's tasks are Finished.
- **Streak:** consecutive Finished days ending today or yesterday.

### 8.4 Level cycle and level-up (single shared handler — resolves the stale-notification risk)
- Constants: `LEVEL_DAYS = 28`, `PASS_RATE = 0.85` (use small test values for the demo).
- At day 28, show an **in-app dialog** on Home.
- **Passed (≥85%):** *Move up to [next level]* or *Repeat [level]*.
- **Below 85%:** only *Repeat [level]*.
- **Pro:** *Stay at Pro* or *Repeat*.
- If the dialog is dismissed without a choice, one `LEVEL_UP_OFFER` row is written to `notifications` (only one — check for an existing unresolved offer before inserting; never create a duplicate).
- **Both** the original dialog's buttons **and** the Notifications-list version of this message call the exact same function, `applyLevelChoice(choice)`. That function (a) updates the level and `levelStartDate`, and (b) marks any matching `notifications` row resolved. One function, one place the logic lives — nothing can go stale because there's only one code path that can ever apply a level change this way.

### 8.5 Analytics (final, detailed — no new tables needed)

Everything below reads from the existing `tasks` table only (`date`, `status`). No schema change, no new data collection — Month and Year are just different `GROUP BY`s on data already being saved.

**Screen layout, top to bottom:**

1. **Level progress card** — `X / 28` days Finished in the current cycle, as a `ProgressBar`. Color shifts (e.g. gray → green) once it crosses the 85% pass line.
2. **Streak card** — current streak (consecutive fully-Finished days), shown as a number with a simple icon.
3. **Status counts** — Finished / Missed / Pending totals, **scoped to the current 28-day cycle** (same window as the level progress card, so the numbers on screen never contradict each other).
4. **Time-range chart** — one chart, fed by whichever of the three views below is selected via a simple 3-tab row (`Week | Month | Year`) directly above it. Only this chart changes when the tabs are switched; the three cards above stay fixed to their own natural window (cycle / streak / cycle).

**Chart data — three views, one chart component:**

| View | Bars | Bar value | Query (conceptually) |
|---|---|---|---|
| **Week** (default tab) | 7, Mon–Sun of the current week | Tasks Finished that day (0–5) | `COUNT(*) WHERE status='Finished' GROUP BY date`, for the 7 dates of this week |
| **Month** | 4–5, one per week of the current calendar month | Total tasks Finished that week | Same query as Week, but summed into week-of-month buckets instead of shown per day |
| **Year** | 12, one per month of the current calendar year | % of that month's days that were **fully Finished** (all tasks for that date done) | For each month: `(days where every task that date is Finished) ÷ (days that had tasks generated that month) × 100` |

Week and Month reuse the exact same per-day Finished-count query, just grouped differently — no separate logic to write. Year uses percentage instead of a raw count so months of different lengths stay comparable on the same chart.

**Why this stays simple to build:**
- One `BarChart` view, one `ViewModel`/data-loading function per tab — swapping tabs just calls a different query and re-populates the same chart.
- No new Activity, no new table, no background job — everything is computed on demand when Analytics opens or a tab is tapped.
- "Current week/month/year" always means the real calendar week/month/year containing today — not anything tied to the 28-day level cycle. That keeps the date math trivial (`Calendar`/`LocalDate` week-of-month and month-of-year helpers, already in the Java standard library).

**One thing to show the user, not just build:** a small caption under the chart — *"Week/Month/Year shows all activity, even across level changes"* — since a calendar month or year can span a level-up or a level-edit wipe (Section 9), while the Level progress card above only reflects the current cycle. Two honest, clearly-labeled numbers rather than one number trying to mean both things.

**Build note:** this whole section fits inside the existing Analytics build step in Section 13 — it's the same chart component and the same table, just three query functions instead of one, so it doesn't need its own separate time slot.

### 8.6 Home screen (top to bottom)
Greeting with level, event and goal → **notifications bell** (badge if unresolved) → **current date header** (e.g. "February 17, 2026") → today's task list preview → level progress and Finished/Missed/Pending counts.

### 8.7 Notifications section
- Bell icon on Home header.
- Currently just one message type: the level-up offer, shown until resolved via `applyLevelChoice()` (8.4).

---

## 9. Profile

- Shows identity + questionnaire fields: first name, last name, gender, DOB, derived age, height, weight, event, level, goal. **No picture.**
- **Edit Profile** reuses the Sign Up/Questionnaire form in `EDIT` mode.
- **Editing height, weight, gender, DOB, event, or goal:** just updates the saved values. Because tasks are generated lazily (8.1), the change simply takes effect the next time a not-yet-generated day is opened. Nothing is wiped, no logged task can be deleted.
- **Editing Level (either direction — up or down, resolved):** show a confirmation pop-up first, with the exact wipe scope spelled out:
  > *"Changing your level will reset your current routine, all logged tasks, your streak, and any notifications. This can't be undone. Continue?"*
  On confirmation: delete all rows in `tasks` and `notifications`, set the new level, reset `levelStartDate` to today. This is the **only** way to change level from Profile — deliberately more destructive than the natural level-up dialog, because it's a manual override the user is choosing to make, not an earned progression.
- "About RUNOVA" reopens the start-up pop-up.

---

## 10. Data & Storage (SQLite)

**`profile`** (single row)
| Column | Content |
|---|---|
| `firstName`, `lastName`, `dob`, `gender` | identity |
| `height`, `weight`, `event`, `level`, `goal` | questionnaire |
| `levelStartDate`, `signupDate`, `seenIntro` | tracking flags |
| `activeTaskId`, `activeTaskEndTime` | the one currently-running timer, if any (nullable) |

**`tasks`** (one row per generated task)
| Column | Content |
|---|---|
| `id`, `date`, `name`, `type`, `minutes`, `status` | see 8.1–8.3 |

**`notifications`**
| Column | Content |
|---|---|
| `id`, `type` (e.g. `LEVEL_UP_OFFER`), `message`, `createdDate`, `resolved` | see 8.4 |

Three tables total. No cloud backup — uninstalling loses all history.

---

## 11. UI & Design System

- One consistent look across all Activities.
- `ConstraintLayout`, `ScrollView` where needed, `dp`/`sp` sizing.
- Dark navy/blue background, bright blue primary, green for completed, rounded cards.
- Colors in `res/values/colors.xml`, never repeated as raw hex in layouts.
- Priority order: **functionality → navigation → validation → offline storage → responsive UI → visual polish.**

---

## 12. Offline & Code Quality

- Must run end to end in airplane mode (local Reminders still fire offline). No internet, Firebase, APIs, cloud, online authentication.
- Clear class/variable names; comments where useful.
- Core methods: `generateDailyTasks()`, `validateInputs()`, `saveProfile()`, `loadProfile()`, `applyLevelChoice()`, `countFinishedDays()`, `checkLevelCycle()`.

---

## 13. Build Order (7 stages, sized for under a week)

1. `DBHelper` (3 tables), `Profile`, `Task`, `DateUtils` — half a day
2. `LevelPlan` approved weekly templates + `TrainingContentLibrary` + `generateDailyTasks()` — half a day
3. Welcome → Sign Up → Questionnaire, with validation — 1 day
4. Home + Target screens: task list, single-timer logic, Mark Done — 1–1.5 days
5. Notifications + level-up dialog, wired to `applyLevelChoice()` — half a day
6. Analytics with MPAndroidChart — half a day
7. Profile + Edit (wipe confirmation), airplane-mode test pass, polish — 1 day

**Test checklist:** app opens · pop-up shows once · Sign Up/Questionnaire validate · Home shows correct date + task list · only one timer can run at a time · a task can't be counted twice · day only shows Finished when all its tasks are · level-up dialog appears at day 28 and, if dismissed, shows in Notifications exactly once · accepting from either the dialog or Notifications updates the level and clears the notification · Profile edits (non-level) don't wipe anything · Profile level-edit shows the wipe warning and actually wipes on confirm · charts render · works fully offline.

---

## 14. Risks (remaining, all low-complexity)

- **Task timer surviving app close/phone lock** — inherent Android limitation, test early.
- **Trust-based tracking** — users can mark tasks done without doing them; the pop-up already says so.
- **No cloud backup** — uninstalling loses all history; acceptable for a class project.
- **Deadline** — finish the full flow end to end before polishing visuals (Section 13 order already reflects this).

*(Every other risk from the prior review — cool-down duration, week-to-week ramp, Goal/Level overlap, stale notifications, duplicate timers, wipe scope, chart granularity — is resolved above and no longer open.)*

---

## 15. Course Requirements, Rubric & Documentation

**Instructor requirements:** Android Studio, Java, completely offline, no online database, at least 5 interconnected Activities (RUNOVA uses 8), Intent navigation, appropriate styles/themes, responsive layouts, meaningful input validation, user interaction, original concept, final APK, source code, documentation, presentation. **Category:** Health and Fitness.

**Rubric:** Proposal 10 · UI/UX & system design 15 · Final application 60 (core functionality 20, UI 15, validation 10, code quality 5, originality 5, offline 5) · Documentation 15.

**Documentation includes:** logo and title ("RUNOVA — Offline Training & Progress Tracker"), group members, description, features, screenshots of every screen, user guide.

**Limitations:** no professional coaching, medical advice, distance/speed/heart-rate tracking, GPS, wearables, online sync, cloud backup, runtime AI-generated training plans, online accounts, nutrition tracking. Completion is self-reported.

**Demo script:** launch → pop-up → Sign Up → Questionnaire (Beginner, Sprint, any Goal) → Home shows date + tasks → Target: complete tasks (one timer at a time) → Analytics shows chart updating → Profile → edit Event (no wipe) → Profile → edit Level (wipe warning, confirm) → (test values) level-up dialog appears → dismiss → open from Notifications → accept.

---

## 16. Deliverables Checklist

- [ ] Start-up pop-up (once, reopenable)
- [ ] Sign Up + Questionnaire with validation (no weekly-minutes field)
- [ ] `LevelPlan` static tables + `generateDailyTasks()` (lazy, one day at a time)
- [ ] Home: date header, notifications bell, today's task list, status counts
- [ ] Target: per-task Start/Complete, single active timer app-wide
- [ ] Finished/Missed/Pending per task, day roll-up, streak
- [ ] Analytics with MPAndroidChart: Week/Month/Year tabs on one chart, plus level-cycle progress, streak, and cycle-scoped status counts
- [ ] Notifications (bell icon, single deduplicated level-up offer)
- [ ] Level-up dialog + Notifications both calling `applyLevelChoice()`
- [ ] Profile edit: quiet update for most fields, wipe-with-confirmation for Level only
- [ ] SQLite: `profile`, `tasks`, `notifications` (3 tables)
- [ ] Airplane-mode tested, APK + docs + screenshots ready

---

## Appendix — AI / Copilot Master Prompt

```text
You are building RUNOVA: an offline Android app (Java, Android Studio) for runners.
This is a school project with a short development timeline (under a week). The
leader has approved the training-plan adaptation in Section 7. Treat this file as
the single source of truth. Keep implementation simple, deterministic, offline,
and compatible with the existing architecture.

HARD RULES
- Fully offline. No internet/Firebase/cloud/AI at runtime/GPS/wearables/heart-rate/
distance/speed tracking. Local Reminders are allowed through
NotificationManager/AlarmManager and are separate from the in-app Notifications
bell.
- 8 Activities, Intent navigation, all declared in AndroidManifest.xml.
- SQLite, exactly 3 tables: profile, tasks, notifications.
- Only ONE task timer can run at a time, app-wide.
- Tasks are generated lazily, one day at a time, only when first opened.
- Never pre-generate a week/month and never regenerate already-generated dates.
- No random workout generator and no runtime AI-generated training plan.
- No week-to-week progression algorithm. The same approved 7-day routine repeats
for the entire 28-day level cycle. Difficulty changes only when the user changes
level.
- Do not create one DB task row per exercise. Group exercises/instructions inside
one task such as Strength & Core or Interval Run.
- No new tables, no new Activity, no new calendar system, and no new external
dependencies beyond MPAndroidChart.
- Complete only the requested files, match XML IDs to findViewById, and avoid
unrelated refactors.

SCREENS
MainActivity -> SignupActivity -> QuestionnaireActivity -> HomeActivity.
Home connects to TargetActivity, AnalyticsActivity, ProfileActivity, and
NotificationsActivity through the existing navigation rules. Bottom navigation:
Home, Analytics, Target, Profile. Notifications is opened from the Home bell.

USER DATA
Sign Up: first name, last name, DOB, gender. Age is derived from DOB.
Questionnaire: height, weight, event, level, goal.
Level = Beginner/Intermediate/Pro and drives the actual training routine.
Event = Sprint/Middle Distance/Long Distance/Hurdles/Relay/General Running and
changes relevant task wording/instructions only.
Goal = display label only; it does not generate a different plan.

APPROVED TRAINING MODEL
Every training day follows the content flow:
WARM-UP -> RUNNING DRILL/ACTIVATION when scheduled -> MAIN TRAINING -> OPTIONAL
STRENGTH/CONDITIONING/MOBILITY -> COOL-DOWN.

Task counts remain:
Beginner     [3,4,3,4,3,4,Rest(2)]
Intermediate [3,4,5,3,4,5,Rest(2)]
Pro          [4,5,4,5,4,5,Rest(2)]
Order is Mon-Sun. 3 = warm-up + 1 main + cool-down; 4 = warm-up + 2 mains +
cool-down; 5 = warm-up + 3 mains + cool-down. Sunday is exactly Easy Walk +
Stretch and has no warm-up/cool-down.

DAILY DURATIONS — USE THE APPROVED WEEKLY TABLES IN SECTION 7 AS THE SOURCE OF
TRUTH. Do not calculate a new duration formula at runtime.
Beginner max 60 min/day.
Intermediate max 90 min/day.
Pro max 120 min/day.
Warm-up/cool-down: Beginner 10/10; Intermediate 15/15; Pro 15/15.

APPROVED WEEKLY CONTENT
Beginner:
Sun 30: Easy Walk 15 + Stretch 15.
Mon 60: Warm-up 10 + Easy Run 40 + Cool-down 10.
Tue 60: Warm-up 10 + Running Drills 15 + Strength & Core 25 + Cool-down 10.
Wed 60: Warm-up 10 + Cross-Training 40 + Cool-down 10.
Thu 60: Warm-up 10 + Interval Run 30 + Mobility & Stretch 10 + Cool-down 10.
Fri 60: Warm-up 10 + Strength & Core 40 + Cool-down 10.
Sat 60: Warm-up 10 + Easy Run 30 + Running Drills 10 + Cool-down 10.

Intermediate:
Sun 30: Easy Walk 15 + Stretch 15.
Mon 75: Warm-up 15 + Easy Run 45 + Cool-down 15.
Tue 90: Warm-up 15 + Interval Run 30 + Strength & Core 30 + Cool-down 15.
Wed 90: Warm-up 15 + Easy Run 25 + Tempo Run 20 + Strength & Core 15 +
Cool-down 15.
Thu 75: Warm-up 15 + Easy Run 45 + Cool-down 15.
Fri 75: Warm-up 15 + Running Drills 10 + Interval Run 35 + Cool-down 15.
Sat 90: Warm-up 15 + Easy/Long Run 40 + Strides 10 + Mobility 10 + Cool-down 15.

Pro:
Sun 40: Easy Walk 20 + Mobility & Stretch 20.
Mon 75: Warm-up 15 + Running Drills 15 + Speed Intervals 30 + Cool-down 15.
Tue 120: Warm-up 15 + Easy Run 45 + Strength & Core 30 + Mobility 15 +
Cool-down 15.
Wed 75: Warm-up 15 + Running Drills 10 + Tempo Run 35 + Cool-down 15.
Thu 105: Warm-up 15 + Easy Run 45 + Technique 15 + Mobility 15 + Cool-down 15.
Fri 75: Warm-up 15 + Running Drills 10 + Interval Run 35 + Cool-down 15.
Sat 115: Warm-up 15 + Easy/Long Run 60 + Strides 10 + Mobility 15 +
Cool-down 15.

CONTENT GROUPING
A task can contain multiple exercises but is still ONE task row. Examples:
Strength & Core: squats, reverse lunges, glute bridges, calf raises, plank.
Interval Run: 5 min easy + repeated faster/easier intervals + easy finish.
Running Drills: marching, high knees, butt kicks, A-skips, controlled strides.
Keep instructions short enough for the Target screen.

EVENT ADAPTATION
General/Middle/Long Distance: Interval Run.
Sprint: Sprint Repeats or Speed Intervals.
Hurdles: Hurdle Technique + Sprint Repeats.
Relay: Relay Exchange Practice.
Event adaptation is content-level only. Do not create new DB tables or a complex
training engine.

STATIC IMPLEMENTATION
LevelPlan stores the exact approved Mon-Sun task templates for each level.
TrainingContentLibrary stores task descriptions, drill/exercise groups, and event
wording. generateDailyTasks() gets the level + weekday template, applies event
content, then saves the final task rows to SQLite.

TIMER / STATUS
Store one activeTaskId + activeTaskEndTime in profile. Keep screen on while
running; beep/vibrate at zero. Complete at zero or Finish early. Save completion
once. Finished/Missed/Pending remain exactly as defined in Section 8. A day is
Finished only when every task for that date is Finished.

LEVEL CYCLE
LEVEL_DAYS=28, PASS_RATE=0.85. At day 28 show the existing level-up dialog.
>=85%: move up/repeat (Pro: stay/repeat). <85%: repeat only. If dismissed,
insert one unresolved LEVEL_UP_OFFER notification if one does not already exist.
Both dialog and Notifications call applyLevelChoice(choice).

PROFILE EDIT
Editing non-level fields updates values without wiping existing task history.
The change applies to future not-yet-generated dates. Editing Level requires the
existing confirmation and then wipes tasks + notifications, sets the new level,
and resets levelStartDate to today.

ANALYTICS
Use only the existing tasks table and the existing MPAndroidChart component.
Keep the approved Week/Month/Year chart behavior, level-cycle progress card,
streak card, and cycle-scoped status counts from Section 8.5. Do not add tables.

STORAGE
profile(firstName,lastName,dob,gender,height,weight,event,level,goal,
levelStartDate,signupDate,seenIntro,activeTaskId,activeTaskEndTime)
tasks(id,date,name,type,minutes,status)
notifications(id,type,message,createdDate,resolved)

BUILD ORDER
1. Existing DBHelper/Profile/Task/DateUtils.
2. Implement approved LevelPlan templates + TrainingContentLibrary +
generateDailyTasks().
3. Welcome/Sign Up/Questionnaire.
4. Home/Target and single-timer logic.
5. Notifications and shared level-up handler.
6. Analytics.
7. Profile/Edit, airplane-mode testing, then visual polish.

PRIORITY
Correct training templates and task counts -> navigation -> validation ->
offline storage -> timer/status correctness -> analytics -> visual polish.
Do not expand the scope without explicit approval.
```


---

# 17. Controlled AI Development Workflow

This section is the operational workflow the coding agent should follow from project start
to final delivery.

## Phase 0 — Project inspection

Before implementing anything:

1. Open the Android project.
2. Identify the Gradle configuration.
3. Identify all Activities.
4. Identify database code.
5. Identify models.
6. Identify layouts/resources.
7. Identify existing navigation.
8. Identify existing timer logic.
9. Identify existing task generation.
10. Identify existing analytics/chart code.
11. Build the project without making changes.

Output:

```text
BASELINE REPORT
Build: PASS/FAIL
Existing Activities: ...
Existing DB tables: ...
Existing dependencies: ...
Existing task generator: ...
Existing timer: ...
Existing Analytics: ...
Major conflicts found: ...
```

Do not redesign the project during inspection.

---

## Phase 1 — Lock the foundation

Implement or verify:

- three SQLite tables;
- profile persistence;
- task persistence;
- notification persistence;
- date helpers;
- profile loading/saving.

Verify:

```text
Fresh install
→ database created
→ profile can be saved
→ profile can be loaded
→ task can be saved
→ task can be loaded
→ notification can be saved
```

Checkpoint 1.

---

## Phase 2 — Implement the approved training engine

Implement:

- `LevelPlan`;
- `TrainingContentLibrary`;
- exact weekday templates;
- event-specific content adaptation;
- `generateDailyTasks()`.

Generation rules:

```text
Profile level
    +
Current date weekday
    +
Profile event
    ↓
Approved static template
    ↓
Event-specific wording/content
    ↓
Task rows
    ↓
SQLite
```

The generated task rows must contain the final user-facing task name, type, minutes,
and status.

Verify every level:

```text
Beginner: 7-day count and durations
Intermediate: 7-day count and durations
Pro: 7-day count and durations
```

Also verify Sunday separately.

Checkpoint 2.

---

## Phase 3 — Onboarding

Implement/verify:

```text
Main
 ↓
Signup
 ↓
Questionnaire
 ↓
Home
```

Validation must prevent invalid values and prevent crashes.

Verify:

- invalid name;
- invalid DOB;
- future DOB;
- invalid numeric values;
- missing selections;
- valid submission;
- persistence after app restart.

Checkpoint 3.

---

## Phase 4 — Home, Target, timer, and task state

Implement/verify:

- current date;
- today's task list;
- task details;
- one active timer;
- Finish/Mark Done;
- Finished/Missed/Pending;
- day roll-up;
- streak.

Critical timer rule:

```text
Task A timer running
       ↓
Task B cannot start simultaneously
       ↓
Starting another timer must stop/block the first
```

Do not create parallel timer systems.

Verify app restart and phone-lock behavior as far as Android permits.

Checkpoint 4.

---

## Phase 5 — Level cycle and Notifications

Implement/verify:

- 28-day cycle;
- pass-rate calculation;
- level-up dialog;
- notification deduplication;
- shared `applyLevelChoice()` path.

Required invariant:

```text
Dialog choice ─────┐
                   ├──> applyLevelChoice()
Notification choice┘
```

Never duplicate level-change logic in two places.

Verify:

- pass;
- fail;
- Pro;
- dismiss;
- reopen from Notifications;
- accept from Notifications;
- notification resolves;
- duplicate offer is not created.

Checkpoint 5.

---

## Phase 6 — Analytics

Implement/verify:

- level progress;
- streak;
- cycle status counts;
- Week chart;
- Month chart;
- Year chart.

Use existing `tasks` data.

Do not create analytics tables.

Checkpoint 6.

---

## Phase 7 — Profile and controlled destructive action

Implement/verify:

- view profile;
- edit non-level fields;
- event change;
- goal change;
- level change confirmation;
- task/notification wipe after confirmed level change;
- level start date reset.

Verify that non-level edits do NOT wipe task history.

Checkpoint 7.

---

## Phase 8 — Final integration

Run the complete flow:

```text
Fresh install
→ Intro
→ Sign Up
→ Questionnaire
→ Home
→ Target
→ Timer
→ Complete tasks
→ Analytics
→ Profile edit
→ Restart app
→ Notifications
→ Level-cycle test
→ Airplane mode
→ Final build
```

Then test the main demo script from Section 15.

Checkpoint 8.

---

# 18. Agent Testing Strategy

## 18.1 Test the smallest thing first

After changing one component:

```text
Compile
→ targeted test
→ inspect result
→ then continue
```

Do not make ten unrelated changes and test everything at the end.

## 18.2 Critical invariants

The following must always remain true:

1. Exactly three SQLite tables.
2. One profile row.
3. One active timer maximum.
4. Generated task rows are not regenerated.
5. A completed task cannot be counted twice.
6. A day is Finished only when all tasks are Finished.
7. Sunday has exactly two recovery tasks.
8. Level determines the routine.
9. Goal does not determine the routine.
10. Event changes approved content wording/instructions only.
11. Non-level profile edits do not wipe history.
12. Manual level change wipes tasks and notifications only after confirmation.
13. Level-up notification is deduplicated.
14. Dialog and notification use `applyLevelChoice()`.
15. App works without internet.
16. No unapproved dependency is introduced.

If a code change violates an invariant, stop.

---

# 19. Conflict Reporting Protocol

When a conflict is found, use this exact structure:

```text
⚠ RUNOVA CONFLICT REPORT

Area:
[database / training / UI / navigation / timer / analytics / other]

Found:
[short factual description]

Specification:
[the relevant RUNOVA rule]

Existing code:
[what the current code does]

Impact:
[what could break]

Proposed options:
1. ...
2. ...

Recommended minimal approach:
...

Developer decision required:
YES

Implementation paused:
YES
```

Never hide conflicts inside a completion message.

---

# 20. Change Request Protocol

When the developer requests a new change:

### Step A
Classify it:

```text
BUG FIX
REQUIRED FEATURE
UI CHANGE
CONTENT CHANGE
ARCHITECTURE CHANGE
SCOPE CHANGE
```

### Step B
Check this document.

### Step C
Estimate impact.

### Step D
If it is safe and in scope, implement it.

### Step E
If it changes an approved decision, stop and report.

### Step F
Test.

### Step G
Report.

This prevents accidental redesign during fast development.

---

# 21. Simple-First Engineering Rules

Prefer:

- plain Java;
- small classes;
- straightforward SQLite queries;
- existing Android components;
- existing Activities;
- existing layouts;
- deterministic data;
- reusable helper methods;
- clear names;
- explicit control flow.

Avoid unless explicitly necessary:

- dependency injection frameworks;
- complex reactive architecture;
- unnecessary repositories;
- large abstraction layers;
- custom rendering;
- background services;
- synchronization engines;
- advanced animation systems;
- generic workout engines;
- over-engineered state management.

For a sub-week student project, complexity is a risk.

---

# 22. AI Context Preservation Rules

Because the project may be developed over multiple AI sessions:

1. Always treat this file as the requirements anchor.
2. Do not assume previous chat context is still available.
3. Before a new development task, identify the relevant sections of this file.
4. Read the actual project files before editing.
5. Do not rely on memory for code structure.
6. Keep important implementation decisions in project documentation/comments when useful.
7. If a previous agent made an undocumented change that conflicts with this file, report it.
8. Never let a temporary workaround silently become a permanent requirement.

Recommended task prompt to the agent:

```text
Read the RUNOVA development specification first.

Task:
[one clearly defined task]

Relevant requirement sections:
[sections]

Constraints:
- Do not change unrelated behavior.
- Do not add dependencies.
- Do not change database schema.
- Do not expand scope.
- Report conflicts before implementing them.

Required result:
[exact result]

Verification:
[exact tests]
```

---

# 23. Final Release Gate

The project is ready for final APK generation only when:

```text
[ ] Clean build succeeds
[ ] App launches
[ ] Intro behavior works
[ ] Signup validation works
[ ] Questionnaire validation works
[ ] Profile persists
[ ] Beginner plan verified
[ ] Intermediate plan verified
[ ] Pro plan verified
[ ] Sunday recovery verified
[ ] Event adaptation verified
[ ] Goal remains display-only
[ ] Lazy generation verified
[ ] No duplicate generation
[ ] Timer works
[ ] Only one timer can run
[ ] Finished/Missed/Pending verified
[ ] Day roll-up verified
[ ] Streak verified
[ ] Level cycle verified
[ ] Level-up dialog verified
[ ] Notification deduplication verified
[ ] Notification choice uses shared handler
[ ] Profile non-level edit verified
[ ] Profile level-change wipe verified
[ ] Analytics Week verified
[ ] Analytics Month verified
[ ] Analytics Year verified
[ ] Airplane-mode operation verified
[ ] No prohibited dependency
[ ] No debug-only bypass in final flow
[ ] No unapproved scope added
[ ] Final developer report delivered
[ ] APK generated
[ ] Documentation/screenshots prepared
```

If an item fails, report it. Do not mark it complete because the deadline is near.

---



---

# 25. HARD RULE — NO OVER-ENGINEERING / NO OVER-DEVELOPMENT

This rule is mandatory.

> **RUNOVA must be developed as simply as possible while still being correct, reliable,
> maintainable, demonstrable, and fully aligned with this specification.**

The coding agent must understand that **more code does not mean better development**.
More features do not mean a better project. More abstraction does not mean a better
architecture.

The goal is a **complete and reliable RUNOVA**, not a large or sophisticated software
system.

## 25.1 What "simple" means for RUNOVA

Simple means:

- Use the existing architecture whenever it is sufficient.
- Use straightforward Java and Android components.
- Keep classes focused and understandable.
- Keep methods reasonably small.
- Use direct SQLite operations where appropriate.
- Reuse existing helpers.
- Reuse existing screens.
- Reuse existing navigation.
- Reuse existing UI components.
- Keep training data static and deterministic.
- Keep business rules explicit.
- Avoid unnecessary layers between the UI and the actual logic.
- Avoid creating abstractions that solve problems RUNOVA does not have.
- Prefer a working simple solution over a theoretically sophisticated solution.

**Simple does NOT mean careless.**

The implementation must still be clean, validated, tested, and reliable.

---

## 25.2 What the agent must NOT over-engineer

Do not introduce complexity such as:

- unnecessary design-pattern layers;
- unnecessary interfaces;
- unnecessary factories;
- unnecessary repositories;
- unnecessary service layers;
- unnecessary dependency-injection frameworks;
- unnecessary ViewModels or architectural layers when the existing project does not need them;
- unnecessary generic frameworks;
- custom frameworks for task generation;
- complex rule engines;
- configurable workflow engines;
- plugin systems;
- excessive utility classes;
- excessive abstraction of simple operations;
- complicated state-management systems;
- background processing when a direct operation is sufficient;
- complicated caching;
- unnecessary asynchronous infrastructure;
- unnecessary animations;
- elaborate transition systems;
- complex custom UI components;
- elaborate error-reporting systems;
- complicated database migration systems for a fixed three-table project;
- speculative scalability infrastructure;
- features designed for hypothetical future versions.

If a normal Android/Java solution can solve the problem directly, prefer the normal solution.

---

## 25.3 Do not over-develop the product

The agent must not expand the RUNOVA product beyond its approved purpose.

Do NOT add features simply because they are common in fitness apps.

Examples of prohibited "extra development":

```text
GPS
Distance tracking
Pace tracking
Heart-rate tracking
Wearable synchronization
Social accounts
Friends
Leaderboards
Achievements
Badges
Gamification systems
Nutrition
Hydration
Sleep tracking
Weather
Maps
Cloud backup
Online accounts
AI coaching
Personalized AI workouts
Advanced training-load calculations
Complex race prediction
Advanced performance metrics
Automatic exercise recognition
```

These are not needed to make RUNOVA successful.

---

## 25.4 Do not over-polish before functionality is complete

Development priority is:

```text
FUNCTIONALITY
    ↓
CORRECTNESS
    ↓
DATA / STATE RELIABILITY
    ↓
VALIDATION
    ↓
NAVIGATION
    ↓
USABILITY
    ↓
VISUAL POLISH
```

Do not spend significant development time on:

- animations;
- decorative effects;
- unnecessary icons;
- advanced transitions;
- visual micro-interactions;
- cosmetic refactoring;

while core functionality is still broken or incomplete.

A simple working screen is better than a beautiful broken screen.

---

## 25.5 The "simplest sufficient solution" test

Before introducing a new class, dependency, abstraction, or architectural change,
the agent must ask:

```text
1. Is this actually required by the RUNOVA specification?
2. Can the existing code already solve it?
3. Can a small local change solve it?
4. Will this introduce new maintenance cost?
5. Will this consume significant development time or AI tokens?
6. Does it improve a required RUNOVA behavior?
```

If the answer to #1 is no and the change is not required to fix a real problem:

> **Do not implement it.**

---

## 25.6 Three-solution rule

When solving an implementation problem, mentally compare:

```text
SOLUTION A — simplest direct solution
SOLUTION B — moderate abstraction
SOLUTION C — complex architecture
```

Choose Solution A whenever it satisfies the requirements correctly.

Only move to Solution B or C if the simpler solution demonstrably cannot satisfy the
requirements.

If a complex solution appears necessary, notify the developer before implementing it.

---

## 25.7 No speculative future-proofing

Do not build systems for hypothetical requirements.

Examples:

**Wrong:**
> "We might eventually support 20 training levels, so let's build a configurable
> multi-level rules engine."

**Correct:**
> Implement the three approved levels cleanly using the approved `LevelPlan`.

**Wrong:**
> "We might eventually support online synchronization, so let's design a cloud-ready
> repository architecture."

**Correct:**
> Implement the required offline SQLite storage.

**Wrong:**
> "We might eventually support dozens of workout types, so let's build a generic
> workout scripting language."

**Correct:**
> Implement the approved static `TrainingContentLibrary`.

Future possibilities do not justify current complexity.

---

## 25.8 No unnecessary rewrites

If existing code works and is compatible with the specification:

> **Leave it alone.**

Do not rewrite a working Activity merely because another coding style looks cleaner.

Do not replace working SQLite code merely because Room or another library may be more
modern.

Do not rewrite layouts merely because they could be organized differently.

Do not refactor unrelated code while implementing a feature.

The safest code during a short development cycle is often the code that does not need
to be changed.

---

## 25.9 No unnecessary testing infrastructure

Testing is required, but the agent should not build a large testing framework solely
for this project.

Prioritize:

- build verification;
- manual end-to-end testing;
- targeted logic checks;
- database checks;
- task-generation checks;
- timer checks;
- validation checks;
- offline checks;
- final demo-flow verification.

If automated tests are already present, use them where appropriate.

Do not spend the limited development budget creating elaborate testing infrastructure
unless the developer explicitly requests it.

---

## 25.10 No unnecessary documentation inside the code

Comments should explain:

- non-obvious business rules;
- important constraints;
- destructive behavior;
- timer/state behavior;
- unusual Android limitations.

Do not comment every obvious line.

Bad:

```java
// Set the text of the TextView.
nameText.setText(name);
```

Good:

```java
// Only one task timer may be active across the app.
```

The goal is understandable code, not a wall of comments.

---

## 25.11 Complexity is a warning signal

If the agent finds itself needing:

- many new classes;
- many new dependencies;
- large architectural changes;
- complex algorithms;
- extensive refactoring;
- large amounts of generated code;
- many new database structures;

for a relatively small RUNOVA feature, it must stop and reconsider.

Use:

```text
⚠ COMPLEXITY WARNING

Requested feature:
...

Why the implementation is becoming large:
...

Simpler alternative:
...

Would this alter the approved architecture?
YES / NO

Developer approval needed:
YES / NO
```

Do not continue expanding the solution automatically.

---

## 25.12 Development-time protection

Because RUNOVA has a short development schedule and limited AI/coding-agent budget:

> **Every unnecessary implementation decision consumes time, context, tokens, and money.**

Therefore:

```text
Avoid unnecessary code
        ↓
Avoid unnecessary context
        ↓
Avoid unnecessary debugging
        ↓
Avoid unnecessary token usage
        ↓
Reduce development cost
        ↓
Increase probability of finishing on time
```

The agent should always prefer the shortest reliable path to the required behavior.

---

## 25.13 Final over-engineering rule

Before considering a task finished, ask:

> **"Did I build what RUNOVA needs, or did I build what I personally think a larger
> application might need?"**

If the answer is the second:

**Remove the unnecessary complexity.**

The final RUNOVA implementation should be:

```text
SIMPLE
   +
CORRECT
   +
DETERMINISTIC
   +
OFFLINE
   +
MAINTAINABLE
   +
APPROVED
   =
GOOD RUNOVA DEVELOPMENT
```

Not:

```text
MORE FEATURES
   +
MORE CLASSES
   +
MORE FRAMEWORKS
   +
MORE CODE
   =
BETTER RUNOVA
```

The second equation is explicitly rejected.


# 24. Final Instruction to the Coding Agent

> **Build what is specified. Do not redesign what is specified.**
>
> RUNOVA is intentionally simple. Its strength is a clear, deterministic offline flow:
> profile → level/event → approved weekly template → daily tasks → timer/status →
> analytics → 28-day cycle.
>
> Your job is to make that flow reliable.
>
> Do not invent missing requirements.
> Do not silently resolve conflicts.
> Do not add features because they seem useful.
> Do not replace working architecture without approval.
> Do not waste tokens on unnecessary rewrites.
> Do not hide problems from the developer.
>
> When something is unclear, stop and ask.
> When something conflicts, report it.
> When something is safe and clearly in scope, implement it simply.
> After every meaningful stage, verify and report.
>
> **The developer owns the decisions. The agent owns the implementation.**
>
> The final objective is a stable, demonstrable, offline RUNOVA application delivered
> within the available time and AI budget while preserving the approved concept exactly.
