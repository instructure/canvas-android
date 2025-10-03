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

package com.instructure.pandautils.room.offline

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.common.Converters
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
import com.instructure.pandautils.room.offline.daos.PlannerItemDao
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
import com.instructure.pandautils.room.offline.entities.AssignmentDueDateEntity
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.AssignmentOverrideEntity
import com.instructure.pandautils.room.offline.entities.AssignmentRubricCriterionEntity
import com.instructure.pandautils.room.offline.entities.AssignmentScoreStatisticsEntity
import com.instructure.pandautils.room.offline.entities.AssignmentSetEntity
import com.instructure.pandautils.room.offline.entities.AttachmentEntity
import com.instructure.pandautils.room.offline.entities.AuthorEntity
import com.instructure.pandautils.room.offline.entities.ConferenceEntity
import com.instructure.pandautils.room.offline.entities.ConferenceRecordingEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.CourseFeaturesEntity
import com.instructure.pandautils.room.offline.entities.CourseFilesEntity
import com.instructure.pandautils.room.offline.entities.CourseGradingPeriodEntity
import com.instructure.pandautils.room.offline.entities.CourseSettingsEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.CustomGradeStatusEntity
import com.instructure.pandautils.room.offline.entities.DashboardCardEntity
import com.instructure.pandautils.room.offline.entities.DiscussionEntryAttachmentEntity
import com.instructure.pandautils.room.offline.entities.DiscussionEntryEntity
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicPermissionEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicRemoteFileEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicSectionEntity
import com.instructure.pandautils.room.offline.entities.EditDashboardItemEntity
import com.instructure.pandautils.room.offline.entities.EnrollmentEntity
import com.instructure.pandautils.room.offline.entities.ExternalToolAttributesEntity
import com.instructure.pandautils.room.offline.entities.FileFolderEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.GradesEntity
import com.instructure.pandautils.room.offline.entities.GradingPeriodEntity
import com.instructure.pandautils.room.offline.entities.GroupEntity
import com.instructure.pandautils.room.offline.entities.GroupUserEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.pandautils.room.offline.entities.LockInfoEntity
import com.instructure.pandautils.room.offline.entities.LockedModuleEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathAssignmentEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathEntity
import com.instructure.pandautils.room.offline.entities.MediaCommentEntity
import com.instructure.pandautils.room.offline.entities.ModuleCompletionRequirementEntity
import com.instructure.pandautils.room.offline.entities.ModuleContentDetailsEntity
import com.instructure.pandautils.room.offline.entities.ModuleItemEntity
import com.instructure.pandautils.room.offline.entities.ModuleNameEntity
import com.instructure.pandautils.room.offline.entities.ModuleObjectEntity
import com.instructure.pandautils.room.offline.entities.NeedsGradingCountEntity
import com.instructure.pandautils.room.offline.entities.PageEntity
import com.instructure.pandautils.room.offline.entities.PlannerItemEntity
import com.instructure.pandautils.room.offline.entities.PlannerOverrideEntity
import com.instructure.pandautils.room.offline.entities.QuizEntity
import com.instructure.pandautils.room.offline.entities.RemoteFileEntity
import com.instructure.pandautils.room.offline.entities.RubricCriterionAssessmentEntity
import com.instructure.pandautils.room.offline.entities.RubricCriterionEntity
import com.instructure.pandautils.room.offline.entities.RubricCriterionRatingEntity
import com.instructure.pandautils.room.offline.entities.RubricSettingsEntity
import com.instructure.pandautils.room.offline.entities.ScheduleItemAssignmentOverrideEntity
import com.instructure.pandautils.room.offline.entities.ScheduleItemEntity
import com.instructure.pandautils.room.offline.entities.SectionEntity
import com.instructure.pandautils.room.offline.entities.StudioMediaProgressEntity
import com.instructure.pandautils.room.offline.entities.SubmissionCommentEntity
import com.instructure.pandautils.room.offline.entities.SubmissionDiscussionEntryEntity
import com.instructure.pandautils.room.offline.entities.SubmissionEntity
import com.instructure.pandautils.room.offline.entities.SyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.TabEntity
import com.instructure.pandautils.room.offline.entities.TermEntity
import com.instructure.pandautils.room.offline.entities.UserCalendarEntity
import com.instructure.pandautils.room.offline.entities.UserEntity

@Database(
    entities = [
        AssignmentDueDateEntity::class,
        AssignmentEntity::class,
        AssignmentGroupEntity::class,
        AssignmentOverrideEntity::class,
        AssignmentRubricCriterionEntity::class,
        AssignmentScoreStatisticsEntity::class,
        AssignmentSetEntity::class,
        CourseEntity::class,
        CourseFilesEntity::class,
        CourseGradingPeriodEntity::class,
        CourseSettingsEntity::class,
        CourseSyncSettingsEntity::class,
        DashboardCardEntity::class,
        DiscussionEntryAttachmentEntity::class,
        DiscussionEntryEntity::class,
        DiscussionParticipantEntity::class,
        DiscussionTopicHeaderEntity::class,
        DiscussionTopicPermissionEntity::class,
        DiscussionTopicRemoteFileEntity::class,
        DiscussionTopicSectionEntity::class,
        EnrollmentEntity::class,
        FileFolderEntity::class,
        EditDashboardItemEntity::class,
        ExternalToolAttributesEntity::class,
        GradesEntity::class,
        GradingPeriodEntity::class,
        GroupEntity::class,
        GroupUserEntity::class,
        LocalFileEntity::class,
        MasteryPathAssignmentEntity::class,
        MasteryPathEntity::class,
        ModuleContentDetailsEntity::class,
        ModuleItemEntity::class,
        ModuleObjectEntity::class,
        NeedsGradingCountEntity::class,
        PageEntity::class,
        PlannerItemEntity::class,
        PlannerOverrideEntity::class,
        RemoteFileEntity::class,
        RubricCriterionAssessmentEntity::class,
        RubricCriterionEntity::class,
        RubricCriterionRatingEntity::class,
        RubricSettingsEntity::class,
        ScheduleItemAssignmentOverrideEntity::class,
        ScheduleItemEntity::class,
        SectionEntity::class,
        SubmissionDiscussionEntryEntity::class,
        SubmissionEntity::class,
        SyncSettingsEntity::class,
        TabEntity::class,
        TermEntity::class,
        UserCalendarEntity::class,
        UserEntity::class,
        QuizEntity::class,
        LockInfoEntity::class,
        LockedModuleEntity::class,
        ModuleNameEntity::class,
        ModuleCompletionRequirementEntity::class,
        FileSyncSettingsEntity::class,
        ConferenceEntity::class,
        ConferenceRecordingEntity::class,
        CourseFeaturesEntity::class,
        AttachmentEntity::class,
        MediaCommentEntity::class,
        AuthorEntity::class,
        SubmissionCommentEntity::class,
        DiscussionTopicEntity::class,
        CourseSyncProgressEntity::class,
        FileSyncProgressEntity::class,
        StudioMediaProgressEntity::class,
        CustomGradeStatusEntity::class
    ], version = 6
)
@TypeConverters(value = [Converters::class, OfflineConverters::class])
abstract class OfflineDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    abstract fun enrollmentDao(): EnrollmentDao

    abstract fun gradesDao(): GradesDao

    abstract fun gradingPeriodDao(): GradingPeriodDao

    abstract fun sectionDao(): SectionDao

    abstract fun termDao(): TermDao

    abstract fun userCalendarDao(): UserCalendarDao

    abstract fun userDao(): UserDao

    abstract fun courseGradingPeriodDao(): CourseGradingPeriodDao

    abstract fun tabDao(): TabDao

    abstract fun courseSyncSettingsDao(): CourseSyncSettingsDao

    abstract fun pageDao(): PageDao

    abstract fun assignmentGroupDao(): AssignmentGroupDao

    abstract fun assignmentDao(): AssignmentDao

    abstract fun rubricSettingsDao(): RubricSettingsDao

    abstract fun submissionDao(): SubmissionDao

    abstract fun groupDao(): GroupDao

    abstract fun plannerItemDao(): PlannerItemDao

    abstract fun plannerOverrideDao(): PlannerOverrideDao

    abstract fun discussionTopicHeaderDao(): DiscussionTopicHeaderDao

    abstract fun discussionParticipantDao(): DiscussionParticipantDao

    abstract fun syncSettingsDao(): SyncSettingsDao

    abstract fun assignmentScoreStatisticsDao(): AssignmentScoreStatisticsDao

    abstract fun rubricCriterionDao(): RubricCriterionDao

    abstract fun quizDao(): QuizDao

    abstract fun lockInfoDao(): LockInfoDao

    abstract fun lockedModuleDao(): LockedModuleDao

    abstract fun moduleNameDao(): ModuleNameDao

    abstract fun moduleCompletionRequirementDao(): ModuleCompletionRequirementDao

    abstract fun dashboardCardDao(): DashboardCardDao

    abstract fun fileSyncSettingsDao(): FileSyncSettingsDao

    abstract fun courseSettingsDao(): CourseSettingsDao

    abstract fun scheduleItemDao(): ScheduleItemDao

    abstract fun scheduleItemAssignmentOverrideDao(): ScheduleItemAssignmentOverrideDao

    abstract fun assignmentOverrideDao(): AssignmentOverrideDao

    abstract fun conferenceDao(): ConferenceDao

    abstract fun conferenceRecordingDao(): ConferenceRecodingDao

    abstract fun moduleObjectDao(): ModuleObjectDao

    abstract fun moduleItemDao(): ModuleItemDao

    abstract fun moduleContentDetailsDao(): ModuleContentDetailsDao

    abstract fun masteryPathDao(): MasteryPathDao

    abstract fun assignmentSetDao(): AssignmentSetDao

    abstract fun masteryPathAssignmentDao(): MasteryPathAssignmentDao

    abstract fun courseFeaturesDao(): CourseFeaturesDao

    abstract fun attachmentDao(): AttachmentDao

    abstract fun authorDao(): AuthorDao

    abstract fun mediaCommentDao(): MediaCommentDao

    abstract fun submissionCommentDao(): SubmissionCommentDao

    abstract fun rubricCriterionAssessmentDao(): RubricCriterionAssessmentDao

    abstract fun rubricCriterionRatingDao(): RubricCriterionRatingDao

    abstract fun assignmentRubricCriterionDao(): AssignmentRubricCriterionDao

    abstract fun fileFolderDao(): FileFolderDao

    abstract fun localFileDao(): LocalFileDao

    abstract fun editDashboardItemDao(): EditDashboardItemDao

    abstract fun courseSyncProgressDao(): CourseSyncProgressDao

    abstract fun fileSyncProgressDao(): FileSyncProgressDao

    abstract fun groupUserDao(): GroupUserDao

    abstract fun discussionTopicDao(): DiscussionTopicDao

    abstract fun discussionEntryDao(): DiscussionEntryDao

    abstract fun discussionTopicPermissionDao(): DiscussionTopicPermissionDao

    abstract fun remoteFileDao(): RemoteFileDao

    abstract fun discussionTopicRemoteFileDao(): DiscussionTopicRemoteFileDao

    abstract fun studioMediaProgressDao(): StudioMediaProgressDao

    abstract fun customGradeStatusDao(): CustomGradeStatusDao
}