/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.ngc.features.rubric

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.models.RubricSettings
import com.instructure.canvasapi2.models.Submission
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NgcRubricMapperTest {

    private fun criterion() = RubricCriterion(
        id = "c1",
        description = "Criteria 1",
        longDescription = "Use of space",
        points = 6.0,
        ratings = mutableListOf(
            RubricCriterionRating(id = "r1", description = "Excellent", longDescription = "Great", points = 6.0),
            RubricCriterionRating(id = "r2", description = "Very Good", longDescription = "Good", points = 5.0),
        ),
    )

    private fun assignment(
        criteria: List<RubricCriterion> = listOf(criterion()),
        assessment: RubricCriterionAssessment? = null,
        rubricSettings: RubricSettings? = null,
        freeForm: Boolean = false,
    ) = Assignment(
        rubric = criteria,
        rubricSettings = rubricSettings,
        freeFormCriterionComments = freeForm,
        submission = assessment?.let { Submission(rubricAssessment = hashMapOf("c1" to it)) },
    )

    @Test
    fun `returns null when the assignment has no rubric`() {
        assertNull(buildNgcRubric(Assignment(rubric = emptyList()), restrictQuantitativeData = false))
        assertNull(buildNgcRubric(null, restrictQuantitativeData = false))
    }

    @Test
    fun `resolves the assessed rating by rating id and exposes the score`() {
        val rubric = buildNgcRubric(
            assignment(assessment = RubricCriterionAssessment(ratingId = "r1", points = 6.0)),
            restrictQuantitativeData = false,
        )!!

        val criterion = rubric.criteria.single()
        assertEquals("Criteria 1", criterion.title)
        // Ratings are sorted by points ascending.
        assertEquals(listOf("Very Good", "Excellent"), criterion.ratings.map { it.title })
        val assessed = criterion.ratings.single { it.isAssessed }
        assertEquals("Excellent", assessed.title)
        assertEquals("6", assessed.pointsLabel)
        assertEquals("6", criterion.assessedScore)
        assertEquals("6", criterion.maxScore)
    }

    @Test
    fun `ungraded criterion has no assessed rating and no score`() {
        val rubric = buildNgcRubric(assignment(assessment = null), restrictQuantitativeData = false)!!

        val criterion = rubric.criteria.single()
        assertNull(criterion.assessedScore)
        assertTrue(criterion.ratings.none { it.isAssessed })
    }

    @Test
    fun `hides points and score when quantitative data is restricted`() {
        val rubric = buildNgcRubric(
            assignment(assessment = RubricCriterionAssessment(ratingId = "r1", points = 6.0)),
            restrictQuantitativeData = true,
        )!!

        val criterion = rubric.criteria.single()
        assertNull(criterion.assessedScore)
        assertTrue(criterion.ratings.all { it.pointsLabel == null })
        // The assessed rating is still flagged so it can be highlighted.
        assertTrue(criterion.ratings.any { it.isAssessed })
    }

    @Test
    fun `criterion exposes the teacher feedback comment when present`() {
        val withComment = buildNgcRubric(
            assignment(assessment = RubricCriterionAssessment(ratingId = "r1", points = 6.0, comments = "Nice work")),
            restrictQuantitativeData = false,
        )!!
        assertEquals("Nice work", withComment.criteria.single().comment)

        val blankComment = buildNgcRubric(
            assignment(assessment = RubricCriterionAssessment(ratingId = "r1", points = 6.0, comments = "")),
            restrictQuantitativeData = false,
        )!!
        assertNull(blankComment.criteria.single().comment)
    }

    @Test
    fun `custom score with no matching rating id is surfaced as the assessed rating`() {
        val rubric = buildNgcRubric(
            assignment(assessment = RubricCriterionAssessment(ratingId = null, points = 4.5)),
            restrictQuantitativeData = false,
        )!!

        val criterion = rubric.criteria.single()
        val assessed = criterion.ratings.single { it.isAssessed }
        assertEquals("4.5", assessed.pointsLabel)
        assertEquals("4.5", criterion.assessedScore)
    }
}
