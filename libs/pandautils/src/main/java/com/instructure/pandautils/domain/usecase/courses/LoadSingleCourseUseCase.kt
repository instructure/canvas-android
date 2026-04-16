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

import com.instructure.canvasapi2.DashboardSingleCourseQuery
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Enrollment.EnrollmentType as KotlinEnrollmentType
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoadSingleCourseUseCase @Inject constructor(
    private val dashboardCoursesManager: DashboardCoursesManager
) : BaseUseCase<LoadSingleCourseUseCase.Params, LoadSingleCourseUseCase.Result>() {

    override suspend fun execute(params: Params): Result {
        val data = dashboardCoursesManager.getSingleCourse(
            courseId = params.courseId,
            forceNetwork = params.forceNetwork
        )

        val courseNode = data.course?.onCourse
            ?: throw IllegalStateException("Course not found: ${params.courseId}")

        val course = mapGraphQlCourse(courseNode)
            ?: throw IllegalStateException("Failed to map course: ${params.courseId}")

        val announcements = mapGraphQlAnnouncements(courseNode.announcements)

        return Result(course = course, announcements = announcements)
    }

    private fun mapGraphQlCourse(courseNode: DashboardSingleCourseQuery.OnCourse): Course? {
        val courseId = courseNode._id.toLongOrNull() ?: return null

        val enrollmentNode = courseNode.enrollmentsConnection?.nodes?.firstOrNull()
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
            name = courseNode.name,
            courseCode = courseNode.courseCode,
            imageUrl = courseNode.imageUrl,
            courseColor = courseNode.dashboardCard?.color,
            isFavorite = courseNode.dashboardCard?.isFavorited == true,
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

    private fun mapGraphQlAnnouncements(
        announcementsConnection: DashboardSingleCourseQuery.Announcements?
    ): List<DiscussionTopicHeader> {
        return announcementsConnection?.nodes?.mapNotNull { node ->
            node ?: return@mapNotNull null
            if (node.participant?.read == true) return@mapNotNull null

            DiscussionTopicHeader(
                id = node._id.toLongOrNull() ?: return@mapNotNull null,
                title = node.title,
                message = node.message,
                postedDate = node.postedAt,
                announcement = true
            )
        } ?: emptyList()
    }

    data class Params(
        val courseId: Long,
        val forceNetwork: Boolean = true
    )

    data class Result(
        val course: Course,
        val announcements: List<DiscussionTopicHeader>
    )
}