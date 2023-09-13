/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.edit

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.hasActiveEnrollment
import com.instructure.canvasapi2.utils.isNotDeleted
import com.instructure.canvasapi2.utils.isPublished
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRepository
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.dashboard.edit.datasource.StudentEditDashboardDataSource
import com.instructure.student.features.dashboard.edit.datasource.StudentEditDashboardLocalDataSource
import com.instructure.student.features.dashboard.edit.datasource.StudentEditDashboardNetworkDataSource

class StudentEditDashboardRepository(
    localDataSource: StudentEditDashboardLocalDataSource,
    networkDataSource: StudentEditDashboardNetworkDataSource,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider
) : Repository<StudentEditDashboardDataSource>(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider), EditDashboardRepository {

    override suspend fun getCurses(): List<List<Course>> {
        return dataSource().getCurses()
    }

    override suspend fun getGroups(): List<Group> = dataSource().getGroups()

    override fun isOpenable(course: Course) = course.isNotDeleted() && course.isPublished()

    override fun isFavoriteable(course: Course) = course.isValidTerm() && course.isNotDeleted() && course.isPublished() && course.hasActiveEnrollment()
}
