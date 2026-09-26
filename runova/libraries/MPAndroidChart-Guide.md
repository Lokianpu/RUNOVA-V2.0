# MPAndroidChart Implementation Guide for RUNOVA

**Library:** MPAndroidChart v3.1.0  
**Status:** ✅ **Approved for RUNOVA** — the single external dependency; works fully offline at runtime (the AAR is bundled at build time)  
**Purpose:** Data visualization for Analytics screen (weekly progress, level progress, streaks)  
**Documentation:** https://github.com/PhilJay/MPAndroidChart

---

## Setup Complete

✅ Added to `gradle/libs.versions.toml`:
```toml
mpandroidchart = "v3.1.0"
mpandroidchart = { group = "com.github.PhilJay", name = "MPAndroidChart", version.ref = "mpandroidchart" }
```

✅ Added to `app/build.gradle.kts`:
```kotlin
implementation(libs.mpandroidchart)
```

✅ Added JitPack repository to `settings.gradle.kts`:
```kotlin
maven { url = uri("https://jitpack.io") }
```

---

## Basic Usage in RUNOVA

### 1. Weekly Progress Bar Chart

**Use Case:** Show 7-day completion status (Mon-Sun) in Analytics

```xml
<!-- In activity_analytics.xml -->
<com.github.mikephil.charting.charts.BarChart
    android:id="@+id/weekly_chart"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:layout_margin="16dp"
    android:background="@color/runova_primary" />
```

```java
// In AnalyticsActivity.java
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import java.util.ArrayList;

public class AnalyticsActivity extends AppCompatActivity {
    
    private BarChart weeklyChart;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        
        weeklyChart = findViewById(R.id.weekly_chart);
        setupWeeklyChart();
    }
    
    private void setupWeeklyChart() {
        // Get weekly data (0 = missed, 1 = completed)
        int[] weeklyStatus = getWeeklyStatus(); // [1, 1, 0, 1, 0, 1, 0] for Mon-Sun
        
        // Create entries
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, weeklyStatus[i]));
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Days Completed");
        dataSet.setColor(getColor(R.color.runova_success)); // Green for completed
        dataSet.setValueTextColor(getColor(R.color.runova_text)); // White text
        dataSet.setValueTextSize(12f);
        
        // Create BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        
        // Configure chart
        weeklyChart.setData(barData);
        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.setDrawGridBackground(false);
        weeklyChart.setBackgroundColor(getColor(R.color.runova_primary));
        weeklyChart.getLegend().setTextColor(getColor(R.color.runova_text));
        
        // Configure X-axis (day labels)
        XAxis xAxis = weeklyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getColor(R.color.runova_text));
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
            new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}
        ));
        
        // Configure Y-axis
        weeklyChart.getAxisLeft().setTextColor(getColor(R.color.runova_text));
        weeklyChart.getAxisLeft().setAxisMinimum(0f);
        weeklyChart.getAxisLeft().setAxisMaximum(1f);
        weeklyChart.getAxisRight().setEnabled(false);
        
        // Animate
        weeklyChart.animateY(500);
        weeklyChart.invalidate();
    }
    
    private int[] getWeeklyStatus() {
        // Calculate from completedDates
        Set<String> completedDates = dbHelper.getStringSet("completedDates", new HashSet<>());
        int[] status = new int[7];
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        for (int i = 0; i < 7; i++) {
            String date = DateUtils.format(cal.getTime()); // yyyy-MM-dd
            status[i] = completedDates.contains(date) ? 1 : 0;
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        
        return status;
    }
}
```

---

### 2. Level Progress Line Chart

**Use Case:** Show progress over 28-day level cycle

```xml
<com.github.mikephil.charting.charts.LineChart
    android:id="@+id/level_progress_chart"
    android:layout_width="match_parent"
    android:layout_height="250dp"
    android:layout_margin="16dp"
    android:background="@color/runova_primary" />
```

```java
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

private void setupLevelProgressChart() {
    LineChart chart = findViewById(R.id.level_progress_chart);
    
    // Get cumulative completions over 28 days
    ArrayList<Entry> entries = new ArrayList<>();
    Set<String> completedDates = dbHelper.getStringSet("completedDates", new HashSet<>());
    String levelStartDate = dbHelper.getString("levelStartDate", DateUtils.today());
    
    int cumulative = 0;
    for (int day = 0; day <= 28; day++) {
        String date = DateUtils.addDays(levelStartDate, day);
        if (completedDates.contains(date)) {
            cumulative++;
        }
        entries.add(new Entry(day, cumulative));
    }
    
    // Create dataset
    LineDataSet dataSet = new LineDataSet(entries, "Days Completed");
    dataSet.setColor(getColor(R.color.runova_success));
    dataSet.setCircleColor(getColor(R.color.runova_success));
    dataSet.setLineWidth(3f);
    dataSet.setCircleRadius(4f);
    dataSet.setDrawCircleHole(false);
    dataSet.setValueTextColor(getColor(R.color.runova_text));
    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curve
    
    // Add pass threshold line (24 days needed)
    ArrayList<Entry> thresholdEntries = new ArrayList<>();
    thresholdEntries.add(new Entry(0, 24));
    thresholdEntries.add(new Entry(28, 24));
    
    LineDataSet thresholdSet = new LineDataSet(thresholdEntries, "Pass Threshold (85%)");
    thresholdSet.setColor(getColor(R.color.runova_error));
    thresholdSet.setLineWidth(2f);
    thresholdSet.enableDashedLine(10f, 5f, 0f);
    thresholdSet.setDrawCircles(false);
    thresholdSet.setDrawValues(false);
    
    // Combine data
    LineData lineData = new LineData(dataSet, thresholdSet);
    
    // Configure chart
    chart.setData(lineData);
    chart.getDescription().setEnabled(false);
    chart.setBackgroundColor(getColor(R.color.runova_primary));
    chart.getLegend().setTextColor(getColor(R.color.runova_text));
    chart.getXAxis().setTextColor(getColor(R.color.runova_text));
    chart.getAxisLeft().setTextColor(getColor(R.color.runova_text));
    chart.getAxisRight().setEnabled(false);
    
    chart.animateX(800);
    chart.invalidate();
}
```

---

### 3. Streak Indicator (Simple Custom View)

**Use Case:** Show current streak number with visual indicator

```java
// Alternative to chart: use custom TextView styling
private void displayStreak() {
    TextView streakText = findViewById(R.id.streak_text);
    int streak = DateUtils.getCurrentStreak(completedDates);
    
    streakText.setText(streak + " day" + (streak != 1 ? "s" : ""));
    streakText.setTextSize(48);
    streakText.setTextColor(getColor(R.color.runova_success));
    
    // Add fire emoji if streak > 7
    if (streak >= 7) {
        streakText.setText("🔥 " + streak + " days");
    }
}
```

---

### 4. Pie Chart for Status Distribution

**Use Case:** Show Finished/Missed/Pending ratio

```xml
<com.github.mikephil.charting.charts.PieChart
    android:id="@+id/status_pie_chart"
    android:layout_width="250dp"
    android:layout_height="250dp"
    android:layout_gravity="center"
    android:layout_margin="16dp" />
```

```java
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

private void setupStatusPieChart() {
    PieChart chart = findViewById(R.id.status_pie_chart);
    
    // Get status counts
    int finished = countFinishedDays();
    int missed = countMissedDays();
    int pending = 1; // Today if not done
    
    // Create entries
    ArrayList<PieEntry> entries = new ArrayList<>();
    entries.add(new PieEntry(finished, "Finished"));
    entries.add(new PieEntry(missed, "Missed"));
    entries.add(new PieEntry(pending, "Pending"));
    
    // Create dataset
    PieDataSet dataSet = new PieDataSet(entries, "Status");
    dataSet.setColors(
        getColor(R.color.runova_success),  // Green for finished
        getColor(R.color.runova_error),    // Red for missed
        getColor(R.color.runova_text_secondary) // Gray for pending
    );
    dataSet.setValueTextColor(getColor(R.color.runova_text));
    dataSet.setValueTextSize(14f);
    
    PieData pieData = new PieData(dataSet);
    
    // Configure chart
    chart.setData(pieData);
    chart.getDescription().setEnabled(false);
    chart.setBackgroundColor(getColor(R.color.runova_background));
    chart.getLegend().setTextColor(getColor(R.color.runova_text));
    chart.setHoleColor(getColor(R.color.runova_background));
    chart.setTransparentCircleColor(getColor(R.color.runova_primary));
    chart.setTransparentCircleAlpha(50);
    
    chart.animateY(800);
    chart.invalidate();
}
```

---

## RUNOVA Theme Styling for Charts

```java
// Reusable method to apply RUNOVA theme to any chart
private void applyRunovaTheme(Chart chart) {
    chart.setBackgroundColor(getColor(R.color.runova_primary));
    chart.getDescription().setEnabled(false);
    chart.getLegend().setTextColor(getColor(R.color.runova_text));
    chart.getLegend().setTextSize(14f);
    
    if (chart instanceof BarLineChartBase) {
        BarLineChartBase<?> barLineChart = (BarLineChartBase<?>) chart;
        barLineChart.getXAxis().setTextColor(getColor(R.color.runova_text));
        barLineChart.getAxisLeft().setTextColor(getColor(R.color.runova_text));
        barLineChart.getAxisRight().setEnabled(false);
        barLineChart.setDrawGridBackground(false);
        barLineChart.getXAxis().setDrawGridLines(false);
        barLineChart.getAxisLeft().setDrawGridLines(true);
        barLineChart.getAxisLeft().setGridColor(getColor(R.color.runova_text_secondary));
    }
}
```

---

## Best Practices for RUNOVA

1. **Keep it simple** - Use BarChart for weekly view, LineChart for trends
2. **RUNOVA colors** - Always use theme colors (success green, primary blue, error red)
3. **Dark background** - Set chart background to runova_primary or runova_background
4. **Animations** - Use 500-800ms animations for smooth feel
5. **Accessibility** - Ensure text size ≥ 14sp, high contrast colors
6. **Offline** - All data calculated locally from the SQLite database

---

## Common Issues & Solutions

**Issue:** Chart doesn't appear  
**Solution:** Call `chart.invalidate()` after setting data

**Issue:** Labels cut off  
**Solution:** Add `android:layout_margin="16dp"` or call `chart.setExtraOffsets(10, 10, 10, 10)`

**Issue:** Wrong colors in dark theme  
**Solution:** Use `getColor(R.color.runova_*)` not hardcoded hex values

**Issue:** Chart crashes with empty data  
**Solution:** Check if entries list is empty before creating dataset

---

## Example: Complete Analytics Screen with Charts

```java
public class AnalyticsActivity extends AppCompatActivity {
    
    private BarChart weeklyChart;
    private LineChart levelChart;
    private TextView streakText;
    private DbHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        
        dbHelper = new DbHelper(this);
        
        weeklyChart = findViewById(R.id.weekly_chart);
        levelChart = findViewById(R.id.level_chart);
        streakText = findViewById(R.id.streak_text);
        
        setupWeeklyChart();
        setupLevelProgressChart();
        displayStreak();
        
        setupBottomNavigation();
    }
    
    // Chart setup methods here...
}
```

---

**MPAndroidChart is now configured and ready for use in RUNOVA Analytics screen.**
