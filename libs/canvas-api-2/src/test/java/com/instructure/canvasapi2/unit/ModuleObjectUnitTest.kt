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

import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class ModuleObjectUnitTest : Assert() {

    @Test
    fun testModuleObject() {
        val moduleObjects: Array<ModuleObject> = moduleObjectJSON.parse()

        for (i in moduleObjects.indices) {
            Assert.assertTrue(moduleObjects[i].id > 0)
            Assert.assertNotNull(moduleObjects[i].name)

            // Only the module object with index of 1 has an unlock date
            if (i == 1) {
                Assert.assertNotNull(moduleObjects[i].unlockAt)
            }

            Assert.assertNotNull(moduleObjects[i].state)

            // Objects 1 - 3 have prerequisite ids
            if (i > 0) {
                for (j in 0 until (moduleObjects[i].prerequisiteIds?.size ?: 0)) {
                    Assert.assertTrue(moduleObjects[i].prerequisiteIds!![j] > 0)
                }
            }
        }
    }

    @Language("JSON")
    private var moduleObjectJSON = """
      [
        {
          "id": 1059720,
          "name": "Beginners",
          "position": 1,
          "unlock_at": null,
          "require_sequential_progress": false,
          "prerequisite_module_ids": [],
          "state": "started",
          "completed_at": null,
          "items_count": 4,
          "items_url": "https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059720/items"
        },
        {
          "id": 1059721,
          "name": "Advanced",
          "position": 2,
          "unlock_at": "2013-07-31T06:00:00Z",
          "require_sequential_progress": false,
          "prerequisite_module_ids": [
            1059720
          ],
          "state": "locked",
          "completed_at": null,
          "items_count": 1,
          "items_url": "https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059721/items"
        },
        {
          "id": 1059722,
          "name": "User Interface",
          "position": 3,
          "unlock_at": null,
          "require_sequential_progress": false,
          "prerequisite_module_ids": [
            1059721
          ],
          "state": "locked",
          "completed_at": null,
          "items_count": 0,
          "items_url": "https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059722/items"
        },
        {
          "id": 1059723,
          "name": "Jelly Bean",
          "position": 4,
          "unlock_at": null,
          "require_sequential_progress": false,
          "prerequisite_module_ids": [
            1059722
          ],
          "state": "locked",
          "completed_at": null,
          "items_count": 0,
          "items_url": "https://mobiledev.instructure.com/api/v1/courses/833052/modules/1059723/items"
        }
      ]"""
}
