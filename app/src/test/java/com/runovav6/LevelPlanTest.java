package com.runovav6;

import com.runova.helpers.DateUtils;
import com.runova.models.DayTemplate;
import com.runova.models.TaskTemplate;
import com.runova.training.LevelPlan;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies the approved weekly training plan.
 *
 * <p>This replaces {@code RoutineEngineTest}, which tested the V5 {@code RoutineEngine} API
 * (a fixed 5-task training day scaled by a weekly-minutes band). That API no longer exists:
 * the approved plan is a static weekly template per level, implemented by {@link LevelPlan},
 * so the V5 rules (5 tasks per training day, band scaling) are superseded. The invariants the
 * old test protected are kept: a fixed task count per weekday, a distinct recovery day,
 * warm-up first, cool-down last, only approved task types, no nutrition / hydration / sleep
 * content, and deterministic output.
 *
 * <p>Everything here is host-testable: {@code LevelPlan}, {@code DayTemplate},
 * {@code TaskTemplate} and {@code DateUtils} are plain Java with no Android dependencies.
 */
public class LevelPlanTest {

    private static final String BEGINNER = "Beginner";
    private static final String INTERMEDIATE = "Intermediate";
    private static final String PRO = "Pro";

    private static final String[] LEVELS = {BEGINNER, INTERMEDIATE, PRO};

    /** Approved Monday-to-Saturday task counts, in {@link #LEVELS} order (runova/README.md). */
    private static final int[][] TASKS_PER_WEEKDAY = {
            {3, 4, 3, 4, 3, 4},
            {3, 4, 5, 3, 4, 5},
            {4, 5, 4, 5, 4, 5}};

    /** Approved maximum minutes per day, in {@link #LEVELS} order. */
    private static final int[] DAILY_CAP_MINUTES = {60, 90, 120};

    private static final int[] WARMUP_MINUTES = {10, 15, 15};
    private static final int[] COOLDOWN_MINUTES = {10, 15, 15};

    /** Sunday is the recovery day for every level. */
    private static final int RECOVERY_WEEKDAY = 7;
    private static final int TASKS_PER_RECOVERY_DAY = 2;

    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "WARMUP", "EASY_RUN", "EASY_LONG_RUN", "TEMPO_RUN", "INTERVAL_RUN", "SPEED_INTERVALS",
            "STRIDES", "DRILLS", "STRENGTH_CORE", "CROSS_TRAINING", "MOBILITY", "TECHNIQUE",
            "COOLDOWN", "EASY_WALK", "STRETCH"));

    /** Running types that must never appear on the Sunday recovery day. */
    private static final Set<String> RUNNING_TYPES = new HashSet<>(Arrays.asList(
            "EASY_RUN", "EASY_LONG_RUN", "TEMPO_RUN", "INTERVAL_RUN", "SPEED_INTERVALS",
            "STRIDES", "DRILLS"));

    /** Out of scope by the project charter, section 0.3. */
    private static final String[] FORBIDDEN_WORDS = {
            "nutrition", "food", "calorie", "water", "hydration", "sleep", "meditation"};

    @Test
    public void trainingDaysMatchTheApprovedTaskCounts() {
        for (int levelIndex = 0; levelIndex < LEVELS.length; levelIndex++) {
            for (int weekday = 1; weekday <= 6; weekday++) {
                List<TaskTemplate> tasks = template(LEVELS[levelIndex], weekday).tasks;
                assertEquals(LEVELS[levelIndex] + " weekday " + weekday + " task count",
                        TASKS_PER_WEEKDAY[levelIndex][weekday - 1], tasks.size());
            }
        }
    }

    @Test
    public void everyLevelHasATwoTaskRecoveryDayOnSunday() {
        for (String level : LEVELS) {
            List<TaskTemplate> tasks = template(level, RECOVERY_WEEKDAY).tasks;
            assertEquals(level + " Sunday task count", TASKS_PER_RECOVERY_DAY, tasks.size());
            for (TaskTemplate task : tasks) {
                assertTrue(level + " Sunday type must be allowed: " + task.type,
                        ALLOWED_TYPES.contains(task.type));
                assertFalse(level + " Sunday must not contain training: " + task.type,
                        RUNNING_TYPES.contains(task.type));
            }
        }
    }

    @Test
    public void dailyTotalNeverExceedsTheLevelCap() {
        for (int levelIndex = 0; levelIndex < LEVELS.length; levelIndex++) {
            for (int weekday = 1; weekday <= 7; weekday++) {
                int total = totalMinutes(template(LEVELS[levelIndex], weekday).tasks);
                assertTrue(LEVELS[levelIndex] + " weekday " + weekday + " totals " + total
                                + " minutes, cap is " + DAILY_CAP_MINUTES[levelIndex],
                        total <= DAILY_CAP_MINUTES[levelIndex]);
            }
        }
    }

    @Test
    public void everyTaskHasANameAPositiveDurationAndAnAllowedType() {
        for (String level : LEVELS) {
            for (int weekday = 1; weekday <= 7; weekday++) {
                for (TaskTemplate task : template(level, weekday).tasks) {
                    assertNotNull(task.name);
                    assertNotNull(task.type);
                    assertFalse("empty task name", task.name.trim().isEmpty());
                    assertTrue("minutes must be positive on " + task.name, task.minutes > 0);
                    assertTrue("allowed type: " + task.type, ALLOWED_TYPES.contains(task.type));
                    assertFalse("forbidden content in " + task.name,
                            mentionsForbiddenWord(task.name));
                }
            }
        }
    }

    @Test
    public void trainingDaysStartWithWarmupAndEndWithCooldown() {
        for (int levelIndex = 0; levelIndex < LEVELS.length; levelIndex++) {
            for (int weekday = 1; weekday <= 6; weekday++) {
                List<TaskTemplate> tasks = template(LEVELS[levelIndex], weekday).tasks;
                assertEquals("first task", "WARMUP", tasks.get(0).type);
                assertEquals("last task", "COOLDOWN", tasks.get(tasks.size() - 1).type);
                assertEquals("warm-up minutes", WARMUP_MINUTES[levelIndex], tasks.get(0).minutes);
                assertEquals("cool-down minutes", COOLDOWN_MINUTES[levelIndex],
                        tasks.get(tasks.size() - 1).minutes);
            }
        }
    }

    @Test
    public void unknownLevelFallsBackToBeginner() {
        for (int weekday = 1; weekday <= 7; weekday++) {
            assertEquals("weekday " + weekday,
                    describe(template(BEGINNER, weekday)), describe(template("Elite", weekday)));
        }
    }

    @Test
    public void planIsDeterministicAndLevelsDiffer() {
        for (String level : LEVELS) {
            for (int weekday = 1; weekday <= 7; weekday++) {
                assertEquals(level + " weekday " + weekday,
                        describe(template(level, weekday)), describe(template(level, weekday)));
            }
        }
        assertNotEquals(describe(template(BEGINNER, 1)), describe(template(PRO, 1)));
    }

    /** Exercises the path the screens use: ISO date to weekday to template. */
    @Test
    public void everyCalendarDayMapsToTheExpectedTemplateSize() {
        String[] week = {"2026-09-21", "2026-09-22", "2026-09-23", "2026-09-24",
                "2026-09-25", "2026-09-26", "2026-09-27"};
        int[] expectedWeekdays = {1, 2, 3, 4, 5, 6, 7};

        for (int i = 0; i < week.length; i++) {
            int weekday = DateUtils.getWeekday(week[i]);
            assertEquals(week[i] + " weekday", expectedWeekdays[i], weekday);

            for (int levelIndex = 0; levelIndex < LEVELS.length; levelIndex++) {
                int expected = weekday == RECOVERY_WEEKDAY
                        ? TASKS_PER_RECOVERY_DAY
                        : TASKS_PER_WEEKDAY[levelIndex][weekday - 1];
                assertEquals(LEVELS[levelIndex] + " on " + week[i], expected,
                        template(LEVELS[levelIndex], weekday).tasks.size());
            }
        }
    }

    @Test
    public void unparsableDateFallsBackToMonday() {
        assertEquals(1, DateUtils.getWeekday("not-a-date"));
    }

    // --- helpers ---------------------------------------------------------

    private static DayTemplate template(String level, int weekday) {
        DayTemplate day = LevelPlan.getTemplate(level, weekday);
        assertNotNull("no template for " + level + " weekday " + weekday, day);
        assertNotNull("no task list for " + level + " weekday " + weekday, day.tasks);
        return day;
    }

    private static int totalMinutes(List<TaskTemplate> tasks) {
        int total = 0;
        for (TaskTemplate task : tasks) {
            total += task.minutes;
        }
        return total;
    }

    private static String describe(DayTemplate day) {
        StringBuilder description = new StringBuilder();
        for (TaskTemplate task : day.tasks) {
            description.append(task.name).append(':').append(task.type)
                    .append(':').append(task.minutes).append('|');
        }
        return description.toString();
    }

    private static boolean mentionsForbiddenWord(String text) {
        String lower = text.toLowerCase(Locale.US);
        for (String word : FORBIDDEN_WORDS) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
