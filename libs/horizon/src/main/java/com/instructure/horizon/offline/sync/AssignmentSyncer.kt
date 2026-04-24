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

import com.instructure.horizon.data.datasource.AssignmentDetailsLocalDataSource
import com.instructure.horizon.data.datasource.AssignmentDetailsNetworkDataSource
import com.instructure.horizon.data.repository.HorizonFileSyncRepository
import com.instructure.horizon.di.HorizonHtmlParserQualifier
import com.instructure.pandautils.features.offline.sync.HtmlParser
import javax.inject.Inject

class AssignmentSyncer @Inject constructor(
    private val networkDataSource: AssignmentDetailsNetworkDataSource,
    private val localDataSource: AssignmentDetailsLocalDataSource,
    @HorizonHtmlParserQualifier private val htmlParser: HtmlParser,
    private val fileSyncRepository: HorizonFileSyncRepository,
) {
    suspend fun syncAssignments(courseId: Long, assignmentIds: List<Long>): ContentSyncResult {
        val additionalFileIds = mutableSetOf<Long>()
        val externalFileUrls = mutableSetOf<String>()

        for (assignmentId in assignmentIds) {
            try {
                val assignment = networkDataSource.getAssignment(courseId, assignmentId, forceRefresh = true)
                val parsedDescription = assignment.description?.let {
                    htmlParser.createHtmlStringWithLocalFiles(it, courseId)
                }
                if (parsedDescription != null) {
                    additionalFileIds.addAll(parsedDescription.internalFileIds)
                    externalFileUrls.addAll(parsedDescription.externalFileUrls)
                    fileSyncRepository.syncHtmlFiles(courseId, parsedDescription)
                }
                localDataSource.saveAssignment(assignment, courseId, parsedDescription?.htmlWithLocalFileLinks)
            } catch (_: Exception) {
                // Skip individual assignment failures
            }
        }

        return ContentSyncResult(
            additionalFileIds = additionalFileIds,
            externalFileUrls = externalFileUrls,
        )
    }
}
