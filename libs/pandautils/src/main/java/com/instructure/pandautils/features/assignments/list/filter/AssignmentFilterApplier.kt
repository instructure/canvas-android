/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.assignments.list.filter

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.orderedCheckpoints
import java.util.Date

fun List<Assignment>.applyAssignmentTypeFilters(filters: List<AssignmentFilter>): List<Assignment> {
    val effective = filters.ifEmpty { listOf(AssignmentFilter.All) }
    return effective.flatMap { filter ->
        when (filter) {
            AssignmentFilter.All -> this
            AssignmentFilter.NotYetSubmitted -> filter { assignment ->
                val parentNotSubmitted = !assignment.isSubmitted && assignment.isOnlineSubmissionType
                parentNotSubmitted || assignment.hasAnyCheckpointNotSubmitted()
            }
            AssignmentFilter.ToBeGraded -> filter { assignment ->
                val parentToBeGraded = assignment.isSubmitted && !assignment.isGraded() && assignment.isOnlineSubmissionType
                val checkpointToBeGraded = assignment.hasAnyCheckpointToBeGraded() && assignment.isOnlineSubmissionType
                parentToBeGraded || checkpointToBeGraded
            }
            AssignmentFilter.Graded -> filter { assignment ->
                val checkpointGraded = assignment.hasAnyCheckpointWithGrade() && assignment.isOnlineSubmissionType
                assignment.isGraded() || checkpointGraded
            }
            AssignmentFilter.Other -> filterNot { assignment ->
                val notYetSubmitted = !assignment.isSubmitted && assignment.isOnlineSubmissionType
                val toBeGraded = assignment.isSubmitted && !assignment.isGraded() && assignment.isOnlineSubmissionType
                val graded = assignment.isGraded() && assignment.isOnlineSubmissionType
                val checkpointNotYetSubmitted = assignment.hasAnyCheckpointNotSubmitted()
                val checkpointGraded = assignment.hasAnyCheckpointWithGrade()
                notYetSubmitted || toBeGraded || graded || checkpointNotYetSubmitted || checkpointGraded
            }
            AssignmentFilter.NeedsGrading -> filter { it.needsGradingCount > 0 }
            AssignmentFilter.NotSubmitted -> filter { it.unpublishable }
        }
    }.distinct()
}

fun List<Assignment>.applyStatusFilter(status: AssignmentStatusFilterOption?): List<Assignment> {
    if (status == null) return this
    return when (status) {
        AssignmentStatusFilterOption.All -> this
        AssignmentStatusFilterOption.Published -> filter { it.published }
        AssignmentStatusFilterOption.Unpublished -> filterNot { it.published }
    }
}

fun List<Assignment>.applyGradingPeriodFilter(
    period: GradingPeriod?,
    gradingPeriodsWithAssignments: Map<GradingPeriod, List<Assignment>>,
): List<Assignment> {
    if (period == null || gradingPeriodsWithAssignments.isEmpty()) return this
    val periodAssignmentIds = gradingPeriodsWithAssignments[period].orEmpty().map { it.id }.toSet()
    val periodStart = period.startDate?.toDate()
    val periodEnd = period.endDate?.toDate()
    return filter { assignment ->
        val assignmentInPeriod = assignment.id in periodAssignmentIds
        val checkpointInPeriod = assignment.checkpoints.isNotEmpty() &&
            periodStart != null && periodEnd != null &&
            assignment.checkpoints.any { checkpoint ->
                val due = checkpoint.dueDate
                due != null && !due.before(periodStart) && !due.after(periodEnd)
            }
        assignmentInPeriod || checkpointInPeriod
    }
}

fun Assignment.hasAnyCheckpointWithGrade(): Boolean {
    return if (checkpoints.isNotEmpty()) {
        submission?.subAssignmentSubmissions?.any { sub ->
            sub.grade != null || sub.customGradeStatusId != null
        }.orDefault()
    } else {
        false
    }
}

fun Assignment.hasAnyCheckpointToBeGraded(): Boolean {
    return if (checkpoints.isNotEmpty()) {
        submission?.subAssignmentSubmissions?.let { submissions ->
            checkpoints.any { checkpoint ->
                val checkpointSubmission = submissions.find { it.subAssignmentTag == checkpoint.tag }
                checkpointSubmission?.submittedAt != null &&
                    checkpointSubmission.grade == null &&
                    checkpointSubmission.customGradeStatusId == null
            }
        }.orDefault()
    } else {
        false
    }
}

fun Assignment.hasAnyCheckpointNotSubmitted(): Boolean {
    return if (checkpoints.isNotEmpty()) {
        submission?.subAssignmentSubmissions?.let { submissions ->
            checkpoints.any { checkpoint ->
                val checkpointSubmission = submissions.find { it.subAssignmentTag == checkpoint.tag }
                checkpointSubmission?.submittedAt == null
            }
        } ?: true
    } else {
        false
    }
}

fun Assignment.dueDateIncludingCheckpoints(): Date? {
    return (dueAt ?: orderedCheckpoints.firstOrNull { it.dueAt != null }?.dueAt).toDate()
}
