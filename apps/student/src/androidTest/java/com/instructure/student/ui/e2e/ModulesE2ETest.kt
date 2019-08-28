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
import com.instructure.canvas.espresso.Stub
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class ModulesE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.MODULES, TestCategory.E2E, false)
    fun testModulesE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Create a couple of modules.  They start out as unpublished.
        val module1 = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = null)

        val module2 = ModulesApi.createModule(
                courseId = course.id,
                teacherToken = teacher.token,
                unlockAt = null)

        // Sign in and navigate to our course
        tokenLogin(student)
        dashboardPage.waitForRender()
        dashboardPage.assertDisplaysCourse(course)
        dashboardPage.selectCourse(course)

        // Assert that no modules are present, since none are published
        courseBrowserPage.assertTitleCorrect(course)
        courseBrowserPage.assertTabNotDisplayed("Modules")
        courseBrowserPage.selectHomeModules()
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

        // Verify that both modules are displayed
        modulesPage.assertModuleDisplayed(module1)
        modulesPage.assertModuleDisplayed(module2)

    }
}