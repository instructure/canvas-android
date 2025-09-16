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
 */
package com.instructure.student.ui.renderTests

import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.studentdb.StudentDb
import com.instructure.pandautils.room.studentdb.entities.CreatePendingSubmissionCommentEntity
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsViewState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.SubmissionCommentsFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.ui.renderTests.renderPages.SubmissionCommentsRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SubmissionCommentsRenderTest: StudentRenderTest() {
    private lateinit var user: User
    private lateinit var teacher: User
    private lateinit var baseAssignment: Assignment
    private lateinit var baseSubmission: Submission
    private lateinit var commentItemNotAudience: CommentItemState.CommentItem
    private lateinit var commentItemIsAudience: CommentItemState.CommentItem
    private lateinit var submissionItem: CommentItemState.SubmissionItem
    private lateinit var pendingCommentItem: CommentItemState.PendingCommentItem

    @Inject
    lateinit var db: StudentDb

    val page = SubmissionCommentsRenderPage()

    @Before
    fun setUp() = runTest {
        user = User(id=100,name="Bart Simpson",shortName="Bart",avatarUrl="BartAvatarUrl")
        teacher = User(id=101,name="Edna Krabapple",shortName="Edna",avatarUrl="EdnaAvatarUrl")

        // Set up our base models
        baseSubmission = Submission(
                attempt = 1,
                workflowState = "submitted",
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                body = "I'm a submission!"
        )
        baseAssignment = Assignment(
                id = 123,
                name = "Assignment Name",
                description = "This is a description",
                pointsPossible = 35.0
        )

        commentItemNotAudience = CommentItemState.CommentItem(
                authorName = user.shortName!!,
                authorPronouns = null,
                id = 1,
                avatarUrl = user.avatarUrl!!,
                sortDate = Date(119,6,4,16,25),
                dateText = "Date 1",
                message = "Giddy-up!",
                isAudience = false,
                tint = 42
        )

        commentItemIsAudience = CommentItemState.CommentItem(
                authorName = teacher.shortName!!,
                authorPronouns = null,
                id = 2,
                avatarUrl = teacher.avatarUrl!!,
                sortDate = Date(119,6,4,17,25),
                dateText = "Date 2",
                message = "Yippy-ay-oh-kay-ay!",
                isAudience = true,
                tint = 112
        )

        submissionItem = CommentItemState.SubmissionItem(
                authorName = user.shortName!!,
                authorPronouns = null,
                avatarUrl = user.avatarUrl!!,
                sortDate = Date(119,6,4,16,50),
                dateText = "Date 3",
                submission = baseSubmission,
                tint = 1000
        )

        // Just another example of how painful it is to test pending comments.
        // This is dependent on a generated PendingSubmissionComment interface.
        pendingCommentItem = CommentItemState.PendingCommentItem(
                authorName = user.shortName!!,
                authorPronouns = null,
                avatarUrl = user.avatarUrl!!,
                sortDate = Date(119, 6, 4, 13, 20),
                pendingComment = makePendingComment(
                        accountDomain = "myDomain.com",
                        assignmentId = 42,
                        assignmentName = "Special assignment",
                        canvasContext = CanvasContext.defaultCanvasContext(),
                        isGroupMessage = false,
                        lastActivityDate = Date(),
                        mediaPath = "/media/path",
                        message = "Pending Message",
                        attemptId = 1
                )
        )
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testSingleComment() {
        val state = SubmissionCommentsViewState(
            commentStates = listOf(commentItemIsAudience),
            enableFilesButton = true
        )
        loadPageWithViewState(state)
        page.verifyCommentPresent(commentItemIsAudience)

    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testSingleSubmission() {
        val state = SubmissionCommentsViewState(
                commentStates = listOf(submissionItem)
        )
        loadPageWithViewState(state)
        page.verifyCommentPresent(submissionItem)
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testSinglePendingComment() {
        val state = SubmissionCommentsViewState(
            commentStates = listOf(pendingCommentItem)
        )
        loadPageWithViewState(state)
        page.verifyPendingCommentPresent(pendingCommentItem, failed = false)
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testSingleCommentDisplaysAuthorPronoun() {
        val commentItem = commentItemIsAudience.copy(authorPronouns = "Pro/Noun")
        val state = SubmissionCommentsViewState(commentStates = listOf(commentItem))
        loadPageWithViewState(state)
        page.verifyDisplaysPronoun(commentItem)
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testSingleSubmissionDisplaysAuthorPronoun() {
        val commentItem = submissionItem.copy(authorPronouns = "Pro/Noun")
        val state = SubmissionCommentsViewState(commentStates = listOf(commentItem))
        loadPageWithViewState(state)
        page.verifyDisplaysPronoun(commentItem)
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testSinglePendingCommentDisplaysAuthorPronoun() {
        val commentItem = pendingCommentItem.copy(authorPronouns = "Pro/Noun")
        val state = SubmissionCommentsViewState(commentStates = listOf(commentItem))
        loadPageWithViewState(state)
        page.verifyDisplaysPronoun(commentItem)
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testEmptyState() {
        val state = SubmissionCommentsViewState(
            commentStates = listOf(CommentItemState.Empty)
        )
        loadPageWithViewState(state)
        page.verifyDisplaysEmptyState()
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testFailedCommentDisplaysRetryAndDeleteOptions() = runTest {
        db.pendingSubmissionCommentDao().setCommentError(true, pendingCommentItem.pendingComment.id)
        val state = SubmissionCommentsViewState(
            commentStates = listOf(pendingCommentItem)
        )
        loadPageWithViewState(state)
        page.verifyPendingCommentPresent(pendingCommentItem, failed = true)
        page.verifyDisplaysRetryOptions()
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testMixedCommentsAndSubmission() {
        val state = SubmissionCommentsViewState(
                commentStates = listOf(commentItemIsAudience, submissionItem, commentItemNotAudience)
        )
        loadPageWithViewState(state)
        page.verifyCommentPresent(commentItemIsAudience)
        page.verifyCommentPresent(submissionItem)
        page.verifyCommentPresent(commentItemNotAudience)

        // Interact to make Accessibility tests kick in
        page.clickInCommentBox()
        Espresso.closeSoftKeyboard()
    }

    @Test
    @TestMetaData(
        Priority.COMMON,
        FeatureCategory.ASSIGNMENTS,
        TestCategory.RENDER,secondaryFeature = SecondaryFeatureCategory.ASSIGNMENT_COMMENTS)
    fun testAudienceDistinction() {
        val state = SubmissionCommentsViewState(
                commentStates = listOf(commentItemIsAudience, commentItemNotAudience)
        )
        loadPageWithViewState(state)
        val audienceLeft = page.getCommentLeftOffset(commentItemIsAudience)
        val notAudienceLeft = page.getCommentLeftOffset(commentItemNotAudience)
        //println("audience left = $audienceLeft, me left = $notAudienceLeft")
        assertTrue(audienceLeft < notAudienceLeft)
    }

    private fun loadPageWithViewState(state: SubmissionCommentsViewState) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val data = SubmissionDetailsTabData.CommentData( // ?? I don't know what this does, but I need to provide it
            name = "Name",
            assignment = baseAssignment,
            submission = baseSubmission,
            attemptId = 1,
            true
        )
        val fragment = SubmissionCommentsFragment.newInstance(data).apply {
            overrideInitViewState = state
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
        page.assertPageObjects()
    }

    @Suppress("SameParameterValue")
    private suspend fun makePendingComment(
        accountDomain: String,
        canvasContext: CanvasContext,
        assignmentName: String,
        assignmentId: Long,
        lastActivityDate: Date,
        isGroupMessage: Boolean,
        message: String?,
        mediaPath: String?,
        attemptId: Long?
    ): CreatePendingSubmissionCommentEntity {
        val rowId = db.pendingSubmissionCommentDao().insert(
            CreatePendingSubmissionCommentEntity(
                accountDomain = accountDomain,
                canvasContext = canvasContext,
                assignmentName = assignmentName,
                assignmentId = assignmentId,
                lastActivityDate = lastActivityDate,
                isGroupMessage = isGroupMessage,
                message = message,
                mediaPath = mediaPath,
                attemptId = attemptId
            )
        )
        val id = db.pendingSubmissionCommentDao().findIdByRowId(rowId)
        val comment = db.pendingSubmissionCommentDao().findCommentById(id)!!
        return comment
    }
}
