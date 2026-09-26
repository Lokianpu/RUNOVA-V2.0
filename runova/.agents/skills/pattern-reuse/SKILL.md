# Pattern Reuse Skill

**Role:** Discover and reuse existing designs, architectures, components, and patterns from current project  
**Trigger:** Automatically by skill-manager when implementing anything new  
**Purpose:** Consistency + speed + token savings through intelligent reuse

---

## Core Principle

**Before creating anything new, scan project for existing patterns and reuse them.**

```
New task → Scan existing → Find similar → Reuse pattern → Adapt if needed
```

**Not:** Create from scratch every time.

---

## 1. What to Discover & Reuse

### A. Design Patterns
- Colors, typography, spacing
- Component styles (buttons, cards, inputs)
- Layout structures
- Animations, transitions

### B. Architecture Patterns
- File structure (where similar files live)
- Class organization (MVC, MVVM, etc.)
- Data flow patterns
- API patterns

### C. Code Patterns
- Validation logic
- Error handling
- Data persistence
- Navigation

### D. Components & Resources
- Reusable UI components
- Utility functions
- Helper classes
- Assets (images, icons)

---

## 2. Discovery Process

**For every new implementation:**

```
1. Identify what you're building (Activity, service, component, etc.)
2. Find similar existing examples
3. Extract common patterns
4. Reuse pattern, adapt to new context
5. Create new only if no pattern exists
```

---

## 3. Pattern Discovery Rules

### A. Design Reuse

**Example: Creating new Activity**

```bash
# 1. Find existing Activities
find . -name "*Activity.java" | head -5

# 2. Read one as template
cat app/src/main/java/.../HomeActivity.java

# 3. Extract design patterns:
Colors used: @color/runova_background, @color/runova_primary
Layout: ConstraintLayout + ScrollView
Typography: 24sp heading, 16sp body
Spacing: 16dp margins, 24dp top
Components: CardView with 8dp corners, 16dp padding
```

**Apply to new Activity:**
```java
// Reuse exact same design system
setBackgroundColor(R.color.runova_background);
textHeading.setTextSize(24);
textHeading.setTextColor(R.color.runova_text);
// ... etc.
```

### B. Architecture Reuse

**Example: Creating new Service**

```bash
# 1. Find existing services
find . -name "*Service.java"

# Found: ProfileService.java, PrefsHelper.java

# 2. Extract architecture pattern:
Structure:
- Static methods (no instance needed)
- Context parameter passed in
- Uses SharedPreferences
- Error handling: try-catch with Log.e()

# 3. Apply to new EmailService.java
```

**Follow same pattern:**
```java
public class EmailService {
    private static final String TAG = "EmailService";
    
    public static void sendEmail(Context context, String email) {
        try {
            // Implementation
        } catch (Exception e) {
            Log.e(TAG, "Failed to send email", e);
        }
    }
}
```

### C. Code Pattern Reuse

**Example: Adding new validation**

```bash
# 1. Find existing validation
grep -r "validateInputs" app/src/

# Found: SignupActivity has validateInputs() method

# 2. Extract validation pattern:
```

```java
private boolean validateInputs() {
    boolean valid = true;
    
    // String validation
    String name = nameInput.getText().toString().trim();
    if (name.isEmpty()) {
        nameInput.setError("Name cannot be empty");
        valid = false;
    }
    
    // Number validation
    try {
        int height = Integer.parseInt(heightInput.getText().toString());
        if (height < 100 || height > 250) {
            heightInput.setError("Height must be between 100 and 250 cm");
            valid = false;
        }
    } catch (NumberFormatException e) {
        heightInput.setError("Please enter a valid number");
        valid = false;
    }
    
    return valid;
}
```

**Reuse pattern for new form:**
```java
// Follow EXACT same structure
private boolean validatePaymentInputs() {
    boolean valid = true;
    
    // Card number validation (follow same pattern)
    String cardNumber = cardInput.getText().toString().trim();
    if (cardNumber.isEmpty()) {
        cardInput.setError("Card number cannot be empty");
        valid = false;
    }
    
    return valid;
}
```

### D. Component Reuse

**Example: Need a card-style container**

```bash
# 1. Find existing cards
grep -r "CardView" app/res/layout/

# Found: activity_home.xml uses CardView

# 2. Extract component pattern:
```

```xml
<androidx.cardview.widget.CardView
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardBackgroundColor="@color/runova_card_background"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- Content here -->
        
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

**Reuse for new screen:**
```xml
<!-- Copy exact pattern, change content only -->
<androidx.cardview.widget.CardView
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardBackgroundColor="@color/runova_card_background"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- NEW content here -->
        
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

---

## 4. Pattern Extraction Algorithm

**For any new implementation:**

```
1. SCAN: Find similar existing examples
   Command: find, grep, glob
   
2. READ: Open 1-2 examples (most recent or best)
   
3. EXTRACT: Identify patterns
   - Design: Colors, sizes, spacing
   - Structure: File organization, class hierarchy
   - Logic: Algorithms, validation, error handling
   
4. DOCUMENT: Note pattern (mental or comment)
   "Following HomeActivity pattern for consistency"
   
5. APPLY: Use pattern in new implementation
   
6. ADAPT: Only change what's different
   Keep: Design system, structure, error handling
   Change: Business logic, specific content
```

---

## 5. Consistency Rules

### A. Always Reuse These

**Design System:**
- ✓ Colors from `colors.xml`
- ✓ Text sizes from existing screens
- ✓ Spacing values (8dp, 16dp, 24dp)
- ✓ Component styles (buttons, cards, inputs)

**Architecture:**
- ✓ File locations (where similar files live)
- ✓ Class structure (follow existing pattern)
- ✓ Method naming (follow conventions)
- ✓ Error handling pattern

**Code Patterns:**
- ✓ Validation structure
- ✓ SharedPreferences usage
- ✓ Intent navigation
- ✓ Lifecycle methods order

### B. Don't Create New Unless Needed

**Before creating new component, check:**
```
- [ ] Does similar component exist?
- [ ] Can existing component be reused?
- [ ] Can existing component be extended?
- [ ] Is new component truly needed?
```

**Only create new if all answers: No, No, No, Yes.**

---

## 6. Fallback Strategy

**When no existing pattern found:**

```
1. Check context/DESIGN.md for design system
2. Check context/ARCHITECTURE.md for architectural patterns
3. Check context/RULES.md for coding patterns
4. Use language/framework best practices
5. Create new pattern (document it for future reuse)
```

**Example:**
```
Task: Add image upload feature (no existing example)

Fallback:
1. Check DESIGN.md → Colors, spacing, button style
2. Check ARCHITECTURE.md → Where to put new class
3. Check RULES.md → File naming conventions
4. Android best practices → Use Intent for image picker
5. Create ImageUploadHelper.java following discovered patterns
```

---

## 7. Token Savings

**Scanning existing patterns is cheaper than asking user or guessing:**

```
Scan + reuse: 200-300 tokens
  find . -name "*Activity.java" | head -3
  cat HomeActivity.java
  Extract pattern
  Apply to new Activity
  
vs.

Ask user: 500-1000 tokens
  "What colors should I use?"
  "What spacing?"
  "What component style?"
  User explains design system
  
vs.

Guess: High risk
  Create inconsistent design
  User rejects
  Redo work
  Total tokens: 2000+
```

**Savings: 60-80% tokens + guaranteed consistency**

---

## 8. Examples by Task Type

### Ex 1: New Screen (Activity)

**Discover:**
```bash
# Find existing Activity
ls app/src/main/java/.../activities/

# Read HomeActivity as template
# Extract: layout structure, color usage, navigation pattern
```

**Reuse:**
- Same `ConstraintLayout` root
- Same background color
- Same heading style (24sp bold white)
- Same bottom navigation (if applicable)
- Same Intent navigation pattern

**Create:** Only new business logic and content.

---

### Ex 2: New Form Field

**Discover:**
```bash
# Find existing form
grep -r "EditText" app/res/layout/

# Read activity_signup.xml
# Extract: input style, hint color, error handling
```

**Reuse:**
```xml
<!-- Exact same EditText style -->
<EditText
    android:id="@+id/input_email"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:hint="Email address"
    android:textColor="@android:color/black"
    android:textColorHint="@color/runova_input_hint"
    android:background="@drawable/input_background"
    android:padding="12dp"
    android:inputType="textEmailAddress" />
```

**Create:** Only new `id` and `hint` text.

---

### Ex 3: New Utility Method

**Discover:**
```bash
# Find existing utils
ls app/src/main/java/.../utils/

# Read DateUtils.java
# Extract: method structure, error handling, static pattern
```

**Reuse:**
```java
// Follow same pattern
public class StringUtils {
    private static final String TAG = "StringUtils";
    
    public static String capitalize(String input) {
        try {
            if (input == null || input.isEmpty()) return input;
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        } catch (Exception e) {
            Log.e(TAG, "Failed to capitalize string", e);
            return input;
        }
    }
}
```

**Create:** Only new logic (capitalize algorithm).

---

### Ex 4: New API Call (if applicable)

**Discover:**
```bash
# Find existing API calls
grep -r "HttpURLConnection\|Retrofit\|Volley" app/src/

# Extract: error handling, async pattern, response parsing
```

**Reuse:** Same error handling, timeout, retry logic.

**Create:** Only new endpoint and response model.

---

## 9. Integration with Skill Manager

**Skill Manager activates pattern-reuse automatically:**

```
User: "Create PaymentActivity"
  ↓
Skill Manager:
  - Activates: pragmatic-engineering, smart-file-placement, screen-builder
  - Activates: pattern-reuse (background)
  ↓
Pattern Reuse:
  - Scans: Find existing Activities
  - Extracts: Design system, structure, patterns
  - Provides to screen-builder: Template to follow
  ↓
Screen Builder:
  - Uses template from pattern-reuse
  - Generates consistent Activity
  ↓
Output: New Activity matching existing design perfectly
```

**User doesn't see pattern-reuse working. Just sees consistent output.**

---

## 10. Pattern Documentation

**After creating new pattern, document for future reuse:**

```markdown
## Reusable Pattern: Card Container

**Context:** Use for grouped content on any screen

**Code:**
```xml
<androidx.cardview.widget.CardView
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardBackgroundColor="@color/runova_card_background"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    <LinearLayout
        android:padding="16dp">
        <!-- Content -->
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

**Used in:** HomeActivity, AnalyticsActivity, ProfileActivity

**Location:** Any activity layout
```

**Store in:** `docs/patterns/` or add to `context/DESIGN.md`

---

## 11. Quick Decision Tree

```
Need to implement something new?
    ↓
Does similar thing exist in project? ──Yes→ Scan → Extract pattern → Reuse
    ↓ No
Check context/ docs for guidance? ──Yes→ Follow documented pattern
    ↓ No
Framework/language best practice? ──Yes→ Follow standard pattern
    ↓ No
Create new + document for future reuse
```

---

## 12. Success Criteria

**Pattern reuse working well:**
- ✓ New implementations match existing design
- ✓ Consistency across all screens/components
- ✓ Fast implementation (template exists)
- ✓ Low token usage (scan vs. ask/guess)
- ✓ User doesn't need to specify design details

**Pattern reuse not working:**
- ✗ Inconsistent designs (each screen different)
- ✗ Slow implementation (recreating from scratch)
- ✗ High token usage (asking for every detail)
- ✗ User needs to specify colors, spacing, etc. every time

---

## 13. Token Efficiency

**Activation:** Automatic by skill-manager when implementing new features.

**Operation:** Background scan, extract patterns, apply silently.

**Output:** Consistent implementation. No explanation needed (user sees it matches existing).

**Budget:** 200-300 tokens (scan + extract + apply) vs. 1000+ tokens (ask user for every detail).

**Deactivation:** After pattern applied to new implementation.

---

## 14. Cross-References

- Design patterns: `context/DESIGN.md`
- Architecture patterns: `context/ARCHITECTURE.md`
- Code patterns: `context/RULES.md`
- Existing components: Scan project files
- Skill coordination: `skill-manager/SKILL.md`

---

**Last Updated:** 2026-09-24  
**Skill Version:** 1.0  
**Token Efficiency:** 60-80% savings + guaranteed consistency

---

**ALWAYS ACTIVE (via skill-manager). Scan before creating. Reuse existing patterns. Create new only when truly needed. Document new patterns for future reuse. Maintain consistency. Save tokens.**
