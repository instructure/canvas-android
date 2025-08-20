/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.inbox.compose

import com.instructure.pandautils.utils.FeatureFlagProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentInboxComposeBehaviorTest {

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val behavior = StudentInboxComposeBehavior(featureFlagProvider)

    @Test
    fun `shouldRestrictStudentAccess returns true when feature flag is enabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns true

        val result = behavior.shouldRestrictStudentAccess()

        assertTrue(result)
    }

    @Test
    fun `shouldRestrictStudentAccess returns false when feature flag is disabled`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns false

        val result = behavior.shouldRestrictStudentAccess()

        assertFalse(result)
    }

    @Test
    fun `shouldRestrictStudentAccess returns false when feature flag throws exception`() = runTest {
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } throws RuntimeException("Network error")

        val result = behavior.shouldRestrictStudentAccess()

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
    fun `shouldHideSendIndividual returns false regardless of feature flag`() = runTest {
        // Test when feature flag is enabled
        coEvery { featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access") } returns true

        val result = behavior.shouldHideSendIndividual()

        assertFalse(result)
    }
}
