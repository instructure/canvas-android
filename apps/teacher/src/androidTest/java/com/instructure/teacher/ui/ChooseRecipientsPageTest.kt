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

import android.os.SystemClock.sleep
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addRecipientsToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.clickInboxTab
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ChooseRecipientsPageTest: TeacherTest() {
    @Test
    override fun displaysPageObjects() {
        getToChooseRecipients()
        chooseRecipientsPage.assertPageObjects()
    }

    @Test
    fun hasStudentCategory() {
        getToChooseRecipients()
        chooseRecipientsPage.assertHasStudent()
    }

    @Test
    fun addRecipient() {
        val student = getToChooseRecipients().students[0]
        chooseRecipientsPage.clickStudentCategory()
        chooseRecipientsPage.clickStudent(student)
        chooseRecipientsPage.clickDone()
        addMessagePage.assertHasStudentRecipient(student)
    }

    private fun getToChooseRecipients(): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val course = data.courses.values.first()
        val teacher = data.teachers[0]

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        // Klunky way to register student/teacher recipients
        // TODO: In the endpoint (SearchEndpoint), compute this info by going through courses / enrollments /
        // roles.
        data.addRecipientsToCourse(
                course = course,
                students = data.students,
                teachers = data.teachers
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.clickInboxTab()
        inboxPage.pressNewMessageButton()
        addMessagePage.clickCourseSpinner()
        addMessagePage.selectCourseFromSpinner(course)
        // Sigh... Sometimes, at least on my local machine, it takes a beat for this button to become responsive
        sleep(2000)
        addMessagePage.clickAddContacts()
        return data
    }
}
