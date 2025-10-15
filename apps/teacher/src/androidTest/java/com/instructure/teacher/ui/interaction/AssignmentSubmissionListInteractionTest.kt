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
 */
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeDifferentiationTagsManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.DifferentiationTagsQuery
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.managers.graphql.DifferentiationTagsManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.pandautils.di.DifferentiationTagsModule
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class, DifferentiationTagsModule::class)
class AssignmentSubmissionListInteractionTest : TeacherComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    // Default to no differentiation tags - tests that need tags will override this
    @BindValue
    @JvmField
    var differentiationTagsManager: DifferentiationTagsManager = FakeDifferentiationTagsManager()

    @Test
    override fun displaysPageObjects() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertPageObjects()
    }

    @Test
    fun displaysNoSubmissionsView() {
        goToAssignmentSubmissionListPage(
                students = 0,
                submissions = 0
        )
        assignmentSubmissionListPage.assertEmptyViewDisplayed()
    }

    @Test
    fun filterLateSubmissions() {
        goToAssignmentSubmissionListPage(
                dueAt = 7.days.ago.iso8601
        )
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmittedLate()
        assignmentSubmissionListPage.clickFilterDialogDone()
        assignmentSubmissionListPage.assertFilterLabelText("Submitted Late")
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun filterUngradedSubmissions() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterUngraded()
        assignmentSubmissionListPage.clickFilterDialogDone()
        assignmentSubmissionListPage.assertFilterLabelText("Haven't Been Graded")
        assignmentSubmissionListPage.assertHasSubmission()
    }

    @Test
    fun displaysAssignmentStatusSubmitted() {
        goToAssignmentSubmissionListPage()
        assignmentSubmissionListPage.assertSubmissionStatusSubmitted()
    }

    @Test
    fun displaysAssignmentStatusNotSubmitted() {
        goToAssignmentSubmissionListPage(
                students = 1,
                submissions = 0
        )
        assignmentSubmissionListPage.assertSubmissionStatusNotSubmitted()
    }

    @Test
    fun displaysAssignmentStatusLate() {
        goToAssignmentSubmissionListPage(
                dueAt = 7.days.ago.iso8601
        )
        assignmentSubmissionListPage.assertSubmissionStatusLate()
    }

    @Test
    fun messageStudentsWho() {
        val data = goToAssignmentSubmissionListPage(
                students = 1
        )
        val student = data.students[0]
        assignmentSubmissionListPage.clickAddMessage()

        inboxComposePage.assertRecipientSelected(student.shortName!!)
    }

    @Test
    fun filterBySection() {
        val data = goToAssignmentSubmissionListPage(
                students = 2,
                submissions = 2,
                createSections = true
        )
        val section = data.courses.values.first().sections.first()

        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.filterBySection(section.name)
        assignmentSubmissionListPage.clickFilterDialogDone()

        // Should show all students since they all belong to the same section
        assignmentSubmissionListPage.assertHasSubmission(expectedCount = 2)
    }

    @Test
    fun filterByCustomGradeStatus() {
        goToAssignmentSubmissionListPage()

        assignmentSubmissionListPage.clickFilterButton()

        // Verify custom status options are available (from FakeCustomGradeStatusesManager)
        assignmentSubmissionListPage.assertSubmissionFilterOption("Custom Status 1")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Custom Status 2")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Custom Status 3")

        // Select a custom status filter
        assignmentSubmissionListPage.clickFilterCustomStatus("Custom Status 1")
        assignmentSubmissionListPage.clickFilterDialogDone()

        // Custom statuses are not assigned by default in MockCanvas,
        // so this should show no submissions
        assignmentSubmissionListPage.assertHasNoSubmission()
    }

    @Test
    fun filterMultipleStatusesWithOrLogic() {
        goToAssignmentSubmissionListPage(
                students = 2,
                submissions = 1
        )

        assignmentSubmissionListPage.clickFilterButton()

        // Select multiple filters - should use OR logic
        assignmentSubmissionListPage.clickFilterUngraded()
        assignmentSubmissionListPage.clickFilterSubmitted()
        assignmentSubmissionListPage.clickFilterDialogDone()

        // Should show submissions matching either ungraded OR submitted
        // With 2 students and 1 submission, we have 1 ungraded submission
        assignmentSubmissionListPage.assertHasSubmission(expectedCount = 1)
    }

    @Test
    fun filterIncludeStudentsWithoutDifferentiationTags() {
        // Set up differentiation tags for this test
        differentiationTagsManager = FakeDifferentiationTagsManager(
            listOf(
                DifferentiationTagsQuery.Group(
                    _id = "tag1",
                    name = "Advanced Learners",
                    nonCollaborative = true,
                    membersConnection = null
                )
            )
        )

        goToAssignmentSubmissionListPage()

        assignmentSubmissionListPage.clickFilterButton()

        // Select to include students without tags
        assignmentSubmissionListPage.clickIncludeStudentsWithoutTags()
        assignmentSubmissionListPage.clickFilterDialogDone()

        // Should show all students since none have tags assigned
        assignmentSubmissionListPage.assertHasSubmission(expectedCount = 1)
    }

    @Test
    fun filterMultipleDifferentiationTagsWithOrLogic() {
        // Set up differentiation tags for this test
        differentiationTagsManager = FakeDifferentiationTagsManager(
            listOf(
                DifferentiationTagsQuery.Group(
                    _id = "tag1",
                    name = "Advanced Learners",
                    nonCollaborative = true,
                    membersConnection = null
                ),
                DifferentiationTagsQuery.Group(
                    _id = "tag2",
                    name = "English Language Learners",
                    nonCollaborative = true,
                    membersConnection = null
                ),
                DifferentiationTagsQuery.Group(
                    _id = "tag3",
                    name = "Special Education",
                    nonCollaborative = true,
                    membersConnection = null
                )
            )
        )

        goToAssignmentSubmissionListPage(
            students = 2,
            submissions = 2
        )

        assignmentSubmissionListPage.clickFilterButton()

        // Select multiple differentiation tag filters - should use OR logic
        assignmentSubmissionListPage.clickFilterDifferentiationTag("Advanced Learners")
        assignmentSubmissionListPage.clickFilterDifferentiationTag("English Language Learners")
        assignmentSubmissionListPage.clickFilterDialogDone()

        // Since no students have tags assigned, should show no submissions
        assignmentSubmissionListPage.assertHasNoSubmission()
    }

    @Test
    fun filterDifferentiationTagsWithIncludeWithoutTags() {
        // Set up differentiation tags for this test
        differentiationTagsManager = FakeDifferentiationTagsManager(
            listOf(
                DifferentiationTagsQuery.Group(
                    _id = "tag1",
                    name = "Advanced Learners",
                    nonCollaborative = true,
                    membersConnection = null
                )
            )
        )

        goToAssignmentSubmissionListPage(
            students = 2,
            submissions = 2
        )

        assignmentSubmissionListPage.clickFilterButton()

        // Select a tag AND include students without tags
        assignmentSubmissionListPage.clickFilterDifferentiationTag("Advanced Learners")
        assignmentSubmissionListPage.clickIncludeStudentsWithoutTags()
        assignmentSubmissionListPage.clickFilterDialogDone()

        // Should show all students since they don't have tags (included by the checkbox)
        assignmentSubmissionListPage.assertHasSubmission(expectedCount = 2)
    }

    private fun goToAssignmentSubmissionListPage(
            students: Int = 1,
            submissions: Int = 1,
            dueAt: String? = null,
            createSections: Boolean = false
    ): MockCanvas {
        val data = MockCanvas.init(
            teacherCount = 1,
            studentCount = students,
            courseCount = 1,
            favoriteCourseCount = 1,
            createSections = createSections
        )
        val course = data.courses.values.first()
        val teacher = data.teachers[0]

        // TODO: Make this part of MockCanvas.init()
        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                dueAt = dueAt
        )

        for (s in 0 until submissions) {
            if(students == 0) {
                throw Exception("Can't specify submissions without students")
            }
            data.addSubmissionForAssignment(
                    assignmentId = assignment.id,
                    userId = data.students[0].id,
                    type = "online_text_entry",
                    body = "A submission"
            )
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()

        return data
    }
}
