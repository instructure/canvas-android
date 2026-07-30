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
package com.instructure.horizon.horizonui.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OfflineScreenWrapper(
    isOffline: Boolean,
    lastSyncedAtMs: Long?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            content()
        }
        if (isOffline) {
            OfflineBanner(
                lastSyncedAtMs = lastSyncedAtMs,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun OfflineScreenWrapperOnlinePreview() {
    OfflineScreenWrapper(isOffline = false, lastSyncedAtMs = null) {}
}

@Preview
@Composable
private fun OfflineScreenWrapperOfflinePreview() {
    OfflineScreenWrapper(isOffline = true, lastSyncedAtMs = System.currentTimeMillis() - 3_600_000L) {}
}
