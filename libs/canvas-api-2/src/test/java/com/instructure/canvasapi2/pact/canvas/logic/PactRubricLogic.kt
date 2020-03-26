/*
 * Copyright (C) 2020 - present Instructure, Inc.
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

package com.instructure.canvasapi2.pact.canvas.logic

import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.models.RubricSettings
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

fun LambdaDslObject.populateRubricSettingsFields(): LambdaDslObject {
    this
            .id("id")
            .numberType("points_possible")
            .stringType("title")
            .booleanType("free_form_criterion_comments")
            .booleanType("hide_score_total")
            .booleanType("hide_points")

    // These were difficult to produce in a provider state, and they are not used in our code.
    //.id("context_id")
    //.stringType("context_type")
    //.booleanType("reusable")
    //.booleanType("public")
    //.booleanType("read_only")

    return this
}

fun assertRubricSettingsPopulated(description: String, settings: RubricSettings) {
    assertNotNull("$description + id", settings.id);
    assertNotNull("$description + pointsPossible", settings.pointsPossible);
    assertNotNull("$description + title", settings.title);
    assertNotNull("$description + freeFormCriterionComments", settings.freeFormCriterionComments);
    assertNotNull("$description + hideScoreTotal", settings.hideScoreTotal);
    assertNotNull("$description + hidePoints", settings.hidePoints);
}

fun LambdaDslObject.populateRubricCriterionRatingFields(): LambdaDslObject {

    this
            .stringType("id")
            .stringType("description")
            .stringType("long_description")
            .numberType("points")
    return this
}

fun assertRubricCriterionRatingPopulated(description: String, rating: RubricCriterionRating) {
    assertNotNull("$description + id", rating.id);
    assertNotNull("$description + description", rating.description);
    assertNotNull("$description + longDescription", rating.longDescription);
    assertNotNull("$description + points", rating.points);
}

fun LambdaDslObject.populateRubricCriterionFields(): LambdaDslObject {
    this
            .stringType("id")
            .stringType("description")
            .stringType("long_description")
            .numberType("points")
            .minArrayLike("ratings", 1) { obj ->
                obj.populateRubricCriterionRatingFields()
            }
            .booleanType("criterion_use_range")
    //.booleanType("ignore_for_scoring") // Not supported by API?

    return this
}

fun assertRubricCriterionPopulated(description: String, criterion: RubricCriterion) {
    assertNotNull("$description + id", criterion.id);
    assertNotNull("$description + description", criterion.description);
    assertNotNull("$description + longDescription", criterion.longDescription);
    assertNotNull("$description + points", criterion.points);
    assertNotNull("$description + criterionUseRange", criterion.criterionUseRange);
    assertNotNull("$description + ratings", criterion.ratings);
    //assertNotNull("$description + ignoreForScoring", criterion.ignoreForScoring); // Not supported by API?

    assertTrue("$description + ratings should have at least one element", criterion.ratings.count() >= 1);
    criterion.ratings.forEach() { rating ->
        assertRubricCriterionRatingPopulated("$description + rating", rating)
    }
}