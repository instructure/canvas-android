/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.interaction

import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.espresso.randomDouble
import com.instructure.espresso.randomString
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Test

@HiltAndroidTest
class EditAssignmentDetailsInteractionTest : TeacherComposeTest() {

    @Test
    override fun displaysPageObjects() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.assertPageObjects()
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheckNames(Matchers.`is`("SpeakableTextPresentCheck")),
            AccessibilityCheckResultUtils.matchesViews(ViewMatchers.withId(R.id.assignTo))
        )

        super.enableAndConfigureAccessibilityChecks()
    }

    @Test
    fun editAssignmentName() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickAssignmentNameEditText()
        val newAssignmentName = randomString()
        editAssignmentDetailsPage.editAssignmentName(newAssignmentName)
        editAssignmentDetailsPage.saveAssignment()
        assignmentDetailsPage.assertAssignmentName(newAssignmentName)
    }

    @Test
    fun editAssignmentPoints() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickPointsPossibleEditText()
        val newPoints = randomDouble()
        editAssignmentDetailsPage.editAssignmentPoints(newPoints)
        editAssignmentDetailsPage.saveAssignment()
        val stringPoints = NumberHelper.formatDecimal(newPoints, 1, true)
        assignmentDetailsPage.assertAssignmentPointsChanged(stringPoints)
    }

    @Test
    fun editDueDate() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditDueDate()
        editAssignmentDetailsPage.editDate(2017, 1, 1)
        editAssignmentDetailsPage.assertDateChanged(2017, 0, 1, R.id.dueDate)
    }

    @Test
    fun editDueTime() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditDueTime()
        editAssignmentDetailsPage.editTime(1, 30)
        editAssignmentDetailsPage.assertTimeChanged(1, 30, R.id.dueTime)
    }

    @Test
    fun editUnlockDate() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditUnlockDate()
        editAssignmentDetailsPage.editDate(2017, 1, 1)
        editAssignmentDetailsPage.assertDateChanged(2017, 0, 1, R.id.fromDate)
    }

    @Test
    fun editUnlockTime() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditUnlockTime()
        editAssignmentDetailsPage.editTime(1, 30)
        editAssignmentDetailsPage.assertTimeChanged(1, 30, R.id.fromTime)
    }

    @Test
    fun editLockDate() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditLockDate()
        editAssignmentDetailsPage.editDate(2017, 1, 1)
        editAssignmentDetailsPage.assertDateChanged(2017, 0, 1, R.id.toDate)
    }

    @Test
    fun editLockTime() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditLockTime()
        editAssignmentDetailsPage.editTime(1, 30)
        editAssignmentDetailsPage.assertTimeChanged(1, 30, R.id.toTime)
    }

    @Test
    fun addOverride() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editAssignmentDetailsPage.assertNewOverrideCreated()
    }

    @Test
    fun removeOverride() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editAssignmentDetailsPage.assertNewOverrideCreated()
        editAssignmentDetailsPage.removeFirstOverride()
        editAssignmentDetailsPage.assertOverrideRemoved()
    }

    @Test
    fun dueDateBeforeUnlockDateError() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditDueDate()
        editAssignmentDetailsPage.editDate(1987, 8, 10)
        editAssignmentDetailsPage.clickEditUnlockDate()
        editAssignmentDetailsPage.editDate(2015, 2, 10)
        editAssignmentDetailsPage.saveAssignment()
        editAssignmentDetailsPage.assertDueDateBeforeUnlockDateErrorShown()
    }

    @Test
    fun dueDateAfterLockDateError() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditDueDate()
        editAssignmentDetailsPage.editDate(2015, 2, 10)
        editAssignmentDetailsPage.clickEditLockDate()
        editAssignmentDetailsPage.editDate(1987, 8, 10)
        editAssignmentDetailsPage.saveAssignment()
        editAssignmentDetailsPage.assertDueDateAfterLockDateErrorShown()
    }

    @Test
    fun unlockDateAfterLockDateError() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickEditUnlockDate()
        editAssignmentDetailsPage.editDate(2015, 2, 10)
        editAssignmentDetailsPage.clickEditLockDate()
        editAssignmentDetailsPage.editDate(1987, 8, 10)
        editAssignmentDetailsPage.saveAssignment()
        editAssignmentDetailsPage.assertLockDateAfterUnlockDateErrorShown()
    }

    @Test
    fun noAssigneesError() {
        getToEditAssignmentDetailsPage()
        editAssignmentDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editAssignmentDetailsPage.assertNewOverrideCreated()
        editAssignmentDetailsPage.saveAssignment()
        editAssignmentDetailsPage.assertNoAssigneesErrorShown()
    }

    private fun getToEditAssignmentDetailsPage(
            withDescription: Boolean = false,
            lockAt: String = "",
            unlockAt: String = "",
            submissionTypes: List<Assignment.SubmissionType> = emptyList()): Assignment {

        val data = MockCanvas.init(teacherCount = 1, favoriteCourseCount = 1, courseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                withDescription = withDescription,
                lockAt = lockAt,
                unlockAt = unlockAt,
                submissionTypeList = submissionTypes.ifEmpty { listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY) }
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)

        assignmentDetailsPage.openEditPage()
        return assignment
    }
}
