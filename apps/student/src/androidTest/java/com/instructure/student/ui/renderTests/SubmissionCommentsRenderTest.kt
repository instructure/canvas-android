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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.PendingSubmissionComment
import com.instructure.student.db.sqlColAdapters.Date
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.CommentItemState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsViewState
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.SubmissionCommentsFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.ui.pages.renderPages.SubmissionCommentsRenderPage
import com.spotify.mobius.runners.WorkRunner
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

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

    val page = SubmissionCommentsRenderPage()

    @Before
    fun setUp() {
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
                avatarUrl = user.avatarUrl!!,
                sortDate = Date(119, 6, 4, 13, 20),
                pendingComment = object : PendingSubmissionComment {
                    override val id: Long
                        get() = 15L
                    override val accountDomain = "myDomain.com"
                    override val assignmentId
                        get() = 42L
                    override val assignmentName: String
                        get() = "Special assignment"
                    override val canvasContext: CanvasContext
                        get() = CanvasContext.defaultCanvasContext()
                    override val currentFile: Long
                        get() = 12
                    override val errorFlag: Boolean
                        get() = false
                    override val fileCount: Long
                        get() = 0
                    override val isGroupMessage: Boolean
                        get() = false
                    override val lastActivityDate: Date
                        get() = Date.now()
                    override val mediaPath: String?
                        get() = "/blah/blah/blah"
                    override val message: String?
                        get() = "Pending message"
                    override val progress: Double?
                        get() = 0.45

                }
        )
    }

    @Test
    @TestMetaData(Priority.P1,FeatureCategory.COMMENTS,TestCategory.RENDER,secondaryFeature = FeatureCategory.ASSIGNMENTS)
    fun testSingleComment() {
        val state = SubmissionCommentsViewState(
            commentStates = listOf(commentItemIsAudience),
            enableFilesButton = true
        )
        loadPageWithViewState(state)
        page.verifyCommentPresent(commentItemIsAudience)

    }

    @Test
    @TestMetaData(Priority.P1,FeatureCategory.COMMENTS,TestCategory.RENDER,secondaryFeature = FeatureCategory.ASSIGNMENTS)
    fun testSingleSubmission() {
        val state = SubmissionCommentsViewState(
                commentStates = listOf(submissionItem)
        )
        loadPageWithViewState(state)
        page.verifyCommentPresent(submissionItem)
    }

    // I'm not going to attempt to test a pending comment right now.  The logic to display
    // the pending comment is just too convoluted for a render test -- need to mock db, set up
    // listeners, etc...
//    @Test
//    fun testSinglePendingComment() {
//        val state = SubmissionCommentsViewState(
//                commentStates = listOf(pendingCommentItem)
//        )
//        loadPageWithViewState(state)
//        page.verifyCommentPresent(pendingCommentItem)
//    }

    @Test
    @TestMetaData(Priority.P1,FeatureCategory.COMMENTS,TestCategory.RENDER,secondaryFeature = FeatureCategory.ASSIGNMENTS)
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
    @TestMetaData(Priority.P1,FeatureCategory.COMMENTS,TestCategory.RENDER,secondaryFeature = FeatureCategory.ASSIGNMENTS)
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
        val fragment = SubmissionCommentsFragment().apply {
            overrideInitViewState = state
            loopMod = { it.effectRunner { emptyEffectRunner } }
            data = SubmissionDetailsTabData.CommentData( // ?? I don't know what this does, but I need to provide it
                    name = "Name",
                    assignment = baseAssignment,
                    submission = baseSubmission
            )

        }
        activityRule.activity.loadFragment(fragment)
        page.assertPageObjects()
    }

}