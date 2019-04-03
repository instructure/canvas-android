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

import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.canvasapi2.utils.parse
import org.intellij.lang.annotations.Language
import org.junit.Assert
import org.junit.Test

class MasteryPathUnitTest : Assert() {

    @Test
    fun testMasteryPath() {
        val masteryPath: MasteryPath = masteryPathsJSON.parse()

        Assert.assertNotNull(masteryPath)
        Assert.assertFalse(masteryPath.isLocked)
        Assert.assertNotNull(masteryPath.assignmentSets)

    }

    @Language("JSON")
    private val masteryPathsJSON = """
      {
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
      }"""
}
