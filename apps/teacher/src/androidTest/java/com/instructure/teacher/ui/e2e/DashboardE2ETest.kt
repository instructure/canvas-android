/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

private const val STEP_TAG = "DashboardE2ETest #STEP# "
private const val PREPARATION_TAG = "DashboardE2ETest #PREPARATION# "

@HiltAndroidTest
class DashboardE2ETest : TeacherTest() {

    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testDashboardE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 2)
        val teacher = data.teachersList[0]
        val course1 = data.coursesList[0]
        val course2 = data.coursesList[1]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG,"Assert that the ${course1.name} and ${course2.name} courses are displayed.")
        dashboardPage.assertDisplaysCourses()
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG,"Click on 'See All' button.")
        dashboardPage.clickSeeAll()

        Log.d(STEP_TAG,"Refresh the page. Assert that both of the courses, ${course1.name} and ${course2.name} are displayed.")
        refresh()
        for (course in data.coursesList) {
            allCoursesListPage.assertDisplaysCourse(course)
        }

        Log.d(STEP_TAG,"Navigate back to the Dashboard Page.")
        allCoursesListPage.navigateBack()

        Log.d(STEP_TAG,"Click on 'Edit Courses' and toggle 'Favourite' icon of ${course2.name} course")
        dashboardPage.openEditCoursesListPage()
        editCoursesListPage.toggleFavoritingCourse(course2.name)

        Log.d(STEP_TAG,"Navigate back to Dashboard Page and refresh it.")
        editCoursesListPage.navigateBack()
        refresh()

        Log.d(STEP_TAG,"Assert that only the favourited course (${course2.name} is displayed.")
        dashboardPage.assertDisplaysCourse(course2)

        Log.d(STEP_TAG,"Opens ${course2.name} course and assert if Course Details Page has been opened. Navigate back to Dashboard Page.")
        dashboardPage.assertOpensCourse(course2)
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on 'Edit Courses' and untoggle 'Favourite' icon of ${course2.name} course.")
        dashboardPage.openEditCoursesListPage()
        editCoursesListPage.toggleFavoritingCourse(course2.name)

        Log.d(STEP_TAG,"Navigate back to Dashboard Page and refresh it.")
        editCoursesListPage.navigateBack()
        refresh()

        Log.d(STEP_TAG,"Assert that both of the courses, ${course1.name} and ${course2.name} are displayed.")
        dashboardPage.assertDisplaysCourse(course1)
        dashboardPage.assertDisplaysCourse(course2)
    }
}