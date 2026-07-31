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

package com.instructure.ngc.utils

import androidx.annotation.ColorInt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.ColorKeeper

/**
 * Resolves the contrast-corrected [ColorKeeper] course color for [courseId], picking the light or
 * dark variant based on [isSystemInDarkTheme]. NGC keys off Compose's theme state rather than
 * [ColorKeeper.darkTheme] so the color recomposes immediately with the rest of the screen.
 */
@ColorInt
@Composable
fun rememberThemedCourseColor(courseId: Long): Int {
    val isDark = isSystemInDarkTheme()
    val themedColor = remember(courseId) { ColorKeeper.getOrGenerateColor(Course(id = courseId)) }
    return if (isDark) themedColor.dark else themedColor.light
}
