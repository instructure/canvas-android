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

package com.instructure.pandautils.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.list.SectionHeader
import com.instructure.pandautils.compose.composables.GroupHeader

@Composable
fun DSSectionHeader(
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (LocalDesignSystem.current) {
        DesignSystem.Legacy -> GroupHeader(
            name = label,
            expanded = expanded,
            onClick = onClick,
            modifier = modifier,
        )
        DesignSystem.InstUI -> SectionHeader(
            label = label,
            expanded = expanded,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

@Preview(name = "DSSectionHeader Legacy — Light", showBackground = true)
@Preview(name = "DSSectionHeader Legacy — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DSSectionHeaderLegacyPreview() {
    CompositionLocalProvider(LocalDesignSystem provides DesignSystem.Legacy) {
        Column {
            DSSectionHeader(label = "Overdue Assignments", expanded = true, onClick = {})
            DSSectionHeader(label = "Upcoming Assignments", expanded = false, onClick = {})
        }
    }
}

@Preview(name = "DSSectionHeader InstUI — Light", showBackground = true)
@Preview(name = "DSSectionHeader InstUI — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DSSectionHeaderInstUIPreview() {
    InstUITheme {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            Column {
                DSSectionHeader(label = "Overdue Assignments", expanded = true, onClick = {})
                DSSectionHeader(label = "Upcoming Assignments", expanded = false, onClick = {})
            }
        }
    }
}