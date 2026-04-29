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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Represents which design system to use for rendering UI components.
 * Screens that support both design systems check [LocalDesignSystem] and
 * delegate to the appropriate component implementation.
 */
@Stable
enum class DesignSystem {
    /** New InstUI design system used by the Next Generation Canvas experience. */
    InstUI,
    /** Current Canvas design system (CanvasTheme-based components). */
    Legacy
}

/**
 * CompositionLocal for providing the current design system throughout the composition tree.
 * Defaults to [DesignSystem.Legacy] so existing screens remain unchanged.
 */
val LocalDesignSystem = staticCompositionLocalOf { DesignSystem.Legacy }