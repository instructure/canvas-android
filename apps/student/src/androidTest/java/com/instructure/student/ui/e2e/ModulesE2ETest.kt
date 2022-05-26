/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.e2e

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.ModuleItemTypes
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ModulesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E, false)
    fun testModulesE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Create some assignments, quizzes, pages, etc...
        val assignment1 = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                dueAt = 1.days.fromNow.iso8601
        ))

        val assignment2 = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                dueAt = 2.days.fromNow.iso8601
        ))

        val quiz1 = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                dueAt = 3.days.fromNow.iso8601,
                token = teacher.token,
                published = true
        ))

        val page1 = PagesApi.createCoursePage(
                courseId = course.id,
                published = true,
                frontPage = false,
                token = teacher.token
        )

        val discussionTopic1 = DiscussionTopicsApi.createDiscussion(
                courseId = course.id,
                token = teacher.token
        )

        // Create a couple of modules.  They start out as unpublished.
        val module1 = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = null)

        val module2 = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = null)

        // Associate items with module 1
        ModulesApi.createModuleItem(
                courseId = course.id,
                moduleId = module1.id,
                teacherToken = teacher.token,
                title = assignment1.name,
                type = ModuleItemTypes.ASSIGNMENT.stringVal,
                contentId = assignment1.id.toString()
        )

        ModulesApi.createModuleItem(
                courseId = course.id,
                moduleId = module1.id,
                teacherToken = teacher.token,
                title = quiz1.title,
                type = ModuleItemTypes.QUIZ.stringVal,
                contentId = quiz1.id.toString()
        )

        // Associated items with module 2
        ModulesApi.createModuleItem(
                courseId = course.id,
                moduleId = module2.id,
                teacherToken = teacher.token,
                title = assignment2.name,
                type = ModuleItemTypes.ASSIGNMENT.stringVal,
                contentId = assignment2.id.toString()
        )

        ModulesApi.createModuleItem(
                courseId = course.id,
                moduleId = module2.id,
                teacherToken = teacher.token,
                title = page1.title,
                type = ModuleItemTypes.PAGE.stringVal,
                contentId = null, // Not necessary for Page item
                pageUrl = page1.url // Only necessary for Page item
        )

        ModulesApi.createModuleItem(
                courseId = course.id,
                moduleId = module2.id,
                teacherToken = teacher.token,
                title = discussionTopic1.title,
                type = ModuleItemTypes.DISCUSSION.stringVal,
                contentId = discussionTopic1.id.toString()
        )

        // Sign in and navigate to our course
        tokenLogin(student)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)
        dashboardPage.selectCourse(course)

        // Assert that no modules are present, since none are published
        courseBrowserPage.assertTitleCorrect(course)
        courseBrowserPage.assertTabNotDisplayed("Modules")
        courseBrowserPage.selectHome()
        modulesPage.assertEmptyView()
        Espresso.pressBack() // Back to course browser view

        // Let's publish our modules
        ModulesApi.updateModule(
                courseId = course.id,
                id = module1.id,
                published = true,
                teacherToken = teacher.token
        )
        ModulesApi.updateModule(
                courseId = course.id,
                id = module2.id,
                published = true,
                teacherToken = teacher.token
        )

        // Refresh our screen to get updated tabs
        courseBrowserPage.refresh()

        // Now see that the Modules tab is displayed
        courseBrowserPage.assertTabDisplayed("Modules")

        // Go to modules
        courseBrowserPage.selectModules()

        // Verify that both modules are displayed, along with their items
        modulesPage.assertModuleDisplayed(module1)
        modulesPage.assertModuleItemDisplayed(module1, assignment1.name)
        modulesPage.assertModuleItemDisplayed(module1, quiz1.title)
        modulesPage.assertModuleDisplayed(module2)
        modulesPage.assertModuleItemDisplayed(module2, assignment2.name)
        modulesPage.assertModuleItemDisplayed(module2, page1.title)
        modulesPage.assertModuleItemDisplayed(module2, discussionTopic1.title)
    }
}