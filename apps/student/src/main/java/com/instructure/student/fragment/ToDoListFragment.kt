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
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckedTextView
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_TO_DO_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.accessibilityClassName
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.adapter.TodoListRecyclerAdapter
import com.instructure.student.databinding.FragmentListTodoBinding
import com.instructure.student.databinding.PandaRecyclerRefreshLayoutBinding
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_TO_DO_LIST)
@PageView
@AndroidEntryPoint
class ToDoListFragment : ParentFragment() {

    private val binding by viewBinding(FragmentListTodoBinding::bind)
    private lateinit var recyclerViewBinding: PandaRecyclerRefreshLayoutBinding

    @Inject
    lateinit var plannerManager: PlannerManager

    @Inject
    lateinit var calendarRouter: CalendarRouter

    @Inject
    lateinit var apiPrefs: ApiPrefs

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: TodoListRecyclerAdapter? = null

    private var adapterToFragmentCallback: NotificationAdapterToFragmentCallback<PlannerItem> = object : NotificationAdapterToFragmentCallback<PlannerItem> {
        override fun onRowClicked(todo: PlannerItem, position: Int, isOpenDetail: Boolean) {
            recyclerAdapter?.setSelectedPosition(position)
            onRowClick(todo)
        }

        override fun onRefreshFinished() {
            if (!isAdded) return
            setRefreshing(false)
            binding.editOptions.setGone()
            if (recyclerAdapter?.size() == 0) {
                setEmptyView(recyclerViewBinding.emptyView, R.drawable.ic_panda_sleeping, R.string.noTodos, R.string.noTodosSubtext)
            }
        }

        override fun onShowEditView(isVisible: Boolean) {
            binding.editOptions.setVisible(isVisible)
        }

        override fun onShowErrorCrouton(message: Int) = Unit
    }

    override fun title(): String = getString(R.string.Todo)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_list_todo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewBinding = PandaRecyclerRefreshLayoutBinding.bind(binding.root)
        with (binding.toolbar) {
            inflateMenu(R.menu.fragment_list_todo)
            menu.findItem(R.id.todoListFilter).setOnMenuItemClickListener {
                showCourseFilterDialog()
                true
            }
        }
        recyclerAdapter = TodoListRecyclerAdapter(requireContext(), canvasContext, adapterToFragmentCallback, plannerManager)
        recyclerAdapter?.let {
            configureRecyclerView(
                view,
                requireContext(),
                it,
                R.id.swipeRefreshLayout,
                R.id.emptyView,
                R.id.listView
            )
        }

        recyclerViewBinding.listView.isSelectionEnabled = false

        binding.confirmButton.text = getString(R.string.markAsDone)
        binding.confirmButton.setOnClickListener { recyclerAdapter?.confirmButtonClicked() }
        binding.cancelButton.setText(R.string.cancel)
        binding.cancelButton.setOnClickListener { recyclerAdapter?.cancelButtonClicked() }

        updateFilterTitle(recyclerAdapter?.getFilterMode() ?: NoFilter)
        binding.clearFilterTextView.setOnClickListener {
            recyclerAdapter?.loadDataWithFilter(NoFilter)
            updateFilterTitle(recyclerAdapter?.getFilterMode() ?: NoFilter)
        }
    }

    private fun updateFilterTitle(filterMode: FilterMode) {
        binding.clearFilterTextView.setVisible(filterMode != NoFilter)
        binding.todoFilterTitle.setText(filterMode.titleId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.title = title()
        navigation?.attachNavigationDrawer(this, binding.toolbar)
    }

    override fun applyTheme() {
        setupToolbarMenu(binding.toolbar)
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recyclerAdapter?.let {
            configureRecyclerView(
                requireView(),
                requireContext(),
                it,
                R.id.swipeRefreshLayout,
                R.id.emptyView,
                R.id.listView,
                R.string.noTodos
            )
        }
        if (recyclerAdapter?.size() == 0) {
            recyclerViewBinding.emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    recyclerViewBinding.emptyView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
                } else {
                    recyclerViewBinding.emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    recyclerViewBinding.emptyView.setGuidelines(.2f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    private fun onRowClick(toDo: PlannerItem) {
        when (toDo.plannableType) {
            PlannableType.ASSIGNMENT -> calendarRouter.openAssignment(toDo.canvasContext, toDo.plannable.id)
            PlannableType.SUB_ASSIGNMENT -> {
                val regex = """assignments/(\d+)""".toRegex()
                val matchResult = regex.find(toDo.htmlUrl.orEmpty())
                matchResult?.groupValues?.getOrNull(1)?.toLongOrNull()?.let {
                    calendarRouter.openAssignment(toDo.canvasContext, it)
                }
            }
            PlannableType.DISCUSSION_TOPIC -> calendarRouter.openDiscussion(toDo.canvasContext, toDo.plannable.id, toDo.plannable.assignmentId)
            PlannableType.QUIZ -> {
                var htmlUrl = toDo.htmlUrl.orEmpty()
                if (htmlUrl.startsWith('/')) htmlUrl = apiPrefs.fullDomain + htmlUrl
                calendarRouter.openQuiz(toDo.canvasContext, htmlUrl)
            }
            PlannableType.PLANNER_NOTE -> calendarRouter.openToDo(toDo)
            PlannableType.CALENDAR_EVENT -> calendarRouter.openCalendarEvent(toDo.canvasContext, toDo.plannable.id)
            else -> {}
        }
    }

    private fun showCourseFilterDialog() {
        val choices = arrayOf(getString(R.string.favoriteCoursesLabel))
        var checkedItem = choices.indexOf(getString(recyclerAdapter?.getFilterMode()?.titleId ?: NoFilter.titleId))

        val dialog = AlertDialog.Builder(requireContext(), R.style.AccessibleAccentDialogTheme)
                .setTitle(R.string.filterByEllipsis)
                .setSingleChoiceItems(choices, checkedItem) { _, index ->
                    checkedItem = index
                }.setPositiveButton(android.R.string.ok) { _, _ ->
                    if (checkedItem >= 0) recyclerAdapter?.loadDataWithFilter(convertFilterChoiceToMode(choices[checkedItem]))
                    updateFilterTitle(recyclerAdapter?.getFilterMode() ?: NoFilter)
                }.setNegativeButton(android.R.string.cancel, null)
                .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.listView.children<AppCompatCheckedTextView>().forEach { checkbox ->
                checkbox.compoundDrawableTintList = ColorStateList.valueOf(ThemePrefs.brandColor)
                checkbox.accessibilityClassName(RadioButton::class.java.name)
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
        recyclerAdapter?.cancel()
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
object FavoritedCourses : FilterMode(R.string.favoriteCoursesLabel)
object NoFilter : FilterMode(R.string.allCourses)

