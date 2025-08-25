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
package com.instructure.pandautils.features.inbox.details

import android.content.Context
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxDetailsBehaviorTest {

    @Test
    fun `default getShowBackButton returns true`() {
        val behavior = InboxDetailsBehavior()
        val context: Context = mockk(relaxed = true)

        val result = behavior.getShowBackButton(context)

        assertTrue(result)
    }

    @Test
    fun `default shouldRestrictDeleteConversation returns false`() = runTest {
        val behavior = InboxDetailsBehavior()

        val result = behavior.shouldRestrictDeleteConversation()

        assertFalse(result)
    }

    @Test
    fun `default shouldRestrictReplyAll returns false`() = runTest {
        val behavior = InboxDetailsBehavior()

        val result = behavior.shouldRestrictReplyAll()

        assertFalse(result)
    }
}
