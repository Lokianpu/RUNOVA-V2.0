#!/bin/bash
# ============================================================
# IS107 Midterm — APPLICATION-ONLY Technical Audit
# ------------------------------------------------------------
# Scope: the app's code/output ONLY. No proposal, no
# flowchart/prototype, no screenshots, no user guide, no
# documentation checks -- purely the specs, technologies,
# and concepts the app itself must implement.
#
# Combines:
#   1) IS107 hard requirements (from the midterm project PDF)
#   2) Android framework fundamentals
#   3) Java language & OOP fundamentals (incl. DB/backend logic)
#
# USAGE:
#   ./is107_app_audit.sh /path/to/your/project
# ============================================================

ROOT="${1:-.}"

if [ ! -d "$ROOT" ]; then
  echo "Error: '$ROOT' is not a valid directory."
  exit 1
fi

PASS="✅"
FAIL="❌"
WARN="⚠️ "

TOTAL=0
FOUND=0

check() {
  local label="$1"
  local count="$2"
  local hint="$3"
  TOTAL=$((TOTAL+1))
  if [ "$count" -gt 0 ]; then
    FOUND=$((FOUND+1))
    printf "%s %-48s (%s match%s)\n" "$PASS" "$label" "$count" "$([ "$count" -eq 1 ] && echo "" || echo "es")"
  else
    printf "%s %-48s %s\n" "$FAIL" "$label" "$hint"
  fi
}

MANIFEST=$(find "$ROOT" -name "AndroidManifest.xml" | head -1)
SRC_FILES=$(find "$ROOT" -type f -name "*.java")
LAYOUT_DIR=$(find "$ROOT" -type d -path "*/res/layout*")
VALUES_DIR=$(find "$ROOT" -type d -path "*/res/values*")
GRADLE_FILES=$(find "$ROOT" -name "build.gradle" -o -name "build.gradle.kts")

if [ -z "$SRC_FILES" ]; then
  echo "No .java files found under '$ROOT'. Nothing to audit."
  exit 1
fi

echo "============================================================"
echo " IS107 APPLICATION-ONLY TECHNICAL AUDIT"
echo " Project: $ROOT"
echo "============================================================"

# ============================================================
# PART 1 — IS107 HARD REQUIREMENTS (from the midterm PDF)
# ============================================================
echo ""
echo "############################################################"
echo "# PART 1: IS107 PROJECT-SPECIFIC REQUIREMENTS"
echo "############################################################"

echo ""
echo "-- Mandatory (non-negotiable) --"

TOTAL=$((TOTAL+1))
if [ -n "$SRC_FILES" ]; then
  FOUND=$((FOUND+1))
  echo "$PASS Built in Java                                -> .java source files present"
else
  echo "$FAIL Built in Java                                -> no .java files found"
fi

ACTIVITY_COUNT=$(grep -c '<activity' "$MANIFEST" 2>/dev/null || echo 0)
TOTAL=$((TOTAL+1))
if [ "$ACTIVITY_COUNT" -ge 5 ]; then
  FOUND=$((FOUND+1))
  echo "$PASS 5+ interconnected Activities                 -> $ACTIVITY_COUNT declared in manifest"
else
  echo "$FAIL 5+ interconnected Activities                 -> only $ACTIVITY_COUNT declared (need $((5-ACTIVITY_COUNT)) more)"
fi

check "Intent-based navigation between Activities" \
  "$(grep -rlE 'new Intent\(|startActivity\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no startActivity()/Intent usage found -- Activities aren't connected"

check "Meaningful input validation" \
  "$(grep -rlE '\.isEmpty\(\)|\.matches\(|\.length\(\)[[:space:]]*[<>=]|setError\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no validation logic found on user input"

check "User interaction (click/input listeners)" \
  "$(grep -rlE 'setOnClickListener|addTextChangedListener|setOnItemSelectedListener' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no interaction handlers found"

check "Styles / Themes applied" \
  "$(find "$VALUES_DIR" -iname "styles.xml" -o -iname "themes.xml" 2>/dev/null | wc -l)" \
  "-> no styles.xml/themes.xml found"

check "Responsive layout technique (ConstraintLayout / weights)" \
  "$(grep -rlE 'ConstraintLayout|layout_weight' $LAYOUT_DIR 2>/dev/null | wc -l)" \
  "-> no ConstraintLayout or weighted layout found"

echo ""
echo "-- Offline-Only Constraint (critical -- app must need ZERO internet) --"

INTERNET_PERM=$(grep -c 'android.permission.INTERNET' "$MANIFEST" 2>/dev/null || echo 0)
TOTAL=$((TOTAL+1))
if [ "$INTERNET_PERM" -eq 0 ]; then
  FOUND=$((FOUND+1))
  echo "$PASS No INTERNET permission in manifest"
else
  echo "$FAIL INTERNET permission found                    -> violates offline-only requirement; remove unless truly justified"
fi

NETWORK_LIBS=$(grep -rlE 'Retrofit|Volley|OkHttpClient|HttpURLConnection' $SRC_FILES 2>/dev/null | wc -l)
TOTAL=$((TOTAL+1))
if [ "$NETWORK_LIBS" -eq 0 ]; then
  FOUND=$((FOUND+1))
  echo "$PASS No networking library usage found"
else
  echo "$FAIL Networking library usage found ($NETWORK_LIBS file(s)) -> remove; app must be fully offline"
fi

ONLINE_DB=$(grep -rliE 'firebase|firestore|mongodb|mysql.*jdbc|remote.*database' $SRC_FILES $GRADLE_FILES 2>/dev/null | wc -l)
TOTAL=$((TOTAL+1))
if [ "$ONLINE_DB" -eq 0 ]; then
  FOUND=$((FOUND+1))
  echo "$PASS No online/cloud database dependency found"
else
  echo "$FAIL Online database reference found ($ONLINE_DB file(s)) -> not allowed; requirement is offline-only, local storage instead"
fi

check "Local (offline) persistence present" \
  "$(grep -rlE 'SQLiteOpenHelper|@Entity|@Dao|SharedPreferences' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no local storage mechanism found -- app likely needs to persist data locally"

# ============================================================
# PART 2 — ANDROID FRAMEWORK FUNDAMENTALS
# ============================================================
echo ""
echo "############################################################"
echo "# PART 2: ANDROID FRAMEWORK CONCEPTS"
echo "############################################################"

echo ""
echo "-- App Components --"
check "Activities (class-level)" \
  "$(grep -rlE 'extends[[:space:]]+(AppCompat)?Activity' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no class extends Activity/AppCompatActivity"

check "Fragments" \
  "$(grep -rlE 'extends[[:space:]]+Fragment' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (optional -- not required by IS107 spec, single-Activity design is fine)"

check "Services" \
  "$(grep -rlE 'extends[[:space:]]+Service' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (optional -- only needed for background tasks)"

check "Broadcast Receivers" \
  "$(grep -rlE 'extends[[:space:]]+BroadcastReceiver' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (optional -- only needed for system event listening)"

check "Content Providers" \
  "$(grep -rlE 'extends[[:space:]]+ContentProvider' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (optional -- only needed if exposing data to other apps)"

echo ""
echo "-- UI Layer --"
check "XML layout files" \
  "$(find "$LAYOUT_DIR" -name '*.xml' 2>/dev/null | wc -l)" \
  "-> res/layout missing or empty"

check "RecyclerView usage" \
  "$(grep -rl 'RecyclerView' $SRC_FILES $LAYOUT_DIR 2>/dev/null | wc -l)" \
  "-> none found (optional unless app displays a list/grid of data)"

check "RecyclerView.Adapter implementation" \
  "$(grep -rlE 'extends[[:space:]]+RecyclerView\.Adapter' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found -- required if using RecyclerView"

check "Material Design components" \
  "$(grep -rl 'com.google.android.material' $GRADLE_FILES $LAYOUT_DIR 2>/dev/null | wc -l)" \
  "-> Material library not detected in dependencies/layouts"

echo ""
echo "-- Data & Persistence --"
check "SharedPreferences" \
  "$(grep -rl 'SharedPreferences' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (optional, for settings/flags)"

check "SQLite (SQLiteOpenHelper)" \
  "$(grep -rl 'SQLiteOpenHelper' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found"

check "Room persistence library" \
  "$(grep -rlE '@Entity|@Dao|@Database' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> not using Room (fine if using raw SQLite instead)"

check "Internal/external storage access" \
  "$(grep -rlE 'getFilesDir\(\)|getExternalFilesDir\(|openFileOutput\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (optional, for file/image handling)"

echo ""
echo "-- Architecture --"
check "ViewModel usage (MVVM)" \
  "$(grep -rlE 'extends[[:space:]]+ViewModel' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (fine if project intentionally uses plain MVC)"

MODEL_DIR=$(find "$ROOT" -type d -iname "model")
check "MVC-style package separation (model/)" \
  "$(echo "$MODEL_DIR" | grep -c . 2>/dev/null)" \
  "-> no dedicated model/ package found"

# ============================================================
# PART 3 — JAVA LANGUAGE & OOP FUNDAMENTALS
# ============================================================
echo ""
echo "############################################################"
echo "# PART 3: JAVA LANGUAGE & OOP CONCEPTS"
echo "############################################################"

echo ""
echo "-- Core Syntax & Control Flow --"
check "Method declarations" \
  "$(grep -rlE '(public|private|protected)[[:space:]]+[A-Za-z<>\[\]]+[[:space:]]+[A-Za-z_]+\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no method signatures detected"

check "Variables & primitive data types" \
  "$(grep -rlE '\b(int|double|float|long|boolean|char|short|byte|String)\b' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no primitive/String variable usage found"

check "Operators (arithmetic/logical/comparison)" \
  "$(grep -rlE '[+\-*/%]=|==|!=|&&|\|\||<=|>=' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no operator usage detected"

check "if / else conditional logic" \
  "$(grep -rlE '\bif[[:space:]]*\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no conditional branching found"

check "Loops (for/while)" \
  "$(grep -rlE '\bfor[[:space:]]*\(|\bwhile[[:space:]]*\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no loops found"

echo ""
echo "-- Arrays & Collections --"
check "Arrays" \
  "$(grep -rlE '[A-Za-z_]+\[\][[:space:]]*[A-Za-z_]+|new[[:space:]]+[A-Za-z_]+\[' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no array declarations found"

check "ArrayList / List usage" \
  "$(grep -rlE '\bArrayList\b|\bList<' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (needed for dynamic data, e.g. RecyclerView datasets)"

echo ""
echo "-- User Input, File Handling & Error Handling --"
check "User input capture (EditText/getText)" \
  "$(grep -rlE 'EditText|getText\(\)' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no user input capture found"

check "File I/O" \
  "$(grep -rlE 'FileReader|FileWriter|FileInputStream|FileOutputStream|BufferedReader|BufferedWriter' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (optional -- only needed if app reads/writes raw files)"

check "Try/catch exception handling" \
  "$(grep -rl 'try[[:space:]]*{' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found -- risky, especially around DB/file operations"

echo ""
echo "-- Object-Oriented Programming --"
check "Class declarations" \
  "$(grep -rlE '\bclass[[:space:]]+[A-Za-z_]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> unexpected -- no classes found"

check "Constructors" \
  "$(grep -rlE 'public[[:space:]]+[A-Za-z_]+[[:space:]]*\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none detected"

check "Encapsulation (private fields)" \
  "$(grep -rlE 'private[[:space:]]+[A-Za-z_<>\[\]]+[[:space:]]+[A-Za-z_]+;' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no private fields found -- check fields aren't left public"

check "Inheritance (extends)" \
  "$(grep -rl 'extends[[:space:]]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found"

check "Interfaces / implements" \
  "$(grep -rlE '\binterface[[:space:]]|implements[[:space:]]' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (common in Adapter/listener patterns)"

check "Polymorphism (@Override)" \
  "$(grep -rl '@Override' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no method overriding found"

echo ""
echo "-- Database / Backend Logic --"
check "Raw SQL statements" \
  "$(grep -rliE 'SELECT[[:space:]]|INSERT[[:space:]]INTO|UPDATE[[:space:]].*SET|DELETE[[:space:]]FROM' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (skip if using Room, which abstracts raw SQL)"

check "ContentValues / Cursor (SQLite CRUD)" \
  "$(grep -rlE 'ContentValues|Cursor' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> none found (skip if using Room instead of raw SQLite)"

# ---------------- Summary ----------------
echo ""
echo "============================================================"
echo " TOTAL: $FOUND / $TOTAL checks passed"
echo "============================================================"
echo ""
echo "Read this as an OUTPUT/CODE audit only. A ❌ next to an"
echo "'optional' item is NOT a deduction -- it just means that"
echo "concept isn't used, which may be fine depending on your app's"
echo "actual features. The items under PART 1 (IS107 requirements)"
echo "are the only ones that are truly mandatory for this project."
