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

package com.instructure.pandautils.data.repository.dashboardcourses

import com.instructure.canvasapi2.DashboardCoursesQuery
import com.instructure.canvasapi2.managers.graphql.DashboardCoursesManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.models.Enrollment.EnrollmentType as KotlinEnrollmentType

class DashboardCoursesNetworkDataSource(
    private val dashboardCoursesManager: DashboardCoursesManager
) : DashboardCoursesDataSource {

    override suspend fun getCourses(forceRefresh: Boolean): DashboardCoursesData {
        val data = dashboardCoursesManager.getDashboardCourses(forceNetwork = forceRefresh)
        val courses = data.allCourses?.mapNotNull { mapGraphQlCourse(it) } ?: emptyList()
        // Announcements are intentionally not loaded here. Online they are fetched per visible
        // course in a capped background pass (see CoursesWidgetViewModel) so course cards render
        // immediately instead of blocking on announcement pagination.
        return DashboardCoursesData(courses = courses)
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
            enrollments = enrollment?.let { mutableListOf(it) } ?: mutableListOf(),
            homePage = Course.HomePage.fromApiString(graphQlCourse.dashboardCard?.defaultView)
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
}
