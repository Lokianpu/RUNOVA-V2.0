# Skill Manager

**Role:** Intelligent dispatcher that analyzes every user input and activates only necessary skills  
**Trigger:** ALWAYS. First thing on every session, every instruction, every task  
**Purpose:** Quality + accuracy while minimizing token usage

---

## 1. Core Responsibility

**Act as gatekeeper between user input and skill execution.**

```
User Input → Skill Manager (analyze) → Activate ONLY needed skills → Execute → Output
```

**Never:** Load all skills. Load skills "just in case". Keep skills loaded after task complete.

---

## 2. Activation Sequence

**EVERY user instruction follows this:**

```
1. Receive user input
2. Analyze task intent
3. Match to skill patterns
4. Activate minimal skill set
5. Skills execute (silently)
6. Deactivate skills
7. Output results
```

**This happens in background. User sees only final output.**

---

## 3. Task Analysis Framework

### A. Parse User Input

**Extract:**
```
Action: What user wants (create, fix, analyze, explain, implement)
Target: What to work on (file, feature, component, system)
Scope: Size of change (line, function, file, multiple files, architecture)
Context: Current state (existing code, new feature, refactor)
```

**Example:**
```
Input: "Add email validation to signup form"

Parsed:
- Action: Add (implement new functionality)
- Target: Email validation + signup form
- Scope: Single function (validation method)
- Context: Existing form (modify existing code)
```

### B. Determine Complexity

```
Simple (no skills needed):
- Single line change
- Typo fix
- Config value update
- Question/explanation

Basic (1 skill):
- Add validation to existing form
- Create new file following pattern
- Fix bug in single component

Moderate (2-3 skills):
- Create new feature (multiple files)
- Refactor component
- Implement UI with logic

Complex (3+ skills):
- New architecture
- Multi-component feature
- System-wide change
```

---

## 4. Skill Selection Logic

### Available Skills Registry

```javascript
SKILLS = {
  // Core workflow
  "pragmatic-engineering": {
    triggers: ["implement", "create feature", "refactor", "architecture"],
    scope: ["moderate", "complex"],
    cost: 1000
  },
  
  // File organization
  "smart-file-placement": {
    triggers: ["create file", "new file", "add file"],
    scope: ["basic", "moderate", "complex"],
    cost: 500
  },
  
  // Android-specific
  "screen-builder": {
    triggers: ["create activity", "new screen", "add activity"],
    scope: ["basic", "moderate"],
    cost: 400
  },
  
  "form-validation": {
    triggers: ["validate", "validation", "check input"],
    scope: ["basic"],
    cost: 300
  },
  
  "timer-implementation": {
    triggers: ["timer", "countdown", "stopwatch"],
    scope: ["basic", "moderate"],
    cost: 400
  },
  
  // Design
  "design-engineering": {
    triggers: ["design", "ui", "layout", "component"],
    scope: ["moderate", "complex"],
    cost: 800
  },
  
  // Testing
  "testing": {
    triggers: ["test", "verify", "check"],
    scope: ["basic", "moderate"],
    cost: 600
  }
}
```

### Matching Algorithm

```
For each skill:
  1. Check if any trigger word in user input
  2. Check if scope matches task complexity
  3. Check if context appropriate (e.g., Android project for android-ui skills)
  4. If all match → Add to activation list
```

---

## 5. Decision Matrix

### Task: "Add email validation to signup"

**Analysis:**
```
Action: Add
Target: Validation
Scope: Basic (single function)
Keywords: "validation"
```

**Skill Match:**
```
form-validation: ✓ (trigger: "validation", scope: basic)
smart-file-placement: ✗ (no new files)
pragmatic-engineering: ✗ (too simple, overkill)
screen-builder: ✗ (not creating screen)
```

**Decision:** Activate `form-validation` only.

**Token cost:** 300 (vs 3000+ if all skills loaded)

---

### Task: "Create PaymentActivity with card input form"

**Analysis:**
```
Action: Create
Target: New Activity + form
Scope: Moderate (multiple components)
Keywords: "create", "activity", "form"
```

**Skill Match:**
```
screen-builder: ✓ (trigger: "create activity", scope: moderate)
form-validation: ✓ (trigger: form, scope: basic)
smart-file-placement: ✓ (trigger: "create", scope: moderate)
pragmatic-engineering: ✓ (trigger: "create feature", scope: moderate)
design-engineering: ✗ (no design discussion requested)
```

**Decision:** Activate `pragmatic-engineering` (orchestrator), `smart-file-placement`, `screen-builder`, `form-validation`.

**Token cost:** 2100 (still saves vs. loading design-engineering + testing unnecessarily)

---

### Task: "Explain how getDailyTarget works"

**Analysis:**
```
Action: Explain
Target: Existing code
Scope: None (read-only)
Keywords: "explain"
```

**Skill Match:**
```
All skills: ✗ (no implementation, just explanation)
```

**Decision:** Activate NO skills. Direct response.

**Token cost:** 100-200 (vs 5000+ if skills loaded)

---

## 6. Orchestration Rules

### A. Skill Priority

**When multiple skills activate:**

```
1. pragmatic-engineering (if present) → Acts as orchestrator
2. smart-file-placement → Runs first (determines locations)
3. Specific skills (screen-builder, form-validation, etc.) → Execute
4. All deactivate after task complete
```

### B. Skill Communication

**Skills don't call each other. Manager coordinates.**

```
Manager activates: [pragmatic-engineering, smart-file-placement, screen-builder]
  ↓
pragmatic-engineering: READ → ANALYZE → PLAN
  ↓
smart-file-placement: Determine file location
  ↓
screen-builder: Generate Activity code
  ↓
Manager: Collect outputs, deactivate all skills, return to user
```

### C. Conflict Resolution

**If skills overlap:**
```
Example: Both pragmatic-engineering and screen-builder handle "create Activity"

Resolution:
- pragmatic-engineering: Handles workflow (READ/ANALYZE/PLAN)
- screen-builder: Handles code generation
- No conflict: Different responsibilities
```

**If true conflict:**
```
- Choose more specific skill
- Example: form-validation (specific) over pragmatic-engineering (general)
```

---

## 7. Token Budget Management

### Track Usage Per Session

```
Session start: Budget = 10,000 tokens available
  ↓
Task 1: Activate form-validation (300 tokens)
Remaining: 9,700
  ↓
Task 2: Activate screen-builder + smart-file-placement (900 tokens)
Remaining: 8,800
  ↓
Task 3: Simple fix, no skills (50 tokens)
Remaining: 8,750
```

### Budget Warnings

```
If remaining < 2000 tokens:
  → Warn user: "Context getting full, consider starting new session"
  
If task requires > remaining tokens:
  → Prioritize essential skills only
  → Or suggest: "This task needs fresh session for best results"
```

---

## 8. Quality Assurance

**Skill Manager ensures:**

```
✓ Right skills for the task
✓ No unnecessary skills loaded
✓ Skills execute in correct order
✓ Skills deactivate after use
✓ Token budget respected
✓ Quality output (skills used when needed)
```

**NOT responsible for:**
```
✗ Executing skills (skills do that)
✗ Writing code (skills do that)
✗ Making architectural decisions (pragmatic-engineering does that)
```

**Only responsible for:** Intelligent skill selection and orchestration.

---

## 9. Decision Examples

### Ex 1: Simple Question
```
User: "What's the difference between Activity and Fragment?"
Analysis: Question, no implementation
Skills: None
Token cost: 100
Output: Direct explanation
```

### Ex 2: Typo Fix
```
User: "Fix typo in line 42"
Analysis: Single line change
Skills: None
Token cost: 50
Output: "Fixed: 'recieve' → 'receive'"
```

### Ex 3: Add Field
```
User: "Add phone field to profile"
Analysis: Basic addition to existing code
Skills: form-validation (if validation needed)
Token cost: 300
Output: [code with field + validation]
```

### Ex 4: New Feature
```
User: "Implement password reset feature"
Analysis: Moderate complexity, multiple files
Skills: pragmatic-engineering, smart-file-placement, form-validation
Token cost: 1800
Output: [complete implementation]
```

### Ex 5: Architecture Change
```
User: "Refactor to MVVM architecture"
Analysis: Complex, system-wide
Skills: pragmatic-engineering, design-engineering (if UI changes)
Token cost: 1800-2600
Output: [refactoring plan + implementation]
```

---

## 10. Integration with RUNOVA

**RUNOVA-specific considerations:**

```javascript
RUNOVA_CONTEXT = {
  platform: "Android",
  language: "Java",
  constraints: ["offline-first", "zero-dependencies", "SharedPreferences-only"],
  available_skills: [
    "pragmatic-engineering",
    "smart-file-placement", 
    "screen-builder",
    "form-validation",
    "timer-implementation",
    "design-engineering",
    "testing"
  ]
}
```

**Skill selection considers:**
- Android-specific skills prioritized
- Offline constraint (no network-related skills)
- MVC architecture (respect layer boundaries)

---

## 11. Skill Manager Workflow

**Every user input triggers this:**

```
┌─────────────────────────────────────────┐
│ USER INPUT                               │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ SKILL MANAGER                            │
│                                          │
│ 1. Parse input (action, target, scope)  │
│ 2. Analyze complexity (simple → complex)│
│ 3. Match skills (check triggers)        │
│ 4. Check context (RUNOVA constraints)   │
│ 5. Estimate token cost                  │
│ 6. Activate minimal skill set           │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ SKILLS EXECUTE (silently)                │
│                                          │
│ - pragmatic-engineering (if activated)   │
│ - smart-file-placement (if activated)    │
│ - screen-builder (if activated)          │
│ - form-validation (if activated)         │
│ - etc.                                   │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ SKILL MANAGER                            │
│                                          │
│ 7. Collect outputs                       │
│ 8. Deactivate all skills                 │
│ 9. Return results to user                │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│ OUTPUT TO USER                           │
└─────────────────────────────────────────┘
```

**User only sees INPUT and OUTPUT. Everything in middle is silent.**

---

## 12. Implementation Checklist

**Skill Manager must:**

- [ ] Analyze every user input before any skill activation
- [ ] Match input to skill triggers (pattern matching)
- [ ] Determine task complexity (simple → complex)
- [ ] Consider project context (RUNOVA constraints)
- [ ] Activate ONLY necessary skills (minimal set)
- [ ] Orchestrate skill execution order
- [ ] Collect outputs from activated skills
- [ ] Deactivate skills after task complete
- [ ] Track token usage per session
- [ ] Warn if budget low

**Skill Manager must NOT:**

- [ ] Execute skills itself (delegate to skills)
- [ ] Keep skills loaded after task done
- [ ] Activate skills "just in case"
- [ ] Explain its own decision process (silent operation)
- [ ] Load all skills by default

---

## 13. Success Metrics

**Effective Skill Management:**

```
✓ Right skills activated (task needs matched)
✓ No unnecessary skills (only what's needed)
✓ Token usage <50% of "load everything" approach
✓ Quality output maintained (skills used when appropriate)
✓ Fast execution (minimal overhead from skill selection)
```

**Poor Skill Management:**

```
✗ All skills loaded every time (wasteful)
✗ Wrong skills activated (mismatch)
✗ Skills kept loaded after task (memory leak)
✗ Quality suffers (missing needed skills)
✗ Slow execution (too much overhead)
```

---

## 14. Quick Reference

**Skill selection algorithm:**
```
Input → Parse → Complexity → Match triggers → Check context → Activate → Execute → Deactivate → Output
```

**Token budgets:**
```
Simple (no skills):     50-200 tokens
Basic (1 skill):        300-600 tokens  
Moderate (2-3 skills):  1000-2000 tokens
Complex (3+ skills):    2000-3000 tokens
```

**vs. loading all skills every time: 5000+ tokens**

---

## 15. Cross-References

- Skill activation rules: `SKILL-ACTIVATION.md`
- Available skills: `AGENTS.md` §3.2
- Token budgets: `SKILL-ACTIVATION.md` §4
- RUNOVA context: `context/ARCHITECTURE.md`

---

**Last Updated:** 2026-09-24  
**Skill Version:** 1.0  
**Token Efficiency:** 50-70% savings vs. loading all skills

---

**ALWAYS ACTIVE. First line of defense. Runs on every user input. Silent operation. Intelligent skill selection. Maximum quality. Minimum tokens.**
