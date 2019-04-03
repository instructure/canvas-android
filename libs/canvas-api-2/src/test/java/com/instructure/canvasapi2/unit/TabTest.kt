/*
 * Copyright (C) 2017 - present Instructure, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Tab

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TabTest : Assert() {

    private lateinit var tabby1: Tab
    private lateinit var id: String
    private lateinit var label: String

    @Before
    fun setUp() {
        id = "id"
        label = "label"
        tabby1 = Tab(tabId = id, label = label)
    }

    @Test
    fun newInstance() {
        Assert.assertNotNull(tabby1)
    }

    //region equals
    @Test
    fun equals_TestSameObject() {
        val tabby2 = tabby1

        Assert.assertTrue(tabby1 == tabby2)
    }

    @Test
    fun equals_TestDifferentClass() {
        val tabby2 = "String class"

        Assert.assertFalse(tabby1::class == tabby2::class)
    }

    @Test
    fun equals_TestSameValues() {
        val tabby2 = Tab(tabId = "id", label = "label")

        Assert.assertTrue(tabby1 == tabby2)
    }

    @Test
    fun equals_TestDifferentValues() {
        val tabby2 = Tab(tabId = "id", label = "label_extra")

        Assert.assertFalse(tabby1 == tabby2)
    }

    @Test
    fun toString_TestNotNull() {
        Assert.assertNotNull(tabby1)
    }

    @Test
    fun toString_TestCorrectFormat() {
        Assert.assertEquals("$id:$label", tabby1.toString())
    }
}
