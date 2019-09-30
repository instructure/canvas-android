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

import com.instructure.espresso.TestRail
import com.instructure.teacher.ui.utils.*
import com.instructure.espresso.ditto.Ditto
import com.instructure.espresso.ditto.DittoMode
import org.junit.Test

class CoursesListPageTest : TeacherTest() {

    @Test
    @Ditto
    @TestRail(ID = "C3108898")
    override fun displaysPageObjects() {
        logIn()
        coursesListPage.assertPageObjects()
    }

    @Test
    @Ditto(mode = DittoMode.LIVE)
    @TestRail(ID = "C3109494")
    fun displaysNoCoursesView() {
        val data = seedData(teachers = 1, pastCourses = 1)
        tokenLogin(data.teachersList[0])
        coursesListPage.assertDisplaysNoCoursesView()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3108898")
    fun displaysCourseList() {
        val data = seedData(teachers = 1, favoriteCourses = 3)
        val teacher = data.teachersList[0]
        tokenLogin(teacher)

        coursesListPage.assertHasCourses(data.favoriteCourses)
    }

    @Test
    @Ditto
    fun displaysCourseListWithOnlyUnpublishedCourses() {
        val data = seedData(teachers = 1, courses = 1, publishCourses = false)
        val teacher = data.teachersList[0]
        tokenLogin(teacher)

        coursesListPage.assertHasCourses(data.coursesList)
    }
}
