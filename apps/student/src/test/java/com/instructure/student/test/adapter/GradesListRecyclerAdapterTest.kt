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
 *
 */

package com.instructure.student.test.adapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.student.features.grades.GradesListRecyclerAdapter
import com.instructure.student.features.grades.GradesListRepository
import io.mockk.mockk
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GradesListRecyclerAdapterTest : TestCase() {
    private var adapter: GradesListRecyclerAdapter? = null

    private val repository: GradesListRepository = mockk(relaxed = true)

    /**
     * Make it so the protected constructor can be called
     */
    class GradesListRecyclerAdapterWrapper(
        context: Context,
        repository: GradesListRepository
    ) : GradesListRecyclerAdapter(
        context,
        onGradingPeriodResponse = {},
        repository = repository
    )

    @Before
    fun setup() {
        adapter = GradesListRecyclerAdapterWrapper(ApplicationProvider.getApplicationContext(), repository)
    }

    @Test
    fun testAreContentsTheSame_SameNameAndPoints() {
        val assignment = Assignment(name = "assignment", pointsPossible = 0.0)
        Assert.assertTrue(adapter!!.createItemCallback().areContentsTheSame(assignment, assignment))
    }

    @Test
    fun testAreContentsTheSame_DifferentName() {
        val assignment1 = Assignment(name = "assignment1", pointsPossible = 0.0)
        val assignment2 = Assignment(name = "assignment2", pointsPossible = 0.0)

        Assert.assertFalse(adapter!!.createItemCallback().areContentsTheSame(assignment1, assignment2))
    }

    @Test
    fun testAreContentsTheSame_DifferentScore() {
        val assignment1 = Assignment(name = "assignment1", pointsPossible = 0.0)
        val assignment2 = Assignment(name = "assignment1", pointsPossible = 1.0)

        Assert.assertFalse(adapter!!.createItemCallback().areContentsTheSame(assignment1, assignment2))
    }

    @Test
    fun testAreContentsTheSame_SameWithSubmission() {
        val submission = Submission(grade = "A")
        val assignment = Assignment(name = "assignment", pointsPossible = 0.0, submission = submission)

        Assert.assertTrue(adapter!!.createItemCallback().areContentsTheSame(assignment, assignment))
    }

    @Test
    fun testAreContentsTheSame_SameWithSubmissionNullChange() {
        val submission1 = Submission(grade = "A")
        val assignment1 = Assignment(name = "assignment", pointsPossible = 0.0, submission = submission1)
        val assignment2 = Assignment(name = "assignment1", pointsPossible = 0.0, submission = null)

        Assert.assertFalse(adapter!!.createItemCallback().areContentsTheSame(assignment1, assignment2))
    }

    @Test
    fun testAreContentsTheSame_SameWithSubmissionNullGrade() {
        val submission1 = Submission(grade = "A")
        val assignment1 = Assignment(name = "assignment", pointsPossible = 0.0, submission = submission1)

        val submission2 = Submission(grade = null)
        val assignment2 = Assignment(name = "assignment1", pointsPossible = 0.0, submission = submission2)

        Assert.assertFalse(adapter!!.createItemCallback().areContentsTheSame(assignment1, assignment2))
    }
}
