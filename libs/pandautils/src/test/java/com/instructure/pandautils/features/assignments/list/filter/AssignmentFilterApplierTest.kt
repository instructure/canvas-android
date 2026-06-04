/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.assignments.list.filter

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.SubAssignmentSubmission
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.Const
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.util.Date

class AssignmentFilterApplierTest {

    private val onlineSubmissionType = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
    private val discussionSubmissionType = listOf(Assignment.SubmissionType.DISCUSSION_TOPIC.apiString)

    // region applyAssignmentTypeFilters

    @Test
    fun `applyAssignmentTypeFilters with empty list defaults to All and returns all assignments`() {
        val assignment1 = Assignment(id = 1, name = "A1")
        val assignment2 = Assignment(id = 2, name = "A2")
        val assignments = listOf(assignment1, assignment2)

        val result = assignments.applyAssignmentTypeFilters(emptyList())

        assertEquals(assignments, result)
    }

    @Test
    fun `applyAssignmentTypeFilters with All returns all assignments`() {
        val assignment1 = Assignment(id = 1, name = "A1")
        val assignment2 = Assignment(id = 2, name = "A2")
        val assignments = listOf(assignment1, assignment2)

        val result = assignments.applyAssignmentTypeFilters(listOf(AssignmentFilter.All))

        assertEquals(assignments, result)
    }

    @Test
    fun `applyAssignmentTypeFilters with NotYetSubmitted returns assignments not yet submitted`() {
        val notSubmitted = Assignment(
            id = 1,
            name = "Not submitted",
            submissionTypesRaw = onlineSubmissionType
        )
        val submitted = Assignment(
            id = 2,
            name = "Submitted",
            submission = Submission(submittedAt = Date()),
            submissionTypesRaw = onlineSubmissionType
        )
        val assignments = listOf(notSubmitted, submitted)

        val result = assignments.applyAssignmentTypeFilters(listOf(AssignmentFilter.NotYetSubmitted))

        assertEquals(listOf(notSubmitted), result)
    }

    @Test
    fun `applyAssignmentTypeFilters with ToBeGraded returns submitted but not graded assignments`() {
        val toBeGraded = Assignment(
            id = 1,
            name = "To be graded",
            submission = Submission(submittedAt = Date(), workflowState = "submitted"),
            submissionTypesRaw = onlineSubmissionType
        )
        val graded = Assignment(
            id = 2,
            name = "Graded",
            submission = Submission(submittedAt = Date(), grade = "A", postedAt = Date()),
            submissionTypesRaw = onlineSubmissionType
        )
        val notSubmitted = Assignment(
            id = 3,
            name = "Not submitted",
            submissionTypesRaw = onlineSubmissionType
        )
        val assignments = listOf(toBeGraded, graded, notSubmitted)

        val result = assignments.applyAssignmentTypeFilters(listOf(AssignmentFilter.ToBeGraded))

        assertEquals(listOf(toBeGraded), result)
    }

    @Test
    fun `applyAssignmentTypeFilters with Graded returns graded assignments`() {
        val graded = Assignment(
            id = 1,
            name = "Graded",
            submission = Submission(submittedAt = Date(), grade = "A", postedAt = Date()),
            submissionTypesRaw = onlineSubmissionType
        )
        val notGraded = Assignment(
            id = 2,
            name = "Not graded",
            submission = Submission(submittedAt = Date(), workflowState = "submitted"),
            submissionTypesRaw = onlineSubmissionType
        )
        val assignments = listOf(graded, notGraded)

        val result = assignments.applyAssignmentTypeFilters(listOf(AssignmentFilter.Graded))

        assertEquals(listOf(graded), result)
    }

    @Test
    fun `applyAssignmentTypeFilters with Other returns assignments that don't match standard filters`() {
        val graded = Assignment(
            id = 1,
            name = "Graded",
            submission = Submission(submittedAt = Date(), grade = "A", postedAt = Date()),
            submissionTypesRaw = onlineSubmissionType
        )
        val notSubmitted = Assignment(
            id = 2,
            name = "Not submitted",
            submissionTypesRaw = onlineSubmissionType
        )
        val other = Assignment(
            id = 3,
            name = "Other",
            submissionTypesRaw = listOf(Assignment.SubmissionType.NONE.apiString)
        )
        val assignments = listOf(graded, notSubmitted, other)

        val result = assignments.applyAssignmentTypeFilters(listOf(AssignmentFilter.Other))

        assertEquals(listOf(other), result)
    }

    @Test
    fun `applyAssignmentTypeFilters with NeedsGrading returns assignments with needsGradingCount over zero`() {
        val needsGrading = Assignment(id = 1, name = "Needs grading", needsGradingCount = 2)
        val noNeedsGrading = Assignment(id = 2, name = "No needs grading", needsGradingCount = 0)
        val assignments = listOf(needsGrading, noNeedsGrading)

        val result = assignments.applyAssignmentTypeFilters(listOf(AssignmentFilter.NeedsGrading))

        assertEquals(listOf(needsGrading), result)
    }

    @Test
    fun `applyAssignmentTypeFilters with NotSubmitted returns unpublishable assignments`() {
        val unpublishable = Assignment(id = 1, name = "Unpublishable", unpublishable = true)
        val published = Assignment(id = 2, name = "Published", unpublishable = false)
        val assignments = listOf(unpublishable, published)

        val result = assignments.applyAssignmentTypeFilters(listOf(AssignmentFilter.NotSubmitted))

        assertEquals(listOf(unpublishable), result)
    }

    @Test
    fun `applyAssignmentTypeFilters with multiple filters combines results and removes duplicates`() {
        val graded = Assignment(
            id = 1,
            name = "Graded",
            submission = Submission(submittedAt = Date(), grade = "A", postedAt = Date()),
            submissionTypesRaw = onlineSubmissionType
        )
        val notSubmitted = Assignment(
            id = 2,
            name = "Not submitted",
            submissionTypesRaw = onlineSubmissionType
        )
        val assignments = listOf(graded, notSubmitted)

        val result = assignments.applyAssignmentTypeFilters(
            listOf(AssignmentFilter.All, AssignmentFilter.Graded, AssignmentFilter.NotYetSubmitted)
        )

        assertEquals(listOf(graded, notSubmitted), result)
    }

    // endregion

    // region applyStatusFilter

    @Test
    fun `applyStatusFilter with null returns the original list`() {
        val assignment1 = Assignment(id = 1, name = "A1", published = true)
        val assignment2 = Assignment(id = 2, name = "A2", published = false)
        val assignments = listOf(assignment1, assignment2)

        val result = assignments.applyStatusFilter(null)

        assertEquals(assignments, result)
    }

    @Test
    fun `applyStatusFilter with All returns all assignments`() {
        val assignment1 = Assignment(id = 1, name = "A1", published = true)
        val assignment2 = Assignment(id = 2, name = "A2", published = false)
        val assignments = listOf(assignment1, assignment2)

        val result = assignments.applyStatusFilter(AssignmentStatusFilterOption.All)

        assertEquals(assignments, result)
    }

    @Test
    fun `applyStatusFilter with Published returns only published assignments`() {
        val published = Assignment(id = 1, name = "Published", published = true)
        val unpublished = Assignment(id = 2, name = "Unpublished", published = false)
        val assignments = listOf(published, unpublished)

        val result = assignments.applyStatusFilter(AssignmentStatusFilterOption.Published)

        assertEquals(listOf(published), result)
    }

    @Test
    fun `applyStatusFilter with Unpublished returns only unpublished assignments`() {
        val published = Assignment(id = 1, name = "Published", published = true)
        val unpublished = Assignment(id = 2, name = "Unpublished", published = false)
        val assignments = listOf(published, unpublished)

        val result = assignments.applyStatusFilter(AssignmentStatusFilterOption.Unpublished)

        assertEquals(listOf(unpublished), result)
    }

    // endregion

    // region applyGradingPeriodFilter

    @Test
    fun `applyGradingPeriodFilter with null period returns the original list`() {
        val assignment = Assignment(id = 1, name = "A1")
        val assignments = listOf(assignment)
        val period = GradingPeriod(id = 1, title = "Fall")

        val result = assignments.applyGradingPeriodFilter(
            period = null,
            gradingPeriodsWithAssignments = mapOf(period to listOf(assignment))
        )

        assertEquals(assignments, result)
    }

    @Test
    fun `applyGradingPeriodFilter with empty map returns the original list`() {
        val assignment = Assignment(id = 1, name = "A1")
        val assignments = listOf(assignment)
        val period = GradingPeriod(id = 1, title = "Fall")

        val result = assignments.applyGradingPeriodFilter(
            period = period,
            gradingPeriodsWithAssignments = emptyMap()
        )

        assertEquals(assignments, result)
    }

    @Test
    fun `applyGradingPeriodFilter includes assignments whose id is in the period mapping`() {
        val inPeriod = Assignment(id = 1, name = "In period")
        val outOfPeriod = Assignment(id = 2, name = "Out of period")
        val period = GradingPeriod(id = 10, title = "Fall")

        val result = listOf(inPeriod, outOfPeriod).applyGradingPeriodFilter(
            period = period,
            gradingPeriodsWithAssignments = mapOf(period to listOf(inPeriod))
        )

        assertEquals(listOf(inPeriod), result)
    }

    @Test
    fun `applyGradingPeriodFilter includes assignments with checkpoints whose dueDate falls within the period`() {
        val period = GradingPeriod(
            id = 10,
            title = "Fall",
            startDate = "2025-09-01T00:00:00Z",
            endDate = "2025-10-31T23:59:59Z"
        )
        val assignmentWithCheckpointInPeriod = Assignment(
            id = 1,
            name = "DCP in period",
            submissionTypesRaw = discussionSubmissionType,
            checkpoints = listOf(
                Checkpoint(tag = Const.REPLY_TO_TOPIC, dueAt = "2025-09-15T12:00:00Z")
            )
        )
        val assignmentWithCheckpointOutOfPeriod = Assignment(
            id = 2,
            name = "DCP out of period",
            submissionTypesRaw = discussionSubmissionType,
            checkpoints = listOf(
                Checkpoint(tag = Const.REPLY_TO_TOPIC, dueAt = "2025-12-01T12:00:00Z")
            )
        )

        val result = listOf(assignmentWithCheckpointInPeriod, assignmentWithCheckpointOutOfPeriod)
            .applyGradingPeriodFilter(
                period = period,
                gradingPeriodsWithAssignments = mapOf(period to emptyList())
            )

        assertEquals(listOf(assignmentWithCheckpointInPeriod), result)
    }

    // endregion

    // region hasAnyCheckpointWithGrade

    @Test
    fun `hasAnyCheckpointWithGrade returns false when there are no checkpoints`() {
        val assignment = Assignment(id = 1, name = "A1")

        assertFalse(assignment.hasAnyCheckpointWithGrade())
    }

    @Test
    fun `hasAnyCheckpointWithGrade returns true when at least one sub-assignment submission has a grade`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(Checkpoint(tag = Const.REPLY_TO_TOPIC)),
            submission = Submission(
                subAssignmentSubmissions = arrayListOf(
                    SubAssignmentSubmission(grade = "A", subAssignmentTag = Const.REPLY_TO_TOPIC)
                )
            )
        )

        assertTrue(assignment.hasAnyCheckpointWithGrade())
    }

    @Test
    fun `hasAnyCheckpointWithGrade returns true when sub-assignment submission has a custom grade status`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(Checkpoint(tag = Const.REPLY_TO_TOPIC)),
            submission = Submission(
                subAssignmentSubmissions = arrayListOf(
                    SubAssignmentSubmission(
                        grade = null,
                        customGradeStatusId = 42,
                        subAssignmentTag = Const.REPLY_TO_TOPIC
                    )
                )
            )
        )

        assertTrue(assignment.hasAnyCheckpointWithGrade())
    }

    @Test
    fun `hasAnyCheckpointWithGrade returns false when no sub-assignment submission has a grade or custom status`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(Checkpoint(tag = Const.REPLY_TO_TOPIC)),
            submission = Submission(
                subAssignmentSubmissions = arrayListOf(
                    SubAssignmentSubmission(grade = null, subAssignmentTag = Const.REPLY_TO_TOPIC)
                )
            )
        )

        assertFalse(assignment.hasAnyCheckpointWithGrade())
    }

    // endregion

    // region hasAnyCheckpointToBeGraded

    @Test
    fun `hasAnyCheckpointToBeGraded returns false when there are no checkpoints`() {
        val assignment = Assignment(id = 1, name = "A1")

        assertFalse(assignment.hasAnyCheckpointToBeGraded())
    }

    @Test
    fun `hasAnyCheckpointToBeGraded returns true when a checkpoint is submitted but not graded`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(Checkpoint(tag = Const.REPLY_TO_TOPIC)),
            submission = Submission(
                subAssignmentSubmissions = arrayListOf(
                    SubAssignmentSubmission(
                        grade = null,
                        customGradeStatusId = null,
                        subAssignmentTag = Const.REPLY_TO_TOPIC,
                        submittedAt = Date()
                    )
                )
            )
        )

        assertTrue(assignment.hasAnyCheckpointToBeGraded())
    }

    @Test
    fun `hasAnyCheckpointToBeGraded returns false when all submitted checkpoints already have a grade`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(Checkpoint(tag = Const.REPLY_TO_TOPIC)),
            submission = Submission(
                subAssignmentSubmissions = arrayListOf(
                    SubAssignmentSubmission(
                        grade = "A",
                        subAssignmentTag = Const.REPLY_TO_TOPIC,
                        submittedAt = Date()
                    )
                )
            )
        )

        assertFalse(assignment.hasAnyCheckpointToBeGraded())
    }

    // endregion

    // region hasAnyCheckpointNotSubmitted

    @Test
    fun `hasAnyCheckpointNotSubmitted returns false when there are no checkpoints`() {
        val assignment = Assignment(id = 1, name = "A1")

        assertFalse(assignment.hasAnyCheckpointNotSubmitted())
    }

    @Test
    fun `hasAnyCheckpointNotSubmitted returns true when there is no submission at all`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(Checkpoint(tag = Const.REPLY_TO_TOPIC)),
            submission = null
        )

        assertTrue(assignment.hasAnyCheckpointNotSubmitted())
    }

    @Test
    fun `hasAnyCheckpointNotSubmitted returns true when at least one checkpoint has no submittedAt`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(
                Checkpoint(tag = Const.REPLY_TO_TOPIC),
                Checkpoint(tag = Const.REPLY_TO_ENTRY)
            ),
            submission = Submission(
                subAssignmentSubmissions = arrayListOf(
                    SubAssignmentSubmission(
                        subAssignmentTag = Const.REPLY_TO_TOPIC,
                        submittedAt = Date()
                    ),
                    SubAssignmentSubmission(
                        subAssignmentTag = Const.REPLY_TO_ENTRY,
                        submittedAt = null
                    )
                )
            )
        )

        assertTrue(assignment.hasAnyCheckpointNotSubmitted())
    }

    @Test
    fun `hasAnyCheckpointNotSubmitted returns false when all checkpoints have a submission`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            checkpoints = listOf(
                Checkpoint(tag = Const.REPLY_TO_TOPIC),
                Checkpoint(tag = Const.REPLY_TO_ENTRY)
            ),
            submission = Submission(
                subAssignmentSubmissions = arrayListOf(
                    SubAssignmentSubmission(
                        subAssignmentTag = Const.REPLY_TO_TOPIC,
                        submittedAt = Date()
                    ),
                    SubAssignmentSubmission(
                        subAssignmentTag = Const.REPLY_TO_ENTRY,
                        submittedAt = Date()
                    )
                )
            )
        )

        assertFalse(assignment.hasAnyCheckpointNotSubmitted())
    }

    // endregion

    // region dueDateIncludingCheckpoints

    @Test
    fun `dueDateIncludingCheckpoints returns the assignment dueAt when present`() {
        val dueAt = Date()
        val assignment = Assignment(
            id = 1,
            name = "A1",
            dueAt = dueAt.toApiString()
        )

        val result = assignment.dueDateIncludingCheckpoints()

        assertEquals(dueAt.toApiString(), result.toApiString())
    }

    @Test
    fun `dueDateIncludingCheckpoints falls back to first checkpoint dueAt when assignment dueAt is null`() {
        val checkpointDueAt = "2025-09-15T12:00:00Z"
        val assignment = Assignment(
            id = 1,
            name = "A1",
            dueAt = null,
            checkpoints = listOf(
                Checkpoint(tag = Const.REPLY_TO_TOPIC, dueAt = checkpointDueAt),
                Checkpoint(tag = Const.REPLY_TO_ENTRY, dueAt = "2025-11-01T12:00:00Z")
            )
        )

        val result = assignment.dueDateIncludingCheckpoints()

        assertEquals(checkpointDueAt.toDate(), result)
    }

    @Test
    fun `dueDateIncludingCheckpoints returns null when neither assignment nor checkpoints have a due date`() {
        val assignment = Assignment(
            id = 1,
            name = "A1",
            dueAt = null,
            checkpoints = emptyList()
        )

        val result = assignment.dueDateIncludingCheckpoints()

        assertNull(result)
    }

    // endregion
}
