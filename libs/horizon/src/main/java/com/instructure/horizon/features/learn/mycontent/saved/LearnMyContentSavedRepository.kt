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
package com.instructure.horizon.features.learn.mycontent.saved

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryRecommendation
import javax.inject.Inject

class LearnMyContentSavedRepository @Inject constructor(
    private val getLearningLibraryManager: GetLearningLibraryManager
) {
    suspend fun toggleLearningLibraryItemIsBookmarked(itemId: String): Boolean {
        return getLearningLibraryManager.toggleLearningLibraryItemIsBookmarked(itemId)
    }

    suspend fun getLearningLibraryRecommendedItems(forceNetwork: Boolean): List<LearningLibraryRecommendation> {
        return getLearningLibraryManager.getLearningLibraryRecommendations(forceNetwork)
    }
}