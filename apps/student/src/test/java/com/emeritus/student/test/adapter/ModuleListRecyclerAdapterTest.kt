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
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.ContextKeeper
import com.emeritus.student.adapter.ModuleListRecyclerAdapter
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleListRecyclerAdapterTest : TestCase() {
    private lateinit var mAdapter: ModuleListRecyclerAdapter

    private lateinit var moduleItem: ModuleItem

    /**
     * Make it so the protected constructor can be called
     */
    class ModuleListRecyclerAdapterWrapper(context: Context) : ModuleListRecyclerAdapter(context)

    @Before
    fun setup() {
        ContextKeeper.appContext = ApplicationProvider.getApplicationContext()
        mAdapter = ModuleListRecyclerAdapterWrapper(ApplicationProvider.getApplicationContext())
        moduleItem = ModuleItem(title = "item")
    }

    @Test
    fun testAreContentsTheSame_SameModule() {
        TestCase.assertTrue(mAdapter.createItemCallback().areContentsTheSame(moduleItem, moduleItem))
    }

    @Test
    fun testAreContentsTheSame_DiffModule() {
        val item1 = moduleItem.copy(title = "item1")
        TestCase.assertFalse(mAdapter.createItemCallback().areContentsTheSame(moduleItem, item1))
    }
}
