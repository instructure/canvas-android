/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e.compose

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.UpdateCourse
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.seedData
import com.instructure.parentapp.utils.extensions.seedDataForK5
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CourseDetailsFrontPageE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COURSE_DETAILS, TestCategory.E2E, SecondaryFeatureCategory.FRONT_PAGE)
    fun testCourseDetailsFrontPageE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, parents = 1, courses = 1)
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val parent = data.parentsList[0]

        Log.d(PREPARATION_TAG, "Seed assignment for '${course.name}' course.")
        val assignment = AssignmentsApi.createAssignment(course.id, teacher.token,
            submissionTypes = listOf(SubmissionType.ON_PAPER),
            withDescription = true,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601
        )

        val url = "https://brinaca.beta.instructure.com/courses/${course.id}/assignments/${assignment.id}"
        val frontPageBody = "This is the front page body <a id=\"assignmentLink\" href=\"$url\">Assignment</a>"

        Log.d(PREPARATION_TAG, "Seed a (front) page to the '${course.name}' course and set the Home Page of the course to the seeded front page.")
        PagesApi.createCoursePage(course.id, teacher.token, frontPage = true, editingRoles = "public", body = frontPageBody)
        CoursesApi.updateCourse(course.id, UpdateCourse(homePage = "wiki"))

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select '${course.name}' course.")
        dashboardPage.waitForRender()
        coursesPage.clickCourseItem(course.name)

        Log.d(STEP_TAG, "Navigate to Front Page Page by selecting Front Page Tab.")
        courseDetailsPage.selectTab("FRONT PAGE")

        Log.d(ASSERTION_TAG, "Assert that the 'FRONT PAGE' tab has been selected.")
        courseDetailsPage.assertTabSelected("FRONT PAGE")

        Log.d(ASSERTION_TAG, "Assert that the body string is displayed and click the link for the assignment.")
        frontPagePage.assertFrontPageBody("This is the front page body Assignment")

        Log.d(STEP_TAG, "Click on the 'assignmentLink' link text on the Front Page.")
        frontPagePage.clickLink("assignmentLink")

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is loaded successfully.")
        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    @E2E
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.COURSE_DETAILS, TestCategory.E2E, SecondaryFeatureCategory.FRONT_PAGE)
    fun testFrontPageTabVisibilityForK5ElementaryCoursesE2E() {

        //Bug Ticket: MBL-19842
        Log.d(PREPARATION_TAG, "Seeding data for K5 sub-account: 2 homeroom courses. The parent observes the K5 elementary student.")
        val data = seedDataForK5(students = 1, teachers = 1, parents = 1, homeroomCourses = 2)
        val courseWithFrontPage = data.coursesList[0]
        val courseWithoutFrontPage = data.coursesList[1]
        val teacher = data.teachersList[0]
        val parent = data.parentsList[0]

        val syllabusBody = "This is the syllabus body of the K5 elementary course."
        val frontPageBody = "This is the front page body of the K5 elementary course."

        Log.d(PREPARATION_TAG, "Seed a front page for '${courseWithFrontPage.name}'.")
        PagesApi.createCoursePage(courseWithFrontPage.id, teacher.token, frontPage = true, editingRoles = "public", body = frontPageBody)

        Log.d(PREPARATION_TAG, "Add a syllabus body to both courses so that both get SYLLABUS and SUMMARY tabs.")
        CoursesApi.updateCourse(courseWithFrontPage.id, UpdateCourse(syllabusBody = syllabusBody))
        CoursesApi.updateCourse(courseWithoutFrontPage.id, UpdateCourse(syllabusBody = syllabusBody))

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select '${courseWithFrontPage.name}' course.")
        dashboardPage.waitForRender()
        coursesPage.clickCourseItem(courseWithFrontPage.name)

        Log.d(ASSERTION_TAG, "Assert that '${courseWithFrontPage.name}' has 4 tabs: GRADES, FRONT PAGE, SYLLABUS, SUMMARY.")
        courseDetailsPage.assertTabCount(4)

        Log.d(ASSERTION_TAG, "Assert that the 'FRONT PAGE' tab IS displayed for the course that has a front page, even though the course home is NOT set to 'wiki'.")
        courseDetailsPage.assertTabDisplayed("FRONT PAGE")

        Log.d(STEP_TAG, "Navigate to the Front Page tab.")
        courseDetailsPage.selectTab("FRONT PAGE")

        Log.d(ASSERTION_TAG, "Assert that the 'FRONT PAGE' tab has been selected and the front page body is displayed.")
        courseDetailsPage.assertTabSelected("FRONT PAGE")
        frontPagePage.assertFrontPageBody(frontPageBody)

        Log.d(STEP_TAG, "Navigate back to the Dashboard and select '${courseWithoutFrontPage.name}' course.")
        Espresso.pressBack()
        coursesPage.clickCourseItem(courseWithoutFrontPage.name)

        Log.d(ASSERTION_TAG, "Assert that '${courseWithoutFrontPage.name}' has 3 tabs: GRADES, SYLLABUS, SUMMARY — no FRONT PAGE tab since no front page was created.")
        courseDetailsPage.assertTabCount(3)

        Log.d(ASSERTION_TAG, "Assert that the 'FRONT PAGE' tab is NOT displayed for the course that has no front page.")
        courseDetailsPage.assertTabDoesNotExist("FRONT PAGE")
    }
}
