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
 */package com.instructure.pandautils.analytics.pageview.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class PageViewEvent(
    @PrimaryKey
    val key: String = UUID.randomUUID().toString(),
    val eventName: String,
    val sessionId: String,
    val postUrl: String,
    val signedProperties: String,
    val domain: String,
    val url: String,
    val contextType: String?,
    val contextId: String?,
    val userId: Long,
    val realUserId: Long?,
    val eventDuration: Double = 0.0,
    val timestamp: Date = Date()
)