/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.dashboard

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface InboxCountUpdater {
    val shouldRefreshInboxCountFlow: SharedFlow<Boolean>
    val increaseInboxCountFlow: SharedFlow<Int>
    suspend fun updateShouldRefreshInboxCount(shouldRefresh: Boolean)
    suspend fun increaseInboxCount(increaseBy: Int)
}

class InboxCountUpdaterImpl : InboxCountUpdater {
    private val _shouldRefreshInboxCountFlow = MutableSharedFlow<Boolean>(replay = 1)
    override val shouldRefreshInboxCountFlow = _shouldRefreshInboxCountFlow.asSharedFlow()
    private val _increaseInboxCountFlow = MutableSharedFlow<Int>(replay = 1)
    override val increaseInboxCountFlow = _increaseInboxCountFlow.asSharedFlow()


    override suspend fun updateShouldRefreshInboxCount(shouldRefresh: Boolean) {
        _shouldRefreshInboxCountFlow.emit(shouldRefresh)
    }

    override suspend fun increaseInboxCount(increaseBy: Int) {
        _increaseInboxCountFlow.emit(increaseBy)
    }
}