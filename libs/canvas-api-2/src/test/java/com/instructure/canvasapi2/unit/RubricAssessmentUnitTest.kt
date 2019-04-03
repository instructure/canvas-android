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

class RubricAssessmentUnitTest : Assert() {

    @Test
    fun testRubricAssessmentRating() {
        val rubricCriterions: Array<RubricCriterion> = rubricAssessmentJSON.parse()

        Assert.assertNotNull(rubricCriterions)

        for (rubricCriterion in rubricCriterions) {
            Assert.assertNotNull(rubricCriterion)

            Assert.assertTrue(rubricCriterion.points >= 0)
        }
    }

    @Language("JSON")
    private var rubricAssessmentJSON = """
      [
        {
          "387653_8589": {
            "points": 10,
            "comments": "fdsfsd"
          },
          "387653_1612": {
            "points": 12,
            "comments": "test"
          },
          "387653_8896": {
            "points": 10,
            "comments": null
          },
          "387653_5670": {
            "points": 8,
            "comments": null
          }
        }
      ]"""

}
