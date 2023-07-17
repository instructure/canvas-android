/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.assignments.details

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.repository.Repository
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsDataSource
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsLocalDataSource
import com.instructure.student.features.assignments.details.datasource.AssignmentDetailsNetworkDataSource

class AssignmentDetailsRepository(
    localDataSource: AssignmentDetailsLocalDataSource,
    networkDataSource: AssignmentDetailsNetworkDataSource,
    networkStateProvider: NetworkStateProvider
) : Repository<AssignmentDetailsDataSource>(localDataSource, networkDataSource, networkStateProvider) {

    suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        return dataSource.getCourseWithGrade(courseId, forceNetwork)
    }

    suspend fun getAssignment(isObserver: Boolean, assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        return dataSource.getAssignment(isObserver, assignmentId, courseId, forceNetwork)
    }

    suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): Quiz {
        return dataSource.getQuiz(courseId, quizId, forceNetwork)
    }

    suspend fun getExternalToolLaunchUrl(courseId: Long, externalToolId: Long, assignmentId: Long, forceNetwork: Boolean): LTITool? {
        return dataSource.getExternalToolLaunchUrl(courseId, externalToolId, assignmentId, forceNetwork)
    }

    suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): LTITool? {
        return dataSource.getLtiFromAuthenticationUrl(url, forceNetwork)
    }
}
