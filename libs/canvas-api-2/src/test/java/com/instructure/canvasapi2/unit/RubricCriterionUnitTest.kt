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

import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.utils.parse
import org.junit.Assert
import org.intellij.lang.annotations.Language
import org.junit.Test


class RubricCriterionUnitTest : Assert() {

    @Test
    fun testRubricCriterion() {
        val rubricCriterions: Array<RubricCriterion> = rubricCriterionJSON.parse()

        Assert.assertNotNull(rubricCriterions)

        for (rubricCriterion in rubricCriterions) {
            Assert.assertNotNull(rubricCriterion)
            Assert.assertNotNull(rubricCriterion.description)
            Assert.assertNotNull(rubricCriterion.id)
            Assert.assertTrue(rubricCriterion.points >= 0)
        }
    }

    @Test
    fun testRubricCriterionRating() {
        val rubricCriteria: Array<RubricCriterion> = rubricCriterionJSON.parse()

        for (rubricCriterion in rubricCriteria) {
            for ((id, description) in rubricCriterion.ratings) {
                Assert.assertNotNull(id)
                Assert.assertNotNull(description)
            }
        }
    }

    @Language("JSON")
    private var rubricCriterionJSON = """
      [
        {
          "id": "387653_8589",
          "points": 10,
          "description": "Description of criterion",
          "long_description": "",
          "ratings": [
            {
              "id": "blank",
              "points": 10,
              "description": "Full Marks"
            },
            {
              "id": "blank_2",
              "points": 0,
              "description": "No Marks"
            }
          ]
        },
        {
          "id": "387653_1612",
          "points": 12,
          "description": "Description of criterion",
          "long_description": "",
          "ratings": [
            {
              "id": "387653_8361",
              "points": 12,
              "description": "Full Marks"
            },
            {
              "id": "387653_870",
              "points": 0,
              "description": "No Marks"
            }
          ]
        },
        {
          "id": "387653_8896",
          "points": 10,
          "description": "Description of criterion",
          "long_description": "",
          "ratings": [
            {
              "id": "387653_7003",
              "points": 10,
              "description": "Full Marks"
            },
            {
              "id": "387653_6719",
              "points": 0,
              "description": "No Marks"
            }
          ]
        },
        {
          "id": "387653_5670",
          "points": 8,
          "description": "Description of criterion",
          "long_description": "",
          "ratings": [
            {
              "id": "387653_3621",
              "points": 8,
              "description": "Full Marks"
            },
            {
              "id": "387653_2577",
              "points": 0,
              "description": "No Marks"
            }
          ]
        }
      ]"""

}
