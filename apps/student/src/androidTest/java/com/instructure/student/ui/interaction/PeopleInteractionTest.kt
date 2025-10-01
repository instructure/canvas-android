/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.User
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.routeTo
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PeopleInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.PEOPLE, TestCategory.INTERACTION)
    fun testClick_openContextCard() {
        // Should be able to view all enrolled users and tap on one to open their context card
        goToPeopleList()
        peopleListPage.selectPerson(personToSelect)
        personDetailsPage.assertIsPerson(personToSelect)
    }

    private lateinit var personToSelect: User

    private fun goToPeopleList() {
        val data = MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 1
        )

        val course = data.courses.values.first()
        val student = data.students[0]
        personToSelect = data.teachers[0]

        tokenLogin(data.domain, data.tokenFor(student)!!, student)
        routeTo("courses/${course.id}/users", data.domain)
        peopleListPage.waitForPage()
    }

}