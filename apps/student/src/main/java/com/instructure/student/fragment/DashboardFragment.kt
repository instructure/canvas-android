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

package com.instructure.student.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.managers.CourseNicknameManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_DASHBOARD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationsFragment
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.DashboardRecyclerAdapter
import com.instructure.student.decorations.VerticalGridSpacingDecoration
import com.instructure.student.dialog.ColorPickerDialog
import com.instructure.student.dialog.EditCourseNicknameDialog
import com.instructure.student.events.CoreDataFinishedLoading
import com.instructure.student.events.CourseColorOverlayToggledEvent
import com.instructure.student.events.ShowGradesToggledEvent
import com.instructure.student.features.dashboard.edit.EditDashboardFragment
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.StudentPrefs
import kotlinx.android.synthetic.main.course_grid_recycler_refresh_layout.*
import kotlinx.android.synthetic.main.fragment_course_grid.*
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.swipeRefreshLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.listView as recyclerView

@ScreenView(SCREEN_VIEW_DASHBOARD)
@PageView
class DashboardFragment : ParentFragment() {

    private var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: DashboardRecyclerAdapter? = null

    private var courseColumns: Int = 1
    private var groupColumns: Int = 1

    private val somethingChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (recyclerAdapter != null && intent?.extras?.getBoolean(Const.COURSE_FAVORITES) == true) {
                swipeRefreshLayout?.isRefreshing = true
                recyclerAdapter?.refresh()
            }
        }
    }

    override fun title(): String = if (isAdded) getString(R.string.dashboard) else ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.fragment_course_grid, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recyclerAdapter = DashboardRecyclerAdapter(requireActivity(), object : CourseAdapterToFragmentCallback {

            override fun onRefreshFinished() {
                swipeRefreshLayout?.isRefreshing = false
                notificationsFragment.setVisible()
            }

            override fun onSeeAllCourses() {
                RouteMatcher.route(requireContext(), EditDashboardFragment.makeRoute())
            }

            override fun onGroupSelected(group: Group) {
                canvasContext = group
                RouteMatcher.route(requireContext(), CourseBrowserFragment.makeRoute(group))
            }

            override fun onCourseSelected(course: Course) {
                canvasContext = course
                RouteMatcher.route(requireContext(), CourseBrowserFragment.makeRoute(course))
            }

            @Suppress("EXPERIMENTAL_FEATURE_WARNING")
            override fun onEditCourseNickname(course: Course) {
                EditCourseNicknameDialog.getInstance(requireFragmentManager(), course) { s ->
                    tryWeave {
                        val response = awaitApi<CourseNickname> { CourseNicknameManager.setCourseNickname(course.id, s, it) }
                        if (response.nickname == null) {
                            course.name = response.name!!
                            course.originalName = null
                        } else {
                            course.name = response.nickname!!
                            course.originalName = response.name
                        }
                        recyclerAdapter?.addOrUpdateItem(DashboardRecyclerAdapter.ItemType.COURSE_HEADER, course)
                    } catch {
                        toast(R.string.courseNicknameError)
                    }
                }.show(requireFragmentManager(), EditCourseNicknameDialog::class.java.simpleName)
            }

            @Suppress("EXPERIMENTAL_FEATURE_WARNING")
            override fun onPickCourseColor(course: Course) {
                ColorPickerDialog.newInstance(requireFragmentManager(), course) { color ->
                    tryWeave {
                        awaitApi<CanvasColor> { UserManager.setColors(it, course.contextId, color) }
                        ColorKeeper.addToCache(course.contextId, color)
                        FlutterComm.sendUpdatedTheme()
                        recyclerAdapter?.notifyDataSetChanged()
                    } catch {
                        toast(R.string.colorPickerError)
                    }
                }.show(requireFragmentManager(), ColorPickerDialog::class.java.simpleName)
            }
        })

        configureRecyclerView()
        recyclerView.isSelectionEnabled = false
    }

    override fun applyTheme() {
        toolbar.title = title()
        // Styling done in attachNavigationDrawer
        navigation?.attachNavigationDrawer(this, toolbar)

        toolbar.setMenu(R.menu.menu_dashboard) { item ->
            when (item.itemId) {
                R.id.menu_dashboard_cards -> changeDashboardLayout(item)
            }
        }

        val dashboardLayoutMenuItem = toolbar.menu.findItem(R.id.menu_dashboard_cards)
        val menuIconRes = if (StudentPrefs.listDashboard) R.drawable.ic_grid_dashboard else R.drawable.ic_list_dashboard
        dashboardLayoutMenuItem.setIcon(menuIconRes)
    }

    private fun changeDashboardLayout(item: MenuItem) {
        if (StudentPrefs.listDashboard) {
            item.setIcon(R.drawable.ic_list_dashboard)
            StudentPrefs.listDashboard = false
        } else {
            item.setIcon(R.drawable.ic_grid_dashboard)
            StudentPrefs.listDashboard = true
        }

        recyclerView.fadeAnimationWithAction {
            courseColumns = if (StudentPrefs.listDashboard) 1 else resources.getInteger(R.integer.course_card_columns)
            groupColumns = if (StudentPrefs.listDashboard) 1 else resources.getInteger(R.integer.group_card_columns)
            (recyclerView.layoutManager as? GridLayoutManager)?.spanCount = courseColumns * groupColumns
            view?.post { recyclerAdapter?.notifyDataSetChanged() }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isTablet) {
                emptyCoursesView.setGuidelines(.37f, .49f, .6f, .7f, .12f, .88f)
            } else {
                emptyCoursesView.setGuidelines(.36f, .54f, .64f,.77f, .12f, .88f)

            }
        } else {
            if (isTablet) {
                // Change nothing, at least for now
            } else {
                emptyCoursesView.setGuidelines(.27f, .52f, .58f,.73f, .15f, .85f)
            }
        }
    }

    private fun configureRecyclerView() {
        // Set up GridLayoutManager
        courseColumns = if (StudentPrefs.listDashboard) 1 else resources.getInteger(R.integer.course_card_columns)
        groupColumns = if (StudentPrefs.listDashboard) 1 else resources.getInteger(R.integer.group_card_columns)
        val layoutManager = GridLayoutManager(
            context,
            courseColumns * groupColumns,
            RecyclerView.VERTICAL,
            false
        )
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = recyclerView.adapter!!.getItemViewType(position)
                return when (DashboardRecyclerAdapter.ItemType.values()[viewType]) {
                    DashboardRecyclerAdapter.ItemType.COURSE -> groupColumns
                    DashboardRecyclerAdapter.ItemType.GROUP -> courseColumns
                    else -> courseColumns * groupColumns
                }
            }
        }

        // Add decoration
        recyclerView.removeAllItemDecorations()
        recyclerView.addItemDecoration(VerticalGridSpacingDecoration(requireContext(), layoutManager))

        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = recyclerAdapter
        recyclerView.setEmptyView(emptyCoursesView)
        swipeRefreshLayout.setOnRefreshListener {
            if (!Utils.isNetworkAvailable(context)) {
                swipeRefreshLayout.isRefreshing = false
            } else {
                recyclerAdapter?.refresh()
                notificationsFragment.setGone()
                (childFragmentManager.findFragmentByTag("notifications_fragment") as DashboardNotificationsFragment).refresh()
            }
        }

        // Set up RecyclerView padding
        val padding = resources.getDimensionPixelSize(R.dimen.courseListPadding)
        recyclerView.setPaddingRelative(padding, padding, padding, padding)
        recyclerView.clipToPadding = false

        emptyCoursesView.onClickAddCourses {
            if (!APIHelper.hasNetworkConnection()) {
                toast(R.string.notAvailableOffline)
            } else {
                RouteMatcher.route(requireContext(), EditDashboardFragment.makeRoute())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(somethingChangedReceiver, IntentFilter(Const.COURSE_THING_CHANGED))
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(somethingChangedReceiver)
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun onShowGradesToggled(event: ShowGradesToggledEvent) {
        recyclerAdapter?.notifyDataSetChanged()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(sticky = true)
    fun onColorOverlayToggled(event: CourseColorOverlayToggledEvent) {
        recyclerAdapter?.notifyDataSetChanged()
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe
    fun onCoreDataLoaded(event: CoreDataFinishedLoading) {
        applyTheme()
    }

    override fun onDestroy() {
        recyclerAdapter?.cancel()
        super.onDestroy()
    }

    companion object {
        fun newInstance(route: Route) =
                DashboardFragment().apply {
                    arguments = route.canvasContext?.makeBundle(route.arguments) ?: route.arguments
                }

        fun makeRoute(canvasContext: CanvasContext?) = Route(DashboardFragment::class.java, canvasContext)
    }
}
