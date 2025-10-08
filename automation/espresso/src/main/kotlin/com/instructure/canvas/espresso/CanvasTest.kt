/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.canvas.espresso

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.compose.ui.platform.ComposeView
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheckNames
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesViews
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.instructure.espresso.AccessibilityChecker
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.espresso.ScreenshotTestRule
import com.instructure.espresso.UiControllerSingleton
import com.instructure.espresso.page.InstructureTestingContract
import dagger.hilt.android.testing.HiltAndroidRule
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

// InstructureTest wrapper for Canvas code
abstract class CanvasTest : InstructureTestingContract {

    val STEP_TAG = "${this::class.java.simpleName} #STEP# "
    val ASSERTION_TAG = "${this::class.java.simpleName} #ASSERTION# "
    val PREPARATION_TAG = "${this::class.java.simpleName} #PREPARATION# "
    val EMPTY_STRING = ""

    abstract val activityRule: InstructureActivityTestRule<out Activity>

    abstract val isTesting: Boolean

    var extraAccessibilitySupressions: Matcher<in AccessibilityViewCheckResult>? = anyOf()

    val connectivityManager = InstrumentationRegistry.getInstrumentation().context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    lateinit var originalActivity : Activity

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Rule(order = 2)
    override fun chain(): TestRule {
        return RuleChain
                .outerRule(ScreenshotTestRule())
                .around(activityRule)
    }

    // Sometimes activityRule.activity can get nulled out over time, probably as we
    // navigate away from the original login screen.  Capture the activity here so
    // that we can reference it safely later.
    @Before
    fun recordOriginalActivity() {
        originalActivity = activityRule.activity
        try {
            hiltRule.inject()
        } catch (e: IllegalStateException) {
            // Catch this exception to avoid multiple injection
            Log.w("Test Inject", e.message ?: "")
        }

        val application = originalActivity.application as? TestAppManager
        application?.workerFactory = this.workerFactory
        application?.initWorkManager(application)
    }

    @Before
    override fun preLaunchSetup() {

        // Enable accessibility testing for all apps
        enableAndConfigureAccessibilityChecks()

        if (!configChecked) {
            checkBuildConfig()
            configChecked = true
        }
        setupCoverageFolder()
        UiControllerSingleton.get()

        // Let's set ourselves up to log information about our retries and failures.
        ScreenshotTestRule.registerFailureHandler( handler = { error, testMethod, testClass, disposition ->

            if(disposition == "retry") {
                Log.d("TEST RETRY", "testMethod: $testMethod, error=$error, stacktrace=${error.stackTrace.joinToString("\n")} cause=${error.cause}")
            }

            // Grab the Observe-mobile token from Bitrise
            val observeToken = InstrumentationRegistry.getArguments().getString("OBSERVE_MOBILE_TOKEN")

            // Only continue if we're on Bitrise
            // (More accurately, if we are on FTL launched from Bitrise.)
            if(!observeToken.isNullOrEmpty()) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                val hasActiveNetwork = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false

                if (hasActiveNetwork) {
                    reportToObserve(disposition, testMethod, testClass, error, observeToken)
                } else {
                    turnOnConnectionViaADB()
                    connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            connectivityManager.unregisterNetworkCallback(this)
                            reportToObserve(disposition, testMethod, testClass, error, observeToken)
                        }
                    })
                }

            }
            else {
                Log.d("CanvasTest", "Retry report logic aborting because we're not on Bitrise")
            }
        })

    }

    private fun reportToObserve(
        disposition: String,
        testMethod: String,
        testClass: String,
        error: Throwable,
        observeToken: String?
    ) {
        val bitriseWorkflow =
            InstrumentationRegistry.getArguments().getString("BITRISE_TRIGGERED_WORKFLOW_ID")
        val bitriseApp = InstrumentationRegistry.getArguments().getString("BITRISE_APP_TITLE")
        val bitriseBranch = InstrumentationRegistry.getArguments().getString("BITRISE_GIT_BRANCH")
        val bitriseBuildNumber =
            InstrumentationRegistry.getArguments().getString("BITRISE_BUILD_NUMBER")

        val eventObject = JSONObject()
        eventObject.put("workflow", bitriseWorkflow)
        eventObject.put("branch", bitriseBranch)
        eventObject.put("bitriseApp", bitriseApp)
        eventObject.put("buildNumber", bitriseBuildNumber)
        eventObject.put("status", disposition)
        eventObject.put("testName", testMethod)
        eventObject.put("testClass", testClass)
        eventObject.put("stackTrace", error.stackTrace.take(15).joinToString(", "))
        eventObject.put("osVersion", Build.VERSION.SDK_INT.toString())
        eventObject.put("sourcetype", "mobile-android-qa-testresult")
        // Limit our error message to 4096 chars; they can be unreasonably long (e.g., 137K!) when
        // they contain a view hierarchy, and there is typically not much useful info after the
        // first few lines.
        eventObject.put("message", error.toString().take(4096))

        val payloadObject = JSONObject()

        payloadObject.put("data", eventObject)

        val payload = payloadObject.toString()
        val payloadBytes = payload.toByteArray(Charsets.UTF_8)
        Log.d("CanvasTest", "payload = $payload")

        // Can't run a curl command from FTL, so let's do this the hard way
        var os: OutputStream? = null
        var inputStream: InputStream? = null
        var conn: HttpURLConnection? = null

        try {

            // Set up our url/connection
            val url = URL("https://103443579803.collect.observeinc.com/v1/http")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $observeToken")
            conn.setRequestProperty("Content-Type", "application/json; utf-8")
            conn.setRequestProperty("Content-Length", payloadBytes.size.toString())
            conn.setRequestProperty("Accept", "application/json")
            conn.setDoInput(true)
            conn.setDoOutput(true)

            // Connect
            conn.connect()

            // Send out our post body
            os = BufferedOutputStream(conn.outputStream)
            os.write(payload.toByteArray())
            os.flush()

            // Report the result summary
            Log.d(
                "CanvasTest",
                "Response code: ${conn.responseCode}, message: ${conn.responseMessage}"
            )

            // Report the Observe result JSON
            inputStream = conn.inputStream
            val content = inputStream.bufferedReader().use(BufferedReader::readText)
            Log.d("CanvasTest", "Response: $content")
        } finally {
            // Clean up our mess
            if (os != null) os.close()
            if (inputStream != null) inputStream.close()
            if (conn != null) conn.disconnect()
        }
    }

    // Creates an /sdcard/coverage folder if it does not already exist.
    // This is necessary for us to generate/process code coverage data.
    private fun setupCoverageFolder() {
        val dir = File(Environment.getExternalStorageDirectory(), "coverage")
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun checkBuildConfig() {
        if (!isTesting) throw RuntimeException("Build config must be IS_TESTING! (qaDebug)")
    }

    // Enable and configure accessibility checks
    protected open fun enableAndConfigureAccessibilityChecks() {
        AccessibilityChecker.runChecks() // Enable accessibility checks

        Log.v("overflowWidth", "enableAndConfigureAccessibilityChecks() called, validator=${AccessibilityChecker.accessibilityValidator != null}")
        // For now, suppress warnings about overflow menus with width < 48dp, so long as the height is >= 48dp
        AccessibilityChecker.accessibilityValidator
                // Causes all views to be checked, instead of just the views with which the test interacts
                ?.setRunChecksFromRootView(true)

                // Suppression (exclusion) rules
                ?.setSuppressingResultMatcher(
                        anyOf(
                            // Extra suppressions that can be added to individual tests
                            extraAccessibilitySupressions,
                            // Suppress issues in PsPDFKit, date/time picker, calendar grid
                            isExcludedNamedView( listOf("pspdf", "_picker_", "timePicker", "calendar1")),

                            // Suppress, for now, errors about overflow menus being too narrow
                            allOf(
                                    isOverflowMenu(),
                                    withOnlyWidthLessThan(48)
                            ),

                            // Short-term workaround as we try to return a11y-compliant colors for submit button
                            allOf(
                                    matchesCheckNames(`is`("TextContrastViewCheck")),
                                    anyOf(
                                            matchesViews(ViewMatchers.withResourceName("submitButton")),
                                            matchesViews(ViewMatchers.withResourceName("submit_button"))
                                    )
                            ),

                            // On very low-res devices, controls can be squished to the point that they are smaller
                            // than their specified minimum.  This seems unavoidable, so let's not log an
                            // accessibility error for this.
                            underMinSizeOnLowRes(),

                            // Let's ignore size issues with WebViews, since they do not honor minHeight/minWidth
                            // Note that TouchTargetSizeViewCheck is the *old* name of the check.  The new name would
                            // be "TouchTargetSizeCheck".
                            allOf(
                                    matchesViews(ViewMatchers.isAssignableFrom(WebView::class.java)),
                                    matchesCheckNames(`is`("TouchTargetSizeViewCheck"))
                            ),

                            allOf(
                                matchesCheckNames(`is`("SpeakableTextPresentCheck")),
                                matchesViews(
                                    ViewMatchers.withParent(
                                        ViewMatchers.withClassName(
                                            Matchers.equalTo(ComposeView::class.java.name)
                                        )
                                    )
                                )
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
                                    val resourceName = view.context.resources.getResourceName(view.id)
                                    for (excludedName in excludes) {
                                        if (resourceName.contains(excludedName)) {
                                            //Log.v("AccessibilityExclude", "Caught $resourceName")
                                            return true
                                        }
                                    }
                                } catch (_: Resources.NotFoundException) {
                                }
                            }

                            val parent = view.parent
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

    fun isTabletDevice(): Boolean {

        val metrics = activityRule.activity.resources.displayMetrics
        val screenWidth = metrics.widthPixels / metrics.density
        val screenHeight = metrics.heightPixels / metrics.density

        // Assuming a tablet has a minimum width of 720dp and height of 720dp
        return screenWidth >= 720 && screenHeight >= 720
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
        val activity = activityRule.activity
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
                        if (item.view == null) return false
                        val result = item.view!!.width < dim && item.view!!.height >= dim
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
                        val contentDescription = item.view?.contentDescription
                        val result = (contentDescription?.contains("Overflow", ignoreCase = true) ?: false) || (contentDescription?.contains("More options", ignoreCase = true) ?: false)
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
                        if (item.view == null) return false
                        val v = item.view!!
                        val toss =
                                v.height > 0 // Require some dimension in order to be tossed
                                && v.width > 0
                                &&  ( (v.height < 48 && v.height < v.minimumHeight) ||
                                      (v.width < 48 && v.width < v.minimumWidth) )

                        if(toss) {
                            val resourceName = getResourceName(v)
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

    // Copy an asset file to the external cache, typically for use in uploading the asset
    // file via mocked intents.
    fun copyAssetFileToExternalCache(context: Context, fileName: String) {
        var inputStream : InputStream? = null
        var outputStream : OutputStream? = null

        try {
            inputStream = InstrumentationRegistry.getInstrumentation().context.resources.assets.open(fileName)
            val dir = context.externalCacheDir
            val file = File(dir?.path, fileName)
            outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
        }
        finally {
            inputStream?.close()
            if(outputStream != null) {
                outputStream.flush()
                outputStream.close()
            }
        }
    }

    companion object {

        /* Both read & write permission are required for saving screenshots
         * otherwise the code will error with permission denied.
         * Read also required due to a bug specifically with full read/write external storage not being granted
         * https://issuetracker.google.com/issues/64389280 */
        @ClassRule
        @JvmField
        val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private var configChecked = false

        private const val ENABLE_WIFI_COMMAND: String = "svc wifi enable"
        private const val DISABLE_WIFI_COMMAND: String = "svc wifi disable"

        private const val ENABLE_MOBILE_DATA_COMMAND: String = "svc data enable"
        private const val DISABLE_MOBILE_DATA_COMMAND: String = "svc data disable"

        private fun getDeviceOrientation(context: Context): Int {
            val configuration = context.resources.configuration
            return configuration.orientation
        }

        fun isLandscapeDevice(): Boolean {
            return getDeviceOrientation(ApplicationProvider.getApplicationContext()) == Configuration.ORIENTATION_LANDSCAPE
        }

        fun isPortraitDevice(): Boolean {
            return getDeviceOrientation(ApplicationProvider.getApplicationContext()) == Configuration.ORIENTATION_PORTRAIT
        }

        // Does the test device have particularly low screen resolution?
        fun isLowResDevice() : Boolean {
            return ApplicationProvider.getApplicationContext<Context?>().resources.displayMetrics.densityDpi < DisplayMetrics.DENSITY_HIGH
        }

        fun isCompactDevice(): Boolean {
            return ApplicationProvider.getApplicationContext<Context?>().resources.displayMetrics.widthPixels < 600
        }

        fun turnOffConnectionViaADB() {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.executeShellCommand(DISABLE_WIFI_COMMAND)
            device.executeShellCommand(DISABLE_MOBILE_DATA_COMMAND)
        }

        fun turnOnConnectionViaADB() {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.executeShellCommand(ENABLE_WIFI_COMMAND)
            device.executeShellCommand(ENABLE_MOBILE_DATA_COMMAND)
        }

    }

}
