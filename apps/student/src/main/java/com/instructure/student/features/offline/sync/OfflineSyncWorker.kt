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
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.entities.PageEntity
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OfflineSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val courseApi: CourseAPI.CoursesInterface,
    private val pageApi: PageAPI.PagesInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val pageDao: PageDao,
    private val courseFacade: CourseFacade,
    private val assignmentFacade: AssignmentFacade
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val courses = courseSyncSettingsDao.findAll()
        courses.forEach { courseSettings ->
            fetchCourseDetails(courseSettings.courseId)
            if (courseSettings.pages) {
                fetchPages(courseSettings.courseId)
            }
            if (courseSettings.assignments || courseSettings.pages) {
                fetchAssignments(courseSettings.courseId)
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

        assignmentFacade.insertAssignmentGroups(assignmentGroups)
    }

    private suspend fun fetchCourseDetails(courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true)
        val course = courseApi.getFullCourseContent(courseId, params).dataOrThrow

        courseFacade.insertCourse(course)
    }
}