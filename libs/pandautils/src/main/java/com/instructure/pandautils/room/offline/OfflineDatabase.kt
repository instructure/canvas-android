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
import com.instructure.pandautils.room.Converters
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
        CourseEntity::class,
        CourseFilesEntity::class,
        CourseGradingPeriodEntity::class,
        CourseSyncSettingsEntity::class,
        DiscussionEntryAttachmentEntity::class,
        DiscussionEntryEntity::class,
        DiscussionParticipantEntity::class,
        DiscussionTopicHeaderEntity::class,
        DiscussionTopicPermissionEntity::class,
        DiscussionTopicRemoteFileEntity::class,
        DiscussionTopicSectionEntity::class,
        EnrollmentEntity::class,
        ExternalToolAttributesEntity::class,
        GradesEntity::class,
        GradingPeriodEntity::class,
        GradingRuleEntity::class,
        GroupEntity::class,
        GroupUserEntity::class,
        NeedsGradingCountEntity::class,
        PageEntity::class,
        PlannerOverrideEntity::class,
        RemoteFileEntity::class,
        RubricCriterionAssessmentEntity::class,
        RubricCriterionEntity::class,
        RubricCriterionRatingEntity::class,
        RubricSettingsEntity::class,
        SectionEntity::class,
        SubmissionDiscussionEntryEntity::class,
        SubmissionEntity::class,
        SubmissionHistoryEntity::class,
        TabEntity::class,
        TermEntity::class,
        UserCalendarEntity::class,
        UserEntity::class
    ], version = 2
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
}