/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.domain.usecase.courses

import com.instructure.canvasapi2.CourseAnnouncementsQuery
import com.instructure.canvasapi2.DashboardCoursesQuery
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Enrollment.EnrollmentType as KotlinEnrollmentType
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoadVisibleCoursesUseCase @Inject constructor(
    private val dashboardCoursesManager: DashboardCoursesManager
) : BaseUseCase<LoadVisibleCoursesUseCase.Params, LoadVisibleCoursesUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        val data = dashboardCoursesManager.getDashboardCourses(
            forceNetwork = params.forceRefresh
        )

        val allCourses = data.allCourses?.mapNotNull { mapGraphQlCourse(it) } ?: emptyList()

        val visibleCourses = allCourses
            .filter { course ->
                data.allCourses?.find { it._id == course.id.toString() }?.dashboardCard != null
            }
            .sortedBy { course ->
                data.allCourses?.find { it._id == course.id.toString() }
                    ?.dashboardCard?.position ?: Int.MAX_VALUE
            }

        val announcementsMap = buildAnnouncementsMap(data, params.forceRefresh)

        return Result(
            visibleCourses = visibleCourses,
            allCourses = allCourses,
            announcementsMap = announcementsMap
        )
    }

    private suspend fun buildAnnouncementsMap(
        data: DashboardCoursesQuery.Data,
        forceRefresh: Boolean
    ): Map<Long, List<DiscussionTopicHeader>> {
        val announcementsMap = mutableMapOf<Long, List<DiscussionTopicHeader>>()

        data.allCourses?.forEach { graphQlCourse ->
            val courseId = graphQlCourse._id.toLongOrNull() ?: return@forEach
            val firstPage = mapGraphQlAnnouncements(graphQlCourse.announcements?.nodes)
            val hasNextPage = graphQlCourse.announcements?.pageInfo?.hasNextPage == true

            if (hasNextPage) {
                val allNodes = dashboardCoursesManager.getCourseAnnouncements(courseId, forceRefresh)
                announcementsMap[courseId] = mapGraphQlAnnouncements(allNodes)
            } else {
                announcementsMap[courseId] = firstPage
            }
        }

        return announcementsMap
    }

    private fun mapGraphQlCourse(graphQlCourse: DashboardCoursesQuery.AllCourse): Course? {
        val courseId = graphQlCourse._id.toLongOrNull() ?: return null

        val enrollmentNode = graphQlCourse.enrollmentsConnection?.nodes?.firstOrNull()
        val enrollment = enrollmentNode?.let { node ->
            Enrollment(
                id = node._id?.toLongOrNull() ?: 0L,
                type = mapEnrollmentType(node.type),
                role = mapEnrollmentType(node.type),
                grades = node.grades?.let { grades ->
                    Grades(
                        currentGrade = grades.currentGrade,
                        currentScore = grades.currentScore
                    )
                }
            )
        }

        return Course(
            id = courseId,
            name = graphQlCourse.name,
            courseCode = graphQlCourse.courseCode,
            imageUrl = graphQlCourse.imageUrl,
            courseColor = graphQlCourse.dashboardCard?.color,
            isFavorite = graphQlCourse.dashboardCard?.isFavorited == true,
            enrollments = enrollment?.let { mutableListOf(it) } ?: mutableListOf()
        )
    }

    private fun mapEnrollmentType(type: EnrollmentType): KotlinEnrollmentType {
        return when (type) {
            EnrollmentType.StudentEnrollment -> KotlinEnrollmentType.Student
            EnrollmentType.TeacherEnrollment -> KotlinEnrollmentType.Teacher
            EnrollmentType.TaEnrollment -> KotlinEnrollmentType.Ta
            EnrollmentType.ObserverEnrollment -> KotlinEnrollmentType.Observer
            EnrollmentType.DesignerEnrollment -> KotlinEnrollmentType.Designer
            else -> KotlinEnrollmentType.NoEnrollment
        }
    }

    private fun <T> mapGraphQlAnnouncements(
        nodes: List<T?>?
    ): List<DiscussionTopicHeader> {
        return nodes?.mapNotNull { node ->
            node ?: return@mapNotNull null
            val id: Long
            val title: String?
            val message: String?
            val postedAt: java.util.Date?
            val isRead: Boolean

            when (node) {
                is DashboardCoursesQuery.Node1 -> {
                    id = node._id.toLongOrNull() ?: return@mapNotNull null
                    title = node.title
                    message = node.message
                    postedAt = node.postedAt
                    isRead = node.participant?.read == true
                }
                is CourseAnnouncementsQuery.Node1 -> {
                    id = node._id.toLongOrNull() ?: return@mapNotNull null
                    title = node.title
                    message = node.message
                    postedAt = node.postedAt
                    isRead = node.participant?.read == true
                }
                else -> return@mapNotNull null
            }

            if (isRead) return@mapNotNull null

            DiscussionTopicHeader(
                id = id,
                title = title,
                message = message,
                postedDate = postedAt,
                announcement = true
            )
        } ?: emptyList()
    }

    data class Params(
        val forceRefresh: Boolean = false
    )

    data class Result(
        val visibleCourses: List<Course>,
        val allCourses: List<Course>,
        val announcementsMap: Map<Long, List<DiscussionTopicHeader>> = emptyMap()
    )
}