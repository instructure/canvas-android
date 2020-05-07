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
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instructure.canvasapi2.managers.CourseNicknameManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.AllCoursesRecyclerAdapter
import com.instructure.student.dialog.ColorPickerDialog
import com.instructure.student.dialog.EditCourseNicknameDialog
import com.instructure.student.events.CourseColorOverlayToggledEvent
import com.instructure.student.events.ShowGradesToggledEvent
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_all_courses.*
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import kotlinx.android.synthetic.main.fragment_all_courses.allCoursesFragmentContainer as rootView
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.listView as recyclerView

@PageView(url = "courses")
class AllCoursesFragment : ParentFragment() {
    private var recyclerAdapter: AllCoursesRecyclerAdapter? = null

    private val somethingChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (recyclerAdapter != null && intent?.extras?.getBoolean(Const.COURSE_FAVORITES) == true) {
                swipeRefreshLayout?.isRefreshing = true
                recyclerAdapter?.refresh()
            }
        }
    }

    override fun title(): String = if (isAdded) getString(R.string.allCourses) else ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_all_courses, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerAdapter = AllCoursesRecyclerAdapter(requireActivity(), object : CourseAdapterToFragmentCallback {
            override fun onRemoveAnnouncement(announcement: AccountNotification, position: Int) = Unit
            override fun onHandleCourseInvitation(course: Course, accepted: Boolean) = Unit
            override fun onConferenceSelected(conference: Conference) = Unit
            override fun onDismissConference(conference: Conference) = Unit
            override fun onSeeAllCourses() = Unit
            override fun onGroupSelected(group: Group) = Unit

            override fun onRefreshFinished() {
                swipeRefreshLayout?.isRefreshing = false
            }

            override fun onCourseSelected(course: Course) {
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
                        recyclerAdapter?.add(course)
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
        toolbar.title = getString(R.string.allCourses)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView()
    }

    private fun configureRecyclerView() {
        val courseColumns = resources.getInteger(R.integer.course_card_columns)
        configureRecyclerViewAsGrid(view!!, recyclerAdapter!!, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView, R.string.no_courses_available, courseColumns)
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

    override fun onDestroy() {
        if (recyclerAdapter != null) recyclerAdapter?.cancel()
        super.onDestroy()
    }

    companion object {
        fun makeRoute() = Route(AllCoursesFragment::class.java, null)

        fun validRoute(route: Route) = route.primaryClass == AllCoursesFragment::class.java

        fun newInstance(route: Route): AllCoursesFragment? {
            if (!validRoute(route)) return null
            return AllCoursesFragment()
        }

    }
}
