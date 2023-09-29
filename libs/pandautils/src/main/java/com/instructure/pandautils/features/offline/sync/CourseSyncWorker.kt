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
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.instructure.canvasapi2.apis.*
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.pandautils.features.offline.offlinecontent.CourseFileSharedRepository
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.CourseFeaturesEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.pandautils.room.offline.facade.*
import com.instructure.pandautils.utils.toJson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

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
    private val fileSyncSettingsDao: FileSyncSettingsDao,
    private val localFileDao: LocalFileDao,
    private val workManager: WorkManager,
    private val syncSettingsFacade: SyncSettingsFacade,
    private val enrollmentsApi: EnrollmentAPI.EnrollmentInterface,
    private val htmlParser: HtmlParser,
    private val fileFolderApi: FileFolderAPI.FilesFoldersInterface
) : CoroutineWorker(context, workerParameters) {

    private lateinit var progress: CourseProgress

    private var fileOperation: Operation? = null

    private val additionalFileIdsToSync = mutableSetOf<Long>()
    private val externalFilesToSync = mutableSetOf<String>()

    override suspend fun doWork(): Result {
        val courseSettingsWithFiles = courseSyncSettingsDao.findWithFilesById(inputData.getLong(COURSE_ID, -1)) ?: return Result.failure()
        val courseSettings = courseSettingsWithFiles.courseSyncSettings
        val courseId = courseSettings.courseId
        val course = fetchCourseDetails(courseId)

        progress = initProgress(courseSettings, course)
        updateProgress()

        if (courseSettings.fullFileSync || courseSettingsWithFiles.files.isNotEmpty()) {
            fetchFiles(courseId)
        }

        val workContinuation = syncFiles(courseSettings)

        if (courseSettings.isTabSelected(Tab.PAGES_ID)) {
            fetchPages(courseId)
        } else {
            pageFacade.deleteAllByCourseId(courseId)
        }

        if (courseSettings.areAnyTabsSelected(setOf(Tab.ASSIGNMENTS_ID, Tab.GRADES_ID, Tab.SYLLABUS_ID))) {
            fetchAssignments(courseId)
        } else {
            assignmentFacade.deleteAllByCourseId(courseId)
        }

        if (courseSettings.isTabSelected(Tab.SYLLABUS_ID)) {
            fetchSyllabus(courseId)
        } else {
            scheduleItemFacade.deleteAllByCourseId(courseId)
        }

        if (courseSettings.isTabSelected(Tab.CONFERENCES_ID)) {
            fetchConferences(courseId)
        } else {
            conferenceFacade.deleteAllByCourseId(courseId)
        }

        if (courseSettings.isTabSelected(Tab.DISCUSSIONS_ID)) {
            fetchDiscussions(courseId)
        } else {
            discussionTopicHeaderFacade.deleteAllByCourseId(courseId, false)
        }

        if (courseSettings.isTabSelected(Tab.ANNOUNCEMENTS_ID)) {
            fetchAnnouncements(courseId)
        } else {
            discussionTopicHeaderFacade.deleteAllByCourseId(courseId, true)
        }

        if (courseSettings.isTabSelected(Tab.PEOPLE_ID)) {
            fetchUsers(courseId)
        }

        if (courseSettings.isTabSelected(Tab.MODULES_ID)) {
            fetchModules(courseId)
        } else {
            moduleFacade.deleteAllByCourseId(courseId)
        }

        if (courseSettings.isTabSelected(Tab.QUIZZES_ID)) {
            fetchAllQuizzes(CanvasContext.Type.COURSE.apiString, courseId)
        } else if (!courseSettings.areAnyTabsSelected(setOf(Tab.ASSIGNMENTS_ID, Tab.GRADES_ID, Tab.SYLLABUS_ID))) {
            quizDao.deleteAllByCourseId(courseId)
        }

        syncAdditionalFiles(courseSettings, workContinuation)

        return Result.success(workDataOf(OUTPUT to progress.toJson()))
    }

    private suspend fun fetchSyllabus(courseId: Long) {
        try {
            val calendarEvents = fetchCalendarEvents(courseId)
            val assignmentEvents = fetchCalendarAssignments(courseId)
            val scheduleItems = mutableListOf<ScheduleItem>()

            scheduleItems.addAll(calendarEvents)
            scheduleItems.addAll(assignmentEvents)

            scheduleItemFacade.insertScheduleItems(scheduleItems, courseId)

            updateTabSuccess(Tab.SYLLABUS_ID)
        } catch (e: Exception) {
            updateTabError(Tab.SYLLABUS_ID)
        }
    }

    private suspend fun fetchCalendarEvents(courseId: Long): List<ScheduleItem> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        return calendarEventApi.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.CALENDAR.apiName,
            null,
            null,
            listOf("course_$courseId"),
            restParams
        ).depaginate {
            calendarEventApi.next(it, restParams)
        }.dataOrThrow
    }

    private suspend fun fetchCalendarAssignments(courseId: Long): List<ScheduleItem> {
        val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
        return calendarEventApi.getCalendarEvents(
            true,
            CalendarEventAPI.CalendarEventType.ASSIGNMENT.apiName,
            null,
            null,
            listOf("course_$courseId"),
            restParams
        ).depaginate {
            calendarEventApi.next(it, restParams)
        }.dataOrThrow
    }

    private suspend fun fetchPages(courseId: Long) {
        try {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            val pages = pageApi.getFirstPagePagesWithBody(courseId, CanvasContext.Type.COURSE.apiString, params)
                .depaginate { nextUrl ->
                    pageApi.getNextPagePagesList(nextUrl, params)
                }.dataOrThrow
                .map {
                    val htmlParsingResult = htmlParser.createHtmlStringWithLocalFiles(it.body, courseId)
                    additionalFileIdsToSync.addAll(htmlParsingResult.internalFileIds)
                    externalFilesToSync.addAll(htmlParsingResult.externalFileUrls)
                    it.copy(body = htmlParsingResult.htmlWithLocalFileLinks)
                }

            pageFacade.insertPages(pages, courseId)

            updateTabSuccess(Tab.PAGES_ID)
        } catch (e: Exception) {
            updateTabError(Tab.PAGES_ID)
        }
    }

    private suspend fun fetchAssignments(courseId: Long) {
        try {
            val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            val assignmentGroups = assignmentApi.getFirstPageAssignmentGroupListWithAssignments(courseId, restParams)
                .depaginate { nextUrl ->
                    assignmentApi.getNextPageAssignmentGroupListWithAssignments(nextUrl, restParams)
                }.dataOrThrow

            fetchQuizzes(assignmentGroups, courseId)

            assignmentFacade.insertAssignmentGroups(assignmentGroups, courseId)

            updateTabSuccess(Tab.ASSIGNMENTS_ID)
            updateTabSuccess(Tab.GRADES_ID)
        } catch (e: Exception) {
            updateTabError(Tab.ASSIGNMENTS_ID)
            updateTabError(Tab.GRADES_ID)
        }
    }

    private suspend fun fetchCourseDetails(courseId: Long): Course {
        val params = RestParams(isForceReadFromNetwork = true)
        val course = courseApi.getFullCourseContent(courseId, params).dataOrThrow
        val enrollments = course.enrollments.orEmpty().flatMap {
            enrollmentsApi.getEnrollmentsForUserInCourse(courseId, it.userId, params).dataOrThrow
        }.toMutableList()

        courseFacade.insertCourse(course.copy(enrollments = enrollments))

        val courseFeatures = featuresApi.getEnabledFeaturesForCourse(courseId, params).dataOrNull
        courseFeatures?.let {
            courseFeaturesDao.insert(CourseFeaturesEntity(courseId, it))
        }

        return course
    }

    private suspend fun fetchUsers(courseId: Long) {
        try {
            val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            val users = userApi.getFirstPagePeopleList(courseId, CanvasContext.Type.COURSE.apiString, restParams)
                .depaginate { userApi.getNextPagePeopleList(it, restParams) }.dataOrThrow

            userFacade.insertUsers(users, courseId)

            updateTabSuccess(Tab.PEOPLE_ID)
        } catch (e: Exception) {
            updateTabError(Tab.PEOPLE_ID)
        }
    }

    private suspend fun fetchQuizzes(assignmentGroups: List<AssignmentGroup>, courseId: Long) {
        val params = RestParams(isForceReadFromNetwork = true)
        val quizzes = mutableListOf<QuizEntity>()
        assignmentGroups.forEach { group ->
            group.assignments.forEach { assignment ->
                if (assignment.quizId != 0L) {
                    val quiz = quizApi.getQuiz(assignment.courseId, assignment.quizId, params).dataOrNull
                    quiz?.let { quizzes.add(QuizEntity(it, assignment.courseId)) }
                }
            }
        }
        quizDao.deleteAndInsertAll(quizzes, courseId)
    }

    private suspend fun fetchAllQuizzes(contextType: String, courseId: Long) {
        try {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            val quizzes = quizApi.getFirstPageQuizzesList(contextType, courseId, params).depaginate { nextUrl ->
                quizApi.getNextPageQuizzesList(nextUrl, params)
            }.dataOrThrow

            quizDao.deleteAndInsertAll(quizzes.map { QuizEntity(it, courseId) }, courseId)

            updateTabSuccess(Tab.QUIZZES_ID)
        } catch (e: Exception) {
            updateTabError(Tab.QUIZZES_ID)
        }
    }

    private suspend fun fetchConferences(courseId: Long) {
        try {
            val conferences = getConferencesForContext(CanvasContext.emptyCourseContext(courseId), true).dataOrThrow

            conferenceFacade.insertConferences(conferences, courseId)

            updateTabSuccess(Tab.CONFERENCES_ID)
        } catch (e: Exception) {
            updateTabError(Tab.CONFERENCES_ID)
        }
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
        try {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            val discussions = discussionApi.getFirstPageDiscussionTopicHeaders(CanvasContext.Type.COURSE.apiString, courseId, params)
                .depaginate { nextPage -> discussionApi.getNextPage(nextPage, params) }.dataOrThrow

            discussionTopicHeaderFacade.insertDiscussions(discussions, courseId, false)

            updateTabSuccess(Tab.DISCUSSIONS_ID)
        } catch (e: Exception) {
            updateTabError(Tab.DISCUSSIONS_ID)
        }
    }

    private suspend fun fetchAnnouncements(courseId: Long) {
        try {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
            val announcements = announcementApi.getFirstPageAnnouncementsList(CanvasContext.Type.COURSE.apiString, courseId, params)
                .depaginate { nextPage -> announcementApi.getNextPageAnnouncementsList(nextPage, params) }.dataOrThrow

            discussionTopicHeaderFacade.insertDiscussions(announcements, courseId, true)

            updateTabSuccess(Tab.ANNOUNCEMENTS_ID)
        } catch (e: Exception) {
            updateTabError(Tab.ANNOUNCEMENTS_ID)
        }
    }

    private suspend fun fetchModules(courseId: Long) {
        try {
            val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = true)
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

            updateTabSuccess(Tab.MODULES_ID)
        } catch (e: Exception) {
            updateTabError(Tab.MODULES_ID)
        }
    }

    private suspend fun syncFiles(syncSettings: CourseSyncSettingsEntity): WorkContinuation? {
        val courseId = syncSettings.courseId
        val allFiles = getAllFiles(courseId)
        val allFileIds = allFiles.map { it.id }

        cleanupSyncedFiles(courseId, allFileIds)

        val fileSyncData = mutableListOf<FileSyncData>()
        val fileWorkers = mutableListOf<OneTimeWorkRequest>()
        fileFolderDao.findFilesToSync(courseId, syncSettings.fullFileSync)
            .forEach {
                val worker = FileSyncWorker.createOneTimeWorkRequest(
                    courseId,
                    it.id,
                    it.displayName.orEmpty(),
                    it.url.orEmpty(),
                    syncSettingsFacade.getSyncSettings().wifiOnly
                )
                fileWorkers.add(worker)
                fileSyncData.add(FileSyncData(worker.id.toString(), it.displayName.orEmpty(), it.size))
            }

        val chunkedWorkers = fileWorkers.chunked(6)

        if (chunkedWorkers.isEmpty()) {
            progress = progress.copy(fileSyncData = emptyList())
            updateProgress()
            return null
        }

        var continuation = workManager
            .beginWith(chunkedWorkers.first())

        chunkedWorkers.drop(1).forEach {
            continuation = continuation.then(it)
        }

        fileOperation = continuation.enqueue()

        progress = progress.copy(fileSyncData = fileSyncData)
        updateProgress()

        return continuation
    }

    private suspend fun syncAdditionalFiles(
        syncSettings: CourseSyncSettingsEntity,
        workContinuation: WorkContinuation?
    ) {
        val courseId = syncSettings.courseId

        val fileSyncData = mutableListOf<FileSyncData>()
        val fileWorkers = mutableListOf<OneTimeWorkRequest>()
        val additionalPublicFilesToSync = fileFolderDao.findByIds(additionalFileIdsToSync)

        additionalPublicFilesToSync.forEach {
                val worker = FileSyncWorker.createOneTimeWorkRequest(
                    courseId,
                    it.id,
                    it.displayName.orEmpty(),
                    it.url.orEmpty(),
                    syncSettingsFacade.getSyncSettings().wifiOnly
                )
                fileWorkers.add(worker)
                fileSyncData.add(FileSyncData(worker.id.toString(), it.displayName.orEmpty(), it.size))
            }

        val nonPublicFileIds = additionalFileIdsToSync.minus(additionalPublicFilesToSync.map { it.id }.toSet())
        nonPublicFileIds.forEach {
            val file = fileFolderApi.getCourseFile(courseId, it, RestParams(isForceReadFromNetwork = false)).dataOrNull
            if (file != null) {
                val worker = FileSyncWorker.createOneTimeWorkRequest(
                    courseId,
                    file.id,
                    file.displayName.orEmpty(),
                    file.url.orEmpty(),
                    syncSettingsFacade.getSyncSettings().wifiOnly
                )
                fileWorkers.add(worker)
                fileSyncData.add(FileSyncData(worker.id.toString(), file.displayName.orEmpty(), file.size))
            }
        }

        externalFilesToSync.forEach {
            val fileName = Uri.parse(it).lastPathSegment
            if (fileName != null) {
                val worker = FileSyncWorker.createOneTimeWorkRequest(
                    courseId,
                    -1,
                    fileName,
                    it,
                    syncSettingsFacade.getSyncSettings().wifiOnly
                )
                fileWorkers.add(worker)
                fileSyncData.add(FileSyncData(worker.id.toString(), fileName, -1))
            }
        }

        if (fileWorkers.isEmpty()) return

        val chunkedWorkers = fileWorkers.chunked(6)

        if (chunkedWorkers.isEmpty()) {
            progress = progress.copy(additionalFileSyncData = emptyList())
            updateProgress()
            return
        }

        var continuation = if (workContinuation != null) workContinuation.then(chunkedWorkers.first()) else workManager.beginWith(chunkedWorkers.first())

        chunkedWorkers.drop(1).forEach {
            continuation = continuation.then(it)
        }

        continuation.enqueue()

        progress = progress.copy(additionalFileSyncData = fileSyncData)
        updateProgress()
    }

    private suspend fun fetchFiles(courseId: Long) {
        val fileFolders = courseFileSharedRepository.getCourseFoldersAndFiles(courseId)

        val entities = fileFolders.map { FileFolderEntity(it) }
        fileFolderDao.replaceAll(entities, courseId)
    }

    private suspend fun cleanupSyncedFiles(courseId: Long, remoteIds: List<Long>) {
        val syncedIds = fileSyncSettingsDao.findByCourseId(courseId).map { it.id }
        val localRemovedFiles = localFileDao.findRemovedFiles(courseId, syncedIds)
        val remoteRemovedFiles = localFileDao.findRemovedFiles(courseId, remoteIds)

        (localRemovedFiles + remoteRemovedFiles).forEach {
            File(it.path).delete()
            localFileDao.delete(it)
        }

        val file = File(context.filesDir, "${ApiPrefs.user?.id.toString()}/external_$courseId")
        file.listFiles()?.forEach {
            it.delete()
        }

        fileSyncSettingsDao.deleteAllExcept(courseId, remoteIds)
    }

    private suspend fun getAllFiles(courseId: Long): List<FileFolderEntity> {
        return fileFolderDao.findAllFilesByCourseId(courseId)
    }

    private suspend fun updateProgress() {
        setProgress(workDataOf(COURSE_PROGRESS to progress.toJson()))
    }

    private fun initProgress(courseSettings: CourseSyncSettingsEntity, course: Course): CourseProgress {
        val availableTabs = course.tabs?.map { it.tabId } ?: emptyList()
        val selectedTabs = courseSettings.tabs.filter { availableTabs.contains(it.key) && it.value == true }.keys
        return CourseProgress(
            courseId = courseSettings.courseId,
            courseName = courseSettings.courseName,
            tabs = selectedTabs.associateWith { tabId ->
                TabSyncData(
                    course.tabs?.find { it.tabId == tabId }?.label ?: tabId,
                    ProgressState.IN_PROGRESS
                )
            },
            fileSyncData = null
        )
    }

    private suspend fun updateTabError(tabId: String) {
        progress = progress.copy(
            tabs = progress.tabs.toMutableMap().apply {
                val newProgress = get(tabId)?.copy(state = ProgressState.ERROR) ?: return@apply
                put(tabId, newProgress)
            },
        )
        updateProgress()
    }

    private suspend fun updateTabSuccess(tabId: String) {
        progress = progress.copy(
            tabs = progress.tabs.toMutableMap().apply {
                val newProgress = get(tabId)?.copy(state = ProgressState.COMPLETED) ?: return@apply
                put(tabId, newProgress)
            },
        )
        updateProgress()
    }

    companion object {
        const val COURSE_ID = "course_id"
        const val COURSE_PROGRESS = "courseProgress"
        const val OUTPUT = "output"
        const val TAG = "CourseSyncWorker"

        fun createOnTimeWork(courseId: Long, wifiOnly: Boolean): OneTimeWorkRequest {
            val data = workDataOf(COURSE_ID to courseId)
            return OneTimeWorkRequestBuilder<CourseSyncWorker>()
                .addTag(TAG)
                .setInputData(data)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        }
    }
}