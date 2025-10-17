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

import com.instructure.pandautils.room.common.createMigration

val offlineDatabaseMigrations = arrayOf(

    createMigration(1, 2) { database ->
        //CourseSyncProgressEntity
        database.execSQL("CREATE TABLE IF NOT EXISTS `CourseSyncProgressEntity_temp` (`courseId` INTEGER NOT NULL," +
                "`courseName` TEXT NOT NULL," +
                "`tabs` TEXT NOT NULL," +
                "`additionalFilesStarted` INTEGER NOT NULL," +
                "`progressState` TEXT NOT NULL, PRIMARY KEY(`courseId`))")
        database.execSQL(
            "INSERT INTO CourseSyncProgressEntity_temp (courseId, courseName, tabs, additionalFilesStarted, progressState) " +
                    "SELECT courseId, courseName, tabs, additionalFilesStarted, progressState FROM CourseSyncProgressEntity"
        )
        database.execSQL("DROP TABLE CourseSyncProgressEntity")
        database.execSQL("ALTER TABLE CourseSyncProgressEntity_temp RENAME TO CourseSyncProgressEntity")

        //FileSyncProgressEntity
        database.execSQL(
            "CREATE TABLE FileSyncProgressEntity_temp (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "fileId INTEGER NOT NULL," +
                    "courseId INTEGER NOT NULL," +
                    "fileName TEXT NOT NULL," +
                    "progress INTEGER NOT NULL," +
                    "fileSize INTEGER NOT NULL," +
                    "additionalFile INTEGER NOT NULL DEFAULT 0," +
                    "progressState TEXT NOT NULL," +
                    "FOREIGN KEY(courseId) REFERENCES CourseSyncProgressEntity(courseId) ON DELETE CASCADE" +
                    ")"
        )
        database.execSQL(
            "INSERT INTO FileSyncProgressEntity_temp (fileId, courseId, fileName, progress, fileSize, additionalFile, progressState) " +
                    "SELECT fileId, courseId, fileName, progress, fileSize, additionalFile, progressState FROM FileSyncProgressEntity"
        )
        database.execSQL("DROP TABLE FileSyncProgressEntity")
        database.execSQL("ALTER TABLE FileSyncProgressEntity_temp RENAME TO FileSyncProgressEntity")

        //LockInfoEntity
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `LockInfoEntity_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`modulePrerequisiteNames` TEXT," +
                    "`unlockAt` TEXT," +
                    "`lockedModuleId` INTEGER," +
                    "`assignmentId` INTEGER," +
                    "`moduleId` INTEGER," +
                    "`pageId` INTEGER," +
                    "FOREIGN KEY(`moduleId`) REFERENCES `ModuleContentDetailsEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED," +
                    "FOREIGN KEY(`assignmentId`) REFERENCES `AssignmentEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED," +
                    "FOREIGN KEY(`pageId`) REFERENCES `PageEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED)"
        )
        database.execSQL(
            "INSERT INTO LockInfoEntity_temp (id, modulePrerequisiteNames, unlockAt, lockedModuleId, assignmentId, moduleId, pageId) " +
                    "SELECT id, modulePrerequisiteNames, unlockAt, lockedModuleId, assignmentId, moduleId, pageId FROM LockInfoEntity"
        )
        database.execSQL("DROP TABLE LockInfoEntity")
        database.execSQL("ALTER TABLE LockInfoEntity_temp RENAME TO LockInfoEntity")

        //LockedModuleEntity
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `LockedModuleEntity_temp` (" +
                    "`id` INTEGER NOT NULL," +
                    "`contextId` INTEGER NOT NULL," +
                    "`contextType` TEXT," +
                    "`name` TEXT," +
                    "`unlockAt` TEXT," +
                    "`isRequireSequentialProgress` INTEGER NOT NULL," +
                    "`lockInfoId` INTEGER, PRIMARY KEY(`id`)," +
                    "FOREIGN KEY(`lockInfoId`) REFERENCES `LockInfoEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        database.execSQL(
            "INSERT INTO LockedModuleEntity_temp (id, contextId, contextType, name, unlockAt, isRequireSequentialProgress) " +
                    "SELECT id, contextId, contextType, name, unlockAt, isRequireSequentialProgress FROM LockedModuleEntity"
        )
        database.execSQL("DROP TABLE LockedModuleEntity")
        database.execSQL("ALTER TABLE LockedModuleEntity_temp RENAME TO LockedModuleEntity")
    },
    createMigration(2, 3) { database ->
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `StudioMediaProgressEntity` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`ltiLaunchId` TEXT NOT NULL," +
                    "`progress` INTEGER NOT NULL," +
                    "`fileSize` INTEGER NOT NULL," +
                    "`progressState` TEXT NOT NULL)"
        )
    },
    createMigration(3, 4) { database ->
        database.execSQL("ALTER TABLE CourseEntity ADD COLUMN pointsBasedGradingScheme INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE CourseEntity ADD COLUMN scalingFactor REAL NOT NULL DEFAULT 1.0")
    },
    createMigration(4, 5) { database ->
        database.execSQL("ALTER TABLE `SubmissionEntity` ADD COLUMN `customGradeStatusId` INTEGER")
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `CustomGradeStatusEntity` (" +
                    "`id` TEXT NOT NULL," +
                    "`name` TEXT NOT NULL," +
                    "`courseId` INTEGER NOT NULL," +
                    "PRIMARY KEY(`id`, `courseId`)," +
                    "FOREIGN KEY(`courseId`) REFERENCES `CourseEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
    },
    createMigration(5, 6) { database ->
        database.execSQL("ALTER TABLE `SubmissionEntity` ADD COLUMN `hasSubAssignmentSubmissions` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `DiscussionTopicHeaderEntity` ADD COLUMN `replyRequiredCount` INTEGER")
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `CheckpointEntity` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`assignmentId` INTEGER NOT NULL," +
                    "`name` TEXT," +
                    "`tag` TEXT," +
                    "`pointsPossible` REAL," +
                    "`dueAt` TEXT," +
                    "`onlyVisibleToOverrides` INTEGER NOT NULL," +
                    "`lockAt` TEXT," +
                    "`unlockAt` TEXT," +
                    "FOREIGN KEY(`assignmentId`) REFERENCES `AssignmentEntity`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `SubAssignmentSubmissionEntity` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "`submissionId` INTEGER NOT NULL," +
                    "`submissionAttempt` INTEGER NOT NULL," +
                    "`grade` TEXT," +
                    "`score` REAL NOT NULL," +
                    "`late` INTEGER NOT NULL," +
                    "`excused` INTEGER NOT NULL," +
                    "`missing` INTEGER NOT NULL," +
                    "`latePolicyStatus` TEXT," +
                    "`customGradeStatusId` INTEGER," +
                    "`subAssignmentTag` TEXT," +
                    "`enteredScore` REAL NOT NULL," +
                    "`enteredGrade` TEXT," +
                    "`userId` INTEGER NOT NULL," +
                    "`isGradeMatchesCurrentSubmission` INTEGER NOT NULL," +
                    "FOREIGN KEY(`submissionId`, `submissionAttempt`) REFERENCES `SubmissionEntity`(`id`, `attempt`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
    },
    createMigration(6, 7) { database ->
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `PlannerItemEntity` (" +
                    "`id` INTEGER PRIMARY KEY NOT NULL," +
                    "`courseId` INTEGER," +
                    "`groupId` INTEGER," +
                    "`userId` INTEGER," +
                    "`contextType` TEXT," +
                    "`contextName` TEXT," +
                    "`plannableType` TEXT NOT NULL," +
                    "`plannableId` INTEGER NOT NULL," +
                    "`plannableTitle` TEXT," +
                    "`plannableDetails` TEXT," +
                    "`plannableTodoDate` TEXT," +
                    "`plannableEndAt` INTEGER," +
                    "`plannableAllDay` INTEGER," +
                    "`plannableCourseId` INTEGER," +
                    "`plannableGroupId` INTEGER," +
                    "`plannableUserId` INTEGER," +
                    "`plannableDate` INTEGER NOT NULL," +
                    "`htmlUrl` TEXT," +
                    "`submissionStateSubmitted` INTEGER," +
                    "`submissionStateExcused` INTEGER," +
                    "`submissionStateGraded` INTEGER," +
                    "`newActivity` INTEGER," +
                    "`plannerOverrideId` INTEGER," +
                    "`plannerOverrideMarkedComplete` INTEGER," +
                    "FOREIGN KEY(`courseId`) REFERENCES `CourseEntity`(`id`) ON DELETE CASCADE)"
        )
    },
    createMigration(7, 8) { database ->
        database.execSQL("ALTER TABLE `ScheduleItemEntity` ADD COLUMN `subAssignmentId` INTEGER")
    }
)