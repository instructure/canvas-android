/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.features.assignments.list.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.student.R
import com.instructure.student.adapter.ExpandableRecyclerAdapter
import com.instructure.student.features.assignments.list.AssignmentListRepository
import com.instructure.student.holders.AssignmentViewHolder
import com.instructure.student.holders.EmptyViewHolder
import com.instructure.student.holders.ExpandableViewHolder
import com.instructure.student.interfaces.AdapterToAssignmentsCallback
import com.instructure.student.interfaces.GradingPeriodsCallback

abstract class AssignmentListRecyclerAdapter(
    context: Context,
    private val canvasContext: CanvasContext,
    private val adapterToAssignmentsCallback: AdapterToAssignmentsCallback,
    isTesting: Boolean = false,
    filter: AssignmentListFilter = AssignmentListFilter.ALL,
    private val repository: AssignmentListRepository
) : ExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder>(
    context,
    AssignmentGroup::class.java,
    Assignment::class.java
), GradingPeriodsCallback {

    override var currentGradingPeriod: GradingPeriod? = null

    private var assignmentGroupsJob: WeaveJob? = null
    private var gradingPeriodJob: WeaveJob? = null
    private var courseJob: WeaveJob? = null

    protected var assignmentGroups: List<AssignmentGroup> = emptyList()
    private var restrictQuantitativeData = false
    private var gradingSchemes = listOf<GradingSchemeRow>()

    var filter: AssignmentListFilter = AssignmentListFilter.ALL
    set(value) {
        field = value
        if (isAllPagesLoaded) {
            clear()
            populateData()
            onCallbackFinished(ApiType.CACHE)
        }
    }

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
        isExpandedByDefault = true
        isDisplayEmptyCell = true
        this.filter = filter
        if (!isTesting) loadData()
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

        courseJob = tryWeave {
            val detailedCourse = repository.getCourseWithGrade(canvasContext.id, isRefresh)
            restrictQuantitativeData = detailedCourse?.settings?.restrictQuantitativeData ?: false
            gradingSchemes = detailedCourse?.gradingScheme ?: emptyList()
            loadAssignmentsData(course)
        } catch {
            loadAssignmentsData(course)
        }
    }

    private fun loadAssignmentsData(course: Course) {
        //This check is for the "all grading periods" option
        if (currentGradingPeriod != null && currentGradingPeriod!!.title != null
            && currentGradingPeriod!!.title == context.getString(R.string.assignmentsListAllGradingPeriods)) {
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
            } else {
                adapterToAssignmentsCallback.gradingPeriodsFetched(emptyList())
            }
        }
        //If we made it this far, MGP is disabled so we just go forward with the standard
        loadAssignment()
    }

    private fun fetchGradingPeriods(courseId: Long) {
        gradingPeriodJob?.cancel()
        gradingPeriodJob = tryWeave {
            val periods = repository.getGradingPeriodsForCourse(courseId, isRefresh)
            adapterToAssignmentsCallback.gradingPeriodsFetched(periods)
        } catch {
            adapterToAssignmentsCallback.gradingPeriodsFetched(emptyList())
            Logger.w("Unable to fetch grading periods")
            it.printStackTrace()
        }
    }

    override fun contextReady() {}

    override val isPaginated get() = false

    override fun onBindChildHolder(
            holder: RecyclerView.ViewHolder,
            assignmentGroup: AssignmentGroup,
            assignment: Assignment
    ) {
        (holder as AssignmentViewHolder).bind(context, assignment, canvasContext.backgroundColor, adapterToAssignmentsCallback, restrictQuantitativeData, gradingSchemes)
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

    override fun loadAssignmentsForGradingPeriod(gradingPeriodId: Long, refreshFirst: Boolean) {
        /*Logic regarding MGP is similar here as it is in both assignment recycler adapters,
            if changes are made here, check if they are needed in the other recycler adapters.*/
        if (refreshFirst) resetData()

        // Scope assignments if its for a student
        val scopeToStudent = (canvasContext as Course).isStudent

        assignmentGroupsJob?.cancel()
        assignmentGroupsJob = tryWeave {
            val groups = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                canvasContext.id,
                gradingPeriodId,
                scopeToStudent,
                isRefresh
            )
            onAssignmentGroupsFetched(groups)
        } catch {
            adapterToAssignmentsCallback.assignmentLoadingFinished()
            onCallbackFinished(null)
        }
    }

    override fun loadAssignment() {
        assignmentGroupsJob?.cancel()
        assignmentGroupsJob = tryWeave {
            val groups = repository.getAssignmentGroupsWithAssignments(canvasContext.id, isRefresh)
            onAssignmentGroupsFetched(groups)
        } catch {
            adapterToAssignmentsCallback.assignmentLoadingFinished()
            onCallbackFinished(null)
        }
    }

    private fun onAssignmentGroupsFetched(groups: List<AssignmentGroup>) {
        assignmentGroups = groups
        populateData()
        adapterToAssignmentsCallback.onRefreshFinished()
        adapterToAssignmentsCallback.assignmentLoadingFinished()
        onCallbackFinished(null)
    }

    protected abstract fun populateData()

    // region Expandable callbacks

    override fun createGroupCallback() = object : GroupSortedList.GroupComparatorCallback<AssignmentGroup> {
        override fun compare(o1: AssignmentGroup, o2: AssignmentGroup) = o1.position - o2.position
        override fun areContentsTheSame(old: AssignmentGroup, new: AssignmentGroup) = old.name == new.name
        override fun areItemsTheSame(group1: AssignmentGroup, group2: AssignmentGroup) = group1.position == group2.position
        override fun getGroupType(group: AssignmentGroup) = Types.TYPE_HEADER
        override fun getUniqueGroupId(group: AssignmentGroup) = group.position.toLong()
    }

    // endregion

    override fun cancel() {
        super.cancel()
        assignmentGroupsJob?.cancel()
        gradingPeriodJob?.cancel()
        courseJob?.cancel()
    }
}

enum class AssignmentListFilter {
    ALL,
    LATE,
    MISSING,
    GRADED,
    UPCOMING
}