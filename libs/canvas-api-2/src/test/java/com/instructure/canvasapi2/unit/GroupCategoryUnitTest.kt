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

import com.instructure.canvasapi2.models.GroupCategory
import com.instructure.canvasapi2.utils.parse
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Test

class GroupCategoryUnitTest : Assert() {
    @Test
    fun testGroupCategories() {
        val groupCategories: Array<GroupCategory> = groupCategoriesJSON.parse()

        Assert.assertNotNull(groupCategories)
        Assert.assertEquals(3, groupCategories.size)

        for (groupCategory in groupCategories) {
            Assert.assertNotNull(groupCategory.id)
            Assert.assertNotNull(groupCategory.name)
            Assert.assertNotNull(groupCategory.selfSignup)
            Assert.assertNotNull(groupCategory.contextType)
            Assert.assertNotNull(groupCategory.courseId)
        }
    }

    @Language("JSON")
    private val groupCategoriesJSON = """
      [
        {
          "auto_leader": "random",
          "group_limit": 4,
          "id": 55525,
          "name": "Group Set 1",
          "role": null,
          "self_signup": "restricted",
          "context_type": "Course",
          "course_id": 833052,
          "allows_multiple_memberships": false,
          "is_member": false
        },
        {
          "auto_leader": "random",
          "group_limit": 4,
          "id": 55524,
          "name": "Group Set 2",
          "role": null,
          "self_signup": "restricted",
          "context_type": "Course",
          "course_id": 833052,
          "allows_multiple_memberships": false,
          "is_member": false
        },
        {
          "auto_leader": "random",
          "group_limit": 4,
          "id": 54322,
          "name": "Group Set 3",
          "role": null,
          "self_signup": "restricted",
          "context_type": "Course",
          "course_id": 833052,
          "allows_multiple_memberships": false,
          "is_member": false
        }
      ]"""
}
