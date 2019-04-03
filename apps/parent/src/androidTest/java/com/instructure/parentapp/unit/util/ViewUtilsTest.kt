/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.parentapp.unit.util

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ViewUtils
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class ViewUtilsTest {

    private var mMockContext: Context? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mMockContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    @Throws(Exception::class)
    fun applyKerning() {
        val str = "George was a curious monkey"
        val spannable = ViewUtils.applyKerning(str, 1f)
        assertNotEquals(str, spannable.toString())
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentIcon_ONLINE_QUIZ() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(
                Assignment.SubmissionType.ONLINE_QUIZ.apiString
            )
        )
        @DrawableRes val iconRes = ViewUtils.getAssignmentIcon(assignment)
        assertTrue(iconRes == R.drawable.ic_cv_quizzes_fill)
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentIcon_DISCUSSION_TOPIC() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(
                Assignment.SubmissionType.DISCUSSION_TOPIC.apiString
            )
        )
        @DrawableRes val iconRes = ViewUtils.getAssignmentIcon(assignment)
        assertTrue(iconRes == R.drawable.ic_cv_discussions_fill)
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentIcon_OTHERS() {
        val assignment = Assignment(
            submissionTypesRaw = listOf(
                Assignment.SubmissionType.NONE.apiString,
                Assignment.SubmissionType.ON_PAPER.apiString,
                Assignment.SubmissionType.EXTERNAL_TOOL.apiString,
                Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                Assignment.SubmissionType.ONLINE_URL.apiString,
                Assignment.SubmissionType.MEDIA_RECORDING.apiString,
                Assignment.SubmissionType.ATTENDANCE.apiString,
                Assignment.SubmissionType.NOT_GRADED.apiString
            )
        )
        @DrawableRes val iconRes = ViewUtils.getAssignmentIcon(assignment)
        assertTrue(iconRes == R.drawable.ic_cv_assignments_fill)
    }

    @Test
    @Throws(Exception::class)
    fun getAssignmentIconOrder() {
        /**
         * Tests the order that the icons are retrieved.
         * Quizzes, Discussion Topics, Assignment icons in that order
         *
         * To properly test the order we need to test the method 3 times for each icon type
         */

        var assignment = Assignment(
            submissionTypesRaw = listOf(
                Assignment.SubmissionType.NONE.apiString,
                Assignment.SubmissionType.ON_PAPER.apiString,
                Assignment.SubmissionType.EXTERNAL_TOOL.apiString,
                Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
                Assignment.SubmissionType.ONLINE_URL.apiString,
                Assignment.SubmissionType.MEDIA_RECORDING.apiString,
                Assignment.SubmissionType.ATTENDANCE.apiString,
                Assignment.SubmissionType.NOT_GRADED.apiString
            )
        )
        @DrawableRes var iconRes: Int

        // Add all other submission types
        iconRes = ViewUtils.getAssignmentIcon(assignment)
        assertTrue(iconRes == R.drawable.ic_cv_assignments_fill)

        // Add the one we want to test
        assignment = assignment.copy(
            submissionTypesRaw = assignment.submissionTypesRaw + Assignment.SubmissionType.DISCUSSION_TOPIC.apiString
        )
        iconRes = ViewUtils.getAssignmentIcon(assignment)
        assertTrue(iconRes == R.drawable.ic_cv_discussions_fill)

        // Add the next one we want to test
        assignment = assignment.copy(
            submissionTypesRaw = assignment.submissionTypesRaw + Assignment.SubmissionType.ONLINE_QUIZ.apiString
        )
        iconRes = ViewUtils.getAssignmentIcon(assignment)
        assertTrue(iconRes == R.drawable.ic_cv_quizzes_fill)
    }

    @Test
    @Throws(Exception::class)
    fun getGradeText() {
        val score = 2.0
        val pointsPossible = 100
        val graded = mMockContext!!.resources.getString(R.string.submitted)

        //Test Graded
        val gradedText = ViewUtils.getGradeText(AssignmentUtils2.ASSIGNMENT_STATE_GRADED, score, pointsPossible.toDouble(), mMockContext)
        assertTrue(gradedText.contains(graded))
    }

    @Test
    @Throws(Exception::class)
    fun getLateText() {
        val score = 2.0
        val pointsPossible = 100
        val late = mMockContext!!.resources.getString(R.string.late)

        val lateAssignmentStates = ArrayList<Int>()
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_DROPPED)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_DUE)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_MISSING)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE)
        lateAssignmentStates.add(AssignmentUtils2.ASSIGNMENT_STATE_UNKNOWN)

        // Test Late
        for (state in lateAssignmentStates) {
            assertTrue(ViewUtils.getGradeText(state, score, pointsPossible.toDouble(), mMockContext).contains(late))
        }
    }
}
