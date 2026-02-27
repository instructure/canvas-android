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
package com.instructure.horizon.features.learn.learninglibrary.enroll

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class LearnLearningLibraryEnrollRepository @Inject constructor(
    private val getLearningLibraryManager: GetLearningLibraryManager,
    private val getCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs
) {
    suspend fun loadLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        return getLearningLibraryManager.getLearningLibraryItem(itemId, false)
    }

    suspend fun loadCourseDetails(courseId: Long): CourseWithProgress {
        return getCoursesManager.getCourseWithProgressById(courseId, apiPrefs.user?.id ?: -1L).dataOrThrow
    }

    suspend fun enrollLearningLibraryItem(itemId: String): LearningLibraryCollectionItem {
        return getLearningLibraryManager.enrollLearningLibraryItem(itemId)
    }
}