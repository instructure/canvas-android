/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.canvasapi2.managers

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.HorizonGetProgramCourseByIdQuery
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.enqueueQuery
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import java.util.Date

interface HorizonGetCoursesManager {
    suspend fun getCoursesWithProgress(userId: Long, forceNetwork: Boolean = false): DataResult<List<CourseWithProgress>>

    suspend fun getEnrollments(userId: Long, forceNetwork: Boolean = false): DataResult<List<GetCoursesQuery.Enrollment>>

    suspend fun getProgramCourses(courseId: Long, forceNetwork: Boolean = false): DataResult<CourseWithModuleItemDurations>
}

class HorizonGetCoursesManagerImpl(private val apolloClient: ApolloClient): HorizonGetCoursesManager {

    override suspend fun getCoursesWithProgress(userId: Long, forceNetwork: Boolean): DataResult<List<CourseWithProgress>> {
        return try {
            val query = GetCoursesQuery(userId.toString())
            val result = apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors

            val coursesList = result.legacyNode?.onUser?.enrollments
                ?.filter { it.state == EnrollmentWorkflowState.active }
                ?.mapNotNull { mapCourse(it.course) } ?: emptyList()
            return DataResult.Success(coursesList)
        } catch (e: Exception) {
            DataResult.Fail(Failure.Exception(e))
        }
    }

    private fun mapCourse(course: GetCoursesQuery.Course?): CourseWithProgress? {
        val progress = course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.requirements?.completionPercentage ?: 0.0
        val courseId = course?.id?.toLong()
        val courseName = course?.name
        val courseSyllabus = course?.syllabus_body

        return if (courseId != null && courseName != null) {
            CourseWithProgress(courseId, courseName, courseSyllabus, progress)
        } else {
            null
        }
    }

    override suspend fun getEnrollments(userId: Long, forceNetwork: Boolean): DataResult<List<GetCoursesQuery.Enrollment>> {
        return try {
            val query = GetCoursesQuery(userId.toString())
            val result = apolloClient.enqueueQuery(query, forceNetwork).dataAssertNoErrors

            return DataResult.Success(result.legacyNode?.onUser?.enrollments.orEmpty())
        } catch (e: Exception) {
            DataResult.Fail(Failure.Exception(e))
        }
    }

    override suspend fun getProgramCourses(courseId: Long, forceNetwork: Boolean): DataResult<CourseWithModuleItemDurations> {
        var hasNextPage = true
        var nextCursor: String? = null
        val moduleItemDurations = mutableListOf<String>()
        var courseName: String? = null

        try {
            while (hasNextPage) {
                val nextCursorParam = if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
                val query = HorizonGetProgramCourseByIdQuery(courseId.toString(), QLClientConfig.GRAPHQL_PAGE_SIZE, nextCursorParam)
                val result = apolloClient.enqueueQuery(query, forceNetwork = forceNetwork).dataAssertNoErrors
                val course = result.legacyNode?.onCourse
                courseName = course?.name
                val newItems = course?.modulesConnection?.edges
                    ?.flatMap { it?.node?.moduleItems.orEmpty() }
                    ?.mapNotNull {
                        it.estimatedDuration
                    }.orEmpty()
                moduleItemDurations.addAll(newItems)
                hasNextPage = course?.modulesConnection?.pageInfo?.hasNextPage ?: false
                nextCursor = course?.modulesConnection?.pageInfo?.endCursor
            }

            return DataResult.Success(
                CourseWithModuleItemDurations(
                    courseId = courseId,
                    courseName = courseName.orEmpty(),
                    moduleItemsDuration = moduleItemDurations,
                )
            )
        } catch (e: Exception) {
            return DataResult.Fail(Failure.Exception(e))
        }
    }
}

data class CourseWithProgress(
    val courseId: Long,
    val courseName: String,
    val courseSyllabus: String? = null,
    val progress: Double,
)

data class DashboardContent(
    val courses: List<DashboardCourse>,
    val courseInvites: List<CourseInvite>
)

data class DashboardCourse(
    val course: CourseWithProgress,
    val institutionName: String?,
    val nextUpModuleItemId: Long?,
    val nextUpModuleId: Long?,
    val nextUpModuleTitle: String?,
    val nextUpModuleItemTitle: String?,
    val nextModuleItemType: String?,
    val nextModuleItemDueDate: Date?,
    val nextModuleItemEstimatedDuration: String?,
    val isNewQuiz: Boolean = false
)

data class CourseInvite(
    val courseId: Long,
    val courseName: String,
    val enrollmentId: Long,
    val acceptLoading: Boolean = false,
)

data class CourseWithModuleItemDurations(
    val courseId: Long = -1,
    val courseName: String = "",
    val moduleItemsDuration: List<String> = emptyList(),
    val startDate: Date? = null,
    val endDate: Date? = null
)