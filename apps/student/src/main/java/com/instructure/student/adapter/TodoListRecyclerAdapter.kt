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

package com.instructure.student.adapter

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.student.R
import com.instructure.student.fragment.FavoritedCourses
import com.instructure.student.fragment.FilterMode
import com.instructure.student.fragment.NoFilter
import com.instructure.student.holders.ExpandableViewHolder
import com.instructure.student.holders.TodoViewHolder
import com.instructure.student.interfaces.NotificationAdapterToFragmentCallback
import org.threeten.bp.LocalDate
import java.util.Date

open class TodoListRecyclerAdapter : ExpandableRecyclerAdapter<Date, PlannerItem, RecyclerView.ViewHolder> {

    private var adapterToFragmentCallback: NotificationAdapterToFragmentCallback<PlannerItem>? =
        null
    private var todoCheckboxCallback: TodoCheckboxCallback? = null

    private var loadJob: WeaveJob? = null
    private var canvasContext: CanvasContext? = null

    private val checkedTodos = ArrayList<PlannerItem>()
    private val deletedTodos = ArrayList<PlannerItem>()

    private var isEditMode: Boolean = false
    private var isNoNetwork: Boolean =
        false // With multiple callbacks, some could fail while others don't. This manages when to display no connection when offline
    private var filterMode: FilterMode = NoFilter

    private lateinit var plannerApi: PlannerAPI.PlannerInterface

    // region Interfaces
    interface TodoCheckboxCallback {
        val isEditMode: Boolean
        fun onCheckChanged(todo: PlannerItem, isChecked: Boolean, position: Int)
    }

    // endregion

    /* For testing purposes only */
    protected constructor(context: Context) : super(
        context,
        Date::class.java,
        PlannerItem::class.java
    )

    constructor(
        context: Context,
        canvasContext: CanvasContext,
        adapterToFragmentCallback: NotificationAdapterToFragmentCallback<PlannerItem>,
        plannerApi: PlannerAPI.PlannerInterface
    ) : super(context, Date::class.java, PlannerItem::class.java) {
        this.canvasContext = canvasContext
        this.adapterToFragmentCallback = adapterToFragmentCallback
        isEditMode = false
        isExpandedByDefault = true
        this.plannerApi = plannerApi
        loadData()
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Types.TYPE_HEADER -> ExpandableViewHolder(v)
            else -> TodoViewHolder(v)
        }
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return when (viewType) {
            Types.TYPE_HEADER -> ExpandableViewHolder.HOLDER_RES_ID
            else -> TodoViewHolder.HOLDER_RES_ID
        }
    }

    override fun onBindChildHolder(
        holder: RecyclerView.ViewHolder,
        date: Date,
        plannerItem: PlannerItem
    ) {
        (holder as TodoViewHolder).bind(
            context,
            plannerItem,
            adapterToFragmentCallback,
            todoCheckboxCallback!!
        )
    }

    override fun onBindHeaderHolder(
        holder: RecyclerView.ViewHolder,
        date: Date,
        isExpanded: Boolean
    ) {
        // If the to do doesn't have a due date (like if you're a teacher grading an assignment) the date that is set for the
        // header is a cleaned up version of a new date with the value of Long.MAX_VALUE
        val defaultDate = DateHelper.getCleanDate(Date(java.lang.Long.MAX_VALUE).time)

        val displayDate = if (date == defaultDate) {
            context.getString(R.string.toDoNoDueDate)
        } else {
            DateHelper.getFormattedDate(context, date)
        }

        (holder as ExpandableViewHolder).bind(
            context,
            date,
            displayDate,
            isExpanded,
            viewHolderHeaderClicked
        )
    }

    fun getFilterMode(): FilterMode {
        return filterMode
    }

    fun loadDataWithFilter(mode: FilterMode) {
        if (filterMode == mode) {
            return
        }

        filterMode = mode

        // Don't do a refresh so we can get the empty view loading indicator
        resetData()
        loadData()
    }

    final override fun loadData() {
        loadJob = tryWeave {
            val courses = CourseManager.getCoursesAsync(isRefresh)
            val restParams =
                RestParams(isForceReadFromNetwork = isRefresh, usePerPageQueryParam = true)
            val plannerItems = plannerApi.getPlannerItems(
                startDate = LocalDate.now().minusDays(28).toApiString(),
                endDate = LocalDate.now().plusDays(28).toApiString(),
                contextCodes = emptyList(),
                restParams = restParams
            ).depaginate { nextUrl ->
                plannerApi.nextPagePlannerItems(nextUrl, restParams)
            }

            val filteredCourses = courses.await().dataOrThrow.filter {
                !it.accessRestrictedByDate && !it.isInvited() && (when (filterMode) {
                    is FavoritedCourses -> it.isFavorite
                    else -> true
                })
            }
            val courseMap = CourseManager.createCourseMap(filteredCourses)

            val plannerList = plannerItems.dataOrThrow
                .filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }

            populateAdapter(courseMap, plannerList)
        } catch {
            it.printStackTrace()
        }
    }

    override fun onCallbackFinished(type: ApiType?) {
        // Workaround for the multiple callbacks, some will succeed while others don't
        isLoadedFirstPage = true
        shouldShowLoadingFooter()
        if (!isNoNetwork) {
            adapterToRecyclerViewCallback?.setDisplayNoConnection(false)
            adapterToRecyclerViewCallback?.setIsEmpty(isAllPagesLoaded && size() == 0)
        }
    }

    override fun onNoNetwork() {
        super.onNoNetwork()
        isNoNetwork = true
    }

    override fun refresh() {
        isNoNetwork = false
        adapterToRecyclerViewCallback?.setDisplayNoConnection(false)
        super.refresh()
    }

    private fun populateAdapter(courseMap: Map<Long, Course>, plannerList: List<PlannerItem>) {
        val todos = plannerList
            .filter { it.plannerOverride?.markedComplete != true }
            .filter { shownByFilter(it, courseMap) }
            .filter { !isComplete(it) }
            .sortedBy { it.comparisonDate }

        todos.forEach {
            addOrUpdateItem(DateHelper.getCleanDate(it.comparisonDate.time), it)
        }
        adapterToFragmentCallback?.onRefreshFinished()
        isAllPagesLoaded = true

        onCallbackFinished(ApiType.API)
    }

    private fun shownByFilter(plannerItem: PlannerItem, courseMap: Map<Long, Course>): Boolean {
        return when (filterMode) {
            is NoFilter -> true
            is FavoritedCourses -> plannerItem.courseId?.let { courseId -> courseMap.get(courseId)?.isFavorite == true }
                ?: false
        }
    }

    private fun isComplete(plannerItem: PlannerItem): Boolean {
        return if (plannerItem.plannableType == PlannableType.ASSIGNMENT
            || plannerItem.plannableType == PlannableType.DISCUSSION_TOPIC
            || plannerItem.plannableType == PlannableType.SUB_ASSIGNMENT
        ) {
            plannerItem.submissionState?.submitted == true
        } else {
            false
        }
    }

    override fun setupCallbacks() {
        todoCheckboxCallback = object : TodoCheckboxCallback {

            override val isEditMode: Boolean
                get() = this@TodoListRecyclerAdapter.isEditMode

            override fun onCheckChanged(todo: PlannerItem, isChecked: Boolean, position: Int) {
                // We don't want to let them try to delete things while offline because they
                // won't actually delete them from the server
                if (!APIHelper.hasNetworkConnection()) {
                    Toast.makeText(context, context.getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
                    return
                }

                todo.isChecked = isChecked
                if (isChecked && !deletedTodos.contains(todo)) {
                    checkedTodos.add(todo)
                } else {
                    checkedTodos.remove(todo)
                }

                // If we aren't in the edit mode, enable edit mode for future clicks

                if (!this@TodoListRecyclerAdapter.isEditMode) {
                    this@TodoListRecyclerAdapter.isEditMode = true
                } else if (checkedTodos.size == 0) { // If this was the last item, cancel
                    this@TodoListRecyclerAdapter.isEditMode = false
                }

                adapterToFragmentCallback?.onShowEditView(checkedTodos.size > 0)
                notifyItemChanged(position)
            }
        }
    }

    // endregion

    // region Data

    fun confirmButtonClicked() {
        checkedTodos.forEach {
            hideTodoItem(it)
            deletedTodos.add(it)
        }
        isEditMode = false
        clearMarked()
    }

    fun cancelButtonClicked() {
        checkedTodos.forEach { it.isChecked = false }
        isEditMode = false
        clearMarked()
        notifyDataSetChanged()
    }

    private fun clearMarked() {
        checkedTodos.clear()
        adapterToFragmentCallback?.onShowEditView(checkedTodos.size > 0)
    }

    private fun hideTodoItem(plannerItem: PlannerItem) {
        tryWeave {
            if (plannerItem.plannerOverride == null) {
                val override = PlannerOverride(
                    plannableId = plannerItem.plannable.id,
                    plannableType = plannerItem.plannableType,
                    markedComplete = true
                )
                plannerApi.createPlannerOverride(override, RestParams(isForceReadFromNetwork = true))
            } else {
                plannerApi.updatePlannerOverride(
                    plannerOverrideId = plannerItem.plannerOverride?.id ?: return@tryWeave,
                    complete = true,
                    params = RestParams(isForceReadFromNetwork = true)
                )
            }
            removeItem(plannerItem)
            adapterToRecyclerViewCallback?.setIsEmpty(size() == 0)
        } catch {
            deletedTodos.remove(plannerItem)
            it.printStackTrace()
        }
    }

    // endregion

    override fun cancel() {
        loadJob?.cancel()
    }

    // region Expandable Callbacks

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<Date> {
        return object : GroupSortedList.GroupComparatorCallback<Date> {
            override fun compare(o1: Date, o2: Date): Int {
                return o1.compareTo(o2)
            }

            override fun areContentsTheSame(oldGroup: Date, newGroup: Date): Boolean { return oldGroup == newGroup }
            override fun areItemsTheSame(group1: Date, group2: Date): Boolean { return group1.time == group2.time }
            override fun getUniqueGroupId(group: Date): Long { return group.time }

            override fun getGroupType(group: Date): Int {
                return Types.TYPE_HEADER
            }
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<Date, PlannerItem> {
        return object : GroupSortedList.ItemComparatorCallback<Date, PlannerItem> {
            override fun areItemsTheSame(item1: PlannerItem, item2: PlannerItem): Boolean { return item1.id == item2.id }
            override fun getUniqueItemId(item: PlannerItem): Long { return item.id }
            override fun getChildType(group: Date, item: PlannerItem): Int { return Types.TYPE_ITEM }
            override fun compare(group: Date, o1: PlannerItem, o2: PlannerItem): Int { return o1.compareTo(o2) }
            override fun areContentsTheSame(oldItem: PlannerItem, newItem: PlannerItem): Boolean {
                return oldItem.plannable.title == newItem.plannable.title // cache is not used
            }
        }
    }

    // endregion
}
