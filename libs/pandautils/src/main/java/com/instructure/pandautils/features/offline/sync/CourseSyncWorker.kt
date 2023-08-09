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
 *
 */

package com.instructure.pandautils.features.offline.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.offline.offlinecontent.CourseFileSharedRepository
import com.instructure.pandautils.room.offline.daos.CourseFeaturesDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.CourseFeaturesEntity
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.ConferenceFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade
import com.instructure.pandautils.room.offline.facade.UserFacade
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CourseSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val courseApi: CourseAPI.CoursesInterface,
    private val pageApi: PageAPI.PagesInterface,
    private val userApi: UserAPI.UsersInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val pageFacade: PageFacade,
    private val userFacade: UserFacade,
    private val courseFacade: CourseFacade,
    private val assignmentFacade: AssignmentFacade,
    private val quizDao: QuizDao,
    private val quizApi: QuizAPI.QuizInterface,
    private val courseSettingsDao: CourseSettingsDao,
    private val scheduleItemFacade: ScheduleItemFacade,
    private val conferencesApi: ConferencesApi.ConferencesInterface,
    private val conferenceFacade: ConferenceFacade,
    private val discussionApi: DiscussionAPI.DiscussionInterface,
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
    private val announcementApi: AnnouncementAPI.AnnouncementInterface,
    private val moduleApi: ModuleAPI.ModuleInterface,
    private val moduleFacade: ModuleFacade,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    private val courseFeaturesDao: CourseFeaturesDao,
    private val courseFileSharedRepository: CourseFileSharedRepository,
    private val fileFolderDao: FileFolderDao
    ) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        val courseSettingsWithFiles = courseSyncSettingsDao.findWithFilesById(inputData.getLong(COURSE_ID, -1)) ?: return Result.failure()
        val courseSettings = courseSettingsWithFiles.courseSyncSettings

        val syllabusCourseIds = mutableListOf<Long>()

        fetchCourseDetails(courseSettings.courseId)
        if (courseSettings.isTabSelected(Tab.PAGES_ID)) {
            fetchPages(courseSettings.courseId)
        }
        if (courseSettings.areAnyTabsSelected(setOf(Tab.ASSIGNMENTS_ID, Tab.GRADES_ID, Tab.SYLLABUS_ID))) {
            fetchAssignments(courseSettings.courseId)
        }
        if (courseSettings.isTabSelected(Tab.SYLLABUS_ID)) {
            syllabusCourseIds.add(courseSettings.courseId)
        }
        if (courseSettings.isTabSelected(Tab.CONFERENCES_ID)) {
            fetchConferences(courseSettings.courseId)
        }
        if (courseSettings.isTabSelected(Tab.DISCUSSIONS_ID)) {
            fetchDiscussions(courseSettings.courseId)
        }
        if (courseSettings.isTabSelected(Tab.ANNOUNCEMENTS_ID)) {
            fetchAnnouncements(courseSettings.courseId)
        }
        if (courseSettings.isTabSelected(Tab.PEOPLE_ID)) {
            fetchUsers(courseSettings.courseId)
        }
        if (courseSettings.isTabSelected(Tab.MODULES_ID)) {
            fetchModules(courseSettings.courseId)
        }
        if (courseSettings.isTabSelected(Tab.QUIZZES_ID)) {
            fetchAllQuizzes(CanvasContext.Type.COURSE.apiString, courseSettings.courseId)
        }
        if (courseSettings.fullFileSync || courseSettingsWithFiles.files.isNotEmpty()) {
            fetchFiles(courseSettings.courseId)
        }

        fetchSyllabus(syllabusCourseIds)

        return Result.success()
    }

    private suspend fun fetchFiles(courseId: Long) {
        val fileFolders = courseFileSharedRepository.getCourseFoldersAndFiles(courseId)

        val entities = fileFolders.map { FileFolderEntity(it) }

        fileFolderDao.insertAll(entities)
    }

    private suspend fun fetchSyllabus(courseIds: List<Long>) {
        if (courseIds.isNotEmpty()) {
            val calendarEvents = fetchCalendarEvents(courseIds)
            val assignmentEvents = fetchCalendarAssignments(courseIds)
            val scheduleItems = mutableListOf<ScheduleItem>()

            if (calendarEvents != null) {
                scheduleItems.addAll(calendarEvents)
            }
            if (assignmentEvents != null) {
                scheduleItems.addAll(assignmentEvents)
            }

            scheduleItemFacade.insertScheduleItems(scheduleItems)
        }
    }

    private suspend fun fetchCalendarEvents(courseIds: List<Long>): List<ScheduleItem>? {
        val contextCodes = courseIds.map { "course_$it" }
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        return calendarEventApi.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
            null,
            null,
            contextCodes,
            restParams
        ).depaginate { calendarEventApi.next(it, restParams) }.dataOrNull
    }

    private suspend fun fetchCalendarAssignments(courseIds: List<Long>): List<ScheduleItem>? {
        val contextCodes = courseIds.map { "course_$it" }
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        return calendarEventApi.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
            null,
            null,
            contextCodes,
            restParams
        ).depaginate { calendarEventApi.next(it, restParams) }.dataOrNull
    }

    private suspend fun fetchPages(courseId: Long) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val pages = pageApi.getFirstPagePagesWithBody(courseId, CanvasContext.Type.COURSE.apiString, params)
            .depaginate { nextUrl ->
                pageApi.getNextPagePagesList(nextUrl, params)
            }.dataOrNull.orEmpty()

        pageFacade.insertPages(pages, courseId)
    }

    private suspend fun fetchAssignments(courseId: Long) {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val assignmentGroups = assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams)
            .depaginate { nextUrl ->
                assignmentApi.getNextPageAssignmentGroupListWithAssignments(nextUrl, restParams)
            }.dataOrNull.orEmpty()

        fetchQuizzes(assignmentGroups)

        assignmentFacade.insertAssignmentGroups(assignmentGroups)
    }

    private suspend fun fetchCourseDetails(courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true)
        val course = courseApi.getFullCourseContent(courseId, params).dataOrThrow

        courseFacade.insertCourse(course)

        val courseSettings = courseApi.getCourseSettings(courseId, params).dataOrNull
        courseSettings?.let {
            courseSettingsDao.insert(CourseSettingsEntity(it, courseId))
        }

        val courseFeatures = featuresApi.getEnabledFeaturesForCourse(courseId, params).dataOrNull
        courseFeatures?.let {
            courseFeaturesDao.insert(CourseFeaturesEntity(courseId, it))
        }
    }

    private suspend fun fetchUsers(courseId: Long) {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val users = userApi.getFirstPagePeopleList(courseId, CanvasContext.Type.COURSE.apiString, restParams)
            .depaginate { userApi.getNextPagePeopleList(it, restParams) }.dataOrThrow

        users.let {
            userFacade.insertUsers(it, courseId)
        }
    }

    private suspend fun fetchQuizzes(assignmentGroups: List<AssignmentGroup>) {
        val params = RestParams(isForceReadFromNetwork = true)
        assignmentGroups.forEach { group ->
            group.assignments.forEach { assignment ->
                if (assignment.quizId != 0L) {
                    val quiz = quizApi.getQuiz(assignment.courseId, assignment.quizId, params).dataOrNull
                    quiz?.let { quizDao.insert(QuizEntity(it)) }
                }
            }
        }
    }

    private suspend fun fetchAllQuizzes(contextType: String, courseId: Long) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val quizzes = quizApi.getFirstPageQuizzesList(contextType, courseId, params).depaginate { nextUrl ->
            quizApi.getNextPageQuizzesList(nextUrl, params)
        }.dataOrNull
        quizzes?.forEach { quiz ->
            quizDao.insert(QuizEntity(quiz, courseId))

        }
    }

    private suspend fun fetchConferences(courseId: Long) {
        val conferences = getConferencesForContext(CanvasContext.emptyCourseContext(courseId), true).dataOrNull
        conferences?.let { conferenceFacade.insertConferences(it, courseId) }
    }

    private suspend fun getConferencesForContext(
        canvasContext: CanvasContext, forceNetwork: Boolean
    ): DataResult<List<Conference>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)

        return conferencesApi.getConferencesForContext(canvasContext.toAPIString().drop(1), params).map {
            it.conferences
        }.depaginate { url ->
            conferencesApi.getNextPage(url, params).map { it.conferences }
        }
    }

    private suspend fun fetchDiscussions(courseId: Long) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val discussions =
            discussionApi.getFirstPageDiscussionTopicHeaders(CanvasContext.Type.COURSE.apiString, courseId, params)
                .depaginate { nextPage -> discussionApi.getNextPage(nextPage, params) }.dataOrNull.orEmpty()

        discussionTopicHeaderFacade.insertDiscussions(discussions, courseId)
    }

    private suspend fun fetchAnnouncements(courseId: Long) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val announcements =
            announcementApi.getFirstPageAnnouncementsList(CanvasContext.Type.COURSE.apiString, courseId, params)
                .depaginate { nextPage ->
                    announcementApi.getNextPageAnnouncementsList(
                        nextPage,
                        params
                    )
                }.dataOrNull.orEmpty()

        discussionTopicHeaderFacade.insertDiscussions(announcements, courseId)
    }

    private suspend fun fetchModules(courseId: Long) {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        val moduleObjects = moduleApi.getFirstPageModuleObjects(
            CanvasContext.Type.COURSE.apiString, courseId, params
        ).depaginate { nextPage ->
            moduleApi.getNextPageModuleObjectList(nextPage, params)
        }
            .dataOrNull
            ?.map { moduleObject ->
                val moduleItems =
                    moduleApi.getFirstPageModuleItems(
                        CanvasContext.Type.COURSE.apiString,
                        courseId,
                        moduleObject.id,
                        params
                    ).depaginate { nextPage ->
                        moduleApi.getNextPageModuleItemList(nextPage, params)
                    }.dataOrNull ?: moduleObject.items
                moduleObject.copy(items = moduleItems)
            }.orEmpty()

        moduleFacade.insertModules(moduleObjects, courseId)
    }

    companion object {
        const val COURSE_ID = "course_id"
    }
}