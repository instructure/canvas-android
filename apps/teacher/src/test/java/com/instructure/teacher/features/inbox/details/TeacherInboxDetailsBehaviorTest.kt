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
import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherInboxDetailsBehaviorTest {

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val behavior = TeacherInboxDetailsBehavior(featureFlagProvider)

    @Test
    fun `shouldRestrictDeleteConversation returns true when feature flag is enabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns true

        val result = behavior.shouldRestrictDeleteConversation()

        assertTrue(result)
    }

    @Test
    fun `shouldRestrictDeleteConversation returns false when feature flag is disabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns false

        val result = behavior.shouldRestrictDeleteConversation()

        assertFalse(result)
    }

    @Test
    fun `shouldRestrictDeleteConversation returns false when feature flag throws exception`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } throws RuntimeException("Network error")

        val result = behavior.shouldRestrictDeleteConversation()

        assertFalse(result)
    }

    @Test
    fun `shouldRestrictReplyAll returns true when feature flag is enabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns true

        val result = behavior.shouldRestrictReplyAll()

        assertTrue(result)
    }

    @Test
    fun `shouldRestrictReplyAll returns false when feature flag is disabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns false

        val result = behavior.shouldRestrictReplyAll()

        assertFalse(result)
    }

    @Test
    fun `shouldRestrictReplyAll returns false when feature flag throws exception`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } throws RuntimeException("Network error")

        val result = behavior.shouldRestrictReplyAll()

        assertFalse(result)
    }

    @Test
    fun `getShowBackButton returns false for tablet`() {
        val context: Context = mockk(relaxed = true)
        every { context.resources.getBoolean(any()) } returns true // Mock tablet

        val result = behavior.getShowBackButton(context)

        assertFalse(result)
    }
}
