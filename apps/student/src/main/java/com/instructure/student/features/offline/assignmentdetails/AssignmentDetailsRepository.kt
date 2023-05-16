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

package com.instructure.student.features.offline.assignmentdetails

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.pandautils.utils.NetworkStateProvider

class AssignmentDetailsRepository(
    private val networkStateProvider: NetworkStateProvider,
    private val localDataSource: AssignmentDetailsLocalDataSource,
    private val networkDataSource: AssignmentDetailsNetworkDataSource
) {

    fun isOnline() = networkStateProvider.isOnline()

    suspend fun getCourseWithGrade(courseId: Long, forceNetwork: Boolean): Course {
        return if (networkStateProvider.isOnline()) {
            networkDataSource.getCourseWithGrade(courseId, forceNetwork).dataOrThrow
        } else {
            localDataSource.getCourseWithGrade(courseId) ?: throw IllegalStateException("Could not load from DB")
        }
    }

    suspend fun getAssignment(isObserver: Boolean, assignmentId: Long, courseId: Long, forceNetwork: Boolean): Assignment {
        return if (networkStateProvider.isOnline()) {
            if (isObserver) {
                networkDataSource.getAssignmentIncludeObservees(assignmentId, courseId, forceNetwork).dataOrThrow
            } else {
                networkDataSource.getAssignmentWithHistory(assignmentId, courseId, forceNetwork).dataOrThrow
            }
        } else {
            localDataSource.getAssignment(assignmentId) ?: throw IllegalStateException("Could not load from DB")
        }
    }

    suspend fun getQuiz(courseId: Long, quizId: Long, forceNetwork: Boolean): Quiz {
        return if (networkStateProvider.isOnline()) {
            networkDataSource.getQuiz(courseId, quizId, forceNetwork).dataOrThrow
        } else {
            localDataSource.getQuiz(quizId) ?: throw IllegalStateException("Could not load from DB")
        }
    }

    suspend fun getExternalToolLaunchUrl(courseId: Long, externalToolId: Long, assignmentId: Long, forceNetwork: Boolean): LTITool? {
        return if (networkStateProvider.isOnline()) {
            networkDataSource.getExternalToolLaunchUrl(courseId, externalToolId, assignmentId, forceNetwork).dataOrThrow
        } else {
            null
        }
    }

    suspend fun getLtiFromAuthenticationUrl(url: String, forceNetwork: Boolean): LTITool? {
        return if (networkStateProvider.isOnline()) {
            networkDataSource.getLtiFromAuthenticationUrl(url, forceNetwork).dataOrThrow
        } else {
            null
        }
    }
}
