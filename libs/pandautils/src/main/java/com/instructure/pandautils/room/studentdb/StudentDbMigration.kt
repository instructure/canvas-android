/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */package com.instructure.pandautils.room.studentdb

import com.instructure.pandautils.room.common.createMigration

val studentDbMigrations = arrayOf(
    createMigration(4, 5) { database ->
        // Add a new table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `CreatePendingSubmissionCommentEntity` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `accountDomain` TEXT NOT NULL,
                `canvasContext` TEXT NOT NULL,
                `assignmentName` TEXT NOT NULL,
                `assignmentId` INTEGER NOT NULL,
                `lastActivityDate` INTEGER NOT NULL,
                `isGroupMessage` INTEGER NOT NULL,
                `message` TEXT,
                `mediaPath` TEXT,
                `currentFile` INTEGER NOT NULL DEFAULT 0,
                `fileCount` INTEGER NOT NULL DEFAULT 0,
                `progress` REAL,
                `errorFlag` INTEGER NOT NULL DEFAULT 0,
                `attemptId` INTEGER
            )
            """
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `CreateSubmissionEntity` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `submissionEntry` TEXT,
                `lastActivityDate` INTEGER,
                `assignmentName` TEXT,
                `assignmentId` INTEGER NOT NULL,
                `canvasContext` TEXT NOT NULL,
                `submissionType` TEXT NOT NULL,
                `errorFlag` INTEGER NOT NULL DEFAULT 0,
                `assignmentGroupCategoryId` INTEGER,
                `userId` INTEGER NOT NULL,
                `currentFile` INTEGER NOT NULL DEFAULT 0,
                `fileCount` INTEGER NOT NULL DEFAULT 0,
                `progress` REAL,
                `annotatableAttachmentId` INTEGER,
                `isDraft` INTEGER NOT NULL DEFAULT 0
            )
            """
        )

        database.execSQL(
            """
               CREATE TABLE IF NOT EXISTS `CreateSubmissionCommentFileEntity` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `pendingCommentId` INTEGER NOT NULL,
                `attachmentId` INTEGER,
                `name` TEXT NOT NULL,
                `size` INTEGER NOT NULL,
                `contentType` TEXT NOT NULL,
                `fullPath` TEXT NOT NULL,
                 FOREIGN KEY(`pendingCommentId`) REFERENCES `CreatePendingSubmissionCommentEntity`(`id`) ON DELETE CASCADE
            )
            """
        )

        database.execSQL(
            """
               CREATE TABLE IF NOT EXISTS `CreateFileSubmissionEntity` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `dbSubmissionId` INTEGER NOT NULL,
                `attachmentId` INTEGER,
                `name` TEXT,
                `size` INTEGER,
                `contentType` TEXT,
                `fullPath` TEXT,
                `error` TEXT,
                `errorFlag` INTEGER NOT NULL DEFAULT 0,
                 FOREIGN KEY(`dbSubmissionId`) REFERENCES `CreateSubmissionEntity`(`id`) ON DELETE CASCADE
            )
            """
        )

        database.execSQL(
            """
        INSERT INTO `CreatePendingSubmissionCommentEntity` 
        SELECT * FROM `pendingSubmissionComment`
        """
        )

        database.execSQL(
            """
        INSERT INTO `CreateSubmissionEntity`
        SELECT * FROM `submission`
            """
        )

        database.execSQL(
            """
        INSERT INTO `CreateSubmissionCommentFileEntity`
        SELECT * FROM `submissionCommentFile`
            """
        )

        database.execSQL(
            """
        INSERT INTO `CreateFileSubmissionEntity`
        SELECT * FROM `fileSubmission`
            """
        )

        database.execSQL(
            """
        DROP TABLE `pendingSubmissionComment`
            """
        )

        database.execSQL(
            """
        DROP TABLE `submission`
            """
        )

        database.execSQL(
            """
        DROP TABLE `submissionCommentFile`
            """
        )

        database.execSQL(
            """
        DROP TABLE `fileSubmission`
            """
        )
    }
)