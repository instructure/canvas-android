/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.student.ui

import com.instructure.dataseeding.api.SeedApi
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.espresso.ditto.Ditto
import org.junit.Test

class DashboardPageTest : StudentTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToDashboard()
        dashboardPage.assertPageObjects()
    }

    @Test
    @Ditto
    fun displaysAddCourseMessage() {
        getToDashboard()
        dashboardPage.assertDisplaysAddCourseMessage()
    }

    private fun getToDashboard(courseCount: Int = 1) : SeedApi.SeededDataApiModel {
        val data = seedData(students = 1, courses = courseCount)
        tokenLogin(data.studentsList[0])
        return data
    }

}
