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
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.student.adapter.CalendarListRecyclerAdapter
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class CalendarListRecyclerAdapterTest : TestCase() {

    private val title = "ScheduleItem1"
    private val title2 = "ScheduleItem2"

    private var mAdapter: CalendarListRecyclerAdapter? = null

    /**
     * Make it so the protected constructor can be called
     */
    class CalendarListRecyclerAdapterWrapper(context: Context) : CalendarListRecyclerAdapter(context)

    @Before
    fun setup() {
        mAdapter = CalendarListRecyclerAdapterWrapper(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testAreContentsTheSame_noAssignmentSame() {
        val scheduleItem1 = ScheduleItem(
                title = "ScheduleItem1",
                startAt = Date().toApiString()
        )
        TestCase.assertTrue(mAdapter!!.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem1))
    }

    @Test
    fun testAreContentsTheSame_noAssignmentDifferentName() {
        val date = Date()
        val scheduleItem1 = ScheduleItem(
                title = title,
                startAt = date.toApiString()
        )

        val scheduleItem2 = scheduleItem1.copy(title = title2)

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2))
    }


    @Test
    fun `Two schedule items with no assignment and different date should not be the same`() {
        val startAt1 = Date(Calendar.getInstance().timeInMillis + 1000).toApiString()
        val startAt2 = Date(Calendar.getInstance().timeInMillis - 1000).toApiString()

        val scheduleItem1 = ScheduleItem(
                title = title,
                startAt = startAt1
        )

        val scheduleItem2 = ScheduleItem(
                title = title,
                startAt = startAt2
        )

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2))
    }

    @Test
    fun testAreContentsTheSame_sameAssignment() {
        val scheduleItem1 = ScheduleItem(
                title = title,
                startAt = Date().toApiString()
        )

        val assignment1 = Assignment(
                dueAt = Date().toApiString()
        )

        scheduleItem1.assignment = assignment1
        TestCase.assertTrue(mAdapter!!.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem1))
    }

    @Test
    fun testAreContentsTheSame_differentAssignment() {
        val date = Date()

        val scheduleItem1 = ScheduleItem(
                title = title,
                startAt = date.toApiString()
        )

        val assignment1 = Assignment(
                dueAt = Date(Calendar.getInstance().timeInMillis - 1000).toApiString()
        )
        scheduleItem1.assignment = assignment1

        val scheduleItem2 = scheduleItem1.copy()
        val assignment2 = Assignment(
                dueAt = Date(Calendar.getInstance().timeInMillis + 1000).toApiString()
        )

        scheduleItem2.assignment = assignment2

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2))
    }

    @Test
    fun testAreContentsTheSame_nullAssignment() {
        val date = Date().toApiString()

        val scheduleItem1 = ScheduleItem(
                title = title,
                startAt = date
        )

        val assignment1 = Assignment(
                dueAt = date
        )

        scheduleItem1.assignment = assignment1

        val scheduleItem2 = scheduleItem1.copy()
        val assignment2: Assignment? = null
        scheduleItem2.assignment = assignment2

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2))
    }
}
