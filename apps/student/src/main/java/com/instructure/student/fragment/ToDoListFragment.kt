/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckedTextView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_TO_DO_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.TodoListRecyclerAdapter
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_list_todo.*
import kotlinx.android.synthetic.main.fragment_list_todo.view.*
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.*

@ScreenView(SCREEN_VIEW_TO_DO_LIST)
@PageView
class ToDoListFragment : ParentFragment() {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private lateinit var recyclerAdapter: TodoListRecyclerAdapter

    private var adapterToFragmentCallback: NotificationAdapterToFragmentCallback<ToDo> = object : NotificationAdapterToFragmentCallback<ToDo> {
        override fun onRowClicked(todo: ToDo, position: Int, isOpenDetail: Boolean) {
            recyclerAdapter.setSelectedPosition(position)
            onRowClick(todo)
        }

        override fun onRefreshFinished() {
            if (!isAdded) return
            setRefreshing(false)
            editOptions.setGone()
            if (recyclerAdapter.size() == 0) {
                setEmptyView(emptyView, R.drawable.ic_panda_sleeping, R.string.noTodos, R.string.noTodosSubtext)
            }
        }

        override fun onShowEditView(isVisible: Boolean) {
            editOptions.setVisible(isVisible)
        }

        override fun onShowErrorCrouton(message: Int) = Unit
    }

    override fun title(): String = getString(R.string.Todo)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_list_todo, container, false)
        with (rootView.toolbar) {
            inflateMenu(R.menu.fragment_list_todo)
            menu.findItem(R.id.todoListFilter).setOnMenuItemClickListener {
                showCourseFilterDialog()
                true
            }
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerAdapter = TodoListRecyclerAdapter(requireContext(), canvasContext, adapterToFragmentCallback)
        configureRecyclerView(
            view,
            requireContext(),
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )

        listView.isSelectionEnabled = false

        confirmButton.text = getString(R.string.markAsDone)
        confirmButton.setOnClickListener { recyclerAdapter.confirmButtonClicked() }

        cancelButton.setText(R.string.cancel)
        cancelButton.setOnClickListener { recyclerAdapter.cancelButtonClicked() }

        updateFilterTitle(recyclerAdapter.getFilterMode())
        clearFilterTextView.setOnClickListener {
            recyclerAdapter.loadDataWithFilter(NoFilter)
            updateFilterTitle(recyclerAdapter.getFilterMode())
        }
    }

    private fun updateFilterTitle(filterMode: FilterMode) {
        clearFilterTextView.setVisible(filterMode != NoFilter)
        todoFilterTitle.setText(filterMode.titleId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.title = title()
        navigation?.attachNavigationDrawer(this, toolbar)
    }

    override fun applyTheme() {
        setupToolbarMenu(toolbar)
        ViewStyler.themeToolbar(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            requireView(),
            requireContext(),
            recyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView,
                R.string.noTodos
        )
        if (recyclerAdapter.size() == 0) {
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
                    emptyView.setGuidelines(.2f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    private fun onRowClick(toDo: ToDo?) {
        when {
            toDo?.assignment != null -> { // Launch assignment details fragment.
                if (toDo.assignment!!.discussionTopicHeader != null) {
                    val groupTopic = toDo.assignment!!.discussionTopicHeader!!.groupTopicChildren.firstOrNull()
                    if (groupTopic == null) { // Launch discussion details fragment
                        RouteMatcher.route(requireContext(), DiscussionDetailsFragment.makeRoute(toDo.canvasContext!!, toDo.assignment!!.discussionTopicHeader!!))
                    } else { // Launch discussion details fragment with the group
                        RouteMatcher.route(requireContext(), DiscussionDetailsFragment.makeRoute(CanvasContext.emptyGroupContext(groupTopic.groupId), groupTopic.id))
                    }
                } else {
                    // Launch assignment details fragment.
                    RouteMatcher.route(requireContext(), AssignmentDetailsFragment.makeRoute(toDo.canvasContext!!, toDo.assignment!!.id))
                }
            }
            toDo?.scheduleItem != null -> // It's a Calendar event from the Upcoming API.
                RouteMatcher.route(requireContext(), CalendarEventFragment.makeRoute(toDo.canvasContext!!, toDo.scheduleItem!!))
            toDo?.quiz != null -> // It's a Quiz let's launch the quiz details fragment
                RouteMatcher.route(requireContext(), BasicQuizViewFragment.makeRoute(toDo.canvasContext!!, toDo.quiz!!, toDo.quiz!!.url!!))
        }
    }

    private fun showCourseFilterDialog() {
        val choices = arrayOf(getString(R.string.favoritedCoursesLabel))
        var checkedItem = choices.indexOf(getString(recyclerAdapter.getFilterMode().titleId))

        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.filterByEllipsis)
                .setSingleChoiceItems(choices, checkedItem) { _, index ->
                    checkedItem = index
                }.setPositiveButton(android.R.string.ok) { _, _ ->
                    if (checkedItem >= 0) recyclerAdapter.loadDataWithFilter(convertFilterChoiceToMode(choices[checkedItem]))
                    updateFilterTitle(recyclerAdapter.getFilterMode())
                }.setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.listView.children<AppCompatCheckedTextView>().forEach { checkbox ->
                checkbox.compoundDrawableTintList = ColorStateList.valueOf(ThemePrefs.brandColor)
            }
        }

        dialog.show()
    }

    private fun convertFilterChoiceToMode(filter: String) : FilterMode {
        return when (filter) {
            getString(FavoritedCourses.titleId) -> FavoritedCourses
            else -> NoFilter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerAdapter.cancel()
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext): Route = Route(ToDoListFragment::class.java, canvasContext, Bundle())

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): ToDoListFragment? {
            if (!validateRoute(route)) return null
            return ToDoListFragment().withArgs(route.canvasContext!!.makeBundle())
        }

    }
}

sealed class FilterMode(val titleId: Int)
object FavoritedCourses : FilterMode(R.string.favoritedCoursesLabel)
object NoFilter : FilterMode(R.string.allCourses)

