package com.instructure.canvas.espresso

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.instructure.espresso.AccessibilityChecker
import com.instructure.espresso.BuildConfig
import com.instructure.espresso.page.InstructureTest
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matchers
import org.junit.Before

// InstructureTest wrapper for Canvas code
abstract class CanvasTest : InstructureTest(BuildConfig.GLOBAL_DITTO_MODE) {
    @Before
    override fun preLaunchSetup() {

        // Enable accessibility testing for all apps
        enableAndConfigureAccessibilityChecks()
        super.preLaunchSetup()
    }

    // Enable and configure accessibility checks
    protected fun enableAndConfigureAccessibilityChecks() {
        AccessibilityChecker.runChecks() // Enable accessibility checks

        Log.v("overflowWidth", "enableAndConfigureAccessibilityChecks() called, validator=${AccessibilityChecker.accessibilityValidator != null}")
        // For now, suppress warnings about overflow menus with width < 48dp, so long as the height is >= 48dp
        AccessibilityChecker.accessibilityValidator
                // Causes all views to be checked, instead of just the views with which the test interacts
                ?.setRunChecksFromRootView(true)

                // Suppression (exclusion) rules
                ?.setSuppressingResultMatcher(
                        Matchers.anyOf(
                            // Suppress issues in PsPDFKit, date/time picker, calendar grid
                            isExcludedNamedView( listOf("pspdf", "_picker_", "timePicker", "calendar1")),

                            // Suppress, for now, errors about overflow menus being too narrow
                            Matchers.allOf(
                                    isOverflowMenu(),
                                    withOnlyWidthLessThan(48)
                            ),

                            // Short-term workaround as we try to return a11y-compliant colors for submit button
                            AccessibilityCheckResultUtils.matchesViews(ViewMatchers.withResourceName("submitButton")),

                            // On very low-res devices, controls can be squished to the point that they are smaller
                            // than their specified minimum.  This seems unavoidable, so let's not log an
                            // accessibility error for this.
                            underMinSizeOnLowRes(),

                            // Let's ignore size issues with WebViews, since they do not honor minHeight/minWidth
                            // Note that TouchTargetSizeViewCheck is the *old* name of the check.  The new name would
                            // be "TouchTargetSizeCheck".
                            Matchers.allOf(
                                    AccessibilityCheckResultUtils.matchesViews(ViewMatchers.isAssignableFrom(WebView::class.java)),
                                    AccessibilityCheckResultUtils.matchesCheckNames(Matchers.`is`("TouchTargetSizeViewCheck"))
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
                            // Weeding out id == -1 will filter out a lot of lines from our logs
                            if(view.id != -1) {
                                try {
                                    var resourceName = view.context.resources.getResourceName(view.id)
                                    for (excludedName in excludes) {
                                        if (resourceName.contains(excludedName)) {
                                            //Log.v("AccessibilityExclude", "Caught $resourceName")
                                            return true
                                        }
                                    }
                                } catch (e: Resources.NotFoundException) {
                                }
                            }

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

    // Copying this matcher from the shared espresso lib to here.  In the espresso lib, we had
    // to rely on ActivityHelper.currentActivity() to get an activity, and that
    // was occasionally buggy due to small windows where there might be no resumed
    // activities.  Here, we have access to activityRule to get an activity.
    //
    // A matcher for views whose width is less than the specified amount (in dp),
    // but whose height is at least the specified amount.
    // This is used to suppress accessibility failures related to overflow menus
    // in action bars being to narrow.
    fun withOnlyWidthLessThan(dimInDp: Int) : BaseMatcher<AccessibilityViewCheckResult>
    {
        var activity = activityRule.activity
        val densityDpi = activity.resources.displayMetrics.densityDpi
        val dim_f = dimInDp * (densityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT.toDouble())
        val dim = dim_f.toInt()
        Log.v("overflowWidth", "dimInDp=$dimInDp,densityDpi=$densityDpi,dim_f=$dim_f,dim=$dim")
        return object : BaseMatcher<AccessibilityViewCheckResult>() {
            override fun describeTo(description: Description?) {
                description?.appendText("checking whether width < $dim && height >= $dim")
            }

            override fun matches(item: Any): Boolean {
                when(item) {
                    is AccessibilityViewCheckResult -> {
                        val result = item.view.width < dim && item.view.height >= dim
                        //Log.v("overflowWidth", "view=${getResourceName(item.view)}, desc=${item.view.contentDescription}, w=${item.view.width}, h=${item.view.height}, res=$result")
                        return result
                    }
                    else -> {
                        //Log.v("overflowWidth", "rejecting, item = ${item::javaClass::name}")
                        return false
                    }
                }
            }

        }
    }

    // Checks to see whether or no a view is an overflow menu.
    fun isOverflowMenu() : BaseMatcher<AccessibilityViewCheckResult> {
        return object : BaseMatcher<AccessibilityViewCheckResult>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Checking whether view is the overflow menu")
            }

            override fun matches(item: Any?): Boolean {
                when(item) {
                    is AccessibilityViewCheckResult -> {
                        var result = item.view?.contentDescription?.contains("More options", ignoreCase = true) ?: false
                        //Log.v("overflowWidth", "isOverflowMenu: contentDescription=${item.view?.contentDescription ?: "unknown"}, result=$result ")
                        return result
                    }
                    else -> {
                        //Log.v("overflowWidth", "isOverflowMenu: rejected  item = ${item!!::class.java::getSimpleName}")
                        return false
                    }
                }
            }

        }
    }

    /**
     * Returns a matcher that will match if:
     *
     * We are on a low-res device (density==1), and either dimension falls below the
     * minimum size *and* falls below its specified minimum.
     */
    fun underMinSizeOnLowRes() : BaseMatcher<AccessibilityViewCheckResult> {
        val activity = activityRule.activity
        val density = activity.resources.displayMetrics.density
        Log.v("underMinSizeOnLowRes", "density: $density")
        return object : BaseMatcher<AccessibilityViewCheckResult>() {
            override fun describeTo(description: Description?) {
                description?.appendText("Checks to see if size < minimum on low-res device")
            }

            override fun matches(item: Any?): Boolean {
                if(density > 1) return false
                when(item) {
                    is AccessibilityViewCheckResult -> {
                        val v = item.view
                        val toss =
                                v.height > 0 // Require some dimension in order to be tossed
                                && v.width > 0
                                &&  ( (v.height < 48 && v.height < v.minimumHeight) ||
                                      (v.width < 48 && v.width < v.minimumWidth) )

                        if(toss) {
                            var resourceName = getResourceName(v)
                            Log.v("underMinSizeOnLowRes",
                                    "Tossing $resourceName, w=${v.width}, h=${v.height}, mw=${v.minimumWidth}, mh=${v.minimumHeight}")
                            return true
                        }
                    }
                }
                return false
            }
        }
    }

    // Grab the resourceName for a view
    private fun getResourceName(view: View) : String {
        var resourceName = "<unknown>"
        // Don't bother trying for view with an ID of -1, since that is the standard representation
        // of "no resource name defined".  Pressing forward with id == -1 clutters our log with
        // a lot of unnecessary messages.
        if(view.id != -1) {
            try {
                resourceName = view.context.resources.getResourceName(view.id)
            } catch (nfe: Resources.NotFoundException) {
                // Eat this exception
            }
        }

        return resourceName
    }

}
