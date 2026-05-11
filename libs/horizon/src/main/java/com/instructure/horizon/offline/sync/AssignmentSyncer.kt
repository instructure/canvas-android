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

import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCommentsManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.data.datasource.AssignmentCommentsLocalDataSource
import com.instructure.horizon.data.datasource.AssignmentDetailsLocalDataSource
import com.instructure.horizon.data.datasource.AssignmentDetailsNetworkDataSource
import com.instructure.horizon.data.datasource.SubmissionLocalDataSource
import com.instructure.horizon.data.repository.HorizonFileSyncRepository
import com.instructure.horizon.di.HorizonHtmlParserQualifier
import com.instructure.pandautils.features.offline.sync.HtmlParser
import javax.inject.Inject

class AssignmentSyncer @Inject constructor(
    private val networkDataSource: AssignmentDetailsNetworkDataSource,
    private val localDataSource: AssignmentDetailsLocalDataSource,
    private val submissionLocalDataSource: SubmissionLocalDataSource,
    private val commentsLocalDataSource: AssignmentCommentsLocalDataSource,
    private val commentsManager: HorizonGetCommentsManager,
    private val apiPrefs: ApiPrefs,
    private val imageSyncer: ImageSyncer,
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

                val submissionHistory = assignment.submission?.submissionHistory?.filterNotNull() ?: emptyList()
                if (submissionHistory.isNotEmpty()) {
                    submissionLocalDataSource.saveSubmissions(assignmentId, submissionHistory)
                    syncSubmissionAttachmentFiles(courseId, submissionHistory)
                    syncSubmissionPreviewImages(submissionHistory)
                }

                syncComments(assignmentId, submissionHistory)
            } catch (_: Exception) {
                // Skip individual assignment failures
            }
        }

        return ContentSyncResult(
            additionalFileIds = additionalFileIds,
            externalFileUrls = externalFileUrls,
        )
    }

    private suspend fun syncSubmissionAttachmentFiles(courseId: Long, submissions: List<com.instructure.canvasapi2.models.Submission>) {
        val attachments = submissions.flatMap { it.attachments }
        for (attachment in attachments) {
            val url = attachment.url ?: continue
            val displayName = attachment.displayName ?: "file_${attachment.id}"
            try {
                fileSyncRepository.downloadFileByUrl(attachment.id, courseId, url, displayName)
            } catch (_: Exception) {
                // Attachment download failure is non-fatal
            }
        }
    }

    private suspend fun syncSubmissionPreviewImages(submissions: List<com.instructure.canvasapi2.models.Submission>) {
        val imageUrls = submissions
            .flatMap { it.attachments }
            .mapNotNull { it.thumbnailUrl }
            .filter { it.isNotBlank() }
            .toSet()
        if (imageUrls.isNotEmpty()) {
            imageSyncer.syncImages(imageUrls)
        }
    }

    private suspend fun syncComments(assignmentId: Long, submissions: List<com.instructure.canvasapi2.models.Submission>) {
        val userId = apiPrefs.user?.id ?: return
        val attempts = submissions.map { it.attempt.toInt() }.distinct()
        for (attempt in attempts) {
            try {
                val commentsData = commentsManager.getComments(
                    assignmentId = assignmentId,
                    userId = userId,
                    attempt = attempt,
                    forceNetwork = true,
                )
                commentsLocalDataSource.saveComments(assignmentId, attempt, commentsData)
            } catch (_: Exception) {
                // Comment sync failure is non-fatal
            }
        }
    }
}
