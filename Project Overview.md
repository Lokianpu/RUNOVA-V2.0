# RUNOVA — Project Overview (V6)

*The single reference for building RUNOVA from scratch. It is written for the dev team and for any AI helping with the build. Everything in this document is a decided feature or rule.*

---

## 1. Project Overview

**What it is:** RUNOVA is an offline Android app for runners and regular people who run. It gives the user one **target** each day, matched to their level and event, provides an in-app **timer** for the session, and tracks how consistently the user follows the plan.

**Who it's for:** Student athletes, beginner and recreational runners, and anyone who wants a simple structured running plan without internet.

**Core idea:**
```text
Start-up pop-up → Sign Up → Questionnaire (incl. Level + Event) → Home
        ↓
Each day: Today's Target (from level + event + weekday + week in level)
        ↓
Start timer → Complete → Finished / Missed / Pending
        ↓
Weekly progress + Level progress → level-up prompt after 28 days
```

**Success looks like:** A new user signs up, picks a level, gets a clear daily target with a timer, completes days, sees their progress, is offered a level-up, and can edit their profile so the plan adjusts, all without any internet connection.

**Honest limits:** RUNOVA has no GPS, distance, speed or heart-rate tracking. Nothing is verified, so a user can mark a day done without doing it. The start-up pop-up says this plainly.

**Positioning:** a training organizer and educational prototype, **not** medical advice or professional coaching. Avoid claims such as "prevents injury" or "guarantees results". Use neutral labels: Today's Target, Suggested Activity, Recovery Day, Track Your Progress.

---

## 2. Scope

### Included
- Start-up pop-up, Sign Up, Questionnaire, Home, Target (with timer), Analytics, Profile (editable)
- Three levels (Beginner, Intermediate, Pro), each with a 7-day target plan that progresses over 4 weeks
- One target per day, session timer, Finished / Missed / Pending status, weekly progress, level progress, streak
- Level-up prompt (in-app dialog)
- Input validation, Intent navigation, consistent theme, airplane-mode testing

**Not part of the app:** Firebase or any online database, cloud sync, online accounts, AI/ML, GPS, wearables, heart-rate or distance tracking, social features, leaderboards, external APIs, push/server notifications (device-local scheduled notifications ARE part of the app), profile pictures, medical advice. **Chart libraries are allowed offline — MPAndroidChart is approved** for Analytics. Nutrition/hydration/sleep/meditation habit tracking, in-app calendar, and local notifications/background scheduling ARE part of the app.

---

## 3. Tech Stack & Architecture

| Piece | Choice |
|---|---|
| Platform / IDE | Android, Java, Android Studio |
| Storage | SQLite — tables: `profile`, `routines`, `tasks`, history/meta (Section 10) |
| Timer | `CountDownTimer` plus a saved end time |
| Pattern | MVC-style: Model, View, Controller |
| Dependencies | None beyond the default Android Studio project, **except MPAndroidChart (offline, approved for Analytics)** |

**Model:** `Profile` (all user fields), `Routine`/`Task` rows (generated plan), `DailyTarget` (name, type, effort label, total minutes, tasks), `TaskPlan` (ordinal, title, type, minutes, effort, step prescription), `DbHelper` (SQLite storage wrapper), `DateUtils`.
**View:** the Activities and layouts in Section 4.
**Controller:** `getDailyTarget()`, status calculation, level-cycle check, validation, timer logic.

**Data flow:**
1. First launch: pop-up, then Sign Up, then Questionnaire. Everything is saved to SQLite.
2. Whenever a screen needs the plan, it calls `getDailyTarget(level, event, weekday, weekInLevel, weeklyMinutesBand)`. **Nothing is stored** except the profile and the completed dates.
3. Completing a day saves today's date. Status, streak, weekly progress and level progress are all calculated from those dates.
4. Editing the profile changes the saved values, so the next calculation already reflects the change.

---

## 4. Screens & Navigation

At least 5 interconnected Activities, all navigated with `Intent`. RUNOVA uses seven.

| Activity | Layout | Purpose |
|---|---|---|
| `MainActivity` | `activity_main.xml` | Welcome screen (launcher): logo, START, start-up pop-up on first launch |
| `SignupActivity` | `activity_signup.xml` | Identity |
| `QuestionnaireActivity` | `activity_questionnaire.xml` | Body, training, event, level |
| `HomeActivity` | `activity_home.xml` | Greeting, week strip, Today's Target, level progress, status counts |
| `TargetActivity` | `activity_target.xml` | Run today's session with the timer, complete it |
| `AnalyticsActivity` | `activity_analytics.xml` | Weekly progress, level progress, streak, counts |
| `ProfileActivity` | `activity_profile.xml` | Name and identity, editable, no picture |

Home, Analytics, Target and Profile share a fixed **bottom navigation bar** (Home, Analytics, Target, Profile).

**Flow:**
```text
Welcome ──START──▶ Sign Up ──▶ Questionnaire ──▶ Home
   │ (saved profile exists → straight to Home)      │  ⇄ Target
                                                    ├── Analytics
                                                    └── Profile ──Edit──▶ Home (adjusted)
```

**Rules:**
- Declare every Activity in `AndroidManifest.xml`; `MainActivity` is the launcher.
- Call `finish()` when moving forward from Welcome so Back does not return to it.
- If a saved profile exists, START goes straight to Home.

---

## 5. Start-up Pop-up

Shown **once on first launch**, before Sign Up (use a saved `seenIntro` flag). It can be reopened from Profile ("About RUNOVA").

> **Before you start 👟**
> Hello, runner! RUNOVA is an offline training guide made for a school project. It gives you a daily routine and targets, but it can't track your distance, speed or heart rate. Nothing here is automatic.
> That means you *could* tap "Done" without doing the session, and nobody will stop you. But real results only come from actually doing each target, so follow the plan and give it your best. Listen to your body: if something hurts or you feel unwell, stop and rest. If you have any health concerns, talk to a doctor before you begin.
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

Age is **derived** from the date of birth with `DateUtils.ageFrom()` and shown on Profile. It updates by itself on the birthday. Age is never asked.

### 6.2 Questionnaire
| Field | Input | Validation | Affects |
|---|---|---|---|
| Height | decimal | positive number (suggested bounds, e.g. 100–250 cm) | display only |
| Weight | decimal | positive number (suggested bounds, e.g. 25–250 kg) | display only |
| Weekly running minutes | number | whole number, 0 or more, sensible upper cap | low / mid / high duration band |
| Event | Spinner | must be selected: Sprint, Middle Distance, Long Distance, Hurdles, Relay, General Running | wording of targets |
| Level | Spinner | must be selected, **Beginner preselected** | the whole weekly plan |

Hint under the Level spinner: *"Not sure? Start with Beginner. You can move up later."*

**Duration band (from weekly minutes):** under 60 = low, 60–150 = mid, over 150 = high. Use the mid value if the field is dropped.

### 6.3 Validation behavior (every form, including Edit Profile)
- Show the error beside the field (`setError`) or a clear Toast.
- Do **not** advance; keep the values already entered.
- Wrap number parsing in `try/catch`; invalid input must never crash the app.

### 6.4 Training Goal label (automatic, from level)
```text
Beginner     → Build Foundation
Intermediate → Improve Performance
Pro          → Competition Performance
```

---

## 7. Daily Target Plans

### 7.1 Research basis and design rules
- Beginner plans use run/walk intervals, three run days a week with rest between, about 30-minute sessions, and allow repeating a week if not ready (NHS Couch to 5K).
- Intermediate plans rotate easy, tempo, interval, cross-training and long-run days, and assume the user can already run 2–3 miles continuously (Active.com plans, Age UK plan).
- Advanced plans use multiple quality sessions with structured recovery (Run247 guide). Most volume is easy (the 80/20 approach of Seiler and Fitzgerald), and hard sessions are spaced apart by easy days.
- Strength: about two short bodyweight sessions a week, using squats, lunges, single-leg work, planks, bridges and calf raises.
- **Effort is judged by the talk test** (CDC), because the app cannot measure heart rate.

**Design rules:**
1. Everything is **time-based** (minutes and effort), never distance or pace.
2. **Effort labels:** *Easy* = you can speak full sentences; *Tempo* = short sentences, comfortably hard; *Hard* = only a few words.
3. Every training day has exactly **one target made of five tasks** — warm-up, three main tasks, cool-down. The rest day is one target made of **two tasks** (easy walk, stretching). A full week is 7 targets and 32 tasks.
4. Each task carries its own minutes, so the timer runs per task. The day total is warm-up + the three main tasks + cool-down.
5. Recovery and Rest days are targets too and are completed like any other day.
6. Non-medical wording only.
7. Every warm-up lists a maximum of **6 movements**, each with reps, seconds or minutes, from the Warm-up Catalogue (`runova/context/TRAINING-PLANS.md` §7).
8. Every cool-down is an easy walk plus 4-5 static stretches held 15-20 s (`runova/context/TRAINING-PLANS.md` §8). Static stretching never comes before the session.
9. Every prescription follows a named source: NHS warm-up and post-exercise stretching, RAMP (Jeffreys), Page 2012, FIFA 11+, ACSM resistance-training guidance, 80/20 and the CDC talk test (`runova/context/TRAINING-PLANS.md` §9).

### 7.2 Beginner — Build Foundation (run/walk, 3 run days)
| Day | Target | Task chain (title · type · minutes) |
|---|---|---|
| Mon | Run/Walk Intervals | Running warm-up · `Warm-up` · 6 — Run/Walk pattern · `Run` · 20 — Strength circuit · `Strength` · 6 / 8 / 10 — Stretch block · `Mobility` · 5 — Cool-down · `Cool-down` · 4 |
| Tue | Basic Strength | General warm-up · `Warm-up` · 5 — Lower-body strength · `Strength` · 10 / 12 / 15 — Core circuit · `Strength` · 6 / 8 / 10 — Stretch block · `Mobility` · 5 — Cool-down · `Cool-down` · 4 |
| Wed | Run/Walk Intervals | Same five tasks as Monday |
| Thu | Recovery & Mobility | General warm-up · `Warm-up` · 5 — Easy walk · `Mobility` · 8 / 10 / 12 — Mobility flow · `Mobility` · 6 / 8 / 10 — Stretching · `Mobility` · 5 — Cool-down · `Cool-down` · 4 |
| Fri | Run/Walk Intervals | Same five tasks as Monday |
| Sat | Cross-Training | General warm-up · `Warm-up` · 5 — Brisk walk, cycling or yoga · `Cross-Train` · 15 / 18 / 20 — Core circuit · `Strength` · 5 / 6 / 8 — Stretch block · `Mobility` · 5 — Cool-down · `Cool-down` · 4 |
| Sun | Rest & Recover | Easy walk · `Mobility` · 8 / 10 / 12 — Stretching · `Mobility` · 5 / 6 / 8 — **2 tasks only** |

Five tasks on every training day, two on the rest day. The bands shown are low / mid / high from the weekly running minutes answer.

A Run/Walk day holds its 20-minute pattern inside Main A, between the 6-minute running warm-up and the 4-minute cool-down. Intermediate and Pro level tables are in `runova/context/TRAINING-PLANS.md` §12-§13.

| Week | Pattern (about 20 min) |
|---|---|
| 1 | 1 min run / 1.5 min walk, repeated |
| 2 | 1.5 min run / 2 min walk, repeated |
| 3 | 3 min run / 2 min walk, repeated |
| 4 (review) | 5 min run / 2 min walk, repeated |

### 7.3 Intermediate — Improve Performance (4 run days)
| Day | Target | Timer |
|---|---|---|
| Mon | Rest & Recover | 10 / 12 / 15 min |
| Tue | Easy Run | 25 / 30 / 35 min |
| Wed | Tempo Run | 10 min easy + tempo block + 5 min easy |
| Thu | Strength & Core (bodyweight) | 20 / 25 / 30 min |
| Fri | Interval Run | 10 min warm-up + repeats + 5 min cool-down |
| Sat | Cross-Training or Easy Jog | 30 min |
| Sun | Long Run (Easy effort) | see ramp |

| Week | Tempo block | Intervals (4 min each) | Long run |
|---|---|---|---|
| 1 | 10 min (25 total) | 4 × (2 min Hard / 2 min Easy) (31 total) | 35 min |
| 2 | 12 min (27 total) | 5 repeats (35 total) | 40 min |
| 3 | 15 min (30 total) | 6 repeats (39 total) | 45 min |
| 4 (lighter) | 10 min (25 total) | 4 repeats (31 total) | 35 min |

### 7.4 Pro — Competition Performance (5 run days, two quality sessions)
| Day | Target | Timer |
|---|---|---|
| Mon | Rest & Recover | 10 / 12 / 15 min |
| Tue | Interval Session | 10 min warm-up + repeats + 10 min cool-down |
| Wed | Easy Run + 10-min core finisher | 40 / 45 / 50 min |
| Thu | Tempo Run | 10 min warm-up + tempo block + 10 min cool-down |
| Fri | Strength & Stability | 30 / 35 / 40 min |
| Sat | Easy Run + relaxed fast strides | 30 / 35 / 40 min |
| Sun | Long Run (Easy effort) | see ramp |

| Week | Intervals (5 min each) | Tempo block | Long run |
|---|---|---|---|
| 1 | 5 × (3 min Hard / 2 min Easy) (45 total) | 20 min (40 total) | 60 min |
| 2 | 6 repeats (50 total) | 22 min (42 total) | 70 min |
| 3 | 6 × (4 min Hard / 2 min Easy) (56 total) | 25 min (45 total) | 80 min |
| 4 (lighter) | 4 repeats of week 1 (40 total) | 15 min (35 total) | 50 min |

By total time, most of a Pro week is Easy effort, in line with the 80/20 approach.

### 7.5 Task wording (what each task row shows)
| Task | Prescription lines |
|---|---|
| Running warm-up (6 moves, max) | March on the spot 3 min · heel digs 60 reps in 60 s · knee lifts 30 reps in 30 s · shoulder rolls 2 x 10 · leg swings 10 each leg · ankle bounces 2 x 15 |
| General warm-up (6 moves, max) | March or easy walk 2 min · arm circles 2 x 10 · shoulder rolls 2 x 10 · torso twists 2 x 10 · hip circles 10 each side · half squats 2 x 8 |
| Run/Walk pattern (Main A) | 1 min run / 1.5 min walk, repeated (week 1), then 1.5/2, 3/2, 5/2 |
| Easy Run / Easy lead-in (Main A) | Easy effort throughout, full sentences possible |
| Tempo block (Main B) | Tempo effort, short sentences only |
| Interval repeats (Main A) | Hard effort repeats with easy recovery between |
| Long Run (Main A) | Easy effort, steady throughout |
| Strength or core (Main B) | Squats · lunges · glute bridges · planks · calf raises, 2 sets of 8-12 reps, planks 20-30 s |
| Cross-Training (Main A) | Brisk walk, cycling or yoga at Easy effort |
| Recovery & Mobility (Main A / Main B) | Light movement · gentle mobility · stretching |
| Stretch block (Main C) | Buttock, hamstring, inner thigh, calf and thigh stretch, 15-20 s each |
| Cool-down (always the last task) | Easy walk 2 min · buttock, hamstring, inner thigh, calf and thigh stretch, 15-20 s each |
| Rest day tasks (2) | Easy walk (band-scaled) · stretching (cool-down movements 2-6) |

Intermediate and Pro carry the same task wording; only Main A, Main B and the minutes change.

### 7.6 Event wording (task count, structure and minutes stay the same)
| Event | Wording |
|---|---|
| General Running, Middle Distance, Long Distance | As written above |
| Sprint | Easy Run → *Easy Jog & Running Drills*; Interval → *Sprint Repeats (short fast reps, full rest)* |
| Hurdles | Interval → *Hurdle Technique + Sprint Repeats* |
| Relay | Interval → *Relay Exchange Practice + Acceleration Runs* |

The event changes wording only. Any missing combination must fall back to the generic day — warm-up · training session · strength and core · mobility · cool-down — so the app never crashes and the day always holds 5 tasks.

---

## 8. Daily Flow, Timer, Status, Level-up, Analytics

### 8.1 Calculating today's target
```text
weekInLevel = min(4, daysSince(levelStartDate) / 7 + 1)
getDailyTarget(level, event, weekday, weekInLevel, weeklyMinutesBand)
  → target name, effort label, total minutes, task list
generateTasks(...)
  → exactly 5 tasks on a training day, exactly 2 on the rest day
  → slot 1 Warm-up, slots 2-4 main tasks, slot 5 Cool-down
```
Nothing is stored. Editing the profile changes the result immediately. Slot rules, band scaling and fallbacks: `runova/context/ROUTINE-ENGINE.md`.

### 8.2 Target screen and timer
1. Shows Today's Target, effort label (with a talk-test hint), the task checklist — 5 tasks, or 2 on the rest day — and the timer for the selected task.
2. Every task row has its own action: **Start** for timed tasks, **Mark as Done** for short tasks (warm-up, cool-down, mobility). Pause and Resume are available while a timer runs.
3. Save the **end time** of the running task. When the user returns to the screen, recalculate the time left.
4. Keep the screen on while the timer runs (`FLAG_KEEP_SCREEN_ON`). Beep and vibrate at zero.
5. **Complete** unlocks when the task timer ends. **Finish early** is also allowed. A simple task completes on its first tap.
6. Completing a task saves it and shows "✓ Task complete." The day shows "✓ Today's target complete" only when all of its tasks are done (5, or 2 on the rest day). A second tap on a finished task shows "Already complete" and does not count twice.
7. Show the hint "Keep the app open during your session."
8. Known limitation: if the app is closed or the phone locks, the end-of-timer beep may not sound.

**Build order inside this screen:** get Start and Complete working first, then add the timer.

### 8.3 Status (calculated from dates, no background job)
- **Finished (task):** that task has a saved completion.
- **Finished (day):** all of the day's tasks are finished (5, or 2 on the rest day).
- **Pending:** today, not yet completed.
- **Missed:** a past day on or after the sign-up date with at least one task not finished.
- Days before sign-up are not counted.
- **Streak:** consecutive Finished days ending today or yesterday. A half-finished day breaks the streak.

### 8.4 Level cycle and level-up
- Constants: `LEVEL_DAYS = 28`, `PASS_RATE = 0.85` (use small test values for the demo).
- At the end of the cycle (today ≥ `levelStartDate` + 28), show an **in-app dialog** on Home. It is not a phone notification.
- **Passed (85% or more):** buttons **Move up to [next level]** and **Repeat [level]**.
  > *"Great work! You finished this level. If you feel ready, you can move up to [next level]. If you feel you need more time, repeat [level] to build a stronger base. It's your choice."*
- **Below 85%:** an encouraging message and only **Repeat [level]**.
  > *"You completed [X] of 28 days. A little more time at this level will help you build a stronger base. Repeat [level] and keep going!"*
- **Pro** has no next level: the buttons are **Stay at Pro** and **Repeat**.
- Move up or Repeat sets the level and resets `levelStartDate` to today. Nothing else is stored.
- The dialog reports days completed only; it never claims to judge fitness.

### 8.5 Analytics
- Days completed this week (x / 7) with a `ProgressBar`
- **Level progress:** days Finished in the current cycle (for example 9 / 28, with 24 needed to pass)
- Current streak
- Counts of Finished / Missed / Pending
- Built with `TextView`, `ProgressBar` and cards only; no chart library

### 8.6 Home screen (top to bottom)
Greeting with level and event → week strip (✓ / missed / pending per day) → **Today's Target card** (name, effort, total minutes, the task checklist with minutes — warm-up expanded so the reps show — and a Start button per timed task) → Level progress and the Finished / Missed / Pending counts → Training Goal label.

---

## 9. Profile

- Shows name and identity only: first name, last name, gender, date of birth, derived age, height, weight, weekly minutes, event, level and Training Goal. **No picture.**
- **Edit Profile** reuses the Sign Up / Questionnaire form in `EDIT` mode (one form, two modes: `SIGNUP` and `EDIT`), pre-filled and with the same validation.
- Every field is editable, except that the **level can only move down** (Beginner < Intermediate < Pro). Moving up happens only through the level-up dialog.
- On Save: validate, save, return to Home. The app adjusts automatically because the plan is calculated from the saved values.
- **History is never rewritten.** Past ✓ and Missed days stay as they were. If today is already completed it stays ✓, and the new plan applies from tomorrow.
- "About RUNOVA" reopens the start-up pop-up.

---

## 10. Data & Storage (SharedPreferences, file `RUNOVA_DATA`)

| Key | Content |
|---|---|
| `seenIntro` | pop-up shown flag |
| `firstName`, `lastName`, `dob`, `gender` | identity |
| `height`, `weight`, `weeklyMinutes`, `event` | questionnaire |
| `level` | Beginner / Intermediate / Pro |
| `levelStartDate` | start of the current 28-day cycle |
| `signupDate` | first day that counts |
| `completedDates` | string set of `yyyy-MM-dd` |
| `timerEndTime` | saved end time while a timer is running |

Course note: if the instructor requires a database, keep `completedDates` in one table, `daily_log(date, status)`. Nothing else changes. There is no cloud backup, so uninstalling the app loses all history.

---

## 11. UI & Design System

- One consistent look across all Activities.
- `ConstraintLayout`, `ScrollView` where needed (Sign Up and Questionnaire must scroll on small phones), `0dp` with constraints, `dp` for sizes, `sp` for text.
- Dark navy/blue background, bright blue primary, green for completed, rounded cards, white main text, light gray secondary text, rounded blue buttons.
- Colors live in `res/values/colors.xml`, for example: `runova_background #07152E`, `runova_primary #1683E8`, `runova_secondary #0D5DB7`, `runova_success #39C84A`, `runova_text #FFFFFF`, `runova_text_secondary #B8C4D6`. Never repeat raw hex values in layouts.
- One theme for every Activity.
- Priority order: **functionality → navigation → validation → offline storage → responsive UI → visual polish.**

---

## 12. Offline & Code Quality

- Must run end to end in airplane mode. No internet, Firebase, APIs, cloud, online authentication, or unnecessary dependencies.
- Clear class and variable names (`nameInput`, `eventSpinner`, `startButton`; never `x` or `button1`).
- Simple methods: `getDailyTarget()`, `validateInputs()`, `saveProfile()`, `loadProfile()`, `countFinishedDays()`, `checkLevelCycle()`.
- Match XML IDs to `findViewById`; comments where useful; no unnecessary code.

---

## 13. Build Order

1. `PrefsHelper`, `Profile` and `DateUtils`
2. `LevelPlan` and `getDailyTarget()` with fallbacks (test with sample values)
3. Welcome screen and start-up pop-up
4. Sign Up screen with validation
5. Questionnaire screen with validation (Level spinner defaults to Beginner)
6. Home screen: Today's Target card, week strip, status counts
7. Target screen: Start and Complete first, status calculation, no double counting
8. Timer on the Target screen
9. Analytics screen (weekly, level progress, streak, counts)
10. Level-up dialog (`checkLevelCycle()`)
11. Profile screen and Edit Profile (`EDIT` mode, level moves down only)
12. Airplane-mode testing, then UI polish

**Test checklist (airplane mode):** app opens · pop-up shows once · START works · Sign Up validates · Questionnaire validates · level and event selectable · Home shows the correct target for level and weekday · Start and Complete work · a day cannot be counted twice · timer survives leaving and returning · Missed and Pending are correct · level-up dialog appears (use small test values) · Repeat and Move up work · Profile edit adjusts the plan · Back navigation works · no crashes · no internet needed.

---

## 14. Risks

- **Timer while the app is closed or the phone is locked:** limited by design (Section 8.2). Keep the screen on and show the hint.
- **Trust-based tracking:** users can mark days done without doing them. The pop-up says so.
- **No cloud backup:** uninstalling loses all history.
- **Deadline:** finish the full flow end to end before polishing visuals.

---

## 15. Course Requirements, Rubric & Documentation

**Instructor requirements:** Android Studio, Java, completely offline, no online database, at least 5 interconnected Activities, Intent navigation, appropriate styles and themes, responsive layouts, meaningful input validation, user interaction, original concept, final APK, source code, documentation and presentation. **Category:** Health and Fitness.

**Originality:** an offline training companion that gives each user a level-based daily target with an in-app timer and a performance-based level-up, rather than a generic calorie tracker or step counter.

**Rubric:** Proposal 10 · UI/UX & system design 15 (Activity Diagram with all screens, Intent paths, validation and offline storage, plus a high-fidelity prototype of every screen) · Final application 60 (core functionality 20, UI 15, validation 10, code quality 5, originality 5, offline 5) · Documentation 15.

**Documentation includes:** logo and title ("RUNOVA — Offline Training & Progress Tracker"), group members, app description, features, screenshots of every screen, user guide.

**Objectives:**
1. Provide a simple offline training companion.
2. Give each user a daily target matched to their level and event.
3. Provide an in-app timer for each session.
4. Track daily completion, weekly progress and level progress.
5. Let users move up or repeat a level based on their own performance.
6. Demonstrate Android Studio, Java, Intent navigation, validation and offline storage.
7. Provide a responsive, consistent interface.

**Limitations:** no professional coaching, medical advice, distance/speed/heart-rate tracking, GPS, wearables, online sync, cloud backup, AI-generated plans, online accounts or nutrition tracking. Completion is self-reported.

**Demo script:** launch → read the pop-up → START → Sign Up → Questionnaire (pick Beginner and Sprint) → Home shows today's target → open Target → start the timer (short test value) → Complete → Analytics shows updated progress → Profile → Edit (change the event) → Home shows the adjusted target → (with test values) the level-up dialog appears.

---

## 16. Deliverables Checklist

- [ ] Start-up pop-up (once, reopenable from Profile)
- [ ] Sign Up with validation (names, DOB, gender)
- [ ] Questionnaire with validation (height, weight, weekly minutes, event, level)
- [ ] `getDailyTarget()` + `generateTasks()` for all levels, weeks, weekdays and events, with fallback
- [ ] Every training day generates exactly 5 tasks and the rest day exactly 2 (asserted in a unit-test matrix)
- [ ] Home with Today's Target checklist, week strip, status counts
- [ ] Target screen with per-task Start / timer / Complete / Finish early, day roll-up, no double counting
- [ ] Warm-up Catalogue applied (max 6 movements, each with reps or seconds) and Cool-down applied
- [ ] Finished / Missed / Pending, streak, weekly and level progress
- [ ] Analytics screen
- [ ] Level-up dialog (Move up / Repeat, Pro variant)
- [ ] Profile (no picture) with Edit Profile that adjusts the plan
- [ ] SQLite storage
- [ ] Consistent theme and responsive layouts
- [ ] Tested in airplane mode
- [ ] APK, source code, documentation, screenshots, presentation

---

## Appendix — AI / Copilot Master Prompt

```text
You are helping me build RUNOVA from scratch: an offline Android app (Java, Android
Studio) for runners and regular people who run. Class project, close deadline: keep
everything simple and buildable after each change.

HARD RULES
- Fully offline. No internet, Firebase, APIs, cloud, online database, AI/ML, GPS,
- **Full offline support** with local notifications allowed: any internet-dependent feature (Firebase, online DB, cloud, AI/ML, GPS, wearables, social, external APIs, push/server notifications, profile pictures, medical advice) is excluded; **MPAndroidChart charts, local scheduled notifications, background scheduling (simple approach) and the in-app calendar are included**. Tasks are activity-only: nutrition, food, water/hydration, sleep and meditation tasks are never generated (meditation is an open team item).
- 7 Activities, all navigation with Intents. Declare every Activity in
  AndroidManifest.xml. Launcher = MainActivity (Welcome).
- Complete files, file name before each code block, match XML IDs to findViewById,
  do not modify unrelated working files, no new dependencies.
- Build one screen at a time; explain what changes and why before changing a file.

SCREENS
MainActivity (Welcome + one-time start-up pop-up) -> SignupActivity ->
QuestionnaireActivity -> HomeActivity. Home <-> TargetActivity (timer),
AnalyticsActivity, ProfileActivity (editable, no picture). Fixed bottom nav on
Home, Analytics, Target and Profile.

SIGN UP: first name, last name, date of birth, gender. Age is derived from DOB
(DateUtils.ageFrom); never asked.
QUESTIONNAIRE: height, weight, weekly running minutes, event (Sprint, Middle
Distance, Long Distance, Hurdles, Relay, General Running), level (Beginner,
Intermediate, Pro; Beginner preselected). Height, weight, gender, age are
display-only. Weekly minutes only picks a low/mid/high duration band (<60, 60-150, >150).
VALIDATION: names not empty; valid DOB, not future, derived age 13-100; gender
selected; height/weight positive numbers; weekly minutes whole number >= 0; event
and level selected. Errors beside fields, keep entered values, never crash.

PLAN (nothing stored; calculate on the fly)
getDailyTarget(level, event, weekday, weekInLevel, weeklyMinutesBand) returns
target name, effort label (Easy/Tempo/Hard by talk test), total minutes, task list.
Every training day = exactly 5 tasks: Warm-up (max 6 moves, each with reps or
seconds) -> 3 main tasks (theme activity, strength or core, mobility/stretch) ->
Cool-down (easy walk + static stretches of 15-20 s). Rest day = exactly 2 tasks
(easy walk, stretching). weekInLevel = min(4, daysSince(levelStartDate)/7 + 1).
One target per day, 7 per week, 32 tasks per week. Use the plan tables in this
overview and runova/context/TRAINING-PLANS.md. Every combination needs a fallback
that still returns 5 tasks. Time-based only; no distance.

DAILY FLOW
Start a task with a per-task CountDownTimer; save the end time and recalculate on
return; keep screen on; beep/vibrate at zero. Complete unlocks at zero; Finish early
allowed. Each task is marked once (one row per task); the day rolls up to Finished
only when all 5 tasks (2 on the rest day) are done. Never count a day twice.
Status is calculated from dates: Finished, Pending (today), Missed (past day on or
after signupDate with at least one task unfinished).

LEVEL CYCLE: LEVEL_DAYS=28, PASS_RATE=0.85. At the end show an in-app dialog.
>=85%: Move up or Repeat. Below: Repeat only. Pro: Stay at Pro or Repeat. Either
choice sets level and resets levelStartDate to today.

PROFILE: shows name and identity (no picture). Edit reuses the Sign Up/Questionnaire
form in EDIT mode. Level can only move down there. Never rewrite past days.

STORAGE: SQLite via DbHelper — tables profile, routines, tasks, history/meta. Key
profile fields: seenIntro, firstName, lastName, dob, gender, height, weight,
weeklyMinutes, event, level, goal, levelStartDate, signupDate. tasks rows:
id, routineId, ordinal, title, type, minutes, effort, steps, status — 5 rows per
training day, 2 on the rest day. Completions in history rows; timer end time in meta.

DESIGN: ConstraintLayout, ScrollView where needed, dp/sp, colors in colors.xml,
one theme, consistent rounded cards and buttons. Present the app as a training
organizer, not medical advice.

BUILD ORDER: DbHelper/Profile/DateUtils -> routine generator -> Welcome+pop-up ->
Sign Up -> Questionnaire -> Home -> Target (Start/Complete, then timer) ->
Analytics (MPAndroidChart) -> level-up dialog -> Profile/Edit -> local
notifications -> calendar -> airplane-mode test -> UI polish.
```
