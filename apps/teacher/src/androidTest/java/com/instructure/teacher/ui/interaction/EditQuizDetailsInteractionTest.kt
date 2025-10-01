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
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Quiz
import com.instructure.espresso.randomString
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers
import org.junit.Test

@HiltAndroidTest
class EditQuizDetailsInteractionTest : TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.assertPageObjects()
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheckNames(Matchers.`is`("SpeakableTextPresentCheck")),
            AccessibilityCheckResultUtils.matchesViews(ViewMatchers.withId(R.id.assignTo))
        )

        super.enableAndConfigureAccessibilityChecks()
    }

    @Test
    fun editQuizTitle() {
        getToEditQuizDetailsPage()
        val newName = randomString()
        editQuizDetailsPage.editQuizTitle(newName)
        quizDetailsPage.assertQuizTitleChanged(newName)
    }

    @Test
    fun editAccessCode() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAccessCode()
        val newCode: String = editQuizDetailsPage.editAccessCode()
        quizDetailsPage.assertAccessCodeChanged(newCode)
    }

    @Test
    fun editDueDate() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditDueDate()
        editQuizDetailsPage.editDate(2017, 1, 1)
        editQuizDetailsPage.assertDateChanged(2017, 0, 1, R.id.dueDate)
    }

    @Test
    fun editDueTime() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditDueTime()
        editQuizDetailsPage.editTime(1, 30)
        editQuizDetailsPage.assertTimeChanged(1, 30, R.id.dueTime)
    }

    @Test
    fun editUnlockDate() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditUnlockDate()
        editQuizDetailsPage.editDate(2017, 1, 1)
        editQuizDetailsPage.assertDateChanged(2017, 0, 1, R.id.fromDate)
    }

    @Test
    fun editUnlockTime() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditUnlockTime()
        editQuizDetailsPage.editTime(1, 30)
        editQuizDetailsPage.assertTimeChanged(1, 30, R.id.fromTime)
    }

    @Test
    fun editLockDate() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditLockDate()
        editQuizDetailsPage.editDate(2017, 1, 1)
        editQuizDetailsPage.assertDateChanged(2017, 0, 1, R.id.toDate)
    }

    @Test
    fun editLockTime() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditLockTime()
        editQuizDetailsPage.editTime(1, 30)
        editQuizDetailsPage.assertTimeChanged(1, 30, R.id.toTime)
    }

    @Test
    fun addOverride() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editQuizDetailsPage.assertNewOverrideCreated()
    }

    @Test
    fun removeOverride() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editQuizDetailsPage.assertNewOverrideCreated()
        editQuizDetailsPage.removeSecondOverride()
        editQuizDetailsPage.assertOverrideRemoved()
    }

    @Test
    fun dueDateBeforeUnlockDateError() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditDueDate()
        editQuizDetailsPage.editDate(1987, 8, 10)
        editQuizDetailsPage.clickEditUnlockDate()
        editQuizDetailsPage.editDate(2015, 2, 10)
        editQuizDetailsPage.saveQuiz()
        editQuizDetailsPage.assertDueDateBeforeUnlockDateErrorShown()
    }

    @Test
    fun dueDateAfterLockDateError() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditDueDate()
        editQuizDetailsPage.editDate(2015, 2, 10)
        editQuizDetailsPage.clickEditLockDate()
        editQuizDetailsPage.editDate(1987, 8, 10)
        editQuizDetailsPage.saveQuiz()
        editQuizDetailsPage.assertDueDateAfterLockDateErrorShown()
    }

    @Test
    fun unlockDateAfterLockDateError() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditUnlockDate()
        editQuizDetailsPage.editDate(2015, 2, 10)
        editQuizDetailsPage.clickEditLockDate()
        editQuizDetailsPage.editDate(1987, 8, 10)
        editQuizDetailsPage.saveQuiz()
        editQuizDetailsPage.assertLockDateAfterUnlockDateErrorShown()
    }

    @Test
    fun noAssigneesError() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editQuizDetailsPage.assertNewOverrideCreated()
        editQuizDetailsPage.saveQuiz()
        editQuizDetailsPage.assertNoAssigneesErrorShown()
    }

    private fun getToEditQuizDetailsPage() {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val quiz = data.addQuizToCourse(
                course = course,
                quizType = Quiz.TYPE_ASSIGNMENT,
                description = ""
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(quiz)
        quizDetailsPage.openEditPage()
    }
}
