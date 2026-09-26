# Pragmatic Engineering Skill

**Skill Name:** pragmatic-engineering  
**Purpose:** Senior-level engineering approach balancing simplicity with appropriate complexity  
**Domain:** Full-stack development (frontend + backend)  
**Last Updated:** 2026-09-24

---

## Role / Authority

- **Role:** Guides engineering decisions from simple fixes to complex solutions, preventing over-engineering while ensuring appropriate rigor when needed.
- **Authority:** Defines workflow patterns, decision criteria, and learning mechanisms for iterative development.
- **Must not define:** Language-specific syntax, framework-specific patterns (those reference language guides), project-specific requirements (those stay in context/).

---

## 1. Core Philosophy

### 1.1 Simplicity First, Complexity When Justified

**Principle:**
> Use the simplest solution that correctly solves the problem. Add complexity only when simpler approaches fail to meet requirements.

**Not:**
- Over-engineering: Abstract too early, add layers "just in case"
- Under-engineering: Ignore known requirements, create technical debt

**But:**
- Right-engineering: Match solution complexity to problem complexity
- Evolve complexity: Start simple, add sophistication when evidence demands it

### 1.2 Senior Developer Mindset

**Characteristics:**
- **Experience-driven:** Learn from past implementations
- **Context-aware:** Understand current state before changing
- **Future-conscious:** Consider maintenance and evolution
- **Trade-off explicit:** Document why complexity was added or avoided
- **Component-synchronized:** Changes respect system boundaries

---

## 2. The Pragmatic Workflow

### 2.1 Mandatory Six-Step Process

Every engineering task follows this sequence:

```
1. READ      → Understand current state
2. ANALYZE   → Identify problem and constraints
3. PLAN      → Design solution with complexity justification
4. TEST      → Verify approach before full implementation
5. EXECUTE   → Implement solution
6. IMPLEMENT → Integrate, document, and learn
```

**Never skip steps.** Each builds on previous.

---

### Step 1: READ (Understand Current State)

**Objective:** Build complete mental model of existing system before changing anything.

**Actions:**

**A. Read Relevant Code**
```markdown
- Current implementation (the file/component being changed)
- Direct dependencies (what this code calls)
- Direct dependents (what calls this code)
- Related components (similar functionality elsewhere)
```

**B. Read Project Context**
```markdown
- ARCHITECTURE.md → System structure, data flow
- DESIGN.md → UI patterns, component standards
- SCHEMA.md → Data models, validation rules
- RULES.md → Coding standards, conventions
- PRD.md → Requirements, user stories
```

**C. Read Past Implementations**
```markdown
- Git history: `git log --oneline -20 [file]`
- Recent commits: `git log -p -5 [file]`
- Related changes: `git log --grep="[feature]"`
- Past decisions: Check commit messages for "why"
```

**D. Check Current Progress**
```markdown
- Open PRs touching this area
- Recent bug fixes
- Ongoing work by others
- Technical debt notes (TODO, FIXME comments)
```

**Output:** Summary of current state (2-3 paragraphs):
```
Current state of [component]:
- Implemented as [architecture pattern]
- Handles [responsibilities]
- Dependencies: [list]
- Recent changes: [summary from git log]
- Known issues: [from comments/TODOs]
```

---

### Step 2: ANALYZE (Identify Problem & Constraints)

**Objective:** Define problem precisely, identify constraints, determine solution complexity level.

**Actions:**

**A. Problem Definition**
```markdown
What is the actual problem?
- User need: [from PRD or user story]
- Technical gap: [what's missing or broken]
- Root cause: [why current implementation insufficient]
```

**B. Constraints Identification**
```markdown
Hard constraints (cannot violate):
- Performance: [e.g., must load in <500ms]
- Data integrity: [e.g., cannot lose user data]
- Compatibility: [e.g., must work with existing API]
- Security: [e.g., must validate all inputs]

Soft constraints (prefer but flexible):
- Code simplicity: [prefer fewer abstractions]
- Consistency: [match existing patterns]
- Maintainability: [future developers can understand]
```

**C. Complexity Assessment**

**Simple Problem Indicators:**
- Single component affected
- Clear, straightforward solution
- No edge cases beyond standard validation
- Similar to existing implementations
- No performance concerns

**→ Solution: Direct fix, minimal abstraction**

**Complex Problem Indicators:**
- Multiple components interact
- Non-obvious edge cases
- Performance/scalability concerns
- No existing pattern to follow
- Security/data integrity critical

**→ Solution: Considered architecture, appropriate abstractions**

**Output:** Problem analysis:
```
Problem: [1-2 sentence definition]
Complexity Level: Simple | Moderate | Complex
Justification: [why this complexity level]

Hard Constraints:
1. [constraint with rationale]
2. [constraint with rationale]

Soft Constraints:
1. [preference with flexibility]
2. [preference with flexibility]
```

---

### Step 3: PLAN (Design Solution)

**Objective:** Design solution matching problem complexity, justify any added complexity.

**Actions:**

**A. Solution Design by Complexity**

**For Simple Problems:**
```markdown
Direct approach:
1. Identify exact location of change (file, function, line)
2. Describe minimal change needed
3. List affected tests
4. Verify no component synchronization issues

Example:
- Add email validation to signup form
- Change: Add regex check in validateInputs()
- Test: Test empty, invalid, valid emails
- Impact: None (contained in single method)
```

**For Moderate Problems:**
```markdown
Structured approach:
1. Identify components to modify
2. Define interface contracts (what each component expects)
3. Describe data flow between components
4. Plan synchronization points
5. Identify new abstractions (if any) with justification

Example:
- Add "forgot password" feature
- Components: LoginActivity, EmailService, PrefsHelper
- Contract: LoginActivity calls EmailService.sendResetLink(email)
- Flow: User enters email → validate → send link → show confirmation
- Abstraction: EmailService (new) - justified because email will be used
  for multiple features (signup confirmation, password reset, notifications)
```

**For Complex Problems:**
```markdown
Architectural approach:
1. Define system-level requirements
2. Propose architecture pattern (e.g., MVC, Observer, Strategy)
3. Justify pattern choice with trade-offs
4. Design component interactions (sequence diagrams)
5. Plan backward compatibility or migration
6. Identify performance implications
7. Define testing strategy (unit, integration, E2E)

Example:
- Add offline sync with conflict resolution
- Requirements: Sync on network restore, handle conflicts, preserve user data
- Pattern: Event-driven with SyncManager coordinator
- Justification: Decouples network layer from data layer, allows retry logic,
  handles partial failures. Trade-off: More components but clearer responsibilities.
- Interactions: [sequence diagram]
- Migration: Existing data compatible, no schema change
- Performance: Background thread, batch operations
- Testing: Mock network, simulate conflicts, verify data integrity
```

**B. Complexity Justification**

For every abstraction or pattern added, answer:
```
1. What problem does this solve?
2. Why can't a simpler approach work?
3. What is the maintenance cost?
4. Will future features benefit from this?
```

**If answers unclear → Simplify.**

**C. Component Synchronization Check**

```markdown
For each modified component, verify:
1. Does it maintain existing contracts (API unchanged or backward compatible)?
2. Are dependent components aware of changes?
3. Will concurrent changes conflict?
4. Does shared state need locking/coordination?
```

**Output:** Solution plan:
```
Solution: [High-level description]
Complexity Level: [Simple | Moderate | Complex]

Changes:
1. [Component/file]: [What changes]
2. [Component/file]: [What changes]

New Abstractions (if any):
- [Abstraction name]: [Purpose, justification, trade-off]

Component Synchronization:
- [Component A] ↔ [Component B]: [How they coordinate]

Risks:
- [Risk 1]: [Mitigation]
- [Risk 2]: [Mitigation]

Testing Plan:
- [Test type]: [What to verify]
```

---

### Step 4: TEST (Verify Approach)

**Objective:** Validate solution design before full implementation.

**Actions:**

**A. For Simple Problems:**
```markdown
1. Write test cases (inputs → expected outputs)
2. Verify edge cases covered
3. Check existing tests still pass (mental check or quick run)
```

**B. For Moderate Problems:**
```markdown
1. Prototype critical path (minimal implementation)
2. Test component integration points
3. Verify data flow works as designed
4. Check performance (if concern)
5. Run existing test suite
```

**C. For Complex Problems:**
```markdown
1. Build proof-of-concept for riskiest part
2. Write integration tests before implementation
3. Simulate failure modes
4. Performance benchmark baseline
5. Review design with team (if available)
6. Document assumptions to validate
```

**B. Test Execution**

```bash
# Run relevant tests
npm test [module]
pytest tests/[module]
./gradlew test --tests [TestClass]

# Check results
- All existing tests pass?
- New test cases cover changes?
- Edge cases handled?
```

**C. Decision Point**

```
Tests pass + design validated → PROCEED to Execute
Tests fail or design flawed → RETURN to Plan (revise solution)
```

**Output:** Test results:
```
Tests Run: [count]
Tests Passed: [count]
Tests Failed: [count] - [if any, explain why and how plan adjusted]

Design Validation:
- Critical path works: [Yes/No]
- Performance acceptable: [Yes/No with metrics]
- Edge cases handled: [List verified cases]

Decision: PROCEED | REVISE PLAN
```

---

### Step 5: EXECUTE (Implement Solution)

**Objective:** Implement planned solution, maintaining code quality and documentation.

**Actions:**

**A. Implementation Order**

**For Simple Problems:**
```
1. Make the change
2. Add inline comments (if logic non-obvious)
3. Update related comments (remove outdated)
4. Format code (follow RULES.md)
```

**B. For Moderate Problems:**
```
1. Implement lowest-level components first (no dependencies)
2. Implement higher-level components (depend on above)
3. Wire components together
4. Add logging/error handling
5. Update documentation (inline + README if needed)
6. Format and review code
```

**C. For Complex Problems:**
```
1. Implement core abstractions (interfaces, base classes)
2. Implement concrete components
3. Add integration layer
4. Add error handling and recovery
5. Add logging and monitoring hooks
6. Write comprehensive documentation
7. Create architecture diagram (if needed)
8. Code review (self-review checklist)
```

**B. Code Quality Checks**

```markdown
During implementation:
- [ ] Follows naming conventions (RULES.md)
- [ ] No hardcoded values (use constants/config)
- [ ] Error handling for all failure modes
- [ ] Input validation at trust boundaries
- [ ] Logging at key decision points
- [ ] Comments explain "why", not "what"
- [ ] No duplicate code (DRY principle)
- [ ] Functions single-purpose (SRP principle)
- [ ] No premature optimization
```

**C. Component Synchronization**

```markdown
Verify during implementation:
- Shared state: Is access thread-safe (if needed)?
- API contracts: Do interfaces match design?
- Data formats: Are serialization/deserialization correct?
- Event ordering: Are async operations coordinated?
```

**Output:** Implemented code with:
```
Files Changed:
- [file path]: [summary of changes]
- [file path]: [summary of changes]

Lines Added/Removed: +X / -Y

Documentation Updated:
- [file/section]: [what was added/changed]

Self-Review Completed: Yes
Quality Checks Passed: Yes
```

---

### Step 6: IMPLEMENT (Integrate & Learn)

**Objective:** Integrate changes into codebase, verify system health, document learnings.

**Actions:**

**A. Integration**

```bash
# 1. Run full test suite
npm test
pytest
./gradlew test

# 2. Lint/format check
npm run lint
black .
./gradlew ktlintCheck

# 3. Build verification
npm run build
mvn clean install
./gradlew build

# 4. Integration testing (if applicable)
npm run test:integration
pytest tests/integration
```

**B. Verification Checklist**

```markdown
- [ ] All tests pass (unit, integration, E2E)
- [ ] No new linter errors
- [ ] Build succeeds
- [ ] No regression in existing features (manual smoke test)
- [ ] Performance metrics within acceptable range
- [ ] Documentation updated (README, architecture docs)
- [ ] Commit message clear and descriptive
```

**C. Learning Capture**

**Document learnings for future reference:**

```markdown
## Implementation Learning Log

**Feature:** [Name]
**Date:** [YYYY-MM-DD]
**Complexity:** [Simple | Moderate | Complex]

### What Worked Well:
- [Decision/approach that was successful]
- [Pattern that should be reused]

### What Didn't Work:
- [Approach that failed or was revised]
- [Complexity that should have been avoided]

### Key Insights:
- [Technical learning]
- [Architectural insight]
- [Team/process learning]

### Future Improvements:
- [What to do differently next time]
- [Technical debt created (with ticket reference)]

### Reusable Patterns:
- [Pattern that can be applied elsewhere]
- [Abstraction that proved useful]
```

**Store in:**
- Project wiki
- `docs/learnings/YYYY-MM-DD-[feature].md`
- Or inline in commit message (for smaller changes)

**D. Knowledge Transfer**

```markdown
Update project knowledge:
1. Add to ARCHITECTURE.md (if architectural change)
2. Add to DESIGN.md (if new UI pattern)
3. Add to SCHEMA.md (if data model change)
4. Add to RULES.md (if new coding convention)
5. Update skills/ (if new reusable pattern)
```

**Output:** Integration complete:
```
Commit: [hash] [message]
Tests: All passing
Build: Success
Documentation: Updated
Learning Log: Created/Updated

Learnings Summary:
- [Key insight 1]
- [Key insight 2]
- [Pattern to reuse: X]
- [Anti-pattern to avoid: Y]
```

---

## 3. Complexity Decision Framework

### 3.1 When to Keep It Simple

**Use simplest approach when:**
- ✓ Single use case, no planned variations
- ✓ Performance requirements easily met
- ✓ Existing pattern covers this case
- ✓ Low risk of change
- ✓ Small scope (single function/component)

**Examples:**
- Add field to form → Just add field, no abstraction
- Fix typo in calculation → Just fix it
- Update static text → Just update it
- Add validation rule → Add to existing validation function

### 3.2 When to Add Abstraction

**Add abstraction when:**
- ✓ Multiple implementations of same concept
- ✓ Variation anticipated (not speculated, actually planned)
- ✓ Testing requires isolation (mocking)
- ✓ Shared logic across components
- ✓ Encapsulation improves understanding

**Examples:**
- Multiple data sources → Repository pattern
- Different export formats → Strategy pattern
- Reusable UI component → Extract component
- Shared validation logic → Validation service

**Justify abstraction:**
```
Abstraction: [Name]
Solves: [Specific problem]
Why needed: [Evidence: multiple use cases, planned variation, testing need]
Trade-off: [Added complexity vs. benefit]
Alternative considered: [Simpler approach and why it doesn't work]
```

### 3.3 When to Refactor

**Refactor when:**
- ✓ Duplicated code in 3+ places (Rule of Three)
- ✓ Function >50 lines or >3 levels deep
- ✓ Class >500 lines or >10 public methods
- ✓ Tests impossible to write (tight coupling)
- ✓ New feature requires fighting existing structure

**Don't refactor when:**
- ✗ "It could be cleaner" (no concrete problem)
- ✗ Code works and no changes planned
- ✗ Different style preference (no functional improvement)
- ✗ Speculative future needs

**Refactoring is a task in itself, not bundled with features.**

---

## 4. Component Synchronization Rules

### 4.1 Identify Component Boundaries

**Component = Unit with single responsibility:**
- Module/package
- Class with clear API
- Service/utility
- UI component

**Boundaries defined by:**
- Public interface (what others can call)
- Private implementation (internal details)
- Contracts (inputs → outputs)

### 4.2 Synchronization Checklist

Before changing a component:

```markdown
1. Who depends on this component?
   - Direct callers: [list]
   - Indirect dependents: [list]

2. Does this change break contracts?
   - API signature changed: [Yes/No]
   - Data format changed: [Yes/No]
   - Behavior changed: [Yes/No]

3. How will dependents learn of change?
   - Compile error (breaking change): [if applicable]
   - Runtime error (incompatible data): [if applicable]
   - Silent breakage (changed behavior): [AVOID THIS]

4. Coordination needed?
   - Update multiple components atomically: [Yes/No]
   - Migration path for breaking changes: [describe]
   - Deprecation period: [if needed]
```

### 4.3 Communication Patterns

**A. Synchronous Components (tight coupling)**
```
Frontend → Backend API

Synchronization:
- API contract (OpenAPI, GraphQL schema)
- Version negotiation (API v1, v2)
- Breaking changes require coordinated deploy
```

**B. Asynchronous Components (loose coupling)**
```
Service A → Event Bus → Service B

Synchronization:
- Event schema (versioned)
- Service B handles old/new event formats
- No coordinated deploy needed
```

**C. Shared Data (state synchronization)**
```
Component A ← Database → Component B

Synchronization:
- Database schema versioning
- Migration scripts
- Both components handle old/new schemas during transition
```

---

## 5. Learning from Past Implementations

### 5.1 Before Starting New Work

**Query past implementations:**

```bash
# 1. Find similar features
git log --all --grep="[feature-keyword]"
git log --all --oneline -- [related-file]

# 2. Read relevant commits
git show [commit-hash]
git diff [commit-hash]^ [commit-hash]

# 3. Check for patterns
git log --oneline --all -- "**/*[pattern]*"
```

**Extract learnings:**
```markdown
Similar past implementations:
1. [Feature A] (commit: [hash])
   - Approach: [summary]
   - Outcome: [success/failure/refactored]
   - Lesson: [what to do or avoid]

2. [Feature B] (commit: [hash])
   - Approach: [summary]
   - Outcome: [success/failure/refactored]
   - Lesson: [what to do or avoid]

Apply to current task:
- Pattern to reuse: [specific pattern]
- Anti-pattern to avoid: [specific mistake]
- Complexity justified: [based on past evidence]
```

### 5.2 Pattern Recognition

**Look for:**
- Repeated abstractions → Reusable pattern
- Repeated fixes → Systemic issue (refactor root cause)
- Repeated rollbacks → Approach doesn't fit system
- Repeated success → Codify as team standard

**Document patterns:**
```markdown
## Reusable Pattern: [Name]

**Context:** When to use this pattern
**Problem:** What problem it solves
**Solution:** How to implement it
**Evidence:** Past implementations where this worked
**Trade-offs:** Complexity added, maintenance cost
**Example:** Code snippet or reference commit
```

Store in `docs/patterns/` or project wiki.

### 5.3 Continuous Improvement

**After each implementation:**

1. **Review:**
   - What complexity was necessary?
   - What complexity could have been avoided?
   - Did plan match execution?
   - Were tests sufficient?

2. **Update documentation:**
   - Add new pattern to reusable patterns
   - Update anti-patterns list
   - Refine complexity decision criteria

3. **Share knowledge:**
   - Team discussion (retrospective)
   - Update onboarding docs
   - Create skill document (like this one)

---

## 6. Anti-Over-Engineering Checklist

Before adding complexity, verify:

```markdown
- [ ] Problem requires this complexity (not speculative)
- [ ] Simpler approaches considered and documented as insufficient
- [ ] Abstraction used in 2+ places already (or planned in scope)
- [ ] Future benefit clear and likely (not "might need someday")
- [ ] Maintenance cost justified by benefit
- [ ] Team can understand and maintain this
- [ ] Documentation explains why complexity necessary
```

**If any unchecked → Simplify.**

---

## 7. Senior Developer Signals

### 7.1 Good Engineering Decisions

**Signs of appropriate complexity:**
- Code is obvious and boring (no cleverness for cleverness sake)
- Abstractions have 2+ concrete implementations
- Future developers can understand without deep context
- Tests are simple and fast
- Performance measured, not guessed
- Trade-offs explicit in comments/docs

### 7.2 Over-Engineering Signals

**Warning signs:**
- Abstractions with single implementation
- Layers of indirection for "flexibility" never used
- Configuration for values that never change
- Premature optimization without measurement
- Complex patterns for simple problems
- "Enterprise-grade" solutions for small features

**Fix:** Simplify aggressively.

### 7.3 Under-Engineering Signals

**Warning signs:**
- Copy-pasted code in 3+ places
- Long functions (>50 lines) doing multiple things
- Tight coupling (can't test without entire system)
- No error handling or validation
- Hardcoded values throughout
- No documentation for non-obvious logic

**Fix:** Refactor toward clarity and maintainability.

---

## 8. Workflow Examples

### 8.1 Example: Simple Fix

**Task:** Add email format validation to signup form

**READ:**
- Current: SignupActivity has validateInputs() method
- Validation pattern: try-catch with setError()
- No existing email validation

**ANALYZE:**
- Problem: Users entering invalid emails
- Complexity: Simple (single method, single validation)
- Constraint: Must match existing validation pattern

**PLAN:**
- Add email regex check in validateInputs()
- Use same error display pattern (setError())
- No abstraction needed (single use case)

**TEST:**
```java
// Test cases:
- Empty email → error
- "invalid" → error  
- "user@domain" → error (no TLD)
- "user@domain.com" → pass
```

**EXECUTE:**
```java
// In validateInputs() method:
String email = emailInput.getText().toString().trim();
if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
    emailInput.setError("Please enter a valid email address");
    return false;
}
```

**IMPLEMENT:**
- All tests pass
- No other components affected
- Learning: Email regex pattern works, can reuse for future email fields

**Complexity justified:** None added. Direct fix.

---

### 8.2 Example: Moderate Feature

**Task:** Add export user data to JSON

**READ:**
- Current: Profile stored in SharedPreferences
- No existing export functionality
- Similar pattern: Profile serialization for preferences

**ANALYZE:**
- Problem: User wants to backup data
- Complexity: Moderate (new feature, file I/O, JSON formatting)
- Constraints: Must include all user data, JSON format required

**PLAN:**
- New method: ExportHelper.exportToJson(Profile, Set<String> completedDates)
- Returns JSON string
- Existing profile serialization can be reused
- File writing handled by Android external storage API
- No abstraction for export formats yet (only JSON in scope)

**TEST:**
```java
// Test ExportHelper:
- Profile with all fields → valid JSON
- Empty completedDates → empty array in JSON
- Parse exported JSON → matches original data
```

**EXECUTE:**
```java
public class ExportHelper {
    public static String exportToJson(Profile profile, Set<String> completedDates) {
        JSONObject json = new JSONObject();
        json.put("profile", profileToJson(profile));
        json.put("completedDates", new JSONArray(completedDates));
        return json.toString(2); // Pretty print
    }
    
    private static JSONObject profileToJson(Profile profile) {
        JSONObject obj = new JSONObject();
        obj.put("name", profile.getName());
        // ... all fields
        return obj;
    }
}
```

**IMPLEMENT:**
- Tests pass
- Integrated with ProfileActivity (Export button)
- File saved to Downloads/
- Learning: JSON serialization straightforward, no need for Jackson/Gson for simple case

**Complexity justified:** None added beyond necessary JSON formatting. Direct implementation sufficient.

---

### 8.3 Example: Complex Feature

**Task:** Add offline sync with conflict resolution

**READ:**
- Current: No sync, all data local
- No network layer exists
- No conflict resolution strategy

**ANALYZE:**
- Problem: Users want data on multiple devices
- Complexity: Complex (new architecture, distributed state, conflicts)
- Constraints: Must preserve local data, handle network failures, resolve conflicts

**PLAN:**
- Architecture: Event-driven sync with SyncManager
- Components:
  - SyncManager (coordinator)
  - NetworkClient (HTTP communication)
  - ConflictResolver (strategy pattern: last-write-wins, manual, custom)
  - SyncState (tracks sync status)
- Data flow: LocalChange → SyncQueue → NetworkClient → Server → ConflictCheck → LocalUpdate
- Justification: Complexity necessary for distributed system, no simpler approach handles conflicts

**TEST:**
- Unit: SyncManager queues changes correctly
- Unit: ConflictResolver strategies work
- Integration: Mock server, simulate conflicts
- E2E: Two devices, make conflicting changes, verify resolution

**EXECUTE:**
(Implement all components, add error handling, logging, recovery)

**IMPLEMENT:**
- All tests pass
- Integration verified
- Documentation: Architecture diagram, conflict resolution algorithm
- Learning: 
  - Last-write-wins sufficient for V1, manual resolution can wait
  - Event-driven architecture cleaner than callback hell
  - SyncManager pattern reusable for future sync features

**Complexity justified:** Yes. Distributed sync requires coordination, conflict resolution, error recovery. Simpler approaches (blind overwrite) lose user data.

---

## 9. Integration with RUNOVA

### 9.1 Apply to RUNOVA Development

**For every RUNOVA task:**

1. **READ** RUNOVA context:
   - `context/ARCHITECTURE.md` → Current system
   - `context/DESIGN.md` → UI patterns
   - `context/SCHEMA.md` → Data models
   - `context/RULES.md` → Coding standards
   - `context/PRD.md` → Requirements

2. **ANALYZE** using RUNOVA constraints:
   - Offline-first: No network calls
   - Zero dependencies: No libraries
   - SharedPreferences only: No database
   - MVC architecture: Respect layer boundaries

3. **PLAN** with RUNOVA patterns:
   - Follow existing validation patterns
   - Reuse PrefsHelper for storage
   - Match design system (colors, spacing)
   - Use existing Activity structure

4. **TEST** against RUNOVA requirements:
   - Works in airplane mode
   - Data persists across restarts
   - Follows design system
   - Validates inputs

5. **EXECUTE** following RUNOVA rules:
   - Java naming conventions
   - Resource naming (no hardcoded values)
   - Error handling patterns
   - Component synchronization

6. **IMPLEMENT** and update RUNOVA docs:
   - Update context/ if architecture changed
   - Add pattern to skills/ if reusable
   - Document in learning log

### 9.2 RUNOVA-Specific Examples

**Simple:** Add height unit toggle (cm/inches)
- READ: Current height stored as cm in Profile
- ANALYZE: Simple conversion, no data model change
- PLAN: Add toggle, convert on display/save
- Execute direct fix

**Moderate:** Add weekly summary email
- READ: No email capability exists
- ANALYZE: Requires EmailService abstraction (future use for notifications)
- PLAN: Create EmailService, format summary, integrate with AnalyticsActivity
- Abstraction justified: Email reusable for multiple features

**Complex:** Add multi-event training (train for 5K and 10K simultaneously)
- READ: Current data model assumes single active event
- ANALYZE: Requires Profile → Profiles, target calculation changes, UI changes
- PLAN: Refactor data model, update all dependencies, migration path
- Complexity justified: Fundamental architecture change needed

---

## 10. Success Criteria

### 10.1 You're Doing It Right When:

- ✓ Code reads like prose (minimal mental overhead)
- ✓ Abstractions justify themselves (2+ implementations)
- ✓ Tests are fast and clear
- ✓ New developers understand quickly
- ✓ Changes are localized (few files touched)
- ✓ Documentation explains "why", not "what"
- ✓ Past learnings inform current decisions
- ✓ Component boundaries clear and respected

### 10.2 Red Flags:

- ✗ "We might need this later" (speculative complexity)
- ✗ Layers of indirection without clear benefit
- ✗ Can't explain why abstraction needed
- ✗ Tests harder to write than production code
- ✗ Changes ripple across many files
- ✗ Documentation says "this is complex because..."
- ✗ Repeating past mistakes (not learning)
- ✗ Components tightly coupled (can't change independently)

---

## 11. Quick Reference

### Complexity Decision Tree

```
Problem to solve
    ↓
Is there an existing pattern? ──Yes→ USE IT
    ↓ No
Is it a single, isolated change? ──Yes→ DIRECT FIX
    ↓ No
Does it affect 2+ components? ──Yes→ MODERATE (design interfaces)
    ↓ No
Does it need new architecture? ──Yes→ COMPLEX (justify carefully)
    ↓ No
Am I over-thinking this? ──Yes→ GO BACK TO DIRECT FIX
```

### Six-Step Workflow

```
1. READ    → Current state + past implementations
2. ANALYZE → Problem + constraints + complexity level
3. PLAN    → Solution + justify complexity + sync check
4. TEST    → Verify approach before full implementation
5. EXECUTE → Implement with quality checks
6. IMPLEMENT → Integrate + document + learn
```

### Senior Developer Mantra

```
"Make it work, make it right, make it fast — in that order.
And only make it fast if measurement proves it's slow."
```

---

## 12. Cross-References

- **Architecture patterns:** See project's `context/ARCHITECTURE.md`
- **Design patterns:** See project's `context/DESIGN.md`
- **Data patterns:** See project's `context/SCHEMA.md`
- **Coding standards:** See project's `context/RULES.md`
- **Requirements validation:** See project's `context/PRD.md`
- **Autonomous development:** See `spec/skills/autonomous-dev/`
- **Testing workflows:** See `spec/skills/dev-workflow/testing/`

---

**Last Updated:** 2026-09-24  
**Maintained By:** Agent-spec community  
**Skill Version:** 1.0  
**Status:** Production-ready

---

## Token Efficiency

**Activation:** Only when implementing features or making architectural decisions. Not for simple questions or small edits.

**Operation:** Background execution. Follow READ → ANALYZE → PLAN → TEST → EXECUTE → IMPLEMENT silently. Don't narrate each step.

**Output:** 
- Report key decisions only (complexity level, abstractions added)
- Show final code
- Brief confirmation (1-2 lines)
- No verbose process descriptions

**Budget:** <1000 tokens per use (vs. 3000+ tokens if narrating entire workflow)

**Deactivation:** Automatic after implementation complete.

**Example output:**
```
Analyzed: Moderate complexity (new feature, 2 components)
Created: src/services/PaymentService.java
Created: tests/services/PaymentServiceTest.java
Pattern: Direct implementation (no abstraction needed, single use case)
```

Not:
```
I'm now using the pragmatic-engineering skill to implement this feature.
STEP 1 - READ: I will scan the current codebase...
[20 lines of scan results]
STEP 2 - ANALYZE: Based on the complexity framework...
[15 lines of analysis explanation]
...
```

---

**Use this skill when:** Engineering any feature (simple fix to complex system), making architectural decisions, reviewing code for appropriate complexity, mentoring junior developers on pragmatic engineering.
