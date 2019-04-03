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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.NeedsGradingCount
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test


class NeedsGradingCountUnitTest : Assert() {

    @Test
    fun testNeedsGradingCount() {
        val needsGradingCount: NeedsGradingCount = needsGradingCountJSON.parse()
        Assert.assertNotNull(needsGradingCount)
        Assert.assertTrue(needsGradingCount.sectionId > 0)
        Assert.assertTrue(needsGradingCount.needsGradingCount > 0)
    }

    @Language("JSON")
    private var needsGradingCountJSON = """
      {
        "section_id": 889720,
        "needs_grading_count": 1
      }"""
}
