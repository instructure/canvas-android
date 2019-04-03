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

import com.instructure.canvasapi2.models.CustomColumn
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test


class CustomGradebookColumnUnitTest : Assert() {

    @Test
    fun testCustomColumnData() {
        val customColumns: Array<CustomColumn> = customColumnData.parse()

        Assert.assertNotNull(customColumns)
        Assert.assertEquals(3, customColumns.size)

        for (customColumn in customColumns) {
            Assert.assertNotNull(customColumn.id)
            Assert.assertNotNull(customColumn.position)
            Assert.assertNotNull(customColumn.teacherNotes)
            Assert.assertNotNull(customColumn.hidden)
        }
    }

    @Language("JSON")
    private val customColumnData = """
      [
        {
          "id": 1234,
          "position": 0,
          "teacher_notes": false,
          "title": "Column1",
          "hidden": false
        },
        {
          "id": 2345,
          "position": 1,
          "teacher_notes": false,
          "title": "Column2",
          "hidden": false
        },
        {
          "id": 3456,
          "position": 2,
          "teacher_notes": true,
          "title": "Column3",
          "hidden": false
        }
      ]"""
}
