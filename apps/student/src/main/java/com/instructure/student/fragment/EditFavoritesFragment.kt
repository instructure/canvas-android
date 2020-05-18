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

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.EditFavoritesRecyclerAdapter
import com.instructure.student.interfaces.AdapterToFragmentCallback
import kotlinx.android.synthetic.main.fragment_favoriting.*
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.*

@PageView(url = "courses")
class EditFavoritesFragment : ParentFragment() {

    private var recyclerAdapter: EditFavoritesRecyclerAdapter? = null
    private var hasChanges = false
    private var courseCall: WeaveJob? = null
    private var groupCall: WeaveJob? = null

    override fun title(): String = requireContext().getString(R.string.editDashboard)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTablet) setStyle(STYLE_NORMAL, R.style.LightStatusBarDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_favoriting, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        applyTheme()
        recyclerAdapter = EditFavoritesRecyclerAdapter(requireActivity(), object : AdapterToFragmentCallback<CanvasComparable<*>> {
            override fun onRefreshFinished() {
                setRefreshing(false)
                if (recyclerAdapter?.size() == 0) {
                    setEmptyView(emptyView, R.drawable.vd_panda_nocourses, R.string.noCourses, R.string.noCoursesSubtext)
                }
            }

            override fun onRowClicked(canvasContext: CanvasComparable<*>, position: Int, isOpenDetail: Boolean) {
                hasChanges = true
                when(canvasContext) {
                    is Course -> updateCourseFavorite(canvasContext)
                    is Group -> updateGroupFavorite(canvasContext)
                }
            }
        })
        configureRecyclerView(view!!, requireContext(), recyclerAdapter!!, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView, R.string.no_courses_available)
        listView.isSelectionEnabled = false

        // Disable item animator when TalkBack is enabled, otherwise a11y focus resets when a favorite is toggled
        if (requireContext().a11yManager.hasSpokenFeedback) listView.itemAnimator = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (recyclerAdapter!!.size() == 0) {
            emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    emptyView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
                } else {
                    emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    override fun applyTheme() {
        toolbar.setTitle(R.string.editDashboard)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, Color.WHITE, Color.BLACK, false)
    }

    private fun updateCourseFavorite(course: Course) {
        courseCall?.cancel()
        course.isFavorite = !course.isFavorite
        recyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.COURSE_HEADER,course)
        courseCall = tryWeave {
            awaitApi<Favorite> {
                if (course.isFavorite) CourseManager.addCourseToFavorites(course.id, it, true)
                else CourseManager.removeCourseFromFavorites(course.id, it, true)
            }
        } catch {
            course.isFavorite = !course.isFavorite
            recyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.COURSE_HEADER,course)
        }
    }

    private fun updateGroupFavorite(group: Group) {
        groupCall?.cancel()
        group.isFavorite = !group.isFavorite
        recyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.GROUP_HEADER,group)
        groupCall = tryWeave {
            awaitApi<Favorite> {
                if (group.isFavorite) GroupManager.addGroupToFavorites(group.id, it)
                else GroupManager.removeGroupFromFavorites(group.id, it)
            }
        } catch {
            group.isFavorite = !group.isFavorite
            recyclerAdapter?.addOrUpdateItem(EditFavoritesRecyclerAdapter.ItemType.GROUP_HEADER,group)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isTablet && dialog != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onStop() {
        super.onStop()
        courseCall?.cancel()
        groupCall?.cancel()
        recyclerAdapter?.cancel()
        if (hasChanges) {
            val intent = Intent(Const.COURSE_THING_CHANGED)
            intent.putExtras(Bundle().apply { putBoolean(Const.COURSE_FAVORITES, true) })
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }

    companion object {

        fun makeRoute() = Route(EditFavoritesFragment::class.java, null)

        fun validRoute(route: Route) = route.primaryClass == EditFavoritesFragment::class.java

        fun newInstance(route: Route): EditFavoritesFragment? {
            if (!validRoute(route)) return null
            return EditFavoritesFragment()
        }

    }
}
