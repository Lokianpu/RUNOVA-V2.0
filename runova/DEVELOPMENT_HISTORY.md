# RUNOVA Development History

> **Purpose:** This file tracks all development sessions, changes, and modifications to the RUNOVA project.
> Each session is recorded with date, phase, changes, and rationale so future AI agents/developers understand the project evolution.

---

## How to Use This File

**For AI Agents/Developers:**
- Read this file FIRST before making any changes
- Understand what has been built and why
- Check the latest session to see current state
- Review "Known Issues" and "Testing Results" before modifications

**To Add New Session:**
- Use command: `./add-to-history.sh` from project root
- Or use template in `.add-history-template.md`
- Include: date, phase, changes made, files created/modified, reasoning

---

## Project Overview

- **Start Date:** 2026-09-26
- **Project:** RUNOVA - Offline Android Training App
- **Language:** Java
- **Platform:** Android (minSdk 26, targetSdk 34)
- **Architecture:** Simple MVC pattern
- **Database:** SQLite (3 tables: profile, tasks, notifications)
- **Spec:** RUNOVA_Project_Overview:Agent_Specs.md V8
- **Build Tool:** Gradle 9.5.0
- **Dependencies:** AndroidX, Material3, MPAndroidChart, CardView

---

## Session 1: Foundation & Database (2026-09-26)

### Phase 1: Database + Models + Training Data

**Duration:** ~3-4 hours

**Objective:** Establish backend foundation before any UI work.

**Changes Made:**

#### 1. Database Schema (DBHelper.java)
- Created SQLite database with 3 tables:
  - `profile` (single row, 13 fields including activeTaskId/activeTaskEndTime for timer)
  - `tasks` (task rows with date, name, type, minutes, status, sortOrder)
  - `notifications` (level-up offers, type/message/created/resolved)
- Added index on `tasks.date` for performance

**Why:** Spec requires exactly 3 tables, offline SQLite storage, single timer state in profile.

#### 2. Model Classes
- `Profile.java`: User identity + training config + timer state
- `Task.java`: Individual task with status (Pending/Finished/Missed)
- `Notification.java`: In-app messages
- `DayTemplate.java`: Container for daily task templates
- `TaskTemplate.java`: Individual task in template (name/type/minutes)

**Why:** Clean data models separate from database logic.

#### 3. Helper Utilities
- `DateUtils.java`: Age calculation, weekday (1-7), date validation, daysBetween
- `ValidationHelper.java`: Form validation rules (created but not used, validation done inline)

**Why:** Centralized date/validation logic per spec.

#### 4. Training Content
- `LevelPlan.java`: All 21 static templates (3 levels × 7 days)
  - Beginner: 3-4 tasks/day, 60min max, 10min warm-up/cool-down
  - Intermediate: 3-5 tasks/day, 90min max, 15min warm-up/cool-down
  - Pro: 4-5 tasks/day, 120min max, 15min warm-up/cool-down
  - Sunday: 2 recovery tasks (Easy Walk + Stretch)
- `TrainingContentLibrary.java`: Task instructions + event-specific wording

**Why:** Spec requires static approved weekly routines, no runtime generation/randomness.

#### 5. Task Generation
- `TaskGenerator.java`: Lazy one-day-at-a-time generation
  - Checks if tasks exist for date
  - If not: gets level/event from profile, gets template, creates tasks
  - Never generates in advance, never regenerates past days

**Why:** Spec requires lazy generation to avoid invalidation on profile edits.

**Files Created (12):**
```
app/src/main/java/com/runova/database/
└── DBHelper.java

app/src/main/java/com/runova/models/
├── Profile.java
├── Task.java
├── Notification.java
├── DayTemplate.java
└── TaskTemplate.java

app/src/main/java/com/runova/helpers/
├── DateUtils.java
└── ValidationHelper.java

app/src/main/java/com/runova/training/
├── LevelPlan.java
└── TrainingContentLibrary.java

app/src/main/java/com/runova/controllers/
└── TaskGenerator.java
```

**Key Decisions:**
- Profile table stores timer state (no separate timer table)
- Day status derived (not stored) - all tasks Finished = day Finished
- Static templates hardcoded (no database storage, prevents drift)

---

## Session 2: Welcome + Onboarding (2026-09-26)

### Phase 2-3: Welcome Screen + Signup + Questionnaire

**Duration:** ~2-3 hours

**Objective:** First-time user onboarding flow.

**Changes Made:**

#### 1. Build Configuration
- Set `minSdk = 26` (was 24, needed for adaptive icons)
- Added `android.useAndroidX=true` to gradle.properties
- Added `android.enableJetifier=true` for third-party library conversion
- Removed splash screen theme (missing library)
- Added `app_name` string resource to strings.xml

**Why:** Build errors, adaptive icon requirement, AndroidX dependencies needed for Material3.

#### 2. Welcome Screen
- `activity_main.xml`: Logo, "RUNOVA" text, START button on gradient background
- `MainActivity.java`: 
  - Checks if profile exists (skip to Home)
  - Shows start-up pop-up on first launch (exact spec text with emoji)
  - Navigates to Signup
  - Calls `finish()` so Back doesn't return here

**Why:** Spec requires start-up disclaimer, profile existence check, no return to welcome.

#### 3. Signup Screen
- `activity_signup.xml`: Blue card, 4 fields (firstName, lastName, DOB date picker, gender spinner)
- `SignupActivity.java`:
  - Validation: not empty, valid date, age 13-100
  - Passes data to Questionnaire via Intent extras
  - Age derived from DOB, never asked directly

**Why:** Spec requires identity collection before training config.

#### 4. Questionnaire Screen
- `activity_questionnaire.xml`: Blue card, 5 fields (height, weight, event, level, goal)
- `QuestionnaireActivity.java`:
  - Validation: height 100-250cm, weight 25-250kg, all selections required
  - Beginner preselected for level
  - Creates profile row in database (id=1, seenIntro=1, levelStartDate=today)
  - Navigates to Home after save

**Why:** Spec requires training configuration, Beginner default, profile creation.

#### 5. Supporting Resources
- `arrays.xml`: Spinner options (gender, event, level, goal)
- Updated AndroidManifest: MainActivity launcher, Signup/Questionnaire/Home activities declared

**Why:** Spinners need string-array resources.

**Files Created:**
```
app/src/main/res/layout/
├── activity_main.xml (fresh, template archived to archive_original/)
├── activity_signup.xml
└── activity_questionnaire.xml

app/src/main/java/com/runova/
├── MainActivity.java
├── SignupActivity.java
└── QuestionnaireActivity.java

app/src/main/res/values/
├── strings.xml (updated with app_name)
└── arrays.xml (new)

gradle.properties (updated with androidx flags)
app/build.gradle.kts (minSdk 26)
app/src/main/AndroidManifest.xml (activities declared)
```

**Key Decisions:**
- Age derived from DOB (spec requirement)
- Beginner preselected (spec requirement)
- Start-up popup shows exact spec text with emoji

---

## Session 3: Home Screen + Navigation (2026-09-26)

### Phase 4: Home Screen + Task Generation + Bottom Nav

**Duration:** ~3-4 hours

**Objective:** Display today's tasks, trigger generation, navigation to other screens.

**Changes Made:**

#### 1. Home Screen Layout
- `activity_home.xml`: Greeting header, date display, bell icon, level progress card, task list (RecyclerView), bottom nav bar
- `item_task_card.xml`: Task card showing task name, duration, status badge (Pending/Finished/Missed)
- Design: Blue cards (#0174DB), 20dp radius, white text, gradient background from design system

**Why:** Spec requires greeting with firstName, date, tasks list, progress indicator, notifications access.

#### 2. HomeActivity Logic
- Load profile: get firstName for greeting ("Hello, [name]!")
- Generate tasks: call `TaskGenerator.generateDailyTasks(today)` on load
- Display date: current date formatted
- Level progress: "Day X of 28" calculated from levelStartDate
- RecyclerView with TaskAdapter showing all today's tasks
- Bottom nav buttons navigate to Target/Analytics/Profile
- Bell icon (stub, wired in Session 7)

**Why:** Lazy generation triggered when Home opens, shows only today's tasks.

#### 3. Stub Activities
- `TargetActivity.java`: empty stub with layout
- `AnalyticsActivity.java`: empty stub with layout  
- `ProfileActivity.java`: empty stub with layout
- `layout_navigation_bar.xml`: reusable bottom nav component (4 icon buttons)
- Updated AndroidManifest: declared 3 new activities

**Why:** Navigation needs targets, implemented in later phases. Bottom nav reused across all main screens.

**Files Created:**
```
app/src/main/res/layout/
├── activity_home.xml
├── item_task_card.xml
├── activity_target.xml (stub)
├── activity_analytics.xml (stub)
├── activity_profile.xml (stub)
└── layout_navigation_bar.xml

app/src/main/java/com/runova/
├── HomeActivity.java (complete)
├── TargetActivity.java (stub)
├── AnalyticsActivity.java (stub)
└── ProfileActivity.java (stub)

app/src/main/AndroidManifest.xml (3 activities added)
```

**Key Decisions:**
- RecyclerView over ListView (modern Android)
- Bottom nav as reusable `<include>` layout
- Task status shown as badge on card (color-coded)

---

## Session 4: Target Screen + Timer (2026-09-26)

### Phase 5: Task Execution + Single Active Timer

**Duration:** ~4-5 hours

**Objective:** Allow users to start timers, complete tasks, save status to database.

**Changes Made:**

#### 1. Timer Controller
- `TimerController.java`:
  - Wraps Android CountDownTimer with callback interface
  - Saves `activeTaskId` + `activeTaskEndTime` to profile table on start
  - Single timer app-wide (only one can run, blocks other starts)
  - `clearTimer()` removes timer state from profile
  - `stopTimer()` cancels countdown without clearing DB state

**Why:** Spec requires single active timer stored in profile table for persistence across rotations.

#### 2. Target Screen Layout
- `activity_target.xml`: Today's task list with timer controls per task
- `item_task_control.xml`: Task card with:
  - Task name and duration
  - START button (enabled when no active timer)
  - Timer display showing MM:SS countdown
  - COMPLETE button (unlocks at zero, "Finish early" allowed)
- Shows "WAIT" on other START buttons when timer active
- Finished tasks show "DONE" button (disabled)

**Why:** Spec requires per-task timer with completion controls and single active timer enforcement.

#### 3. TargetActivity Logic
- RecyclerView adapter with timer state per task
- START button: checks no active timer exists, starts CountDownTimer, saves state to profile
- Timer display: updates every second showing MM:SS format
- COMPLETE button: updates task status to "Finished" in database, clears timer state
- Screen stays on during timer (`FLAG_KEEP_SCREEN_ON`)
- Finished tasks show "DONE" (disabled button, no interaction)

**Why:** Single timer enforcement prevents conflicts, persistence allows survival across rotation/app close.

**Files Created:**
```
app/src/main/res/layout/
├── activity_target.xml (complete implementation)
└── item_task_control.xml

app/src/main/java/com/runova/controllers/
└── TimerController.java

app/src/main/java/com/runova/
└── TargetActivity.java (complete implementation)
```

**Key Decisions:**
- Timer state in profile table (not separate timer table per spec)
- Recalculate remaining time from `activeTaskEndTime` on screen reopen
- Allow "Finish early" (COMPLETE unlocks immediately, not waiting for zero)
- Keep screen on during active timer (user experience)

---

## Session 5: Analytics Screen + Charts (2026-09-26)

### Phase 6: Progress Tracking + MPAndroidChart

**Duration:** ~4-5 hours

**Objective:** Display level progress, streak, status counts, weekly chart using MPAndroidChart library.

**Changes Made:**

#### 1. Analytics Layout
- `activity_analytics.xml`: 4 cards stacked vertically
  - Level progress card: ProgressBar showing X/28 days, green at 85%
  - Streak card: Single number display
  - Status counts card: 3 columns (Finished/Missed/Pending)
  - Chart card: BarChart from MPAndroidChart (300dp height)
- ScrollView wrapper for small screens

**Why:** Spec requires progress visualization with real charts, not just text.

#### 2. AnalyticsActivity Logic
- **Level Progress Calculation:**
  - Query levelStartDate from profile
  - Count Finished days where ALL tasks for that date are Finished
  - Display X/28, change ProgressBar color to green at 24 days (85%)
  
- **Streak Calculation:**
  - Start from today, iterate backwards day-by-day
  - Count consecutive days where all tasks Finished
  - Stop at first incomplete day or missing data
  
- **Status Counts:**
  - Query tasks table from levelStartDate to today
  - GROUP BY status, COUNT(*)
  - Scoped to current 28-day cycle only

- **Weekly Chart (MPAndroidChart BarChart):**
  - Current week Mon-Sun (7 bars)
  - Each bar = count of Finished tasks for that day
  - White bars on blue background
  - X-axis labels: Mon, Tue, Wed, Thu, Fri, Sat, Sun
  - Y-axis shows task count

**Why:** All data from existing tasks table, no new tables. Week view is default per spec. Chart provides visual motivation.

**Files Created:**
```
app/src/main/res/layout/
└── activity_analytics.xml (complete)

app/src/main/java/com/runova/
└── AnalyticsActivity.java (complete implementation)
```

**Key Decisions:**
- Day status derived (all tasks Finished = day Finished), not stored in DB
- Queries check this condition to avoid inconsistent state
- MPAndroidChart used (already in dependencies)
- Week starts Monday (Calendar.MONDAY)
- Streak stops at first incomplete day (strict definition)

---

## Session 6: Profile Editing (2026-09-26)

### Phase 7: Profile View/Edit + Level Change

**Duration:** ~3-4 hours

**Objective:** Allow users to edit profile, handle level changes correctly with confirmation.

**Changes Made:**

#### 1. Profile Layout
- `activity_profile.xml`: 2 cards stacked vertically
  - Identity card: firstName, lastName, age (read-only TextView), gender spinner
  - Training card: height, weight, event, level, goal spinners
- SAVE CHANGES button below cards
- About RUNOVA button (reopens start-up pop-up)
- Age derived from DOB, shown as "Age: X"

**Why:** Spec requires editable profile, age derived not editable, access to disclaimer popup.

#### 2. ProfileActivity Logic
- Load profile: populate all fields from database on open
- Validation: same rules as Signup/Questionnaire (not empty, ranges)
- **Level Change (special case):**
  - Detects if level spinner value changed from current
  - Shows confirmation dialog: "Changing level will delete all tasks and reset progress. Continue?"
  - If Yes: `DELETE FROM tasks`, update level in profile, set `levelStartDate = today`
  - Wipes everything for clean 28-day start
- **Normal Edits:**
  - Just save to profile table
  - Next lazy-generation uses new values automatically (no regeneration needed)
- About button: reopens start-up pop-up with exact text

**Why:** Spec requires level change = clean slate. Other edits (event/goal) don't need regeneration due to lazy generation pattern.

**Files Created:**
```
app/src/main/res/layout/
└── activity_profile.xml (complete)

app/src/main/java/com/runova/
└── ProfileActivity.java (complete implementation)

app/src/main/res/values/
└── arrays.xml (already exists, used for spinners)
```

**Key Decisions:**
- Level change is destructive (wipes tasks) with confirmation dialog prevents accidental loss
- Event/goal changes are non-destructive (tomorrow's tasks use new values)
- About popup reopens original text (helps users who missed it first time)
- Age shown but not editable (DOB stored, never changed)

---

## Session 7: Level Cycle + Notifications (2026-09-26)

### Phase 8: 28-Day Cycle + Level-Up System

**Duration:** ~3-4 hours

**Objective:** Implement level-up prompts at day 28, persistent notifications inbox.

**Changes Made:**

#### 1. Level Cycle Controller
- `LevelCycleController.java`:
  - `shouldShowLevelUpPrompt()`: checks if >= 28 days since levelStartDate and no unresolved LEVEL_UP_OFFER
  - `getPassRate()`: calculates (Finished days / 28) where Finished = all tasks that day complete
  - `getCurrentLevel()`: queries current level from profile
  - `applyLevelChoice(newLevel)`: updates level, wipes tasks, sets levelStartDate=today, marks notifications resolved
  - `createLevelUpOffer(message)`: inserts LEVEL_UP_OFFER row if none unresolved exists
  - `hasUnresolvedLevelUpOffer()`: checks notifications table

**Why:** Centralized level cycle logic, shared by dialog and notifications to prevent duplicate code.

#### 2. Notifications Screen
- `activity_notifications.xml`: RecyclerView for notification list, empty state TextView
- `item_notification.xml`: Notification card with message text + 2 action buttons
- `NotificationsActivity.java`:
  - Query unresolved notifications from database (resolved=0)
  - Display level-up offers with dynamic options:
    - Passed (≥85%): "Move up to [next level]" or "Repeat [current level]"
    - Failed (<85%): "Repeat [current level]" only (second button hidden)
    - Pro passed: "Stay at Pro" (no higher level exists)
  - Both buttons call `applyLevelChoice()`, then refresh list
  - Empty state: "No notifications" when list empty

**Why:** Persistent inbox for missed level-up prompts. Same handler as dialog prevents stale state.

#### 3. Home Screen Integration
- Added `LevelCycleController` to HomeActivity
- Added `checkLevelUpPrompt()` in onCreate
- Shows AlertDialog at day 28 with pass/fail options:
  - Passed: "Move up to [next]" positive button, "Repeat [current]" negative button
  - Failed: "Repeat [current]" positive button only
- Dialog dismissed without choice → calls `createLevelUpOffer()` to save to notifications table
- Bell icon click → opens NotificationsActivity

**Why:** Spec requires dialog at day 28, persistent in notifications if dismissed. Bell icon provides access.

**Files Created:**
```
app/src/main/res/layout/
├── activity_notifications.xml
└── item_notification.xml

app/src/main/java/com/runova/controllers/
└── LevelCycleController.java

app/src/main/java/com/runova/
└── NotificationsActivity.java

Updated files:
├── app/src/main/java/com/runova/HomeActivity.java (level-up check added)
└── app/src/main/AndroidManifest.xml (NotificationsActivity declared)
```

**Key Decisions:**
- Single `applyLevelChoice()` handler called by both dialog and notifications (prevents divergence)
- Both paths wipe tasks, update level, reset levelStartDate, mark notifications resolved
- Only one unresolved LEVEL_UP_OFFER at a time (check before creating)
- Dialog onDismiss creates notification (ensures never lost)
- Pro level can repeat but not advance (no "Super Pro")

---

## Session 8: History System + Documentation (2026-09-26)

### Phase: Documentation + Tooling

**Duration:** ~1 hour

**Objective:** Create comprehensive development history file and automated tooling for future AI agents to understand project evolution.

**Changes Made:**

#### 1. Development History File
- Created DEVELOPMENT_HISTORY.md with all 7 previous sessions documented
- Included complete context: changes, files, reasoning, key decisions for each phase
- Added sections: Known Issues, Future Enhancements, Testing Results, Notes for Future AI Agents
- Documented all 21 Java classes, 8 activities, architecture patterns
- ~800 lines of detailed session history from Session 1 (Foundation) through Session 7 (Level Cycle)

**Why:** Future AI agents need complete context about past development to make informed changes without breaking existing functionality or duplicating work. Without history, agents must reverse-engineer decisions from code alone.

#### 2. Automation Script
- Created add-to-history.sh interactive bash script
- Prompts for session details (number, date, phase, changes, files, reasoning)
- Automatically inserts formatted entry before "Current State" section
- Updates "Last Updated" timestamp, creates backup file
- Made executable with proper permissions (chmod +x)

**Why:** Manual history updates prone to formatting errors and inconsistency. Script ensures uniform format and prevents content loss through automatic backups.

#### 3. Manual Template
- Created .add-history-template.md with example format
- Instructions for manual editing when script unavailable
- Shows expected structure and markdown formatting
- Includes example Session 8 entry

**Why:** Provides fallback option and reference for proper formatting when script cannot be used interactively.

**Files Created:**
```
runova/DEVELOPMENT_HISTORY.md (new, ~800 lines)
add-to-history.sh (new, executable bash script)
.add-history-template.md (new, template)
```

**Key Decisions:**
- Placed history in runova/ folder with other documentation (RUNOVA_Project_Overview:Agent_Specs.md) for discoverability
- Used markdown for readability by both humans and AI agents
- Script uses awk for safe insertion to preserve existing content
- Backup created automatically on each run (*.backup) to prevent accidental data loss
- Comprehensive format: each session includes objective, changes, files, reasoning, key decisions
- Added "Notes for Future AI Agents" section with patterns, architecture, testing checklist

---

---

## Session 9: Full-Screen Chrome + Instant Navigation (2026-09-26)

### Phase: UI Consistency + Polish

**Duration:** ~1 hour

**Objective:** Make every screen fill the display edge to edge (no empty band at the top or the bottom), keep the shared bottom navigation bar fixed in place, and remove the default slide activity transition so screen changes are instant.

**Changes Made:**

#### 1. Shared window chrome helper
- Added `WindowHelper` to the helpers package: `applyEdgeToEdge()` makes the window draw behind the system bars and disables this activity's open/close transition; `applyTopInset()`, `applyBottomInset()` and `applyVerticalInsets()` add the status bar / gesture bar / display cutout heights on top of the padding a view already declares
- Existing padding is captured once from XML and insets are always added to those values, so repeated inset dispatches cannot stack or change declared spacing
- API 34+ uses `overrideActivityTransition(..., 0, 0)`; older releases use `overridePendingTransition(0, 0)`

**Why:** Insets cannot be hardcoded in XML because status bar and gesture bar heights are runtime values.

#### 2. Theme
- Added `android:windowAnimationStyle` = `Animation.RUNOVA.None` with all four activity animations set to `@null`
- Added `android:windowLayoutInDisplayCutoutMode` = `shortEdges` so the gradient also fills the display cutout

**Why:** Killing transitions in one theme style covers every screen and every platform version, including back navigation, with no per-screen animation code.

#### 3. Bottom navigation bar
- One id for the bar on every screen (`@+id/navigationBar`); home and target previously used `bottomNavBar` while the other four used `navigationBar`
- Height changed from the hardcoded 95dp (already overridden to wrap_content by every `<include>`) to an explicit `wrap_content` with the same 25dp padding, so the bar grows by exactly the system inset and its background reaches the physical bottom edge
- Buttons, padding, sizes and colours untouched

#### 4. Screens
- All 8 Activities call the same lines in `onCreate`: `applyEdgeToEdge()` before `setContentView()`, then the inset calls on the scroll container and (where present) the navigation bar
- Scroll containers only gained ids (`screenScroll`; home's `main` was renamed) so insets could be applied; no child view was modified

**Why:** The app targets SDK 34 with no edge-to-edge call, so the framework inset the content area and the gradient background was drawn twice at different bounds - producing the empty band at the top, a strip below the navigation bar and a visible seam. The default activity transition also made the shared navigation bar appear to move between screens.

**Verification:**
- `./gradlew :app:assembleDebug` -> BUILD SUCCESSFUL
- Installed on the Pixel_6a AVD (API 37, gesture navigation) and screenshotted Main, Home, Target, Analytics and Profile: the content view spans [0,0]-[1080,2400] with no bands; the navigation bar starts at y=2100 and ends flush at y=2400
- Pixel scan down x=4 of the Home screenshot: smooth ramp from #080042 at y=0 (delta 3-4 per 60px, no seam); the only jump is the intended start of the navigation bar background
- Transition test with `transition_animation_scale` and `window_animation_scale` set to 10: burst captures after a navigation tap were byte-identical and showed only the settled new screen, so no animated frame was ever rendered
- AlertDialog (start-up popup and About) still renders and dims correctly

**Files Created/Modified:**
```
app/src/main/java/com/runova/helpers/WindowHelper.java (new)
app/src/main/res/values/themes.xml
app/src/main/res/layout/layout_navigation_bar.xml
app/src/main/res/layout/activity_home.xml
app/src/main/res/layout/activity_target.xml
app/src/main/res/layout/activity_analytics.xml
app/src/main/res/layout/activity_profile.xml
app/src/main/res/layout/activity_notifications.xml
app/src/main/res/layout/activity_signup.xml
app/src/main/res/layout/activity_questionnaire.xml
app/src/main/res/layout/activity_main.xml
app/src/main/java/com/runova/MainActivity.java
app/src/main/java/com/runova/SignupActivity.java
app/src/main/java/com/runova/QuestionnaireActivity.java
app/src/main/java/com/runova/HomeActivity.java
app/src/main/java/com/runova/TargetActivity.java
app/src/main/java/com/runova/AnalyticsActivity.java
app/src/main/java/com/runova/ProfileActivity.java
app/src/main/java/com/runova/NotificationsActivity.java
```

**Key Decisions:**
- Changed the chrome only: no inner widget, size, colour or spacing was altered. Insets are added on top of the spacing the layouts already declare
- Used `WindowCompat.setDecorFitsSystemWindows()` from androidx.core (already a transitive dependency) instead of adding a dependency or an `EdgeToEdge` call
- Disabled transitions in the theme (global and declarative) and per activity (API 34+ safe) so no screen can animate on any supported version
- Applied insets in code instead of XML because bar heights are runtime values
- Did not fix the pre-existing bugs found during the review (see Known Issues) because they are outside this session's scope

---

## Session 10: Bug Fixes + Full Verification of All 8 Screens (2026-09-26)

### Phase: Defect Resolution

**Duration:** ~1 hour

**Objective:** Clear device data as approved, fix the two defects Session 9
identified but left open, and verify every remaining screen that could not be
reached before (Signup, Questionnaire, Notifications).

**Changes Made:**

#### 1. Home notification bell fixed
- Removed the second `btnNotifications.setOnClickListener(...)` in
  `HomeActivity.onCreate()` - the `// Stub for now` lambda that overwrote the
  real listener registered a few lines above
- The header bell now opens `NotificationsActivity`

**Why:** The bell was dead: `NotificationsActivity` was unreachable from the UI,
which is why it could not be screenshotted in Session 9.

#### 2. Unit test suite repaired
- Deleted `app/src/test/java/com/runovav6/RoutineEngineTest.java`. It tested the
  V5 `RoutineEngine` API (fixed 5-task training day scaled by a weekly-minutes
  band). That class no longer exists, so the file produced 95 compile errors and
  `:app:testDebugUnitTest` could never pass
- Added `app/src/test/java/com/runovav6/LevelPlanTest.java`, which verifies the
  approved plan implemented by `LevelPlan` instead:
  - weekday task counts per level (Beginner 3/4/3/4/3/4, Intermediate 3/4/5/3/4/5,
    Pro 4/5/4/5/4/5)
  - a 2-task Sunday recovery day for every level, with no running-type task
  - daily totals within the per-level cap (60 / 90 / 120 minutes)
  - warm-up first and cool-down last on training days, 10 / 15 / 15 minutes
  - every task has a name, a positive duration and a whitelisted type
  - no nutrition / food / calorie / water / hydration / sleep / meditation content
  - unknown level falls back to Beginner; output is deterministic
  - `DateUtils.getWeekday()` maps a real Monday-to-Sunday week correctly and
    falls back to Monday for an unparsable date

**Why:** The invariants the V5 test protected are still required; only the API
changed. Testing `LevelPlan` keeps the same guarantees against the plan that
actually ships.

**Verification:**
- `./gradlew :app:testDebugUnitTest` -> BUILD SUCCESSFUL, `LevelPlanTest`
  `tests="9" failures="0" errors="0"` plus `ExampleUnitTest` (1)
- `./gradlew :app:assembleDebug` -> BUILD SUCCESSFUL
- The new suite was mutation-tested: temporarily adding a third Sunday task to
  the Pro plan failed exactly the two tests that should fail
  (`everyLevelHasATwoTaskRecoveryDayOnSunday`,
  `everyCalendarDayMapsToTheExpectedTemplateSize`). The mutation was reverted and
  the suite returned to green
- `TaskGenerator` (thin wrapper over `DBHelper`), `LevelPlan`, `DateUtils` and the
  models are plain Java, so the suite is host-testable with no new dependency

#### 3. Onboarding screens verified
- Backed up `runova.db`, cleared app data, and walked the real flow end to end:
  start-up popup -> Signup -> Questionnaire -> Home
- Captured Signup and Questionnaire: both span [0,0]-[1080,2400] with the
  gradient continuous to all four edges. Neither screen shows the bottom
  navigation bar, which matches the design (the bar exists on Home, Target,
  Analytics and Profile only)
- Captured Notifications after the bell fix: same edge-to-edge chrome, the bar
  sits flush on the bottom edge

**Why:** Session 9 could not capture these three screens - Signup and
Questionnaire need a cleared profile and the bell was dead.

#### 4. Device data backed up and restored
- Before clearing anything, the database was copied out and inspected: 1 profile
  row (loki, Beginner, General Running, 28-day level started 2026-09-26) and 4
  task rows for 2026-09-26 (Warm-up and Easy Run `Finished`, Running Drills and
  Cool-down `Pending`)
- After verification the original file was copied back and re-inspected row by
  row, and Home was re-captured showing the original Finished / Finished /
  Pending / Pending states

**Why:** No functional result was deleted by the verification. The cleared state
existed only for the duration of the onboarding walkthrough.

**Files Created/Modified:**
```
app/src/test/java/com/runovav6/LevelPlanTest.java (new)
app/src/test/java/com/runovav6/RoutineEngineTest.java (deleted - tested a removed V5 class)
app/src/main/java/com/runova/HomeActivity.java (stub listener removed)
```

**Key Decisions:**
- Deleted the obsolete test instead of keeping it disabled, because it cannot
  compile against the current code; the invariants it protected are carried over
  into `LevelPlanTest`
- Chose `LevelPlan` as the test seam rather than `TaskGenerator`, because
  `TaskGenerator` needs an Android `SQLiteDatabase` and the project has no
  Robolectric dependency, which must not be added without approval
- Ran the whole onboarding against a temporary cleared profile rather than
  adding a debug flag or a test-only entry point
- Confirmed with the project owner before clearing device data; the database was
  copied out first and restored afterwards

**Result:** All 8 activities now verified edge to edge, `testDebugUnitTest` passes
for the first time, and the notification bell works.

---


## Session 11: Design Consistency, Responsive Layouts, Chart Fixes & Timer Rework (2026-09-28)

### Phase: UI Consistency + Adaptive Layouts + Device Verification

**Duration:** ~4-5 hours

**Objective:** Apply the reference design across every screen (cards, colors,
typography, icons, terminology), add Week/Month/Year analytics charts, remove
all touch/ripple effects, make layouts adaptive for phones/foldables/tablets,
and verify everything on a real emulator - ending with a full Timer rework
giving the user complete control of the countdown.

**Changes Made:**

#### 1. Header, navigation and touch targets
- Bell added to Analytics, Profile and Target headers (wired to
  `NotificationsActivity`); all bell/settings icons 24dp visual inside 48dp
  touch targets (`touch_target`, 12dp padding, 8dp marginStart)
- `fillViewport=true` on all four scrolling screens
- Nav bar buttons 40dp -> 48dp with 6dp padding, bar padding 25dp/21dp (fixed
  90dp height); `SettingsActivity.setupNavigation()` wired (home finishes,
  target/analytics/profile navigate)
- Invisible `settingsb` removed from the Profile header (layout + Java)
- Back buttons on Notifications/Settings normalized to 48dp targets

**Why:** V8 spec + user decision: identical header/footers on all four main
screens, no dead controls, 48dp minimum touch areas.

#### 2. Dashboard restructure (Home)
- Rebuilt `activity_home.xml`: date card -> `28 Days Progress` (full-width bar)
  -> `Today's Status` (Completed green / Pending amber / Missed red columns) ->
  `Today's Target` (light rows + status chips + `View all`)
- New `helpers/StatsHelper.java` shared by Home/Analytics/Profile for
  `sessions()` and `streak()`; streak fixed so an unfinished *today* no longer
  breaks it (spec RULES.md:196-212)
- `item_task_card.xml` flattened; rows open Target on click

**Why:** User-driven info architecture; streak bug was spec-violating.

#### 3. Target screen + terminology
- Title `Today's Target`; sections `Pending Targets` / `Completed Targets` /
  new `Missed Targets` (collapsible, past unfinished rows displayed as Missed -
  DB keeps `Finished`/`Pending`, no migration)
- Summary `X of Y completed`; rows got status chips; active row
  RESUME+COMPLETE; Finished/Missed rows hide action buttons
- Display-only term pass: `Finished`->`Completed` (labels, chart legend), timer
  `DONE`->`COMPLETE`; DB values untouched per spec lock

**Why:** User terminology decision; spec forbids changing stored status values.

#### 4. Analytics Week/Month/Year charts
- Tab strip (`bg_chip` selected) driving `setRange()`/`loadChart()`;
  `weekEntries()` daily bars, `monthEntries()` W1-W5 buckets, `yearEntries()`
  Jan-Dec % of fully-finished days; caption "shows all activity, even across
  level changes"; 12sp white axis/legend/values
- Status Counts card `input_bg`->`card_bg` with colored counts, reordered
  Completed|Pending|Missed; progress bar tinted `status_success`
- **Two bugs found during on-device verification and fixed:**
  1. `Calendar.set(DAY_OF_WEEK, MONDAY)` on a Sunday-first locale resolved to
     *next* Monday, so the week window excluded today and the chart always read
     0 -> replaced with an add-based offset to Monday
  2. Zero-data charts rendered a -1.2..1.2 Y axis -> `setAxisMinimum(0f)`

**Why:** Requirement was three ranges; the two defects only reproduced on a
real device with real data.

#### 5. Profile, Settings, Notifications, dialogs
- Profile: view/edit mode toggle, buttons (`EDIT PROFILE`, `About RUNOVA`)
  wrapped in a card matching My Stats, V8 level-change dialog text, wipes
  `tasks`+`notifications` on level change
- Settings trimmed to approved scope: Reminders switch (pref-only, no reminder
  backend exists), Dark Mode switch, About card; nav bar wired
- Notifications header back+title only (removed settings icon)
- New `Theme.RUNOVA.AlertDialog` (blue surface, white text/buttons) used by all
  confirm dialogs; titles normalized to 24sp; `item_notification` light row
  (card radius, 2dp, 15sp dark text); empty states 14sp `#99FFFFFF`
- **All ripple/touch effects removed** at theme level: 8 transparent attributes
  in `values/themes.xml` (selectableItemBackground x3, rippleColor,
  colorControlHighlight, list highlights) + `Theme.RUNOVA.Button` via
  `materialButtonStyle` (no ripple, no lift). EditText caret/underline, switch
  and spinner mechanics kept.

**Why:** User decisions: settings scope cut, zero visual touch feedback, one
consistent dialog style.

#### 6. Responsive / adaptive (phones, foldables, tablets)
- `item_task_control.xml` two-row card: name+chip row, duration+buttons row;
  name/duration/greeting ellipsize with maxLines=1; 100dp minWidth dropped
- `item_notification` action buttons weight-based (0dp + weight1, 12sp,
  maxLines 2)
- `activity_timer.xml` root -> ScrollView + fillViewport (only screen without
  one)
- New `values-sw600dp/dimens.xml` (spacing only: padding/gaps)
- `HomeActivity` + `TargetActivity` GridLayoutManager span=2 when
  `screenWidthDp >= 600` (each Target list gets its own manager instance)
- Verified via `wm size/density` overrides: 320dp phone, 800dp tablet, landscape

**Why:** Spec invariant - only spacing scales; colors/radius/fonts/components
identical everywhere, no duplicated layouts, no manifest changes.

#### 7. Timer rework (user-controlled countdown)
- Single `btnPrimary` with three states on the SAME view (zero relayout,
  proven by identical uiautomator bounds across state changes):
  START blue -> PAUSE red (`status_missed_icon`) -> RESUME green
  (`status_success`); removed the `btnResume` visibility swap that caused
  re-layout
- Timer no longer auto-starts on entry: READY state shows full duration with
  `Ready to start` caption; counting begins only on START
- New RESET button (secondary style `bg_btn_secondary`) with confirm dialog
  ("Reset timer" / "The timer will start again from the beginning." /
  CANCEL|RESET); on confirm -> `clearTimer()` + full duration + READY (Option A,
  no auto-restart)
- `← BACK TO TASKS` -> 24dp `ic_chevron_left` + `BACK TO TARGET`
- State caption under digits (Ready to start / Running / Paused); COMPLETE flow
  unchanged

**Why:** User requirements: fixed button positions, red pause -> green resume,
full manual control of start, reset with popup, correct back label.

#### 8. On-device verification pass
- Pixel_6a emulator (headless swiftshader), APK installed, walked every flow
  with uiautomator dumps + screencaps into `/tmp/runova_shots/`: Signup, Home,
  Target (complete flow, sections), Analytics (3 tabs), Profile (view/edit),
  Settings, Notifications, Timer (portrait/landscape), 320dp, 800dp tablet
- Noted environment quirks (not app bugs): emulator occasionally drops a tap
  and screencap can return one stale frame - re-tap/re-shoot resolves

**Files Created/Modified:**
```
app/src/main/res/layout/activity_home.xml (rebuilt)
app/src/main/res/layout/activity_target.xml
app/src/main/res/layout/activity_analytics.xml
app/src/main/res/layout/activity_profile.xml
app/src/main/res/layout/activity_settings.xml
app/src/main/res/layout/activity_notifications.xml
app/src/main/res/layout/activity_timer.xml (rebuilt)
app/src/main/res/layout/item_task_card.xml (rebuilt)
app/src/main/res/layout/item_task_control.xml (two-row)
app/src/main/res/layout/item_notification.xml
app/src/main/res/layout/layout_navigation_bar.xml
app/src/main/res/values/themes.xml (ripple removal, button style, dialog theme)
app/src/main/res/values/colors.xml (status_pending)
app/src/main/res/values-sw600dp/dimens.xml (new)
app/src/main/res/drawable/bg_btn_secondary.xml (new)
app/src/main/java/com/runova/HomeActivity.java
app/src/main/java/com/runova/TargetActivity.java
app/src/main/java/com/runova/AnalyticsActivity.java
app/src/main/java/com/runova/ProfileActivity.java
app/src/main/java/com/runova/NotificationsActivity.java
app/src/main/java/com/runova/SettingsActivity.java (new this batch, nav wired)
app/src/main/java/com/runova/TimerActivity.java (3-state rework)
app/src/main/java/com/runova/helpers/StatsHelper.java (new)
```

**Key Decisions:**
- Missed is derived (past + not Finished), never written to the DB - spec
  locks status values
- Ripple removal done in the theme instead of per-view so every current and
  future view inherits it; function (caret, switch, spinner) preserved
- Responsive work scales spacing only; grid span2 only where lists exist
  (Home today list, three Target lists)
- Timer state machine uses one button view with text+tint changes rather than
  swapping views - relayout was the reported defect
- Reset returns to READY (Option A) so the user retains full control

---

## Session 12: Offline Audit, Concurrency Fix, Timer Background Expiration & Spec Alignment (2026-09-28)

### Phase: Quality Assurance + Defect Resolution + Spec Cleanliness

**Duration:** ~1 hour

**Objective:** Audit the codebase for offline compliance and requirement conflicts, resolve static thread concurrency hazards in DateUtils, fix background timer expiration handling, eliminate navigation back-stack leakage in Settings, clean up out-of-scope legacy drawables, and add concurrency unit tests.

**Changes Made:**

#### 1. Concurrency Fix in DateUtils
- Replaced non-thread-safe static `SimpleDateFormat ISO_DATE` in `DateUtils.java` with `ThreadLocal<SimpleDateFormat>`.
- Updated all accessor methods (`ageFrom`, `getWeekday`, `today`, `isValidDate`, `isFuture`, `daysBetween`) to use `ISO_DATE.get()`.

**Why:** `LoadingActivity` executes `TaskGenerator.generateDailyTasks()` on a background thread while UI components access `DateUtils` on the Main thread. Shared mutable `SimpleDateFormat` state risked parsing corruption and `NumberFormatException`.

#### 2. Background Timer Expiration in TimerController
- Enhanced `TimerController.readState()`: when `activeTaskId` exists, `activeTaskPaused` is false, and `remaining <= 0` (timer expired while app was minimized/killed), automatically marks the task as `Finished` in SQLite `tasks` table and clears active timer columns in `profile`.

**Why:** Previously, backgrounded timer expiration returned `null` without updating the database, leaving the completed task in `Pending` and profile in a stale state.

#### 3. Settings Navigation Back-Stack Normalization
- Added `finish()` to `targetb`, `analyticsb`, and `profileb` listeners in `SettingsActivity.java`.

**Why:** Tapping bottom navigation tabs from Settings previously left `SettingsActivity` alive in the activity back-stack.

#### 4. Legacy Asset Deletion
- Deleted `app/src/main/res/drawable/water.xml`, `eat.xml`, `meditation.xml`, and `ic_sleep.xml`.

**Why:** Project charter explicitly forbids nutrition, hydration, sleep, and meditation features. Deleting unreferenced draft icons enforces strict spec adherence.

#### 5. Unit Testing
- Added `DateUtilsTest.java` verifying date logic and executing 10 concurrent threads over 50 iterations against `DateUtils` methods with zero race conditions or errors.
- Verified all 12 unit tests pass (`LevelPlanTest` 9, `DateUtilsTest` 2, `ExampleUnitTest` 1).

**Files Created/Modified:**
```
app/src/main/java/com/runova/helpers/DateUtils.java (modified)
app/src/main/java/com/runova/controllers/TimerController.java (modified)
app/src/main/java/com/runova/SettingsActivity.java (modified)
app/src/test/java/com/runovav6/DateUtilsTest.java (new)
app/src/main/res/drawable/water.xml (deleted)
app/src/main/res/drawable/eat.xml (deleted)
app/src/main/res/drawable/meditation.xml (deleted)
app/src/main/res/drawable/ic_sleep.xml (deleted)
```

**Verification:**
- `./gradlew test` -> BUILD SUCCESSFUL (12 tests pass)
- `./gradlew assembleDebug` -> BUILD SUCCESSFUL

---

## Session 13: Welcome Dialog, Official Documentation & Level-Up System Rework (2026-09-28)

### Phase: Onboarding Polish + Documentation + Level-Up UX Overhaul

**Duration:** ~2 hours

**Objective:** Add a first-time-only welcome/disclaimer dialog before Sign Up, export the official project documentation as a Word file for panel presentation, then rework the level-up system: shared dialog across Home and Notifications, polished congratulation wording including a dedicated Pro-level branch, button-free notification rows, data continuation on level-up (wipe reserved for Profile edit only), and fix the stale level-up offer defect.

**Changes Made:**

#### 1. First-Launch Welcome Dialog
- Created `helpers/AboutDialog.java` as the single owner of the welcome/disclaimer text (exact copy of the former About RUNOVA popup: "Before you start" + runner guidance + health disclaimer), `setCancelable(false)` with an OK button.
- `SignupActivity.onCreate()`: shows the dialog on the no-profile path (fresh install or onboarding not completed). Profile-exists path returns before it, so returning users never see it.
- `ProfileActivity.showAboutPopup()` now delegates to `AboutDialog.show()` (zero text duplication).

**Why:** Spec/PRD requires a welcome message before the user inputs sign-up data, shown only to first-time users. Gate = `!profileExists()` — covers both fresh install and "no data entered" conditions with one check; no SharedPreferences flag (a once-per-install flag would suppress the dialog when data is still absent).

#### 2. Official Documentation Export
- Generated `RUNOVA_Official_Documentation.docx` (repo root): 14 sections covering concept, tools, architecture, principles, database, training engine, features, navigation, design system, development history, QA, scope, inventory, conclusion; 12 bordered tables, heading styles, black-on-white, no icons/emoji.
- Pipeline: clean HTML -> macOS `textutil` (tables flattened, rejected) -> Microsoft Word AppleScript conversion (real tables preserved).

**Why:** Panel deliverable required as a single .doc/.docx file, text-only.

#### 3. Level-Up Test Data Runs (temporary, deleted)
- Seeded the SQLite DB via `run-as` + host `sqlite3` (backup -> seed -> push) to trigger day-28/85% conditions without waiting; scenarios: move-up 100%, repeat, fail 71%, bell path, threshold edge 24 vs 23 days. All passed.
- **Defect found:** `setOnDismissListener` in the old Home dialog fired after button clicks too, re-inserting an unresolved `LEVEL_UP_OFFER` after the choice was already applied (false bell notification; suppressed next cycle's Home prompt).

**Why:** Proved the level-up function end-to-end before reworking it. All seeds/backups destroyed on "del test"; real profile restored and verified.

#### 4. Shared LevelUpDialog + Wording Rework
- Created `helpers/LevelUpDialog.java`: one dialog used by Home and Notifications.
  - Pass (Beginner/Intermediate): title "Congratulations!", message congratulating readiness for the next level, buttons **Move to Next Level** / **Continue Current Level** (the word "Repeat" removed everywhere).
  - Pass (Pro): dedicated branch - grateful message stating Pro is the highest level, no next level exists, invitation to keep streak and keep using the app, single **Continue Current Level** button.
  - Fail (any level): title "Level Cycle Complete", "You finished X of 28 days this cycle. You need 24 days to move up. Continue with [level] to start a new cycle?", single Continue button.
  - Dismiss guard (`choiceApplied` flag): offer row created only when dismissed without a choice - fixes the Session 13 defect (3).
  - Notification labels: `Congratulations! level up available` / `Congratulations! Pro level complete` / `Level cycle complete`.
- `HomeActivity`: inline dialog (~40 lines) + `getNextLevel` deleted -> `LevelUpDialog.show(this, this::recreate)`; added `onResume()` refresh of level progress + task list + status counts (fixes stale "Day 28 of 28" and stale counts when returning from Notifications or Target).

#### 5. Notification Rows: Label Only, Tap Opens Dialog
- `item_notification.xml`: removed the two action buttons (entire `layoutActions` block) - rows show only the short label.
- `NotificationsActivity`: button logic, `getNextLevel`, and controller field deleted; whole row clickable -> `LevelUpDialog.show(...)`; list refreshes after a choice (offer resolves -> empty state). Applies uniformly to all levels and any future notification type: rows never hold buttons; buttons live only in the dialog opened by tapping.

#### 6. Data Continuation Rule
- Removed `db.delete(TABLE_TASKS, ...)` from `LevelCycleController.applyLevelChoice` (one line). Level-up/continue now updates `level` + `levelStartDate` only: task history is preserved, so weekly/monthly/yearly analytics stay continuous across a level change. The only wipe authority left is the Profile level edit (`ProfileActivity.saveLevelChange` + confirmation dialog), unchanged.

**Files Created/Modified:**
```
app/src/main/java/com/runova/helpers/AboutDialog.java (new)
app/src/main/java/com/runova/helpers/LevelUpDialog.java (new)
app/src/main/java/com/runova/SignupActivity.java (modified)
app/src/main/java/com/runova/ProfileActivity.java (modified)
app/src/main/java/com/runova/HomeActivity.java (modified)
app/src/main/java/com/runova/NotificationsActivity.java (modified)
app/src/main/java/com/runova/controllers/LevelCycleController.java (modified)
app/src/main/res/layout/item_notification.xml (modified)
RUNOVA_Official_Documentation.docx (new, repo root)
```

**Why:** User-driven UX decisions: welcome popup before onboarding for first-timers only; congratulation-style level-up flow with proceed/continue wording; Pro users congratulated without a fake next level; notifications tappable instead of inline actions; level-up must never destroy usage history while Profile edit remains the explicit wipe path.

**Key Decisions:**
- Single gate for the welcome dialog: `!profileExists()` (fresh install OR no data both covered; returning users bypass SignupActivity entirely).
- One shared dialog class for Home + Notifications = one wording source, consistent across all levels.
- Pro branch inside the same dialog (no separate Pro class) - consistency by construction.
- Level-up never wipes tasks; Profile level change is the sole authorized wipe (confirm dialog retained).
- Dismiss guard instead of restructuring listener timing (4-line fix, root cause addressed).
- Test data strictly temporary: backup/seed/push/restore cycle, artifacts deleted on "del test".

**Verification:**
- `./gradlew assembleDebug test` -> BUILD SUCCESSFUL (12 unit tests, 0 failures)
- On device: welcome dialog before Sign Up (fresh install), absent for returning users; all three level-up dialogs screenshot-verified; inbox rows button-free with correct labels; row tap opens identical dialog; move-up keeps history (27-28 past task rows intact); Pro continue resets cycle only; offer resolved after every choice (no stale rows); Home shows "Day 1 of 28" after choice
- Test seeds and screenshots cleaned; real profile restored (`loki | Beginner | 2026-09-28`, 3 tasks, 0 notifications)

---

## Session 14: Training Content Screen & Generic Exercise Timer (2026-10-02)

### Phase: Task Content Flow + Per-Exercise Timer + Progress Persistence

**Duration:** ~2 hours

**Objective:** Replace the whole-task timer flow with a training content screen: task row/START opens a content screen listing that task's exercises (name, description, duration, START), each exercise runs on a generic timer template (name + seconds), completing every exercise is the only signal that marks the task Finished, and exercise progress persists across navigation so finished cards never revert to Pending.

**Changes Made:**

#### 1. Exercise Model + Content Data
- Created `models/Exercise.java` (`name`, `description`, `seconds`).
- `training/TrainingContentLibrary.java`: append-only `getExercises(taskType)` for all 15 task types plus `getInstruction(type, event)`; existing 108 lines untouched.

#### 2. Training Content Screen
- Created `activity_training_content.xml` + `TrainingContentActivity.java`: back header, title/subtitle, instruction card, exercise card list. Card layout `item_exercise_card.xml` = name, description, "Duration: X seconds", Done status chip, START button.
- Removed during review per developer: TASK TIMER button (whole-task timer concept dropped), inline card countdown (redundant with timer screen), COMPLETE button (task finishes only when all exercise timers end).

#### 3. Generic Timer Template
- `TimerActivity.java` rewritten: extras `TASK_NAME` + `DURATION_SECONDS` (no DB, no TimerController, no taskId). Local `CountDownTimer`; states Ready -> Running -> Paused -> Completed (00:00, primary shows DONE disabled); `btnReset`/`btnDone`/`COMPLETE` removed from `activity_timer.xml`. Result contract: `RESULT_OK` + `EXTRA_COMPLETED` on natural finish, `RESULT_CANCELED` on back-before-zero.
- `controllers/TimerController.java` deleted (zero references after rework); dead DB columns `activeTaskId`/`activeTaskEndTime`/`activeTaskPaused` left in place (no migration per spec).

#### 4. Navigation & Dead-Logic Removal
- `TargetActivity`: START button and row click both open the content screen; whole active-task mechanism removed (`timerController`, `activeTaskId`, RESUME/WAIT/COMPLETE branches, `completeTask`); pending card = START only, finished/missed hide START, COMPLETE stays GONE.

#### 5. Progress Persistence & Task Completion
- `TrainingContentActivity`: `SharedPreferences` file `exercise_progress`, key `task_<taskId>` = CSV of finished card indices; saved on each exercise completion, loaded on inflate (chip Done + DONE disabled), cleared only when the task transitions to Finished. In-flight index held for the Activity Result API (`registerForActivityResult`), cancel result leaves card pending.
- All cards done -> `UPDATE tasks SET status='Finished'` (logic moved from TargetActivity), toast, auto-back to Target where counts update. Finished task reopened -> all cards pre-marked Done.

**Files Created/Modified:**
```
app/src/main/java/com/runova/models/Exercise.java (new)
app/src/main/java/com/runova/TrainingContentActivity.java (new)
app/src/main/java/com/runova/training/TrainingContentLibrary.java (modified)
app/src/main/java/com/runova/TimerActivity.java (rewritten)
app/src/main/java/com/runova/TargetActivity.java (modified)
app/src/main/java/com/runova/controllers/TimerController.java (deleted)
app/src/main/res/layout/activity_training_content.xml (new)
app/src/main/res/layout/item_exercise_card.xml (new)
app/src/main/res/layout/activity_timer.xml (modified)
```

**Why:** Developer decisions in plan review: task timer replaced by per-exercise timers (single reusable timer template); timer completion is the sole task-finish signal; no manual COMPLETE button; progress must persist (bug: finished cards reverted to Pending on re-entry).

**Key Decisions:**
- Progress store = SharedPreferences CSV (no new DB table; tasks table contract unchanged).
- One generic timer Activity driven by name + seconds serves every exercise.
- Completion delivered via Activity Result, not global state.
- TimerController deleted only after grep proved zero references (DB columns untouched).

**Verification:**
- `./gradlew assembleDebug test` -> green (12 unit tests, 0 failures)
- Device walk: Target START -> content (5 cards, no TASK TIMER/inline timer/COMPLETE) -> RUN/PAUSE/RESUME -> 00:00 Completed DONE-disabled -> back -> card Done chip; exit + re-enter content -> Done persisted; remaining 4 exercises run -> auto back to Target, `tasks.status='Finished'`, prefs key cleared, badges 2 pending / 1 completed, summary "1 of 3 completed"; reopened finished task -> all 5 cards Done; 0 FATAL. Shots: `/tmp/runova_shots/ui10..ui21`.

---

## Session 15: Profile — Event/Goal Removed, Level Moved into My Stats (2026-10-02)

**What changed:**
- View mode: "Training Configuration" card (Event/Level/Goal rows) deleted from `activity_profile.xml`; Level row added inside "My Stats" card as its own container (`bg_summary_card` box, padding 12dp) above the stat grid, separated from Height/Weight etc.
- Edit mode: `spinnerEvent` and `spinnerGoal` deleted from the edit "Training Configuration" card; card keeps Height, Weight, `spinnerLevel`.
- `ProfileActivity`: removed event/goal spinner + `tvCfg*` fields, adapters, load bindings, and `PROFILE_EVENT`/`PROFILE_GOAL` writes in both save paths; new `tvStatLevel` bound in `loadProfile()`.
- Event/Goal remain in DB + questionnaire + Home wording (set once at onboarding, no longer editable in Profile).

**Files Created/Modified:**
```
app/src/main/res/layout/activity_profile.xml (modified)
app/src/main/java/com/runova/ProfileActivity.java (modified)
```

**Why:** Developer instruction — fully remove event and goal spinners and their display card; level display belongs in My Stats with its own container.

**Key Decisions:**
- Spec demo step "Profile → edit Event (no wipe)" (Agent_Specs demo script) is now dead — developer-approved deviation (LEVEL 1 instruction over spec).
- Level spinner kept → level-change wipe flow intact; `PROFILE_LEVEL_START` write kept.
- No DB/schema change; `event_options`/`goal_options` arrays left in place.

**Verification:**
- `./gradlew assembleDebug test` -> green.
- Device: Profile view shows "Level / Beginner" box inside My Stats (y709, above Height y825), Training Configuration card gone; edit mode has exactly 2 spinners (gender + level), no Event/Goal; CANCEL exits without save; 0 com.runova FATAL. Shots: `/tmp/runova_shots/profile_stats.png`, `profile_edit.png`.

---

## Current State (2026-10-02 11:27 UTC)

### All Core Phases Complete ✅

**What Works:**
- ✅ Full onboarding flow (Welcome → Signup → Questionnaire)
- ✅ Task generation (lazy, deterministic, from static templates)
- ✅ Home screen (greeting, date, tasks, level progress, bell icon)
- ✅ Target screen (task content flow, per-exercise timers, task completion)
- ✅ Analytics (level progress, streak, status counts, weekly chart)
- ✅ Profile editing (validation, level change with confirmation); Level shown in My Stats, Event/Goal no longer edited in Profile (Session 15)
- ✅ Level cycle (28-day check, dialog, notifications inbox)
- ✅ Bottom navigation on all 4 screens (Home/Target/Analytics/Profile) - fixed to the bottom edge, no transition
- ✅ Edge-to-edge on all 8 screens (Session 9 + 10), verified screen by screen
- ✅ Notification bell reaches the inbox (Session 10)
- ✅ Unit tests pass: 10 tests (Session 10)
- ✅ Offline operation (no network calls, pure SQLite)
- ✅ Database persistence (3 tables: profile, tasks, notifications)
- ✅ Consistent design system: card/text/dialog/touch-target rules applied to all screens (Session 11)
- ✅ Bell + settings icons on all 4 main screens, 48dp touch targets, 48dp nav buttons (Session 11)
- ✅ Home dashboard: 28 Days Progress + Today's Status trio + Today's Target (Session 11)
- ✅ Target: Pending/Completed/Missed sections, status chips, display-only Completed terminology (Session 11)
- ✅ Analytics Week/Month/Year charts with working week window + non-negative axis (Session 11, 2 bugs fixed)
- ✅ No ripple/touch feedback anywhere (theme-level removal) (Session 11)
- ✅ Adaptive layouts verified at 320dp phone / landscape / 800dp tablet (spacing-only scaling, grid span2) (Session 11)
- ✅ Training content screen per task: exercise cards with Done chips, progress persists across navigation (Session 14)
- ✅ Generic timer: START/PAUSE/RESUME, 00:00 auto-complete, DONE disabled, back cancels (Session 14)
- ✅ Task marked Finished only when all exercise timers complete (Session 14)
- ✅ Full on-device verification of every screen (Session 11, Session 14)
- ✅ First-launch welcome/disclaimer dialog before Sign Up, first-time users only (Session 13)
- ✅ All 11 Activities functional
- ✅ Official documentation exported to RUNOVA_Official_Documentation.docx, 14 sections (Session 13)
- ✅ Level-up system reworked: shared dialog, congratulation wording, Pro branch with single Continue, polished notification labels (Session 13)
- ✅ Notification rows: label only + tap-to-dialog, no inline buttons, all levels (Session 13)
- ✅ Level-up keeps task history (wipe reserved for Profile edit only) (Session 13)
- ✅ Stale level-up offer defect fixed (dismiss guard) (Session 13)
- ✅ All 10 Activities functional

**Build Status:** ✅ BUILD SUCCESSFUL  
**APK Location:** `/Users/loki/RUNOVAV62/app/build/outputs/apk/debug/app-debug.apk`  
**Gradle Version:** 9.5.0  
**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 34 (Android 14)

**Architecture Summary:**
- **28 Java files total**
- **11 Activities:** SignupActivity, QuestionnaireActivity, LoadingActivity, HomeActivity, TargetActivity, AnalyticsActivity, ProfileActivity, NotificationsActivity, SettingsActivity, TimerActivity, TrainingContentActivity
- **2 Controllers:** TaskGenerator, LevelCycleController
- **6 Models:** Profile, Task, Notification, DayTemplate, TaskTemplate, Exercise
- **6 Helpers:** DateUtils, ValidationHelper, WindowHelper, StatsHelper, AboutDialog, LevelUpDialog
- **2 Training classes:** LevelPlan, TrainingContentLibrary
- **1 Database helper:** DBHelper
- **Pattern:** Simple MVC, no over-engineering

---

## Known Issues

**Found during the Session 9 UI review:**

1. ~~**Home notification bell does nothing.**~~ **FIXED in Session 10.** The stub
   listener was removed from `HomeActivity.onCreate()`; the bell now opens
   `NotificationsActivity` (verified on device).
2. ~~**Unit tests do not compile.**~~ **FIXED in Session 10.** The obsolete
   `RoutineEngineTest.java` was replaced by `LevelPlanTest.java`, which tests the
   approval-backed `LevelPlan`; `:app:testDebugUnitTest` now passes (10 tests).
3. **Dead style.** `res/values-night/themes.xml` defines `Base.Theme.RUNOVAV6`,
   which nothing references (the app uses `Base.Theme.RUNOVA`). Harmless; left
   in place, still open.
4. ~~**Week chart always 0.**~~ **FIXED in Session 11.** `set(DAY_OF_WEEK,
   MONDAY)` resolved to next Monday on a Sunday-first locale; replaced with an
   add-based offset. Negative Y axis on zero data also fixed (`setAxisMinimum(0)`).
5. **`lintDebug` fails environmentally** (AGP requires JDK 17, environment has
   JDK 11/26 only) - pre-existing, not a code defect; `assembleDebug` + `test` pass.
6. **Settings Dark Mode switch is preference-only** - no light theme consumer
   verified on device (accepted scope, Session 11).

**Potential Edge Cases to Test:**
- Timer behavior during low battery mode
- Task generation on leap year dates
- Profile edit during active timer
- Rotation during timer countdown
- Multiple rapid level changes

---

## Future Enhancements

**Nice-to-Have (Not Critical):**

1. **Task Instructions Display**
   - Show `TrainingContentLibrary.getInstruction()` on Target screen
   - Already implemented, just needs UI wire-up
   - Small TextView below task name

2. **OS-Level Reminders**
   - ReminderHelper class (not created, marked optional in spec)
   - Android AlarmManager integration
   - Notification at configured time to open app

3. **Missed Task Auto-Update**
   - Background job to mark yesterday's Pending tasks as Missed
   - Currently requires user to open app

4. **Export Progress Data**
   - CSV export of completed tasks
   - Share via email/cloud

5. **Dark Mode Support**
   - Theme switching in Profile
   - Already uses Material3 which supports theming

**Not Planned (Spec Explicitly Excludes):**
- GPS tracking
- Heart rate monitoring
- Pace/speed tracking
- Social features
- Cloud sync
- In-app purchases

---

## Testing Results

### Manual Testing Checklist

**Session 1-7 (2026-09-26):**

- [x] **First Launch Flow**
  - Welcome screen appears
  - START button shows popup with disclaimer
  - Popup navigates to Signup
  - Back button does not return to Welcome (finish() works)

- [x] **Onboarding**
  - Signup validates all fields
  - Date picker works for DOB
  - Age 13-100 enforced
  - Questionnaire preselects Beginner
  - Profile saved to database (id=1)
  - Navigation to Home after save

- [x] **Home Screen**
  - Greeting shows firstName
  - Date displays correctly
  - Level progress shows "Day 1 of 28" on first day
  - Tasks generated on load (lazy generation)
  - Bell icon present (wired in Session 7)
  - Bottom nav buttons work

- [x] **Target Screen**
  - Tasks list appears
  - START button enables when no timer active
  - Timer counts down MM:SS format
  - Other START buttons show "WAIT" during active timer
  - COMPLETE button updates task status
  - Screen stays on during timer
  - Finished tasks show "DONE" (disabled)

- [x] **Analytics Screen**
  - Level progress calculates correctly
  - Streak shows consecutive Finished days
  - Status counts display (Finished/Missed/Pending)
  - Chart renders with MPAndroidChart
  - Weekly bars show correct counts

- [x] **Profile Screen**
  - All fields load from database
  - Age shows as read-only derived value
  - Validation matches Signup/Questionnaire
  - Level change shows confirmation dialog
  - Level change wipes tasks and resets cycle
  - About button reopens disclaimer popup

- [x] **Level Cycle**
  - Day 28 triggers level-up dialog
  - Pass rate calculated correctly (≥85% = pass)
  - Dialog dismissed creates notification
  - Notifications screen shows offer
  - Both options call same handler
  - Tasks wiped on level choice

- [x] **Build & Compilation**
  - Gradle assembleDebug succeeds
  - No compilation errors
  - APK generated successfully

**Automated Testing:** None (manual QA only)

---

## Notes for Future AI Agents

### Before Making Changes:

1. **Read spec first:** `/Users/loki/RUNOVAV62/runova/RUNOVA_Project_Overview:Agent_Specs.md`
2. **Check approved decisions:** Section 0.19 (no new tables, no new activities without approval, etc.)
3. **Understand lazy generation:** Tasks generated one day at a time, never in advance, never regenerated
4. **Single timer rule:** Only one active timer app-wide, state in profile table
5. **Level change = destructive:** Wipes all tasks, resets levelStartDate to today
6. **Day status is derived:** Not stored, calculated (all tasks Finished = day Finished)
7. **Read this history file FIRST:** Understand what exists and why before modifying

### Common Patterns:

- **Database access:** Use DBHelper, always close cursors in finally block
- **Navigation:** Intent-based, bottom nav on Home/Target/Analytics/Profile
- **Validation:** Inline in activities (ValidationHelper created but unused)
- **Colors:** From colors.xml, no hardcoded hex in Java
- **Design system:** Blue cards (#0174DB), 20dp radius, white text, gradient background
- **Strings:** Always use strings.xml, never hardcode English text
- **Date format:** ISO 8601 (YYYY-MM-DD) in database, SimpleDateFormat for display

### Code Style:

- **No over-engineering:** Simple MVC, no unnecessary abstractions
- **No external services:** Pure offline, no network calls
- **No libraries unless needed:** Spec approves AndroidX, Material3, MPAndroidChart only
- **Explicit is better:** Clear variable names, no clever tricks
- **Fail fast:** Validate early, show Toast on error

### Database Schema:

```sql
-- profile (single row, id always = 1)
id INTEGER PRIMARY KEY
firstName TEXT NOT NULL
lastName TEXT NOT NULL
dateOfBirth TEXT NOT NULL
gender TEXT NOT NULL
height REAL NOT NULL
weight REAL NOT NULL
event TEXT NOT NULL
level TEXT NOT NULL
goal TEXT NOT NULL
levelStartDate TEXT NOT NULL
seenIntro INTEGER NOT NULL DEFAULT 0
activeTaskId INTEGER (nullable)
activeTaskEndTime INTEGER (nullable)

-- tasks (many rows)
id INTEGER PRIMARY KEY AUTOINCREMENT
date TEXT NOT NULL
name TEXT NOT NULL
type TEXT NOT NULL
minutes INTEGER NOT NULL
status TEXT NOT NULL DEFAULT 'Pending'
sortOrder INTEGER NOT NULL

-- notifications (many rows)
id INTEGER PRIMARY KEY AUTOINCREMENT
type TEXT NOT NULL
message TEXT NOT NULL
created INTEGER NOT NULL
resolved INTEGER NOT NULL DEFAULT 0
```

### Testing Checklist for Changes:

- [ ] First launch still works (welcome → signup → questionnaire → home)
- [ ] Task generation on Home screen loads correctly
- [ ] Timer starts, counts down, persists across rotation
- [ ] Profile edits save correctly
- [ ] Level change wipes tasks and resets cycle
- [ ] Day 28 dialog appears correctly
- [ ] Offline mode works (airplane mode test)
- [ ] Build succeeds: `./gradlew assembleDebug`
- [ ] No compilation errors or warnings

---

## Command Usage

### To Add New Session History:

**Option 1: Use Script (Recommended)**
```bash
cd /Users/loki/RUNOVAV62
./add-to-history.sh
```

**Option 2: Manual Edit**
- Use template in `.add-history-template.md`
- Insert new session BEFORE "## Current State" section
- Update "Last Updated" timestamp
- Update "Current State" section

### Script Prompts:
```
Enter session number (e.g., 8): 
Enter date (YYYY-MM-DD) [2026-09-26]: 
Enter phase/title: 
Describe what changed: 
List files created/modified (comma-separated): 
Explain why these changes were made: 
Any key decisions made?: 
```


*Last Updated: 2026-10-02 11:27 UTC*  
*Total Development Time: ~37-42 hours across 8 phases*  
*Sessions: 14 (Foundation + Welcome + Onboarding + Home + Target + Analytics + Profile + LevelCycle + Full-Screen Chrome + Fixes & Verification + Design Consistency / Responsive / Charts / Timer + Offline Audit / Concurrency / Timer Expiration + Welcome Dialog / Documentation / Level-Up Rework + Training Content / Generic Timer)*  
*Total Files: 28 Java classes + layouts + resources*  
*Unit Tests: 12 passing (LevelPlanTest 9, DateUtilsTest 2, ExampleUnitTest 1)*  
*Build Status: SUCCESS ✅*
