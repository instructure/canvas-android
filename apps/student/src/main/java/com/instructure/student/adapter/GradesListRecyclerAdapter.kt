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
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.isNullOrEmpty
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.binders.EmptyBinder
import com.instructure.student.binders.ExpandableHeaderBinder
import com.instructure.student.binders.GradeBinder
import com.instructure.student.dialog.WhatIfDialogStyled
import com.instructure.student.holders.EmptyViewHolder
import com.instructure.student.holders.ExpandableViewHolder
import com.instructure.student.holders.GradeViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback
import retrofit2.Call
import retrofit2.Response
import java.util.*

open class GradesListRecyclerAdapter(
        context: Context,
        var canvasContext: CanvasContext? = null,
        val adapterToFragmentCallback: AdapterToFragmentCallback<Assignment>? = null,
        val adapterToGradesCallback: AdapterToGradesCallback? = null,
        val gradingPeriodsCallback: StatusCallback<GradingPeriodResponse>? = null,
        private val whatIfDialogCallback: WhatIfDialogStyled.WhatIfDialogCallback? = null
) : ExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder>(context, AssignmentGroup::class.java, Assignment::class.java) {

    init {
        isExpandedByDefault = true
        if (adapterToFragmentCallback != null) loadData() // Callback is null when testing
    }

    private var assignmentGroupCallback: StatusCallback<List<AssignmentGroup>>? = null
    private var courseCallback: StatusCallback<Course>? = null
    private var enrollmentCallback: StatusCallback<List<Enrollment>>? = null

    private var selectedItemCallback: SetSelectedItemCallback? = null

    var assignmentGroups: ArrayList<AssignmentGroup> = arrayListOf()
    var assignmentsHash: HashMap<Long, Assignment> = hashMapOf()

    var whatIfGrade: Double? = null
    var currentGradingPeriod: GradingPeriod? = null

    // State for keeping track of grades for what/if and switching between periods
    var courseGrade: CourseGrade? = null
        private set

    val isAllGradingPeriodsSelected: Boolean
        get() = (currentGradingPeriod != null
                && currentGradingPeriod!!.title != null
                && currentGradingPeriod!!.title == context.getString(R.string.allGradingPeriods))

    interface AdapterToGradesCallback {
        val isEdit: Boolean
        fun notifyGradeChanged(courseGrade: CourseGrade?)
        fun setTermSpinnerState(isEnabled: Boolean)
        fun setIsWhatIfGrading(isWhatIfGrading: Boolean)
    }

    interface SetSelectedItemCallback {
        fun setSelected(position: Int)
    }


    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        Types.TYPE_HEADER -> ExpandableViewHolder(v)
        Types.TYPE_EMPTY_CELL -> EmptyViewHolder(v)
        else -> GradeViewHolder(v)
    }

    override fun itemLayoutResId(viewType: Int): Int = when (viewType) {
        Types.TYPE_HEADER -> ExpandableViewHolder.holderResId()
        Types.TYPE_EMPTY_CELL -> EmptyViewHolder.holderResId()
        else -> GradeViewHolder.HOLDER_RES_ID
    }

    override fun loadData() {
        CourseManager.getCourseWithGrade(canvasContext!!.id, courseCallback!!, true)
    }

    fun loadAssignmentsForGradingPeriod(gradingPeriodID: Long, refreshFirst: Boolean) {
        // Logic regarding MGP is similar here as it is in both assignment recycler adapters,
        // if changes are made here, check if they are needed in the other recycler adapters
        if (refreshFirst) {
            resetData()
        }

        // Scope assignments if its for a student
        val scopeToStudent = (canvasContext as Course).isStudent
        AssignmentManager.getAssignmentGroupsWithAssignmentsForGradingPeriod(canvasContext!!.id, gradingPeriodID, scopeToStudent, isRefresh, assignmentGroupCallback!!)

        // Fetch the enrollments associated with the selected gradingPeriodID, these will contain the
        // correct grade for the period
        CourseManager.getUserEnrollmentsForGradingPeriod(canvasContext!!.id, ApiPrefs.user!!.id, gradingPeriodID, enrollmentCallback!!, true)
    }

    private fun loadAssignment() {
        // All grading periods and no grading periods are the same case
        courseGrade = (canvasContext as Course).getCourseGrade(true)
        adapterToGradesCallback?.notifyGradeChanged(courseGrade)

        // Standard load assignments, unfiltered
        AssignmentManager.getAssignmentGroupsWithAssignments(canvasContext!!.id, isRefresh, assignmentGroupCallback!!)
    }

    override fun setupCallbacks() {
        // Logic regarding MGP is similar here as it is in both assignment recycler adapters,
        // if changes are made here, check if they are needed in the other recycler adapters
        courseCallback = object : StatusCallback<Course>() {

            override fun onResponse(response: Response<Course>, linkHeaders: LinkHeaders, type: ApiType) {
                val course = response.body()
                val enrollments = (canvasContext as Course).enrollments
                canvasContext = course as Course

                // Use the enrollments that were passed in with the course if one returned has none
                // Should only be concluded courses that this applies
                // TODO: Fix in canvas API to have a single course return ALL enrollments, not just current ones
                (canvasContext as Course).apply {
                    if (this.enrollments.isNullOrEmpty()) {
                        this.enrollments = enrollments
                    }
                }

                // We want to disable what if grading if MGP weights are enabled
                if (course.isWeightedGradingPeriods) {
                    adapterToGradesCallback?.setIsWhatIfGrading(false)
                } else {
                    adapterToGradesCallback?.setIsWhatIfGrading(true)
                }

                if (isAllGradingPeriodsSelected) {
                    isRefresh = true
                    loadAssignment()
                    return
                }

                for (enrollment in course.enrollments!!) {
                    if (enrollment.isStudent && enrollment.multipleGradingPeriodsEnabled) {
                        if (currentGradingPeriod == null || currentGradingPeriod!!.title == null) {
                            // we load current term
                            currentGradingPeriod = GradingPeriod()
                            currentGradingPeriod!!.id = enrollment.currentGradingPeriodId
                            currentGradingPeriod!!.title = enrollment.currentGradingPeriodTitle
                            // request the grading period objects and make the assignment calls
                            // This callback is fulfilled in the grade list fragment.
                            CourseManager.getGradingPeriodsForCourse(gradingPeriodsCallback!!, course.id, true)
                            return
                        } else {
                            // Otherwise we load the info from the current grading period
                            loadAssignmentsForGradingPeriod(currentGradingPeriod!!.id, true)
                            return
                        }
                    }
                }

                // if we've made it this far, MGP is not enabled, so we do the standard behavior
                isRefresh = true
                loadAssignment()
            }
        }

        assignmentGroupCallback = object : StatusCallback<List<AssignmentGroup>>() {
            override fun onResponse(response: Response<List<AssignmentGroup>>, linkHeaders: LinkHeaders, type: ApiType) {
                // We still need to maintain local copies of the assignments/groups for what if grades
                // so we have the assignments Hash and assignments group list
                for (group in response.body()!!) {
                    addOrUpdateAllItems(group, group.assignments)
                    for (assignment in group.assignments) {
                        assignmentsHash[assignment.id] = assignment
                    }
                    if (!assignmentGroups.contains(group)) {
                        assignmentGroups.add(group)
                    }
                }
                isAllPagesLoaded = true

                adapterToFragmentCallback?.onRefreshFinished()
            }

            override fun onFinished(type: ApiType) {
                this@GradesListRecyclerAdapter.onCallbackFinished(type)
            }
        }

        enrollmentCallback = object : StatusCallback<List<Enrollment>>() {

            override fun onResponse(response: Response<List<Enrollment>>, linkHeaders: LinkHeaders, type: ApiType) {
                for (enrollment in response.body()!!) {
                    if (enrollment.isStudent && enrollment.userId == ApiPrefs.user!!.id) {
                        val course = canvasContext as Course?
                        courseGrade = course!!.getCourseGradeFromEnrollment(enrollment, false)
                        adapterToGradesCallback?.notifyGradeChanged(courseGrade)
                        // Inform the spinner things are done
                        adapterToGradesCallback?.setTermSpinnerState(true)
                        // We need to update the course that the fragment is using
                        //                        course = course.copy(enrollments = course.enrollments + enrollment)
                        course.addEnrollment(enrollment)
                    }
                }
            }

            override fun onFail(call: Call<List<Enrollment>>?, error: Throwable, response: Response<*>?) {
                adapterToGradesCallback?.setTermSpinnerState(true)
            }

        }

        selectedItemCallback = object : SetSelectedItemCallback {
            override fun setSelected(position: Int) {
                selectedPosition = position
            }
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, assignmentGroup: AssignmentGroup, assignment: Assignment) {
        val isEdit = adapterToGradesCallback?.isEdit ?: false
        if (isEdit) {
            GradeBinder.bind(holder as GradeViewHolder, context, ColorKeeper.getOrGenerateColor(canvasContext), assignmentsHash[assignment.id]!!, adapterToGradesCallback!!.isEdit, whatIfDialogCallback!!, adapterToFragmentCallback!!, selectedItemCallback!!)
        } else {
            GradeBinder.bind(holder as GradeViewHolder, context, ColorKeeper.getOrGenerateColor(canvasContext), assignment, adapterToGradesCallback!!.isEdit, whatIfDialogCallback!!, adapterToFragmentCallback!!, selectedItemCallback!!)
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, assignmentGroup: AssignmentGroup, isExpanded: Boolean) {
        ExpandableHeaderBinder.bind(context, canvasContext, holder as ExpandableViewHolder, assignmentGroup, assignmentGroup.name, isExpanded, viewHolderHeaderClicked)
    }

    override fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, assignmentGroup: AssignmentGroup) {
        EmptyBinder.bind(holder as EmptyViewHolder, context.resources.getString(R.string.noAssignmentsInGroup))
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<AssignmentGroup> {
        return object : GroupSortedList.GroupComparatorCallback<AssignmentGroup> {
            override fun compare(o1: AssignmentGroup, o2: AssignmentGroup): Int {
                return o1.position - o2.position
            }

            override fun areContentsTheSame(oldGroup: AssignmentGroup, newGroup: AssignmentGroup): Boolean {
                return oldGroup.name == newGroup.name
            }

            override fun areItemsTheSame(group1: AssignmentGroup, group2: AssignmentGroup): Boolean {
                return group1.id == group2.id
            }

            override fun getGroupType(group: AssignmentGroup): Int {
                return Types.TYPE_HEADER
            }

            override fun getUniqueGroupId(group: AssignmentGroup): Long {
                return group.id
            }
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> {
        return object : GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> {
            override fun compare(group: AssignmentGroup, o1: Assignment, o2: Assignment): Int {
                return o1.position - o2.position
            }

            override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment): Boolean {
                return compareAssignments(oldItem, newItem)
            }

            override fun areItemsTheSame(item1: Assignment, item2: Assignment): Boolean {
                return item1.id == item2.id
            }

            override fun getUniqueItemId(item: Assignment): Long {
                return item.id
            }

            override fun getChildType(group: AssignmentGroup, item: Assignment): Int {
                return Types.TYPE_ITEM
            }
        }
    }

    override fun resetData() {
        assignmentsHash.clear()
        assignmentGroups.clear()
        super.resetData()
    }

    private fun compareAssignments(oldItem: Assignment, newItem: Assignment): Boolean {
        val isSameName = oldItem.name == newItem.name
        val isSameScore = oldItem.pointsPossible == newItem.pointsPossible
        var isSameSubmission = true
        var isSameGrade = true
        val oldSubmission = oldItem.submission
        val newSubmission = newItem.submission
        if (oldSubmission != null && newSubmission != null) {
            if (oldSubmission.grade != null && newSubmission.grade != null) {
                isSameGrade = oldSubmission.grade == newSubmission.grade
            } else if (isNullableChanged(oldSubmission.grade, newSubmission.grade)) {
                isSameGrade = false
            }
        } else if (isNullableChanged(oldSubmission, newSubmission)) {
            isSameSubmission = false
        }
        return isSameName && isSameGrade && isSameScore && isSameSubmission
    }

    private fun isNullableChanged(o1: Any?, o2: Any?): Boolean {
        return o1 == null && o2 != null || o1 != null && o2 == null
    }

    override fun cancel() {
        super.cancel()
        assignmentGroupCallback!!.cancel()
        courseCallback!!.cancel()
        gradingPeriodsCallback?.cancel()
        enrollmentCallback!!.cancel()
    }
}
