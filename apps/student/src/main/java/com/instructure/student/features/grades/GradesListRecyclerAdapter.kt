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

package com.instructure.student.features.grades

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isNullOrEmpty
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.student.R
import com.instructure.student.adapter.ExpandableRecyclerAdapter
import com.instructure.student.dialog.WhatIfDialogStyled
import com.instructure.student.holders.EmptyViewHolder
import com.instructure.student.holders.ExpandableViewHolder
import com.instructure.student.holders.GradeViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
open class GradesListRecyclerAdapter(
    context: Context,
    var canvasContext: CanvasContext? = null,
    val adapterToFragmentCallback: AdapterToFragmentCallback<Assignment>? = null,
    private val repository: GradesListRepository,
    private val onGradingPeriodResponse: (List<GradingPeriod>) -> Unit,
    private val adapterToGradesCallback: AdapterToGradesCallback? = null,
    private val whatIfDialogCallback: WhatIfDialogStyled.WhatIfDialogCallback? = null
) : ExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder>(context, AssignmentGroup::class.java, Assignment::class.java) {

    init {
        isExpandedByDefault = true
        if (adapterToFragmentCallback != null) loadData() // Callback is null when testing
    }

    private var allAssignmentsAndGroupsJob: Job? = null

    private var selectedItemCallback: SetSelectedItemCallback? = null

    var assignmentGroups: ArrayList<AssignmentGroup> = arrayListOf()
    var assignmentsHash: HashMap<Long, Assignment> = hashMapOf()

    var whatIfGrade: Double? = null
    var currentGradingPeriod: GradingPeriod? = null

    private var observerCourseGradeJob: Job? = null
    private var loadDataJob: Job? = null
    private var assignmentsForGradingPeriodJob: Job? = null

    // State for keeping track of grades for what/if and switching between periods
    var courseGrade: CourseGrade? = null
        private set

    val isAllGradingPeriodsSelected: Boolean
        get() = (currentGradingPeriod != null
                && currentGradingPeriod!!.title != null
                && currentGradingPeriod!!.title == context.getString(R.string.allGradingPeriods))

    interface AdapterToGradesCallback {
        val isEdit: Boolean
        fun notifyGradeChanged(course: Course?, courseGrade: CourseGrade?, restrictQuantitativeData: Boolean, gradingScheme: List<GradingSchemeRow>)
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
        Types.TYPE_HEADER -> ExpandableViewHolder.HOLDER_RES_ID
        Types.TYPE_EMPTY_CELL -> EmptyViewHolder.HOLDER_RES_ID
        else -> GradeViewHolder.HOLDER_RES_ID
    }

    final override fun loadData() {
        loadData(true)
    }

    fun loadCachedData() {
        adapterToRecyclerViewCallback?.refresh()
        resetData()
        loadData(false)
    }

    private fun loadData(forceNetwork: Boolean) {
        loadDataJob = GlobalScope.launch(Dispatchers.Main) {
            try {
                // Logic regarding MGP is similar here as it is in both assignment recycler adapters,
                // if changes are made here, check if they are needed in the other recycler adapters
                val course = repository.getCourseWithGrade(canvasContext!!.id, forceNetwork)
                val enrollments = (canvasContext as Course).enrollments
                canvasContext = course

                // Use the enrollments that were passed in with the course if one returned has none
                // Should only be concluded courses that this applies
                (canvasContext as Course).apply {
                    if (this.enrollments.isNullOrEmpty()) {
                        this.enrollments = enrollments
                    }
                }

                if (isAllGradingPeriodsSelected) {
                    isRefresh = true
                    updateCourseGrade()
                    updateWithAllAssignments(forceNetwork)
                    return@launch
                }

                course.enrollments!!.firstOrNull {
                    it.isStudent && it.multipleGradingPeriodsEnabled
                }?.let { enrollment ->
                    setupStudentGrades(enrollment, course, forceNetwork)
                } ?: course.enrollments!!.firstOrNull { it.isObserver }?.let { enrollment ->
                    setupObserverGrades(enrollment, course, forceNetwork)
                }

                // If we've made it this far, MGP is not enabled, so we do the standard behavior
                isRefresh = true
                updateCourseGrade()
                updateWithAllAssignments(forceNetwork)
            } catch (e: CancellationException) {
                //We cancelled the job, nothing to do here
            } catch (e: Throwable) {
                Toast.makeText(context, R.string.errorOccurred, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObserverGrades(enrollment: Enrollment, course: Course, forceNetwork: Boolean) {
        // Load current term
        currentGradingPeriod = GradingPeriod(
            id = enrollment.currentGradingPeriodId,
            title = enrollment.currentGradingPeriodTitle
        )

        // Get the first student that this user is observing
        observerCourseGradeJob = GlobalScope.launch(Dispatchers.Main) {
            try {
                // We need to use an ID from an observee, not the user (who is currently logged in as an observer) when retrieving the enrollments

                // Get the first student this user is observing, if none show empty assignments
                val student = repository.getObserveeEnrollments(forceNetwork)
                    .firstOrNull { it.courseId == course.id && it.observedUser != null }?.observedUser
                    ?: return@launch updateAssignmentGroups(emptyList())

                // Get Assignment Groups
                val assignmentGroups = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                    canvasContext!!.id,
                    currentGradingPeriod!!.id,
                    false,
                    forceNetwork
                )
                // The assignments in the assignment groups do not come with their submissions (with the associated grades), so we get them all here
                val assignmentIds = assignmentGroups.map { it.assignments }.flatten().map { it.id }
                val submissions = repository.getSubmissionsForMultipleAssignments(student.id, course.id, assignmentIds, forceNetwork)
                assignmentGroups.forEach { group ->
                    group.assignments.forEach { assignment ->
                        assignment.submission = submissions.firstOrNull { it.assignmentId == assignment.id }
                    }
                }

                updateAssignmentGroups(assignmentGroups)

                repository.getCoursesWithSyllabus(forceNetwork).onEach { course ->
                    course.enrollments?.find { it.userId == student.id }?.let {
                        course.enrollments = mutableListOf(it)
                        courseGrade = course.getCourseGradeFromEnrollment(it, false)
                        val restrictQuantitativeData = course.settings?.restrictQuantitativeData ?: false
                        adapterToGradesCallback?.notifyGradeChanged(course, courseGrade, restrictQuantitativeData, course.gradingScheme)
                    }
                }
            } catch (e: CancellationException) {
                //We cancelled the job, nothing to do here
            } catch (e: Throwable) {
                Toast.makeText(context, R.string.errorOccurred, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun setupStudentGrades(enrollment: Enrollment, course: Course, forceNetwork: Boolean) {
        if (currentGradingPeriod == null || currentGradingPeriod?.title == null) {
            // We load current term
            currentGradingPeriod = GradingPeriod(
                id = enrollment.currentGradingPeriodId,
                title = enrollment.currentGradingPeriodTitle
            )

            // Request the grading period objects and make the assignment calls
            val result = repository.getGradingPeriodsForCourse(course.id, forceNetwork)
            if (result.isNotEmpty()) onGradingPeriodResponse(result)
        } else {
            // Otherwise we load the info from the current grading period
            loadAssignmentsForGradingPeriod(currentGradingPeriod!!.id, true, forceNetwork)
        }
    }

    fun loadAssignmentsForGradingPeriod(gradingPeriodID: Long, refreshFirst: Boolean, forceNetwork: Boolean) {
        // Logic regarding MGP is similar here as it is in both assignment recycler adapters,
        // if changes are made here, check if they are needed in the other recycler adapters
        if (refreshFirst) {
            resetData()
        }

        // Scope assignments if it's for a student
        val scopeToStudent = (canvasContext as Course).isStudent
        if (scopeToStudent) {
            assignmentsForGradingPeriodJob = GlobalScope.launch(Dispatchers.Main) {
                try {
                    val assignmentGroups = repository.getAssignmentGroupsWithAssignmentsForGradingPeriod(
                        canvasContext!!.id,
                        gradingPeriodID,
                        true,
                        forceNetwork
                    )
                    updateAssignmentGroups(assignmentGroups)

                    // Fetch the enrollments associated with the selected gradingPeriodID, these will contain the
                    // correct grade for the period
                    val enrollments = repository.getUserEnrollmentsForGradingPeriod(
                        canvasContext!!.id,
                        ApiPrefs.user!!.id,
                        gradingPeriodID,
                        forceNetwork
                    )
                    updateCourseGradeFromGradingPeriodSpecificEnrollment(enrollments)

                    // Inform the spinner things are done
                    adapterToGradesCallback?.setTermSpinnerState(true)
                } catch (e: CancellationException) {
                    //We cancelled the job, nothing to do here
                } catch (e: Throwable) {
                    Toast.makeText(context, R.string.errorOccurred, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateCourseGradeFromGradingPeriodSpecificEnrollment(enrollments: List<Enrollment>) {
        for (enrollment in enrollments) {
            if (enrollment.isStudent && enrollment.userId == ApiPrefs.user!!.id) {
                val course = canvasContext as Course?
                courseGrade = course!!.getCourseGradeForGradingPeriodSpecificEnrollment(enrollment = enrollment)
                val restrictQuantitativeData = course.settings?.restrictQuantitativeData ?: false
                adapterToGradesCallback?.notifyGradeChanged(course, courseGrade, restrictQuantitativeData, course.gradingScheme)
                // We need to update the course that the fragment is using
                course.addEnrollment(enrollment)
            }
        }
    }

    private fun updateCourseGrade() {
        // All grading periods and no grading periods are the same case
        val course = canvasContext as? Course
        courseGrade = course?.getCourseGrade(true)
        val restrictQuantitativeData = course?.settings?.restrictQuantitativeData ?: false
        val gradingScheme = course?.gradingScheme ?: emptyList()
        adapterToGradesCallback?.notifyGradeChanged(course, courseGrade, restrictQuantitativeData, gradingScheme)
    }

    private fun updateWithAllAssignments(forceNetwork: Boolean) {
        allAssignmentsAndGroupsJob = GlobalScope.launch(Dispatchers.Main) {
            try {
                // Standard load assignments, unfiltered
                val aGroups = repository.getAssignmentGroupsWithAssignments(canvasContext!!.id, forceNetwork)
                updateAssignmentGroups(aGroups)
            } catch (e: CancellationException) {
                //We cancelled the job, nothing to do here
            } catch (e: Throwable) {
                e.printStackTrace()
                Toast.makeText(context, R.string.errorOccurred, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAssignmentGroups(aGroups: List<AssignmentGroup>) {
        // We still need to maintain local copies of the assignments/groups for what-if grades
        // so that we have the assignments Hash and assignments group list
        var hasValidGroupRule = false
        for (group in aGroups) {
            // Mark the flag as true if any group has a valid rule
            if (group.rules?.hasValidRule() == true) {
                hasValidGroupRule = true
            }

            val gradedAssignments = group.assignments.filter { isGradedAssignment(it) }
            addOrUpdateAllItems(group, gradedAssignments)
            for (assignment in gradedAssignments) {
                assignmentsHash[assignment.id] = assignment
            }
            if (assignmentGroups.none { it.id == group.id }) {
                assignmentGroups.add(group)
            }
        }
        isAllPagesLoaded = true

        // We want to disable what if grading if MGP weights are enabled, or assignment groups are enabled
        val course = (canvasContext as Course)
        if (course.isWeightedGradingPeriods || hasValidGroupRule || course.settings?.restrictQuantitativeData == true) {
            adapterToGradesCallback?.setIsWhatIfGrading(false)
        } else {
            adapterToGradesCallback?.setIsWhatIfGrading(true)
        }

        adapterToFragmentCallback?.onRefreshFinished()
        this@GradesListRecyclerAdapter.onCallbackFinished(null)
    }

    private fun isGradedAssignment(assignment: Assignment): Boolean {
        val gradingType = assignment.gradingType ?: ""
        return Assignment.getGradingTypeFromAPIString(gradingType) != Assignment.GradingType.NOT_GRADED
    }

    override fun setupCallbacks() {
        selectedItemCallback = object : SetSelectedItemCallback {
            override fun setSelected(position: Int) {
                setSelectedPosition(position)
            }
        }
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, assignmentGroup: AssignmentGroup, assignment: Assignment) {
        val isEdit = adapterToGradesCallback?.isEdit ?: false
        if (isEdit) {
            (holder as GradeViewHolder).bind(context, canvasContext, assignmentsHash[assignment.id]!!, adapterToGradesCallback!!.isEdit, whatIfDialogCallback!!, adapterToFragmentCallback!!, selectedItemCallback!!)
        } else {
            (holder as GradeViewHolder).bind(context, canvasContext, assignment, adapterToGradesCallback!!.isEdit, whatIfDialogCallback!!, adapterToFragmentCallback!!, selectedItemCallback!!)
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, assignmentGroup: AssignmentGroup, isExpanded: Boolean) {
        (holder as ExpandableViewHolder).bind(context, assignmentGroup, assignmentGroup.name, isExpanded, viewHolderHeaderClicked)
    }

    override fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, assignmentGroup: AssignmentGroup) {
        (holder as EmptyViewHolder).bind(context.resources.getString(R.string.noAssignmentsInGroup))
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<AssignmentGroup> {
        return object : GroupSortedList.GroupComparatorCallback<AssignmentGroup> {
            override fun compare(o1: AssignmentGroup, o2: AssignmentGroup) = o1.position - o2.position
            override fun areContentsTheSame(oldGroup: AssignmentGroup, newGroup: AssignmentGroup) = oldGroup.name == newGroup.name
            override fun areItemsTheSame(group1: AssignmentGroup, group2: AssignmentGroup) = group1.id == group2.id
            override fun getGroupType(group: AssignmentGroup) = Types.TYPE_HEADER
            override fun getUniqueGroupId(group: AssignmentGroup) = group.id
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> {
        return object : GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> {
            override fun compare(group: AssignmentGroup, o1: Assignment, o2: Assignment) = o2.dueDate?.let { o1.dueDate?.compareTo(o2.dueDate) ?: -1 } ?: -1
            override fun areContentsTheSame(oldItem: Assignment, newItem: Assignment) = compareAssignments(oldItem, newItem)
            override fun areItemsTheSame(item1: Assignment, item2: Assignment) = item1.id == item2.id
            override fun getUniqueItemId(item: Assignment) = item.id
            override fun getChildType(group: AssignmentGroup, item: Assignment) = Types.TYPE_ITEM
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
        observerCourseGradeJob?.cancel()
        loadDataJob?.cancel()
        allAssignmentsAndGroupsJob?.cancel()
        assignmentsForGradingPeriodJob?.cancel()
    }
}
