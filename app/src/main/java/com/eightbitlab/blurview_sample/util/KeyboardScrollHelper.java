package com.eightbitlab.blurview_sample.util;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;

/**
 * Helper that automatically scrolls a {@link ScrollView} when the soft
 * keyboard appears, so the currently-focused {@link EditText} is never
 * hidden behind the keyboard.
 *
 * <p>Requires {@code android:windowSoftInputMode="adjustResize"} on the
 * Activity in AndroidManifest.xml.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // in onCreate():
 * KeyboardScrollHelper helper = new KeyboardScrollHelper(this, svRoot);
 * helper.registerEditText(et1, et2, ...);
 * helper.attach();
 * }</pre>
 */
public class KeyboardScrollHelper {

    private final Activity activity;
    private final ScrollView scrollView;
    private View.OnLayoutChangeListener layoutChangeListener;
    private boolean attached;

    public KeyboardScrollHelper(Activity activity, ScrollView scrollView) {
        this.activity = activity;
        this.scrollView = scrollView;
    }

    /**
     * Register {@link EditText} views so the ScrollView scrolls to reveal
     * whichever one gains focus.
     */
    public void registerEditText(EditText... editTexts) {
        for (EditText et : editTexts) {
            et.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // Two attempts: keyboard animation takes ~200–500ms
                    scrollView.postDelayed(() -> scrollToReveal(v), 300);
                    scrollView.postDelayed(() -> scrollToReveal(v), 600);
                }
            });
        }
    }

    /**
     * Start monitoring the ScrollView's own layout changes.
     * When the ScrollView shrinks (keyboard appeared) we scroll to
     * keep the focused view visible.
     */
    public void attach() {
        if (attached) return;
        attached = true;

        layoutChangeListener = (v, left, top, right, bottom,
                                  oldLeft, oldTop, oldRight, oldBottom) -> {
            int oldHeight = oldBottom - oldTop;
            int newHeight = bottom - top;
            if (oldHeight > 0 && newHeight < oldHeight) {
                View focused = activity.getCurrentFocus();
                if (focused != null) {
                    // post() → run after layout is fully settled
                    scrollView.post(() -> scrollToReveal(focused));
                    // Delayed safety net
                    scrollView.postDelayed(() -> scrollToReveal(focused), 350);
                }
            }
        };
        scrollView.addOnLayoutChangeListener(layoutChangeListener);
    }

    /**
     * Stop listening for layout changes.
     */
    public void detach() {
        attached = false;
        if (layoutChangeListener != null && scrollView != null) {
            scrollView.removeOnLayoutChangeListener(layoutChangeListener);
            layoutChangeListener = null;
        }
    }

    // ---------------------------------------------------------------
    //  internal
    // ---------------------------------------------------------------

    /**
     * Core logic: compare the target view's screen position against the
     * ScrollView's visible screen area.  If the target is below the
     * visible area, scroll the content up.
     *
     * <p>Uses <strong>screen coordinates</strong> throughout.</p>
     */
    private void scrollToReveal(View target) {
        if (target == null || scrollView == null) return;

        // ScrollView visible bottom (screen coordinates)
        int[] svLoc = new int[2];
        scrollView.getLocationOnScreen(svLoc);
        int svScreenBottom = svLoc[1] + scrollView.getHeight();

        // Focused EditText bottom (screen coordinates)
        int[] tLoc = new int[2];
        target.getLocationOnScreen(tLoc);
        int targetScreenBottom = tLoc[1] + target.getHeight();

        if (targetScreenBottom > svScreenBottom) {
            int deficit = targetScreenBottom - svScreenBottom;
            // 120px so the user sees context below the field
            scrollView.smoothScrollBy(0, deficit + 120);
        }
    }
}
