/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.teacher.features.inbox.details

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherInboxDetailsBehaviorTest {

    private val behavior = TeacherInboxDetailsBehavior()

    @Test
    fun `getShowBackButton returns false for tablet`() {
        val context: Context = mockk(relaxed = true)
        every { context.resources.getBoolean(any()) } returns true // Mock tablet

        val result = behavior.getShowBackButton(context)

        assertFalse(result)
    }

    @Test
    fun `getShowBackButton returns true for non-tablet`() {
        val context: Context = mockk(relaxed = true)
        every { context.resources.getBoolean(any()) } returns false // Mock non-tablet

        val result = behavior.getShowBackButton(context)

        assertTrue(result)
    }
}
