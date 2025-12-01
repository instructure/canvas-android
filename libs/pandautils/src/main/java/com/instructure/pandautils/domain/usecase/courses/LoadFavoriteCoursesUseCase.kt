/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.domain.models.courses.CourseCardItem
import com.instructure.pandautils.domain.models.courses.GradeDisplay
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

data class LoadFavoriteCoursesParams(
    val forceRefresh: Boolean = false
)

class LoadFavoriteCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val networkStateProvider: NetworkStateProvider,
    private val featureFlagProvider: FeatureFlagProvider
) : BaseUseCase<LoadFavoriteCoursesParams, List<CourseCardItem>>() {

    override suspend fun execute(params: LoadFavoriteCoursesParams): List<CourseCardItem> {
        val courses = courseRepository.getFavoriteCourses(params.forceRefresh).dataOrThrow
        val dashboardCards = courseRepository.getDashboardCards(params.forceRefresh).dataOrThrow
        val syncedIds = getSyncedCourseIds()
        val isOnline = networkStateProvider.isOnline()

        return courses
            .filter { it.isFavorite }
            .sortedBy { course -> dashboardCards.find { it.id == course.id }?.position ?: Int.MAX_VALUE }
            .map { course ->
                val isSynced = syncedIds.contains(course.id)
                val themedColor = ColorKeeper.getOrGenerateColor(course)
                CourseCardItem(
                    id = course.id,
                    name = course.name,
                    courseCode = course.courseCode,
                    color = themedColor.light,
                    imageUrl = course.imageUrl,
                    grade = mapGrade(course),
                    announcementCount = 0,
                    isSynced = isSynced,
                    isClickable = isOnline || isSynced
                )
            }
    }

    private suspend fun getSyncedCourseIds(): Set<Long> {
        if (!featureFlagProvider.offlineEnabled()) return emptySet()

        val courseSyncSettings = courseSyncSettingsDao.findAll()
        return courseSyncSettings
            .filter { it.anySyncEnabled }
            .map { it.courseId }
            .toSet()
    }

    private fun mapGrade(course: com.instructure.canvasapi2.models.Course): GradeDisplay {
        val enrollment = course.enrollments?.firstOrNull()

        return when {
            enrollment == null -> GradeDisplay.NotAvailable
            enrollment.computedCurrentGrade == null && enrollment.computedCurrentScore == null -> {
                if (course.hideFinalGrades == true) {
                    GradeDisplay.Locked
                } else {
                    GradeDisplay.NotAvailable
                }
            }
            enrollment.computedCurrentGrade != null -> GradeDisplay.Letter(enrollment.computedCurrentGrade!!)
            enrollment.computedCurrentScore != null -> {
                val score = enrollment.computedCurrentScore!!
                GradeDisplay.Percentage("${score.toInt()}%")
            }
            else -> GradeDisplay.NotAvailable
        }
    }
}