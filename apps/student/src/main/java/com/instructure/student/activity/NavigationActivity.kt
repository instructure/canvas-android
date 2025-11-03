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
@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")
package com.instructure.student.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.annotation.PluralsRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.StorageQuotaExceededError
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.FullScreenInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.dialog.MasqueradingDialog
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.analytics.OfflineAnalyticsManager
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.calendar.CalendarFragment
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.features.help.HelpDialogFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.features.notification.preferences.PushNotificationPreferencesFragment
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.features.settings.SettingsFragment
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.pandautils.update.UpdateManager
import com.instructure.pandautils.utils.ActivityResult
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import com.instructure.pandautils.utils.LocaleUtils
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.applyHorizontalSystemBarInsets
import com.instructure.pandautils.utils.OnActivityResults
import com.instructure.pandautils.utils.OnBackStackChangedEvent
import com.instructure.pandautils.utils.PermissionReceiver
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.RequestCodes.CAMERA_PIC_REQUEST
import com.instructure.pandautils.utils.RequestCodes.PICK_FILE_FROM_DEVICE
import com.instructure.pandautils.utils.RequestCodes.PICK_IMAGE_GALLERY
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.WebViewAuthenticator
import com.instructure.pandautils.utils.applyTheme
import com.instructure.pandautils.utils.hideKeyboard
import com.instructure.pandautils.utils.isAccessibilityEnabled
import com.instructure.pandautils.utils.items
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.post
import com.instructure.pandautils.utils.postSticky
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.databinding.ActivityNavigationBinding
import com.instructure.student.databinding.LoadingCanvasViewBinding
import com.instructure.student.databinding.NavigationDrawerBinding
import com.instructure.student.dialog.BookmarkCreationDialog
import com.instructure.student.events.CoreDataFinishedLoading
import com.instructure.student.events.CourseColorOverlayToggledEvent
import com.instructure.student.events.ShowConfettiEvent
import com.instructure.student.events.ShowGradesToggledEvent
import com.instructure.student.events.UserUpdatedEvent
import com.instructure.student.features.files.list.FileListFragment
import com.instructure.student.features.modules.progression.CourseModuleProgressionFragment
import com.instructure.student.features.navigation.NavigationRepository
import com.instructure.student.fragment.BookmarksFragment
import com.instructure.student.fragment.DashboardFragment
import com.instructure.student.fragment.NotificationListFragment
import com.instructure.student.fragment.ToDoListFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.navigation.AccountMenuItem
import com.instructure.student.navigation.NavigationBehavior
import com.instructure.student.navigation.NavigationMenuItem
import com.instructure.student.navigation.OptionsMenuItem
import com.instructure.student.router.EnabledTabs
import com.instructure.student.router.RouteMatcher
import com.instructure.student.router.RouteResolver
import com.instructure.student.tasks.StudentLogoutTask
import com.instructure.student.util.Analytics
import com.instructure.student.util.AppShortcutManager
import com.instructure.student.util.StudentPrefs
import com.instructure.student.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayDeque
import java.util.Deque
import javax.inject.Inject

private const val BOTTOM_NAV_SCREEN = "bottomNavScreen"
private const val BOTTOM_SCREENS_BUNDLE_KEY = "bottomScreens"

@AndroidEntryPoint
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class NavigationActivity : BaseRouterActivity(), Navigation, MasqueradingDialog.OnMasqueradingSet,
    FullScreenInteractions, ActivityCompat.OnRequestPermissionsResultCallback by PermissionReceiver() {

    private val binding by viewBinding(ActivityNavigationBinding::inflate)
    private lateinit var navigationDrawerBinding: NavigationDrawerBinding
    private lateinit var canvasLoadingBinding: LoadingCanvasViewBinding

    @Inject
    lateinit var navigationBehavior: NavigationBehavior

    @Inject
    lateinit var appShortcutManager: AppShortcutManager

    @Inject
    lateinit var typefaceBehavior: TypefaceBehavior

    @Inject
    lateinit var updateManager: UpdateManager

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    @Inject
    lateinit var databaseProvider: DatabaseProvider

    @Inject
    lateinit var repository: NavigationRepository

    @Inject
    lateinit var offlineDatabase: OfflineDatabase

    @Inject
    lateinit var offlineSyncHelper: OfflineSyncHelper

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var offlineAnalyticsManager: OfflineAnalyticsManager

    @Inject
    lateinit var enabledCourseTabs: EnabledTabs

    @Inject
    lateinit var webViewAuthenticator: WebViewAuthenticator

    private var routeJob: WeaveJob? = null
    private var debounceJob: Job? = null
    private var drawerItemSelectedJob: Job? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var colorOverlayJob: Job? = null

    private val bottomNavScreensStack: Deque<String> = ArrayDeque()

    private val notificationsPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun contentResId(): Int = R.layout.activity_navigation

    private val isDrawerOpen: Boolean
        get() = binding.drawerLayout.isDrawerOpen(navigationDrawerBinding.navigationDrawer)

    private val mNavigationDrawerItemClickListener = View.OnClickListener { v ->
        drawerItemSelectedJob = weave {
            closeNavigationDrawer()
            delay(250)
            when (v.id) {
                R.id.navigationDrawerItem_help -> {
                    HelpDialogFragment.show(this@NavigationActivity)
                }
                R.id.navigationDrawerItem_files -> {
                    ApiPrefs.user?.let { handleRoute(FileListFragment.makeRoute(it)) }
                }
                R.id.navigationDrawerItem_gauge, R.id.navigationDrawerItem_studio, R.id.navigationDrawerItem_mastery -> {
                    val launchDefinition = v.tag as? LaunchDefinition ?: return@weave
                    launchLti(launchDefinition)
                }
                R.id.navigationDrawerItem_bookmarks -> {
                    val route = BookmarksFragment.makeRoute(ApiPrefs.user)
                    addFragment(
                            BookmarksFragment.newInstance(route) {
                                RouteMatcher.routeUrl(this@NavigationActivity, it.url!!)
                            }, route)
                }
                R.id.navigationDrawerItem_changeUser -> {
                    StudentLogoutTask(
                        if (ApiPrefs.isStudentView) LogoutTask.Type.LOGOUT else LogoutTask.Type.SWITCH_USERS,
                        typefaceBehavior = typefaceBehavior,
                        databaseProvider = databaseProvider,
                        alarmScheduler = alarmScheduler
                    ).execute()
                }
                R.id.navigationDrawerItem_logout -> {
                    AlertDialog.Builder(this@NavigationActivity)
                            .setTitle(R.string.logout_warning)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                StudentLogoutTask(
                                    LogoutTask.Type.LOGOUT,
                                    typefaceBehavior = typefaceBehavior,
                                    databaseProvider = databaseProvider,
                                    alarmScheduler = alarmScheduler
                                ).execute()
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                            .show()
                }
                R.id.navigationDrawerItem_startMasquerading -> {
                    MasqueradingDialog.show(supportFragmentManager, ApiPrefs.domain, null, !isTablet)
                }
                R.id.navigationDrawerItem_stopMasquerading -> {
                    MasqueradeHelper.stopMasquerading(startActivityClass)
                }
                R.id.navigationDrawerSettings -> {
                    val route = SettingsFragment.makeRoute(featureFlagProvider.offlineEnabled())
                    val fragment = SettingsFragment.newInstance(route)
                    addFragment(fragment, route)
                }
                R.id.navigationDrawerItem_closeDrawer -> {
                    closeNavigationDrawer()
                }
            }
        }
    }

    private fun launchLti(launchDefinition: LaunchDefinition) {
        val user = ApiPrefs.user ?: return
        val title = launchDefinition.name
        val route = LtiLaunchFragment.makeRoute(
            canvasContext = CanvasContext.currentUserContext(user),
            url = launchDefinition.placements?.globalNavigation?.url.orEmpty(),
            title = title,
            sessionLessLaunch = true
        )
        RouteMatcher.route(this, route)
    }

    private val onBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        currentFragment?.let {
            // Sends a broadcast event to notify the backstack has changed and which fragment class is on top.
            OnBackStackChangedEvent(it::class.java).post()
            applyCurrentFragmentTheme()

            /* Update nav bar visibility to show for specific 'root' fragments. Also show the nav bar when there is
             only one fragment on the backstack, which commonly occurs with non-root fragments when routing
             from external sources. */
            val visible = isBottomNavFragment(it) || supportFragmentManager.backStackEntryCount <= 1
            binding.bottomBar.setVisible(visible)
            binding.bottomBarDivider.setVisible(visible)
        }
    }

    override fun onResume() {
        super.onResume()
        applyCurrentFragmentTheme()
        webViewAuthenticator.authenticateWebViews(lifecycleScope, this)
    }

    private fun checkAppUpdates() {
        updateManager.checkForInAppUpdate(this)
    }

    private fun applyCurrentFragmentTheme() {
        Handler().post {
            (currentFragment as? FragmentInteractions)?.let {
                it.applyTheme()
                setBottomBarItemSelected(it as Fragment)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (bottomNavScreensStack.isNotEmpty()) {
            val bottomScreens = ArrayList(bottomNavScreensStack.toList())
            outState.putStringArrayList(BOTTOM_SCREENS_BUNDLE_KEY, bottomScreens)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)
        RouteMatcher.offlineDb = offlineDatabase
        RouteMatcher.networkStateProvider = networkStateProvider
        RouteMatcher.enabledTabs = enabledCourseTabs
        navigationDrawerBinding = NavigationDrawerBinding.bind(binding.root)
        canvasLoadingBinding = LoadingCanvasViewBinding.bind(binding.root)
        setContentView(binding.root)

        setupWindowInsets()

        val masqueradingUserId: Long = intent.getLongExtra(Const.QR_CODE_MASQUERADE_ID, 0L)
        if (masqueradingUserId != 0L) {
            MasqueradeHelper.startMasquerading(masqueradingUserId, ApiPrefs.domain, NavigationActivity::class.java)
            finish()
        }

        binding.bottomBar.inflateMenu(navigationBehavior.bottomBarMenu)

        supportFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)

        if (savedInstanceState == null) {
            if (hasUnreadPushNotification(intent.extras) || hasLocalNotificationLink(intent.extras)) {
                handlePushNotification(hasUnreadPushNotification(intent.extras))
            }
        }

        appShortcutManager.make(this)

        setupNavDrawerItems()

        checkAppUpdates()

        val savedBottomScreens = savedInstanceState?.getStringArrayList(BOTTOM_SCREENS_BUNDLE_KEY)
        restoreBottomNavState(savedBottomScreens)

        requestNotificationsPermission()

        networkStateProvider.isOnlineLiveData.observe(this) { isOnline ->
            logOfflineEvents(isOnline)
            setOfflineState(!isOnline)
            handleTokenCheck(isOnline)
        }

        lifecycleScope.tryLaunch {
            offlineSyncHelper.scheduleWorkAfterLogin()
        } catch {
            firebaseCrashlytics.recordException(it)
        }

        scheduleAlarms()

        WidgetUpdater.updateWidgets()
    }

    private fun setupWindowInsets() = with(binding) {
        ViewCompat.setOnApplyWindowInsetsListener(fullscreen) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                0
            )
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }

        navigationDrawerBinding.navigationDrawer.applyHorizontalSystemBarInsets()
    }

    private fun logOfflineEvents(isOnline: Boolean) {
        lifecycleScope.launch {
            if (isOnline) {
                offlineAnalyticsManager.offlineModeEnded()
            } else {
                offlineAnalyticsManager.offlineModeStarted()
            }
        }
    }

    private fun handleTokenCheck(online: Boolean?) {
        val checkToken = ApiPrefs.checkTokenAfterOfflineLogin
        if (checkToken && online == true) {
            ApiPrefs.checkTokenAfterOfflineLogin = false
            lifecycleScope.launch {
                val isTokenValid = repository.isTokenValid()
                if (!isTokenValid) {
                    StudentLogoutTask(
                        LogoutTask.Type.LOGOUT,
                        typefaceBehavior = typefaceBehavior,
                        databaseProvider = databaseProvider,
                        alarmScheduler = alarmScheduler
                    ).execute()
                }
            }
        }
    }

    private fun requestNotificationsPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationsPermissionContract.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun restoreBottomNavState(savedBottomScreens: List<String>?) {
        if (savedBottomScreens != null && savedBottomScreens.isNotEmpty() && bottomNavScreensStack.isEmpty()) {
            savedBottomScreens.reversed().forEach { bottomNavScreensStack.push(it) }
        }

        currentFragment?.let {
            val visible = isBottomNavFragment(it) || supportFragmentManager.backStackEntryCount <= 1
            binding.bottomBar.setVisible(visible)
            binding.bottomBarDivider.setVisible(visible)
        }
    }

    private fun setupNavDrawerItems() {
        navigationDrawerBinding.navigationDrawerItemFiles.setVisible(navigationBehavior.visibleNavigationMenuItems.contains(NavigationMenuItem.FILES))
        navigationDrawerBinding.navigationDrawerItemBookmarks.setVisible(navigationBehavior.visibleNavigationMenuItems.contains(NavigationMenuItem.BOOKMARKS))
        navigationDrawerBinding.navigationDrawerSettings.setVisible(navigationBehavior.visibleNavigationMenuItems.contains(NavigationMenuItem.SETTINGS))
        navigationDrawerBinding.navigationMenuItemsDivider.setVisible(navigationBehavior.visibleNavigationMenuItems.isNotEmpty())

        navigationDrawerBinding.optionsMenuTitle.setVisible(navigationBehavior.visibleOptionsMenuItems.isNotEmpty())
        navigationDrawerBinding.navigationDrawerItemShowGrades.setVisible(navigationBehavior.visibleOptionsMenuItems.contains(OptionsMenuItem.SHOW_GRADES))
        navigationDrawerBinding.navigationDrawerItemColorOverlay.setVisible(navigationBehavior.visibleOptionsMenuItems.contains(OptionsMenuItem.COLOR_OVERLAY))
        navigationDrawerBinding.optionsMenuItemsDivider.setVisible(navigationBehavior.visibleOptionsMenuItems.isNotEmpty())

        navigationDrawerBinding.navigationDrawerItemHelp.setVisible(navigationBehavior.visibleAccountMenuItems.contains(AccountMenuItem.HELP))
        navigationDrawerBinding.navigationDrawerItemChangeUser.setVisible(navigationBehavior.visibleAccountMenuItems.contains(AccountMenuItem.CHANGE_USER))
        navigationDrawerBinding.navigationDrawerItemLogout.setVisible(navigationBehavior.visibleAccountMenuItems.contains(AccountMenuItem.LOGOUT))
    }

    override fun initialCoreDataLoadingComplete() {
        // We are ready to load our UI
        if (currentFragment == null) {
            loadLandingPage(true)
        }

        if (ApiPrefs.user == null ) {
            // Hard case to repro but it's possible for a user to force exit the app before we finish saving the user but they will still launch into the app
            // If that happens, log out
            StudentLogoutTask(LogoutTask.Type.LOGOUT, databaseProvider = databaseProvider, alarmScheduler = alarmScheduler).execute()
        }

        setupBottomNavigation()

        // There is a chance our fragment may attach before we have our core data back.
        EventBus.getDefault().post(CoreDataFinishedLoading)
        applyCurrentFragmentTheme()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        logOfflineEvents(networkStateProvider.isOnline())
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        logOfflineEvents(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        debounceJob?.cancel()
        drawerItemSelectedJob?.cancel()
        routeJob?.cancel()
        colorOverlayJob?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_PIC_REQUEST ||
            requestCode == PICK_FILE_FROM_DEVICE ||
            requestCode == PICK_IMAGE_GALLERY ||
            PickerSubmissionUploadEffectHandler.isPickerRequest(requestCode) ||
            SubmissionDetailsEmptyContentFragment.isFileRequest(requestCode)
        ) {
            // UploadFilesFragment will not be notified of onActivityResult(), alert manually
            OnActivityResults(ActivityResult(requestCode, resultCode, data), null).postSticky()
        }
    }

    override fun loadLandingPage(clearBackStack: Boolean) {
        if (clearBackStack) clearBackStack(navigationBehavior.homeFragmentClass)
        selectBottomNavFragment(navigationBehavior.homeFragmentClass)
        bottomNavScreensStack.clear()

        if (intent.extras?.containsKey(AppShortcutManager.APP_SHORTCUT_PLACEMENT) == true) {
            // Launch to the app shortcut placement
            val placement = intent.extras!!.getString(AppShortcutManager.APP_SHORTCUT_PLACEMENT)

            // Remove the extra so we don't accidentally launch into the shortcut again.
            intent.extras!!.remove(AppShortcutManager.APP_SHORTCUT_PLACEMENT)

            when (placement) {
                AppShortcutManager.APP_SHORTCUT_BOOKMARKS -> {
                    val route = BookmarksFragment.makeRoute(ApiPrefs.user)
                    addFragment(BookmarksFragment.newInstance(route) { RouteMatcher.routeUrl(this, it.url!!) }, route)
                }
                AppShortcutManager.APP_SHORTCUT_CALENDAR -> selectBottomNavFragment(
                    CalendarFragment::class.java)
                AppShortcutManager.APP_SHORTCUT_TODO -> selectBottomNavFragment(ToDoListFragment::class.java)
                AppShortcutManager.APP_SHORTCUT_NOTIFICATIONS -> selectBottomNavFragment(NotificationListFragment::class.java)
                AppShortcutManager.APP_SHORTCUT_INBOX -> {
                    if (ApiPrefs.isStudentView) {
                        // Inbox not available in Student View
                        selectBottomNavFragment(NothingToSeeHereFragment::class.java)
                    } else {
                        selectBottomNavFragment(InboxFragment::class.java)
                    }
                }
            }
        }
    }

    override fun showHomeAsUp(): Boolean = false

    override fun showTitleEnabled(): Boolean = true

    override fun onUpPressed() {}

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Switching languages will trigger this, so we check for our Pending intent id
        if (hasPendingLanguageIntent(intent.extras) ||
            hasLocalNotificationLink(intent.extras) ||
            hasUnreadPushNotification(intent.extras)
        ) {
            handlePushNotification(hasUnreadPushNotification(intent.extras))
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // Setup the actionbar but make sure we call super last so the fragments can override it as needed.
        mDrawerToggle?.onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
        applyThemeForAllFragments()
    }

    private fun applyThemeForAllFragments() {
        supportFragmentManager.fragments.forEach {
            (it as? FragmentInteractions)?.applyTheme()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle?.syncState()
    }

    //region Navigation Drawer

    private fun setupUserDetails(user: User?) = with(navigationDrawerBinding) {
        if (user != null) {
            navigationDrawerUserName.text = Pronouns.span(user.shortName, user.pronouns)
            navigationDrawerUserEmail.text = user.primaryEmail
            ProfileUtils.loadAvatarForUser(navigationDrawerProfileImage, user.shortName, user.avatarUrl)
        }
    }

    private fun closeNavigationDrawer() {
        binding.drawerLayout.closeDrawer(navigationDrawerBinding.navigationDrawer)
    }

    fun openNavigationDrawer() {
        binding.drawerLayout.openDrawer(navigationDrawerBinding.navigationDrawer)
    }

    override fun <F> attachNavigationDrawer(fragment: F, toolbar: Toolbar?) where F : Fragment, F : FragmentInteractions {
        //Navigation items
        with(navigationDrawerBinding) {
            navigationDrawerItemFiles.onClickWithRequireNetwork(mNavigationDrawerItemClickListener)
            navigationDrawerItemGauge.onClickWithRequireNetwork(mNavigationDrawerItemClickListener)
            navigationDrawerItemStudio.onClickWithRequireNetwork(mNavigationDrawerItemClickListener)
            navigationDrawerItemMastery.onClickWithRequireNetwork(mNavigationDrawerItemClickListener)
            navigationDrawerItemBookmarks.onClickWithRequireNetwork(mNavigationDrawerItemClickListener)
            navigationDrawerItemChangeUser.setOnClickListener(mNavigationDrawerItemClickListener)
            navigationDrawerItemHelp.onClickWithRequireNetwork(mNavigationDrawerItemClickListener)
            navigationDrawerItemLogout.setOnClickListener(mNavigationDrawerItemClickListener)
            navigationDrawerSettings.setOnClickListener(mNavigationDrawerItemClickListener)
            navigationDrawerItemStartMasquerading.setOnClickListener(mNavigationDrawerItemClickListener)
            navigationDrawerItemStopMasquerading.setOnClickListener(mNavigationDrawerItemClickListener)
            navigationDrawerItemCloseDrawer.setOnClickListener(mNavigationDrawerItemClickListener)
            listOf(
                navigationDrawerItemFiles,
                navigationDrawerItemGauge,
                navigationDrawerItemStudio,
                navigationDrawerItemMastery,
                navigationDrawerItemBookmarks,
                navigationDrawerItemChangeUser,
                navigationDrawerItemHelp,
                navigationDrawerItemLogout,
                navigationDrawerSettings,
                navigationDrawerItemStartMasquerading,
                navigationDrawerItemStopMasquerading,
                navigationDrawerItemCloseDrawer
            ).forEach {
                it.accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(
                        host: View,
                        info: AccessibilityNodeInfo
                    ) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = "android.widget.Button"
                    }
                }
            }
        }


        //Load Show Grades
        navigationDrawerBinding.navigationDrawerShowGradesSwitch.isChecked = StudentPrefs.showGradesOnCard
        navigationDrawerBinding.navigationDrawerShowGradesSwitch.setOnCheckedChangeListener { _, isChecked ->
            StudentPrefs.showGradesOnCard = isChecked
            EventBus.getDefault().post(ShowGradesToggledEvent)
        }
        ViewStyler.themeSwitch(this@NavigationActivity, navigationDrawerBinding.navigationDrawerShowGradesSwitch, ThemePrefs.brandColor)

        // Set up Color Overlay setting
        setUpColorOverlaySwitch()

        //Load version
        try {
            val navigationDrawerVersion = findViewById<TextView>(R.id.navigationDrawerVersion)
            navigationDrawerVersion.text = String.format(getString(R.string.version),
                    packageManager.getPackageInfo(applicationInfo.packageName, 0).versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("Error getting version: " + e)
        }

        if (isBottomNavFragment(fragment)) {
            toolbar?.setNavigationIcon(R.drawable.ic_hamburger)
            toolbar?.navigationContentDescription = getString(R.string.navigation_drawer_open)
            toolbar?.setNavigationOnClickListener {
                openNavigationDrawer()
            }
        } else {
            toolbar?.setupAsBackButton(fragment)
        }

        binding.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)

        mDrawerToggle = object : ActionBarDrawerToggle(this@NavigationActivity, binding.drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu()
                setCloseDrawerVisibility()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                invalidateOptionsMenu()
                // Make the scrollview that is inside the drawer scroll to the top
                navigationDrawerBinding.navigationDrawer.scrollTo(0, 0)
            }
        }

        binding.drawerLayout.post { mDrawerToggle!!.syncState() }
        binding.drawerLayout.addDrawerListener(mDrawerToggle!!)

        setupUserDetails(ApiPrefs.user)

        toolbar?.let {
            ViewStyler.themeToolbarColored(this, it, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        }

        navigationDrawerBinding.navigationDrawerItemStartMasquerading.setVisible(!ApiPrefs.isMasquerading && ApiPrefs.canBecomeUser == true)
        navigationDrawerBinding.navigationDrawerItemStopMasquerading.setVisible(ApiPrefs.isMasquerading)
    }

    private fun setUpColorOverlaySwitch() = with(navigationDrawerBinding) {
        navigationDrawerColorOverlaySwitch.isChecked = !StudentPrefs.hideCourseColorOverlay
        lateinit var checkListener: CompoundButton.OnCheckedChangeListener
        checkListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            colorOverlayJob?.cancel()
            colorOverlayJob = GlobalScope.launch(Dispatchers.Main) {
                navigationDrawerColorOverlaySwitch.isEnabled = false
                UserManager.setHideColorOverlay(!isChecked).await()
                    .onSuccess {
                        StudentPrefs.hideCourseColorOverlay = it.hideDashCardColorOverlays
                        CanvasRestAdapter.clearCacheUrls("""/users/self/settings""")
                        EventBus.getDefault().post(CourseColorOverlayToggledEvent)
                    }
                    .onFailure {
                        toast(R.string.errorOccurred)
                        navigationDrawerColorOverlaySwitch.setOnCheckedChangeListener(null)
                        navigationDrawerColorOverlaySwitch.isChecked = !isChecked
                        navigationDrawerColorOverlaySwitch.setOnCheckedChangeListener(checkListener)
                    }
                navigationDrawerColorOverlaySwitch.isEnabled = true
            }
        }
        navigationDrawerColorOverlaySwitch.setOnCheckedChangeListener(checkListener)
        ViewStyler.themeSwitch(this@NavigationActivity, navigationDrawerColorOverlaySwitch, ThemePrefs.brandColor)
    }

    private fun setOfflineState(isOffline: Boolean) {
        binding.offlineIndicator.root.setVisible(isOffline)
        with(navigationDrawerBinding) {
            navigationDrawerOfflineIndicator.setVisible(isOffline)
            navigationDrawerItemStudio.alpha = if (isOffline) 0.5f else 1f
            navigationDrawerItemGauge.alpha = if (isOffline) 0.5f else 1f
            navigationDrawerItemHelp.alpha = if (isOffline) 0.5f else 1f
            navigationDrawerItemBookmarks.alpha = if (isOffline) 0.5f else 1f
            navigationDrawerItemFiles.alpha = if (isOffline) 0.5f else 1f
            navigationDrawerItemColorOverlay.alpha = if (isOffline) 0.5f else 1f
            navigationDrawerColorOverlaySwitch.isEnabled = !isOffline
        }

        with(binding.bottomBar.menu) {
            findItem(R.id.bottomNavigationCalendar).isEnabled = !isOffline
            findItem(R.id.bottomNavigationToDo).isEnabled = !isOffline
            findItem(R.id.bottomNavigationNotifications).isEnabled = !isOffline
            findItem(R.id.bottomNavigationInbox).isEnabled = !isOffline
        }
    }

    override fun onStartMasquerading(domain: String, userId: Long) {
        MasqueradeHelper.startMasquerading(userId, domain, NavigationActivity::class.java)
    }

    override fun onStopMasquerading() {
        MasqueradeHelper.stopMasquerading(NavigationActivity::class.java)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUserUpdatedEvent(event: UserUpdatedEvent){
        event.once(javaClass.simpleName) {
            setupUserDetails(it)
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQuotaExceeded(errorCode: StorageQuotaExceededError) {
        toast(R.string.fileQuotaExceeded)
    }

    override fun overrideFont() {
        super.overrideFont()
        typefaceBehavior.overrideFont(navigationBehavior.canvasFont)
    }

    //endregion

    //region Bottom Bar Navigation

    private val bottomBarItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
        when (item.itemId) {
            R.id.bottomNavigationHome -> selectBottomNavFragment(navigationBehavior.homeFragmentClass)
            R.id.bottomNavigationCalendar -> selectBottomNavFragment(CalendarFragment::class.java)
            R.id.bottomNavigationToDo -> selectBottomNavFragment(ToDoListFragment::class.java)
            R.id.bottomNavigationNotifications -> selectBottomNavFragment(NotificationListFragment::class.java)
            R.id.bottomNavigationInbox -> {
                if (ApiPrefs.isStudentView) {
                    selectBottomNavFragment(NothingToSeeHereFragment::class.java)
                } else {
                    selectBottomNavFragment(InboxFragment::class.java)
                }
            }
        }
        true
    }

    private val bottomBarItemReselectedListener = BottomNavigationView.OnNavigationItemReselectedListener { item: MenuItem ->
        // If the top fragment != courses, calendar, to-do, notifications, inbox then load the item

        var abortReselect = true
        topFragment?.let {
            val currentFragmentClass = it::class.java
            when (item.itemId) {
                R.id.bottomNavigationHome -> abortReselect = currentFragmentClass.isAssignableFrom(navigationBehavior.homeFragmentClass)
                R.id.bottomNavigationCalendar -> abortReselect = currentFragmentClass.isAssignableFrom(
                    CalendarFragment::class.java)
                R.id.bottomNavigationToDo -> abortReselect = currentFragmentClass.isAssignableFrom(ToDoListFragment::class.java)
                R.id.bottomNavigationNotifications -> abortReselect = currentFragmentClass.isAssignableFrom(NotificationListFragment::class.java)
                R.id.bottomNavigationInbox -> abortReselect = currentFragmentClass.isAssignableFrom(InboxFragment::class.java)
            }
        }

        if(!abortReselect) {
            when (item.itemId) {
                R.id.bottomNavigationHome -> selectBottomNavFragment(navigationBehavior.homeFragmentClass)
                R.id.bottomNavigationCalendar -> selectBottomNavFragment(CalendarFragment::class.java)
                R.id.bottomNavigationToDo -> selectBottomNavFragment(ToDoListFragment::class.java)
                R.id.bottomNavigationNotifications -> selectBottomNavFragment(NotificationListFragment::class.java)
                R.id.bottomNavigationInbox -> {
                    if (ApiPrefs.isStudentView) {
                        selectBottomNavFragment(NothingToSeeHereFragment::class.java)
                    } else {
                        selectBottomNavFragment(InboxFragment::class.java)
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation() = with(binding) {
        Logger.d("NavigationActivity:setupBottomNavigation()")
        bottomBar.applyTheme(ThemePrefs.brandColor, ContextCompat.getColor(this@NavigationActivity, R.color.textDarkest))
        bottomBar.setOnNavigationItemSelectedListener(bottomBarItemSelectedListener)
        bottomBar.setOnNavigationItemReselectedListener(bottomBarItemReselectedListener)
        updateBottomBarContentDescriptions()
    }

    private fun setBottomBarItemSelected(itemId: Int) = with(binding) {
        bottomBar.setOnNavigationItemReselectedListener(null)
        bottomBar.setOnNavigationItemSelectedListener(null)
        bottomBar.selectedItemId = itemId
        bottomBar.setOnNavigationItemSelectedListener(bottomBarItemSelectedListener)
        bottomBar.setOnNavigationItemReselectedListener(bottomBarItemReselectedListener)
        updateBottomBarContentDescriptions(itemId)
        drawerLayout.hideKeyboard()
    }

    private fun updateBottomBarContentDescriptions(itemId: Int = -1) {
        /* Manually apply content description on each MenuItem since BottomNavigationView won't
        automatically set it from either the title or content description specified in the menu xml */
        loop@ binding.bottomBar.menu.items.forEach {
            val title = if (it.itemId == itemId) getString(R.string.selected) + " " + it.title else it.title
            // skip inbox, we set it with the unread count even if there are no new messages
            if(it.itemId != R.id.bottomNavigationInbox) {
                MenuItemCompat.setContentDescription(it, title)
            }
        }
    }

    /**
     * Determines which tab is highlighted in the bottom navigation bar.
     */
    private fun setBottomBarItemSelected(fragment: Fragment) {
        when(fragment) {
            //Calendar
            is CalendarFragment -> setBottomBarItemSelected(R.id.bottomNavigationCalendar)
            is EventFragment -> setBottomBarItemSelected(R.id.bottomNavigationCalendar)
            //To-do
            is ToDoListFragment -> setBottomBarItemSelected(R.id.bottomNavigationToDo)
            //Notifications
            is NotificationListFragment-> {
                setBottomBarItemSelected(if(fragment.isCourseOrGroup()) R.id.bottomNavigationHome
                else R.id.bottomNavigationNotifications)
            }
            //Inbox
            is InboxFragment,
            is InboxDetailsFragment,
            is InboxComposeFragment -> setBottomBarItemSelected(R.id.bottomNavigationInbox)
            //courses
            else -> setBottomBarItemSelected(R.id.bottomNavigationHome)
        }
    }

    //endregion

    //region Actionbar

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle?.onOptionsItemSelected(item) == true) return true

        if (item.itemId == R.id.bookmark) {
            if (!APIHelper.hasNetworkConnection()) {
                Toast.makeText(context, context.getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
                return true
            }
            addBookmark()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region Adding/Removing Fragments

    override fun popCurrentFragment() {
        try {
            supportFragmentManager.popBackStack()
        } catch (e: Exception) {
            Logger.e("Unable to pop current fragment." + e)
        }
    }

    override fun handleRoute(route: Route) {
        if (routeJob?.isActive == true) return

        routeJob = tryWeave {
            if(route.routeContext == RouteContext.EXTERNAL) showLoadingIndicator()

            // When statements not being used, for some reason they are breaking with the Route enum types.
            if(route.canvasContext == null) {
                val contextId = Route.extractCourseId(route)
                if (contextId != 0L) {
                    when {
                        RouteContext.FILE == route.routeContext && route.secondaryClass != CourseModuleProgressionFragment::class.java -> {
                            if (route.queryParamsHash.containsKey(RouterParams.VERIFIER) && route.queryParamsHash.containsKey(RouterParams.DOWNLOAD_FRD)) {
                                if(route.uri != null) openMedia(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, contextId, ""), route.uri.toString(), null)
                            }
                            route.paramsHash[RouterParams.FILE_ID]?.let { handleSpecificFile(contextId, it) }

                            if(route.canvasContext != null) addFragment(RouteResolver.getFragment(route), route)
                        }
                        RouteContext.LTI == route.routeContext -> {
                            val contextType = route.getContextType()
                            when (contextType) {
                                CanvasContext.Type.COURSE -> {
                                    route.canvasContext = repository.getCourse(contextId, false)
                                    if(route.canvasContext == null) showMessage(getString(R.string.could_not_route_course))
                                }
                                CanvasContext.Type.GROUP -> {
                                    route.canvasContext = awaitApi<Group> { GroupManager.getDetailedGroup(contextId, it, false) }
                                    if(route.canvasContext == null) showMessage(getString(R.string.could_not_route_group))
                                }
                                CanvasContext.Type.USER -> route.canvasContext = ApiPrefs.user
                                else -> showMessage(getString(R.string.could_not_route_unknown))
                            }

                            route.canvasContext?.let {
                                val ltiRoute = LtiLaunchFragment.makeRoute(it, route.uri.toString())
                                RouteMatcher.route(this@NavigationActivity, ltiRoute)
                            }
                        }
                        RouteContext.NOTIFICATION_PREFERENCES == route.routeContext -> {
                            Analytics.trackAppFlow(this@NavigationActivity, PushNotificationPreferencesFragment::class.java)
                            RouteMatcher.route(this@NavigationActivity, Route(PushNotificationPreferencesFragment::class.java, null))
                        }
                        else -> {
                            //fetch the CanvasContext
                            val contextType = route.getContextType()
                            when (contextType) {
                                CanvasContext.Type.COURSE -> {
                                    route.canvasContext = repository.getCourse(contextId, false)
                                    if(route.canvasContext == null) showMessage(getString(R.string.could_not_route_course))
                                }
                                CanvasContext.Type.GROUP -> {
                                    route.canvasContext = awaitApi<Group> { GroupManager.getDetailedGroup(contextId, it, false) }
                                    if(route.canvasContext == null) showMessage(getString(R.string.could_not_route_group))
                                }
                                CanvasContext.Type.USER -> route.canvasContext = ApiPrefs.user
                                else -> showMessage(getString(R.string.could_not_route_unknown))
                            }

                            if(route.canvasContext != null) addFragment(RouteResolver.getFragment(route), route)
                        }
                    }
                } else {
                    // Some routes have no CanvasContext
                    addFragment(RouteResolver.getFragment(route), route)
                }
            } else {
                addFragment(RouteResolver.getFragment(route), route)
            }

            hideLoadingIndicator()
        } catch {
            hideLoadingIndicator()
            Logger.e("Could not route: ${it.message}")
        }
    }

    private fun addFragment(fragment: Fragment?, route: Route) {
        if (fragment != null && fragment::class.java.name in getBottomNavFragmentNames() && isBottomNavFragment(currentFragment)) {
            selectBottomNavFragment(fragment::class.java)
        } else {
            addFullScreenFragment(fragment, route.removePreviousScreen)
        }
    }

    private fun selectBottomNavFragment(fragmentClass: Class<out Fragment>) {
        val selectedFragment = supportFragmentManager.findFragmentByTag(fragmentClass.name)

        (topFragment as? DashboardFragment)?.cancelCardDrag()

        if (selectedFragment == null) {
            val fragment = createBottomNavFragment(fragmentClass.name)
            val newArguments = if (fragment?.arguments != null) fragment.requireArguments() else Bundle()
            newArguments.putBoolean(BOTTOM_NAV_SCREEN, true)
            fragment?.arguments = newArguments
            addFullScreenFragment(fragment)
        } else {
            showHiddenFragment(selectedFragment)
        }

        bottomNavScreensStack.remove(fragmentClass.name)
        bottomNavScreensStack.push(fragmentClass.name)
    }

    private fun addFullScreenFragment(fragment: Fragment?, removePreviousFragment: Boolean = false) {
        if (fragment == null) {
            Logger.e("NavigationActivity:addFullScreenFragment() - Could not route null Fragment.")
            return
        }

        val ft = supportFragmentManager.beginTransaction()
        if (removePreviousFragment) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            ft.setCustomAnimations(R.anim.fade_in_quick, R.anim.fade_out_quick)
        }

        currentFragment?.let { ft.hide(it) }
        ft.add(R.id.fullscreen, fragment, fragment::class.java.name)
        ft.addToBackStack(fragment::class.java.name)
        ft.commitAllowingStateLoss()
    }

    private fun showHiddenFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.fade_in_quick, R.anim.fade_out_quick)
        val bottomBarFragments = getBottomBarFragments(fragment::class.java.name)
        bottomBarFragments.forEach {
            ft.hide(it)
        }
        ft.show(fragment)
        ft.commitAllowingStateLoss()

        if (fragment is CalendarFragment) {
            fragment.calendarTabSelected()
        }
    }

    private fun getBottomBarFragments(selectedFragmentName: String): List<Fragment> {
        return getBottomNavFragmentNames()
            .filter { it != selectedFragmentName }
            .mapNotNull { supportFragmentManager.findFragmentByTag(it) }
    }
    //endregion

    //region Back Stack

    override fun onBackPressed() {
        if (isDrawerOpen) {
            closeNavigationDrawer()
            return
        }

        if (supportFragmentManager.backStackEntryCount == 1) {
            // Exits if we only have one fragment
            finish()
            return
        }

        val topFragment = topFragment
        if (topFragment is NavigationCallbacks) {
            if (!topFragment.onHandleBackPressed()) {
                if (isBottomNavFragment(topFragment)) {
                    handleBottomNavBackStack()
                } else {
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun handleBottomNavBackStack() {
        if (bottomNavScreensStack.size == 0) {
            finish()
        } else if (bottomNavScreensStack.size == 1) {
            bottomNavScreensStack.pop()
            val previousFragment = supportFragmentManager.findFragmentByTag(navigationBehavior.homeFragmentClass.name)
            if (previousFragment != null) {
                showHiddenFragment(previousFragment)
                applyCurrentFragmentTheme()
            }
        } else {
            bottomNavScreensStack.pop()
            val previousFragmentName = bottomNavScreensStack.peek()
            val previousFragment = supportFragmentManager.findFragmentByTag(previousFragmentName)
            if (previousFragment != null) {
                showHiddenFragment(previousFragment)
                applyCurrentFragmentTheme()
            }
        }
    }

    override val topFragment: Fragment?
        get() {
            val stackSize = supportFragmentManager.backStackEntryCount
            if (stackSize > 0) {
                val backStackEntryName = supportFragmentManager.getBackStackEntryAt(stackSize - 1).name
                return if (backStackEntryName in getBottomNavFragmentNames()) {
                    currentFragment
                } else {
                    supportFragmentManager.findFragmentByTag(backStackEntryName)
                }
            }
            return null
        }

    override val peekingFragment: Fragment?
        get() {
            val stackSize = supportFragmentManager.backStackEntryCount
            if (stackSize > 1) {
                val fragmentTag = supportFragmentManager.getBackStackEntryAt(stackSize - 2).name
                return supportFragmentManager.findFragmentByTag(fragmentTag)
            }
            return null
        }

    override val currentFragment: Fragment?
        get() {
            val fragment = supportFragmentManager.findFragmentById(R.id.fullscreen)
            return if (fragment != null && isBottomNavFragment(fragment)) {
                val currentFragmentName = bottomNavScreensStack.peek() ?: navigationBehavior.homeFragmentClass.name
                supportFragmentManager.findFragmentByTag(currentFragmentName)
            } else {
                fragment
            }
        }

    private fun isBottomNavFragment(fragment: Fragment?) = fragment?.arguments?.getBoolean(BOTTOM_NAV_SCREEN) == true

    private fun getBottomNavFragmentNames() = navigationBehavior.bottomNavBarFragments.map { it.name }

    private fun clearBackStack(cls: Class<*>?) {
        val fragment = topFragment
        if (fragment != null && cls != null && fragment::class.java.isAssignableFrom(cls)) {
            return
        }
        try {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (e: Exception) {
            Logger.e("NavigationActivity: clearBackStack() - Unable to clear backstack. $e")
        }
    }

    //endregion

    //region Routing

    override fun existingFragmentCount(): Int = supportFragmentManager.backStackEntryCount

    override fun showLoadingIndicator() {
        canvasLoadingBinding.loadingRoute.setVisible()
    }

    override fun hideLoadingIndicator() {
        canvasLoadingBinding.loadingRoute.setGone()
    }

    //endregion

    //region Push Notifications

    private fun handlePushNotification(hasUnreadNotifications: Boolean) {
        val intent = intent
        if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                if (hasUnreadNotifications) {
                    setPushNotificationAsRead()
                }

                val htmlUrl = extras.getString(PushNotification.HTML_URL, "")

                if (!RouteMatcher.canRouteInternally(this, htmlUrl, ApiPrefs.domain, true) && ApiPrefs.user != null) {
                    RouteMatcher.route(this, NotificationListFragment.makeRoute(ApiPrefs.user!!))
                }
            }
        }
    }

    private fun hasUnreadPushNotification(extras: Bundle?): Boolean {
        return (extras != null && extras.containsKey(PushExternalReceiver.NEW_PUSH_NOTIFICATION)
                && extras.getBoolean(PushExternalReceiver.NEW_PUSH_NOTIFICATION, false))
    }

    private fun setPushNotificationAsRead() {
        intent.putExtra(PushExternalReceiver.NEW_PUSH_NOTIFICATION, false)
        PushNotification.remove(intent)
    }

    private fun hasLocalNotificationLink(extras: Bundle?): Boolean {
        val flag = extras != null && extras.containsKey(Const.LOCAL_NOTIFICATION)
            && extras.getBoolean(Const.LOCAL_NOTIFICATION, false)
        if (flag) {
            // Clear the flag if we are handling this, so subsequent app opens don't deep link again
            extras!!.putBoolean(Const.LOCAL_NOTIFICATION,false)
        }
        return flag
    }

    private fun hasPendingLanguageIntent(extras: Bundle?): Boolean {
        return extras != null && extras.containsKey(LocaleUtils.LANGUAGES_PENDING_INTENT_KEY)
            && extras.getInt(LocaleUtils.LANGUAGES_PENDING_INTENT_KEY, 0) != LocaleUtils.LANGUAGES_PENDING_INTENT_ID
    }

    //endregion

    override fun gotLaunchDefinitions(launchDefinitions: List<LaunchDefinition>?) {
        val studioLaunchDefinition = launchDefinitions?.firstOrNull { it.domain == LaunchDefinition.STUDIO_DOMAIN }
        val gaugeLaunchDefinition = launchDefinitions?.firstOrNull { it.domain == LaunchDefinition.GAUGE_DOMAIN }
        val masteryLaunchDefinition = launchDefinitions?.firstOrNull { it.domain == LaunchDefinition.MASTERY_DOMAIN }

        val studio = findViewById<View>(R.id.navigationDrawerItem_studio)
        studio.visibility = if (studioLaunchDefinition != null) View.VISIBLE else View.GONE
        studio.tag = studioLaunchDefinition

        val gauge = findViewById<View>(R.id.navigationDrawerItem_gauge)
        gauge.visibility = if (gaugeLaunchDefinition != null) View.VISIBLE else View.GONE
        gauge.tag = gaugeLaunchDefinition

        val mastery = findViewById<View>(R.id.navigationDrawerItem_mastery)
        mastery.visibility = if (masteryLaunchDefinition != null) View.VISIBLE else View.GONE
        mastery.tag = masteryLaunchDefinition
    }

    override fun addBookmark() {
        val dialog = BookmarkCreationDialog.newInstance(this, topFragment, peekingFragment)
        dialog?.show(supportFragmentManager, BookmarkCreationDialog::class.java.simpleName)
    }

    override fun canBookmark(): Boolean = navigationBehavior.visibleNavigationMenuItems.contains(NavigationMenuItem.BOOKMARKS)

    override fun updateUnreadCount(unreadCount: Int) {
        updateBottomBarBadge(R.id.bottomNavigationInbox, unreadCount, R.plurals.a11y_inboxUnreadCount)
    }

    override fun increaseUnreadCount(increaseBy: Int) {
        updateUnreadCount(binding.bottomBar.getOrCreateBadge(R.id.bottomNavigationInbox).number + increaseBy)
    }

    override fun updateNotificationCount(notificationCount: Int) {
        updateBottomBarBadge(R.id.bottomNavigationNotifications, notificationCount, R.plurals.a11y_notificationsUnreadCount)
    }

    private fun updateBottomBarBadge(@IdRes menuItemId: Int, count: Int, @PluralsRes quantityContentDescription: Int? = null) = with(binding) {
        if (count > 0) {
            bottomBar.getOrCreateBadge(menuItemId).number = count
            bottomBar.getOrCreateBadge(menuItemId).backgroundColor = getColor(R.color.backgroundInfo)
            bottomBar.getOrCreateBadge(menuItemId).badgeTextColor = getColor(R.color.textLightest)
            if (quantityContentDescription != null) {
                bottomBar.getOrCreateBadge(menuItemId).setContentDescriptionQuantityStringsResource(quantityContentDescription)
            }
        } else {
            // Don't set the badge or display it, remove any badge
            bottomBar.removeBadge(menuItemId)
        }
    }

    /** Handles showing confetti on a successful assignment submission */
    @Subscribe
    fun showConfetti(event: ShowConfettiEvent) {
        runOnUiThread {
            val root = window.decorView.rootView as ViewGroup
            val animation = LottieAnimationView(this).apply {
                setAnimation("confetti.json")
                scaleType = ImageView.ScaleType.CENTER_CROP;
            }
            animation.addAnimatorUpdateListener {
                if (it.animatedFraction >= 1.0) root.removeView(animation)
            }
            root.addView(animation, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            animation.playAnimation()
        }
    }

    private fun createBottomNavFragment(name: String?): Fragment? {
        return when (name) {
            navigationBehavior.homeFragmentClass.name -> {
                val route = navigationBehavior.createHomeFragmentRoute(ApiPrefs.user)
                navigationBehavior.createHomeFragment(route)
            }
            CalendarFragment::class.java.name -> {
                val route = CalendarFragment.makeRoute()
                CalendarFragment.newInstance(route)
            }
            ToDoListFragment::class.java.name -> {
                val route = ToDoListFragment.makeRoute(ApiPrefs.user!!)
                ToDoListFragment.newInstance(route)
            }
            NotificationListFragment::class.java.name -> {
                val route = NotificationListFragment.makeRoute(ApiPrefs.user!!)
                NotificationListFragment.newInstance(route)
            }
            InboxFragment::class.java.name -> {
                val route = InboxFragment.makeRoute()
                InboxFragment.newInstance(route)
            }
            NothingToSeeHereFragment::class.java.name -> NothingToSeeHereFragment.newInstance()
            else -> null
        }
    }

    private fun scheduleAlarms() {
        lifecycleScope.launch {
            alarmScheduler.scheduleAllAlarmsForCurrentUser()
        }
    }

    private fun setCloseDrawerVisibility() {
        navigationDrawerBinding.navigationDrawerItemCloseDrawer.setVisible(isAccessibilityEnabled(this))
    }

    companion object {
        fun createIntent(context: Context, route: Route): Intent {
            return Intent(context, NavigationActivity::class.java).apply { putExtra(Route.ROUTE, route) }
        }

        fun createIntent(context: Context, masqueradingUserId: Long): Intent {
            return Intent(context, NavigationActivity::class.java).apply {
                putExtra(Const.QR_CODE_MASQUERADE_ID, masqueradingUserId)
            }
        }

        val startActivityClass: Class<out Activity>
            get() = NavigationActivity::class.java
    }
}
