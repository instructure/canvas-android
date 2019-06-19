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
package com.instructure.teacher.ui

import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.TestRail
import com.instructure.espresso.ditto.Ditto
import com.instructure.teacher.ui.utils.*
import okreplay.DittoResponseMod
import okreplay.JsonObjectResponseMod
import okreplay.JsonObjectValueMod
import org.junit.Test

class QuizDetailsPageTest: TeacherTest() {

    @Test
    @Ditto
    @TestRail(ID = "C3109579")
    override fun displaysPageObjects() {
        getToQuizDetailsPage()
        quizDetailsPage.assertPageObjects()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3109579")
    fun displaysCorrectDetails() {
        val quiz = getToQuizDetailsPage()
        quizDetailsPage.assertQuizDetails(quiz)
    }

    @Test
    @Ditto
    @TestRail(ID = "C3109579")
    fun displaysInstructions() {
        getToQuizDetailsPage(withDescription = true)
        quizDetailsPage.assertDisplaysInstructions()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134480")
    fun displaysNoInstructionsMessage() {
        getToQuizDetailsPage()
        quizDetailsPage.assertDisplaysNoInstructionsView()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134481")
    fun displaysClosedAvailability() {
        getToQuizDetailsPage(lockAt = 1.days.ago.iso8601)
        quizDetailsPage.assertQuizClosed()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134482")
    fun displaysNoFromDate() {
        val lockAt = 2.days.fromNow.iso8601
        addDittoMod(getQuizLockDateMod(lockAt))
        addDittoMod(getAssignmentLockDateMod(lockAt))
        getToQuizDetailsPage(lockAt = lockAt)
        quizDetailsPage.assertToFilledAndFromEmpty()
    }

    @Test
    @Ditto
    @TestRail(ID = "C3134483")
    fun displaysNoToDate() {
        getToQuizDetailsPage(unlockAt = 2.days.ago.iso8601)
        quizDetailsPage.assertFromFilledAndToEmpty()
    }

    @Test
    @Ditto
    fun displaysSubmittedDonut() {
        getToQuizDetailsPage(students = 1, submissions = 1)
        quizDetailsPage.assertHasSubmitted()
    }

    @Test
    @Ditto
    fun displaysNotSubmittedDonut() {
        getToQuizDetailsPage(students = 1, submissions = 0)
        quizDetailsPage.assertNotSubmitted()
    }

    private fun getToQuizDetailsPage(
            withDescription: Boolean = false,
            lockAt: String = "",
            unlockAt: String = "",
            students: Int = 0,
            submissions: Int = 0): QuizApiModel {
        val data = seedData(teachers = 1, favoriteCourses = 1, students = students)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val quiz = seedQuizzes(
                courseId = course.id,
                quizzes = 1,
                withDescription = withDescription,
                lockAt = lockAt,
                unlockAt = unlockAt,
                teacherToken = teacher.token).quizList[0]

        for (s in 0 until submissions) {
            seedQuizSubmission(course.id, quiz.id, data.studentsList[s].token, true)
        }

        tokenLogin(teacher)
        routeTo("courses/${course.id}/quizzes/${quiz.id}")
        quizDetailsPage.waitForRender()
        return quiz
    }

    private fun getQuizLockDateMod(dateString: String): DittoResponseMod {
        return JsonObjectResponseMod(
            Regex("""(.*)/api/v1/courses/\d+/quizzes/\d+"""),
            JsonObjectValueMod("lock_at", dateString),
            JsonObjectValueMod("all_dates[0]:lock_at", dateString)
        )
    }

    private fun getAssignmentLockDateMod(dateString: String): DittoResponseMod {
        return JsonObjectResponseMod(
            Regex("""(.*)/api/v1/courses/\d+/assignments/\d+\?(.*)"""),
            JsonObjectValueMod("lock_at", dateString),
            JsonObjectValueMod("all_dates[0]:lock_at", dateString)
        )
    }

}
