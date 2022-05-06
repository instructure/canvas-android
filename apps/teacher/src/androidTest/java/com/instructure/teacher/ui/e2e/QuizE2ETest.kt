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
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun testQuizE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]


        tokenLogin(teacher)
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()
        quizListPage.assertDisplaysNoQuizzesView()

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

        quizListPage.refresh()
        quizListPage.clickQuiz(quizPublished.quizList[0])
        quizDetailsPage.assertNotSubmitted()
        Espresso.pressBack()

        seedQuizSubmission(
                courseId = course.id,
                quizId = quizPublished.quizList[0].id,
                studentToken = student.token
        )

        quizListPage.refresh()
        quizListPage.clickQuiz(quizPublished.quizList[0])
        quizDetailsPage.refresh()
        quizDetailsPage.assertNeedsGrading()
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.QUIZZES, TestCategory.E2E)
    fun editQuizE2E() {
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]


        tokenLogin(teacher)
        dashboardPage.openCourse(course.name)
        courseBrowserPage.openQuizzesTab()
        quizListPage.assertDisplaysNoQuizzesView()

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

        quizListPage.refresh()
        quizListPage.clickQuiz(quizPublished.quizList[0])
        quizDetailsPage.assertNotSubmitted()
        quizDetailsPage.assertQuizPublished()

        quizDetailsPage.openEditPage()
        editQuizDetailsPage.editQuizTitle("This is a new quiz")
        quizDetailsPage.assertQuizNameChanged("This is a new quiz")

        quizDetailsPage.openEditPage()
        editQuizDetailsPage.switchPublish()
        editQuizDetailsPage.saveQuiz()
        quizDetailsPage.refresh()
        quizDetailsPage.assertQuizUnpublished()
    }
}