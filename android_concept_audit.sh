#!/bin/bash
# ============================================================
# Android Fundamentals Coverage Audit
# ------------------------------------------------------------
# Scans an Android Studio project and checks whether the core
# Android concepts are actually present in the codebase.
#
# USAGE:
#   ./android_concept_audit.sh /path/to/your/project
#   (defaults to current directory if no path given)
#
# Works for Java or Kotlin projects. Safe to re-run anytime.
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
    printf "%s %-42s (%s match%s)\n" "$PASS" "$label" "$count" "$([ "$count" -eq 1 ] && echo "" || echo "es")"
  else
    printf "%s %-42s %s\n" "$FAIL" "$label" "$hint"
  fi
}

MANIFEST=$(find "$ROOT" -name "AndroidManifest.xml" | head -1)
SRC_FILES=$(find "$ROOT" -type f \( -name "*.java" -o -name "*.kt" \))
LAYOUT_DIR=$(find "$ROOT" -type d -path "*/res/layout*")
GRADLE_FILES=$(find "$ROOT" -name "build.gradle" -o -name "build.gradle.kts")

echo "============================================================"
echo " ANDROID FUNDAMENTALS COVERAGE AUDIT"
echo " Project: $ROOT"
echo "============================================================"

# ---------------- App Components ----------------
echo ""
echo "-- App Components --"

check "Activities" \
  "$(grep -rlE 'extends[[:space:]]+(AppCompat)?Activity' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no class extends Activity/AppCompatActivity"

check "Fragments" \
  "$(grep -rlE 'extends[[:space:]]+Fragment' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no class extends Fragment (may be intentional if single-Activity app)"

check "Intents (explicit/implicit)" \
  "$(grep -rlE 'new Intent\(|Intent\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no Intent usage found for navigation/sharing"

check "Services" \
  "$(grep -rlE 'extends[[:space:]]+Service' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no background Service class (skip if app has no bg tasks)"

check "Broadcast Receivers" \
  "$(grep -rlE 'extends[[:space:]]+BroadcastReceiver' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no BroadcastReceiver (skip if app doesn't listen for system events)"

check "Content Providers" \
  "$(grep -rlE 'extends[[:space:]]+ContentProvider' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no ContentProvider (only needed if exposing data to other apps)"

# ---------------- UI Layer ----------------
echo ""
echo "-- UI Layer --"

check "XML Layout files" \
  "$(find "$LAYOUT_DIR" -name '*.xml' 2>/dev/null | wc -l)" \
  "-> res/layout directory missing or empty"

check "ConstraintLayout usage" \
  "$(grep -rl 'ConstraintLayout' $LAYOUT_DIR 2>/dev/null | wc -l)" \
  "-> not using ConstraintLayout (fine if using Linear/RelativeLayout instead)"

check "RecyclerView usage" \
  "$(grep -rl 'RecyclerView' $SRC_FILES $LAYOUT_DIR 2>/dev/null | wc -l)" \
  "-> no RecyclerView found for list/grid display"

check "RecyclerView.Adapter implementation" \
  "$(grep -rlE 'extends[[:space:]]+RecyclerView\.Adapter' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no custom Adapter class (required to feed data into RecyclerView)"

check "Material Design components" \
  "$(grep -rl 'com.google.android.material' $GRADLE_FILES $LAYOUT_DIR 2>/dev/null | wc -l)" \
  "-> Material library not in dependencies / not used in layouts"

# ---------------- Data & Persistence ----------------
echo ""
echo "-- Data & Persistence --"

check "SharedPreferences" \
  "$(grep -rl 'SharedPreferences' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no SharedPreferences usage (settings/flags storage)"

check "SQLite (SQLiteOpenHelper)" \
  "$(grep -rl 'SQLiteOpenHelper' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no raw SQLite database helper found"

check "Room persistence library" \
  "$(grep -rlE '@Entity|@Dao|@Database' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> not using Room (OK if project intentionally uses raw SQLite)"

check "Internal/External storage access" \
  "$(grep -rlE 'getFilesDir\(\)|getExternalFilesDir\(|openFileOutput\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no direct file storage access (skip if app doesn't handle files/images)"

check "Networking (Retrofit/Volley/OkHttp)" \
  "$(grep -rlE 'Retrofit|Volley|OkHttpClient|HttpURLConnection' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no networking library found (skip if app is fully offline/local-only)"

# ---------------- Architecture ----------------
echo ""
echo "-- Architecture Pattern --"

check "ViewModel usage (MVVM)" \
  "$(grep -rlE 'extends[[:space:]]+ViewModel|androidx\.lifecycle\.ViewModel' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no ViewModel classes (project likely using plain MVC instead)"

check "LiveData usage" \
  "$(grep -rl 'LiveData' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no LiveData (pairs with ViewModel for MVVM)"

MODEL_DIR=$(find "$ROOT" -type d -iname "model")
CONTROLLER_DIR=$(find "$ROOT" -type d -iname "controller")
check "MVC folder structure (model/controller)" \
  "$(( $(echo "$MODEL_DIR" | grep -c . 2>/dev/null) + $(echo "$CONTROLLER_DIR" | grep -c . 2>/dev/null) ))" \
  "-> no dedicated model/ or controller/ package found"

# ---------------- Permissions & Manifest ----------------
echo ""
echo "-- Manifest & Permissions --"

if [ -n "$MANIFEST" ]; then
  echo "$PASS AndroidManifest.xml found              -> $MANIFEST"
  FOUND=$((FOUND+1))
else
  echo "$FAIL AndroidManifest.xml                    -> not found anywhere in project"
fi
TOTAL=$((TOTAL+1))

check "uses-permission declarations" \
  "$(grep -c 'uses-permission' "$MANIFEST" 2>/dev/null || echo 0)" \
  "-> no permissions declared (fine if app needs none)"

check "Runtime permission requests" \
  "$(grep -rlE 'ActivityCompat\.requestPermissions|requestPermissions\(' $SRC_FILES 2>/dev/null | wc -l)" \
  "-> no runtime permission requests (needed for camera/location/storage on API 23+)"

# ---------------- Summary ----------------
echo ""
echo "============================================================"
echo " SUMMARY: $FOUND / $TOTAL checks passed"
echo "============================================================"
echo ""
echo "NOTE: A '${FAIL}' is not automatically a bug — some concepts"
echo "(Services, Broadcast Receivers, Content Providers, networking)"
echo "are only needed if your app's requirements call for them."
echo "Use this as a coverage map to confirm intentional gaps vs."
echo "missing implementation, especially before a production release."
