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

/**
 * Replays a category of pending offline actions once connectivity is available.
 *
 * Implementations own reading their pending actions, replaying them against the network, and
 * removing them once they succeed.
 *
 * Poison-pill protection is the implementation's responsibility: an action that fails must have its
 * per-action retry count bumped (see PendingActionDao.updateRetry / getReadyToDrain) and must be
 * dropped once it exceeds the retry limit. [drain] should throw only for failures worth retrying;
 * a permanently failing action must not keep throwing, otherwise the drain worker will retry
 * forever. When there is nothing left worth retrying, [drain] must return normally.
 */
interface OfflineActionSyncer {
    suspend fun drain()
}
