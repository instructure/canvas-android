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

package com.instructure.pandautils.di

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.StudioApi
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.offlinecontent.CourseFileSharedRepository
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.CourseSync
import com.instructure.pandautils.features.offline.sync.FileSync
import com.instructure.pandautils.features.offline.sync.HtmlParser
import com.instructure.pandautils.features.offline.sync.StudioSync
import com.instructure.pandautils.room.offline.daos.CourseFeaturesDao
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class OfflineSyncModule {

    @Provides
    fun provideAggregateProgressObserver(
        @ApplicationContext context: Context,
        courseSyncProgressDao: CourseSyncProgressDao,
        fileSyncProgressDao: FileSyncProgressDao,
        studioMediaProgressDao: StudioMediaProgressDao,
        firebaseCrashlytics: FirebaseCrashlytics
    ): AggregateProgressObserver {
        return AggregateProgressObserver(context, courseSyncProgressDao, fileSyncProgressDao, studioMediaProgressDao, firebaseCrashlytics)
    }

    @Provides
    fun provideFileSync(
        @ApplicationContext context: Context,
        fileDownloadApi: FileDownloadAPI,
        localFileDao: LocalFileDao,
        fileFolderDao: FileFolderDao,
        firebaseCrashlytics: FirebaseCrashlytics,
        fileSyncProgressDao: FileSyncProgressDao,
        fileSyncSettingsDao: FileSyncSettingsDao,
        courseSyncProgressDao: CourseSyncProgressDao,
        fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    ): FileSync {
        return FileSync(
            context,
            fileDownloadApi,
            localFileDao,
            fileFolderDao,
            firebaseCrashlytics,
            fileSyncProgressDao,
            fileSyncSettingsDao,
            courseSyncProgressDao,
            fileFolderApi
        )
    }

    @Provides
    fun provideCourseSync(
        courseApi: CourseAPI.CoursesInterface,
        pageApi: PageAPI.PagesInterface,
        userApi: UserAPI.UsersInterface,
        assignmentApi: AssignmentAPI.AssignmentInterface,
        calendarEventApi: CalendarEventAPI.CalendarEventInterface,
        courseSyncSettingsDao: CourseSyncSettingsDao,
        pageFacade: PageFacade,
        userFacade: UserFacade,
        courseFacade: CourseFacade,
        assignmentFacade: AssignmentFacade,
        quizDao: QuizDao,
        quizApi: QuizAPI.QuizInterface,
        scheduleItemFacade: ScheduleItemFacade,
        conferencesApi: ConferencesApi.ConferencesInterface,
        conferenceFacade: ConferenceFacade,
        discussionApi: DiscussionAPI.DiscussionInterface,
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
        announcementApi: AnnouncementAPI.AnnouncementInterface,
        moduleApi: ModuleAPI.ModuleInterface,
        moduleFacade: ModuleFacade,
        featuresApi: FeaturesAPI.FeaturesInterface,
        courseFeaturesDao: CourseFeaturesDao,
        courseFileSharedRepository: CourseFileSharedRepository,
        fileFolderDao: FileFolderDao,
        discussionTopicFacade: DiscussionTopicFacade,
        groupApi: GroupAPI.GroupInterface,
        groupFacade: GroupFacade,
        enrollmentsApi: EnrollmentAPI.EnrollmentInterface,
        courseSyncProgressDao: CourseSyncProgressDao,
        htmlParser: HtmlParser,
        fileFolderApi: FileFolderAPI.FilesFoldersInterface,
        pageDao: PageDao,
        firebaseCrashlytics: FirebaseCrashlytics,
        fileSync: FileSync
    ): CourseSync {
        return CourseSync(
            courseApi,
            pageApi,
            userApi,
            assignmentApi,
            calendarEventApi,
            courseSyncSettingsDao,
            pageFacade,
            userFacade,
            courseFacade,
            assignmentFacade,
            quizDao,
            quizApi,
            scheduleItemFacade,
            conferencesApi,
            conferenceFacade,
            discussionApi,
            discussionTopicHeaderFacade,
            announcementApi,
            moduleApi,
            moduleFacade,
            featuresApi,
            courseFeaturesDao,
            courseFileSharedRepository,
            fileFolderDao,
            discussionTopicFacade,
            groupApi,
            groupFacade,
            enrollmentsApi,
            courseSyncProgressDao,
            htmlParser,
            fileFolderApi,
            pageDao,
            firebaseCrashlytics,
            fileSync
        )
    }

    @Provides
    fun provideStudioSync(
        @ApplicationContext context: Context,
        launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface,
        apiPrefs: ApiPrefs,
        studioApi: StudioApi,
        studioMediaProgressDao: StudioMediaProgressDao,
        fileDownloadApi: FileDownloadAPI,
        firebaseCrashlytics: FirebaseCrashlytics
    ): StudioSync {
        return StudioSync(context, launchDefinitionsApi, apiPrefs, studioApi, studioMediaProgressDao, fileDownloadApi, firebaseCrashlytics)
    }
}