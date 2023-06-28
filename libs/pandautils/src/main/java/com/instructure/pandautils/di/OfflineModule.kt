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
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.common.daos.MediaCommentDao
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.facade.*
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class OfflineModule {

    @Provides
    fun provideOfflineDatabase(offlineDatabaseProvider: DatabaseProvider, apiPrefs: ApiPrefs): OfflineDatabase {
        return offlineDatabaseProvider.getDatabase(apiPrefs.user?.id)
    }

    @Provides
    fun provideNetworkStateProvider(@ApplicationContext context: Context): NetworkStateProvider {
        return NetworkStateProvider(context)
    }

    @Provides
    fun provideCourseDao(appDatabase: OfflineDatabase): CourseDao {
        return appDatabase.courseDao()
    }

    @Provides
    fun provideEnrollmentDao(appDatabase: OfflineDatabase): EnrollmentDao {
        return appDatabase.enrollmentDao()
    }

    @Provides
    fun provideGradesDao(appDatabase: OfflineDatabase): GradesDao {
        return appDatabase.gradesDao()
    }

    @Provides
    fun provideGradingPeriodDao(appDatabase: OfflineDatabase): GradingPeriodDao {
        return appDatabase.gradingPeriodDao()
    }

    @Provides
    fun provideSectionDao(appDatabase: OfflineDatabase): SectionDao {
        return appDatabase.sectionDao()
    }

    @Provides
    fun provideTermDao(appDatabase: OfflineDatabase): TermDao {
        return appDatabase.termDao()
    }

    @Provides
    fun provideUserCalendarDao(appDatabase: OfflineDatabase): UserCalendarDao {
        return appDatabase.userCalendarDao()
    }

    @Provides
    fun provideUserDao(appDatabase: OfflineDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideCourseGradingPeriodDao(appDatabase: OfflineDatabase): CourseGradingPeriodDao {
        return appDatabase.courseGradingPeriodDao()
    }

    @Provides
    fun provideTabDao(appDatabase: OfflineDatabase): TabDao {
        return appDatabase.tabDao()
    }

    @Provides
    fun provideCourseSyncSettingsDao(appDatabase: OfflineDatabase): CourseSyncSettingsDao {
        return appDatabase.courseSyncSettingsDao()
    }

    @Provides
    fun providePageDao(appDatabase: OfflineDatabase): PageDao {
        return appDatabase.pageDao()
    }

    @Provides
    fun provideAssignmentGroupDao(appDatabase: OfflineDatabase): AssignmentGroupDao {
        return appDatabase.assignmentGroupDao()
    }

    @Provides
    fun provideAssignmentDao(appDatabase: OfflineDatabase): AssignmentDao {
        return appDatabase.assignmentDao()
    }

    @Provides
    fun provideRubricSettings(appDatabase: OfflineDatabase): RubricSettingsDao {
        return appDatabase.rubricSettingsDao()
    }

    @Provides
    fun provideSubmissionDao(appDatabase: OfflineDatabase): SubmissionDao {
        return appDatabase.submissionDao()
    }

    @Provides
    fun provideGroupDao(appDatabase: OfflineDatabase): GroupDao {
        return appDatabase.groupDao()
    }

    @Provides
    fun providePlannerOverrideDao(appDatabase: OfflineDatabase): PlannerOverrideDao {
        return appDatabase.plannerOverrideDao()
    }

    @Provides
    fun provideDiscussionTopicHeaderDao(appDatabase: OfflineDatabase): DiscussionTopicHeaderDao {
        return appDatabase.discussionTopicHeaderDao()
    }

    @Provides
    fun provideDiscussionParticipantDao(appDatabase: OfflineDatabase): DiscussionParticipantDao {
        return appDatabase.discussionParticipantDao()
    }

    @Provides
    fun provideAssignmentScoreStatisticsDao(appDatabase: OfflineDatabase): AssignmentScoreStatisticsDao {
        return appDatabase.assignmentScoreStatisticsDao()
    }

    @Provides
    fun provideRubricCriterionDao(appDatabase: OfflineDatabase): RubricCriterionDao {
        return appDatabase.rubricCriterionDao()
    }

    @Provides
    fun provideQuizDao(appDatabase: OfflineDatabase): QuizDao {
        return appDatabase.quizDao()
    }

    @Provides
    fun provideLockInfoDao(appDatabase: OfflineDatabase): LockInfoDao {
        return appDatabase.lockInfoDao()
    }

    @Provides
    fun provideLockedModuleDao(appDatabase: OfflineDatabase): LockedModuleDao {
        return appDatabase.lockedModuleDao()
    }

    @Provides
    fun provideModuleNameDao(appDatabase: OfflineDatabase): ModuleNameDao {
        return appDatabase.moduleNameDao()
    }

    @Provides
    fun provideModuleCompletionRequirementDao(appDatabase: OfflineDatabase): ModuleCompletionRequirementDao {
        return appDatabase.moduleCompletionRequirementDao()
    }

    @Provides
    fun provideDashboardCardDao(offlineDatabase: OfflineDatabase): DashboardCardDao {
        return offlineDatabase.dashboardCardDao()
    }

    @Provides
    fun provideCourseSettingsDao(offlineDatabase: OfflineDatabase): CourseSettingsDao {
        return offlineDatabase.courseSettingsDao()
    }

    @Provides
    fun provideScheduleItemDao(offlineDatabase: OfflineDatabase): ScheduleItemDao {
        return offlineDatabase.scheduleItemDao()
    }

    @Provides
    fun provideScheduleItemAssignmentOverrideDao(offlineDatabase: OfflineDatabase): ScheduleItemAssignmentOverrideDao {
        return offlineDatabase.scheduleItemAssignmentOverrideDao()
    }

    @Provides
    fun provideAssignmentOverrideDao(offlineDatabase: OfflineDatabase): AssignmentOverrideDao {
        return offlineDatabase.assignmentOverrideDao()
    }

    @Provides
    fun provideModuleObjectDao(offlineDatabase: OfflineDatabase): ModuleObjectDao {
        return offlineDatabase.moduleObjectDao()
    }

    @Provides
    fun provideModuleItemDao(offlineDatabase: OfflineDatabase): ModuleItemDao {
        return offlineDatabase.moduleItemDao()
    }

    @Provides
    fun provideAssignmentFacade(
        assignmentGroupDao: AssignmentGroupDao,
        assignmentDao: AssignmentDao,
        plannerOverrideDao: PlannerOverrideDao,
        rubricSettingsDao: RubricSettingsDao,
        submissionFacade: SubmissionFacade,
        discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
        assignmentScoreStatisticsDao: AssignmentScoreStatisticsDao,
        rubricCriterionDao: RubricCriterionDao,
        lockInfoFacade: LockInfoFacade
    ): AssignmentFacade {
        return AssignmentFacade(
            assignmentGroupDao,
            assignmentDao,
            plannerOverrideDao,
            rubricSettingsDao,
            submissionFacade,
            discussionTopicHeaderFacade,
            assignmentScoreStatisticsDao,
            rubricCriterionDao,
            lockInfoFacade
        )
    }

    @Provides
    fun provideSubmissionFacade(
        submissionDao: SubmissionDao,
        groupDao: GroupDao,
        mediaCommentDao: MediaCommentDao,
        userDao: UserDao,
        userApi: UserAPI.UsersInterface
    ): SubmissionFacade {
        return SubmissionFacade(submissionDao, groupDao, mediaCommentDao, userDao, userApi)
    }

    @Provides
    fun provideDiscussionTopicHeaderFacade(
        discussionTopicHeaderDao: DiscussionTopicHeaderDao,
        discussionParticipantDao: DiscussionParticipantDao
    ): DiscussionTopicHeaderFacade {
        return DiscussionTopicHeaderFacade(discussionTopicHeaderDao, discussionParticipantDao)
    }

    @Provides
    fun provideCourseFacade(
        termDao: TermDao,
        courseDao: CourseDao,
        gradingPeriodDao: GradingPeriodDao,
        courseGradingPeriodDao: CourseGradingPeriodDao,
        sectionDao: SectionDao,
        tabDao: TabDao,
        enrollmentFacade: EnrollmentFacade
    ): CourseFacade {
        return CourseFacade(
            termDao,
            courseDao,
            gradingPeriodDao,
            courseGradingPeriodDao,
            sectionDao,
            tabDao,
            enrollmentFacade
        )
    }

    @Provides
    fun provideEnrollmentFacade(
        userDao: UserDao,
        enrollmentDao: EnrollmentDao,
        gradesDao: GradesDao,
        userApi: UserAPI.UsersInterface
    ): EnrollmentFacade {
        return EnrollmentFacade(userDao, enrollmentDao, gradesDao, userApi)
    }

    @Provides
    fun provideSyncSettingsDao(appDatabase: OfflineDatabase): SyncSettingsDao {
        return appDatabase.syncSettingsDao()
    }

    @Provides
    fun provideSyncSettingsFacade(syncSettingsDao: SyncSettingsDao): SyncSettingsFacade {
        return SyncSettingsFacade(syncSettingsDao)
    }

    @Provides
    fun provideLockInfoFacade(
        lockInfoDao: LockInfoDao,
        lockedModuleDao: LockedModuleDao,
        moduleNameDao: ModuleNameDao,
        completionRequirementDao: ModuleCompletionRequirementDao
    ): LockInfoFacade {
        return LockInfoFacade(lockInfoDao, lockedModuleDao, moduleNameDao, completionRequirementDao)
    }

    @Provides
    fun provideFileSyncSettingsDao(appDatabase: OfflineDatabase): FileSyncSettingsDao {
        return appDatabase.fileSyncSettingsDao()
    }

    @Provides
    fun provideScheduleItemFacade(
        scheduleItemDao: ScheduleItemDao,
        assignmentDao: AssignmentDao,
        assignmentOverrideDao: AssignmentOverrideDao,
        scheduleItemAssignmentOverrideDao: ScheduleItemAssignmentOverrideDao
    ): ScheduleItemFacade {
        return ScheduleItemFacade(scheduleItemDao, assignmentOverrideDao, scheduleItemAssignmentOverrideDao, assignmentDao)
    }

    @Provides
    fun provideConferenceDao(appDatabase: OfflineDatabase): ConferenceDao {
        return appDatabase.conferenceDao()
    }

    @Provides
    fun provideConferenceRecodingDao(appDatabase: OfflineDatabase): ConferenceRecodingDao {
        return appDatabase.conferenceRecordingDao()
    }

    @Provides
    fun provideConferenceFacade(
        conferenceDao: ConferenceDao,
        conferenceRecodingDao: ConferenceRecodingDao
    ): ConferenceFacade {
        return ConferenceFacade(conferenceDao, conferenceRecodingDao)
    }

    @Provides
    fun provideModuleFacade(
        moduleObjectDao: ModuleObjectDao,
        moduleItemDao: ModuleItemDao
    ): ModuleFacade {
        return ModuleFacade(moduleObjectDao, moduleItemDao)
    }
}