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
    @Ditto
    @TestRail(ID = "C3109494")
    fun displaysNoCoursesView() {
        logIn()
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
}
