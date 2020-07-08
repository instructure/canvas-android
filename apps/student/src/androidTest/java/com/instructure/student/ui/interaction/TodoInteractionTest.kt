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

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizQuestion
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class TodoInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var quiz: Quiz

    // Todo items should be clickable
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.TODOS, TestCategory.INTERACTION, false)
    fun testClick_todoItemClickable() {

        val data = goToTodos()

        todoPage.assertAssignmentDisplayed(assignment)
        todoPage.selectAssignment(assignment)
        assignmentDetailsPage.verifyAssignmentDetails(assignment)
        Espresso.pressBack() // Back to todo page

        todoPage.assertQuizDisplayed(quiz)
        /* TODO: Check that the quiz is displayed if/when we can do so via WebView
        todoPage.selectQuiz(quiz)
        quizDetailsPage.assertQuizDisplayed(quiz,false,listOf<QuizQuestion>())
        */
    }

    // Seeds ToDos (assignment + quiz) for tomorrow and then navigates to the ToDo page
    fun goToTodos() : MockCanvas {
        var data = MockCanvas.init(
                studentCount = 1,
                teacherCount = 1,
                courseCount = 1,
                favoriteCourseCount = 1
        )

        val student = data.students.first()
        course = data.courses.values.first()
        assignment = data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                dueAt = 1.days.fromNow.iso8601
        )

        quiz = data.addQuizToCourse(
                course = course,
                quizType = Quiz.TYPE_ASSIGNMENT,
                dueAt = 1.days.fromNow.iso8601
        )

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)

        dashboardPage.waitForRender()
        dashboardPage.clickTodoTab()

        return data
    }

}
