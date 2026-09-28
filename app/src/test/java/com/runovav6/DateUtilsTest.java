package com.runovav6;

import com.runova.helpers.DateUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DateUtilsTest {

    @Test
    public void testDateCalculations() {
        assertEquals(1, DateUtils.getWeekday("2026-09-28")); // Monday
        assertEquals(7, DateUtils.getWeekday("2026-10-04")); // Sunday
        assertTrue(DateUtils.isValidDate("2026-09-28"));
        assertNotNull(DateUtils.today());
        assertEquals(6, DateUtils.daysBetween("2026-09-28", "2026-10-04"));
    }

    @Test
    public void testConcurrentDateAccessDoesNotCorrupt() throws Exception {
        int threadCount = 10;
        int iterationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    int weekday = DateUtils.getWeekday("2026-09-28");
                    if (weekday != 1) return false;
                    long diff = DateUtils.daysBetween("2026-01-01", "2026-01-10");
                    if (diff != 9) return false;
                    if (!DateUtils.isValidDate("2026-09-28")) return false;
                }
                return true;
            });
        }

        List<Future<Boolean>> results = executor.invokeAll(tasks);
        for (Future<Boolean> result : results) {
            assertTrue("Concurrent DateUtils access produced inconsistent result", result.get());
        }
        executor.shutdown();
    }
}
