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

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.model.ModuleItemTypes
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentApiManager
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ModulesE2ETest: StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.MODULES, TestCategory.E2E)
    fun testModulesE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val assignment1 = StudentApiManager.createAssignment(course, teacher, withDescription = true)

        Log.d(PREPARATION_TAG,"Seeding another assignment for '${course.name}' course.")
        val assignment2 = StudentApiManager.createAssignment(course, teacher, dueAt = 2.days.fromNow.iso8601, withDescription = true)

        Log.d(PREPARATION_TAG,"Create a PUBLISHED quiz for '${course.name}' course.")
        val quiz1 = StudentApiManager.createQuiz(course, teacher, dueAt = 3.days.fromNow.iso8601)

        Log.d(PREPARATION_TAG,"Create a page for '${course.name}' course.")
        val page1 = StudentApiManager.createPage(course, teacher)

        Log.d(PREPARATION_TAG,"Create a discussion topic for '${course.name}' course.")
        val discussionTopic1 = StudentApiManager.createDiscussion(course, teacher)

        //Modules start out as unpublished.
        Log.d(PREPARATION_TAG,"Create a module for '${course.name}' course.")
        val module1 = StudentApiManager.createModule(course, teacher)

        Log.d(PREPARATION_TAG,"Create another module for '${course.name}' course.")
        val module2 = StudentApiManager.createModule(course, teacher)

        Log.d(PREPARATION_TAG,"Associate '${assignment1.name}' assignment with '${module1.name}' module.")
        StudentApiManager.createModuleItem(course.id, module1.id, teacher, assignment1.name, ModuleItemTypes.ASSIGNMENT.stringVal, contentId = assignment1.id.toString())

        Log.d(PREPARATION_TAG,"Associate '${quiz1.title}' quiz with '${module1.name}' module.")
        StudentApiManager.createModuleItem(course.id, module1.id, teacher, quiz1.title, ModuleItemTypes.QUIZ.stringVal, contentId = quiz1.id.toString())

        Log.d(PREPARATION_TAG,"Associate '${assignment2.name}' assignment with '${module2.name}' module.")
        StudentApiManager.createModuleItem(course.id, module2.id, teacher, assignment2.name, ModuleItemTypes.ASSIGNMENT.stringVal, contentId = assignment2.id.toString())

        Log.d(PREPARATION_TAG,"Associate '${page1.title}' page with '${module2.name}' module.")
        StudentApiManager.createModuleItem(course.id, module2.id, teacher, page1.title, ModuleItemTypes.PAGE.stringVal, pageUrl = page1.url)

        Log.d(PREPARATION_TAG,"Associate '${discussionTopic1.title}' discussion topic with '${module2.name}' module.")
        StudentApiManager.createModuleItem(course.id, module2.id, teacher, discussionTopic1.title, ModuleItemTypes.DISCUSSION.stringVal, contentId = discussionTopic1.id.toString())

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Assert that '${course.name}' course is displayed.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG,"Select '${course.name}' course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Assert that there are no modules displayed yet because there are not published. Assert that the 'Modules' Tab is not displayed as well.")
        courseBrowserPage.assertTitleCorrect(course)
        courseBrowserPage.assertTabNotDisplayed("Modules")

        Log.d(STEP_TAG,"Click on 'Home' label and assert that the empty view is displayed on the Modules Page.")
        courseBrowserPage.selectHome()
        modulesPage.assertEmptyView()

        Log.d(STEP_TAG,"Navigate back to Course Browser Page.")
        Espresso.pressBack()

        Log.d(PREPARATION_TAG,"Publish '${module1.name}' module.")
        StudentApiManager.publishModule(course, module1, teacher)

        Log.d(PREPARATION_TAG,"Publish '${module2.name}' module.")
        StudentApiManager.publishModule(course, module2, teacher)

        Log.d(STEP_TAG,"Refresh the page. Assert that the 'Modules' Tab is displayed.")
        courseBrowserPage.refresh()
        courseBrowserPage.assertTabDisplayed("Modules")

        Log.d(STEP_TAG,"Navigate to Modules Page.")
        courseBrowserPage.selectModules()

        Log.d(STEP_TAG,"Assert that '${module1.name}' module is displayed with the following items: '${assignment1.name}' assignment, '${quiz1.title}' quiz.")
        modulesPage.assertModuleDisplayed(module1)
        modulesPage.assertModuleItemDisplayed(module1, assignment1.name)
        modulesPage.assertModuleItemDisplayed(module1, quiz1.title)

        Log.d(STEP_TAG,"Assert that '${module2.name}' module is displayed with the following items: '${assignment2.name}' assignment," +
                " '${page1.title}' page, '${discussionTopic1.title}' discussion topic.")
        modulesPage.assertModuleDisplayed(module2)
        modulesPage.assertModuleItemDisplayed(module2, assignment2.name)
        modulesPage.assertModuleItemDisplayed(module2, page1.title)
        modulesPage.assertModuleItemDisplayed(module2, discussionTopic1.title)

        Log.d(STEP_TAG, "Collapse the '${module2.name}' module. Assert that there will be 4 countable items on the screen.")
        modulesPage.clickOnModuleExpandCollapseIcon(module2.name)
        modulesPage.assertModulesAndItemsCount(4) // 2 modules titles and 2 module item in first module

        Log.d(STEP_TAG, "Expand the '${module2.name}' module. Assert that there will be 7 countable items on the screen.")
        modulesPage.clickOnModuleExpandCollapseIcon(module2.name)
        modulesPage.assertModulesAndItemsCount(7) // 2 modules titles, 2 module items in first module, 3 items in second module

        Log.d(STEP_TAG, "Assert that '${assignment1.name}' module item is displayed and open it. Assert that the Assignment Details page is displayed with the corresponding assignment title.")
        modulesPage.assertAndClickModuleItem(module1.name, assignment1.name, true)
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertAssignmentTitle(assignment1.name)
    }
}