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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.LaunchDefinitionsManager
import com.instructure.canvasapi2.managers.ThemeManager
import com.instructure.canvasapi2.managers.UnreadCountManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Account
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.SelfRegistration
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.canvasapi2.utils.pageview.PandataManager
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.inbox.list.OnUnreadCountInvalidated
import com.instructure.pandautils.room.appdatabase.daos.ToDoFilterDao
import com.instructure.pandautils.room.appdatabase.entities.ToDoFilterEntity
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.utils.SHA256
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.filterByToDoFilters
import com.instructure.pandautils.utils.isComplete
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toast
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.student.fragment.NotificationListFragment
import com.instructure.student.router.EnabledTabs
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.WidgetLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Response
import sdk.pendo.io.Pendo
import javax.inject.Inject

@AndroidEntryPoint
abstract class CallbackActivity : ParentActivity(), OnUnreadCountInvalidated, NotificationListFragment.OnNotificationCountInvalidated {

    @Inject
    lateinit var featureFlagProvider: FeatureFlagProvider

    @Inject
    lateinit var enabledTabs: EnabledTabs

    @Inject
    lateinit var pandataAppKey: PandataInfo.AppKey

    @Inject
    lateinit var userApi: UserAPI.UsersInterface

    @Inject
    lateinit var plannerApi: PlannerAPI.PlannerInterface

    @Inject
    lateinit var widgetLogger: WidgetLogger

    @Inject
    lateinit var toDoFilterDao: ToDoFilterDao

    @Inject
    lateinit var apiPrefs: ApiPrefs

    @Inject
    lateinit var courseApi: CourseAPI.CoursesInterface

    private var loadInitialDataJob: Job? = null

    abstract fun gotLaunchDefinitions(launchDefinitions: List<LaunchDefinition>?)
    abstract fun updateUnreadCount(unreadCount: Int)
    abstract fun increaseUnreadCount(increaseBy: Int)
    abstract fun updateNotificationCount(notificationCount: Int)
    abstract fun updateToDoCount(toDoCount: Int)
    abstract fun initialCoreDataLoadingComplete()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RatingDialog.showRatingDialog(this@CallbackActivity, AppType.STUDENT)
        reloadCoreData()
    }

    private fun loadInitialData() {
        loadInitialDataJob = tryWeave {
            featureFlagProvider.fetchEnvironmentFeatureFlags()

            setupPendoTracking()

            // Get enabled tabs
            enabledTabs.initTabs()

            // Determine if user can masquerade
            if (ApiPrefs.canBecomeUser == null) {
                if (ApiPrefs.domain.startsWith("siteadmin", true)) {
                    ApiPrefs.canBecomeUser = true
                } else try {
                    val account = awaitApi<Account> { UserManager.getSelfAccount(true, it) }
                    val permission = awaitApi { UserManager.getBecomeUserPermission(true, account.id, it) }
                    ApiPrefs.canBecomeUser = permission.becomeUser
                } catch (e: StatusCallbackError) {
                    if (e.response?.code() == 401) ApiPrefs.canBecomeUser = false
                }
            }

            val termsOfService = awaitApi<TermsOfService> { UserManager.getTermsOfService(it, true) }
            ApiPrefs.canGeneratePairingCode = termsOfService.selfRegistrationType == SelfRegistration.ALL
                || termsOfService.selfRegistrationType == SelfRegistration.OBSERVER

            // Grab colors
            // We don't show custom course colors for K5 view so we need to skip this so we don't overwrite course colors.
            if (!ApiPrefs.showElementaryView) {
                if (ColorKeeper.previouslySynced) {
                    UserManager.getColors(userColorsCallback, true)
                } else {
                    ColorKeeper.addToCache(awaitApi<CanvasColor> { UserManager.getColors(it, true) })
                    ColorKeeper.previouslySynced = true
                }
            }

            // Grab theme
            if (ThemePrefs.isThemeApplied) {
                ThemeManager.getTheme(themeCallback, true)
            } else {
                ThemePrefs.applyCanvasTheme(awaitApi { ThemeManager.getTheme(it, true) }, this@CallbackActivity)
            }

            // Refresh pandata info if null or expired
            if (ApiPrefs.pandataInfo?.isValid != true) {
                try {
                    ApiPrefs.pandataInfo = awaitApi<PandataInfo> {
                        PandataManager.getToken(pandataAppKey, it)
                    }
                } catch (ignore: Throwable) {
                    Logger.w("Unable to refresh pandata info")
                }
            }

            // Get course color overlay setting
            UserManager.getSelfSettings(false).await().onSuccess {
                StudentPrefs.hideCourseColorOverlay = it.hideDashCardColorOverlays
            }

            if (!ApiPrefs.isMasquerading) {
                // We don't know how the crashlytics stores the userId so we just set it to empty to make sure we don't log it.
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.setUserId("")
            }

            // get unread count of conversations
            getUnreadMessageCount()

            getUnreadNotificationCount()

            if (RemoteConfigUtils.getBoolean(RemoteConfigParam.TODO_REDESIGN)) {
                getToDoCount()
            }

            val launchDefinitions = awaitApi { LaunchDefinitionsManager.getLaunchDefinitions(it, false) }
            launchDefinitions?.let {
                gotLaunchDefinitions(it)
            }

            initialCoreDataLoadingComplete()
        } catch {
            initialCoreDataLoadingComplete()
        }
    }

    private suspend fun setupPendoTracking() {
        val user = userApi.getSelfWithUUID(RestParams(isForceReadFromNetwork = true)).dataOrNull
        val featureFlagsResult = FeaturesManager.getEnvironmentFeatureFlagsAsync(true).await().dataOrNull
        val sendUsageMetrics = featureFlagsResult?.get(FeaturesManager.SEND_USAGE_METRICS) ?: false
        if (sendUsageMetrics) {
            val visitorData = mapOf("locale" to ApiPrefs.effectiveLocale)
            val accountData = mapOf("surveyOptOut" to featureFlagProvider.checkAccountSurveyNotificationsFlag())
            widgetLogger.cancelLogging()
            Pendo.startSession(user?.uuid?.SHA256().orEmpty(), user?.accountUuid.orEmpty(), visitorData, accountData)
        } else {
            Pendo.endSession()
        }
    }

    private suspend fun getUnreadMessageCount() {
        val unreadCount = awaitApi<UnreadConversationCount> { UnreadCountManager.getUnreadConversationCount(it, true) }
        unreadCount.let {
            val unreadCountInt = (it.unreadCount ?: "0").toInt()
            updateUnreadCount(unreadCountInt)
        }
    }

    protected suspend fun getToDoCount() {
        val todoFilters = toDoFilterDao.findByUser(
            apiPrefs.fullDomain,
            apiPrefs.user?.id.orDefault()
        ) ?: ToDoFilterEntity(userDomain = apiPrefs.fullDomain, userId = apiPrefs.user?.id.orDefault())

        val startDate = todoFilters.pastDateRange.calculatePastDateRange().toApiString()
        val endDate = todoFilters.futureDateRange.calculateFutureDateRange().toApiString()

        val restParams = RestParams(isForceReadFromNetwork = true, usePerPageQueryParam = true)
        val plannerItems = plannerApi.getPlannerItems(
            startDate = startDate,
            endDate = endDate,
            contextCodes = emptyList(),
            restParams = restParams
        ).depaginate { nextUrl ->
            plannerApi.nextPagePlannerItems(nextUrl, restParams)
        }

        val filteredCourses = if (todoFilters.favoriteCourses) {
            val restParams = RestParams(isForceReadFromNetwork = true)
            val allCourses = courseApi.getFirstPageCourses(restParams).depaginate { nextUrl ->
                courseApi.next(nextUrl, restParams)
            }
            allCourses.dataOrNull?.filter { !it.accessRestrictedByDate && !it.isInvited() }.orEmpty()
        } else {
            emptyList()
        }

        // Filter planner items - exclude announcements, assessment requests, completed items
        val todoCount = plannerItems.dataOrNull
            ?.distinctBy { it.id }
            ?.filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST && !it.isComplete() }
            ?.filterByToDoFilters(todoFilters, filteredCourses)
            ?.count() ?: 0
        updateToDoCount(todoCount)
    }

    private fun getUnreadNotificationCount() {
        UnreadCountManager.getUnreadNotificationCount(object : StatusCallback<List<UnreadNotificationCount>>() {
            override fun onResponse(data: Call<List<UnreadNotificationCount>>, response: Response<List<UnreadNotificationCount>>) {
                updateNotificationCount(response.body()?.sumOf { it.unreadCount.orDefault() }.orDefault())
            }
        }, true)
    }

    private val themeCallback = object : StatusCallback<CanvasTheme>() {
        override fun onResponse(response: Response<CanvasTheme>, linkHeaders: LinkHeaders, type: ApiType) {
            //store the theme
            response.body()?.let { ThemePrefs.applyCanvasTheme(it, this@CallbackActivity) }
        }
    }

    private val userColorsCallback = object : StatusCallback<CanvasColor>() {
        override fun onResponse(response: Response<CanvasColor>, linkHeaders: LinkHeaders, type: ApiType) {
            if (type == ApiType.API) {
                ColorKeeper.addToCache(response.body())
                ColorKeeper.previouslySynced = true
            }
        }
    }

    private val userWithDataCallback = object : StatusCallback<User>() {
        override fun onResponse(response: Response<User>, linkHeaders: LinkHeaders, type: ApiType) {
            val user = response.body()!!
            val shouldRestartForLocaleChange = setupUser(user, type)
            if (shouldRestartForLocaleChange) {
                if (BuildConfig.DEBUG) toast(R.string.localeRestartMessage)
                LocaleUtils.restartApp(this@CallbackActivity)
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
                // This has a habit of running after we've retrieved the Test user account when in Student View, which then
                // overrides the Test user info in ApiPrefs. We only want to override if we're not in Student view
                if (!ApiPrefs.isStudentView)
                    ApiPrefs.user = user
                return ApiPrefs.effectiveLocale != oldLocale
            }
            ApiType.CACHE -> if (!APIHelper.hasNetworkConnection()) ApiPrefs.user = user
            ApiType.UNKNOWN -> {}
            else -> {}
        }
        return false
    }

    override fun invalidateUnreadCount() {
        tryWeave {
            getUnreadMessageCount()
        } catch {

        }
    }

    override fun updateUnreadCountOffline(increaseBy: Int) {
        increaseUnreadCount(increaseBy)
    }

    override fun invalidateNotificationCount() {
        getUnreadNotificationCount()
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
