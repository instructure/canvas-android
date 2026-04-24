/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.offline.sync

import com.instructure.horizon.data.datasource.CourseEnrollmentLocalDataSource
import com.instructure.horizon.data.datasource.CourseEnrollmentNetworkDataSource
import com.instructure.horizon.data.datasource.LearnLearningLibraryLocalDataSource
import com.instructure.horizon.data.datasource.LearnLearningLibraryNetworkDataSource
import com.instructure.horizon.data.datasource.LearnMyContentLocalDataSource
import com.instructure.horizon.data.datasource.LearnMyContentNetworkDataSource
import com.instructure.horizon.data.datasource.ProgramLocalDataSource
import com.instructure.horizon.data.datasource.ProgramNetworkDataSource
import javax.inject.Inject

class EnrollmentSyncer @Inject constructor(
    private val enrollmentNetworkDataSource: CourseEnrollmentNetworkDataSource,
    private val enrollmentLocalDataSource: CourseEnrollmentLocalDataSource,
    private val programNetworkDataSource: ProgramNetworkDataSource,
    private val programLocalDataSource: ProgramLocalDataSource,
    private val learnMyContentNetworkDataSource: LearnMyContentNetworkDataSource,
    private val learnMyContentLocalDataSource: LearnMyContentLocalDataSource,
    private val learnLibraryNetworkDataSource: LearnLearningLibraryNetworkDataSource,
    private val learnLibraryLocalDataSource: LearnLearningLibraryLocalDataSource,
) {
    suspend fun sync() {
        val enrollments = enrollmentNetworkDataSource.getEnrollments(forceRefresh = true)
        enrollmentLocalDataSource.saveEnrollments(enrollments)

        val enrolledCourseIds = enrollments.map { it.courseId }.toSet()

        val programs = programNetworkDataSource.getPrograms(forceRefresh = true)
        programLocalDataSource.savePrograms(programs, enrolledCourseIds)

        syncLearnContent()
        syncLearningLibrary()
    }

    private suspend fun syncLearnContent() {
        for (queryKey in listOf(
            LearnMyContentLocalDataSource.QUERY_KEY_IN_PROGRESS,
            LearnMyContentLocalDataSource.QUERY_KEY_COMPLETED,
        )) {
            val allItems = mutableListOf<com.instructure.canvasapi2.models.journey.mycontent.LearnItem>()
            var cursor: String? = null
            val status = when (queryKey) {
                LearnMyContentLocalDataSource.QUERY_KEY_IN_PROGRESS -> listOf(
                    com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus.IN_PROGRESS
                )
                else -> listOf(
                    com.instructure.canvasapi2.models.journey.mycontent.LearnItemStatus.COMPLETED
                )
            }
            do {
                val page = learnMyContentNetworkDataSource.getLearnItems(
                    cursor = cursor,
                    searchQuery = null,
                    sortBy = null,
                    status = status,
                    itemTypes = null,
                    forceRefresh = true,
                )
                allItems.addAll(page.items)
                cursor = if (page.pageInfo.hasNextPage) page.pageInfo.nextCursor else null
            } while (cursor != null)
            learnMyContentLocalDataSource.saveLearnItems(allItems, queryKey)
        }
    }

    private suspend fun syncLearningLibrary() {
        val collections = learnLibraryNetworkDataSource.getEnrolledLearningLibraries(
            limit = 100,
            forceRefresh = true,
        )
        learnLibraryLocalDataSource.saveEnrolledLearningLibraries(collections)

        val savedItems = learnLibraryNetworkDataSource.getLearningLibraryItems(
            cursor = null,
            limit = null,
            searchQuery = null,
            typeFilter = null,
            bookmarkedOnly = true,
            completedOnly = false,
            sortBy = null,
            forceRefresh = true,
        )
        learnLibraryLocalDataSource.saveSavedItems(savedItems.items)
    }
}
