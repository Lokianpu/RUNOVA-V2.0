# Token-Efficient Skill Activation Framework

**Location:** `runova/.agents/SKILL-ACTIVATION.md`  
**Purpose:** Define when/how skills activate to save tokens across all AI platforms  
**Last Updated:** 2026-09-24

---

## Core Principle

**Skills are dormant by default. Activate only when task explicitly requires them.**

---

## 1. Activation Rules

### A. Manual Activation (User Command)

```
User says: "use pragmatic engineering workflow"
→ Load pragmatic-engineering skill

User says: "validate this form"
→ Load android-ui/form-validation skill

User says: "design the UI"
→ Load design-engineering skill
```

### B. Automatic Activation (Task Detection)

**Triggers:**

| Task Pattern | Auto-Load Skill |
|--------------|-----------------|
| Creating/modifying files | `smart-file-placement` |
| "implement [feature]" | `pragmatic-engineering` |
| "create Activity" | `android-ui/screen-builder` |
| "validate inputs" | `android-ui/form-validation` |
| "add timer" | `android-ui/timer-implementation` |
| "design [component]" | `design-engineering` |
| "test [feature]" | `dev-workflow/testing` |

### C. Never Auto-Load

❌ Don't load skills for:
- Simple questions ("what is X?")
- Reading/explaining code
- Small edits (typo fixes, single-line changes)
- Clarification requests

---

## 2. Silent Background Operation

**When skill activates:**

✓ **DO:**
- Apply skill rules silently
- Output only results
- Report decision if non-obvious
- Keep explanations minimal

✗ **DON'T:**
- Announce skill activation ("Using pragmatic-engineering skill...")
- Explain every step in detail
- Repeat skill content
- Verbose process descriptions

**Example:**

**Bad (token-wasting):**
```
I'm going to use the pragmatic-engineering skill to implement this feature.
First, I'll READ the current implementation by scanning the codebase...
[Lists 20 files scanned]
Now I'm ANALYZING the problem...
[Repeats analysis framework]
Now I'm PLANNING the solution...
[Repeats planning steps]
```

**Good (token-efficient):**
```
Scanned: HomeActivity, Profile, DbHelper
Plan: Add email field to Profile, update validation
Creating: ProfileValidator.emailValidation()
```

---

## 3. Token-Saving Output Rules

### A. Process in Background
```
✓ Do: READ → ANALYZE → PLAN → TEST → EXECUTE → IMPLEMENT
✗ Show: Only final results or key decisions
```

### B. Minimal Reporting
```
Status updates: Brief (1 line)
✓ "Created PaymentService at src/services/"
✗ "I have successfully created the PaymentService.java file and placed it in the src/services/ directory following the existing pattern."

Decisions: Only if non-obvious
✓ "Using Strategy pattern (3 payment types)"
✗ "I decided to use Strategy pattern because..."

Errors: Direct statement
✓ "Test failed: NullPointerException in line 42"
✗ "Unfortunately, the test has failed with a NullPointerException..."
```

### C. File Operations
```
✓ "Created: src/services/PaymentService.java"
✗ "I will now create a new file called PaymentService.java in the src/services/ directory. This file will contain..."
```

---

## 4. Skill-Specific Token Budgets

| Skill | Max Tokens/Use | Notes |
|-------|----------------|-------|
| `smart-file-placement` | 500 | Silent scan, brief output |
| `pragmatic-engineering` | 1000 | Background workflow, report decisions |
| `form-validation` | 300 | Apply pattern, show validation code |
| `screen-builder` | 400 | Generate scaffolding, minimal explanation |
| `timer-implementation` | 400 | Generate timer code, explain persistence |
| `design-engineering` | 800 | Design decisions need some justification |
| `dev-workflow/testing` | 600 | Test strategy, execution results |

**Target: <50% of token budget per skill use.**

---

## 5. Dormant Skill Behavior

**Skills NOT in use:**
- ❌ Not loaded into context
- ❌ Not mentioned in responses
- ❌ Not applied to unrelated tasks

**Example:**
```
Task: Fix typo in string
Skills Active: None (simple fix, no skill needed)
Output: "Fixed typo in welcome_message"
```

---

## 6. Multi-Skill Coordination

**When multiple skills needed:**

```
Task: Create new Activity with form validation

Skills to activate:
1. smart-file-placement (where to put Activity)
2. screen-builder (Activity scaffolding)
3. form-validation (validation logic)
4. pragmatic-engineering (overall workflow)

Execution:
- pragmatic-engineering (background): READ → ANALYZE → PLAN
- smart-file-placement (background): Determine location
- screen-builder (output): Generate Activity code
- form-validation (output): Generate validation code

Report: Only final code + location
```

**Don't report each skill activation.**

---

## 7. Platform-Agnostic Operation

**Works across:**
- Claude (Desktop, API)
- Cursor
- GitHub Copilot
- Windsurf
- Cline
- Kiro
- Any CLI tool

**How:**
- Skills contain logic, not platform commands
- Agent interprets for current platform
- Output format consistent across platforms

---

## 8. Skill Activation Examples

### Example 1: Simple Task (No Skills)
```
User: "Fix the typo in line 42"
Skills: None activated
Process: Direct fix
Output: "Fixed: 'recieve' → 'receive'"
Tokens: ~50
```

### Example 2: Moderate Task (1 Skill)
```
User: "Add email validation to signup"
Skills: form-validation (auto-activated)
Process: Apply validation pattern silently
Output: [validation code]
Tokens: ~300
```

### Example 3: Complex Task (Multiple Skills)
```
User: "Create PaymentActivity"
Skills: pragmatic-engineering, smart-file-placement, screen-builder (auto-activated)
Process: 
  - pragmatic-engineering: READ existing Activities → PLAN structure
  - smart-file-placement: Scan for Activities → Determine location
  - screen-builder: Generate scaffolding
Output: [Activity code] + "Created: app/src/.../PaymentActivity.java"
Tokens: ~1200 (3 skills coordinated efficiently)
```

---

## 9. Token Efficiency Checklist

**Before activating skill, verify:**
- [ ] Task actually needs this skill (not a simple fix)
- [ ] Skill adds value (not just overhead)
- [ ] Can't be done without skill logic
- [ ] Output will be concise (not verbose)

**During skill execution:**
- [ ] Process in background (don't narrate)
- [ ] Apply rules silently
- [ ] Output only results
- [ ] Minimal explanations

**After skill execution:**
- [ ] Report briefly (1-2 lines max for simple tasks)
- [ ] Only explain if decision was non-obvious
- [ ] Deactivate skill (don't keep in context)

---

## 10. Skill Update: Token-Saving Addendum

**Add to ALL skills in `.agents/skills/`:**

```markdown
---

## Token Efficiency

**Activation:** Only when task explicitly requires this skill.

**Operation:** Background. Silent. Apply rules without narration.

**Output:** Results only. Minimal explanation. Brief confirmation.

**Budget:** <[X] tokens per use.

**Deactivation:** Automatic after task complete.

---
```

---

## 11. Implementation for RUNOVA

**Update these files:**

1. `.agents/skills/pragmatic-engineering/SKILL.md` → Add token-efficiency section
2. `.agents/skills/smart-file-placement/SKILL.md` → Already token-efficient
3. `skills/android-ui/form-validation.md` → Add token-efficiency rules
4. `skills/android-ui/screen-builder.md` → Add token-efficiency rules
5. `skills/android-ui/timer-implementation.md` → Add token-efficiency rules
6. `AGENTS.md` → Reference this activation framework

---

## 12. Agent Behavior Summary

**Default state:**
```
Skills: Dormant
Context: Minimal (project files + AGENTS.md)
Tokens: Low baseline
```

**On task start:**
```
1. Analyze task
2. Identify required skills (if any)
3. Activate ONLY needed skills
4. Execute silently
5. Output results
6. Deactivate skills
```

**Token flow:**
```
Simple task:    ~50-200 tokens (no skills)
Moderate task:  ~300-600 tokens (1 skill)
Complex task:   ~1000-1500 tokens (2-3 skills coordinated)
```

**Not:**
```
Every task: Load all skills + verbose process = 5000+ tokens
```

---

## 13. Cross-References

- Skill list: See `AGENTS.md` §3.2
- Token budgets: See table in §4
- Activation triggers: See table in §1.B
- Output format: See `smart-file-placement` §10 for example

---

**Last Updated:** 2026-09-24  
**Framework Version:** 1.0  
**Target:** <50% token usage vs. verbose approach

---

**Use always. Load skills on-demand. Execute silently. Report briefly. Deactivate after use.**
