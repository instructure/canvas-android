/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.student.features.offline.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val courseApi: CourseAPI.CoursesInterface,
    private val userApi: UserAPI.UsersInterface,
    private val pageApi: PageAPI.PagesInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val enrollmentDao: EnrollmentDao,
    private val gradingPeriodDao: GradingPeriodDao,
    private val courseGradingPeriodDao: CourseGradingPeriodDao,
    private val tabDao: TabDao,
    private val termDao: TermDao,
    private val gradesDao: GradesDao,
    private val sectionDao: SectionDao,
    private val pageDao: PageDao
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val courses = courseSyncSettingsDao.findAll()
        courses.forEach { courseSettings ->
            fetchCourseDetails(courseSettings.courseId)
            if (courseSettings.pages) {
                fetchPages(courseSettings.courseId)
            }
        }
        return Result.success()
    }

    private suspend fun fetchPages(courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true)
        val pages = pageApi.getFirstPagePages(courseId, "courses", params).depaginate { nextUrl ->
            pageApi.getNextPagePagesList(nextUrl, params)
        }.dataOrThrow

        val entities = pages.map {
            PageEntity(it, courseId)
        }

        pageDao.insert(*entities.toTypedArray())
    }

    private suspend fun fetchAssignments(courseId: Long) {
        val restParams = RestParams(isForceReadFromNetwork = true)
        val assignmentGroups = assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams)
            .depaginate { nextUrl ->
                assignmentApi.getNextPageAssignmentGroupListWithAssignments(nextUrl, restParams)
            }.dataOrThrow
    }

    private fun fetchGrades(courseId: Long) {

    }

    private suspend fun fetchCourseDetails(courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true)
        val course = courseApi.getFullCourseContent(courseId, params).dataOrThrow

        course.term?.let {
            termDao.insert(TermEntity(it))
        }

        courseDao.insert(CourseEntity(course))

        course.enrollments?.forEach { enrollment ->
            if (enrollment.userId != 0L) {
                val user = enrollment.user ?: userApi.getUser(
                    enrollment.userId,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrThrow
                userDao.insert(UserEntity(user))
            }

            enrollment.observedUser?.let { observedUser ->
                userDao.insert(UserEntity(observedUser))
            }

            val enrollmentId = enrollmentDao.insert(
                EnrollmentEntity(
                    enrollment,
                    courseId = courseId,
                    observedUserId = enrollment.observedUser?.id
                )
            )
            enrollment.grades?.let { grades -> gradesDao.insert(GradesEntity(grades, enrollmentId)) }
        }

        course.gradingPeriods?.forEach { gradingPeriod ->
            gradingPeriodDao.insert(GradingPeriodEntity(gradingPeriod))
            courseGradingPeriodDao.insert(CourseGradingPeriodEntity(courseId, gradingPeriod.id))
        }

        course.sections.forEach { section ->
            sectionDao.insert(SectionEntity(section))
        }

        course.tabs?.forEach { tab ->
            tabDao.insert(TabEntity(tab, courseId))
        }
    }
}