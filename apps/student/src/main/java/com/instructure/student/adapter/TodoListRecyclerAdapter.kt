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
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.ToDoManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.toApiString
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
import retrofit2.Call
import retrofit2.Response
import java.util.*

open class TodoListRecyclerAdapter : ExpandableRecyclerAdapter<Date, PlannerItem, RecyclerView.ViewHolder> {

    private var adapterToFragmentCallback: NotificationAdapterToFragmentCallback<PlannerItem>? = null
    private var todoCheckboxCallback: TodoCheckboxCallback? = null

    private var courseMap: Map<Long, Course>? = null
    private var groupMap: Map<Long, Group>? = null
    private var todoList: List<ToDo>? = null
    private var plannerList: List<PlannerItem>? = null

    private var todoCallback: StatusCallback<List<ToDo>>? = null
    private var coursesCallback: StatusCallback<List<Course>>? = null
    private var groupsCallback: StatusCallback<List<Group>>? = null
    private var plannerCallback: StatusCallback<List<PlannerItem>>? = null
    private var canvasContext: CanvasContext? = null

    private val checkedTodos = HashSet<PlannerItem>()
    private val deletedTodos = HashSet<PlannerItem>()

    private var isEditMode: Boolean = false
    private var isNoNetwork: Boolean = false // With multiple callbacks, some could fail while others don't. This manages when to display no connection when offline
    private var filterMode: FilterMode = NoFilter

    private lateinit var plannerManager: PlannerManager

    // region Interfaces
    interface TodoCheckboxCallback {
        val isEditMode: Boolean
        fun onCheckChanged(todo: PlannerItem, isChecked: Boolean, position: Int)
    }

    // endregion

    /* For testing purposes only */
    protected constructor(context: Context) : super(context, Date::class.java, PlannerItem::class.java)

    constructor(
        context: Context,
        canvasContext: CanvasContext,
        adapterToFragmentCallback: NotificationAdapterToFragmentCallback<PlannerItem>,
        plannerManager: PlannerManager
    ) : super(context, Date::class.java, PlannerItem::class.java) {
        this.canvasContext = canvasContext
        this.adapterToFragmentCallback = adapterToFragmentCallback
        isEditMode = false
        isExpandedByDefault = true
        this.plannerManager = plannerManager
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

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, date: Date, plannerItem: PlannerItem) {
        (holder as TodoViewHolder).bind(context, plannerItem, adapterToFragmentCallback, todoCheckboxCallback!!)
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, date: Date, isExpanded: Boolean) {
        // If the to do doesn't have a due date (like if you're a teacher grading an assignment) the date that is set for the
        // header is a cleaned up version of a new date with the value of Long.MAX_VALUE
        val defaultDate = DateHelper.getCleanDate(Date(java.lang.Long.MAX_VALUE).time)

        val displayDate = if (date == defaultDate) {
            context.getString(R.string.toDoNoDueDate)
        } else {
            DateHelper.getFormattedDate(context, date)
        }

        (holder as ExpandableViewHolder).bind(context, date, displayDate, isExpanded, viewHolderHeaderClicked)
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
        CourseManager.getCourses(true, coursesCallback!!)
        GroupManager.getAllGroups(groupsCallback!!, true)
        ToDoManager.getTodosWithUngradedQuizzes(todoCallback!!, isRefresh)
        plannerManager.getPlannerItems(isRefresh, plannerCallback!!, LocalDate.now().toApiString())
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

    private fun populateAdapter() {
        if (todoList == null || courseMap == null || groupMap == null || plannerList == null) {
            return
        }

        /*val todos = todoList?.sortedBy { it.comparisonDate ?: Date(0) } ?: emptyList()

        // Now populate the todoList and upcomingList with the course information
        todos
            .filter { it.todoType != ToDo.Type.Grading }
            .forEach {
            ToDo.setContextInfo(it, courseMap!!, groupMap!!)
            if (it.canvasContext != null && it.comparisonDate != null) {
                addOrUpdateItem(DateHelper.getCleanDate(it.comparisonDate!!.time), it)
            }
        }*/
        val todos = plannerList?.sortedBy { it.comparisonDate ?: Date(0) } ?: emptyList()

        todos.forEach {
            addOrUpdateItem(DateHelper.getCleanDate(it.comparisonDate.time), it)
        }
        adapterToFragmentCallback?.onRefreshFinished()
        isAllPagesLoaded = true

        // Reset the lists
        todoList = null
        courseMap = null
        groupMap = null

        onCallbackFinished(ApiType.API)
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

        coursesCallback = object : StatusCallback<List<Course>>() {
            override fun onResponse(response: Response<List<Course>>, linkHeaders: LinkHeaders, type: ApiType) {
                val body = response.body() ?: return
                val filteredCourses = body.filter {
                    !it.accessRestrictedByDate && !it.isInvited() && (when (filterMode) {
                                is FavoritedCourses -> it.isFavorite
                                else -> true
                            })
                }
                courseMap = CourseManager.createCourseMap(filteredCourses)
                populateAdapter()
            }
        }

        groupsCallback = object : StatusCallback<List<Group>>() {
            override fun onResponse(response: Response<List<Group>>, linkHeaders: LinkHeaders, type: ApiType) {
                groupMap = response.body()?.let { GroupManager.createGroupMap(it) }
                populateAdapter()
            }
        }

        todoCallback = object : StatusCallback<List<ToDo>>() {
            override fun onResponse(response: Response<List<ToDo>>, linkHeaders: LinkHeaders, type: ApiType) {
                todoList = response.body()
                populateAdapter()

                // Remove the todos that have been deleted
                deletedTodos.forEach { removeItem(it) }
            }
        }

        plannerCallback = object : StatusCallback<List<PlannerItem>>() {
            override fun onResponse(
                response: Response<List<PlannerItem>>,
                linkHeaders: LinkHeaders,
                type: ApiType
            ) {
                plannerList = response.body()
                    ?.filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }
                    ?: return
                populateAdapter()
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
        plannerManager.updatePlannerOverride(
            forceNetwork = true,
            overrideId = plannerItem.plannerOverride?.id ?: return,
            completed = true,
            callback = object : StatusCallback<PlannerOverride>() {
                override fun onResponse(
                    response: Response<PlannerOverride>,
                    linkHeaders: LinkHeaders,
                    type: ApiType
                ) {
                    removeItem(plannerItem)
                    adapterToRecyclerViewCallback?.setIsEmpty(size() == 0)
                }

                override fun onFail(call: Call<PlannerOverride>?, error: Throwable, response: Response<*>?) {
                    // If the update fails, we don't want to remove it from the list
                    deletedTodos.remove(plannerItem)
                }
            }
        )

        /*if (todo.ignore == null) {
            // No Url to hit to remove this to-do so it's probably dead and gone by now
            removeItem(todo)
        } else {
            ToDoManager.dismissTodo(todo, object : StatusCallback<Void>() {
                override fun onResponse(response: Response<Void>, linkHeaders: LinkHeaders, type: ApiType) {
                    removeItem(todo)
                    adapterToRecyclerViewCallback?.setIsEmpty(size() == 0)
                }

                override fun onFail(call: Call<Void>?, error: Throwable, response: Response<*>?) {
                    deletedTodos.remove(todo)
                }
            })
        }*/
    }

    // endregion

    override fun cancel() {
        coursesCallback?.cancel()
        groupsCallback?.cancel()
        todoCallback?.cancel()
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
