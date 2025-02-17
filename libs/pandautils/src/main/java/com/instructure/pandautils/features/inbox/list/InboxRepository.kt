/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.list

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Progress
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.depaginate
import kotlinx.coroutines.delay

private const val POLLING_TIMEOUT = 5000L
private const val POLLING_INTERVAL = 500L

abstract class InboxRepository(
    private val inboxApi: InboxApi.InboxInterface,
    private val groupsApi: GroupAPI.GroupInterface,
    private val progressApi: ProgressAPI.ProgressInterface,
    private val inboxSettingsManager: InboxSettingsManager
) {

    suspend fun getConversations(scope: InboxApi.Scope, forceNetwork: Boolean, filter: CanvasContext?, nextPageLink: String? = null): DataResult<List<Conversation>> {
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        return if (nextPageLink == null) {
            if (filter == null) {
                getConversationsAllCourses(scope, params)
            } else {
                getConversationsFiltered(scope, filter.contextId, params)
            }
        } else {
            inboxApi.getNextPage(nextPageLink, params)
        }
    }

    private suspend fun getConversationsAllCourses(scope: InboxApi.Scope, params: RestParams): DataResult<List<Conversation>> {
        return inboxApi.getConversations(InboxApi.conversationScopeToString(scope), params)
    }

    private suspend fun getConversationsFiltered(scope: InboxApi.Scope, contextId: String, params: RestParams): DataResult<List<Conversation>> {
        return inboxApi.getConversationsFiltered(InboxApi.conversationScopeToString(scope), contextId, params)
    }

    suspend fun getCanvasContexts(): DataResult<List<CanvasContext>> {
        val params = RestParams(usePerPageQueryParam = true)

        val coursesResult = getCourses(params)

        if (coursesResult.isFail) return coursesResult

        val groupsResult = groupsApi.getFirstPageGroups(params)
            .depaginate { nextUrl -> groupsApi.getNextPageGroups(nextUrl, params) }

        val courses = (coursesResult as DataResult.Success).data
        val groups = groupsResult.dataOrNull ?: emptyList()

        val courseMap = courses.associateBy { it.id }
        val validGroups = groups.filter { it.courseId == 0L || courseMap[it.courseId] != null }

        return DataResult.Success(courses + validGroups)
    }

    protected abstract suspend fun getCourses(params: RestParams): DataResult<List<Course>>

    suspend fun batchUpdateConversations(conversationIds: List<Long>, conversationEvent: String): DataResult<Progress> {
        return inboxApi.batchUpdateConversations(conversationIds, conversationEvent)
    }

    fun invalidateCachedResponses() {
        CanvasRestAdapter.clearCacheUrls("conversations")
    }

    suspend fun pollProgress(progress: Progress): DataResult<Progress> {
        val params = RestParams(isForceReadFromNetwork = true)
        var currentProgress = progress
        var pollingTime = 0L

        while (!currentProgress.hasRun && pollingTime < POLLING_TIMEOUT) {
            delay(POLLING_INTERVAL)
            pollingTime += POLLING_INTERVAL

            val newProgress = progressApi.getProgress(progress.id.toString(), params)
            if (newProgress.isSuccess) {
                currentProgress = (newProgress as DataResult.Success).data
            } else {
                return newProgress
            }
        }
        return if (pollingTime < POLLING_TIMEOUT) {
            DataResult.Success(currentProgress)
        } else {
            DataResult.Fail(Failure.Network("Progress timed out"))
        }
    }

    suspend fun updateConversation(id: Long, workflowState: Conversation.WorkflowState? = null, starred: Boolean? = null): DataResult<Conversation> {
        return inboxApi.updateConversation(id, workflowState?.apiString, starred, RestParams(isForceReadFromNetwork = true))
    }

    suspend fun getInboxSignature(): String {
        return inboxSettingsManager.getInboxSignature()
    }
}