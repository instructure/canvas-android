/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.horizonui.animation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val animatedAlpha: Float = 0.0f
private const val animatedScale: Float = 0.8f

val enterTransition = slideInHorizontally(initialOffsetX = { it }) +
        fadeIn(initialAlpha = animatedAlpha)

val exitTransition = slideOutHorizontally(targetOffsetX = { -it }) +
        fadeOut(targetAlpha = animatedAlpha)

val popEnterTransition = slideInHorizontally(initialOffsetX = { -it }) +
        fadeIn(initialAlpha = animatedAlpha)

val popExitTransition = slideOutHorizontally(targetOffsetX = { it/2 }) +
        fadeOut(targetAlpha = animatedAlpha)

val mainEnterTransition = fadeIn(initialAlpha = animatedAlpha) +
        scaleIn(initialScale = animatedScale)

val mainExitTransition = fadeOut(targetAlpha = animatedAlpha) +
        scaleOut(targetScale = animatedScale)