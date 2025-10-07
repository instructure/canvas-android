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
package com.instructure.student.ui.rendertests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.LockedModule
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentFragment
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Month
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SubmissionDetailsEmptyContentRenderTest : StudentRenderTest() {

    private lateinit var baseModel: SubmissionDetailsEmptyContentModel
    private lateinit var baseAssignment: Assignment
    private var isStudioEnabled = false

    @Before
    fun setup() {
        baseAssignment = Assignment(
            submissionTypesRaw = listOf("online_upload"),
            lockedForUser = false,
            dueAt = OffsetDateTime.now().withHour(23).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME)
        )

        baseModel = SubmissionDetailsEmptyContentModel(
            assignment = baseAssignment,
            course = Course(),
            isStudioEnabled = isStudioEnabled
        )
    }

    @Test
    fun submitButtonIsEnabledWhenUserCanSubmitAssignment() {
        loadPageWithModel(baseModel)

        submissionDetailsEmptyContentRenderPage.assertSubmitButtonEnabled()
    }

    @Test
    fun submitButtonIsHiddenWhenUserCannotSubmit() {
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(lockedForUser = true)
        ))

        submissionDetailsEmptyContentRenderPage.assertSubmitButtonHidden()
    }

    @Test
    fun submitButtonIsHiddenWhenUserIsObserver() {
        baseModel = baseModel.copy(isObserver = true)
        loadPageWithModel(baseModel)

        submissionDetailsEmptyContentRenderPage.assertSubmitButtonHidden()
    }

    @Test
    fun displaysDueYesterday() {
        val expectedText = "Due yesterday at 1:59 pm"
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(
                dueAt = OffsetDateTime.now().minusDays(1L).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME)
            )
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysDueToday() {
        val expectedText = "Due today at 11:59 pm"
        loadPageWithModel(baseModel)

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysDueTomorrow() {
        val expectedText = "Due tomorrow at 1:59 pm"
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(
                dueAt = OffsetDateTime.now().plusDays(1L).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME)
            )
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysDueDate() {
        // Some fancy footwork here to avoid using the Apr 2 date when it is Apr 1, Apr 2 or Apr 3,
        // as that would cause the date to appear as tomorrow, today and yesterday, respectively.
        // So if we are in April, we will just change the due date to June.
        var expectedText = "Due Apr 2 at 1:59 pm"
        var month = 4
        if(OffsetDateTime.now().month == Month.APRIL) {
            expectedText = "Due Jun 2 at 1:59 pm"
            month = 6
        }
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(dueAt = OffsetDateTime.now().withMonth(month).withDayOfMonth(2).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME))
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysNoDueDate() {
        val expectedText = "Your assignment has no due date"
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(dueAt = null)
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysDueDateWithYearWhenNotThisYear() {
        val expectedText = "Due Apr 2, 2018 at 1:59 pm"
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(dueAt = OffsetDateTime.now().withYear(2018).withMonth(4).withDayOfMonth(2).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME))
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysAssignmentLocked() {
        val expectedText = "Your assignment was locked on Apr 2, 2016 at 1:59 pm"
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(
                lockedForUser = true,
                lockAt = OffsetDateTime.now().withYear(2016).withMonth(4).withDayOfMonth(2).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME))
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysAssignmentWillUnlock() {
        val expectedText = "Your assignment will unlock on Apr 2, 2067 at 1:59 pm"
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(
                lockedForUser = true,
                unlockAt = OffsetDateTime.now().withYear(2067).withMonth(4).withDayOfMonth(2).withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME))
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysAssignmentLockedByModule() {
        val expectedText = "Your assignment is locked by module \"Test Module\""
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(
                lockedForUser = true,
                lockInfo = LockInfo(
                    contextModule = LockedModule(
                        name = "Test Module"
                    )
                )
            )
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)
    }

    @Test
    fun displaysAssignmentLockedByModulePrereq() {
        val expectedText = "Your assignment is locked by a module requirement"
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(
                lockedForUser = true,
                lockInfo = LockInfo(
                    modulePrerequisiteNames = arrayListOf("must_view")
                )
            )
        ))

        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate(expectedText)

    }

    @Test
    fun onlyDisplayNoSubmissionAllowedTitleWhenTheAssignmentIsNotGraded() {
        loadPageWithModel(baseModel.copy(
            assignment = baseAssignment.copy(
                gradingType = "not_graded"
            )
        ))

        submissionDetailsEmptyContentRenderPage.assertSubmitButtonHidden()
        submissionDetailsEmptyContentRenderPage.assertTitleText(R.string.submissionDetailsNoSubmissionAllowed)
        submissionDetailsEmptyContentRenderPage.assertExpectedDueDate("")
    }

    private fun loadPageWithModel(model: SubmissionDetailsEmptyContentModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val fragment = SubmissionDetailsEmptyContentFragment.newInstance(model.course, model.assignment, model.isStudioEnabled).apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }

        activityRule.activity.loadFragment(fragment)
    }
}