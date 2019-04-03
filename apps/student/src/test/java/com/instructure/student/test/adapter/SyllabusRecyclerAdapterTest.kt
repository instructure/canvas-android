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
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.student.adapter.SyllabusRecyclerAdapter
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SyllabusRecyclerAdapterTest : TestCase() {
    private var mAdapter: SyllabusRecyclerAdapter? = null

    private lateinit var item: ScheduleItem

    /**
     * Make it so the protected constructor can be called
     */
    class SyllabusRecyclerAdapterWrapper(context: Context) : SyllabusRecyclerAdapter(context)

    @Before
    fun setup() {
        mAdapter = SyllabusRecyclerAdapterWrapper(ApplicationProvider.getApplicationContext())
        item = ScheduleItem(title = "item")
    }

    @Test
    fun areContentsTheSame_NotNullSameDate() {
        val sItem = item.copy(startAt = Date().toApiString())
        TestCase.assertTrue(mAdapter!!.createItemCallback().areContentsTheSame(sItem, sItem))
    }

    @Test
    fun areContentsTheSame_NotNullDifferentDate() {
        val sItem = item.copy(startAt = Date(Calendar.getInstance().timeInMillis + 1000).toApiString())
        val sItem1 = item.copy(startAt = Date(Calendar.getInstance().timeInMillis - 1000).toApiString())
        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(sItem, sItem1))
    }

    @Test
    fun areContentsTheSame_NullDate() {
        val sItem = item.copy(startAt = Date().toApiString())
        val sItem1 = item.copy(startAt = null)
        TestCase.assertFalse(mAdapter!!.createItemCallback().areContentsTheSame(sItem, sItem1))
    }

}
