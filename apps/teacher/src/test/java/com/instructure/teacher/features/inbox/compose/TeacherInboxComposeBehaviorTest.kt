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
package com.instructure.teacher.features.inbox.compose

import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherInboxComposeBehaviorTest {

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val behavior = TeacherInboxComposeBehavior(featureFlagProvider)

    @Test
    fun `shouldHideSendIndividual returns true when feature flag is enabled`() = runTest {
        coEvery { featureFlagProvider.checkRestrictStudentAccessFlag() } returns true

        val result = behavior.shouldHideSendIndividual()

        assertTrue(result)
    }

    @Test
    fun `shouldHideSendIndividual returns false when feature flag is disabled`() = runTest {
        coEvery { featureFlagProvider.checkRestrictStudentAccessFlag() } returns false

        val result = behavior.shouldHideSendIndividual()

        assertFalse(result)
    }

    @Test
    fun `shouldHideSendIndividual returns false when feature flag throws exception`() = runTest {
        coEvery { featureFlagProvider.checkRestrictStudentAccessFlag() } throws RuntimeException("Network error")

        val result = behavior.shouldHideSendIndividual()

        assertFalse(result)
    }
}
