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

package com.emeritus.student.test.adapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.ToDo
import com.emeritus.student.adapter.TodoListRecyclerAdapter
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoListRecyclerAdapterTest : TestCase() {
    private var mAdapter: TodoListRecyclerAdapter? = null

    private lateinit var todoItem: ToDo
    private lateinit var assignment: Assignment

    /**
     * Make it so the protected constructor can be called
     */
    class TodoListRecyclerAdapterWrapper(context: Context) : TodoListRecyclerAdapter(context)

    @Before
    fun setup() {
        mAdapter = TodoListRecyclerAdapterWrapper(ApplicationProvider.getApplicationContext())
        todoItem = ToDo()
        assignment = Assignment(name = "item")
    }

    @Test
    fun testAreContentsTheSame_SameTitleFromAssignment() {
        val item = todoItem.copy(assignment = assignment)
        TestCase.assertTrue(mAdapter!!.createItemCallback().areContentsTheSame(item, item))
    }

    @Test
    fun testAreContentsTheSame_SameTitleFromSchedule() {
        val scheduleItem = ScheduleItem(title = "item")
        val item = todoItem.copy(scheduleItem = scheduleItem)

        TestCase.assertTrue(mAdapter!!.createItemCallback().areContentsTheSame(item, item))
    }

    @Test
    fun testAreContentsTheSame_DifferentTitleFromAssignment() {
        val item = todoItem.copy(assignment = assignment)
        val assignment1 = Assignment(name = "item1")
        val item2 = todoItem.copy(assignment = assignment1)

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(item, item2))
    }

    @Test
    fun testAreContentsTheSame_DifferentTitleFromSchedule() {
        val scheduleItem = ScheduleItem(title = "item")
        val item = todoItem.copy(scheduleItem = scheduleItem)
        val scheduleItem1 = ScheduleItem(title = "item1")
        val item2 = todoItem.copy(scheduleItem = scheduleItem1)

        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(item, item2))
    }
}

