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
package com.instructure.horizon.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

val WindowInsetsSides.Companion.BottomHorizontalSides: WindowInsetsSides
    get() = WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom

val WindowInsetsSides.Companion.TopHorizontalSides: WindowInsetsSides
    get() = WindowInsetsSides.Horizontal + WindowInsetsSides.Top

val WindowInsets.Companion.zeroScreenInsets: WindowInsets
    get() = WindowInsets(0, 0, 0, 0)

val WindowInsets.Companion.bottomNavigationScreenInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.TopHorizontalSides)

val WindowInsets.Companion.topBarScreenInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.BottomHorizontalSides)

val WindowInsets.Companion.horizontalSafeDrawing: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)

val WindowInsets.Companion.verticalSafeDrawing: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)

val WindowInsets.Companion.fullScreenInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing
