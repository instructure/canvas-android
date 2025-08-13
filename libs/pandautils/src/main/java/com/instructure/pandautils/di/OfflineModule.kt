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

import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.AssignmentDao
import com.instructure.pandautils.room.offline.daos.AssignmentGroupDao
import com.instructure.pandautils.room.offline.daos.AssignmentOverrideDao
import com.instructure.pandautils.room.offline.daos.AssignmentRubricCriterionDao
import com.instructure.pandautils.room.offline.daos.AssignmentScoreStatisticsDao
import com.instructure.pandautils.room.offline.daos.AssignmentSetDao
import com.instructure.pandautils.room.offline.daos.AttachmentDao
import com.instructure.pandautils.room.offline.daos.AuthorDao
import com.instructure.pandautils.room.offline.daos.ConferenceDao
import com.instructure.pandautils.room.offline.daos.ConferenceRecodingDao
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseFeaturesDao
import com.instructure.pandautils.room.offline.daos.CourseGradingPeriodDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.CustomGradeStatusDao
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.daos.DiscussionEntryDao
import com.instructure.pandautils.room.offline.daos.DiscussionParticipantDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicHeaderDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicPermissionDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicRemoteFileDao
import com.instructure.pandautils.room.offline.daos.EditDashboardItemDao
import com.instructure.pandautils.room.offline.daos.EnrollmentDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.GradesDao
import com.instructure.pandautils.room.offline.daos.GradingPeriodDao
import com.instructure.pandautils.room.offline.daos.GroupDao
import com.instructure.pandautils.room.offline.daos.GroupUserDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.daos.LockInfoDao
import com.instructure.pandautils.room.offline.daos.LockedModuleDao
import com.instructure.pandautils.room.offline.daos.MasteryPathAssignmentDao
import com.instructure.pandautils.room.offline.daos.MasteryPathDao
import com.instructure.pandautils.room.offline.daos.MediaCommentDao
import com.instructure.pandautils.room.offline.daos.ModuleCompletionRequirementDao
import com.instructure.pandautils.room.offline.daos.ModuleContentDetailsDao
import com.instructure.pandautils.room.offline.daos.ModuleItemDao
import com.instructure.pandautils.room.offline.daos.ModuleNameDao
import com.instructure.pandautils.room.offline.daos.ModuleObjectDao
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.daos.PlannerOverrideDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.daos.RemoteFileDao
import com.instructure.pandautils.room.offline.daos.RubricCriterionAssessmentDao
import com.instructure.pandautils.room.offline.daos.RubricCriterionDao
import com.instructure.pandautils.room.offline.daos.RubricCriterionRatingDao
import com.instructure.pandautils.room.offline.daos.RubricSettingsDao
import com.instructure.pandautils.room.offline.daos.ScheduleItemAssignmentOverrideDao
import com.instructure.pandautils.room.offline.daos.ScheduleItemDao
import com.instructure.pandautils.room.offline.daos.SectionDao
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import com.instructure.pandautils.room.offline.daos.SubmissionCommentDao
import com.instructure.pandautils.room.offline.daos.SubmissionDao
import com.instructure.pandautils.room.offline.daos.SyncSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.daos.TermDao
import com.instructure.pandautils.room.offline.daos.UserCalendarDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.ConferenceFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicFacade
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import com.instructure.pandautils.room.offline.facade.LockInfoFacade
import com.instructure.pandautils.room.offline.facade.MasteryPathFacade
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.room.offline.facade.ScheduleItemFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import com.instructure.pandautils.room.offline.facade.UserFacade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class OfflineModule {

    @Provides
    fun provideOfflineDatabase(offlineDatabaseProvider: DatabaseProvider, apiPrefs: ApiPrefs): OfflineDatabase {
        val userId = if (apiPrefs.isMasquerading || apiPrefs.isMasqueradingFromQRCode) apiPrefs.masqueradeId else apiPrefs.user?.id
        return offlineDatabaseProvider.getDatabase(userId)
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
    fun provideModuleContentDetailsDao(offlineDatabase: OfflineDatabase): ModuleContentDetailsDao {
        return offlineDatabase.moduleContentDetailsDao()
    }

    @Provides
    fun provideMasteryPathDao(offlineDatabase: OfflineDatabase): MasteryPathDao {
        return offlineDatabase.masteryPathDao()
    }

    @Provides
    fun provideAssignmentSetDao(offlineDatabase: OfflineDatabase): AssignmentSetDao {
        return offlineDatabase.assignmentSetDao()
    }

    @Provides
    fun provideMasteryPathAssignmentDao(offlineDatabase: OfflineDatabase): MasteryPathAssignmentDao {
        return offlineDatabase.masteryPathAssignmentDao()
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
        lockInfoFacade: LockInfoFacade,
        rubricCriterionRatingDao: RubricCriterionRatingDao,
        assignmentRubricCriterionDao: AssignmentRubricCriterionDao,
        offlineDatabase: OfflineDatabase
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
            lockInfoFacade,
            rubricCriterionRatingDao,
            assignmentRubricCriterionDao,
            offlineDatabase
        )
    }

    @Provides
    fun provideSubmissionFacade(
        submissionDao: SubmissionDao,
        groupDao: GroupDao,
        mediaCommentDao: MediaCommentDao,
        userDao: UserDao,
        submissionCommentDao: SubmissionCommentDao,
        attachmentDao: AttachmentDao,
        authorDao: AuthorDao,
        rubricCriterionAssessmentDao: RubricCriterionAssessmentDao
    ): SubmissionFacade {
        return SubmissionFacade(
            submissionDao, groupDao, mediaCommentDao, userDao,
            submissionCommentDao, attachmentDao, authorDao, rubricCriterionAssessmentDao
        )
    }

    @Provides
    fun provideDiscussionTopicHeaderFacade(
        discussionTopicHeaderDao: DiscussionTopicHeaderDao,
        discussionParticipantDao: DiscussionParticipantDao,
        discussionTopicPermissionDao: DiscussionTopicPermissionDao,
        remoteFileDao: RemoteFileDao,
        localFileDao: LocalFileDao,
        discussionTopicRemoteFileDao: DiscussionTopicRemoteFileDao,
        offlineDatabase: OfflineDatabase,
    ): DiscussionTopicHeaderFacade {
        return DiscussionTopicHeaderFacade(
            discussionTopicHeaderDao,
            discussionParticipantDao,
            discussionTopicPermissionDao,
            remoteFileDao,
            localFileDao,
            discussionTopicRemoteFileDao,
            offlineDatabase
        )
    }

    @Provides
    fun provideCourseFacade(
        termDao: TermDao,
        courseDao: CourseDao,
        gradingPeriodDao: GradingPeriodDao,
        courseGradingPeriodDao: CourseGradingPeriodDao,
        sectionDao: SectionDao,
        tabDao: TabDao,
        enrollmentFacade: EnrollmentFacade,
        courseSettingsDao: CourseSettingsDao,
        apiPrefs: ApiPrefs
    ): CourseFacade {
        return CourseFacade(
            termDao,
            courseDao,
            gradingPeriodDao,
            courseGradingPeriodDao,
            sectionDao,
            tabDao,
            enrollmentFacade,
            courseSettingsDao,
            apiPrefs
        )
    }

    @Provides
    fun provideEnrollmentFacade(
        userDao: UserDao,
        enrollmentDao: EnrollmentDao,
        gradesDao: GradesDao,
    ): EnrollmentFacade {
        return EnrollmentFacade(userDao, enrollmentDao, gradesDao)
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
        scheduleItemAssignmentOverrideDao: ScheduleItemAssignmentOverrideDao,
        offlineDatabase: OfflineDatabase
    ): ScheduleItemFacade {
        return ScheduleItemFacade(scheduleItemDao, assignmentOverrideDao, scheduleItemAssignmentOverrideDao, assignmentDao, offlineDatabase)
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
        conferenceRecodingDao: ConferenceRecodingDao,
        offlineDatabase: OfflineDatabase
    ): ConferenceFacade {
        return ConferenceFacade(conferenceDao, conferenceRecodingDao, offlineDatabase)
    }

    @Provides
    fun providePeopleFacade(
        userDao: UserDao,
        enrollmentDao: EnrollmentDao,
        sectionDao: SectionDao,
        enrollmentFacade: EnrollmentFacade,
        offlineDatabase: OfflineDatabase
    ): UserFacade {
        return UserFacade(userDao, enrollmentDao, sectionDao, enrollmentFacade, offlineDatabase)
    }

    @Provides
    fun provideModuleFacade(
        moduleObjectDao: ModuleObjectDao,
        moduleItemDao: ModuleItemDao,
        completionRequirementDao: ModuleCompletionRequirementDao,
        moduleContentDetailsDao: ModuleContentDetailsDao,
        lockInfoFacade: LockInfoFacade,
        masteryPathFacade: MasteryPathFacade,
        offlineDatabase: OfflineDatabase
    ): ModuleFacade {
        return ModuleFacade(
            moduleObjectDao,
            moduleItemDao,
            completionRequirementDao,
            moduleContentDetailsDao,
            lockInfoFacade,
            masteryPathFacade,
            offlineDatabase
        )
    }

    @Provides
    fun provideMasteryPathFacade(
        masteryPathDao: MasteryPathDao,
        assignmentSetDao: AssignmentSetDao,
        masteryPathAssignmentDao: MasteryPathAssignmentDao,
        assignmentFacade: AssignmentFacade
    ): MasteryPathFacade {
        return MasteryPathFacade(masteryPathDao, masteryPathAssignmentDao, assignmentSetDao, assignmentFacade)
    }

    @Provides
    fun provideCourseFeaturesDao(appDatabase: OfflineDatabase): CourseFeaturesDao {
        return appDatabase.courseFeaturesDao()
    }

    @Provides
    fun provideAttachmentDao(offlineDatabase: OfflineDatabase): AttachmentDao {
        return offlineDatabase.attachmentDao()
    }

    @Provides
    fun provideAuthorDao(offlineDatabase: OfflineDatabase): AuthorDao {
        return offlineDatabase.authorDao()
    }

    @Provides
    fun provideMediaCommentDao(offlineDatabase: OfflineDatabase): MediaCommentDao {
        return offlineDatabase.mediaCommentDao()
    }

    @Provides
    fun provideSubmissionCommentDao(offlineDatabase: OfflineDatabase): SubmissionCommentDao {
        return offlineDatabase.submissionCommentDao()
    }

    @Provides
    fun provideRubricCriterionAssessmentDao(offlineDatabase: OfflineDatabase): RubricCriterionAssessmentDao {
        return offlineDatabase.rubricCriterionAssessmentDao()
    }

    @Provides
    fun provideRubricCriterionRatingDao(offlineDatabase: OfflineDatabase): RubricCriterionRatingDao {
        return offlineDatabase.rubricCriterionRatingDao()
    }

    @Provides
    fun provideAssignmentRubricCriterionDao(offlineDatabase: OfflineDatabase): AssignmentRubricCriterionDao {
        return offlineDatabase.assignmentRubricCriterionDao()
    }

    @Provides
    fun providePageFacade(pageDao: PageDao, lockInfoFacade: LockInfoFacade, offlineDatabase: OfflineDatabase): PageFacade {
        return PageFacade(pageDao, lockInfoFacade, offlineDatabase)
    }

    @Provides
    fun provideFileFolderDao(appDatabase: OfflineDatabase): FileFolderDao {
        return appDatabase.fileFolderDao()
    }

    @Provides
    fun provideLocalFileDao(appDatabase: OfflineDatabase): LocalFileDao {
        return appDatabase.localFileDao()
    }

    @Provides
    fun provideEditDashboardItemDao(appDatabase: OfflineDatabase): EditDashboardItemDao {
        return appDatabase.editDashboardItemDao()
    }

    @Provides
    fun provideCourseProgressDao(appDatabase: OfflineDatabase): CourseSyncProgressDao {
        return appDatabase.courseSyncProgressDao()
    }

    @Provides
    fun provideFileSyncProgressDao(appDatabase: OfflineDatabase): FileSyncProgressDao {
        return appDatabase.fileSyncProgressDao()
    }

    @Provides
    fun provideDiscussionEntryDao(appDatabase: OfflineDatabase): DiscussionEntryDao {
        return appDatabase.discussionEntryDao()
    }

    @Provides
    fun provideDiscussionTopicDao(appDatabase: OfflineDatabase): DiscussionTopicDao {
        return appDatabase.discussionTopicDao()
    }

    @Provides
    fun provideDiscussionTopicPermissionDao(appDatabase: OfflineDatabase): DiscussionTopicPermissionDao {
        return appDatabase.discussionTopicPermissionDao()
    }

    @Provides
    fun provideGroupUserDao(appDatabase: OfflineDatabase): GroupUserDao {
        return appDatabase.groupUserDao()
    }

    @Provides
    fun provideDiscussionTopicFacade(
        discussionEntryDao: DiscussionEntryDao,
        discussionParticipantDao: DiscussionParticipantDao,
        discussionTopicDao: DiscussionTopicDao,
    ): DiscussionTopicFacade {
        return DiscussionTopicFacade(discussionTopicDao, discussionParticipantDao, discussionEntryDao)
    }

    @Provides
    fun provideGroupFacade(
        groupUserDao: GroupUserDao,
        groupDao: GroupDao,
        userDao: UserDao,
    ): GroupFacade {
        return GroupFacade(groupUserDao, groupDao, userDao)
    }

    @Provides
    fun provideOfflineSyncHelper(
        workManager: WorkManager,
        syncSettingsFacade: SyncSettingsFacade,
        apiPrefs: ApiPrefs
    ): OfflineSyncHelper {
        return OfflineSyncHelper(workManager, syncSettingsFacade, apiPrefs)
    }

    @Provides
    fun provideRemoteFileDao(
        appDatabase: OfflineDatabase
    ): RemoteFileDao {
        return appDatabase.remoteFileDao()
    }

    @Provides
    fun provideDiscussionTopicRemoteFileDao(appDatabase: OfflineDatabase): DiscussionTopicRemoteFileDao {
        return appDatabase.discussionTopicRemoteFileDao()
    }

    @Provides
    fun provideStudioMediaProgressDao(database: OfflineDatabase): StudioMediaProgressDao {
        return database.studioMediaProgressDao()
    }

    @Provides
    fun provideCustomGradeStatusDao(database: OfflineDatabase): CustomGradeStatusDao {
        return database.customGradeStatusDao()
    }
}