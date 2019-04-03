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
package com.instructure.teacher.ui

import com.instructure.espresso.randomString
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.seedQuizzes
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class EditQuizDetailsPageTest : TeacherTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.assertPageObjects()
    }

    @Test
    @Ditto(sequential = true)
    fun editQuizTitle() {
        getToEditQuizDetailsPage()
        val newName = mockableString("quiz title") { randomString() }
        editQuizDetailsPage.editQuizTitle(newName)
        quizDetailsPage.assertQuizTitleChanged(newName)
    }

    @Test
    @Ditto
    fun editAccessCode() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAccessCode()
        val newCode: String = editQuizDetailsPage.editAccessCode()
        quizDetailsPage.assertAccessCodeChanged(newCode)
    }

    @Test
    @Ditto
    fun editDueDate() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditDueDate()
        editQuizDetailsPage.editDate(2017, 1, 1)
        editQuizDetailsPage.assertDateChanged(2017, 0, 1, R.id.dueDate)
    }

    @Test
    @Ditto
    fun editDueTime() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditDueTime()
        editQuizDetailsPage.editTime(1, 30)
        editQuizDetailsPage.assertTimeChanged(1, 30, R.id.dueTime)
    }

    @Test
    @Ditto
    fun editUnlockDate() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditUnlockDate()
        editQuizDetailsPage.editDate(2017, 1, 1)
        editQuizDetailsPage.assertDateChanged(2017, 0, 1, R.id.fromDate)
    }

    @Test
    @Ditto
    fun editUnlockTime() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditUnlockTime()
        editQuizDetailsPage.editTime(1, 30)
        editQuizDetailsPage.assertTimeChanged(1, 30, R.id.fromTime)
    }

    @Test
    @Ditto
    fun editLockDate() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditLockDate()
        editQuizDetailsPage.editDate(2017, 1, 1)
        editQuizDetailsPage.assertDateChanged(2017, 0, 1, R.id.toDate)
    }

    @Test
    @Ditto
    fun editLockTime() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickEditLockTime()
        editQuizDetailsPage.editTime(1, 30)
        editQuizDetailsPage.assertTimeChanged(1, 30, R.id.toTime)
    }

    @Test
    @Ditto
    fun addOverride() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editQuizDetailsPage.assertNewOverrideCreated()
    }

    @Test
    @Ditto
    fun removeOverride() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editQuizDetailsPage.assertNewOverrideCreated()
        editQuizDetailsPage.removeSecondOverride()
        editQuizDetailsPage.assertOverrideRemoved()
    }

    @Test
    @Ditto
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
    @Ditto
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
    @Ditto
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
    @Ditto
    fun noAssigneesError() {
        getToEditQuizDetailsPage()
        editQuizDetailsPage.clickAddOverride()
        assigneeListPage.saveAndClose()
        editQuizDetailsPage.assertNewOverrideCreated()
        editQuizDetailsPage.saveQuiz()
        editQuizDetailsPage.assertNoAssigneesErrorShown()
    }

    private fun getToEditQuizDetailsPage() {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val quiz = seedQuizzes(
                courseId = course.id,
                quizzes = 1,
                withDescription = false,
                teacherToken = teacher.token).quizList[0]

        tokenLogin(teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openQuizzesTab()
        quizListPage.clickQuiz(quiz)
        quizDetailsPage.openEditPage()
    }
}
