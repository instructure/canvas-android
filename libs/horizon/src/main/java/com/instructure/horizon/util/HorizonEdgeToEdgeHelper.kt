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

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsEndWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsStartWidth
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

/**
 * Paints translucent overlays over the system status bar and navigation bar so screens can render
 * edge-to-edge while still showing system bar tinting that matches the screen's design.
 *
 * Side strips covering display cutouts are painted with `statusBarColor` (camera bumps live in the
 * top region semantically). The light/dark appearance of the system bars is set from each color's
 * luminance.
 */
@Composable
fun HorizonEdgeToEdgeSystemBars(
    statusBarColor: Color? = HorizonColors.Surface.pagePrimary(),
    navigationBarColor: Color? = null,
    statusBarAlpha: Float = DefaultBarAlpha,
    navigationBarAlpha: Float = DefaultBarAlpha,
    content: @Composable (statusBarInsets: WindowInsets, navigationBarInsets: WindowInsets) -> Unit,
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        statusBarColor?.let { controller.isAppearanceLightStatusBars = it.luminance() > 0.5f }
        navigationBarColor?.let { controller.isAppearanceLightNavigationBars = it.luminance() > 0.5f }
    }

    val statusBarInsets = if (statusBarColor != null) WindowInsets.statusBars else WindowInsets.zeroScreenInsets
    val navigationBarInsets = if (navigationBarColor != null) {
        WindowInsets.navigationBars
    } else {
        WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
    }

    Box {
        content(statusBarInsets, navigationBarInsets)

        // Side strips for display-cutout areas. Painted with statusBarColor (cutouts live in the
        // "top" region semantically). Width = safeDrawing horizontal = max(navBars.start, cutout.start).
        val sideStripColor = statusBarColor ?: navigationBarColor
        val sideStripAlpha = if (statusBarColor != null) statusBarAlpha else navigationBarAlpha
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .windowInsetsStartWidth(WindowInsets.safeDrawing)
                .background(sideStripColor?.copy(alpha = sideStripAlpha) ?: Color.Transparent)
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .windowInsetsEndWidth(WindowInsets.safeDrawing)
                .background(sideStripColor?.copy(alpha = sideStripAlpha) ?: Color.Transparent)
        )

        // Status bar overlay. Horizontal padding from `navigationBars` (not safeDrawing) so the
        // overlay extends OVER a top-edge cutout corner — drawn after the side strips so the corner
        // ends up the right color.
        statusBarColor?.let {
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                    .background(it.copy(alpha = statusBarAlpha))
            )
        }

        navigationBarColor?.let {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                    .background(it.copy(alpha = navigationBarAlpha))
            )
        }
    }
}

const val DefaultBarAlpha = 0.8f

/** Cutout + side nav horizontal extent; for keeping content clear of camera bumps and side nav. */
val WindowInsets.Companion.horizontalSafeDrawing: WindowInsets
    @Composable
    get() = safeDrawing.only(WindowInsetsSides.Horizontal)

/** System bars + display cutout, **without** IME — for bars that should stay under the keyboard. */
val WindowInsets.Companion.staticBars: WindowInsets
    @Composable
    get() = systemBars.union(displayCutout)

/** Zero-sized inset, useful as a conditional fallback when a colored overlay isn't desired. */
val WindowInsets.Companion.zeroScreenInsets: WindowInsets
    get() = WindowInsets(0, 0, 0, 0)

/** Used by SkillspaceScreen for a one-off top + horizontal safe-drawing area. */
val WindowInsets.Companion.topHorizontalWindowInsets: WindowInsets
    @Composable
    get() = safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + other.calculateBottomPadding()
    )
}
