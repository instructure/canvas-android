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

import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test

class ModuleItemUnitTest : Assert() {

    @Test
    fun testModuleItem() {
        val moduleItems: Array<ModuleItem> = moduleItemJSON.parse()

        for (moduleItem in moduleItems) {
            Assert.assertTrue(moduleItem.id > 0)
            Assert.assertNotNull(moduleItem.type)
            Assert.assertNotNull(moduleItem.title)
            Assert.assertNotNull(moduleItem.htmlUrl)
            Assert.assertNotNull(moduleItem.url)
            Assert.assertNotNull(moduleItem.completionRequirement?.type)
        }
    }

    @Test
    fun testModuleItemMasteryPath() {
        val moduleItems: Array<ModuleItem> = moduleItemWithMasteryPath.parse()

        for (moduleItem in moduleItems) {
            Assert.assertTrue(moduleItem.id > 0)
            Assert.assertNotNull(moduleItem.type)
            Assert.assertNotNull(moduleItem.title)
            Assert.assertNotNull(moduleItem.htmlUrl)
            Assert.assertNotNull(moduleItem.url)

            if (moduleItem.id == 1L) {
                // First module item has a mastery paths
                Assert.assertNotNull(moduleItem.masteryPaths)
            } else {
                Assert.assertNull(moduleItem.masteryPaths)
            }
        }
    }

    @Language("JSON")
    private var moduleItemJSON = """
      [
        {
          "id": 9012239,
          "indent": 0,
          "position": 1,
          "title": "Android 101",
          "type": "Assignment",
          "module_id": 1059720,
          "html_url": "https://mobiledev.instructure.com/courses/833052/modules/items/9012239",
          "content_id": 2241839,
          "url": "https://mobiledev.instructure.com/api/v1/courses/833052/assignments/2241839",
          "completion_requirement": {
            "type": "must_submit",
            "Completed": true
          }
        },
        {
          "id": 9012244,
          "indent": 0,
          "position": 2,
          "title": "Favorite App Video",
          "type": "Assignment",
          "module_id": 1059720,
          "html_url": "https://mobiledev.instructure.com/courses/833052/modules/items/9012244",
          "content_id": 2241864,
          "url": "https://mobiledev.instructure.com/api/v1/courses/833052/assignments/2241864",
          "completion_requirement": {
            "type": "min_score",
            "min_score": "5",
            "Completed": true
          }
        },
        {
          "id": 9012248,
          "indent": 0,
          "position": 3,
          "title": "Android vs. iOS",
          "type": "Discussion",
          "module_id": 1059720,
          "html_url": "https://mobiledev.instructure.com/courses/833052/modules/items/9012248",
          "content_id": 1369942,
          "url": "https://mobiledev.instructure.com/api/v1/courses/833052/discussion_topics/1369942",
          "completion_requirement": {
            "type": "must_contribute",
            "Completed": false
          }
        },
        {
          "id": 9012251,
          "indent": 0,
          "position": 4,
          "title": "Easy Quiz",
          "type": "Quiz",
          "module_id": 1059720,
          "html_url": "https://mobiledev.instructure.com/courses/833052/modules/items/9012251",
          "content_id": 757314,
          "url": "https://mobiledev.instructure.com/api/v1/courses/833052/quizzes/757314",
          "completion_requirement": {
            "type": "must_submit",
            "Completed": true
          }
        }
      ]"""

    @Language("JSON")
    private var moduleItemWithMasteryPath = """
      [
        {
          "id": 1,
          "title": "Assignment 1~1",
          "position": 1,
          "indent": 0,
          "type": "Assignment",
          "module_id": 1,
          "html_url": "http://canvas.docker/courses/1/modules/items/1",
          "content_id": 1,
          "url": "http://canvas.docker/api/v1/courses/1/assignments/1",
          "mastery_paths": {
            "locked": false,
            "assignment_sets": [
              {
                "id": 2,
                "scoring_range_id": 2,
                "created_at": "2016-08-03T19:04:44.860Z",
                "updated_at": "2016-08-03T19:04:44.860Z",
                "position": 1,
                "assignments": [
                  {
                    "id": 2,
                    "assignment_id": "5",
                    "created_at": "2016-08-03T19:04:44.865Z",
                    "updated_at": "2016-08-03T19:04:44.865Z",
                    "override_id": 8,
                    "assignment_set_id": 2,
                    "position": 1,
                    "model": {
                      "id": 5,
                      "title": "Quiz 1~1",
                      "description": "",
                      "due_at": null,
                      "unlock_at": null,
                      "lock_at": null,
                      "points_possible": 0,
                      "min_score": null,
                      "max_score": null,
                      "grading_type": "points",
                      "submission_types": [
                        "online_quiz"
                      ],
                      "workflow_state": "published",
                      "context_id": 1,
                      "context_type": "Course",
                      "updated_at": "2016-08-11T17:32:34Z",
                      "context_code": "course_1"
                    }
                  }
                ]
              }
            ]
          }
        },
        {
          "id": 5,
          "title": "Quiz 1~1",
          "position": 3,
          "indent": 0,
          "type": "Quiz",
          "module_id": 1,
          "html_url": "http://canvas.docker/courses/1/modules/items/5",
          "content_id": 1,
          "url": "http://canvas.docker/api/v1/courses/1/quizzes/1",
          "mastery_paths": null
        },
        {
          "id": 7,
          "title": "Discussion 1~1",
          "position": 5,
          "indent": 0,
          "type": "Discussion",
          "module_id": 1,
          "html_url": "http://canvas.docker/courses/1/modules/items/7",
          "content_id": 1,
          "url": "http://canvas.docker/api/v1/courses/1/discussion_topics/1",
          "mastery_paths": null
        },
        {
          "id": 8,
          "title": "Assignment 1~2",
          "position": 6,
          "indent": 0,
          "type": "Assignment",
          "module_id": 1,
          "html_url": "http://canvas.docker/courses/1/modules/items/8",
          "content_id": 2,
          "url": "http://canvas.docker/api/v1/courses/1/assignments/2",
          "mastery_paths": null
        }
      ]"""
}
