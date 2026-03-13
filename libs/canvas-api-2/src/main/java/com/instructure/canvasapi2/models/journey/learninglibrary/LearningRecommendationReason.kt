/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvasapi2.models.journey.learninglibrary

import com.instructure.journey.type.LearningRecommendationReason as ApolloLearningRecommendationReason

enum class LearningRecommendationReason {
    PAST_LEARNINGS,
    BOOKMARKED_ITEMS,
    EXISTING_SKILLS,
    POPULARITY;

    fun toApiModel(): ApolloLearningRecommendationReason {
        return when (this) {
            PAST_LEARNINGS -> ApolloLearningRecommendationReason.PAST_LEARNINGS
            BOOKMARKED_ITEMS -> ApolloLearningRecommendationReason.BOOKMARKED_ITEMS
            EXISTING_SKILLS -> ApolloLearningRecommendationReason.EXISTING_SKILLS
            POPULARITY -> ApolloLearningRecommendationReason.POPULARITY
        }
    }
}

fun ApolloLearningRecommendationReason.toModel(): LearningRecommendationReason? {
    return when (this) {
        ApolloLearningRecommendationReason.PAST_LEARNINGS -> LearningRecommendationReason.PAST_LEARNINGS
        ApolloLearningRecommendationReason.BOOKMARKED_ITEMS -> LearningRecommendationReason.BOOKMARKED_ITEMS
        ApolloLearningRecommendationReason.EXISTING_SKILLS -> LearningRecommendationReason.EXISTING_SKILLS
        ApolloLearningRecommendationReason.POPULARITY -> LearningRecommendationReason.POPULARITY
        else -> null
    }
}