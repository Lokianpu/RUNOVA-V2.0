#!/bin/bash

# add-to-history.sh
# Script to append new development session to DEVELOPMENT_HISTORY.md
# Usage: ./add-to-history.sh

set -e

HISTORY_FILE="runova/DEVELOPMENT_HISTORY.md"
TIMESTAMP=$(date -u +"%Y-%m-%d %H:%M UTC")

echo "======================================"
echo "  RUNOVA Development History Logger"
echo "======================================"
echo ""

# Check if history file exists
if [ ! -f "$HISTORY_FILE" ]; then
    echo "Error: DEVELOPMENT_HISTORY.md not found at $HISTORY_FILE"
    exit 1
fi

# Prompt for session details
read -p "Enter session number (e.g., 8): " SESSION_NUM
read -p "Enter date (YYYY-MM-DD) [$(/bin/date +%Y-%m-%d)]: " SESSION_DATE
SESSION_DATE=${SESSION_DATE:-$(/bin/date +%Y-%m-%d)}

read -p "Enter phase/title (e.g., 'Bug Fixes + Polish'): " PHASE_TITLE
read -p "Duration estimate (e.g., '2-3 hours'): " DURATION

echo ""
echo "Describe what changed (press ENTER when done, Ctrl+D to finish input):"
CHANGES=$(cat)

echo ""
read -p "List files created/modified (comma-separated): " FILES

echo ""
echo "Explain why these changes were made (press ENTER when done, Ctrl+D to finish input):"
REASONING=$(cat)

echo ""
read -p "Any key decisions made? (optional): " KEY_DECISIONS

# Create session entry
SESSION_ENTRY="
---

## Session $SESSION_NUM: $PHASE_TITLE ($SESSION_DATE)

### Phase: $PHASE_TITLE

**Duration:** $DURATION

**Objective:** $(echo "$CHANGES" | head -n 1)

**Changes Made:**

$CHANGES

**Files Created/Modified:**
\`\`\`
$FILES
\`\`\`

**Why:** $REASONING

**Key Decisions:**
$KEY_DECISIONS

"

# Create backup
cp "$HISTORY_FILE" "$HISTORY_FILE.backup"

# Find insertion point (before "## Current State")
awk -v session="$SESSION_ENTRY" '
/^## Current State/ {
    print session
}
{ print }
' "$HISTORY_FILE.backup" > "$HISTORY_FILE.tmp"

# Update timestamp
sed "s/\*Last Updated: .*\*/\*Last Updated: $TIMESTAMP\*/" "$HISTORY_FILE.tmp" > "$HISTORY_FILE"

# Cleanup
rm "$HISTORY_FILE.tmp"

echo ""
echo "✅ Session $SESSION_NUM added to DEVELOPMENT_HISTORY.md"
echo "✅ Backup saved as DEVELOPMENT_HISTORY.md.backup"
echo "✅ Last Updated timestamp: $TIMESTAMP"
echo ""
echo "Review changes:"
echo "  tail -100 $HISTORY_FILE"
