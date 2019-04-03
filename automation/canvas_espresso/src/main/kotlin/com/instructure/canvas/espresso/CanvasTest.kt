package com.instructure.canvas.espresso

import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.instructure.espresso.AccessibilityChecker
import com.instructure.espresso.matchers.withOnlyWidthLessThan
import com.instructure.espresso.page.InstructureTest
import org.hamcrest.Matchers
import org.junit.Before

// InstructureTest wrapper for Canvas code
abstract class CanvasTest : InstructureTest() {
    @Before
    override fun preLaunchSetup() {
        // Uncomment this to enable accessibility checks on all tests
        //enableAndConfigureAccessibilityChecks()
        super.preLaunchSetup()
    }

    // Enable and configure accessibility checks
    protected fun enableAndConfigureAccessibilityChecks() {
        AccessibilityChecker.runChecks() // Enable accessibility checks

        // For now, suppress warnings about overflow menus with width < 48dp, so long as the height is >= 48dp
        AccessibilityChecker.accessibilityValidator
                // Causes all views to be checked, instead of just the views with which the test interacts
                ?.setRunChecksFromRootView(true)

                // Suppress, for now, errors about overflow menus being too narrow
                ?.setSuppressingResultMatcher(
                        Matchers.allOf(
                                AccessibilityCheckResultUtils.matchesViews(ViewMatchers.withContentDescription("More options")),
                                withOnlyWidthLessThan(48)
                        )

                )
    }
}
