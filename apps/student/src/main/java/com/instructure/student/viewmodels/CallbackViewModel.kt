/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */package com.instructure.student.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallbackViewModel @Inject constructor(
    private val unreadCountApi: UnreadCountAPI.UnreadCountsInterface
) : ViewModel() {

    private val _unreadNotificationCountFlow = MutableStateFlow(0)
    val unreadNotificationCountFlow = _unreadNotificationCountFlow.asStateFlow()

    private val _unreadMessageCountFlow = MutableStateFlow(0)
    val unreadMessageCountFlow = _unreadMessageCountFlow.asStateFlow()

    fun updateNotificationCount() {
        viewModelScope.launch {
            val params = RestParams(isForceReadFromNetwork = true)
            val unreadCount = unreadCountApi.getNotificationsCount(params).dataOrNull?.sumOf { it.unreadCount.orDefault() }.orDefault()
            _unreadNotificationCountFlow.value = unreadCount
        }
    }

    fun updateMessageCount() {
        viewModelScope.launch {
            val params = RestParams(isForceReadFromNetwork = true)
            val unreadCount = unreadCountApi.getUnreadConversationCount(params).dataOrNull?.unreadCount?.toIntOrNull().orDefault()
            _unreadMessageCountFlow.value = unreadCount
        }
    }
}
