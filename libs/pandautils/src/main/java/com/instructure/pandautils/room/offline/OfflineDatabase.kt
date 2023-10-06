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
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*

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
        SyncProgressEntity::class,
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
    ], version = 1
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

    abstract fun syncProgressDao(): SyncProgressDao

    abstract fun editDashboardItemDao(): EditDashboardItemDao

    abstract fun discussionEntryDao(): DiscussionEntryDao

    abstract fun discussionTopicDao(): DiscussionTopicDao

    abstract fun groupUserDao(): GroupUserDao

    abstract fun discussionTopicPermissionDao(): DiscussionTopicPermissionDao
}