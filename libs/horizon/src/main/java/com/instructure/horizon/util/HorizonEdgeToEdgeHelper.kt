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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.instructure.horizon.horizonui.foundation.HorizonColors

@Composable
fun HorizonEdgeToEdgeSystemBars(
    statusBarColor: Color? = HorizonColors.Surface.pagePrimary(),
    navigationBarColor: Color? = null,
    statusBarAlpha: Float = 0.8f,
    navigationBarAlpha: Float = 0.8f,
    content: @Composable () -> Unit
) {
    Box {
        content()
        statusBarColor?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(statusBarColor.copy(alpha = statusBarAlpha))
            )
        }

        navigationBarColor?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(navigationBarColor.copy(alpha = navigationBarAlpha))
            )
        }
    }
}

private val WindowInsetsSides.Companion.BottomHorizontalSides: WindowInsetsSides
    get() = WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom

private val WindowInsetsSides.Companion.TopHorizontalSides: WindowInsetsSides
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

val WindowInsets.Companion.topSafeDrawing: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)

val WindowInsets.Companion.bottomSafeDrawing: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)

val WindowInsets.Companion.fullScreenInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing
