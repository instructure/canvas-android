/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.student.features.calendar

import com.instructure.pandautils.features.calendar.CalendarBehavior
import com.instructure.pandautils.utils.FeatureFlagProvider
import javax.inject.Inject

class StudentCalendarBehavior @Inject constructor(
    private val featureFlagProvider: FeatureFlagProvider
) : CalendarBehavior {
    
    override suspend fun shouldShowAddEventButton(): Boolean {
        return try {
            !featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access")
        } catch (e: Exception) {
            true
        }
    }
}
