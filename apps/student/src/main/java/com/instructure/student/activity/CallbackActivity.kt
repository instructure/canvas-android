/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.student.activity

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.LaunchDefinitionsManager
import com.instructure.canvasapi2.managers.ThemeManager
import com.instructure.canvasapi2.managers.UnreadCountManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.canvasapi2.utils.pageview.PandataManager
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.utils.*
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.fragment.InboxFragment
import com.instructure.student.service.StudentPageViewService
import com.instructure.student.util.StudentPrefs
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response

abstract class CallbackActivity : ParentActivity(), InboxFragment.OnUnreadCountInvalidated {

    private var loadInitialDataJob: Job? = null

    abstract fun gotLaunchDefinitions(launchDefinitions: List<LaunchDefinition>?)
    abstract fun updateUnreadCount(unreadCount: String)
    abstract fun initialCoreDataLoadingComplete()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RatingDialog.showRatingDialog(this@CallbackActivity, AppType.STUDENT)
        reloadCoreData()
    }

    private fun loadInitialData() {
        loadInitialDataJob = tryWeave {
            // Update Flutter with the login info
            FlutterComm.sendUpdatedLogin()

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

            // Grab colors
            if (ColorKeeper.hasPreviouslySynced) {
                UserManager.getColors(userColorsCallback, true)
            } else {
                ColorKeeper.addToCache(awaitApi<CanvasColor> { UserManager.getColors(it, true) })
                ColorKeeper.hasPreviouslySynced = true
            }

            // Grab theme
            if (ThemePrefs.isThemeApplied) {
                ThemeManager.getTheme(themeCallback, true)
            } else {
                ThemePrefs.applyCanvasTheme(awaitApi { ThemeManager.getTheme(it, true) })
            }

            // Update Flutter with the theme
            FlutterComm.sendUpdatedTheme()

            // Refresh pandata info if null or expired
            if (ApiPrefs.pandataInfo?.isValid != true) {
                try {
                    ApiPrefs.pandataInfo = awaitApi<PandataInfo> {
                        PandataManager.getToken(StudentPageViewService.pandataAppKey, it)
                    }
                } catch (ignore: Throwable) {
                    Logger.w("Unable to refresh pandata info")
                }
            }

            // Get course color overlay setting
            UserManager.getSelfSettings(false).await().onSuccess {
                StudentPrefs.hideCourseColorOverlay = it.hideDashCardColorOverlays
            }

            val launchDefinitions = awaitApi<List<LaunchDefinition>?> { LaunchDefinitionsManager.getLaunchDefinitions(it, false) }
            launchDefinitions?.let {
                val definitions = launchDefinitions.filter { it.domain == LaunchDefinition._STUDIO_DOMAIN || it.domain == LaunchDefinition._GAUGE_DOMAIN }
                gotLaunchDefinitions(definitions)
            }

            if (!ApiPrefs.isMasquerading) {
                // Set logged user details
                if (Logger.canLogUserDetails()) {
                    Logger.d("User detail logging allowed. Setting values.")
                    Crashlytics.setUserIdentifier(ApiPrefs.user?.id.toString())
                    Crashlytics.setUserName(ApiPrefs.domain)
                } else {
                    Logger.d("User detail logging disallowed. Clearing values.")
                    Crashlytics.setUserIdentifier("")
                    Crashlytics.setUserName("----")
                }
            }

            // get unread count of conversations
            getUnreadMessageCount()

            initialCoreDataLoadingComplete()
        } catch {
            initialCoreDataLoadingComplete()
        }
    }

    private suspend fun getUnreadMessageCount() {
        val unreadCount = awaitApi<UnreadConversationCount> { UnreadCountManager.getUnreadConversationCount(it, true) }
        unreadCount.let {
            updateUnreadCount(it.unreadCount!!)
        }
    }

    private val themeCallback = object : StatusCallback<CanvasTheme>() {
        override fun onResponse(response: Response<CanvasTheme>, linkHeaders: LinkHeaders, type: ApiType) {
            //store the theme
            response.body()?.let { ThemePrefs.applyCanvasTheme(it) }

            // Update Flutter with the theme
            FlutterComm.sendUpdatedTheme()
        }
    }

    private val userColorsCallback = object : StatusCallback<CanvasColor>() {
        override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
            if (type == ApiType.API) {
                ColorKeeper.addToCache(response.body())
                ColorKeeper.hasPreviouslySynced = true
            }
        }
    }

    private val userWithDataCallback = object : StatusCallback<User>() {
        override fun onResponse(response: Response<User>, linkHeaders: LinkHeaders, type: ApiType) {
            val user = response.body()!!
            val shouldRestartForLocaleChange = setupUser(user, type)
            if (shouldRestartForLocaleChange) {
                if (BuildConfig.DEBUG) toast(R.string.localeRestartMessage)
                LocaleUtils.restartApp(this@CallbackActivity, LoginActivity::class.java)
            } else {
                loadInitialData()
            }
        }

        override fun onFail(call: Call<User>?, error: Throwable, response: Response<*>?) {
            initialCoreDataLoadingComplete()
        }
    }

    /**
     * Caches the user in ApiPrefs. Returns true if the user's locale has changed and an app restart is required.
     */
    private fun setupUser(user: User, type: ApiType): Boolean {
        /* We don't load from cache on this because it will load the users avatar two times and cause world hunger.
           but if we're masquerading we want to, because masquerading can't get user info, so we need to read it from */
        when (type) {
            ApiType.API -> {
                val oldLocale = ApiPrefs.effectiveLocale
                ApiPrefs.user = user
                return ApiPrefs.effectiveLocale != oldLocale
            }
            ApiType.CACHE -> if (!APIHelper.hasNetworkConnection()) ApiPrefs.user = user
        }
        return false
    }

    override fun invalidateUnreadCount() {
        tryWeave {
            getUnreadMessageCount()
        } catch {

        }
    }

    /**
     * This will fetch the user forcing a network request
     */
    private fun reloadCoreData() {
        UserManager.getSelf(false, userWithDataCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        userWithDataCallback.cancel()
        loadInitialDataJob?.cancel()
        userColorsCallback.cancel()
        themeCallback.cancel()
    }
}
