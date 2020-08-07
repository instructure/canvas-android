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
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import com.instructure.student.holders.AssignmentViewHolder
import com.instructure.student.holders.EmptyViewHolder
import com.instructure.student.holders.ExpandableViewHolder
import com.instructure.student.interfaces.AdapterToAssignmentsCallback
import com.instructure.student.interfaces.GradingPeriodsCallback
import retrofit2.Call
import retrofit2.Response
import java.util.*

open class AssignmentDateListRecyclerAdapter(
    context: Context,
    private val canvasContext: CanvasContext,
    private val adapterToAssignmentsCallback: AdapterToAssignmentsCallback,
    isTesting: Boolean = false
) : ExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder>(
    context,
    AssignmentGroup::class.java,
    Assignment::class.java
), GradingPeriodsCallback {

    private val overdue: AssignmentGroup
    private val upcoming: AssignmentGroup
    private val undated: AssignmentGroup
    private val past: AssignmentGroup
    private var assignmentGroupCallback: StatusCallback<List<AssignmentGroup>>? = null
    override var currentGradingPeriod: GradingPeriod? = null
    private var apiJob: WeaveJob? = null
    private var assignmentGroups: List<AssignmentGroup> = emptyList()

    var searchQuery: String = ""
        set(value) {
            field = value
            if (isAllPagesLoaded) {
                clear()
                populateData()
                onCallbackFinished(ApiType.CACHE)
            }
        }

    init {
        overdue = AssignmentGroup(name = context.getString(R.string.overdueAssignments), position = HEADER_POSITION_OVERDUE)
        upcoming = AssignmentGroup(name = context.getString(R.string.upcomingAssignments), position = HEADER_POSITION_UPCOMING)
        undated = AssignmentGroup(name = context.getString(R.string.undatedAssignments), position = HEADER_POSITION_UNDATED)
        past = AssignmentGroup(name = context.getString(R.string.pastAssignments), position = HEADER_POSITION_PAST)
        isExpandedByDefault = true
        isDisplayEmptyCell = true
        if (!isTesting) loadData()
    }

    override fun setupCallbacks() {
        assignmentGroupCallback = object : StatusCallback<List<AssignmentGroup>>() {

            override fun onResponse(response: Response<List<AssignmentGroup>>, linkHeaders: LinkHeaders, type: ApiType) {
                assignmentGroups = response.body()!!
                populateData()
                adapterToAssignmentsCallback.onRefreshFinished()
                adapterToAssignmentsCallback.setTermSpinnerState(true)
            }

            override fun onFail(call: Call<List<AssignmentGroup>>?, error: Throwable, response: Response<*>?) {
                adapterToAssignmentsCallback.setTermSpinnerState(true)
            }

            override fun onFinished(type: ApiType) {
                this@AssignmentDateListRecyclerAdapter.onCallbackFinished(type)
            }
        }


    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Types.TYPE_HEADER -> ExpandableViewHolder(v)
            Types.TYPE_EMPTY_CELL -> EmptyViewHolder(v)
            else -> AssignmentViewHolder(v)
        }
    }

    override fun itemLayoutResId(viewType: Int): Int {
        return when (viewType) {
            Types.TYPE_HEADER -> ExpandableViewHolder.HOLDER_RES_ID
            Types.TYPE_EMPTY_CELL -> EmptyViewHolder.HOLDER_RES_ID
            else -> AssignmentViewHolder.HOLDER_RES_ID
        }
    }

    final override fun loadData() {
        /*Logic regarding MGP is similar here as it is in both assignment recycler adapters,
            if changes are made here, check if they are needed in the other recycler adapters.*/
        val course = canvasContext as Course

        //This check is for the "all grading periods" option
        if (currentGradingPeriod != null && currentGradingPeriod!!.title != null
            && currentGradingPeriod!!.title == context.getString(R.string.allGradingPeriods)) {
            loadAssignment()
            return
        }

        for (enrollment in course.enrollments!!) {
            //Date list is for students
            if (enrollment.multipleGradingPeriodsEnabled) {
                if (currentGradingPeriod == null || currentGradingPeriod!!.title == null) {
                    //we load current term by setting up the current GP
                    currentGradingPeriod = GradingPeriod()
                    currentGradingPeriod!!.id = enrollment.currentGradingPeriodId
                    currentGradingPeriod!!.title = enrollment.currentGradingPeriodTitle
                    //request the grading period objects and make the assignment calls
                    fetchGradingPeriods(course.id)
                    //Then we go ahead and load up the assignments for the current period
                    loadAssignmentsForGradingPeriod(currentGradingPeriod!!.id, false)
                    return
                } else {
                    //Otherwise we load the info from the currently selected grading period
                    loadAssignmentsForGradingPeriod(currentGradingPeriod!!.id, true)
                    return
                }
            }
        }
        //If we made it this far, MGP is disabled so we just go forward with the standard
        loadAssignment()
    }

    private fun fetchGradingPeriods(courseId: Long) {
        apiJob = tryWeave {
            val periods = awaitApi<GradingPeriodResponse> {
                CourseManager.getGradingPeriodsForCourse(it, courseId, isRefresh)
            }.gradingPeriodList
            adapterToAssignmentsCallback.gradingPeriodsFetched(periods)
        } catch {
            Logger.w("Unable to fetch grading periods")
            it.printStackTrace()
        }
    }

    override fun contextReady() {}

    override fun isPaginated() = false

    override fun onBindChildHolder(
        holder: RecyclerView.ViewHolder,
        assignmentGroup: AssignmentGroup,
        assignment: Assignment
    ) {
        (holder as AssignmentViewHolder).bind(context, assignment, canvasContext.color, adapterToAssignmentsCallback)
    }

    override fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, assignmentGroup: AssignmentGroup) {
        (holder as EmptyViewHolder).bind(context.resources.getString(R.string.noAssignmentsInGroup))
    }

    override fun onBindHeaderHolder(
        holder: RecyclerView.ViewHolder,
        assignmentGroup: AssignmentGroup,
        isExpanded: Boolean
    ) {
        (holder as ExpandableViewHolder).bind(
            context,
            assignmentGroup,
            assignmentGroup.name ?: "",
            isExpanded,
            viewHolderHeaderClicked
        )
    }

    override fun loadAssignmentsForGradingPeriod(gradingPeriodID: Long, refreshFirst: Boolean) {
        /*Logic regarding MGP is similar here as it is in both assignment recycler adapters,
            if changes are made here, check if they are needed in the other recycler adapters.*/
        if (refreshFirst) resetData()

        // Scope assignments if its for a student
        val scopeToStudent = (canvasContext as Course).isStudent
        AssignmentManager.getAssignmentGroupsWithAssignmentsForGradingPeriod(
            canvasContext.id,
            gradingPeriodID,
            scopeToStudent,
            isRefresh,
            assignmentGroupCallback!!
        )
    }

    override fun loadAssignment() {
        AssignmentManager.getAssignmentGroupsWithAssignments(canvasContext.id, isRefresh, assignmentGroupCallback!!)
    }

    private fun populateData() {
        val today = Date()
        for (assignmentGroup in assignmentGroups) {
            // TODO canHaveOverDueAssignment
            // web does it like this
            // # only handles observer observing one student, this needs to change to handle multiple users in the future
            // canHaveOverdueAssignment = !ENV.current_user_has_been_observer_in_this_course || ENV.observed_student_ids?.length == 1I
            // endtodo
            assignmentGroup.assignments
                .filterWithQuery(searchQuery, Assignment::name)
                .forEach { assignment ->
                    val dueAt = assignment.dueAt
                    val submission = assignment.submission
                    assignment.submission = submission
                    val isWithoutGradedSubmission = submission == null || submission.isWithoutGradedSubmission
                    val isOverdue = assignment.isAllowedToSubmit && isWithoutGradedSubmission
                    if (dueAt == null) {
                        addOrUpdateItem(undated, assignment)
                    } else {
                        when {
                            today.before(dueAt.toDate()) -> addOrUpdateItem(upcoming, assignment)
                            isOverdue -> addOrUpdateItem(overdue, assignment)
                            else -> addOrUpdateItem(past, assignment)
                        }
                    }
                }
        }
        isAllPagesLoaded = true
    }

    // region Expandable callbacks

    override fun createGroupCallback() = object : GroupSortedList.GroupComparatorCallback<AssignmentGroup> {
        override fun compare(o1: AssignmentGroup, o2: AssignmentGroup) = o1.position - o2.position
        override fun areContentsTheSame(old: AssignmentGroup, new: AssignmentGroup) = old.name == new.name
        override fun areItemsTheSame(group1: AssignmentGroup, group2: AssignmentGroup) = group1.position == group2.position
        override fun getGroupType(group: AssignmentGroup) = Types.TYPE_HEADER
        override fun getUniqueGroupId(group: AssignmentGroup) = group.position.toLong()
    }

    override fun createItemCallback() = itemCallback

    // endregion

    override fun cancel() {
        super.cancel()
        apiJob?.cancel()
    }

    companion object {
        const val HEADER_POSITION_OVERDUE = 0
        const val HEADER_POSITION_UPCOMING = 1
        const val HEADER_POSITION_UNDATED = 2
        const val HEADER_POSITION_PAST = 3

        // Decoupled for testability
        val itemCallback = object : GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> {
            private val sameCheck = compareBy<Assignment>({ it.dueAt }, { it.name })
            override fun areContentsTheSame(old: Assignment, new: Assignment) = sameCheck.compare(old, new) == 0
            override fun areItemsTheSame(item1: Assignment, item2: Assignment) = item1.id == item2.id
            override fun getChildType(group: AssignmentGroup, item: Assignment) = Types.TYPE_ITEM
            override fun getUniqueItemId(item: Assignment) = item.id
            override fun compare(group: AssignmentGroup, o1: Assignment, o2: Assignment): Int {
                val position = group.position
                return when (position) {
                    HEADER_POSITION_UNDATED -> o1.name?.toLowerCase()?.compareTo(o2.name?.toLowerCase() ?: "") ?: 0
                    HEADER_POSITION_PAST -> o2.dueAt?.compareTo(o1.dueAt ?: "") ?: 0 // Sort newest date first (o1 and o2 switched places)
                    else -> o1.dueAt?.compareTo(o2.dueAt ?: "") ?: 0 // Sort oldest date first
                }
            }
        }
    }

}
