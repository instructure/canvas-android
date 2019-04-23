package com.instructure.canvas.espresso

import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.instructure.espresso.AccessibilityChecker
import com.instructure.espresso.matchers.withOnlyWidthLessThan
import com.instructure.espresso.page.InstructureTest
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
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

                // Suppression (exclusion) rules
                ?.setSuppressingResultMatcher(
                        Matchers.anyOf(
                            // Suppress issues in PsPDFKit, date/time picker
                            isExcludedNamedView( listOf("pspdf", "_picker_", "timePicker")),

                            // Suppress, for now, errors about overflow menus being too narrow
                            Matchers.allOf(
                                    AccessibilityCheckResultUtils.matchesViews(ViewMatchers.withContentDescription("More options")),
                                    withOnlyWidthLessThan(48)
                            )
                        )

                )
    }

    // Very inefficient matcher that identifies views whose resource name (or the resource name
    // of any of its ancestors up to five levels up) contains any of the strings in the "excludes" list.
    //
    // It would be so much more efficient if the accessibility test framework first flagged a
    // view as bad, then asked us if we wanted to suppress it.  Then we would only have to run this
    // logic on a few views.  But, instead, the framework asks if we want to suppress the view before
    // it gets checked, which means that this logic will run on *all* views being tested.  :-(.
    private fun isExcludedNamedView(excludes: List<String>) : BaseMatcher<AccessibilityViewCheckResult> {

        return object: BaseMatcher<AccessibilityViewCheckResult>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Checks whether the view's resource name is in the exclusion list")
            }

            // Matches if the view being tested, or any of its parents going up 5
            // levels, has any of the excluded strings in its resource name.
            override fun matches(item: Any): Boolean {
                when(item) {
                    is AccessibilityViewCheckResult -> {
                        var view = item.view
                        var upCount = 0
                        while(view != null && upCount < 5)
                        {
                            try {
                                var resourceName = view.context.resources.getResourceName(view.id)
                                for(excludedName in excludes) {
                                    if(resourceName.contains(excludedName)) {
                                        //Log.v("AccessibilityExclude", "Caught $resourceName")
                                        return true
                                    }
                                }
                            }
                            catch(e: Resources.NotFoundException) { }

                            var parent = view.parent
                            when(parent) {
                                is View -> view = parent
                                else -> view = null
                            }
                            upCount += 1
                        }
                        //Log.v("AccessibilityExclude", "Whiffed on ${item.view}")
                    }
                }
                return false
            }
        }
    }

    // Does the test device have particularly low screen resolution?
    fun isLowResDevice() : Boolean {
        return activityRule.activity.resources.displayMetrics.densityDpi < DisplayMetrics.DENSITY_HIGH
    }
}
