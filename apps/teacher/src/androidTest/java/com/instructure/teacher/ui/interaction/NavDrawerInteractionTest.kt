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

package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.User
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.openLeftSideMenu
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class NavDrawerInteractionTest: TeacherTest() {

    @Test
    override fun displaysPageObjects() {
        getToNavDrawerMenu()
        navDrawerPage.assertPageObjects()
    }

    @Test
    fun displaysNavDrawerDetails() {
        val teacher = getToNavDrawerMenu()
        navDrawerPage.assertProfileDetails(teacher)
    }

    private fun getToNavDrawerMenu(): User {
        val data = MockCanvas.init(teacherCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openLeftSideMenu()
        return teacher
    }
}
