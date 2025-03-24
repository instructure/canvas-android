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
 */
package com.instructure.horizon.horizonui.foundation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SpaceSize(val value: Int) {
    SPACE_2(2),
    SPACE_4(4),
    SPACE_8(8),
    SPACE_10(10),
    SPACE_12(12),
    SPACE_16(16),
    SPACE_24(24),
    SPACE_32(32),
    SPACE_36(36),
    SPACE_40(40),
    SPACE_48(48),
}

@Composable
fun RowScope.HorizonSpace(size: SpaceSize, modifier: Modifier = Modifier) = Spacer(modifier = modifier.width(size.value.dp))

@Composable
fun ColumnScope.HorizonSpace(size: SpaceSize, modifier: Modifier = Modifier) = Spacer(modifier = modifier.height(size.value.dp))

@Composable
fun BoxScope.HorizonSpace(size: SpaceSize, modifier: Modifier = Modifier) = Spacer(modifier = modifier.size(size.value.dp))