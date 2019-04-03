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

import com.instructure.canvasapi2.models.ColumnDatum
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class ColumnDataUnitTest : Assert() {

    @Test
    fun testColumnData() {
        val columnDatums: Array<ColumnDatum> = columnDataJSON.parse()

        Assert.assertNotNull(columnDatums)
        Assert.assertEquals(3, columnDatums.size)

        for (columnDatum in columnDatums) {
            Assert.assertNotNull(columnDatum.userId)
            Assert.assertNotNull(columnDatum.content)
        }
    }

    @Language("JSON")
    private val columnDataJSON = """
      [
        {
          "content": "This is the content for a column data",
          "user_id": 123456
        },
        {
          "content": "Should be more like Harry.",
          "user_id": 123456
        },
        {
          "content": "Needs more practice.",
          "user_id": 123456
        }
      ]"""
}
