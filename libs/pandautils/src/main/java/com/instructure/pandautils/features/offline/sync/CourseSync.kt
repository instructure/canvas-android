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

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.models.StudioMediaMetadata
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.offline.offlinecontent.CourseFileSharedRepository
import com.instructure.pandautils.room.offline.daos.CourseFeaturesDao
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.CustomGradeStatusDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.daos.PlannerItemDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.entities.CourseFeaturesEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.CustomGradeStatusEntity
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.PlannerItemEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.ConferenceFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade
import com.instructure.pandautils.room.offline.facade.UserFacade
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class CourseSync(
    private val courseApi: CourseAPI.CoursesInterface,
    private val pageApi: PageAPI.PagesInterface,
    private val userApi: UserAPI.UsersInterface,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val calendarEventApi: CalendarEventAPI.CalendarEventInterface,
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val pageFacade: PageFacade,
    private val userFacade: UserFacade,
    private val courseFacade: CourseFacade,
    private val assignmentFacade: AssignmentFacade,
    private val quizDao: QuizDao,
    private val quizApi: QuizAPI.QuizInterface,
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
    private val fileFolderDao: FileFolderDao,
    private val discussionTopicFacade: DiscussionTopicFacade,
    private val groupApi: GroupAPI.GroupInterface,
    private val groupFacade: GroupFacade,
    private val enrollmentsApi: EnrollmentAPI.EnrollmentInterface,
    private val courseSyncProgressDao: CourseSyncProgressDao,
    private val htmlParser: HtmlParser,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    private val pageDao: PageDao,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val fileSync: FileSync,
    private val customGradeStatusDao: CustomGradeStatusDao,
    private val customGradeStatusesManager: CustomGradeStatusesManager,
    private val plannerItemDao: PlannerItemDao
) {

    private val additionalFileIdsToSync = mutableMapOf<Long, Set<Long>>()
    private val externalFilesToSync = mutableMapOf<Long, Set<String>>()
    private val failedTabsPerCourse = mutableMapOf<Long, Set<String>>()

    private var studioMetadata: List<StudioMediaMetadata> = emptyList()
    val studioMediaIdsToSync = mutableSetOf<String>()

    private var isStopped = false
        set(value) = synchronized(this) {
            field = value
        }

    suspend fun syncCourses(courseIds: Set<Long>, studioMetadata: List<StudioMediaMetadata>) {
        this.studioMetadata = studioMetadata
        coroutineScope {
            courseIds.map {
                async { syncCourse(it) }
            }.awaitAll()
        }
    }

    private suspend fun syncCourse(courseId: Long) {
        additionalFileIdsToSync[courseId] = emptySet()
        externalFilesToSync[courseId] = emptySet()

        val courseSettingsWithFiles =
            courseSyncSettingsDao.findWithFilesById(courseId) ?: return
        val courseSettings = courseSettingsWithFiles.courseSyncSettings
        val course = fetchCourseDetails(courseId)

        initProgress(courseSettings, course)

        if (courseSettings.fullFileSync || courseSettingsWithFiles.files.isNotEmpty()) {
            fetchFiles(courseId)
        }

        coroutineScope {
            val filesDeferred = async { fileSync.syncFiles(courseSettingsWithFiles.courseSyncSettings) }
            val contentDeferred = async { fetchCourseContent(courseSettingsWithFiles, course) }

            listOf(contentDeferred, filesDeferred).awaitAll()
        }

        fileSync.syncAdditionalFiles(
            courseSettings,
            additionalFileIdsToSync[courseId].orEmpty(),
            externalFilesToSync[courseId].orEmpty(),
        )

        val progress = courseSyncProgressDao.findByCourseId(courseId)
        progress
            ?.copy(progressState = if (progress.tabs.any { it.value.state == ProgressState.ERROR }) ProgressState.ERROR else ProgressState.COMPLETED)
            ?.let {
                courseSyncProgressDao.update(it)
            }
    }

    private suspend fun fetchCourseContent(courseSettingsWithFiles: CourseSyncSettingsWithFiles, course: Course) {
        val courseSettings = courseSettingsWithFiles.courseSyncSettings
        if (courseSettings.isTabSelected(Tab.PAGES_ID)) {
            fetchPages(course.id)
        } else {
            pageFacade.deleteAllByCourseId(course.id)
        }

        // We need to do this after the pages request because we delete all the previous pages there
        val isHomeTabAPage = Tab.FRONT_PAGE_ID == course.homePageID
        if (isHomeTabAPage) {
            fetchHomePage(course.id)
        }

        if (courseSettings.areAnyTabsSelected(setOf(Tab.ASSIGNMENTS_ID, Tab.GRADES_ID, Tab.SYLLABUS_ID))) {
            fetchAssignments(course.id)
        } else {
            assignmentFacade.deleteAllByCourseId(course.id)
        }

        if (courseSettings.isTabSelected(Tab.SYLLABUS_ID)) {
            fetchSyllabus(course.id)
        } else {
            scheduleItemFacade.deleteAllByCourseId(course.id)
        }

        if (courseSettings.isTabSelected(Tab.CONFERENCES_ID)) {
            fetchConferences(course.id)
        } else {
            conferenceFacade.deleteAllByCourseId(course.id)
        }

        if (courseSettings.isTabSelected(Tab.DISCUSSIONS_ID)) {
            fetchDiscussions(course.id)
        } else {
            discussionTopicHeaderFacade.deleteAllByCourseId(course.id, false)
        }

        if (courseSettings.isTabSelected(Tab.ANNOUNCEMENTS_ID)) {
            fetchAnnouncements(course.id)
        } else {
            discussionTopicHeaderFacade.deleteAllByCourseId(course.id, true)
        }

        if (courseSettings.isTabSelected(Tab.PEOPLE_ID)) {
            fetchUsers(course.id)
        }

        if (courseSettings.isTabSelected(Tab.QUIZZES_ID)) {
            fetchAllQuizzes(CanvasContext.Type.COURSE.apiString, course.id)
        } else if (!courseSettings.areAnyTabsSelected(setOf(Tab.ASSIGNMENTS_ID, Tab.GRADES_ID, Tab.SYLLABUS_ID))) {
            quizDao.deleteAllByCourseId(course.id)
        }

        if (courseSettings.isTabSelected(Tab.MODULES_ID)) {
            fetchModules(course.id, courseSettingsWithFiles)
        } else {
            moduleFacade.deleteAllByCourseId(course.id)
        }
    }

    private suspend fun fetchSyllabus(courseId: Long) {
        fetchTab(courseId, Tab.SYLLABUS_ID) {
            val calendarEvents = fetchCalendarEvents(courseId)
            val assignmentEvents = fetchCalendarAssignments(courseId)
            val subAssignmentEvents = fetchCalendarSubAssignments(courseId)
            val scheduleItems = mutableListOf<ScheduleItem>()

            scheduleItems.addAll(calendarEvents)
            scheduleItems.addAll(assignmentEvents)
            scheduleItems.addAll(subAssignmentEvents)

            scheduleItemFacade.insertScheduleItems(scheduleItems, courseId)

            val plannerItems = fetchPlannerItems(courseId)
            plannerItemDao.deleteAllByCourseId(courseId)
            plannerItemDao.insertAll(plannerItems)
        }
    }

    private suspend fun fetchPlannerItems(courseId: Long): List<PlannerItemEntity> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        val plannerItems = plannerApi.getPlannerItems(
            null,
            null,
            listOf("course_$courseId"),
            "new_activity",
            restParams
        ).depaginate {
            plannerApi.nextPagePlannerItems(it, restParams)
        }.dataOrThrow

        return plannerItems.map { PlannerItemEntity(it, courseId) }
    }

    private suspend fun fetchCalendarEvents(courseId: Long): List<ScheduleItem> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        val calendarEvents = calendarEventApi.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
            null,
            null,
            listOf("course_$courseId"),
            restParams
        ).depaginate {
            calendarEventApi.next(it, restParams)
        }.dataOrThrow

        calendarEvents.forEach { it.description = parseHtmlContent(it.description, courseId) }

        return calendarEvents
    }

    private suspend fun fetchCalendarAssignments(courseId: Long): List<ScheduleItem> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        val calendarAssignments = calendarEventApi.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
            null,
            null,
            listOf("course_$courseId"),
            restParams
        ).depaginate {
            calendarEventApi.next(it, restParams)
        }.dataOrThrow

        calendarAssignments.forEach { it.description = parseHtmlContent(it.description, courseId) }

        return calendarAssignments
    }

    private suspend fun fetchCalendarSubAssignments(courseId: Long): List<ScheduleItem> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        val calendarAssignments = calendarEventApi.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.SUB_ASSIGNMENT.apiName,
            null,
            null,
            listOf("course_$courseId"),
            restParams
        ).depaginate {
            calendarEventApi.next(it, restParams)
        }.dataOrThrow

        calendarAssignments.forEach { it.description = parseHtmlContent(it.description, courseId) }

        return calendarAssignments
    }

    private suspend fun fetchPages(courseId: Long) {
        fetchTab(courseId, Tab.PAGES_ID) {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            val pages = pageApi.getFirstPagePagesWithBody(courseId, CanvasContext.Type.COURSE.apiString, params)
                .depaginate { nextUrl ->
                    pageApi.getNextPagePagesList(nextUrl, params)
                }.dataOrThrow

            pages.forEach {
                it.body = parseHtmlContent(it.body, courseId)
            }

            pageFacade.insertPages(pages, courseId)
        }
    }

    private suspend fun fetchHomePage(courseId: Long) {
        try {
            val frontPage = pageApi.getFrontPage(
                CanvasContext.Type.COURSE.apiString,
                courseId,
                RestParams(isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            ).dataOrNull
            if (frontPage != null) {
                frontPage.body = parseHtmlContent(frontPage.body, courseId)
                pageFacade.insertPage(frontPage, courseId)
            }
        } catch (e: Exception) {
            firebaseCrashlytics.recordException(e)
        }
    }

    private suspend fun fetchAssignments(courseId: Long) {
        fetchTab(courseId, Tab.ASSIGNMENTS_ID, Tab.GRADES_ID) {
            val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            val assignmentGroups = assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams)
                .depaginate { nextUrl ->
                    assignmentApi.getNextPageAssignmentGroupListWithAssignments(nextUrl, restParams)
                }.dataOrThrow

            assignmentGroups.forEach { group ->
                group.assignments.forEach {
                    it.description = parseHtmlContent(it.description, courseId)
                    it.discussionTopicHeader?.message = parseHtmlContent(it.discussionTopicHeader?.message, courseId)
                }
            }

            fetchCustomGradeStatuses(courseId)

            fetchQuizzes(assignmentGroups, courseId)

            assignmentFacade.insertAssignmentGroups(assignmentGroups, courseId)
        }
    }

    private suspend fun fetchCustomGradeStatuses(courseId: Long) {
        val customGradeStatuses = customGradeStatusesManager.getCustomGradeStatuses(courseId, true)
            ?.course
            ?.customGradeStatusesConnection
            ?.nodes
            ?.filterNotNull()
            ?.map { CustomGradeStatusEntity(it, courseId) }
            .orEmpty()

        customGradeStatusDao.insertAll(customGradeStatuses)
    }

    private suspend fun fetchCourseDetails(courseId: Long): Course {
        val params = RestParams(isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        val course = courseApi.getFullCourseContent(courseId, params).dataOrThrow
        val enrollments = course.enrollments.orEmpty().flatMap {
            enrollmentsApi.getEnrollmentsForUserInCourse(courseId, it.userId, params).dataOrThrow
        }.toMutableList()
        val courseSettings = courseApi.getCourseSettings(courseId, params).dataOrThrow

        course.syllabusBody = parseHtmlContent(course.syllabusBody, courseId)

        courseFacade.insertCourse(course.copy(enrollments = enrollments, settings = courseSettings))

        val courseFeatures = featuresApi.getEnabledFeaturesForCourse(courseId, params).dataOrNull
        courseFeatures?.let {
            courseFeaturesDao.insert(CourseFeaturesEntity(courseId, it))
        }

        return course
    }

    private suspend fun fetchUsers(courseId: Long) {
        fetchTab(courseId, Tab.PEOPLE_ID) {
            val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            val users = userApi.getFirstPagePeopleList(courseId, CanvasContext.Type.COURSE.apiString, restParams)
                .depaginate { userApi.getNextPagePeopleList(it, restParams) }.dataOrThrow

            userFacade.insertUsers(users, courseId)
        }
    }

    private suspend fun fetchQuizzes(assignmentGroups: List<AssignmentGroup>, courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        val quizzes = mutableListOf<QuizEntity>()
        assignmentGroups.forEach { group ->
            group.assignments.forEach { assignment ->
                if (assignment.quizId != 0L) {
                    val quiz = quizApi.getQuiz(assignment.courseId, assignment.quizId, params).dataOrNull
                    quiz?.description = parseHtmlContent(quiz?.description, courseId)
                    quiz?.let { quizzes.add(QuizEntity(it, assignment.courseId)) }
                }
            }
        }
        quizDao.deleteAndInsertAll(quizzes, courseId)
    }

    private suspend fun fetchAllQuizzes(contextType: String, courseId: Long) {
        fetchTab(courseId, Tab.QUIZZES_ID) {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            val quizzes = quizApi.getFirstPageQuizzesList(contextType, courseId, params).depaginate { nextUrl ->
                quizApi.getNextPageQuizzesList(nextUrl, params)
            }.dataOrThrow

            quizzes.forEach {
                it.description = parseHtmlContent(it.description, courseId)
            }

            quizDao.deleteAndInsertAll(quizzes.map { QuizEntity(it, courseId) }, courseId)
        }
    }

    private suspend fun fetchConferences(courseId: Long) {
        fetchTab(courseId, Tab.CONFERENCES_ID) {
            val conferences = getConferencesForContext(CanvasContext.emptyCourseContext(courseId), true).dataOrThrow

            conferenceFacade.insertConferences(conferences, courseId)
        }
    }

    private suspend fun getConferencesForContext(
        canvasContext: CanvasContext, forceNetwork: Boolean
    ): DataResult<List<Conference>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork, shouldLoginOnTokenError = false)

        return conferencesApi.getConferencesForContext(canvasContext.toAPIString().drop(1), params).map {
            it.conferences
        }.depaginate { url ->
            conferencesApi.getNextPage(url, params).map { it.conferences }
        }
    }

    private suspend fun fetchDiscussions(courseId: Long) {
        fetchTab(courseId, Tab.DISCUSSIONS_ID) {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            val discussions =
                discussionApi.getFirstPageDiscussionTopicHeaders(CanvasContext.Type.COURSE.apiString, courseId, params)
                    .depaginate { nextPage -> discussionApi.getNextPage(nextPage, params) }.dataOrThrow

            discussions.forEach {
                it.message = parseHtmlContent(it.message, courseId)
                it.attachments.forEach {
                    additionalFileIdsToSync[courseId] = additionalFileIdsToSync[courseId].orEmpty() + it.id
                }
            }

            discussionTopicHeaderFacade.insertDiscussions(discussions, courseId, false)

            fetchDiscussionDetails(discussions, courseId)
        }
    }

    private suspend fun fetchAnnouncements(courseId: Long) {
        fetchTab(courseId, Tab.ANNOUNCEMENTS_ID) {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            val announcements =
                announcementApi.getFirstPageAnnouncementsList(CanvasContext.Type.COURSE.apiString, courseId, params)
                    .depaginate { nextPage ->
                        announcementApi.getNextPageAnnouncementsList(
                            nextPage,
                            params
                        )
                    }.dataOrThrow

            announcements.forEach {
                it.message = parseHtmlContent(it.message, courseId)
                it.attachments.forEach {
                    additionalFileIdsToSync[courseId] = additionalFileIdsToSync[courseId].orEmpty() + it.id
                }
            }

            discussionTopicHeaderFacade.insertDiscussions(announcements, courseId, true)

            fetchDiscussionDetails(announcements, courseId)
        }
    }

    private suspend fun fetchDiscussionDetails(discussions: List<DiscussionTopicHeader>, courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
        discussions.forEach { discussionTopicHeader ->
            val discussionTopic = discussionApi.getFullDiscussionTopic(
                CanvasContext.Type.COURSE.apiString,
                courseId,
                discussionTopicHeader.id,
                1,
                params
            ).dataOrNull
            discussionTopic?.let {
                val topic = parseDiscussionTopicHtml(it, courseId)
                discussionTopicFacade.insertDiscussionTopic(discussionTopicHeader.id, topic)
            }
        }

        val groups = groupApi.getFirstPageGroups(params)
            .depaginate { nextUrl -> groupApi.getNextPageGroups(nextUrl, params) }.dataOrNull

        groups?.let {
            it.forEach { group ->
                ApiPrefs.user?.let { groupFacade.insertGroupWithUser(group, it) }
            }
        }
    }

    private suspend fun parseDiscussionTopicHtml(discussionTopic: DiscussionTopic, courseId: Long): DiscussionTopic {
        discussionTopic.views.map { parseHtmlContent(it.message, courseId) }
        discussionTopic.views.map { it.replies?.map { parseDiscussionEntryHtml(it, courseId) } }
        return discussionTopic
    }

    private suspend fun parseDiscussionEntryHtml(discussionEntry: DiscussionEntry, courseId: Long): DiscussionEntry {
        discussionEntry.message = parseHtmlContent(discussionEntry.message, courseId)
        discussionEntry.replies?.map { parseDiscussionEntryHtml(it, courseId) }
        return discussionEntry
    }

    private suspend fun fetchModules(courseId: Long, courseSettings: CourseSyncSettingsWithFiles) {
        fetchTab(courseId, Tab.MODULES_ID) {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true, shouldLoginOnTokenError = false)
            val moduleObjects = moduleApi.getFirstPageModuleObjects(
                CanvasContext.Type.COURSE.apiString, courseId, params
            ).depaginate { nextPage ->
                moduleApi.getNextPageModuleObjectList(nextPage, params)
            }.dataOrThrow.map { moduleObject ->
                val moduleItems = moduleApi.getFirstPageModuleItems(
                    CanvasContext.Type.COURSE.apiString,
                    courseId,
                    moduleObject.id,
                    params
                ).depaginate { nextPage ->
                    moduleApi.getNextPageModuleItemList(nextPage, params)
                }.dataOrNull ?: moduleObject.items
                moduleObject.copy(items = moduleItems)
            }

            moduleFacade.insertModules(moduleObjects, courseId)

            val moduleItems = moduleObjects.flatMap { it.items }
            moduleItems.forEach {
                when (it.type) {
                    ModuleItem.Type.Page.name -> fetchPageModuleItem(courseId, it, params)
                    ModuleItem.Type.File.name -> fetchFileModuleItem(courseId, it, params, courseSettings)
                    ModuleItem.Type.Quiz.name -> fetchQuizModuleItem(courseId, it, params)
                }
            }
        }
    }

    private suspend fun fetchTab(courseId: Long, vararg tabIds: String, fetchBlock: suspend () -> Unit) {
        if (isStopped) return
        try {
            fetchBlock()
            updateTabSuccess(courseId, *tabIds)
        } catch (e: Exception) {
            e.printStackTrace()
            updateTabError(courseId, *tabIds)
            firebaseCrashlytics.recordException(e)
            failedTabsPerCourse[courseId] = failedTabsPerCourse[courseId].orEmpty() + tabIds
        }
    }

    private suspend fun fetchPageModuleItem(
        courseId: Long,
        it: ModuleItem,
        params: RestParams
    ) {
        // If the pages failed we might already have the page, but we need to get it again to make sure it's up to date
        // and download the files inside because those are always deleted
        if (it.pageUrl != null && (pageDao.findByUrlAndCourse(it.pageUrl!!, courseId) == null || failedTabsPerCourse[courseId]?.contains(Tab.PAGES_ID) == true)) {
            val page = pageApi.getDetailedPage(courseId, it.pageUrl!!, params).dataOrNull
            page?.body = parseHtmlContent(page?.body, courseId)
            page?.let { pageFacade.insertPage(it, courseId) }
        }
    }

    private suspend fun fetchFileModuleItem(
        courseId: Long,
        it: ModuleItem,
        params: RestParams,
        courseSettings: CourseSyncSettingsWithFiles
    ) {
        val fileId = it.contentId
        if (courseSettings.files.any { it.id == fileId }) return // File is selected for sync so we don't need to sync it

        val file = fileFolderApi.getCourseFile(courseId, it.contentId, params).dataOrNull
        if (file?.id != null) {
            additionalFileIdsToSync[courseId]?.let {
                additionalFileIdsToSync[courseId] = it + file.id
            }
        }
    }

    private suspend fun fetchQuizModuleItem(
        courseId: Long,
        it: ModuleItem,
        params: RestParams
    ) {
        // If the pages failed we might already have the page, but we need to get it again to make sure it's up to date
        // and download the files inside because those are always deleted
        if (quizDao.findById(it.contentId) == null || failedTabsPerCourse[courseId]?.contains(Tab.QUIZZES_ID) == true) {
            val quiz = quizApi.getQuiz(courseId, it.contentId, params).dataOrNull
            quiz?.description = parseHtmlContent(quiz?.description, courseId)
            quiz?.let { quizDao.insert(QuizEntity(it, courseId)) }
        }
    }

    private suspend fun parseHtmlContent(htmlContent: String?, courseId: Long): String? {
        val htmlParsingResult = htmlParser.createHtmlStringWithLocalFiles(htmlContent, courseId, studioMetadata)
        additionalFileIdsToSync[courseId]?.let {
            additionalFileIdsToSync[courseId] = it + htmlParsingResult.internalFileIds
        }
        externalFilesToSync[courseId]?.let {
            externalFilesToSync[courseId] = it + htmlParsingResult.externalFileUrls
        }
        studioMediaIdsToSync.addAll(htmlParsingResult.studioMediaIds)
        return htmlParsingResult.htmlWithLocalFileLinks
    }

    private suspend fun fetchFiles(courseId: Long) {
        val fileFolders = courseFileSharedRepository.getCourseFoldersAndFiles(courseId)

        val entities = fileFolders.map { FileFolderEntity(it) }
        fileFolderDao.replaceAll(entities, courseId)
    }

    private suspend fun initProgress(
        courseSettings: CourseSyncSettingsEntity,
        course: Course
    ) {
        val availableTabs = course.tabs?.map { it.tabId } ?: emptyList()
        val selectedTabs = courseSettings.tabs.filter { availableTabs.contains(it.key) && it.value == true }.keys
        val progress = (courseSyncProgressDao.findByCourseId(course.id) ?: createNewProgress(courseSettings))
            .copy(
                tabs = selectedTabs.associateWith { tabId ->
                    TabSyncData(
                        course.tabs?.find { it.tabId == tabId }?.label ?: tabId,
                        ProgressState.IN_PROGRESS
                    )
                }
            )

        courseSyncProgressDao.update(progress)
    }

    private suspend fun createNewProgress(courseSettings: CourseSyncSettingsEntity): CourseSyncProgressEntity {
        val newProgress = CourseSyncProgressEntity(
            courseId = courseSettings.courseId,
            courseName = courseSettings.courseName,
            progressState = ProgressState.STARTING,
        )
        courseSyncProgressDao.insert(newProgress)
        return newProgress
    }

    private suspend fun updateTabError(courseId: Long, vararg tabIds: String) {
        val progress = courseSyncProgressDao.findByCourseId(courseId)
        progress?.copy(
            tabs = progress.tabs.toMutableMap().apply {
                tabIds.forEach { tabId ->
                    get(tabId)?.copy(state = ProgressState.ERROR)?.let {
                        put(tabId, it)
                    }
                }

            },
        )?.let {
            courseSyncProgressDao.update(it)
        }
    }

    private suspend fun updateTabSuccess(courseId: Long, vararg tabIds: String) {
        val progress = courseSyncProgressDao.findByCourseId(courseId)
        progress?.copy(
            tabs = progress.tabs.toMutableMap().apply {
                tabIds.forEach { tabId ->
                    get(tabId)?.copy(state = ProgressState.COMPLETED)?.let {
                        put(tabId, it)
                    }
                }
            },
        )?.let {
            courseSyncProgressDao.update(it)
        }
    }
}