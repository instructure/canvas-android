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

import com.google.gson.Gson
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.horizon.database.dao.PendingActionDao
import com.instructure.horizon.database.entity.PendingActionEntity

/**
 * Base class for syncers that replay a single category of pending offline actions.
 *
 * It owns the shared drain loop and result handling: reading the ready-to-drain rows for
 * [actionType], replaying each one via [replay], and deciding whether the row should be deleted,
 * retried, or marked exhausted based on the HTTP status of the result. Subclasses only need to
 * declare which action they handle and how to replay one action.
 *
 * See [OfflineActionSyncer] for the retry/poison-pill contract this implements.
 */
abstract class PendingActionSyncer(
    private val pendingActionDao: PendingActionDao,
) : OfflineActionSyncer {

    protected val gson = Gson()

    /** The [PendingActionEntity.actionType] this syncer is responsible for draining. */
    protected abstract val actionType: String

    /** Replays a single pending action against the network. */
    protected abstract suspend fun replay(action: PendingActionEntity): DataResult<*>

    final override suspend fun drain() {
        val actions = pendingActionDao.getReadyToDrain(MAX_RETRIES)
            .filter { it.actionType == actionType }

        var hadTransientFailure = false
        for (action in actions) {
            val result = replay(action)
            val code = result.httpStatusCode()
            when {
                // Success, or Canvas already reflects the desired state (idempotent).
                result.isSuccess || code == HTTP_CONFLICT -> pendingActionDao.delete(action.id)

                // Transient: retry later. 401 already went through the token refresh path.
                code == null || code == HTTP_UNAUTHORIZED || code >= HTTP_SERVER_ERROR -> {
                    pendingActionDao.updateRetry(action.id, action.retryCount + 1, result.errorMessage())
                    hadTransientFailure = true
                }

                // Permanent (non-recoverable 4xx): stop auto-draining but keep the row for the UI.
                else -> pendingActionDao.updateRetry(action.id, MAX_RETRIES, result.errorMessage())
            }
        }

        if (hadTransientFailure) throw OfflineActionRetryException(actionType)
    }

    private fun DataResult<*>.httpStatusCode(): Int? {
        val fail = this as? DataResult.Fail ?: return null
        return when (val failure = fail.failure) {
            is Failure.Authorization -> HTTP_UNAUTHORIZED
            is Failure.Network -> failure.errorCode ?: fail.response?.code
            else -> fail.response?.code
        }
    }

    private fun DataResult<*>.errorMessage(): String? {
        return (this as? DataResult.Fail)?.failure?.message
    }

    companion object {
        const val MAX_RETRIES = 5
        private const val HTTP_UNAUTHORIZED = 401
        private const val HTTP_CONFLICT = 409
        private const val HTTP_SERVER_ERROR = 500
    }
}

private class OfflineActionRetryException(actionType: String) :
    Exception("Offline action \"$actionType\" failed transiently and will be retried")
