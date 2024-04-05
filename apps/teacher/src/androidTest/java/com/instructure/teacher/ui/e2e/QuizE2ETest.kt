/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.seedQuizQuestion
import com.instructure.teacher.ui.utils.seedQuizSubmission
import com.instructure.teacher.ui.utils.seedQuizzes
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizE2ETest: TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun testQuizE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open '${course.name}' course and navigate to Quizzes Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()

        Log.d(STEP_TAG,"Assert that there is no quiz displayed on the page.")
        quizListPage.assertDisplaysNoQuizzesView()

        Log.d(PREPARATION_TAG,"Seed a quiz for the '${course.name}' course. Also, seed a question into the quiz and publish it.")
        val testQuizList = seedQuizzes(courseId = course.id, withDescription = true, dueAt = 3.days.fromNow.iso8601, teacherToken = teacher.token, published = false)
        seedQuizQuestion(courseId = course.id, quizId = testQuizList.quizList[0].id, teacherToken = teacher.token)

        Log.d(STEP_TAG,"Refresh the page. Assert that the quiz is there and click on the previously seeded quiz: '${testQuizList.quizList[0].title}'.")
        quizListPage.refresh()
        quizListPage.clickQuiz(testQuizList.quizList[0].title)

        Log.d(STEP_TAG,"Assert that '${testQuizList.quizList[0].title}' quiz is 'Not Submitted' and it is unpublished.")
        quizDetailsPage.assertNotSubmitted()
        quizDetailsPage.assertQuizUnpublished()

        val newQuizTitle = "This is a new quiz"
        Log.d(STEP_TAG,"Open 'Edit' page and edit the '${testQuizList.quizList[0].title}' quiz's title to: '$newQuizTitle'.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.editQuizTitle(newQuizTitle)

        Log.d(STEP_TAG,"Assert that the quiz name has been changed to: '$newQuizTitle'.")
        quizDetailsPage.assertQuizNameChanged(newQuizTitle)

        Log.d(STEP_TAG,"Open 'Edit' page and switch on the 'Published' checkbox, so publish the '$newQuizTitle' quiz. Click on 'Save'.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.switchPublish()
        editQuizDetailsPage.saveQuiz()

        Log.d(STEP_TAG,"Refresh the page. Assert that '$newQuizTitle' quiz has been unpublished.")
        quizDetailsPage.refresh()
        quizDetailsPage.assertQuizPublished()

        Log.d(PREPARATION_TAG,"Submit the '${testQuizList.quizList[0].title}' quiz.")
        seedQuizSubmission(courseId = course.id, quizId = testQuizList.quizList[0].id, studentToken = student.token)

        Log.d(STEP_TAG,"Refresh the page. Assert that it needs grading because of the previous submission.")
        quizListPage.refresh()
        quizDetailsPage.assertNeedsGrading()
    }

}