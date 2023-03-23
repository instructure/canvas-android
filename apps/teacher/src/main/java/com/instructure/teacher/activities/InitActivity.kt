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
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.PluralsRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.instructure.canvasapi2.managers.CourseNicknameManager
import com.instructure.canvasapi2.managers.ThemeManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.canvasapi2.utils.pageview.PandataManager
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.Identity
import com.instructure.interactions.InitActivityInteractions
import com.instructure.interactions.router.Route
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.loginapi.login.dialog.MasqueradingDialog
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.activities.BasePresenterActivity
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.help.HelpDialogFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.pandautils.features.inbox.list.OnUnreadCountInvalidated
import com.instructure.pandautils.features.themeselector.ThemeSelectorBottomSheet
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.pandautils.update.UpdateManager
import com.instructure.pandautils.utils.*
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ActivityInitBinding
import com.instructure.teacher.databinding.NavigationDrawerBinding
import com.instructure.teacher.dialog.ColorPickerDialog
import com.instructure.teacher.dialog.EditCourseNicknameDialog
import com.instructure.teacher.events.CourseUpdatedEvent
import com.instructure.teacher.events.ToDoListUpdatedEvent
import com.instructure.teacher.factory.InitActivityPresenterFactory
import com.instructure.teacher.fragments.*
import com.instructure.teacher.presenters.InitActivityPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.router.RouteResolver
import com.instructure.teacher.services.TeacherPageViewService
import com.instructure.teacher.tasks.TeacherLogoutTask
import com.instructure.teacher.utils.LoggingUtility
import com.instructure.teacher.utils.TeacherPrefs
import com.instructure.teacher.utils.isTablet
import com.instructure.teacher.viewinterface.InitActivityView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class InitActivity : BasePresenterActivity<InitActivityPresenter, InitActivityView>(),
    InitActivityView, DashboardFragment.CourseBrowserCallback, InitActivityInteractions,
    MasqueradingDialog.OnMasqueradingSet, ErrorReportDialog.ErrorReportDialogResultListener, OnUnreadCountInvalidated {

    private val binding by viewBinding(ActivityInitBinding::inflate)
    private lateinit var navigationDrawerBinding: NavigationDrawerBinding

    @Inject
    lateinit var updateManager: UpdateManager

    @Inject
    lateinit var typefaceBehaviour: TypefaceBehavior

    private var selectedTab = 0
    private var drawerItemSelectedJob: Job? = null

    private val mTabSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        selectedTab = when (item.itemId) {
            R.id.tab_courses -> {
                addCoursesFragment()
                COURSES_TAB
            }
            R.id.tab_inbox -> {
                addInboxFragment()
                INBOX_TAB
            }
            R.id.tab_todo -> {
                addToDoFragment()
                TODO_TAB
            }
            else -> return@OnNavigationItemSelectedListener false
        }

        updateBottomBarContentDescriptions(item.itemId)
        true
    }

    private lateinit var checkListener: CompoundButton.OnCheckedChangeListener

    private val isDrawerOpen: Boolean
        get() = binding.drawerLayout.isDrawerOpen(GravityCompat.START)

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        LoggingUtility.log(this.javaClass.simpleName + " --> On Stop")
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nightModeFlags: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        ColorKeeper.darkTheme = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        if (!ThemePrefs.isThemeApplied) {
            // This will be only called when we change dark/light mode, because the Theme is already applied before in the SplashActivity.
            updateTheme()
        }

        typefaceBehaviour.overrideFont(FontFamily.REGULAR.fontPath)
        LoggingUtility.log(this.javaClass.simpleName + " --> On Create")

        val masqueradingUserId: Long = intent.getLongExtra(Const.QR_CODE_MASQUERADE_ID, 0L)
        if (masqueradingUserId != 0L) {
            MasqueradeHelper.startMasquerading(masqueradingUserId, ApiPrefs.domain, InitActivity::class.java)
            finish()
        }

        navigationDrawerBinding = NavigationDrawerBinding.bind(binding.root)
        setContentView(binding.root)

        selectedTab = savedInstanceState?.getInt(SELECTED_TAB) ?: 0

        if (savedInstanceState == null) {
            if (hasUnreadPushNotification(intent.extras)) {
                handlePushNotification(hasUnreadPushNotification(intent.extras))
            }
        }

        RatingDialog.showRatingDialog(this, AppType.TEACHER)

        updateManager.checkForInAppUpdate(this)

        if (!ThemePrefs.themeSelectionShown) {
            val themeSelector = ThemeSelectorBottomSheet()
            themeSelector.show(supportFragmentManager, ThemeSelectorBottomSheet::javaClass.name)
            ThemePrefs.themeSelectionShown = true
        }

        lifecycleScope.launch {
            if (ApiPrefs.pandataInfo?.isValid != true) {
                try {
                    ApiPrefs.pandataInfo = awaitApi<PandataInfo> {
                        PandataManager.getToken(TeacherPageViewService.pandataTokenKey, it)
                    }
                } catch (ignore: Throwable) {
                    Logger.w("Unable to refresh pandata info")
                }
            }
        }
    }

    private fun updateTheme() {
        lifecycleScope.launch {
            val theme = awaitApi<CanvasTheme> { ThemeManager.getTheme(it, false) }
            ThemePrefs.applyCanvasTheme(theme, this@InitActivity)
            binding.bottomBar.applyTheme(ThemePrefs.brandColor, getColor(R.color.textDarkest))
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Switching languages will also trigger this method; check for our Pending intent id
        intent?.let {
            if (it.hasExtra(LocaleUtils.LANGUAGES_PENDING_INTENT_KEY) && it.getIntExtra(LocaleUtils.LANGUAGES_PENDING_INTENT_KEY, 0) != LocaleUtils.LANGUAGES_PENDING_INTENT_ID) {
                handlePushNotification(hasUnreadPushNotification(it.extras))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        drawerItemSelectedJob?.cancel()
    }

    override fun onReadySetGo(presenter: InitActivityPresenter) = with(binding) {
        bottomBar.applyTheme(ThemePrefs.brandColor, getColor(R.color.textDarkest))
        bottomBar.setOnNavigationItemSelectedListener(mTabSelectedListener)
        fakeToolbar.setBackgroundColor(ThemePrefs.primaryColor)
        when (selectedTab) {
            0 -> addCoursesFragment()
            1 -> addToDoFragment()
            2 -> addInboxFragment()
        }

        // Set initially selected item
        updateBottomBarContentDescriptions(bottomBar.menu.getItem(0).itemId)

        presenter.loadData(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_TAB, selectedTab)
    }

    override fun getPresenterFactory() = InitActivityPresenterFactory()

    override fun onPresenterPrepared(presenter: InitActivityPresenter) = Unit

    override fun unBundle(extras: Bundle) = Unit

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ToDoListUpdatedEvent) {
        event.once(javaClass.simpleName) { todoCount ->
            updateTodoCount(todoCount)
        }
    }

    private fun closeNavigationDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun openNavigationDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private val navDrawerOnClick = View.OnClickListener { v ->
        drawerItemSelectedJob = weave {
            closeNavigationDrawer()
            delay(250)
            when (v.id) {
                R.id.navigationDrawerItem_files -> {
                    RouteMatcher.route(this@InitActivity, Route(FileListFragment::class.java, ApiPrefs.user))
                }
                R.id.navigationDrawerItem_gauge, R.id.navigationDrawerItem_arc -> {
                    val launchDefinition = v.tag as? LaunchDefinition ?: return@weave
                    val user = ApiPrefs.user ?: return@weave
                    val canvasContext = CanvasContext.currentUserContext(user)
                    val title = getString(if (launchDefinition.isGauge) R.string.gauge else R.string.studio)
                    val route = LtiLaunchFragment.makeBundle(
                        canvasContext = canvasContext,
                        url = launchDefinition.placements.globalNavigation.url,
                        title = title,
                        sessionLessLaunch = true
                    )
                    RouteMatcher.route(this@InitActivity, Route(LtiLaunchFragment::class.java, canvasContext, route))
                }
                R.id.navigationDrawerItem_help -> HelpDialogFragment.show(this@InitActivity)
                R.id.navigationDrawerItem_changeUser -> TeacherLogoutTask(LogoutTask.Type.SWITCH_USERS).execute()
                R.id.navigationDrawerItem_logout -> {
                    AlertDialog.Builder(this@InitActivity)
                        .setTitle(R.string.logout_warning)
                        .setPositiveButton(android.R.string.yes) { _, _ -> TeacherLogoutTask(LogoutTask.Type.LOGOUT).execute() }
                        .setNegativeButton(android.R.string.no, null)
                        .create()
                        .show()
                }
                R.id.navigationDrawerItem_stopMasquerading -> onStopMasquerading()
                R.id.navigationDrawerItem_startMasquerading -> {
                    MasqueradingDialog.show(supportFragmentManager, ApiPrefs.domain, null, !isTablet)
                }
                R.id.navigationDrawerSettings -> {
                    RouteMatcher.route(this@InitActivity, Route(SettingsFragment::class.java, ApiPrefs.user))
                }
            }
        }
    }

    fun attachNavigationDrawer(toolbar: Toolbar) = with(navigationDrawerBinding) {
        // Navigation items
        navigationDrawerItemFiles.setOnClickListener(navDrawerOnClick)
        navigationDrawerItemGauge.setOnClickListener(navDrawerOnClick)
        navigationDrawerItemArc.setOnClickListener(navDrawerOnClick)
        navigationDrawerItemChangeUser.setOnClickListener(navDrawerOnClick)
        navigationDrawerItemLogout.setOnClickListener(navDrawerOnClick)
        navigationDrawerSettings.setOnClickListener(navDrawerOnClick)
        navigationDrawerItemHelp.setOnClickListener(navDrawerOnClick)
        navigationDrawerItemStopMasquerading.setOnClickListener(navDrawerOnClick)
        navigationDrawerItemStartMasquerading.setOnClickListener(navDrawerOnClick)

        // Set up Color Overlay setting
        setUpColorOverlaySwitch()

        // App version
        navigationDrawerVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        toolbar.setNavigationIcon(R.drawable.ic_hamburger)
        toolbar.navigationContentDescription = getString(R.string.navigation_drawer_open)
        toolbar.setNavigationOnClickListener { openNavigationDrawer() }

        setupUserDetails(ApiPrefs.user)

        navigationDrawerItemStartMasquerading.setVisible(!ApiPrefs.isMasquerading && ApiPrefs.canBecomeUser == true)
        navigationDrawerItemStopMasquerading.setVisible(ApiPrefs.isMasquerading)
    }

    private fun setUpColorOverlaySwitch() = with(navigationDrawerBinding) {
        navigationDrawerColorOverlaySwitch.isChecked = !TeacherPrefs.hideCourseColorOverlay
        checkListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            navigationDrawerColorOverlaySwitch.isEnabled = false
            presenter?.setHideColorOverlay(!isChecked)
        }
        navigationDrawerColorOverlaySwitch.setOnCheckedChangeListener(checkListener)
        ViewStyler.themeSwitch(this@InitActivity, navigationDrawerColorOverlaySwitch, ThemePrefs.brandColor)
    }

    override fun updateColorOverlaySwitch(isChecked: Boolean, isFailed: Boolean) = with(navigationDrawerBinding) {
        if (isFailed) toast(R.string.errorOccurred)
        navigationDrawerColorOverlaySwitch.setOnCheckedChangeListener(null)
        navigationDrawerColorOverlaySwitch.isChecked = isChecked
        navigationDrawerColorOverlaySwitch.setOnCheckedChangeListener(checkListener)
        navigationDrawerColorOverlaySwitch.isEnabled = true
    }

    override fun gotLaunchDefinitions(launchDefinitions: List<LaunchDefinition>?) = with(navigationDrawerBinding) {
        val arcLaunchDefinition = launchDefinitions?.firstOrNull { it.domain == LaunchDefinition._STUDIO_DOMAIN }
        val gaugeLaunchDefinition = launchDefinitions?.firstOrNull { it.domain == LaunchDefinition._GAUGE_DOMAIN }

        navigationDrawerItemArc.setVisible(arcLaunchDefinition != null)
        navigationDrawerItemArc.tag = arcLaunchDefinition

        navigationDrawerItemGauge.setVisible(gaugeLaunchDefinition != null)
        navigationDrawerItemGauge.tag = gaugeLaunchDefinition
    }

    override fun onStartMasquerading(domain: String, userId: Long) {
        MasqueradeHelper.startMasquerading(userId, domain, LoginActivity::class.java)
    }

    override fun onStopMasquerading() {
        MasqueradeHelper.stopMasquerading(LoginActivity::class.java)
    }

    private fun setupUserDetails(user: User?) = with(navigationDrawerBinding) {
        if (user != null) {
            navigationDrawerUserName.text = Pronouns.span(user.shortName, user.pronouns)
            navigationDrawerUserEmail.text = user.primaryEmail
            ProfileUtils.loadAvatarForUser(navigationDrawerProfileImage, user.shortName, user.avatarUrl)
        }
    }

    private fun updateBottomBarContentDescriptions(selectedItemId: Int = -1) {
        // Manually apply content description on each MenuItem since BottomNavigationView won't
        // automatically set it from either the title or content description specified in the menu xml
        binding.bottomBar.menu.items.forEach {
            val title = if (it.itemId == selectedItemId) getString(R.string.selected) + " " + it.title else it.title
            MenuItemCompat.setContentDescription(it, title)
        }
    }


    override fun updateTodoCount(todoCount: Int) {
        updateBottomBarBadge(R.id.tab_todo, todoCount)
    }

    override fun updateInboxUnreadCount(unreadCount: Int) {
        updateBottomBarBadge(R.id.tab_inbox, unreadCount, R.plurals.a11y_inboxUnreadCount)
    }

    override fun invalidateUnreadCount() {
        presenter?.updateUnreadCount()
    }

    private fun updateBottomBarBadge(@IdRes menuItemId: Int, count: Int, @PluralsRes quantityContentDescription: Int? = null) = with(binding) {
        if (count > 0) {
            bottomBar.getOrCreateBadge(menuItemId).number = count
            bottomBar.getOrCreateBadge(menuItemId).backgroundColor = getColor(R.color.backgroundInfo)
            bottomBar.getOrCreateBadge(menuItemId).badgeTextColor = getColor(R.color.white)
            if (quantityContentDescription != null) {
                bottomBar.getOrCreateBadge(menuItemId).setContentDescriptionQuantityStringsResource(quantityContentDescription)
            }
        } else {
            // Don't set the badge or display it, remove any badge
            bottomBar.removeBadge(menuItemId)
        }
    }

    private fun addCoursesFragment() {
        if (supportFragmentManager.findFragmentByTag(DashboardFragment::class.java.simpleName) == null) {
            setBaseFragment(DashboardFragment.getInstance())
        } else if (resources.getBoolean(R.bool.isDeviceTablet)) {
            binding.container.visibility = View.VISIBLE
            binding.masterDetailContainer.visibility = View.GONE
        }
    }

    private fun addInboxFragment() = with(binding) {
        if (supportFragmentManager.findFragmentByTag(InboxFragment::class.java.simpleName) == null) {
            // if we're a tablet we want the master detail view
            if (resources.getBoolean(R.bool.isDeviceTablet)) {
                val route = InboxFragment.makeRoute()
                val masterFragment = RouteResolver.getMasterFragment(null, route)
                val detailFragment =
                    EmptyFragment.newInstance(RouteMatcher.getClassDisplayName(this@InitActivity, route.primaryClass))
                putFragments(masterFragment, detailFragment, true)
                middleTopDivider.setBackgroundColor(ThemePrefs.primaryColor)

            } else {
                setBaseFragment(InboxFragment())
            }
        } else if (resources.getBoolean(R.bool.isDeviceTablet)) {
            masterDetailContainer.visibility = View.VISIBLE
            container.setGone()
            middleTopDivider.setBackgroundColor(ThemePrefs.primaryColor)
        }

    }

    override fun addFragment(route: Route) {
        addDetailFragment(RouteResolver.getDetailFragment(route.canvasContext, route))
    }

    private fun addDetailFragment(fragment: Fragment?) {
        if (fragment == null) throw IllegalStateException("InitActivity.class addDetailFragment was null")

        if (isDrawerOpen) closeNavigationDrawer()

        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val currentFragment = fm.findFragmentById(R.id.detail)

        if (identityMatch(currentFragment, fragment)) return

        ft.replace(R.id.detail, fragment, fragment.javaClass.simpleName)
        if (currentFragment != null && currentFragment !is EmptyFragment) {
            //Add to back stack if not empty fragment and a fragment exists
            ft.addToBackStack(fragment.javaClass.simpleName)
        }
        ft.commit()
    }

    private fun identityMatch(fragment1: Fragment?, fragment2: Fragment): Boolean {
        return (fragment1 as? Identity)?.identity ?: -1 == (fragment2 as? Identity)?.identity ?: -2
    }

    private fun addToDoFragment() {
        if (supportFragmentManager.findFragmentByTag(ToDoFragment::class.java.simpleName) == null) {
            setBaseFragment(ToDoFragment())
        } else if (resources.getBoolean(R.bool.isDeviceTablet)) {
            binding.container.visibility = View.VISIBLE
            binding.masterDetailContainer.visibility = View.GONE
        }
    }

    private fun setBaseFragment(fragment: Fragment) {
        putFragment(fragment, true)
    }

    private fun addFragment(fragment: Fragment) {
        putFragment(fragment, false)
    }

    private fun putFragment(fragment: Fragment, clearBackStack: Boolean) {
        if (isDrawerOpen) closeNavigationDrawer()

        binding.masterDetailContainer.setGone()
        binding.container.visibility = View.VISIBLE

        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        if (clearBackStack) {
            if (fm.backStackEntryCount > 0) {
                fm.popBackStackImmediate(fm.getBackStackEntryAt(0).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        } else {
            ft.addToBackStack(null)
        }
        ft.replace(R.id.container, fragment, fragment.javaClass.simpleName)
        ft.commit()
    }

    private fun putFragments(fragment: Fragment?, detailFragment: Fragment, clearBackStack: Boolean) {
        if (fragment == null) return

        if (isDrawerOpen) closeNavigationDrawer()

        binding.masterDetailContainer.visibility = View.VISIBLE
        binding.container.setGone()
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        if (clearBackStack && fm.backStackEntryCount > 0) {
            fm.popBackStackImmediate(fm.getBackStackEntryAt(0).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            ft.addToBackStack(null)
        }
        ft.replace(R.id.master, fragment, fragment::class.java.simpleName)
        ft.replace(R.id.detail, detailFragment, detailFragment.javaClass.simpleName)
        ft.commit()
    }

    override fun onEditCourseNickname(course: Course) {
        EditCourseNicknameDialog.getInstance(this.supportFragmentManager, course) { s ->
            tryWeave {
                val (name, nickname) = awaitApi<CourseNickname> {
                    CourseNicknameManager.setCourseNickname(course.id, s, it)
                }
                if (nickname == null) {
                    course.name = name ?: ""
                    course.originalName = null
                } else {
                    course.name = nickname
                    course.originalName = name
                }
                val event = CourseUpdatedEvent(course, null)
                // Remove any events just in case they want to change the name more than once
                EventBus.getDefault().removeStickyEvent(event)
                EventBus.getDefault().postSticky(event)
            } catch {
                toast(R.string.errorOccurred)
            }
        }.show(this.supportFragmentManager, EditCourseNicknameDialog::class.java.name)
    }

    override fun onShowCourseDetails(course: Course) {
        RouteMatcher.route(this, Route(CourseBrowserFragment::class.java, course))
    }

    override fun onPickCourseColor(course: Course) {
        ColorPickerDialog.newInstance(supportFragmentManager, course) { color ->
            tryWeave {
                awaitApi<CanvasColor> { UserManager.setColors(it, course.contextId, color) }
                ColorKeeper.addToCache(course.contextId, color)
                val event = CourseUpdatedEvent(course, null)
                EventBus.getDefault().postSticky(event)
            } catch {
                toast(R.string.colorPickerError)
            }
        }.show(supportFragmentManager, ColorPickerDialog::class.java.simpleName)
    }

    override fun onBackPressed() {
        if (isDrawerOpen) {
            closeNavigationDrawer()
        } else {
            if (binding.masterDetailContainer.isVisible) {
                if ((supportFragmentManager.findFragmentById(R.id.master) as? NavigationCallbacks)?.onHandleBackPressed() == true) return
                super.onBackPressed()
            }
            super.onBackPressed()
        }
    }

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

                if (!RouteMatcher.canRouteInternally(this, htmlUrl, ApiPrefs.domain, true)) {
                    RouteMatcher.routeUrl(this, htmlUrl, ApiPrefs.domain)
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

    //endregion

    override fun onTicketPost() {
        dismissHelpDialog()
        Toast.makeText(applicationContext, R.string.errorReportThankyou, Toast.LENGTH_LONG).show()
    }

    override fun onTicketError() {
        dismissHelpDialog()
        Toast.makeText(applicationContext, R.string.errorOccurred, Toast.LENGTH_LONG).show()
    }

    private fun dismissHelpDialog() {
        val fragment = supportFragmentManager.findFragmentByTag(HelpDialogFragment.TAG)
        if (fragment is HelpDialogFragment) {
            try {
                fragment.dismiss()
            } catch (e: IllegalStateException) {
                Logger.e("Committing a transaction after activities saved state was called: " + e)
            }
        }
    }

    companion object {
        private const val SELECTED_TAB = "selectedTab"

        private const val COURSES_TAB = 0
        private const val TODO_TAB = 1
        private const val INBOX_TAB = 2

        fun createIntent(context: Context, intentExtra: Bundle?): Intent =
            Intent(context, InitActivity::class.java).apply {
                if (intentExtra != null) {
                    // Used for passing up push notification intent
                    this.putExtras(intentExtra)
                }
            }
    }
}
