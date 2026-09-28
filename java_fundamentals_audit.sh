#!/bin/bash
# ============================================================
# Java Fundamentals & OOP Coverage Audit
# ------------------------------------------------------------
# Companion to android_concept_audit.sh. This one checks the
# underlying Java language fundamentals and OOP concepts used
# in your app's backend/logic/database layer — not the Android
# framework parts (Activities, Fragments, etc.), which the
# other script already covers.
#
# USAGE:
#   ./java_fundamentals_audit.sh /path/to/your/project
#   (defaults to current directory if no path given)
# ============================================================

ROOT="${1:-.}"

if [ ! -d "$ROOT" ]; then
  echo "Error: '$ROOT' is not a valid directory."
  exit 1
fi

PASS="✅"
FAIL="❌"

TOTAL=0
FOUND=0

check() {
  local label="$1"
  local count="$2"
  local hint="$3"
  TOTAL=$((TOTAL+1))
  if [ "$count" -gt 0 ]; then
    FOUND=$((FOUND+1))
    printf "%s %-42s (%s match%s)\n" "$PASS" "$label" "$count" "$([ "$count" -eq 1 ] && echo "" || echo "es")"
  else
    printf "%s %-42s %s\n" "$FAIL" "$label" "$hint"
  fi
}

SRC_FILES=$(find "$ROOT" -type f -name "*.java")

if [ -z "$SRC_FILES" ]; then
  echo "No .java files found under '$ROOT'. Nothing to audit."
  exit 1
fi

echo "============================================================"
echo " JAVA FUNDAMENTALS & OOP COVERAGE AUDIT"
echo " Project: $ROOT"
echo "============================================================"

# ---------------- Core Syntax & Structure ----------------
echo ""
echo "-- Core Syntax --"

check "Method declarations" \
  "$(grep -rlE '(public|private|protected)[[:space:]]+[A-Za-z<>\[\]]+[[:space:]]+[A-Za-z_]+\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no method signatures detected"

check "Variable declarations" \
  "$(grep -rlE '(int|double|float|long|boolean|char|String)[[:space:]]+[A-Za-z_][A-Za-z0-9_]*[[:space:]]*=' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no primitive/String variable assignments found"

check "Primitive data types (int/double/boolean/etc.)" \
  "$(grep -rlE '\b(int|double|float|long|boolean|char|short|byte)\b' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no primitive types used"

check "Operators (arithmetic/logical/comparison)" \
  "$(grep -rlE '[+\-*/%]=|==|!=|&&|\|\||<=|>=' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no operator usage detected"

# ---------------- Control Flow ----------------
echo ""
echo "-- Control Flow --"

check "if / else statements" \
  "$(grep -rlE '\bif[[:space:]]*\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no conditional branching found"

check "switch statements" \
  "$(grep -rl 'switch[[:space:]]*(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no switch statements (optional, if/else may cover this)"

check "for loops" \
  "$(grep -rlE '\bfor[[:space:]]*\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no for loops found"

check "while / do-while loops" \
  "$(grep -rlE '\bwhile[[:space:]]*\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no while loops found"

# ---------------- Data Structures ----------------
echo ""
echo "-- Arrays & Collections --"

check "Arrays ([] declarations)" \
  "$(grep -rlE '[A-Za-z_]+\[\][[:space:]]*[A-Za-z_]+|new[[:space:]]+[A-Za-z_]+\[' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no array declarations found"

check "ArrayList / List usage" \
  "$(grep -rlE '\bArrayList\b|\bList<' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no ArrayList/List usage (common for dynamic collections, e.g. RecyclerView data)"

check "HashMap / Map usage" \
  "$(grep -rlE '\bHashMap\b|\bMap<' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no HashMap/Map usage (optional, useful for key-value data)"

# ---------------- User Input & I/O ----------------
echo ""
echo "-- User Input & File Handling --"

check "User input capture (EditText/Scanner)" \
  "$(grep -rlE 'EditText|Scanner\(|getText\(\)' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no user input capture found"

check "File I/O (FileReader/FileWriter/Streams)" \
  "$(grep -rlE 'FileReader|FileWriter|FileInputStream|FileOutputStream|BufferedReader|BufferedWriter' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no file handling code (skip if app doesn't read/write raw files)"

check "Try/catch exception handling" \
  "$(grep -rl 'try[[:space:]]*{' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no exception handling found -- risky for file/DB/network operations"

# ---------------- OOP Concepts ----------------
echo ""
echo "-- Object-Oriented Programming --"

check "Class declarations" \
  "$(grep -rlE '\bclass[[:space:]]+[A-Za-z_]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no class declarations (unexpected in a Java project)"

check "Object instantiation (new keyword)" \
  "$(grep -rl 'new[[:space:]]\+[A-Za-z_]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no object instantiation found"

check "Constructors" \
  "$(grep -rlE 'public[[:space:]]+[A-Za-z_]+[[:space:]]*\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no constructors detected"

check "Encapsulation (private fields + getters/setters)" \
  "$(grep -rlE 'private[[:space:]]+[A-Za-z_<>\[\]]+[[:space:]]+[A-Za-z_]+;' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no private fields found -- check that fields aren't left public"

check "Inheritance (extends)" \
  "$(grep -rl 'extends[[:space:]]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no class inheritance found"

check "Interfaces / implements" \
  "$(grep -rlE '\binterface[[:space:]]|implements[[:space:]]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no interfaces used (common in Adapter/listener patterns)"

check "Polymorphism (@Override)" \
  "$(grep -rl '@Override' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no method overriding found"

check "Abstract classes/methods" \
  "$(grep -rl 'abstract[[:space:]]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no abstract classes/methods (optional depending on design)"

# ---------------- Database / Backend Layer ----------------
echo ""
echo "-- Database & Backend Logic --"

check "SQL statements (SELECT/INSERT/UPDATE/DELETE)" \
  "$(grep -rliE 'SELECT[[:space:]]|INSERT[[:space:]]INTO|UPDATE[[:space:]].*SET|DELETE[[:space:]]FROM' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no raw SQL queries found"

check "ContentValues (SQLite insert/update helper)" \
  "$(grep -rl 'ContentValues' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no ContentValues usage -- skip if using Room instead of raw SQLite"

check "Cursor handling (SQLite query results)" \
  "$(grep -rl 'Cursor' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no Cursor usage found -- skip if using Room"

check "DAO pattern (Data Access Object)" \
  "$(grep -rliE 'class[[:space:]]+[A-Za-z]*DAO|@Dao' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no DAO classes -- optional but good practice for separating DB logic"

# ---------------- Summary ----------------
echo ""
echo "============================================================"
echo " SUMMARY: $FOUND / $TOTAL checks passed"
echo "============================================================"
echo ""
echo "NOTE: Some items are contextual, not mandatory --"
echo "e.g. switch statements, HashMap, abstract classes, and"
echo "file I/O are only expected if your app's logic calls for"
echo "them. Use this alongside android_concept_audit.sh for full"
echo "coverage: that script checks the Android framework layer,"
echo "this one checks the underlying Java language/OOP/DB layer."
