# Smart Clarification Skill

**Role:** Ask strategic questions ONLY when instruction is complex/ambiguous and lacks critical details  
**Trigger:** Automatically by skill-manager when detecting high-risk ambiguity  
**Purpose:** Ensure correct implementation approach before significant work, avoiding costly mistakes

---

## Core Principle

**Ask questions ONLY when:**
- Task is large/complex (multiple files, architecture change)
- Instruction lacks critical technical details
- Multiple valid approaches exist (choice needed)
- High risk of wrong direction (costly to redo)

**Never ask when:**
- Task is clear and straightforward
- Existing patterns provide answer
- Context files have the information
- Question would delay simple tasks unnecessarily

---

## 1. Activation Rules

### A. ALWAYS Proceed Without Asking

```
✓ Simple tasks (single file, small change)
✓ Clear instructions (what, where, how specified)
✓ Existing pattern covers it (follow pattern)
✓ Context docs have answer (check ARCHITECTURE, DESIGN, RULES first)
✓ Low-risk changes (easy to adjust if needed)
```

**Examples that DON'T need questions:**
- "Add email validation to signup"
- "Fix typo in line 42"
- "Create PaymentActivity" (pattern exists)
- "Change button color to blue"
- "Add logging to UserService"

### B. ASK When These Conditions BOTH True

```
1. HIGH COMPLEXITY:
   - Multiple files affected (5+)
   - Architecture/design system change
   - New technology/library introduction
   - System-wide impact

2. HIGH AMBIGUITY:
   - Multiple valid approaches
   - Critical technical choice unclear
   - Trade-offs not specified
   - Scope boundaries unclear
```

**Examples that NEED questions:**
- "Add user authentication" (OAuth? JWT? Local? Which library?)
- "Refactor to MVVM" (Migrate all? Phase by phase? Keep MVC where?)
- "Add offline sync" (Conflict resolution strategy? Queue? Background?)
- "Implement payment system" (Stripe? PayPal? In-app? PCI compliance?)

---

## 2. Ambiguity Detection

### A. Analyze Instruction

**Extract:**
```
Scope: Small | Medium | Large | System-wide
Clarity: Clear | Somewhat clear | Ambiguous | Very ambiguous
Technical decisions: Specified | Partially specified | Unspecified
Risk: Low | Medium | High | Critical
```

### B. Detection Matrix

| Scope | Clarity | Technical Decisions | Risk | Action |
|-------|---------|---------------------|------|--------|
| Small | Clear | Specified | Low | PROCEED |
| Medium | Clear | Specified | Low-Medium | PROCEED |
| Medium | Ambiguous | Partially specified | Medium | ASK 1-2 questions |
| Large | Ambiguous | Unspecified | High | ASK 3-5 questions |
| System-wide | Any | Unspecified | Critical | ASK essential questions |

---

## 3. Question Framework

### A. Question Categories

**Only ask about:**

1. **Approach** (if multiple valid ways)
   - "Use REST API or GraphQL?"
   - "Repository pattern or direct data access?"
   
2. **Technology** (if not specified)
   - "Which authentication library? (OAuth, Firebase Auth, custom JWT)"
   - "SQLite or Room database?"
   
3. **Scope Boundaries** (if unclear)
   - "Migrate all screens or start with login flow?"
   - "Support both portrait and landscape?"
   
4. **Trade-offs** (if user should decide)
   - "Prioritize speed or security? (Affects caching strategy)"
   - "Offline-first (more storage) or online-dependent (less storage)?"

### B. Question Quality Rules

**Good questions:**
```
✓ Specific (not "how should I do this?")
✓ Multiple-choice when possible
✓ Explain why asking (impact on implementation)
✓ Suggest default if appropriate
✓ Max 3-5 questions per session
```

**Bad questions:**
```
✗ Vague ("What do you want?")
✗ Answerable from context docs
✗ Design details (use pattern-reuse)
✗ Excessive (10+ questions = analysis paralysis)
✗ No explanation (user doesn't know why it matters)
```

### C. Question Template

```
[Question category]: [Specific question]

Options:
A. [Option 1] - [Brief description]
B. [Option 2] - [Brief description]  
C. [Option 3] - [Brief description]

Impact: [Why this choice matters]
Recommendation: [Suggest default if applicable]
```

---

## 4. Examples

### Ex 1: Clear Instruction (No Questions)

**User:** "Add email field to signup form with validation"

**Analysis:**
- Scope: Small (1 file, 1 field)
- Clarity: Clear (what, where, requirement specified)
- Technical: Specified (email validation = regex pattern)
- Risk: Low (easy to adjust)

**Decision:** PROCEED without questions. Use existing form pattern + validation pattern.

---

### Ex 2: Moderate Ambiguity (1-2 Questions)

**User:** "Add offline mode to the app"

**Analysis:**
- Scope: Large (multiple components)
- Clarity: Ambiguous (what exactly should work offline?)
- Technical: Unspecified (caching strategy? sync mechanism?)
- Risk: High (wrong approach = significant rework)

**Questions:**

```
1. Offline Scope: Which features should work offline?
   A. Read-only (view cached data, no edits)
   B. Full offline (edit data, sync later)
   C. Hybrid (some features read-only, critical features full offline)
   
   Impact: Determines data sync complexity and storage requirements.
   Recommendation: B (Full offline) - aligns with existing offline-first constraint.

2. Conflict Resolution: When online data differs from offline edits?
   A. Last-write-wins (newer timestamp overwrites)
   B. Manual (show user both versions, let them choose)
   C. Merge (combine changes automatically)
   
   Impact: Affects user experience and data integrity.
   Recommendation: A (Last-write-wins) for simplicity in V1.
```

---

### Ex 3: High Ambiguity (3-5 Questions)

**User:** "Add user authentication system"

**Analysis:**
- Scope: System-wide (affects all screens)
- Clarity: Very ambiguous (method? library? flow?)
- Technical: Unspecified (too many choices)
- Risk: Critical (security, wrong choice = redo entire system)

**Questions:**

```
1. Authentication Method:
   A. Email/password (stored locally, no server)
   B. OAuth (Google, Facebook, etc.)
   C. Email/password + server backend
   
   Impact: Determines architecture, security model, dependencies.
   Recommendation: A - aligns with offline-first constraint.

2. Session Management:
   A. Remember login (user stays logged in)
   B. Timeout after inactivity (re-login required)
   C. Biometric (fingerprint/face if available)
   
   Impact: Affects user experience and security level.
   Recommendation: A for convenience in personal fitness app.

3. Account Recovery:
   A. No recovery (user creates new account if forgot password)
   B. Security questions
   C. Email reset (requires server)
   
   Impact: Affects implementation complexity.
   Recommendation: A for offline-first simplicity.

4. Data Migration:
   A. Require login on first launch (lose existing data)
   B. Migrate existing data to authenticated profile
   C. Optional login (existing users keep using without auth)
   
   Impact: Affects existing user experience.
   Recommendation: B - preserve user data.
```

---

## 5. Integration with Skill Manager

**Activation logic:**

```
User input → Skill Manager analyzes
    ↓
Complexity = Large + Ambiguity = High?
    ↓ Yes
Activate smart-clarification
    ↓
Smart-clarification:
  - Detects missing critical details
  - Formulates 1-5 strategic questions
  - Returns questions to Skill Manager
    ↓
Skill Manager:
  - Pauses other skill activation
  - Presents questions to user
  - Waits for answers
    ↓
User answers
    ↓
Skill Manager:
  - Deactivates smart-clarification
  - Proceeds with implementation skills
  - Passes user choices to implementation
```

---

## 6. Token Efficiency

### A. When to Ask (Saves Tokens Long-Term)

```
Complex task + ambiguity:
  Option 1: Proceed with guess
    → Wrong approach
    → User rejects
    → Redo implementation
    → Total: 5000+ tokens wasted
  
  Option 2: Ask 3 questions first
    → 500 tokens (questions + answers)
    → Correct approach
    → Accepted implementation
    → Total: 2000 tokens
    → Savings: 3000+ tokens + time
```

### B. When NOT to Ask (Saves Tokens Short-Term)

```
Simple task + clear:
  Option 1: Ask questions anyway
    → 300 tokens (unnecessary questions)
    → User annoyed (delays simple task)
    → Total: 500 tokens
  
  Option 2: Just do it
    → 200 tokens (direct implementation)
    → User happy (fast)
    → Total: 200 tokens
    → Savings: 300 tokens + user satisfaction
```

**Key:** Ask questions only when token cost of redoing > token cost of asking.

---

## 7. Question Optimization

### A. Batch Questions (Not Serial)

**Bad (serial, high latency):**
```
Agent: "Which authentication method?"
User: "Email/password"
Agent: "Should I add session timeout?"
User: "No, remember login"
Agent: "What about account recovery?"
User: "No recovery needed"

Total round-trips: 3
Total time: 3x wait
User annoyance: High
```

**Good (batch, low latency):**
```
Agent: "I have 3 questions about authentication:
1. Method: Email/password, OAuth, or Email+server?
2. Session: Remember login, timeout, or biometric?
3. Recovery: None, security questions, or email reset?

Recommendations: Email/password (offline-first), Remember login (convenience), None (simplicity)"

User: "1. Email/password, 2. Remember login, 3. None"

Total round-trips: 1
Total time: 1x wait
User annoyance: Low
```

### B. Provide Context + Recommendations

**Bad (no context):**
```
Agent: "Use SQLite or Room?"

User thinks: "What's the difference? Why does it matter?"
```

**Good (context + recommendation):**
```
Agent: "Database choice:
A. SQLite (direct, no dependencies) - recommended for offline-first
B. Room (ORM, easier queries, but adds dependency)

Impact: Room adds ~50KB to APK and violates zero-dependencies constraint.
Recommendation: A (SQLite) for consistency with project constraints."

User: "A, makes sense"
```

---

## 8. Scope Detection

### A. Small Scope (No Questions)

```
Indicators:
- Single file change
- Adding 1-2 fields/methods
- Following existing pattern
- Bug fix
- Config change

Example: "Add phone field to profile"
Action: PROCEED (use existing form pattern)
```

### B. Medium Scope (Maybe 1-2 Questions)

```
Indicators:
- 2-5 files affected
- New feature (but contained)
- Some design decisions
- Moderate complexity

Example: "Add export data feature"
Check: Format specified? (JSON? CSV? Both?)
  → If clear: PROCEED
  → If ambiguous: ASK 1 question about format
```

### C. Large Scope (3-5 Questions)

```
Indicators:
- 5+ files affected
- New subsystem
- Architecture implications
- Multiple technical choices

Example: "Add multi-user support"
Questions needed:
  1. Sharing model? (device-local accounts or cloud sync)
  2. Data isolation? (separate or shared)
  3. Switching? (logout/login or fast-switch)
```

### D. System-wide (Essential Questions Only)

```
Indicators:
- Entire architecture change
- Migration required
- Breaking changes
- Critical decisions

Example: "Migrate to Kotlin"
Questions needed:
  1. Phased or all-at-once?
  2. Interop with existing Java?
  3. Timeline/priorities?
```

---

## 9. Anti-Patterns to Avoid

### A. Analysis Paralysis

```
✗ Don't ask 20 questions about every detail
✓ Ask 3-5 critical questions that unblock implementation

✗ Don't ask about design details (colors, spacing, etc.)
✓ Use pattern-reuse skill for design consistency

✗ Don't ask questions answerable from context docs
✓ Read ARCHITECTURE, DESIGN, RULES first
```

### B. Premature Questioning

```
✗ Don't ask before analyzing existing patterns
✓ Check pattern-reuse first, ask only if truly ambiguous

✗ Don't ask "What colors should I use?"
✓ Scan existing screens, reuse color scheme

✗ Don't ask "Where should I put this file?"
✓ Use smart-file-placement skill
```

### C. Vague Questions

```
✗ "How should I implement this?"
✓ "Should I use REST API or GraphQL for server communication?"

✗ "What do you want?"
✓ "Should offline edits sync automatically or require manual sync button?"

✗ "Any preferences?"
✓ "Prioritize battery life (background sync off) or data freshness (background sync on)?"
```

---

## 10. Success Metrics

**Good clarification:**
- ✓ Asked when truly needed (high complexity + ambiguity)
- ✓ Questions specific and actionable
- ✓ Multiple-choice with recommendations
- ✓ Batched (1 round-trip)
- ✓ Explained impact of choices
- ✓ Saved tokens long-term (avoided wrong implementation)
- ✓ User satisfied (right questions, not annoying)

**Bad clarification:**
- ✗ Asked for simple tasks (annoying delay)
- ✗ Questions vague or unanswerable
- ✗ No options or recommendations
- ✗ Serial questions (multiple round-trips)
- ✗ No explanation of impact
- ✗ Wasted tokens (asked when pattern-reuse could answer)
- ✗ User frustrated (too many or wrong questions)

---

## 11. Quick Decision Tree

```
Task received
    ↓
Simple + clear? ──Yes→ PROCEED (no questions)
    ↓ No
Check context docs? ──Answers found→ PROCEED
    ↓ No answer
Check existing patterns? ──Pattern exists→ PROCEED (follow pattern)
    ↓ No pattern
Large/complex + ambiguous? ──Yes→ ASK strategic questions (1-5)
    ↓ No
PROCEED with reasonable default
```

---

## 12. Token Budget

**Activation cost:**
- Analysis (complexity + ambiguity detection): 100 tokens
- Question formulation: 200 tokens
- User interaction (Q&A): 200-500 tokens
- Total: 500-800 tokens

**Compared to:**
- Wrong implementation + redo: 3000-5000+ tokens
- Net savings when needed: 2000-4000+ tokens

**When not needed:**
- Overhead: 0 tokens (skill not activated)
- Direct implementation: 200-1000 tokens

**Key:** Only activate when potential savings justify cost.

---

## 13. Integration Example

**User:** "Add social sharing feature"

**Skill Manager analyzes:**
- Scope: Large (multiple screens, external integration)
- Clarity: Ambiguous (which platforms? what to share? how?)
- Risk: High (wrong choice = rework)
- Decision: Activate smart-clarification

**Smart-clarification asks:**

```
I need clarification on the social sharing feature:

1. Platforms: Which social networks should be supported?
   A. Just one (e.g., Twitter only)
   B. Major platforms (Twitter, Facebook, Instagram)
   C. System share sheet (user's installed apps)
   
   Impact: More platforms = more integration code + dependencies.
   Recommendation: C (System share sheet) - zero dependencies, supports all apps.

2. Content: What should users be able to share?
   A. Progress stats only (text)
   B. Stats + screenshot
   C. Custom shareable graphics
   
   Impact: Graphics require generation logic + storage.
   Recommendation: A (Stats only) for simplicity.

3. Trigger: Where can users share?
   A. Share button on every screen
   B. Share from Analytics screen only
   C. After completing session (prompt)
   
   Impact: More triggers = more UI work.
   Recommendation: B (Analytics only) for focused feature.
```

**User:** "C, A, B"

**Skill Manager:**
- Passes choices to implementation skills
- Proceeds with correct approach
- No rework needed

**Token cost:** 600 (questions) + 1500 (implementation) = 2100 total

**vs. guessing wrong:** 2000 (wrong implementation) + 1500 (redo) = 3500 total

**Savings:** 1400 tokens + user satisfaction

---

## 14. Cross-References

- Activation rules: Managed by `skill-manager/SKILL.md`
- Complexity detection: See `pragmatic-engineering/SKILL.md` §2
- Pattern discovery: See `pattern-reuse/SKILL.md` (check before asking)
- Context docs: Always read `context/` before asking questions

---

**Last Updated:** 2026-09-24  
**Skill Version:** 1.0  
**Token Efficiency:** Ask when needed (saves 2000-4000+ tokens), don't ask when not needed (saves 500+ tokens)

---

**DORMANT by default. Activated by skill-manager ONLY when: (Large scope + High ambiguity + Missing critical details). Ask 1-5 strategic questions. Batch questions. Provide context + recommendations. Proceed with confidence after answers received.**
