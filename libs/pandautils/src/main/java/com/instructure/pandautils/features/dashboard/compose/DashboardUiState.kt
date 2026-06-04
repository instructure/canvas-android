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
 */

package com.instructure.pandautils.features.dashboard.compose

import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.utils.ThemedColor

data class DashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val refreshing: Boolean = false,
    val isOnline: Boolean = true,
    val widgets: List<WidgetMetadata> = emptyList(),
    val onRefresh: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val color: ThemedColor = ThemedColor(GlobalConfig.DEFAULT_COLOR)
)
