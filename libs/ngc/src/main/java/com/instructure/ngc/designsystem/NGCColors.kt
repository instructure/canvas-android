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

package com.instructure.ngc.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Next Gen Canvas design system colors.
 * Standalone implementation - does not delegate to Horizon.
 */
object NGCColors {

    object Surface {
        @Composable
        fun pageSecondary(): Color = if (isSystemInDarkTheme()) Color(0xFF191C1F) else Color(0xFFFFFFFF)

        @Composable
        fun inversePrimary(): Color = if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF273540)
    }

    object Icon {
        @Composable
        fun default(): Color = if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF273540)

        @Composable
        fun surfaceColored(): Color = if (isSystemInDarkTheme()) Color(0xFF273540) else Color(0xFFFFFFFF)
    }
}
