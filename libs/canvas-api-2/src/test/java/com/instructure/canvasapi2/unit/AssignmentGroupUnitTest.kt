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

import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class AssignmentGroupUnitTest : Assert() {

    @Test
    fun testAssignmentGroup() {
        val assignmentGroup: Array<AssignmentGroup> = assignmentGroupJSON.parse()

        Assert.assertNotNull(assignmentGroup)

        Assert.assertEquals(2, assignmentGroup.size)

        Assert.assertNotNull(assignmentGroup[0].name)
        Assert.assertNotNull(assignmentGroup[1].name)

        Assert.assertTrue(assignmentGroup[0].id > 0)
        Assert.assertTrue(assignmentGroup[1].id > 0)
    }

    @Language("JSON")
    private val assignmentGroupJSON = """
      [
        {
          "group_weight": 0,
          "id": 534101,
          "name": "Extra Credit",
          "position": 1,
          "rules": {}
        },
        {
          "group_weight": 0,
          "id": 534100,
          "name": "Assignments",
          "position": 2,
          "rules": {}
        }
      ]"""
}
