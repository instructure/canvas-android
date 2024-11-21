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
package com.instructure.teacher.features.assignment.submission

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.intersectBy
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.teacher.utils.getState
import com.instructure.teacher.viewinterface.AssignmentSubmissionListView
import com.instructure.pandautils.blueprint.SyncPresenter
import kotlinx.coroutines.Job
import java.util.Locale
import java.util.Random

class AssignmentSubmissionListPresenter(
    val assignment: Assignment,
    private var filter: SubmissionListFilter,
    private val assignmentSubmissionRepository: AssignmentSubmissionRepository
) : SyncPresenter<GradeableStudentSubmission, AssignmentSubmissionListView>(GradeableStudentSubmission::class.java) {

    private var apiCalls: Job? = null

    private var unfilteredSubmissions: List<GradeableStudentSubmission> = emptyList()
    private var filteredSubmissions: List<GradeableStudentSubmission> = emptyList()

    private var filterValue: Double = 0.0

    private var sectionsSelected = ArrayList<CanvasContext>()

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun loadData(forceNetwork: Boolean) {
        // Skip if API call are already in progress
        if (apiCalls?.isActive == true) return

        // Use existing data if we already have it. Unfiltered submissions should be cleared on refresh
        if (!forceNetwork && unfilteredSubmissions.isNotEmpty()) {
            setFilteredData()
            return
        }

        // Get data from network
        apiCalls = weave {
            try {
                viewCallback?.onRefreshStarted()
                unfilteredSubmissions =
                    assignmentSubmissionRepository.getGradeableStudentSubmissions(
                        assignment,
                        assignment.courseId,
                        forceNetwork
                    )
                setFilteredData()
            } catch (ignore: Throwable) {
                ignore.printStackTrace()
            }
        }
    }

    fun setSections(sections: ArrayList<CanvasContext>) {
        sectionsSelected = sections

        setFilteredData()
    }

    /**
     * Convert the list of CanvasContexts to a list of just ids so the dialog can know which CanvasContexts
     * have been selected
     *
     * @return
     */
    fun getSectionListIds(): ArrayList<Long> {
        val contextIds = ArrayList<Long>()
        sectionsSelected.forEach {
            contextIds.add(it.id)
        }
        return contextIds
    }

    fun getSectionFilterText() : String {
        when (sectionsSelected.isEmpty()) {
            true -> return ""
            false -> {
                // get the title based on Section selected
                val title = StringBuilder()
                title.append(", ")
                sectionsSelected.forEachIndexed { index, canvasContext ->
                    title.append(canvasContext.name)
                    if ((index + 1) < sectionsSelected.size) {
                        title.append(", ")
                    }
                }
                return title.toString().trim()
            }
        }
    }

    fun clearFilterList() {
        sectionsSelected.clear()
    }

    private fun setFilteredData() {
        filteredSubmissions = unfilteredSubmissions.filter {
            when (filter) {
                SubmissionListFilter.ALL -> true
                SubmissionListFilter.LATE -> it.submission?.let { assignment.getState(it, true) in listOf(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE) } ?: false
                SubmissionListFilter.NOT_GRADED -> it.submission?.let { assignment.getState(it, true) in listOf(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED, AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE) || !it.isGradeMatchesCurrentSubmission } ?: false
                SubmissionListFilter.GRADED -> it.submission?.let { assignment.getState(it, true) in listOf(AssignmentUtils2.ASSIGNMENT_STATE_GRADED, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE, AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING, AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED)  && it.isGradeMatchesCurrentSubmission} ?: false
                SubmissionListFilter.ABOVE_VALUE -> it.submission?.let { it.isGraded && it.score >= filterValue } ?: false
                SubmissionListFilter.BELOW_VALUE -> it.submission?.let { it.isGraded && it.score < filterValue } ?: false
                // Filtering by ASSIGNMENT_STATE_MISSING here doesn't work because it assumes that the due date has already passed, which isn't necessarily the case when the teacher wants to see
                // which students haven't submitted yet
                SubmissionListFilter.MISSING -> it.submission?.workflowState == "unsubmitted" || it.submission == null
            }
        }

        // Shuffle if grading anonymously
        if (assignment.anonymousGrading) filteredSubmissions = filteredSubmissions.shuffled(Random(1234))

        data.clear()

        // Filter by section if there is a section filter set
        if (sectionsSelected.isNotEmpty()) {

            // get list of ids
            val sectionIds = sectionsSelected.map { it.id }

            filteredSubmissions.forEach { submission ->
                sectionIds.forEach { section ->
                    if (submission.assignee is StudentAssignee) {
                        (submission.assignee as StudentAssignee).student.enrollments.forEach {
                            if (it.courseSectionId == section) {
                                data.addOrUpdate(submission)
                            }
                        }
                    }
                }
            }
        } else {
            // No section filter, add all the submission filtered users
            data.addOrUpdate(filteredSubmissions)
        }

        viewCallback?.onRefreshFinished()
        viewCallback?.checkIfEmpty()
    }


    override fun onDestroyed() {
        apiCalls?.cancel()
        super.onDestroyed()
    }

    override fun refresh(forceNetwork: Boolean) {
        clearData()
        unfilteredSubmissions = emptyList()
        loadData(forceNetwork)
    }

    fun setFilter(filter: SubmissionListFilter, value: Double = 0.0) {
        //In case this filter hasn't been shown yet, we'll need to show the loader so the user
        //doesn't see "no items" view first
        viewCallback?.onRefreshStarted()
        this.filter = filter
        filterValue = value
        setFilteredData()
    }

    fun getFilter() : SubmissionListFilter = filter

    fun getFilterPoints() : Double = filterValue

    fun getRecipients() : List<Recipient> {
        return filteredSubmissions.map { submission ->
            when(val assignee = submission.assignee) {
                is StudentAssignee -> Recipient.from(assignee.student)
                is GroupAssignee -> Recipient.from(assignee.group)
            }
        }
    }

    override fun compare(item1: GradeableStudentSubmission, item2: GradeableStudentSubmission): Int {
        // Turns out we do need to sort them by sortable name, but not when anonymous grading is on
        if (item1.assignee is StudentAssignee && item2.assignee is StudentAssignee && !assignment.anonymousGrading) {
            return (item1.assignee as StudentAssignee).student.sortableName?.lowercase(Locale.getDefault())
                ?.compareTo((item2.assignee as StudentAssignee).student.sortableName?.lowercase(
                    Locale.getDefault()
                )!!) ?: -1
        }
        return -1
    }

    override fun areContentsTheSame(item1: GradeableStudentSubmission, item2: GradeableStudentSubmission) = false

    // Put in a companion object so we can use it to route to speedgrader from the to do list
    companion object {
        fun makeGroupSubmissions(students: List<User>, groups: List<Group>, submissions: List<Submission>): List<GradeableStudentSubmission> {
            // Set up group assignees
            val userMap = students.associateBy { it.id }
            val groupAssignees = groups.map { GroupAssignee(it, it.users.map { userMap[it.id] ?: it }) }

            // Set up individual student assignees, if any
            val individualIds = students.map { it.id } - groupAssignees.flatMap { it.students.map { it.id } }
            val individualAssignees = individualIds
                    .map { userMap[it] }
                    .filterNotNull()
                    .map { StudentAssignee(it) }

            // Divide submissions into group and individual submissions
            val (groupedSubmissions, individualSubmissions) = submissions.partition { it.group?.id ?: 0L != 0L }
            val studentSubmissionMap = individualSubmissions.associateBy { it.userId }
            val groupSubmissionMap = groupedSubmissions
                    .groupBy { it.group!!.id }
                    .mapValues {
                        it.value.reduce { acc, submission ->
                            acc.submissionComments = acc.submissionComments.intersectBy(submission.submissionComments) {
                                "${it.authorId}|${it.comment}|${it.createdAt?.time}"
                            }
                            acc
                        }
                    }
                    .toMap()

            // Set up GradeableStudentSubmissions
            val groupSubs = groupAssignees.map { GradeableStudentSubmission(it, groupSubmissionMap[it.group.id]) }
            val individualSubs = individualAssignees.map { GradeableStudentSubmission(it, studentSubmissionMap[it.student.id]) }

            return groupSubs + individualSubs
        }
    }
}
