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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.student.adapter.AssignmentDateListRecyclerAdapter
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class AssignmentDateListRecyclerAdapterTest : TestCase() {

    private val itemCallback = AssignmentDateListRecyclerAdapter.itemCallback

    @Test
    fun testAreContentsTheSame_sameName() {
        val assignment = Assignment()
        assignment.name = "Assign1"
        TestCase.assertTrue(itemCallback.areContentsTheSame(assignment, assignment))
    }

    @Test
    fun testAreContentsTheSame_differentName() {
        val assignment1 = Assignment()
        assignment1.name = "Assign1"
        val assignment2 = Assignment()
        assignment2.name = "Assign2"
        TestCase.assertFalse(itemCallback.areContentsTheSame(assignment1, assignment2))
    }

    @Test
    fun testAreContentsTheSame_oneNullDueDate() {
        val assignmentDueDate = Assignment(
                name = "Assign1",
                dueAt = Date().toApiString()
        )
        val assignment1 = Assignment(
                name = "Assign1"
        )
        TestCase.assertFalse(itemCallback.areContentsTheSame(assignmentDueDate, assignment1))
        TestCase.assertFalse(itemCallback.areContentsTheSame(assignment1, assignmentDueDate))
        TestCase.assertTrue(itemCallback.areContentsTheSame(assignmentDueDate, assignmentDueDate))
    }
}
