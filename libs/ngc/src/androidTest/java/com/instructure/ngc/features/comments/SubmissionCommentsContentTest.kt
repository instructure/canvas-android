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
package com.instructure.ngc.features.comments

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.instui.compose.InstUITheme
import com.instructure.ngc.R
import com.instructure.ngc.features.comments.domain.SubmissionCommentAttachment
import com.instructure.ngc.features.comments.domain.SubmissionCommentItem
import com.instructure.ngc.features.comments.domain.SubmissionCommentMedia
import com.instructure.ngc.features.comments.domain.SubmissionCommentMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionCommentsContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val courseColor = Color(0xFF2573DF)

    private fun setContent(
        state: SubmissionCommentsUiState,
        onOpenAttachment: (SubmissionCommentAttachment) -> Unit = {},
        onOpenMedia: (SubmissionCommentMedia) -> Unit = {},
    ) {
        composeTestRule.setContent {
            InstUITheme(courseColor = courseColor) {
                SubmissionCommentsContent(
                    state = state,
                    onOpenAttachment = onOpenAttachment,
                    onOpenMedia = onOpenMedia,
                )
            }
        }
    }

    private fun string(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    @Test
    fun showsLoadingSpinner() {
        setContent(SubmissionCommentsUiState(isLoading = true))

        composeTestRule.onNodeWithTag("submissionComments_loading").assertIsDisplayed()
    }

    @Test
    fun showsEmptyState() {
        setContent(SubmissionCommentsUiState(isEmpty = true))

        composeTestRule.onNodeWithText(string(R.string.ngc_commentsEmpty)).assertIsDisplayed()
    }

    @Test
    fun showsOwnComment() {
        setContent(
            SubmissionCommentsUiState(
                comments = listOf(
                    SubmissionCommentItem(id = "1", content = "My comment", isOwnComment = true)
                )
            )
        )

        composeTestRule.onNodeWithTag("ownCommentText").assertTextEquals("My comment")
        composeTestRule.onNodeWithTag("ownCommentDate").assertIsDisplayed()
    }

    @Test
    fun showsOtherComment() {
        setContent(
            SubmissionCommentsUiState(
                comments = listOf(
                    SubmissionCommentItem(
                        id = "2",
                        authorName = "Professor Oak",
                        content = "Nice work",
                        isOwnComment = false
                    )
                )
            )
        )

        composeTestRule.onNodeWithTag("commentAuthorName").assertTextEquals("Professor Oak")
        composeTestRule.onNodeWithTag("commentText").assertTextEquals("Nice work")
    }

    @Test
    fun showsAnonymousAuthorWithLockAvatar() {
        setContent(
            SubmissionCommentsUiState(
                comments = listOf(
                    SubmissionCommentItem(
                        id = "3",
                        authorName = "Student 11234",
                        content = "Hidden author",
                        isOwnComment = false,
                        isAnonymous = true
                    )
                )
            )
        )

        composeTestRule.onNodeWithTag("anonymousCommentAvatar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("commentAuthorName").assertTextEquals("Student 11234")
    }

    @Test
    fun showsUnreadIndicatorForUnreadComment() {
        setContent(
            SubmissionCommentsUiState(
                comments = listOf(
                    SubmissionCommentItem(
                        id = "4",
                        authorName = "Professor Oak",
                        content = "Please read",
                        isOwnComment = false,
                        isUnread = true
                    )
                )
            )
        )

        composeTestRule.onNodeWithTag("unreadCommentIndicator").assertIsDisplayed()
    }

    @Test
    fun showsMediaAttachment() {
        setContent(
            SubmissionCommentsUiState(
                comments = listOf(
                    SubmissionCommentItem(
                        id = "5",
                        authorName = "Professor Oak",
                        isOwnComment = false,
                        mediaObject = SubmissionCommentMedia(
                            id = "m1",
                            mediaDownloadUrl = "https://media",
                            title = "audio.mp3",
                            mediaType = SubmissionCommentMediaType.AUDIO,
                            thumbnailUrl = null,
                            contentType = "audio/mp3"
                        )
                    )
                )
            )
        )

        composeTestRule.onNodeWithTag("mediaAttachment").assertIsDisplayed()
    }

    @Test
    fun sendButtonIsDisabledWhenInputIsBlank() {
        setContent(SubmissionCommentsUiState(commentText = ""))

        composeTestRule.onNodeWithTag("sendCommentButton").assertIsNotEnabled()
    }

    @Test
    fun sendButtonIsEnabledAndInvokesCallbackWhenInputHasText() {
        var sent = false
        setContent(
            SubmissionCommentsUiState(
                commentText = "Hello",
                actions = SubmissionCommentsActions(onSendComment = { sent = true })
            )
        )

        composeTestRule.onNodeWithTag("sendCommentButton").assertIsEnabled().performClick()

        assertTrue(sent)
    }

    @Test
    fun attachmentButtonInvokesCallback() {
        var clicked = false
        setContent(
            SubmissionCommentsUiState(
                actions = SubmissionCommentsActions(onAddAttachment = { clicked = true })
            )
        )

        composeTestRule.onNodeWithTag("addAttachmentButton").performClick()

        assertTrue(clicked)
    }

    @Test
    fun failedOwnCommentShowsRetryAndInvokesCallback() {
        var retried: SubmissionCommentItem? = null
        val failed = SubmissionCommentItem(id = "6", content = "Failed", isOwnComment = true, isFailed = true)
        setContent(
            SubmissionCommentsUiState(
                comments = listOf(failed),
                actions = SubmissionCommentsActions(onRetryComment = { retried = it })
            )
        )

        composeTestRule.onNodeWithText(string(R.string.ngc_commentSendError)).performClick()

        assertEquals("6", retried?.id)
    }

    @Test
    fun clickingAnAttachmentInvokesOpenCallback() {
        var opened: SubmissionCommentAttachment? = null
        val attachment = SubmissionCommentAttachment(id = "a1", displayName = "report.pdf", size = "1 KB")
        setContent(
            state = SubmissionCommentsUiState(
                comments = listOf(
                    SubmissionCommentItem(
                        id = "7",
                        authorName = "Professor Oak",
                        isOwnComment = false,
                        attachments = listOf(attachment)
                    )
                )
            ),
            onOpenAttachment = { opened = it }
        )

        composeTestRule.onNodeWithText("report.pdf").performClick()

        assertEquals("a1", opened?.id)
    }

    @Test
    fun clickingMediaInvokesOpenCallback() {
        var opened: SubmissionCommentMedia? = null
        val media = SubmissionCommentMedia(
            id = "m2",
            mediaDownloadUrl = "https://media",
            title = "clip.mp4",
            mediaType = SubmissionCommentMediaType.VIDEO,
            thumbnailUrl = null,
            contentType = "video/mp4"
        )
        setContent(
            state = SubmissionCommentsUiState(
                comments = listOf(
                    SubmissionCommentItem(id = "8", authorName = "Professor Oak", isOwnComment = false, mediaObject = media)
                )
            ),
            onOpenMedia = { opened = it }
        )

        composeTestRule.onNodeWithTag("mediaAttachment").performClick()

        assertEquals("m2", opened?.id)
    }
}
