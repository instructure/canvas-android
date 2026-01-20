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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.instructure.horizon.horizonui.foundation.HorizonColors

@Composable
fun HorizonEdgeToEdgeSystemBars(
    statusBarColor: Color? = HorizonColors.Surface.pagePrimary(),
    navigationBarColor: Color? = null,
    modifier: Modifier = Modifier,
    statusBarAlpha: Float = 0.8f,
    navigationBarAlpha: Float = 0.8f,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        val insetsController = WindowCompat.getInsetsController(window, view)

        statusBarColor?.let { color ->
            val isLight = color.luminance() > 0.5f
            insetsController.isAppearanceLightStatusBars = isLight
        }

        navigationBarColor?.let { color ->
            val isLight = color.luminance() > 0.5f
            insetsController.isAppearanceLightNavigationBars = isLight
        }
    }

    Box {
        content()
        statusBarColor?.let {
            Box(
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(statusBarColor.copy(alpha = statusBarAlpha))
            )
        }

        navigationBarColor?.let {
            Box(
                modifier = modifier
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

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        end = this.calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding()
    )
}

@Composable
operator fun PaddingValues.minus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection) - other.calculateStartPadding(layoutDirection),
        top = this.calculateTopPadding() - other.calculateTopPadding(),
        end = this.calculateEndPadding(layoutDirection) - other.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() - other.calculateBottomPadding()
    )
}