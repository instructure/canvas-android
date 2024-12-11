/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.utils

import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.api.SeedApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.parentapp.features.login.LoginActivity


fun ParentTest.tokenLogin(user: CanvasUserApiModel) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            user.token,
            user.domain,
            User(
                id = user.id,
                name = user.name,
                shortName = user.shortName,
                avatarUrl = user.avatarUrl,
                effective_locale = "en" // Needed so we don't restart for custom languages (system.exit(0) kills the test process)
            )
        )
    }
    dashboardPage.assertPageObjects()
}

fun CanvasTest.tokenLogin(domain: String, token: String, user: User, assertDashboard: Boolean = true) {
    activityRule.runOnUiThread {
        (originalActivity as LoginActivity).loginWithToken(
            token,
            domain,
            user
        )
    }

    if (assertDashboard && this is ParentTest) {
        dashboardPage.assertPageObjects()
    }
}

fun seedData(
    teachers: Int = 0,
    tas: Int = 0,
    pastCourses: Int = 0,
    courses: Int = 0,
    students: Int = 0,
    parents: Int = 0,
    favoriteCourses: Int = 0,
    homeroomCourses: Int = 0,
    announcements: Int = 0,
    locked: Boolean = false,
    discussions: Int = 0,
    syllabusBody: String? = null,
    gradingPeriods: Boolean = false,
    modules: Int = 0
): SeedApi.SeededDataApiModel {

    val request = SeedApi.SeedDataRequest (
        teachers = teachers,
        TAs = tas,
        students = students,
        parents = parents,
        pastCourses = pastCourses,
        courses = courses,
        favoriteCourses = favoriteCourses,
        homeroomCourses = homeroomCourses,
        gradingPeriods = gradingPeriods,
        discussions = discussions,
        announcements = announcements,
        locked = locked,
        syllabusBody = syllabusBody,
        modules = modules
    )
    return SeedApi.seedData(request)
}
