/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
 *
 */
package com.instructure.parentapp.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.BecomeUserPermission
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.toast
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.R
import com.instructure.parentapp.fragments.NotAParentFragment
import com.instructure.parentapp.tasks.ParentLogoutTask
import com.instructure.parentapp.util.ParentPrefs
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private var checkSignedInJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        setContentView(R.layout.activity_splash)

        if (intent?.getBooleanExtra(Const.SHOW_MESSAGE, false) == true) {
            val msg = intent.getIntExtra(Const.MESSAGE_TO_USER, -1)
            if (msg != -1)
                Toast.makeText(this@SplashActivity, msg, Toast.LENGTH_SHORT).show()
        }

        when {
            ApiPrefs.getValidToken().isNotBlank() -> checkSignedIn() // They have a token
            else -> navigateLoginLandingPage() // They have no token
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun checkSignedIn() {
        checkSignedInJob = tryWeave {
            // Now get it from the new place. This will be the true token whether they signed into dev/retrofit or the old way.
            val token = ApiPrefs.getValidToken()
            ApiPrefs.protocol = "https"

            val user = awaitApi<User> { UserManager.getSelf(true, it) }
            val shouldRestartForLocaleChange = setupUser(user)
            if (shouldRestartForLocaleChange) {
                if (BuildConfig.DEBUG) toast(R.string.localeRestartMessage)
                LocaleUtils.restartApp(this@SplashActivity, LoginActivity::class.java)
                return@tryWeave
            }

            if (ParentPrefs.isObserver == null) {
                val enrollments = awaitApi<List<Enrollment>> { EnrollmentManager.getObserveeEnrollments(true, it) }
                ParentPrefs.isObserver = enrollments.any { it.isObserver }
            }

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

            if (ParentPrefs.isObserver == false && ApiPrefs.canBecomeUser != true) {
                canvasLoadingView.setGone()
                supportFragmentManager.beginTransaction()
                    .add(R.id.splashActivityRootView, NotAParentFragment(), NotAParentFragment::class.java.simpleName)
                    .commit()
                return@tryWeave
            }

            if (token.isNotBlank()) {
                EnrollmentManager.getObserveeEnrollments(true, object: StatusCallback<List<Enrollment>>() {
                    @Suppress("ConvertCallChainIntoSequence")
                    override fun onResponse(response: Response<List<Enrollment>>, linkHeaders: LinkHeaders, type: ApiType) {
                        super.onResponse(response, linkHeaders, type)
                        response.body()?.let { enrollments ->
                            // Use distinct() to prevent duplicates
                            val students = enrollments.mapNotNull { it.observedUser }.distinct()

                            // Note: If the user has no observed students we still let them in
                            val intent = NavigationActivity.createIntent(ContextKeeper.appContext, students)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onFail(call: Call<List<Enrollment>>?, error: Throwable, response: Response<*>?) {
                        super.onFail(call, error, response)
                        // The api call failed, if they're unauthorized log them out
                        // Their token may have changed - make them start over
                        if (response?.code() == 401 && !TextUtils.isEmpty(ApiPrefs.getValidToken())) {
                            ParentLogoutTask(LogoutTask.Type.LOGOUT).execute()
                        }
                    }
                })
            }
        } catch {
            Logger.e(it.message)
            Logger.e(it.stackTrace.toString())
        }
    }

    /** Caches the user in ApiPrefs. Returns true if the user's locale has changed and an app restart is required. */
    private fun setupUser(user: User): Boolean {
        val oldLocale = ApiPrefs.effectiveLocale
        ApiPrefs.user = user
        return ApiPrefs.effectiveLocale != oldLocale
    }

    private fun navigateLoginLandingPage() {
        startActivity(LoginActivity.createIntent(ContextKeeper.appContext))
        finish()
    }

    companion object {

        @JvmStatic fun createIntent(context: Context): Intent {
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            return intent
        }

        @JvmStatic fun createIntent(context: Context, showMessage: Boolean, msgId: Int): Intent {
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            intent.putExtra(Const.SHOW_MESSAGE, showMessage)
            intent.putExtra(Const.MESSAGE_TO_USER, msgId)
            return intent
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkSignedInJob?.cancel()
    }
}
