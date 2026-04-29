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
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.list.Separator
import com.instructure.pandautils.compose.composables.CanvasDivider

@Composable
fun DSSeparator(modifier: Modifier = Modifier) {
    when (LocalDesignSystem.current) {
        DesignSystem.Legacy -> CanvasDivider(modifier = modifier)
        DesignSystem.InstUI -> Separator(modifier = modifier)
    }
}

@Preview(name = "DSSeparator Legacy — Light", showBackground = true)
@Preview(name = "DSSeparator Legacy — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DSSeparatorLegacyPreview() {
    CompositionLocalProvider(LocalDesignSystem provides DesignSystem.Legacy) {
        Column(modifier = Modifier.padding(16.dp)) {
            DSSeparator()
        }
    }
}

@Preview(name = "DSSeparator InstUI — Light", showBackground = true)
@Preview(name = "DSSeparator InstUI — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DSSeparatorInstUIPreview() {
    InstUITheme {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            Column(modifier = Modifier.padding(16.dp)) {
                DSSeparator()
            }
        }
    }
}