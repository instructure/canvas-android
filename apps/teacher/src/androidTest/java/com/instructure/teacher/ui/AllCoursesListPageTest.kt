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
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.logIn
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import com.instructure.espresso.ditto.Ditto
import com.instructure.espresso.ditto.DittoMode
import org.junit.Test

class AllCoursesListPageTest : TeacherTest() {

    @Test
    @Ditto
    @TestRail(ID = "C3108901")
    override fun displaysPageObjects() {
        val data = seedData(teachers = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        tokenLogin(teacher)
        coursesListPage.openAllCoursesList()
        allCoursesListPage.assertPageObjects()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3108901")
    fun displaysCourseList() {
        val data = seedData(teachers = 1, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        tokenLogin(teacher)
        coursesListPage.openAllCoursesList()
        allCoursesListPage.assertHasCourses(data.coursesList)
    }
}
