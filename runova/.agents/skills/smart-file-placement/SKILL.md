# Smart File Placement Skill

**Purpose:** Automatically determine correct file locations without user instruction  
**Domain:** File organization across all project types  
**Token-efficient:** Minimal decision trees, maximum automation

---

## Core Rule

**Before creating any file, scan existing structure and place in correct location automatically.**

---

## 1. Quick Decision Tree

```
New file type?
├─ Code file → Find related files → Place beside them
├─ Test file → Mirror source structure in tests/
├─ Config file → Root or .config/
├─ Doc file → docs/ or root (if main README)
└─ Asset → assets/ or public/
```

---

## 2. Location Detection Rules

### A. Code Files

**Rule:** Place beside related functionality.

```
Creating: UserService.java
Scan for: *Service.java, User*.java, *user*.java
Found: ProfileService.java in src/services/
Action: Create at src/services/UserService.java
```

**Priority order:**
1. Same class name pattern (e.g., `*Service`, `*Controller`)
2. Same feature name (e.g., `user*`, `auth*`)
3. Same package/module structure
4. Default: `src/[type]/` (create if needed)

### B. Test Files

**Rule:** Mirror source structure.

```
Testing: src/services/UserService.java
Action: Create at tests/services/UserServiceTest.java
```

**Naming patterns:**
- Java: `[Name]Test.java`
- JavaScript: `[name].test.js` or `[name].spec.js`
- Python: `test_[name].py`

### C. Configuration Files

**Rule:** Root or `.config/` depending on type.

```
Project-wide config → Root
  .eslintrc, tsconfig.json, package.json
  
Tool-specific config → .config/ or .[tool]/
  .github/, .vscode/, .agents/
```

### D. Documentation

**Rule:** Group by audience.

```
User docs → docs/
  docs/getting-started.md, docs/api.md
  
Developer docs → docs/ or root
  CONTRIBUTING.md, ARCHITECTURE.md (root)
  
Context docs → context/
  context/ARCHITECTURE.md, context/RULES.md
```

### E. Assets & Resources

**Rule:** Type-specific folders.

```
Images → assets/images/ or public/images/
Styles → styles/ or src/styles/
Libraries → libraries/ or lib/
```

---

## 3. Related File Discovery

**Scan these in order:**

```bash
1. Exact name match (case-insensitive)
2. Partial name match (shared words)
3. Same file type in project
4. Parent directory pattern
```

**Example:**
```
Creating: EmailValidator.java

Scan results:
✓ Found: FormValidator.java in src/utils/validators/
✓ Pattern: *Validator.java
→ Place: src/utils/validators/EmailValidator.java
```

---

## 4. Auto-Detection Algorithm

**For every new file:**

```
1. Extract file characteristics
   - Type: .java, .js, .md, .json
   - Category: Service, Controller, Test, Config, Doc
   - Feature: auth, user, payment (from filename)

2. Search existing files (glob)
   - Same category: **/*[Category].*
   - Same feature: **/*[feature]*.*
   - Same type: **/*.[type]

3. Determine location
   - If matches found → Use their directory
   - If no matches → Use convention (see §2)
   - Create parent dirs if needed

4. Verify no collision
   - Check file doesn't already exist
   - If exists → Append number or ask user
```

---

## 5. Project-Specific Conventions

### Android (Java)

```
Source: app/src/main/java/[package]/[type]/
Tests: app/src/test/java/[package]/[type]/
Resources: app/src/main/res/[type]/
```

### Web (React/Vue/Angular)

```
Components: src/components/[feature]/
Services: src/services/
Utils: src/utils/
Styles: src/styles/
Tests: src/__tests__/ or beside source
```

### Backend (Node/Python/Java)

```
Controllers: src/controllers/
Services: src/services/
Models: src/models/
Utils: src/utils/
Tests: tests/ (mirror structure)
```

### Docs

```
User-facing: docs/
Context: context/
Skills: .agents/skills/[skill-name]/
```

---

## 6. Smart Grouping

**Group related files together:**

```
Feature-based (preferred):
src/
  auth/
    AuthController.java
    AuthService.java
    AuthValidator.java
  user/
    UserController.java
    UserService.java

Type-based (if existing):
src/
  controllers/
    AuthController.java
    UserController.java
  services/
    AuthService.java
    UserService.java
```

**Detection:** Scan 3 existing files. If 2+ follow same pattern, use that pattern.

---

## 7. Examples

**Ex 1: New Java class**
```
Task: Create PaymentService
Scan: Find OrderService.java at src/services/
Action: Create src/services/PaymentService.java
Reason: Follow existing *Service pattern
```

**Ex 2: New test**
```
Task: Test PaymentService
Source: src/services/PaymentService.java
Action: Create tests/services/PaymentServiceTest.java
Reason: Mirror source structure
```

**Ex 3: New config**
```
Task: Create ESLint config
Pattern: Project-wide linting
Action: Create .eslintrc.json at root
Reason: Project-wide configs go to root
```

**Ex 4: New doc**
```
Task: Document API endpoints
Audience: Developers
Action: Create docs/api-reference.md
Reason: Developer docs go to docs/
```

**Ex 5: New skill**
```
Task: Create new agent skill
Existing: .agents/skills/pragmatic-engineering/
Action: Create .agents/skills/[new-skill]/SKILL.md
Reason: Follow .agents/skills/ convention
```

---

## 8. Conflict Resolution

**If file exists:**
```
1. Check if intentional overwrite (rare)
2. If new version → Suggest [name]-v2 or [name]-new
3. If test for existing source → Confirm correct
4. If duplicate → Alert user, ask for clarification
```

**If location ambiguous:**
```
1. Prefer established pattern (where similar files are)
2. If no pattern → Use convention (§5)
3. If still unclear → Choose most specific location
4. Never ask user unless true conflict
```

---

## 9. Integration with Projects

**RUNOVA example:**
```
Creating: TimerService.java
Scan: Find DateUtils.java, DbHelper.java at app/src/.../utils/
Action: Create at app/src/.../utils/TimerService.java
Reason: Utility class, group with other utils
```

**Agent-spec example:**
```
Creating: New runtime adapter
Scan: Find claude.md, cursor.md at spec/runtime/
Action: Create at spec/runtime/[new-tool].md
Reason: Runtime adapters grouped together
```

---

## 10. Token-Saving Notes

**Agent should:**
- ✓ Auto-detect location (no user prompt)
- ✓ Scan silently (no verbose output)
- ✓ Place file directly (no explanation unless asked)
- ✓ Only mention location if non-obvious or created new directory

**Agent should NOT:**
- ✗ List all scan results
- ✗ Explain every decision
- ✗ Ask where to place (figure it out)
- ✗ Repeat directory structures

**Output format:**
```
Created: src/services/PaymentService.java
```

Not:
```
I scanned the project and found services at src/services/.
I noticed OrderService.java follows the *Service pattern.
Therefore, I will place PaymentService.java at src/services/.
Creating file now...
File created successfully at src/services/PaymentService.java.
```

---

## 11. Quick Reference

**Scan command:**
```bash
# Find related files
find . -name "*[Pattern]*" -type f | head -5
```

**Decision:**
```
Related files found → Use their directory
No related files → Use convention (§5)
Create parent dirs → Auto-create with mkdir -p
```

**Report:**
```
Created: [path]
```

Done. Silent. Efficient.

---

## 12. Cross-References

- Project structure: See project's `ARCHITECTURE.md`
- Naming conventions: See project's `RULES.md`
- File types: See project's language/framework conventions

---

**Last Updated:** 2026-09-24  
**Skill Version:** 1.0  
**Token Target:** <500 tokens per use

---

**Use when:** Creating any new file. Always. Automatically. Silently determine location, place file, confirm briefly.
