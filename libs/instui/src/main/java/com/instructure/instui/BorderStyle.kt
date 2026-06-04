/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.instui

import androidx.compose.ui.graphics.PathEffect

/**
 * Border drawing styles, mapping CSS border-style values to Compose equivalents.
 *
 * Use [pathEffect] with a stroke-based drawing call to apply the style.
 */
enum class BorderStyle {
    Solid,
    Dashed,
    Dotted;

    /**
     * Returns the [PathEffect] for this border style, or null for [Solid].
     *
     * @param strokeWidth The border width in pixels, used to scale dash/dot patterns.
     */
    fun pathEffect(strokeWidth: Float = 1f): PathEffect? = when (this) {
        Solid -> null
        Dashed -> PathEffect.dashPathEffect(floatArrayOf(strokeWidth * 6, strokeWidth * 4), 0f)
        Dotted -> PathEffect.dashPathEffect(floatArrayOf(strokeWidth, strokeWidth * 2), 0f)
    }
}