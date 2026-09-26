package com.runova.helpers;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * V6 - Shared window chrome for every RUNOVA screen.
 *
 * <p>Two jobs, applied identically on all eight activities so the chrome is consistent:
 *
 * <ol>
 *   <li><b>Full screen.</b> {@link #applyEdgeToEdge(Activity)} lets the window draw behind the
 *       status bar and the system navigation bar, so the background gradient covers the whole
 *       display with no empty band at the top or the bottom.</li>
 *   <li><b>Instant screen changes.</b> Activity transitions are switched off, so tapping a
 *       bottom navigation button shows the next screen immediately - no slide, no fade. Because
 *       every screen also applies the same insets, the shared bottom navigation bar stays exactly
 *       where it is instead of appearing to move.</li>
 * </ol>
 *
 * <p>Insets are added <i>on top of</i> the padding a view already has, so no existing layout
 * spacing is changed - the content is only pushed out of the areas the system bars occupy.
 */
public final class WindowHelper {

    /** System bars plus display cutout: covers notches, punch-holes and gesture bars. */
    private static final int INSET_TYPES =
            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

    private WindowHelper() {
    }

    /**
     * Call once per activity, before {@code setContentView()}, so the first frame is already
     * full screen and the first screen change is already instant.
     */
    public static void applyEdgeToEdge(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        disableActivityTransitions(activity);
    }

    /** Adds the status bar / cutout height to the view's existing top padding. */
    public static void applyTopInset(View view) {
        applyInsetPadding(view, true, false);
    }

    /** Adds the system navigation bar height to the view's existing bottom padding. */
    public static void applyBottomInset(View view) {
        applyInsetPadding(view, false, true);
    }

    /** Both ends - for screens that do not carry the bottom navigation bar. */
    public static void applyVerticalInsets(View view) {
        applyInsetPadding(view, true, true);
    }

    private static void applyInsetPadding(final View view, final boolean top, final boolean bottom) {
        if (view == null) {
            return;
        }

        // Captured once: the padding declared in XML. Insets are always added to these values,
        // never to the values this listener last wrote, so repeated inset dispatches cannot stack.
        final int paddingLeft = view.getPaddingLeft();
        final int paddingTop = view.getPaddingTop();
        final int paddingRight = view.getPaddingRight();
        final int paddingBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (target, windowInsets) -> {
            Insets insets = windowInsets.getInsets(INSET_TYPES);
            target.setPadding(
                    paddingLeft,
                    paddingTop + (top ? insets.top : 0),
                    paddingRight,
                    paddingBottom + (bottom ? insets.bottom : 0));
            return windowInsets;
        });

        // No-op while the view is still detached; insets are dispatched on the first layout pass.
        ViewCompat.requestApplyInsets(view);
    }

    /**
     * Removes the open / close animation for this activity on every supported API level.
     * API 34 (Android 14) replaced {@code overridePendingTransition} with
     * {@code overrideActivityTransition}; older releases still use the earlier call.
     */
    private static void disableActivityTransitions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0);
        } else {
            activity.overridePendingTransition(0, 0);
        }
    }
}
