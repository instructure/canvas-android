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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyFragment
import com.spotify.mobius.runners.WorkRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class SubmissionDetailsEmptyContentRenderTest : StudentRenderTest() {

    private lateinit var baseModel: SubmissionDetailsEmptyContentModel

    @Before
    fun setup() {
        baseModel = SubmissionDetailsEmptyContentModel(
                assignment = Assignment(dueAt = OffsetDateTime.now().withHour(23).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME)),
                course = Course()
        )
    }

    @Test
    fun submitButtonIsEnabledWhenUserCanSubmitAssignment() {
        loadPageWithModel(baseModel.copy(
                assignment = Assignment(
                        submissionTypesRaw = listOf("online_upload"),
                        lockedForUser = false)
        ))

        submissionDetailsEmptyContentRenderPage.assertSubmitButtonEnabled()
    }

    @Test
    fun submitButtonIsDisabledWhenTheUserCannotSubmit() {
        loadPageWithModel(baseModel.copy(
                assignment = Assignment(
                        lockedForUser = false)
        ))

        submissionDetailsEmptyContentRenderPage.assertSubmitButtonDisabled()
    }

    @Test
    fun displaysDueYesterday() {
        val expectedText = "Due yesterday at 1:59pm"
        loadPageWithModel(baseModel.copy(
                assignment = Assignment(dueAt = OffsetDateTime.now().minusDays(1L).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME))
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysDueToday() {
        val expectedText = "Due today at 11:59pm"
        loadPageWithModel(baseModel)

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysDueTomorrow() {
        val expectedText = "Due tomorrow at 1:59pm"
        loadPageWithModel(baseModel.copy(
                assignment = Assignment(dueAt = OffsetDateTime.now().plusDays(1L).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME))
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysDueDate() {
        val expectedText = "Due Apr 2 at 1:59pm"
        loadPageWithModel(baseModel.copy(
                assignment = Assignment(dueAt = OffsetDateTime.now().withMonth(4).withDayOfMonth(2).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME))
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysNoDueDate() {
        val expectedText = "Your assignment has no due date"
        loadPageWithModel(baseModel.copy(
                assignment = Assignment()
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    private fun loadPageWithModel(model: SubmissionDetailsEmptyContentModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val fragment = SubmissionDetailsEmptyFragment.newInstance(model.course, model.assignment).apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }

        activityRule.activity.loadFragment(fragment)
    }

}