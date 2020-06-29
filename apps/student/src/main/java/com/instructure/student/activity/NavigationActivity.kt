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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.FullScreenInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouteType
import com.instructure.interactions.router.RouterParams
import com.instructure.loginapi.login.dialog.MasqueradingDialog
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.dialog.BookmarkCreationDialog
import com.instructure.student.events.*
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.fragment.*
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.router.RouteResolver
import com.instructure.student.tasks.StudentLogoutTask
import com.instructure.student.util.Analytics
import com.instructure.student.util.AppShortcutManager
import com.instructure.student.util.StudentPrefs
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.loading_canvas_view.*
import kotlinx.android.synthetic.main.navigation_drawer.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class NavigationActivity : BaseRouterActivity(), Navigation, MasqueradingDialog.OnMasqueradingSet,
    FullScreenInteractions, ActivityCompat.OnRequestPermissionsResultCallback by PermissionReceiver() {

    private var routeJob: WeaveJob? = null
    private var debounceJob: Job? = null
    private var drawerItemSelectedJob: Job? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var colorOverlayJob: Job? = null

    /** 'Root' fragments that should include the bottom nav bar */
    private val bottomNavBarFragments = listOf(
        DashboardFragment::class.java,
        CalendarFragment::class.java,
        ToDoListFragment::class.java,
        NotificationListFragment::class.java,
        InboxFragment::class.java
    )

    override fun contentResId(): Int = R.layout.activity_navigation

    private val isDrawerOpen: Boolean
        get() = !(drawerLayout == null || navigationDrawer == null) && drawerLayout.isDrawerOpen(navigationDrawer)

    private val mNavigationDrawerItemClickListener = View.OnClickListener { v ->
        drawerItemSelectedJob = weave {
            closeNavigationDrawer()
            delay(250)
            when (v.id) {
                R.id.navigationDrawerItem_files -> {
                    ApiPrefs.user?.let { handleRoute(FileListFragment.makeRoute(it)) }
                }
                R.id.navigationDrawerItem_gauge, R.id.navigationDrawerItem_studio -> {
                    val launchDefinition = v.tag as? LaunchDefinition
                    if (launchDefinition != null) startActivity(LTIActivity.createIntent(this@NavigationActivity, launchDefinition))
                }
                R.id.navigationDrawerItem_bookmarks -> {
                    val route = BookmarksFragment.makeRoute(ApiPrefs.user)
                    addFragment(
                            BookmarksFragment.newInstance(route) {
                                RouteMatcher.routeUrl(this@NavigationActivity, it.url!!)
                            }, route)
                }
                R.id.navigationDrawerItem_changeUser -> {
                    StudentLogoutTask(if (ApiPrefs.isStudentView) LogoutTask.Type.LOGOUT else LogoutTask.Type.SWITCH_USERS).execute()
                }
                R.id.navigationDrawerItem_logout -> {
                    AlertDialog.Builder(this@NavigationActivity)
                            .setTitle(R.string.logout_warning)
                            .setPositiveButton(android.R.string.yes) { _, _ ->
                                StudentLogoutTask(LogoutTask.Type.LOGOUT).execute()
                            }
                            .setNegativeButton(android.R.string.no, null)
                            .create()
                            .show()
                }
                R.id.navigationDrawerItem_startMasquerading -> {
                    MasqueradingDialog.show(supportFragmentManager, ApiPrefs.domain, null, !isTablet)
                }
                R.id.navigationDrawerItem_stopMasquerading -> {
                    MasqueradeHelper.stopMasquerading(startActivityClass)
                }
                R.id.navigationDrawerSettings -> startActivity(Intent(applicationContext, SettingsActivity::class.java))
            }
        }
    }

    private val onBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        currentFragment?.let {
            // Sends a broadcast event to notify the backstack has changed and which fragment class is on top.
            OnBackStackChangedEvent(it::class.java).post()
            applyCurrentFragmentTheme()

            /* Update nav bar visibility to show for specific 'root' fragments. Also show the nav bar when there is
             only one fragment on the backstack, which commonly occurs with non-root fragments when routing
             from external sources. */
            val visible = it::class.java in bottomNavBarFragments || supportFragmentManager.backStackEntryCount <= 1
            bottomBar.setVisible(visible)
            bottomBarDivider.setVisible(visible)
        }
    }

    override fun onResume() {
        super.onResume()
        applyCurrentFragmentTheme()
    }

    private fun applyCurrentFragmentTheme() {
        Handler().post {
            (currentFragment as? FragmentInteractions)?.let {
                it.applyTheme()
                setBottomBarItemSelected(it as Fragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val masqueradingUserId: Long = intent.getLongExtra(Const.QR_CODE_MASQUERADE_ID, 0L)
        if (masqueradingUserId != 0L) {
            MasqueradeHelper.startMasquerading(masqueradingUserId, ApiPrefs.domain, NavigationActivity::class.java)
        }

        supportFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)

        if (savedInstanceState == null) {
            if (hasUnreadPushNotification(intent.extras) || hasLocalNotificationLink(intent.extras)) {
                handlePushNotification(hasUnreadPushNotification(intent.extras))
            }
        }

        AppShortcutManager.make(this)
    }

    override fun initialCoreDataLoadingComplete() {
        // Send updated info to Flutter
        FlutterComm.sendUpdatedLogin()
        FlutterComm.sendUpdatedTheme()

        // We are ready to load our UI
        if (currentFragment == null) {
            loadLandingPage(true)
        }

        if (ApiPrefs.user == null ) {
            // Hard case to repro but it's possible for a user to force exit the app before we finish saving the user but they will still launch into the app
            // If that happens, log out
            StudentLogoutTask(LogoutTask.Type.LOGOUT).execute()
        }

        setupBottomNavigation()

        // There is a chance our fragment may attach before we have our core data back.
        EventBus.getDefault().post(CoreDataFinishedLoading)
        applyCurrentFragmentTheme()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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

        if (requestCode == UploadFilesDialog.CAMERA_PIC_REQUEST ||
            requestCode == UploadFilesDialog.PICK_FILE_FROM_DEVICE ||
            requestCode == UploadFilesDialog.PICK_IMAGE_GALLERY ||
            PickerSubmissionUploadEffectHandler.isPickerRequest(requestCode) ||
            AssignmentDetailsFragment.isFileRequest(requestCode) ||
            SubmissionDetailsEmptyContentFragment.isFileRequest(requestCode)
        ) {
            // UploadFilesFragment will not be notified of onActivityResult(), alert manually
            OnActivityResults(ActivityResult(requestCode, resultCode, data), null).postSticky()
        }
    }

    override fun loadLandingPage(clearBackStack: Boolean) {
        if (clearBackStack) clearBackStack(DashboardFragment::class.java)
        val dashboardRoute = DashboardFragment.makeRoute(ApiPrefs.user)
        addFragment(DashboardFragment.newInstance(dashboardRoute), dashboardRoute)

        if (intent.extras?.containsKey(AppShortcutManager.APP_SHORTCUT_PLACEMENT) == true) {
            // Launch to the app shortcut placement
            val placement = intent.extras.getString(AppShortcutManager.APP_SHORTCUT_PLACEMENT)

            // Remove the extra so we don't accidentally launch into the shortcut again.
            intent.extras.remove(AppShortcutManager.APP_SHORTCUT_PLACEMENT)

            when (placement) {
                AppShortcutManager.APP_SHORTCUT_BOOKMARKS -> {
                    val route = BookmarksFragment.makeRoute(ApiPrefs.user)
                    addFragment(BookmarksFragment.newInstance(route) { RouteMatcher.routeUrl(this, it.url!!) }, route)
                }
                AppShortcutManager.APP_SHORTCUT_CALENDAR -> {
                    val route = CalendarFragment.makeRoute()
                    addFragment(CalendarFragment.newInstance(route), route)
                }
                AppShortcutManager.APP_SHORTCUT_TODO -> {
                    val route = ToDoListFragment.makeRoute(ApiPrefs.user!!)
                    addFragment(ToDoListFragment.newInstance(route), route)
                }
                AppShortcutManager.APP_SHORTCUT_NOTIFICATIONS -> {
                    val route = NotificationListFragment.makeRoute(ApiPrefs.user!!)
                    addFragment(NotificationListFragment.newInstance(route), route)
                }
                AppShortcutManager.APP_SHORTCUT_INBOX -> {
                    if (ApiPrefs.isStudentView) {
                        // Inbox not available in Student View
                        val route = NothingToSeeHereFragment.makeRoute()
                        addFragment(NothingToSeeHereFragment.newInstance(), route)
                    } else {
                        val route = InboxFragment.makeRoute()
                        addFragment(InboxFragment.newInstance(route), route)
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
}

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle?.syncState()
    }

    //region Navigation Drawer

    private fun setupUserDetails(user: User?) {
        if (user != null) {
            navigationDrawerUserName.text = Pronouns.span(user.shortName, user.pronouns)
            navigationDrawerUserEmail.text = user.primaryEmail

            if(ProfileUtils.shouldLoadAltAvatarImage(user.avatarUrl)) {
                val initials = ProfileUtils.getUserInitials(user.shortName ?: "")
                val color = ContextCompat.getColor(context, R.color.avatarGray)
                val drawable = TextDrawable.builder()
                        .beginConfig()
                        .height(context.resources.getDimensionPixelSize(R.dimen.profileAvatarSize))
                        .width(context.resources.getDimensionPixelSize(R.dimen.profileAvatarSize))
                        .toUpperCase()
                        .useFont(Typeface.DEFAULT_BOLD)
                        .textColor(color)
                        .endConfig()
                        .buildRound(initials, Color.WHITE)
                navigationDrawerProfileImage.setImageDrawable(drawable)
            } else {
                Glide.with(context).load(user.avatarUrl).into(navigationDrawerProfileImage)
            }
        }
    }

    private fun closeNavigationDrawer() {
        drawerLayout?.closeDrawer(navigationDrawer)
    }

    fun openNavigationDrawer() {
        drawerLayout?.openDrawer(navigationDrawer)
    }

    override fun <F> attachNavigationDrawer(fragment: F, toolbar: Toolbar) where F : Fragment, F : FragmentInteractions {
        ColorUtils.colorIt(ThemePrefs.primaryColor, navigationDrawerInstitutionImage.background)
        navigationDrawerInstitutionImage.loadUri(Uri.parse(ThemePrefs.logoUrl), R.mipmap.ic_launcher_foreground)

        //Navigation items
        navigationDrawerItem_files.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerItem_gauge.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerItem_studio.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerItem_bookmarks.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerItem_changeUser.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerItem_logout.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerSettings.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerItem_startMasquerading.setOnClickListener(mNavigationDrawerItemClickListener)
        navigationDrawerItem_stopMasquerading.setOnClickListener(mNavigationDrawerItemClickListener)

        //Load Show Grades
        navigationDrawerShowGradesSwitch.isChecked = StudentPrefs.showGradesOnCard
        navigationDrawerShowGradesSwitch.setOnCheckedChangeListener { _, isChecked ->
            StudentPrefs.showGradesOnCard = isChecked
            EventBus.getDefault().post(ShowGradesToggledEvent)
        }
        ViewStyler.themeSwitch(this@NavigationActivity, navigationDrawerShowGradesSwitch, ThemePrefs.brandColor)

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

        toolbar.setNavigationIcon(R.drawable.vd_hamburger)
        toolbar.navigationContentDescription = getString(R.string.navigation_drawer_open)
        toolbar.setNavigationOnClickListener {
            openNavigationDrawer()
        }

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)

        mDrawerToggle = object : ActionBarDrawerToggle(this@NavigationActivity, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                invalidateOptionsMenu()
                // Make the scrollview that is inside the drawer scroll to the top
                navigationDrawer.scrollTo(0, 0)
            }
        }

        drawerLayout.post { mDrawerToggle!!.syncState() }
        drawerLayout.addDrawerListener(mDrawerToggle!!)

        setupUserDetails(ApiPrefs.user)

        ViewStyler.themeToolbar(this, toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)

        navigationDrawerItem_startMasquerading.setVisible(!ApiPrefs.isMasquerading && ApiPrefs.canBecomeUser == true)
        navigationDrawerItem_stopMasquerading.setVisible(ApiPrefs.isMasquerading)
    }

    private fun setUpColorOverlaySwitch() {
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

    //endregion

    //region Bottom Bar Navigation

    private val bottomBarItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
        when (item.itemId) {
            R.id.bottomNavigationCourses -> handleRoute(Route(DashboardFragment::class.java, ApiPrefs.user))
            R.id.bottomNavigationCalendar -> handleRoute(CalendarFragment.makeRoute())
            R.id.bottomNavigationToDo -> {
                val route = ToDoListFragment.makeRoute(ApiPrefs.user!!)
                addFragment(ToDoListFragment.newInstance(route), route)
            }
            R.id.bottomNavigationNotifications ->{
                val route = NotificationListFragment.makeRoute(ApiPrefs.user!!)
                addFragment(NotificationListFragment.newInstance(route), route)
            }
            R.id.bottomNavigationInbox -> {
                if (ApiPrefs.isStudentView) {
                    // Inbox not available in Student View
                    val route = NothingToSeeHereFragment.makeRoute()
                    addFragment(NothingToSeeHereFragment.newInstance(), route)
                } else {
                    val route = InboxFragment.makeRoute()
                    addFragment(InboxFragment.newInstance(route), route)
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
                R.id.bottomNavigationCourses -> abortReselect = currentFragmentClass.isAssignableFrom(DashboardFragment::class.java)
                R.id.bottomNavigationCalendar -> abortReselect = currentFragmentClass.isAssignableFrom(CalendarFragment::class.java)
                R.id.bottomNavigationToDo -> abortReselect = currentFragmentClass.isAssignableFrom(ToDoListFragment::class.java)
                R.id.bottomNavigationNotifications -> abortReselect = currentFragmentClass.isAssignableFrom(NotificationListFragment::class.java)
                R.id.bottomNavigationInbox -> abortReselect = currentFragmentClass.isAssignableFrom(InboxFragment::class.java)
            }
        }

        if(!abortReselect) {
            when (item.itemId) {
                R.id.bottomNavigationCourses -> handleRoute(Route(DashboardFragment::class.java, ApiPrefs.user))
                R.id.bottomNavigationCalendar -> handleRoute(CalendarFragment.makeRoute())
                R.id.bottomNavigationToDo -> {
                    val route = ToDoListFragment.makeRoute(ApiPrefs.user!!)
                    addFragment(ToDoListFragment.newInstance(route), route)
                }
                R.id.bottomNavigationNotifications -> {
                    val route = NotificationListFragment.makeRoute(ApiPrefs.user!!)
                    addFragment(NotificationListFragment.newInstance(route), route)
                }
                R.id.bottomNavigationInbox -> {
                    if (ApiPrefs.isStudentView) {
                        // Inbox not available in Student View
                        val route = NothingToSeeHereFragment.makeRoute()
                        addFragment(NothingToSeeHereFragment.newInstance(), route)
                    } else {
                        val route = InboxFragment.makeRoute()
                        addFragment(InboxFragment.newInstance(route), route)
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        Logger.d("NavigationActivity:setupBottomNavigation()")
        bottomBar.applyTheme(ThemePrefs.brandColor, ContextCompat.getColor(this, R.color.bottomBarUnselectedItemColor))
        bottomBar.setOnNavigationItemSelectedListener(bottomBarItemSelectedListener)
        bottomBar.setOnNavigationItemReselectedListener(bottomBarItemReselectedListener)
        updateBottomBarContentDescriptions()
    }

    private fun setBottomBarItemSelected(itemId: Int) {
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
        loop@ bottomBar.menu.items.forEach {
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
            is CalendarEventFragment -> setBottomBarItemSelected(R.id.bottomNavigationCalendar)
            //To-do
            is ToDoListFragment -> setBottomBarItemSelected(R.id.bottomNavigationToDo)
            //Notifications
            is NotificationListFragment-> {
                setBottomBarItemSelected(if(fragment.isCourseOrGroup()) R.id.bottomNavigationCourses
                else R.id.bottomNavigationNotifications)
            }
            //Inbox
            is InboxFragment,
            is InboxConversationFragment,
            is InboxComposeMessageFragment,
            is InboxRecipientsFragment -> setBottomBarItemSelected(R.id.bottomNavigationInbox)
            //courses
            else -> setBottomBarItemSelected(R.id.bottomNavigationCourses)
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
        } else if (item.itemId == android.R.id.home) {
            //if we hit the x while we're on a detail fragment, we always want to close the top fragment
            //and not have it trigger an actual "back press"
            val topFragment = topFragment
            if (supportFragmentManager.backStackEntryCount > 0) {
                if (topFragment != null) {
                    supportFragmentManager.beginTransaction().remove(topFragment).commit()
                }
                super.onBackPressed()
            } else if (topFragment == null) {
                super.onBackPressed()
            }
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
                        RouteContext.FILE == route.routeContext -> {
                            if (route.queryParamsHash.containsKey(RouterParams.VERIFIER) && route.queryParamsHash.containsKey(RouterParams.DOWNLOAD_FRD)) {
                                if(route.uri != null) openMedia(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, contextId, ""), route.uri.toString())
                            }
                            route.paramsHash[RouterParams.FILE_ID]?.let { handleSpecificFile(contextId, it) }

                            if(route.canvasContext != null) addFragment(RouteResolver.getFragment(route), route)
                        }
                        RouteContext.LTI == route.routeContext -> {
                            val contextType = route.getContextType()
                            when (contextType) {
                                CanvasContext.Type.COURSE -> {
                                    route.canvasContext = awaitApi<Course> { CourseManager.getCourse(contextId, it, false) }
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
                                val ltiRoute = LTIWebViewFragment.makeRoute(it, route.uri.toString())
                                RouteMatcher.route(this@NavigationActivity, ltiRoute)
                            }
                        }
                        RouteContext.NOTIFICATION_PREFERENCES == route.routeContext -> {
                            Analytics.trackAppFlow(this@NavigationActivity, NotificationPreferencesActivity::class.java)
                            startActivity(Intent(this@NavigationActivity, NotificationPreferencesActivity::class.java))
                        }
                        else -> {
                            //fetch the CanvasContext
                            val contextType = route.getContextType()
                            when (contextType) {
                                CanvasContext.Type.COURSE -> {
                                    route.canvasContext = awaitApi<Course> { CourseManager.getCourse(contextId, it, false) }
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
        if (fragment == null) {
            Logger.e("NavigationActivity:addFragment() - Could not route null Fragment.")
            return
        }

        val ft = supportFragmentManager.beginTransaction()

        if (RouteType.DIALOG == route.routeType && fragment is DialogFragment && isTablet) {
            ft.addToBackStack(fragment::class.java.name)
            fragment.show(ft, fragment::class.java.name)
        } else {
            ft.setCustomAnimations(R.anim.fade_in_quick, R.anim.fade_out_quick)
            currentFragment?.let { ft.hide(it) }
            ft.add(R.id.fullscreen, fragment, fragment::class.java.name)
            ft.addToBackStack(fragment::class.java.name)
            ft.commitAllowingStateLoss()
        }
    }

    //endregion

    //region Back Stack

    override fun onBackPressed() {
        if(isDrawerOpen) {
            closeNavigationDrawer()
            return
        }

        if (supportFragmentManager.backStackEntryCount == 1) {
            // Exits if we only have one fragment
            finish()
            return
        }

        val topFragment = topFragment
        if (topFragment is ParentFragment) {
            if (!topFragment.handleBackPressed()) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun getTopFragment(): Fragment? {
        val stackSize = supportFragmentManager.backStackEntryCount
        if(stackSize > 0) {
            val fragmentTag = supportFragmentManager.getBackStackEntryAt(stackSize - 1).name
            return supportFragmentManager.findFragmentByTag(fragmentTag)
        }
        return null
    }

    override fun getPeekingFragment(): Fragment? {
        val stackSize = supportFragmentManager.backStackEntryCount
        if(stackSize > 1) {
            val fragmentTag = supportFragmentManager.getBackStackEntryAt(stackSize - 2).name
            return supportFragmentManager.findFragmentByTag(fragmentTag)
        }
        return null
    }

    override fun getCurrentFragment(): Fragment? = supportFragmentManager.findFragmentById(R.id.fullscreen)

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
        loadingRoute.setVisible()
    }

    override fun hideLoadingIndicator() {
        loadingRoute.setGone()
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
        val studioLaunchDefinition = launchDefinitions?.firstOrNull { it.domain == LaunchDefinition._STUDIO_DOMAIN }
        val gaugeLaunchDefinition = launchDefinitions?.firstOrNull { it.domain == LaunchDefinition._GAUGE_DOMAIN }

        val studio = findViewById<View>(R.id.navigationDrawerItem_studio)
        studio.visibility = if (studioLaunchDefinition != null) View.VISIBLE else View.GONE
        studio.tag = studioLaunchDefinition

        val gauge = findViewById<View>(R.id.navigationDrawerItem_gauge)
        gauge.visibility = if (gaugeLaunchDefinition != null) View.VISIBLE else View.GONE
        gauge.tag = gaugeLaunchDefinition
    }

    override fun updateCalendarStartDay() {
        //Restarts the CalendarListViewFragment to update the changed start day of the week
        val fragment = supportFragmentManager.findFragmentByTag(CalendarFragment::class.java.name) as? ParentFragment
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        val route = CalendarFragment.makeRoute()
        addFragment(CalendarFragment.newInstance(route), route)
    }

    override fun addBookmark() {
        val dialog = BookmarkCreationDialog.newInstance(this, topFragment, peekingFragment)
        dialog?.show(supportFragmentManager, BookmarkCreationDialog::class.java.simpleName)
    }

    override fun updateUnreadCount(unreadCount: String) {
        // get the view
        val bottomBarNavView = bottomBar?.getChildAt(0)
        // get the inbox item
        val view = (bottomBarNavView as BottomNavigationMenuView).getChildAt(4)

        // create the badge, set the text and color it
        val unreadCountValue = unreadCount.toInt()
        var unreadCountDisplay = unreadCount
        if(unreadCountValue > 99) {
            unreadCountDisplay = getString(R.string.moreThan99)
        } else if(unreadCountValue <= 0) {
            //don't set the badge or display it, remove any badge
            if(view.children.size > 2 && view.children[2] is TextView) {
                (view as BottomNavigationItemView).removeViewAt(2)
            }
            // update content description with no unread count number
            bottomBar.menu.items.find { it.itemId == R.id.bottomNavigationInbox }.let {
                val title = it?.title
                MenuItemCompat.setContentDescription(it, title)
            }
            return
        }

        // update content description
        bottomBar.menu.items.find { it.itemId == R.id.bottomNavigationInbox }.let {
            var title: String = it?.title as String
            title += "$unreadCountValue  "  + getString(R.string.unread)
            MenuItemCompat.setContentDescription(it, title)
        }

        // check to see if we already have a badge created
        with((view as BottomNavigationItemView)) {
            // first child is the imageView that we use for the bottom bar, second is a layout for the label
            if(childCount > 2 && getChildAt(2) is TextView) {
                (getChildAt(2) as TextView).text = unreadCountDisplay
            } else {
                // no badge, we need to create one
                val badge = LayoutInflater.from(context)
                        .inflate(R.layout.unread_count, bottomBar, false)
                (badge as TextView).text = unreadCountDisplay

                ColorUtils.colorIt(ContextCompat.getColor(context, R.color.electricBlueBadge), badge.background)
                addView(badge)
            }
        }
    }

    /** Handles status bar color change events posted by FlutterComm */
    @Subscribe
    fun updateStatusBarColor(event: StatusBarColorChangeEvent) {
        event.get { color ->
            if (color == Color.WHITE) {
                ViewStyler.setStatusBarLight(this)
            } else {
                ViewStyler.setStatusBarDark(this, color)
            }
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

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, NavigationActivity::class.java)
        }

        fun createIntent(context: Context, route: Route): Intent {
            return Intent(context, NavigationActivity::class.java).apply { putExtra(Route.ROUTE, route) }
        }

        fun createIntent(context: Context, extras: Bundle): Intent {
            val intent = Intent(context, NavigationActivity::class.java)
            intent.putExtra(Const.EXTRAS, extras)
            return intent
        }

        fun createIntent(context: Context, message: String, messageType: Int): Intent {
            val intent = createIntent(context)
            intent.putExtra(Const.MESSAGE, message)
            intent.putExtra(Const.MESSAGE_TYPE, messageType)
            return intent
        }

        fun createIntent(context: Context, masqueradingUserId: Long): Intent = createIntent(context).apply {
            putExtra(Const.QR_CODE_MASQUERADE_ID, masqueradingUserId)
        }

        val startActivityClass: Class<out Activity>
            get() = NavigationActivity::class.java
    }
}
