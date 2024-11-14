/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.dashboard

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.User
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.help.HelpDialogFragment
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.animateCircularBackgroundColorChange
import com.instructure.pandautils.utils.applyTheme
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.studentColor
import com.instructure.pandautils.utils.toPx
import com.instructure.parentapp.R
import com.instructure.parentapp.databinding.FragmentDashboardBinding
import com.instructure.parentapp.databinding.NavigationDrawerHeaderLayoutBinding
import com.instructure.parentapp.features.addstudent.AddStudentBottomSheetDialogFragment
import com.instructure.parentapp.features.addstudent.AddStudentViewModel
import com.instructure.parentapp.features.addstudent.AddStudentViewModelAction
import com.instructure.parentapp.util.ParentLogoutTask
import com.instructure.parentapp.util.ParentPrefs
import com.instructure.parentapp.util.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject


@AndroidEntryPoint
class DashboardFragment : BaseCanvasFragment(), NavigationCallbacks {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var navigation: Navigation

    @Inject
    lateinit var calendarSharedEvents: CalendarSharedEvents

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    private lateinit var navController: NavController
    private lateinit var headerLayoutBinding: NavigationDrawerHeaderLayoutBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    private var inboxBadge: TextView? = null

    private val addStudentViewModel: AddStudentViewModel by activityViewModels()

    private val onItemSelectedListener = NavigationBarView.OnItemSelectedListener {
        when (it.itemId) {
            R.id.courses -> navigateWithPopBackStack(navigation.courses)
            R.id.calendar -> navigateWithPopBackStack(navigation.calendar)
            R.id.alerts -> navigateWithPopBackStack(navigation.alerts)
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.let {
            navController = it.navController
            navController.graph = navigation.createDashboardNavGraph(navController)
        }
    }

    private val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        if (destination.route == navigation.alerts || destination.route == navigation.courses) {
            binding.todayButtonHolder.setGone()
        }
        val menuId = when (destination.route) {
            navigation.alerts -> R.id.alerts
            navigation.courses -> R.id.courses
            navigation.calendar -> R.id.calendar
            else -> return@OnDestinationChangedListener
        }
        bottomNavigationView.menu.findItem(menuId).isChecked = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(calendarSharedEvents.events, ::handleSharedCalendarAction)

        lifecycleScope.launch {
            addStudentViewModel.events.collectLatest(::handleAddStudentEvents)
        }
        return binding.root
    }

    private fun handleAddStudentEvents(action: AddStudentViewModelAction) {
        when (action) {
            is AddStudentViewModelAction.PairStudentSuccess -> {
                viewModel.reloadData()
            }
            is AddStudentViewModelAction.UnpairStudentSuccess -> {
                viewModel.reloadData()
            }
        }
    }

    private fun handleSharedCalendarAction(sharedCalendarAction: SharedCalendarAction) {
        if (sharedCalendarAction is SharedCalendarAction.TodayButtonVisible) {
            binding.todayButtonHolder.setVisible(sharedCalendarAction.visible)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)

        lifecycleScope.launch {
            viewModel.data.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collectLatest {
                setupNavigationDrawerHeader(it.userViewData)
                setupLaunchDefinitions(it.launchDefinitionViewData)
                setupAppColors(it.selectedStudent)
                updateUnreadCount(it.unreadCount)
                updateAlertCount(it.alertCount)
            }
        }

        lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomNavigationView.setOnItemSelectedListener(null)
        navController.removeOnDestinationChangedListener(onDestinationChangedListener)
    }

    private fun handleAction(action: DashboardViewModelAction) {
        when (action) {
            is DashboardViewModelAction.AddStudent -> {
                AddStudentBottomSheetDialogFragment().show(childFragmentManager, AddStudentBottomSheetDialogFragment::class.java.simpleName)
            }
            is DashboardViewModelAction.NavigateDeepLink -> {
                try {
                    navController.navigate(action.deepLinkUri)
                } catch (e: Exception) {
                    firebaseCrashlytics.recordException(e)
                }
            }
            is DashboardViewModelAction.OpenLtiTool -> {
                navigation.navigate(requireActivity(), navigation.ltiLaunchRoute(action.url, action.name))
            }
        }
    }

    private fun updateAlertCount(alertCount: Int) {
        val badge = binding.bottomNav.getOrCreateBadge(R.id.alerts)
        badge.verticalOffset = 10
        badge.horizontalOffset = 10
        badge.setVisible(alertCount != 0, true)
        badge.maxNumber = 99
        badge.number = alertCount
    }

    private fun updateUnreadCount(unreadCount: Int) {
        val unreadCountText = if (unreadCount <= 99) unreadCount.toString() else requireContext().getString(R.string.inboxUnreadCountMoreThan99)
        inboxBadge?.visibility = if (unreadCount == 0) View.GONE else View.VISIBLE
        inboxBadge?.text = unreadCountText
        binding.unreadCountBadge.visibility = if (unreadCount == 0) View.GONE else View.VISIBLE
        binding.unreadCountBadge.text = unreadCountText

        val navButtonContentDescription = if (unreadCount == 0) {
            getString(R.string.navigation_drawer_open)
        } else {
            getString(R.string.a11y_parentOpenNavigationDrawerWithBadge, unreadCountText)
        }

        binding.navigationButtonHolder.contentDescription = navButtonContentDescription
    }

    private fun setupNavigation() {
        if (!this::navController.isInitialized) {
            val navHostFragment =
                childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController = navHostFragment.navController
            navController.graph = navigation.createDashboardNavGraph(navController)
        }

        setupToolbar()
        setupNavigationDrawer()
        setupBottomNavigationView()
    }

    private fun setupToolbar() {
        binding.navigationButtonHolder.contentDescription = getString(R.string.navigation_drawer_open)
        binding.navigationButtonHolder.onClick {
            openNavigationDrawer()
        }
        binding.todayButtonHolder.onClick {
            calendarSharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.TodayButtonTapped)
        }
        binding.todayButtonText.text = LocalDate.now().dayOfMonth.toString()
    }

    private fun setupNavigationDrawer() {
        val navView = binding.navView

        headerLayoutBinding = NavigationDrawerHeaderLayoutBinding.bind(navView.getHeaderView(0))

        val actionView = (navView.menu.findItem(R.id.inbox)).actionView as LinearLayout
        actionView.gravity = Gravity.CENTER

        inboxBadge = TextView(requireContext())
        actionView.addView(inboxBadge)

        inboxBadge?.width = 24.toPx
        inboxBadge?.height = 24.toPx
        inboxBadge?.gravity = Gravity.CENTER
        inboxBadge?.textSize = 10f
        inboxBadge?.setTextColor(requireContext().getColor(R.color.white))
        inboxBadge?.setBackgroundResource(R.drawable.bg_button_full_rounded_filled)
        inboxBadge?.visibility = View.GONE


        navView.setNavigationItemSelectedListener {
            closeNavigationDrawer()
            when (it.itemId) {
                R.id.inbox -> menuItemSelected { navigation.navigate(activity, navigation.inbox) }
                R.id.manage_students -> menuItemSelected { navigation.navigate(activity, navigation.manageStudents) }
                R.id.mastery -> menuItemSelected { viewModel.openMastery() }
                R.id.studio -> menuItemSelected { viewModel.openStudio() }
                R.id.settings -> menuItemSelected { navigation.navigate(activity, navigation.settings) }
                R.id.help -> menuItemSelected { activity?.let { HelpDialogFragment.show(it) } }
                R.id.log_out -> menuItemSelected { onLogout() }
                R.id.switch_users -> menuItemSelected { onSwitchUsers() }
                else -> false
            }
        }
    }

    private fun menuItemSelected(action: () -> Unit): Boolean {
        action()
        return true
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView = binding.bottomNav
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
    }

    private fun navigateWithPopBackStack(route: String): Boolean {
        navController.popBackStack()
        navController.navigate(route)
        return true
    }

    private fun setupAppColors(student: User?) {
        val color = student.studentColor
        if (binding.toolbar.background == null) {
            binding.toolbar.setBackgroundColor(color)
        } else {
            binding.toolbar.animateCircularBackgroundColorChange(color, binding.toolbarImage)
        }
        inboxBadge?.backgroundTintList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(color))
        binding.bottomNav.applyTheme(color, requireActivity().getColor(R.color.textDarkest))
        ViewStyler.setStatusBarDark(requireActivity(), color)

        val gradientDrawable = requireContext().getDrawableCompat(R.drawable.bg_button_full_rounded_filled_with_border) as? GradientDrawable
        gradientDrawable?.setStroke(2.toPx, color)
        binding.unreadCountBadge.background = gradientDrawable
        binding.unreadCountBadge.setTextColor(color)

        binding.bottomNav.getOrCreateBadge(R.id.alerts).backgroundColor = color
        viewModel.updateColor(color)
    }

    private fun openNavigationDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun closeNavigationDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun setupNavigationDrawerHeader(userViewData: UserViewData?) {
        headerLayoutBinding.userViewData = userViewData
    }

    private fun onLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.logout_warning)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ParentLogoutTask(LogoutTask.Type.LOGOUT).execute()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .showThemed(ParentPrefs.currentStudent.studentColor)
    }

    private fun onSwitchUsers() {
        ParentLogoutTask(LogoutTask.Type.SWITCH_USERS).execute()
    }

    private fun setupLaunchDefinitions(launchDefinitionViewData: List<LaunchDefinitionViewData>) {
        val masteryItem = launchDefinitionViewData.find { it.domain == LaunchDefinition.MASTERY_DOMAIN }
        if (masteryItem != null) {
            val masteryMenuItem = binding.navView.menu.findItem(R.id.mastery)
            masteryMenuItem.isVisible = true
        }

        val studioItem = launchDefinitionViewData.find { it.domain == LaunchDefinition.STUDIO_DOMAIN }
        if (studioItem != null) {
            val studioMenuItem = binding.navView.menu.findItem(R.id.studio)
            studioMenuItem.isVisible = true
        }
    }

    override fun onHandleBackPressed(): Boolean {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeNavigationDrawer()
            return true
        }
        return false
    }
}
