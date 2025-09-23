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

import com.apollographql.apollo.api.Optional
import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.HorizonGetProgramCourseByIdQuery
import com.instructure.canvasapi2.QLClientConfig
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import java.util.Date

class HorizonGetCoursesManager {

    suspend fun getCoursesWithProgress(userId: Long, forceNetwork: Boolean): DataResult<List<CourseWithProgress>> {
        return try {
            val query = GetCoursesQuery(userId.toString())
            val result = QLClientConfig.enqueueQuery(query, forceNetwork).dataAssertNoErrors

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

    suspend fun getEnrollments(userId: Long, forceNetwork: Boolean): DataResult<List<GetCoursesQuery.Enrollment>> {
        return try {
            val query = GetCoursesQuery(userId.toString())
            val result = QLClientConfig.enqueueQuery(query, forceNetwork).dataAssertNoErrors

            return DataResult.Success(result.legacyNode?.onUser?.enrollments.orEmpty())
        } catch (e: Exception) {
            DataResult.Fail(Failure.Exception(e))
        }
    }

    suspend fun getDashboardContent(userId: Long, forceNetwork: Boolean): DataResult<DashboardContent> {
        return try {
            val query = GetCoursesQuery(userId.toString())
            val result = QLClientConfig.enqueueQuery(query, forceNetwork).dataAssertNoErrors

            val coursesList = result.legacyNode?.onUser?.enrollments
                ?.filter { it.state == EnrollmentWorkflowState.active }
                ?.mapNotNull { mapDashboardCourse(it.course) } ?: emptyList()
            val invites = result.legacyNode?.onUser?.enrollments
                ?.filter { it.state == EnrollmentWorkflowState.invited }
                ?.mapNotNull { mapInvites(it.course, it.id) } ?: emptyList()
            return DataResult.Success(DashboardContent(coursesList, invites))
        } catch (e: Exception) {
            DataResult.Fail(Failure.Exception(e))
        }
    }

    private fun mapDashboardCourse(course: GetCoursesQuery.Course?): DashboardCourse? {
        val courseWithProgress = mapCourse(course)
        val institutionName = course?.account?.name
        val incompleteModulesConnection =
            course?.usersConnection?.nodes?.firstOrNull()?.courseProgression?.incompleteModulesConnection?.nodes?.firstOrNull()
        val nextModuleId = incompleteModulesConnection?.module?.id?.toLong()
        val nextModuleItemId = incompleteModulesConnection?.incompleteItemsConnection?.nodes?.firstOrNull()?.id?.toLong()

        val nextModuleTitle = incompleteModulesConnection?.module?.name

        val nextModuleItemEstimatedDuration =
            incompleteModulesConnection?.incompleteItemsConnection?.nodes?.firstOrNull()?.estimatedDuration
        val nextModuleItemDueDate =
            incompleteModulesConnection?.incompleteItemsConnection?.nodes?.firstOrNull()?.content?.onAssignment?.dueAt
        val nextModuleItemType = incompleteModulesConnection?.incompleteItemsConnection?.nodes?.firstOrNull()?.content?.__typename
        val nextModuleItemTitle = incompleteModulesConnection?.incompleteItemsConnection?.nodes?.firstOrNull()?.content?.title
        val isNewQuiz =
            incompleteModulesConnection?.incompleteItemsConnection?.nodes?.firstOrNull()?.content?.onAssignment?.isNewQuiz ?: false

        return if (courseWithProgress != null) {
            DashboardCourse(
                courseWithProgress,
                institutionName,
                nextModuleItemId,
                nextModuleId,
                nextModuleTitle,
                nextModuleItemTitle,
                nextModuleItemType,
                nextModuleItemDueDate,
                nextModuleItemEstimatedDuration,
                isNewQuiz
            )
        } else {
            null
        }
    }

    private fun mapInvites(course: GetCoursesQuery.Course?, enrollmentId: String?): CourseInvite? {
        val courseId = course?.id?.toLong()
        val courseName = course?.name

        return if (courseId != null && courseName != null) {
            CourseInvite(courseId, courseName, enrollmentId?.toLong() ?: -1L)
        } else {
            null
        }
    }

    suspend fun getProgramCourses(courseId: Long, forceNetwork: Boolean = false): DataResult<CourseWithModuleItemDurations> {
        var hasNextPage = true
        var nextCursor: String? = null
        val moduleItemDurations = mutableListOf<String>()
        var courseName: String? = null

        try {
            while (hasNextPage) {
                val nextCursorParam = if (nextCursor != null) Optional.present(nextCursor) else Optional.absent()
                val query = HorizonGetProgramCourseByIdQuery(courseId.toString(), QLClientConfig.GRAPHQL_PAGE_SIZE, nextCursorParam)
                val result = QLClientConfig.enqueueQuery(query, forceNetwork = forceNetwork).dataAssertNoErrors
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