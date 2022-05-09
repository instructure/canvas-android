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
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class QuizE2ETest: TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun testQuizE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Quizzes Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()

        Log.d(STEP_TAG,"Assert that there is no quiz displayed on the page.")
        quizListPage.assertDisplaysNoQuizzesView()

        Log.d(PREPARATION_TAG,"Seed a quiz for the ${course.name} course. Also, seed a question into the quiz and publish it.")
        val quizPublished = seedQuizzes(
                courseId = course.id,
                withDescription = true,
                dueAt = 3.days.fromNow.iso8601,
                teacherToken = teacher.token,
                published = false
        )

        seedQuizQuestion(
                courseId = course.id,
                quizId = quizPublished.quizList[0].id,
                teacherToken = teacher.token
        )

        publishQuiz(
                courseId = course.id,
                quizId = quizPublished.quizList[0].id,
                teacherToken = teacher.token
        )

        Log.d(STEP_TAG,"Refresh the page. Assert that the quiz is there and click on the previously seeded quiz: ${quizPublished.quizList[0].title}.")
        quizListPage.refresh()
        quizListPage.clickQuiz(quizPublished.quizList[0])
        Log.d(STEP_TAG,"Assert that ${quizPublished.quizList[0].title} quiz is not published. Navigate back.")
        quizDetailsPage.assertNotSubmitted()
        Espresso.pressBack()

        Log.d(PREPARATION_TAG,"Submit the ${quizPublished.quizList[0].title} quiz.")
        seedQuizSubmission(
                courseId = course.id,
                quizId = quizPublished.quizList[0].id,
                studentToken = student.token
        )

        Log.d(STEP_TAG,"Refresh the page. Click on ${quizPublished.quizList[0].title} quiz and assert that it needs grading because of the previous submission.")
        quizListPage.refresh()
        quizListPage.clickQuiz(quizPublished.quizList[0])
        quizDetailsPage.refresh()
        quizDetailsPage.assertNeedsGrading()
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun editQuizE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId} , password: ${teacher.password}")
        tokenLogin(teacher)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Quizzes Page.")
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()

        Log.d(STEP_TAG,"Assert that there is no quiz displayed on the page.")
        quizListPage.assertDisplaysNoQuizzesView()

        Log.d(PREPARATION_TAG,"Seed a quiz for the ${course.name} course. Also, seed a question into the quiz and publish it.")
        val quizPublished = seedQuizzes(
                courseId = course.id,
                withDescription = true,
                dueAt = 3.days.fromNow.iso8601,
                teacherToken = teacher.token,
                published = false
        )

        seedQuizQuestion(
                courseId = course.id,
                quizId = quizPublished.quizList[0].id,
                teacherToken = teacher.token
        )

        publishQuiz(
                courseId = course.id,
                quizId = quizPublished.quizList[0].id,
                teacherToken = teacher.token
        )

        Log.d(STEP_TAG,"Refresh the page. Click on ${quizPublished.quizList[0].title} quiz.")
        quizListPage.refresh()
        quizListPage.clickQuiz(quizPublished.quizList[0])

        Log.d(STEP_TAG,"Assert that ${quizPublished.quizList[0].title} quiz is 'Not Submitted' and it is published.")
        quizDetailsPage.assertNotSubmitted()
        quizDetailsPage.assertQuizPublished()

        val newQuizTitle = "This is a new quiz"
        Log.d(STEP_TAG,"Open 'Edit' page and edit the ${quizPublished.quizList[0].title} quiz's title to: $newQuizTitle.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.editQuizTitle(newQuizTitle)

        Log.d(STEP_TAG,"Assert that the quiz name has been changed to: $newQuizTitle.")
        quizDetailsPage.assertQuizNameChanged(newQuizTitle)

        Log.d(STEP_TAG,"Open 'Edit' page and switch off the 'Published' checkbox, so unpublish the $newQuizTitle quiz. Click on 'Save'.")
        quizDetailsPage.openEditPage()
        editQuizDetailsPage.switchPublish()
        editQuizDetailsPage.saveQuiz()

        Log.d(STEP_TAG,"Refresh the page. Assert that $newQuizTitle quiz has been unpublished.")
        quizDetailsPage.refresh()
        quizDetailsPage.assertQuizUnpublished()
    }
}