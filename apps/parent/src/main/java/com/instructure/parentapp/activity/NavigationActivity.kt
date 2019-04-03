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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.UnreadCountManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.loginapi.login.dialog.MasqueradingDialog
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.utils.*
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.R
import com.instructure.parentapp.adapter.UserSpinnerAdapter
import com.instructure.parentapp.fragments.AlertFragment
import com.instructure.parentapp.fragments.CourseListFragment
import com.instructure.parentapp.fragments.WeekFragment
import com.instructure.parentapp.tasks.ParentLogoutTask
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.ViewUtils
import kotlinx.android.synthetic.main.activity_student_view.*
import kotlinx.android.synthetic.main.alert_badge.view.*
import kotlinx.android.synthetic.main.navigation_drawer.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import retrofit2.Response
import java.util.*

class NavigationActivity : BaseParentActivity(), MasqueradingDialog.OnMasqueradingSet {

    private var userSpinner: AppCompatSpinner? = null

    private var drawerItemSelectedJob: Job? = null

    private var unreadAlertsFetchJob: WeaveJob? = null

    private val users: Array<User>
        get() = intent?.extras?.getParcelableArrayList<User>(Const.USER)?.toTypedArray() ?: emptyArray()

    private val alertBadge: TextView?
        get() {
            return bottomBar.alertBadge
                    ?: (bottomBar.getChildAt(0) as? BottomNavigationMenuView)?.let { nav ->
                        (nav.getChildAt(2) as? ViewGroup)?.let {
                            val badge = LayoutInflater.from(this).inflate(R.layout.alert_badge, it, false)
                            it.addView(badge)
                            bottomBar.alertBadge
                        }
                    }
        }

    val currentStudent: User get() = (userSpinner?.selectedItem as? User) ?: User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock phones into portrait, no change for tablets
        ViewUtils.setScreen(this)
        setContentView(R.layout.activity_student_view)
        setupViews()

        @Suppress("ConstantConditionIf") // Always true if building for debug
        if (!BuildConfig.IS_TESTING) {
            RatingDialog.showRatingDialog(this@NavigationActivity, AppType.PARENT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        drawerItemSelectedJob?.cancel()
        unreadAlertsFetchJob?.cancel()
    }

    override fun onPause() {
        super.onPause()
        // Save the position so when the parent comes back to this page it will load the tab they were on last
        ParentPrefs.selectedTab = bottomBar.selectedItemId
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == com.instructure.parentapp.util.Const.SETTINGS_ACTIVITY_REQUEST_CODE) {
            // Make the api call to get all the students
            EnrollmentManager.getObserveeEnrollments(true,
                    object : StatusCallback<List<Enrollment>>() {
                        override fun onResponse(response: Response<List<Enrollment>>, linkHeaders: LinkHeaders, type: ApiType) {
                            // Only non-cache data
                            if (!APIHelper.isCachedResponse(response)) {
                                // Replace the data that the carousel will try to use
                                if (intent.extras?.getParcelableArrayList<Parcelable>(Const.USER) != null) {
                                    // Use distinct() to prevent duplicates
                                    val students = response.body()?.mapNotNull { it.observedUser }?.distinct() ?: emptyList()
                                    val intent = intent.putParcelableArrayListExtra(Const.USER, ArrayList<Parcelable>(students))
                                    getIntent().replaceExtras(intent)
                                }

                                setupViews()
                            }
                        }
                    }
            )
        }
    }

    private fun setupViews() {
        // Give the TabLayout the ViewPager
        bottomBar.setOnNavigationItemSelectedListener { item ->
            val page = when (item.itemId) {
                R.id.tabCourses -> 0
                R.id.tabWeek -> 1
                R.id.tabAlerts -> 2
                else -> throw IndexOutOfBoundsException()
            }
            updateBottomBarContentDescriptions(item.itemId)
            setFragment(page)
            true
        }

        if (users.isNotEmpty()) {
            configureUserSpinner()
        } else {
            setFragment(ParentPrefs.selectedStudentIdx)
        }

        setupNavDrawer()
        setupToolbar()
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun fetchAlerts() {
        // Cancel any ongoing fetch
        unreadAlertsFetchJob?.cancel()

        // Start new fetch
        unreadAlertsFetchJob = tryWeave {
            // Reset count
            val unreadCount = awaitApi<UnreadCount> { UnreadCountManager.getUnreadAlertCount(currentStudent.id, it, true) }
            updateAlertUnreadCount(unreadCount.unreadCount)
        } catch {

        }
    }

    private fun setupNavDrawer() {
        ColorUtils.colorIt(ThemePrefs.primaryColor, navigationDrawerInstitutionImage.background)
        navigationDrawerInstitutionImage.loadUri(Uri.parse(ThemePrefs.logoUrl), R.mipmap.ic_launcher_foreground)

        // Navigation items
        navigationDrawerItem_changeUser.setOnClickListener(navDrawerOnClick)
        navigationDrawerItem_logout.setOnClickListener(navDrawerOnClick)
        navigationDrawerItem_stopMasquerading.setOnClickListener(navDrawerOnClick)
        navigationDrawerItem_startMasquerading.setOnClickListener(navDrawerOnClick)
        navigationDrawerItem_manageChildren.setOnClickListener(navDrawerOnClick)
        navigationDrawerItem_help.setOnClickListener(navDrawerOnClick)

        // App version
        navigationDrawerVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        setupUserDetails(ApiPrefs.user)

        navigationDrawerItem_startMasquerading.setVisible(!ApiPrefs.isMasquerading && ApiPrefs.canBecomeUser == true)
        navigationDrawerItem_stopMasquerading.setVisible(ApiPrefs.isMasquerading)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { openDrawer() }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private val navDrawerOnClick = View.OnClickListener { v ->
        drawerItemSelectedJob = weave {
            closeDrawer()
            delay(250)
            when (v.id) {
                R.id.navigationDrawerItem_changeUser -> {
                    ParentLogoutTask(LogoutTask.Type.SWITCH_USERS).execute()
                }

                R.id.navigationDrawerItem_manageChildren -> {
                    // Use start activity for result because we need to be aware if they remove a student
                    startActivityForResult(SettingsActivity.createIntent(this@NavigationActivity, ""), com.instructure.parentapp.util.Const.SETTINGS_ACTIVITY_REQUEST_CODE)
                    overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out)
                }

                R.id.navigationDrawerItem_logout -> {
                    AlertDialog.Builder(this@NavigationActivity)
                            .setTitle(R.string.logout_warning)
                            .setPositiveButton(android.R.string.yes) { _, _ -> ParentLogoutTask(LogoutTask.Type.LOGOUT).execute() }
                            .setNegativeButton(android.R.string.no, null)
                            .create()
                            .show()
                }

                R.id.navigationDrawerItem_help -> {
                    AnalyticUtils.trackButtonPressed(AnalyticUtils.HELP)
                    startActivity(HelpActivity.createIntent(this@NavigationActivity))
                }

                R.id.navigationDrawerItem_stopMasquerading -> onStopMasquerading()

                R.id.navigationDrawerItem_startMasquerading -> MasqueradingDialog.show(supportFragmentManager, ApiPrefs.domain, null, !resources.getBoolean(R.bool.isTablet))
            }
        }
    }

    private fun setupUserDetails(user: User?) {
        user ?: return
        navigationDrawerUserName.setVisible(user.shortName != null).text = user.shortName
        navigationDrawerUserEmail.text = user.primaryEmail

        if (ProfileUtils.shouldLoadAltAvatarImage(user.avatarUrl)) {
            val initials = ProfileUtils.getUserInitials(user.shortName ?: "")
            val color = ContextCompat.getColor(this, R.color.avatarGray)
            val drawable = TextDrawable.builder()
                    .beginConfig()
                    .height(resources.getDimensionPixelSize(R.dimen.profileAvatarSize))
                    .width(resources.getDimensionPixelSize(R.dimen.profileAvatarSize))
                    .toUpperCase()
                    .useFont(Typeface.DEFAULT_BOLD)
                    .textColor(color)
                    .endConfig()
                    .buildRound(initials, Color.WHITE)
            navigationDrawerProfileImage.setImageDrawable(drawable)
        } else {
            Glide.with(this).load(user.avatarUrl).into(navigationDrawerProfileImage)
        }
    }

    private fun openDrawer() {
        drawerLayout.openDrawer(navigationDrawer)
    }

    private fun closeDrawer() {
        drawerLayout.closeDrawer(navigationDrawer)
    }

    fun updateAlertUnreadCount(unreadCount: Int) {
        val text = if (unreadCount > 9) getString(R.string.greaterThan9) else unreadCount.toString()
        alertBadge?.setVisible(unreadCount > 0)?.text = text
    }

    private fun configureUserSpinner() {
        userSpinner = toolbar.findViewById(R.id.action_bar_spinner)
                ?: AppCompatSpinner(this).apply {
                    id = R.id.action_bar_spinner
                    backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                    toolbar.addView(this)
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            val page = when (bottomBar.selectedItemId) {
                                R.id.tabWeek -> 1
                                R.id.tabAlerts -> 2
                                else -> 0
                            }
                            updateBottomBarContentDescriptions(bottomBar.selectedItemId)
                            setFragment(page)
                            // Save the position so when the parent comes back to this page it will load the student they were on last
                            ParentPrefs.selectedStudentIdx = position
                        }
                    }
                }

        userSpinner!!.apply {
            fetchAlerts()

            post {
                adapter = UserSpinnerAdapter(this@NavigationActivity, users)
                setSelection(ParentPrefs.selectedStudentIdx)
            }
        }
    }

    private fun updateActivityColors(color: Int) {
        ViewStyler.themeToolbar(this, toolbar, color, Color.WHITE)
        window.statusBarColor = ViewUtils.darker(color, 0.9f)
        toolbar.setBackgroundColor(color)
        val colorList = getBottomBarColors(color)
        bottomBar.itemIconTintList = colorList
        bottomBar.itemTextColor = colorList
        alertBadge?.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun updateBottomBarContentDescriptions(selectedItemId: Int = -1) {
        // Manually apply content description on each MenuItem since BottomNavigationView won't
        // automatically set it from either the title or content description specified in the menu xml
        bottomBar.menu.items.forEach {
            val title = if (it.itemId == selectedItemId) getString(R.string.selected) + " " + it.title else it.title
            MenuItemCompat.setContentDescription(it, title)
        }
    }

    private fun setFragment(position: Int) {
        // selectedItemPosition is sometimes -1 when a site admin has logged in and they have no students they are observing
        ParentPrefs.currentColor = getUserColor(userSpinner?.selectedItemPosition.takeUnless { it == -1 }
                ?: 0)

        val fragment: Fragment = when (position) {
            0 -> CourseListFragment.newInstance(currentStudent)
            1 -> WeekFragment.newInstance(currentStudent)
            2 -> AlertFragment.newInstance(currentStudent)
            else -> throw IllegalStateException("Invalid fragment position $position")
        }
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.fragmentContainer, fragment)
            commitAllowingStateLoss()
        }
        updateActivityColors(ParentPrefs.currentColor)
    }

    private fun getBottomBarColors(@ColorInt color: Int): ColorStateList {
        val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_selected)
        )

        val colors = intArrayOf(
                color,
                ContextCompat.getColor(this, R.color.defaultTextGray),
                ContextCompat.getColor(this, R.color.defaultTextGray)
        )

        return ColorStateList(states, colors)
    }

    override fun onStartMasquerading(domain: String, userId: Long) {
        MasqueradeHelper.startMasquerading(userId, domain, LoginActivity::class.java)
    }

    override fun onStopMasquerading() {
        MasqueradeHelper.stopMasquerading(LoginActivity::class.java)
    }

    companion object {

        private val colors = intArrayOf(
                0xFF008EE2.toInt(), // Blue
                0xFF5443C1.toInt(), // Indigo
                0xFFEC3349.toInt(), // Red
                0xFF00AC18.toInt(), // Green
                0xFFFC5E13.toInt(), // Orange
                0xFFBF32A4.toInt() // Red-Violet
        )

        private fun getUserColor(idx: Int) = colors[idx % colors.size]

        fun createIntent(context: Context) = Intent(context, NavigationActivity::class.java)

        fun createIntent(context: Context, students: List<User>): Intent = createIntent(context).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            putParcelableArrayListExtra(Const.USER, ArrayList<Parcelable>(students))
        }

        fun createIntent(context: Context, message: String, messageType: Int): Intent = createIntent(context).apply {
            putExtra(Const.MESSAGE, message)
            putExtra(Const.MESSAGE_TYPE, messageType)
        }
    }
}
