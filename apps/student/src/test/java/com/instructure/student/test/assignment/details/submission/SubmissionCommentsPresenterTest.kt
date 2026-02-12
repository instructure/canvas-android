/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */
package com.instructure.student.test.assignment.details.submission

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Author
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsPresenter
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class SubmissionCommentsPresenterTest : Assert() {
    private lateinit var context: Context
    private lateinit var user: User
    private lateinit var teacher: User
    private lateinit var baseModel: SubmissionCommentsModel
    private lateinit var baseAssignment:  Assignment
    private lateinit var baseSubmission: Submission
    private lateinit var submissionComment: SubmissionComment
    private lateinit var presenter: SubmissionCommentsPresenter

    private val studentDb: StudentDb = mockk(relaxed = true)

    @Before
    fun setup() {
        // Set up our context
        context = ApplicationProvider.getApplicationContext()

        presenter = SubmissionCommentsPresenter(studentDb)

        // Set up our user(s)
        user = User(id=100,name="Bart Simpson",shortName="Bart",avatarUrl="BartAvatarUrl")
        ApiPrefs.user = user
        teacher = User(id=101,name="Edna Krabapple",shortName="Edna",avatarUrl="EdnaAvatarUrl")

        // Set up our base models
        baseSubmission = Submission(
                attempt = 1,
                workflowState = "submitted",
                submissionType = "Type A"
        )
        baseAssignment = Assignment(
                id = 123,
                name = "Assignment Name",
                description = "This is a description",
                pointsPossible = 35.0
        )

        submissionComment = SubmissionComment(
                id=1,
                author=Author(displayName=user.name,avatarImageUrl=user.avatarUrl),
                authorId = user.id,
                comment="Aye Carumba!",
                createdAt = Date(),
                attempt = 1
        )

        baseModel = SubmissionCommentsModel(
                assignment = baseAssignment,
                comments = listOf(submissionComment),
                submissionHistory = listOf(baseSubmission),
                attemptId = 1,
                assignmentEnhancementsEnabled = true
        )

    }

    @Test
    fun `Returns enableFilesButton true when model isFileButtonEnabledButton is true`() {
        val model = baseModel.copy(isFileButtonEnabled = true)
        val actualState = presenter.present(model, context)
        assertTrue(actualState.enableFilesButton)
    }

    @Test
    fun `Returns enableFilesButton false when model isFileButtonEnabledButton is false`() {
        val model = baseModel.copy(isFileButtonEnabled = false)
        val actualState = presenter.present(model, context)
        assertFalse(actualState.enableFilesButton)
    }

    @Test
    fun `Returns enableFilesButton false when user not set`() {
        ApiPrefs.user = null
        val model = baseModel.copy(isFileButtonEnabled = true)
        val actualState = presenter.present(model, context)
        assertFalse(actualState.enableFilesButton)
    }

    @Test
    fun `Returns CommentItem states if comments in model`() {
        val model = baseModel.copy()
        val actualState = presenter.present(model, context)
        assertTrue(actualState.commentStates.filter {it is CommentItemState.CommentItem}.count() > 0)
    }

    @Test
    fun `Returns no CommentItem states if no comments in model`() {
        val model = baseModel.copy(comments = emptyList())
        val actualState = presenter.present(model, context)
        assertTrue(actualState.commentStates.filter {it is CommentItemState.CommentItem}.count() == 0)
    }

    @Test
    fun `Returns SubmissionItem states if submission in model`() {
        val model = baseModel.copy()
        val actualState = presenter.present(model, context)
        assertTrue(actualState.commentStates.filter {it is CommentItemState.SubmissionItem}.count() > 0)
    }

    @Test
    fun `Returns no SubmissionItem states if no submissions in model`() {
        val model = baseModel.copy( submissionHistory = emptyList())
        val actualState = presenter.present(model, context)
        assertTrue(actualState.commentStates.filter {it is CommentItemState.SubmissionItem}.count() == 0)
    }

    @Test
    fun `Returns isAudience true for comments by other user`() {
        val model = baseModel.copy( comments = listOf(submissionComment.copy(authorId = teacher.id)))
        val actualState = presenter.present(model, context)
        assertTrue( (actualState.commentStates.filter {it is CommentItemState.CommentItem}.get(0) as CommentItemState.CommentItem).isAudience)
    }

    @Test
    fun `Returns isAudience false for comments by user`() {
        val model = baseModel.copy()
        val actualState = presenter.present(model, context)
        assertFalse( (actualState.commentStates.filter {it is CommentItemState.CommentItem}.get(0) as CommentItemState.CommentItem).isAudience)
    }

    @Test
    fun `Returns no SubmissionItem states for submissions with null submission type`() {
        val model = baseModel.copy(submissionHistory = listOf(baseSubmission.copy(submissionType = null)))
        val actualState = presenter.present(model, context)
        assertTrue(actualState.commentStates.filter {it is CommentItemState.SubmissionItem}.count() == 0)
    }

    @Test
    fun `Returns no SubmissionItem states for submissions with workflow state unsubmitted`() {
        val model = baseModel.copy(submissionHistory = listOf(baseSubmission.copy(workflowState = "unsubmitted")))
        val actualState = presenter.present(model, context)
        assertTrue(actualState.commentStates.filter {it is CommentItemState.SubmissionItem}.count() == 0)
    }

    @Test
    fun `Returns properly sorted commentStates`() {
        // Feather some comments and submissions together so they are jumbled time-wise
        val comment1 = submissionComment.copy(id=2, createdAt = Date(119,7,4, 15, 22), attempt = 1)
        val comment2 = submissionComment.copy(id=3, createdAt = Date(119,7,4, 16, 4), attempt = 1)
        val submission1 = baseSubmission.copy(attempt = 1, submittedAt = Date(119, 7, 4, 15, 27))
        val submission2 = baseSubmission.copy(attempt = 2, submittedAt = Date(119, 7, 4, 16, 12))

        val model = baseModel.copy(
                submissionHistory = listOf(submission1,submission2),
                comments = listOf(comment1,comment2)
        )

        val actualState = presenter.present(model, context)

        assertEquals(3, actualState.commentStates.count())
        for(i in 1 until actualState.commentStates.count()) {
            val before = actualState.commentStates[i] // Should be sorted in descending order, so latest come first
            val beforeDate = dateFromCommentState(before)
            val after = actualState.commentStates[i-1]
            val afterDate = dateFromCommentState(after)
            assertTrue(beforeDate.before(afterDate))
            println("afterDate=$afterDate,beforeDate=$beforeDate") // sanity check
        }
    }

    @Test
    fun `Always show comments when attemptId is null`() {
        val comments = listOf(
            submissionComment,
            SubmissionComment(id=1, comment="No attempt", attempt = null)
        )
        val model = baseModel.copy(comments = comments)
        val actualState = presenter.present(model, context)
        assertEquals(3, actualState.commentStates.size)
        assertEquals("No attempt", (actualState.commentStates[1] as CommentItemState.CommentItem).message)
    }

    @Test
    fun `Shows attempt 0 comments when attemptId is 1 and assignmentEnhancementsEnabled is true`() {
        val comments = listOf(
            submissionComment.copy(id = 1, comment = "Attempt 0 comment", attempt = 0),
            submissionComment.copy(id = 2, comment = "Attempt 1 comment", attempt = 1)
        )
        val model = baseModel.copy(comments = comments, attemptId = 1, assignmentEnhancementsEnabled = true)
        val actualState = presenter.present(model, context)
        val commentItems = actualState.commentStates.filterIsInstance<CommentItemState.CommentItem>()
        assertEquals(2, commentItems.size)
        assertEquals(commentItems.map { it.message }, listOf("Attempt 0 comment", "Attempt 1 comment"))
    }

    @Test
    fun `Does not show attempt 0 comments when attemptId is 2 and assignmentEnhancementsEnabled is true`() {
        val comments = listOf(
            submissionComment.copy(id = 1, comment = "Attempt 0 comment", attempt = 0),
            submissionComment.copy(id = 2, comment = "Attempt 2 comment", attempt = 2)
        )
        val model = baseModel.copy(comments = comments, attemptId = 2, assignmentEnhancementsEnabled = true)
        val actualState = presenter.present(model, context)
        val commentItems = actualState.commentStates.filterIsInstance<CommentItemState.CommentItem>()
        assertEquals(1, commentItems.size)
        assertEquals("Attempt 2 comment", commentItems[0].message)
    }

    @Test
    fun `Shows all comments when assignmentEnhancementsEnabled is false`() {
        val comments = listOf(
            submissionComment.copy(id = 1, comment = "Attempt 0 comment", attempt = 0),
            submissionComment.copy(id = 2, comment = "Attempt 1 comment", attempt = 1),
            submissionComment.copy(id = 3, comment = "Attempt 2 comment", attempt = 2)
        )
        val model = baseModel.copy(comments = comments, attemptId = 1, assignmentEnhancementsEnabled = false)
        val actualState = presenter.present(model, context)
        val commentItems = actualState.commentStates.filterIsInstance<CommentItemState.CommentItem>()
        assertEquals(3, commentItems.size)
    }

    private fun dateFromCommentState(state: CommentItemState) : Date {
        return when(state) {
            is CommentItemState.CommentItem -> state.sortDate
            is CommentItemState.SubmissionItem -> state.sortDate
            is CommentItemState.PendingCommentItem -> state.sortDate
            else -> throw IllegalArgumentException("Unexpected comment item state type ${state::class.java.simpleName}")
        }
    }

}