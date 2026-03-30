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
package com.instructure.horizon.features.dashboard.widget.course

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.horizon.database.course.HorizonDashboardCourseDao
import com.instructure.horizon.database.moduleitem.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.program.HorizonDashboardProgramDao
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class DashboardCourseLocalDataSource @Inject constructor(
    private val courseDao: HorizonDashboardCourseDao,
    private val programDao: HorizonDashboardProgramDao,
    private val moduleItemDao: HorizonDashboardModuleItemDao,
) : DashboardCourseDataSource {

    override suspend fun getEnrollments(): List<GetCoursesQuery.Enrollment> {
        return courseDao.getAll().map { entity ->
            GetCoursesQuery.Enrollment(
                id = entity.enrollmentId.toString(),
                state = EnrollmentWorkflowState.safeValueOf(entity.enrollmentState),
                lastActivityAt = null,
                course = GetCoursesQuery.Course(
                    id = entity.courseId.toString(),
                    name = entity.courseName,
                    image_download_url = entity.courseImageUrl,
                    syllabus_body = null,
                    account = null,
                    usersConnection = GetCoursesQuery.UsersConnection(
                        nodes = listOf(
                            GetCoursesQuery.Node(
                                courseProgression = GetCoursesQuery.CourseProgression(
                                    requirements = GetCoursesQuery.Requirements(
                                        completionPercentage = entity.completionPercentage,
                                    ),
                                    incompleteModulesConnection = null,
                                )
                            )
                        )
                    ),
                )
            )
        }
    }

    override suspend fun getPrograms(): List<Program> {
        val programs = programDao.getAll()
        return programs.map { programEntity ->
            val refs = programDao.getRefsForProgram(programEntity.programId)
            Program(
                id = programEntity.programId,
                name = programEntity.programName,
                description = null,
                startDate = null,
                endDate = null,
                variant = com.instructure.journey.type.ProgramVariantType.UNKNOWN__,
                sortedRequirements = refs.map { ref ->
                    ProgramRequirement(
                        id = "",
                        progressId = "",
                        courseId = ref.courseId,
                        required = false,
                        enrollmentStatus = ref.enrollmentStatus?.let {
                            ProgramProgressCourseEnrollmentStatus.safeValueOf(it)
                        },
                    )
                }
            )
        }
    }

    override suspend fun getModuleItemsForCourse(courseId: Long): List<ModuleObject> {
        val entity = moduleItemDao.getFirstForCourse(courseId) ?: return emptyList()
        val moduleItem = ModuleItem(
            id = entity.moduleItemId,
            moduleId = 0L,
            title = entity.moduleItemTitle,
            type = entity.moduleItemType,
            quizLti = entity.isQuizLti,
            estimatedDuration = entity.estimatedDuration,
            moduleDetails = entity.dueDateMs?.let { ms ->
                ModuleContentDetails(dueAt = isoFormatter.format(Date(ms)))
            },
        )
        return listOf(ModuleObject(items = listOf(moduleItem)))
    }

    companion object {
        private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
