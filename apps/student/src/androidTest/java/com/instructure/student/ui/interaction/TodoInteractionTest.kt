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

import androidx.compose.ui.platform.ComposeView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.StubLandscape
import com.instructure.canvas.espresso.StubMultiAPILevel
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.junit.Test

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class TodoInteractionTest : StudentComposeTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var quiz: Quiz

    // Todo items should be clickable
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.INTERACTION)
    fun testClick_todoItemClickable() {

        val data = goToTodos()

        todoPage.assertAssignmentDisplayed(assignment)
        todoPage.selectAssignment(assignment)
        assignmentDetailsPage.assertAssignmentDetails(assignment)
        Espresso.pressBack() // Back to todo page

        todoPage.assertQuizDisplayed(quiz)
        /* TODO: Check that the quiz is displayed if/when we can do so via WebView
        todoPage.selectQuiz(quiz)
        quizDetailsPage.assertQuizDisplayed(quiz,false,listOf<QuizQuestion>())
        */
    }

    @Test
    @StubLandscape("Stubbed because on lowres device in landscape mode, the space is too narrow to scroll properly. Will be refactored and running when we changed to non-lowres device on nightly runs.")
    @StubMultiAPILevel("Somehow the 'OK' button within chooseFavoriteCourseFilter row is not clickable and not shown on the layout inspector as well.")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.TODOS, TestCategory.INTERACTION)
    fun testFilters() {
        val data = goToTodos(courseCount = 2, favoriteCourseCount = 1)
        val favoriteCourse = data.courses.values.first {course -> course.isFavorite}
        val notFavoriteCourse = data.courses.values.first {course -> !course.isFavorite}

        val favoriteQuiz = data.courseQuizzes[favoriteCourse.id]!!.first()
        val notFavoriteQuiz = data.courseQuizzes[notFavoriteCourse.id]!!.first()

        todoPage.assertQuizDisplayed(favoriteQuiz)
        todoPage.assertQuizDisplayed(notFavoriteQuiz)

        todoPage.chooseFavoriteCourseFilter()

        todoPage.assertQuizDisplayed(favoriteQuiz)
        todoPage.assertQuizNotDisplayed(notFavoriteQuiz)

        todoPage.clearFilter()

        todoPage.assertQuizDisplayed(favoriteQuiz)
        todoPage.assertQuizDisplayed(notFavoriteQuiz)
    }


    // Seeds ToDos (assignment + quiz) for tomorrow and then navigates to the ToDo page
    fun goToTodos(courseCount: Int = 1, favoriteCourseCount: Int = 1) : MockCanvas {
        var data = MockCanvas.init(
                studentCount = 1,
                teacherCount = 1,
                courseCount = courseCount,
                favoriteCourseCount = favoriteCourseCount
        )

        val student = data.students.first()
        for(course in data.courses.values) {
            assignment = data.addAssignment(
                    courseId = course.id,
                    submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
                    dueAt = 1.days.fromNow.iso8601
            )

            quiz = data.addQuizToCourse(
                    course = course,
                    quizType = Quiz.TYPE_ASSIGNMENT,
                    dueAt = 1.days.fromNow.iso8601
            )
        }

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        dashboardPage.waitForRender()
        dashboardPage.clickTodoTab()

        return data
    }

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = Matchers.allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }

}
