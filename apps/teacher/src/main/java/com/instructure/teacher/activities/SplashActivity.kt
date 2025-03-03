/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.ThemeManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.BecomeUserPermission
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.awaitOrThrow
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ActivitySplashBinding
import com.instructure.teacher.fragments.NotATeacherFragment
import com.instructure.teacher.utils.LoggingUtility
import com.instructure.teacher.utils.TeacherPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import sdk.pendo.io.Pendo

@AndroidEntryPoint
class SplashActivity : BaseCanvasActivity() {

    private val binding by viewBinding(ActivitySplashBinding::inflate)

    private var startUp: Job? = null

    companion object {
        fun createIntent(context: Context, intentExtra: Bundle?): Intent =
                Intent(context, SplashActivity::class.java).apply {
                    if (intentExtra != null) {
                        // Used for passing up push notification intent
                        this.putExtras(intentExtra)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(getColor(R.color.backgroundLightest)))
        setContentView(binding.root)
        LoggingUtility.log(this.javaClass.simpleName + " --> On Create")
        val masqueradingUserId: Long = intent.getLongExtra(Const.QR_CODE_MASQUERADE_ID, 0L)

        try {
            startUp = weave {
                // Grab user teacher status
                try {
                    val user = awaitApi<User> { UserManager.getSelf(true, it) }
                    val shouldRestartForLocaleChange = setupUser(user)
                    if (shouldRestartForLocaleChange) {
                        if (BuildConfig.DEBUG) toast(R.string.localeRestartMessage)
                        LocaleUtils.restartApp(this@SplashActivity)
                        return@weave
                    }

                    setupHeapTracking()

                    // Determine if user is a Teacher, Ta, or Designer
                    // Use GlobalScope since this can continue executing after SplashActivity is destroyed
                    val enrollmentCheck = GlobalScope.async(start = CoroutineStart.LAZY) {
                        try {
                            TeacherPrefs.isConfirmedTeacher =
                                CourseManager.getCoursesWithEnrollmentType(
                                    true,
                                    "teacher"
                                ).awaitOrThrow().isNotEmpty() ||
                                    CourseManager.getCoursesWithEnrollmentType(
                                        true,
                                        "ta"
                                    ).awaitOrThrow().isNotEmpty() ||
                                    CourseManager.getCoursesWithEnrollmentType(
                                        true,
                                        "designer"
                                    ).awaitOrThrow().isNotEmpty()
                        } catch (e: Throwable) {
                            LoggingUtility.log("${SplashActivity::class.java.simpleName} - Failed to load enrollmentCheck")
                            Logger.e(e.message)
                        }
                    }
                    if (!TeacherPrefs.isConfirmedTeacher) {
                        /*
                     * If the user is not confirmed to have a teacher enrollment, we suspend here and await the result
                     * so that we know whether to show the Not-A-Teacher screen after the splash screen.
                     *
                     * If we have previously confirmed that the user has a teacher enrollment, we want to ensure that
                     * is still the case but we don't want to block the splash screen unnecessarily, so we allow the
                     * check to continue asynchronously. If the user is no longer a teacher, this operation will set
                     * the 'isConfirmedTeacher' flag to false and the result will be checked again synchronously the
                     * next time the app starts.
                     */
                        enrollmentCheck.await()
                    } else {
                        enrollmentCheck.start()
                    }

                    // Determine if user can masquerade - and isn't coming from a masquerade QR scan
                    if (ApiPrefs.canBecomeUser == null && masqueradingUserId == 0L) {
                        if (ApiPrefs.domain.startsWith("siteadmin", true)) {
                            ApiPrefs.canBecomeUser = true
                        } else try {
                            val account = awaitApi<Account> { UserManager.getSelfAccount(true, it) }
                            val permission = awaitApi<BecomeUserPermission> {
                                UserManager.getBecomeUserPermission(
                                    true,
                                    account.id,
                                    it
                                )
                            }
                            ApiPrefs.canBecomeUser = permission.becomeUser
                        } catch (e: StatusCallbackError) {
                            if (e.response?.code() == 401) ApiPrefs.canBecomeUser = false
                        }
                    }

                    if (!TeacherPrefs.isConfirmedTeacher && ApiPrefs.canBecomeUser != true && masqueradingUserId == 0L) {
                        // The user is not a teacher in any course and cannot masquerade; Show them the door
                        binding.canvasLoadingView.setGone()
                        supportFragmentManager.beginTransaction()
                            .add(
                                R.id.splashActivityRootView,
                                NotATeacherFragment(),
                                NotATeacherFragment::class.java.simpleName
                            )
                            .commit()
                        return@weave
                    }

                    // Get course color overlay setting
                    UserManager.getSelfSettings(false).await().onSuccess {
                        TeacherPrefs.hideCourseColorOverlay = it.hideDashCardColorOverlays
                    }

                    // Grab colors
                    // Use GlobalScope since this can continue executing after SplashActivity is destroyed
                    val colorFetch = GlobalScope.async(start = CoroutineStart.LAZY) {
                        try {
                            val canvasColor = awaitApi<CanvasColor> { UserManager.getColors(it, true) }
                            ColorKeeper.addToCache(canvasColor)
                            ColorKeeper.previouslySynced = true
                        } catch (e: Throwable) {
                            LoggingUtility.log("${SplashActivity::class.java.simpleName} - Failed to load colorFetch")
                            Logger.e(e.message)
                        }
                    }
                    if (!ColorKeeper.previouslySynced) colorFetch.await() else colorFetch.start()

                    // Grab theme
                    // Use GlobalScope since this can continue executing after SplashActivity is destroyed
                    val themeFetch = GlobalScope.async(start = CoroutineStart.LAZY) {
                        try {
                            val theme = awaitApi<CanvasTheme> { ThemeManager.getTheme(it, true) }
                            ThemePrefs.applyCanvasTheme(theme, this@SplashActivity)
                        } catch (e: Throwable) {
                            LoggingUtility.log("${SplashActivity::class.java.simpleName} - Failed to load themeFetch")
                            Logger.e(e.message)
                        }
                    }
                    if (!ThemePrefs.isThemeApplied) themeFetch.await() else themeFetch.start()
                } catch (e: Throwable) {
                    Logger.e(e.message)
                }

                // We don't know how the crashlytics stores the userId so we just set it to empty to make sure we don't log it.
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.setUserId("")

                startActivity(InitActivity.createIntent(this@SplashActivity, intent?.extras))
                binding.canvasLoadingView.announceForAccessibility(getString(R.string.loading))
                finish()
            }
        } catch (e: Throwable) {
            Logger.e(e.message)
        }
    }

    /** Caches the user in ApiPrefs. Returns true if the user's locale has changed and an app restart is required. */
    private fun setupUser(user: User): Boolean {
        val oldLocale = ApiPrefs.effectiveLocale
        ApiPrefs.user = user
        return ApiPrefs.effectiveLocale != oldLocale
    }

    private suspend fun setupHeapTracking() {
        val featureFlagsResult = FeaturesManager.getEnvironmentFeatureFlagsAsync(true).await().dataOrNull
        val sendUsageMetrics = featureFlagsResult?.get(FeaturesManager.SEND_USAGE_METRICS) ?: false
        if (sendUsageMetrics) {
            Pendo.startSession("", ApiPrefs.domain, emptyMap(), emptyMap())
        } else {
            Pendo.endSession()
        }
    }

    override fun onStop() {
        super.onStop()
        LoggingUtility.log(this.javaClass.simpleName + " --> On Stop")
        startUp?.cancel()
    }
}
