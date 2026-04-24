/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.pages.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.toDate
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.assertTextColor
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.pandautils.utils.toFormattedString


class CourseDetailsPage(private val composeTestRule: ComposeTestRule) {

    fun assertCourseDetailsDisplayed(course: Course) {
        assertCourseNameDisplayed(course)
        composeTestRule.onNodeWithText("GRADES")
            .assertIsDisplayed()
            .assertIsSelected()
        composeTestRule.onNodeWithText("SYLLABUS").assertIsDisplayed()
        composeTestRule.onNodeWithText("SUMMARY").assertIsDisplayed()
    }

    fun assertCourseNameDisplayed(course: Course) {
        composeTestRule.onNodeWithText(course.name).assertIsDisplayed()
    }

    fun assertCourseNameDisplayed(course: CourseApiModel) {
        composeTestRule.onNodeWithText(course.name).assertIsDisplayed()
    }

    fun selectTab(tabName: String) {
        composeTestRule.onNodeWithText(tabName).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertTabSelected(tabName: String) {
        composeTestRule.onNodeWithText(tabName).assertIsSelected()
    }

    fun clickAssignment(assignmentName: String) {
        composeTestRule.onNode(hasTestTag("assignmentItem") and hasText(assignmentName)).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertAssignmentStatus(assignmentName: String, status: String) {
        composeTestRule.onNode(hasText(status) and hasAnyAncestor(hasAnyChild(hasText( assignmentName))), useUnmergedTree = true).assertIsDisplayed()
    }

    fun assertAssignmentLabelTextColor(assignmentName: String, expectedTextColor: Long) {
        composeTestRule.onNodeWithText(assignmentName).assertTextColor(Color(expectedTextColor))
    }

    fun assertTabDisplayed(tabName: String) {
        composeTestRule.onNodeWithText(tabName).assertIsDisplayed()
    }

    fun assertTabDoesNotExist(tabName: String) {
        composeTestRule.onNodeWithText(tabName).assertDoesNotExist()
    }

    fun assertTabCount(expectedCount: Int) {
        composeTestRule.onAllNodes(hasAnyAncestor(hasTestTag("courseDetailsTabRow")) and isSelectable())
            .assertCountEquals(expectedCount)
    }

    fun clickComposeMessageFAB() {
        composeTestRule.onNodeWithContentDescription("Send a message about this course").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickDiscussionCheckpointExpandCollapseIcon(discussionTitle: String) {
        composeTestRule.onNode(hasTestTag("expandDiscussionCheckpoints") and hasParent(hasAnyDescendant(hasText(discussionTitle))), useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun assertDiscussionCheckpointDetails(additionalRepliesCount: Int, dueAtReplyToTopic: String, gradeReplyToTopic: String, statusReplyToTopic: String = "Not Submitted", dueAtAdditionalReplies: String = dueAtReplyToTopic, gradeAdditionalReplies: String = gradeReplyToTopic, statusAdditionalReplies: String = "Not Submitted") {
        composeTestRule.onNode(hasTestTag("checkpointName") and hasText("Reply to topic"), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointDueDate_Reply to topic") and hasText(dueAtReplyToTopic), true).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointGradeText") and hasText(gradeReplyToTopic), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointSubmissionStateLabel") and hasText(statusReplyToTopic) and hasAnySibling(hasTestTag("checkpointDueDate_Reply to topic")), useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNode(hasTestTag("checkpointName") and hasText("Additional replies ($additionalRepliesCount)"), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointDueDate_Additional replies ($additionalRepliesCount)") and hasText(dueAtAdditionalReplies), true).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointGradeText") and hasText(gradeAdditionalReplies), useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("checkpointSubmissionStateLabel") and hasText(statusAdditionalReplies) and hasAnySibling(hasTestTag("checkpointDueDate_Additional replies ($additionalRepliesCount)")), useUnmergedTree = true).assertIsDisplayed()
    }

    fun assertHasAssignmentWithCheckpoints(assignmentName: String, dueAtString: String = "No due date", dueAtStringSecondCheckpoint: String? = null, expectedGrade: String? = null) {
        assertHasAssignmentCommon(assignmentName, dueAtString, dueAtStringSecondCheckpoint, expectedGrade, hasCheckPoints = true)
    }

    fun refresh() {
        composeTestRule.onNodeWithTag("gradesList").performTouchInput { swipeDown() }
        composeTestRule.waitForIdle()
    }

    private fun assertHasAssignmentCommon(assignmentName: String, assignmentDueAt: String?, secondCheckpointDueAt: String? = null, expectedGradeLabel: String? = null, assignmentStatus: String? = null, hasCheckPoints : Boolean = false) {

        // Check if the assignment is a discussion with checkpoints, if yes, we are expecting 2 due dates for the 2 checkpoints.
        if(hasCheckPoints) {
            if (assignmentDueAt == null || assignmentDueAt == "No due date") {
                composeTestRule.onAllNodes(
                    hasText("No due date").and(
                        hasParent(hasAnyDescendant(hasText(assignmentName)))
                    ),
                    true
                ).assertCountEquals(2)
            }
            else {
                if(secondCheckpointDueAt != null) {
                    composeTestRule.onAllNodes(
                        hasText(assignmentDueAt).and(
                            hasParent(hasAnyDescendant(hasText(assignmentName)))
                        ),
                        true
                    ).assertCountEquals(1)
                    composeTestRule.onAllNodes(
                        hasText(secondCheckpointDueAt).and(
                            hasParent(hasAnyDescendant(hasText(assignmentName)))
                        ),
                        true
                    ).assertCountEquals(1)
                }
                else {
                    composeTestRule.onAllNodes(
                        hasText(assignmentDueAt).and(
                            hasParent(hasAnyDescendant(hasText(assignmentName)))
                        ),
                        true
                    ).assertCountEquals(2)
                }
            }
        }
        else {
            // Check that either the assignment due date is present, or "No Due Date" is displayed
            if (assignmentDueAt != null) {
                composeTestRule.onNode(
                    hasText(assignmentName).and(
                        hasParent(
                            hasAnyDescendant(
                                hasText(
                                    "Due ${
                                        assignmentDueAt.toDate()!!.toFormattedString()
                                    }"
                                )
                            )
                        )
                    )
                )
                    .assertIsDisplayed()
            } else {
                composeTestRule.onNode(
                    hasText(assignmentName).and(
                        hasParent(hasAnyDescendant(hasText("No due date")))
                    )
                )
                    .assertIsDisplayed()
            }
        }

        retryWithIncreasingDelay(times = 10, maxDelay = 4000, catchBlock = { refresh() }) {
            // Check that grade is present, if that is specified
            if (expectedGradeLabel != null) {
                composeTestRule.onNode(
                    hasText(assignmentName).and(
                        hasParent(hasAnyDescendant(hasText(expectedGradeLabel, substring = true)))
                    )
                )
                    .assertIsDisplayed()
            }
        }

        retryWithIncreasingDelay(times = 10, maxDelay = 4000, catchBlock = { refresh() }) {
            if(assignmentStatus != null) {
                composeTestRule.onNode(
                    hasText(assignmentStatus).and(
                        hasAnyAncestor(hasAnyChild(hasText(assignmentName)))
                    ),
                    useUnmergedTree = true
                )
                    .assertIsDisplayed()
            }
        }
    }
}
