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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.ThemeManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.teacher.R
import com.instructure.teacher.fragments.NotATeacherFragment
import com.instructure.teacher.utils.TeacherPrefs
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Job
import retrofit2.Response

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class SplashActivity : AppCompatActivity() {

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
        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        setContentView(R.layout.activity_splash)

        startUp = weave {
            // Grab user teacher status
            try {

                // Determine if user is a Teacher, Ta, or Designer
                if (!TeacherPrefs.isConfirmedTeacher) {
                    TeacherPrefs.isConfirmedTeacher =
                            awaitApi<List<Course>> { CourseManager.getCoursesWithEnrollmentType( true, it, "teacher") }.isNotEmpty() ||
                            awaitApi<List<Course>> { CourseManager.getCoursesWithEnrollmentType( true, it, "ta") }.isNotEmpty() ||
                            awaitApi<List<Course>> { CourseManager.getCoursesWithEnrollmentType( true, it, "designer") }.isNotEmpty()
                } else {
                    CourseManager.getCoursesWithEnrollmentType(true, mUserIsTeacherVerificationCallback, "teacher")
                }

                // Determine if user can masquerade
                if (ApiPrefs.canBecomeUser == null) {
                    if (ApiPrefs.domain.startsWith("siteadmin", true)) {
                        ApiPrefs.canBecomeUser = true
                    } else try {
                        val account = awaitApi<Account> { UserManager.getSelfAccount(true, it) }
                        val permission = awaitApi<BecomeUserPermission> { UserManager.getBecomeUserPermission(true, account.id, it) }
                        ApiPrefs.canBecomeUser = permission.becomeUser
                    } catch (e: StatusCallbackError) {
                        if (e.response?.code() == 401) ApiPrefs.canBecomeUser = false
                    }
                }

                if (!TeacherPrefs.isConfirmedTeacher && ApiPrefs.canBecomeUser != true) {
                    CourseManager.getCoursesWithEnrollmentType(true, mUserIsTeacherVerificationCallback, "teacher")
                    // The user is not a teacher in any course and cannot masquerade; Show them the door
                    canvasLoadingView.setGone()
                    supportFragmentManager.beginTransaction()
                        .add(R.id.splashActivityRootView, NotATeacherFragment(), NotATeacherFragment::class.java.simpleName)
                        .commit()
                    return@weave
                }

                // Grab colors
                if (ColorKeeper.hasPreviouslySynced) {
                    UserManager.getColors(mUserColorsCallback, true)
                } else {
                    ColorKeeper.addToCache(awaitApi<CanvasColor> { UserManager.getColors(it, true) })
                    ColorKeeper.hasPreviouslySynced = true
                }

                // Grab theme
                if (ThemePrefs.isThemeApplied) {
                    ThemeManager.getTheme(mThemeCallback, true)
                } else {
                    ThemePrefs.applyCanvasTheme(awaitApi { ThemeManager.getTheme(it, true) })
                }
            } catch (e: Throwable) {
                Logger.e(e.message)
            }

            // Set logged user details
            if (Logger.canLogUserDetails()) {
                Logger.d("User detail logging allowed. Setting values.")
                Crashlytics.setUserIdentifier(ApiPrefs.user?.id.toString())
                Crashlytics.setString("domain", ApiPrefs.domain)
            } else {
                Logger.d("User detail logging disallowed. Clearing values.")
                Crashlytics.setUserIdentifier(null)
                Crashlytics.setString("domain", null)
            }

            startActivity(InitActivity.createIntent(this@SplashActivity, intent?.extras))
            canvasLoadingView.announceForAccessibility(getString(R.string.loading))
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        startUp?.cancel()
    }

    private val mThemeCallback = object : StatusCallback<CanvasTheme>() {
        override fun onResponse(response: Response<CanvasTheme>, linkHeaders: LinkHeaders, type: ApiType) {
            //store the theme
            response.body()?.let {
                ThemePrefs.applyCanvasTheme(it)
            }
        }
    }

    private val mUserColorsCallback = object : StatusCallback<CanvasColor>() {
        override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
            if (type == ApiType.API) {
                ColorKeeper.addToCache(response.body())
                ColorKeeper.hasPreviouslySynced = true
            }
        }
    }

    private val mUserIsTeacherVerificationCallback = object : StatusCallback<List<Course>>() {
        override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
            if (response.body()?.isNotEmpty() == true) {
                TeacherPrefs.isConfirmedTeacher = true
            } else {
                CourseManager.getCoursesWithEnrollmentType(true, mUserIsTAVerificationCallback, "ta")
            }
        }
    }

    private val mUserIsTAVerificationCallback = object : StatusCallback<List<Course>>() {
        override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
                TeacherPrefs.isConfirmedTeacher = response.body()?.isNotEmpty() == true
        }
    }
}
