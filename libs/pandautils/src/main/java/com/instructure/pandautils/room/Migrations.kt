/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PendingSubmissionCommentEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE FileUploadInputEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE SubmissionCommentEntity ADD COLUMN attemptId INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS DashboardFileUploadEntity (workerId TEXT NOT NULL, title TEXT, assignmentName TEXT, PRIMARY KEY(workerId))")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `CourseEntity` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `originalName` TEXT, `courseCode` TEXT, `startAt` TEXT, `endAt` TEXT, `syllabusBody` TEXT, `hideFinalGrades` INTEGER NOT NULL, `isPublic` INTEGER NOT NULL, `license` TEXT NOT NULL, `termId` INTEGER, `needsGradingCount` INTEGER NOT NULL, `isApplyAssignmentGroupWeights` INTEGER NOT NULL, `currentScore` REAL, `finalScore` REAL, `currentGrade` TEXT, `finalGrade` TEXT, `isFavorite` INTEGER NOT NULL, `accessRestrictedByDate` INTEGER NOT NULL, `imageUrl` TEXT, `bannerImageUrl` TEXT, `isWeightedGradingPeriods` INTEGER NOT NULL, `hasGradingPeriods` INTEGER NOT NULL, `homePage` TEXT, `restrictEnrollmentsToCourseDate` INTEGER NOT NULL, `workflowState` TEXT, `homeroomCourse` INTEGER NOT NULL, `courseColor` TEXT, PRIMARY KEY(`id`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `EnrollmentEntity` (`id` INTEGER NOT NULL, `role` TEXT NOT NULL, `type` TEXT NOT NULL, `courseId` INTEGER NOT NULL, `courseSectionId` INTEGER NOT NULL, `enrollmentState` TEXT, `userId` INTEGER NOT NULL, `computedCurrentScore` REAL, `computedFinalScore` REAL, `computedCurrentGrade` TEXT, `computedFinalGrade` TEXT, `multipleGradingPeriodsEnabled` INTEGER NOT NULL, `totalsForAllGradingPeriodsOption` INTEGER NOT NULL, `currentPeriodComputedCurrentScore` REAL, `currentPeriodComputedFinalScore` REAL, `currentPeriodComputedCurrentGrade` TEXT, `currentPeriodComputedFinalGrade` TEXT, `currentGradingPeriodId` INTEGER NOT NULL, `currentGradingPeriodTitle` TEXT, `associatedUserId` INTEGER NOT NULL, `lastActivityAt` INTEGER, `limitPrivilegesToCourseSection` INTEGER NOT NULL, `observedUserId` INTEGER, PRIMARY KEY(`id`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `GradesEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `enrollmentId` INTEGER NOT NULL, `htmlUrl` TEXT, `currentScore` REAL, `finalScore` REAL, `currentGrade` TEXT, `finalGrade` TEXT)")
        database.execSQL("CREATE TABLE IF NOT EXISTS `GradingPeriodEntity` (`id` INTEGER NOT NULL, `title` TEXT, `startDate` TEXT, `endDate` TEXT, `weight` REAL NOT NULL, `courseId` INTEGER, PRIMARY KEY(`id`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `SectionEntity` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `courseId` INTEGER NOT NULL, `startAt` TEXT, `endAt` TEXT, `totalStudents` INTEGER NOT NULL, `restrictEnrollmentsToSectionDates` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `TermEntity` (`id` INTEGER NOT NULL, `name` TEXT, `startAt` TEXT, `endAt` TEXT, `isGroupTerm` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `UserCalendarEntity` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `ics` TEXT NOT NULL)")
        database.execSQL("CREATE TABLE IF NOT EXISTS `UserEntity` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `shortName` TEXT, `loginId` TEXT, `avatarUrl` TEXT, `primaryEmail` TEXT, `email` TEXT, `sortableName` TEXT, `bio` TEXT, `enrollmentIndex` INTEGER NOT NULL, `lastLogin` TEXT, `locale` TEXT, `effective_locale` TEXT, `pronouns` TEXT, `k5User` INTEGER NOT NULL, `rootAccount` TEXT, `isFakeStudent` INTEGER NOT NULL, `calendarId` INTEGER, `sectionId` INTEGER, PRIMARY KEY(`id`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
    }
}
