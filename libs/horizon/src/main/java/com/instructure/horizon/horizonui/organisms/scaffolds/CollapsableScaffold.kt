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
package com.instructure.horizon.horizonui.organisms.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.zIndex
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.util.DefaultBarAlpha
import com.instructure.horizon.util.HorizonEdgeToEdgeSystemBars
import com.instructure.horizon.util.horizontalSafeDrawing
import com.instructure.horizon.util.plus

private val FabMargin = 16.dp

/**
 * Edge-to-edge scaffold with a single collapsing header that hides on scroll.
 *
 * The header is laid out at the top with a vertical offset driven by a nested-scroll connection.
 * Body content is rendered behind it, padded down by the header's currently visible height (so it
 * sits below the header at rest and follows the header as it collapses).
 *
 * Set [contentExtendsBehindStatusBar] to `true` to let body content scroll all the way under the
 * translucent status-bar overlay; otherwise content stops at the status-bar inset (the bar still
 * collapses fully — scroll handoff to the inner scrollable happens at the floor).
 */
@Composable
fun CollapsableHeaderScreen(
    headerContent: @Composable (contentPadding: PaddingValues) -> Unit,
    bodyContent: @Composable (contentPadding: PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
    statusBarColor: Color? = null,
    navigationBarColor: Color? = null,
    statusBarAlpha: Float = DefaultBarAlpha,
    navigationBarAlpha: Float = DefaultBarAlpha,
    contentExtendsBehindStatusBar: Boolean = false,
) {
    HorizonEdgeToEdgeSystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        statusBarAlpha = statusBarAlpha,
        navigationBarAlpha = navigationBarAlpha,
    ) { statusBarInsets, navigationBarInsets ->
        val density = LocalDensity.current
        val connection = rememberSaveable(saver = CollapsingBarsNestedScrollConnection.Saver) {
            CollapsingBarsNestedScrollConnection()
        }
        val statusBarPx = with(density) { statusBarInsets.getTop(density) }

        // When the body shouldn't extend behind the status bar, floor its effective top at
        // statusBars.top. Setting `topBodyFloorCollapsePx` tells the connection to stop claiming
        // scroll once the bar has collapsed past that floor — the rest of the gesture forwards to
        // the inner scrollable, so the handoff is seamless.
        connection.topBodyFloorCollapsePx = if (contentExtendsBehindStatusBar) {
            connection.topBarMaxHeight
        } else {
            (connection.topBarMaxHeight - statusBarPx).coerceAtLeast(0)
        }

        val topFloor = with(density) { if (contentExtendsBehindStatusBar) 0.dp else statusBarPx.toDp() }
        val visibleHeaderHeight = max(
            topFloor,
            with(density) { (connection.topBarMaxHeight + connection.topBarOffset).coerceAtLeast(0).toDp() }
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(connection)
                .clipToBounds()
        ) {
            // Body — fills the whole area, with horizontal cutout padding applied via modifier so
            // nested scaffolds see it as consumed. Horizontal nav-bar insets are handled by the
            // wrapper, so the PaddingValues passed to bodyContent only carries the bottom part —
            // callers applying `.padding(contentPadding)` would otherwise double-pad in landscape.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
            ) {
                bodyContent(
                    PaddingValues(top = visibleHeaderHeight) +
                            navigationBarInsets.only(WindowInsetsSides.Bottom).asPaddingValues()
                )
            }
            // Header overlay — collapses upward as the user scrolls.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
                    .offset { IntOffset(0, connection.topBarOffset) }
                    .onGloballyPositioned { connection.topBarMaxHeight = it.size.height }
            ) {
                headerContent(statusBarInsets.asPaddingValues())
            }
        }
    }
}

/**
 * Edge-to-edge scaffold with a collapsing top bar and a collapsing bottom bar.
 *
 * Both bars hide on scroll via the same nested-scroll connection. The bottom bar slot stays at the
 * screen bottom (it is *not* IME-padded), so when the keyboard opens it is hidden behind it; body
 * content is pushed above the keyboard via `Modifier.imePadding()` on the body wrapper.
 */
@Composable
fun CollapsableScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (contentPadding: PaddingValues) -> Unit = {},
    bottomBar: @Composable (contentPadding: PaddingValues) -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    statusBarColor: Color? = HorizonColors.Surface.pagePrimary(),
    navigationBarColor: Color? = null,
    statusBarAlpha: Float = DefaultBarAlpha,
    navigationBarAlpha: Float = DefaultBarAlpha,
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    HorizonEdgeToEdgeSystemBars(
        statusBarColor = statusBarColor,
        navigationBarColor = navigationBarColor,
        statusBarAlpha = statusBarAlpha,
        navigationBarAlpha = navigationBarAlpha,
    ) { statusBarInsets, navigationBarInsets ->
        val density = LocalDensity.current
        val connection = rememberSaveable(saver = CollapsingBarsNestedScrollConnection.Saver) {
            CollapsingBarsNestedScrollConnection()
        }

        // No clamped floor — body padding tracks the bars' full motion, so the connection should
        // consume scroll across the entire collapse range. Without this the bars mutate their
        // offsets (driving the visual collapse + body padding shrink) but return 0 consumed,
        // letting any inner nested-scroll connection claim the same gesture and double the effect.
        connection.topBodyFloorCollapsePx = connection.topBarMaxHeight
        connection.bottomBodyFloorCollapsePx = connection.bottomBarMaxHeight

        val visibleTopBarHeight = with(density) {
            (connection.topBarMaxHeight + connection.topBarOffset).coerceAtLeast(0).toDp()
        }
        val visibleBottomBarHeight = with(density) {
            (connection.bottomBarMaxHeight - connection.bottomBarOffset).coerceAtLeast(0).toDp()
        }
        val bodyPadding = PaddingValues(top = visibleTopBarHeight, bottom = visibleBottomBarHeight)

        val fabAlignment = when (floatingActionButtonPosition) {
            FabPosition.Start -> Alignment.BottomStart
            FabPosition.Center -> Alignment.BottomCenter
            else -> Alignment.BottomEnd
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(containerColor)
                .nestedScroll(connection)
                .clipToBounds()
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                // Body — fills the whole area; `imePadding()` pushes it above the keyboard; the
                // bottom-bar height is `consumeWindowInsets`'d so `imePadding` only adds the
                // *remaining* keyboard height past the bar (no bar+ime double-padding).
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(PaddingValues(bottom = visibleBottomBarHeight))
                        .imePadding()
                        .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
                        .testTag("collapsableContent")
                ) {
                    content(bodyPadding)
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                        .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
                        .offset { IntOffset(0, connection.topBarOffset) }
                        .onGloballyPositioned { connection.topBarMaxHeight = it.size.height }
                ) {
                    topBar(statusBarInsets.asPaddingValues())
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
                        .offset { IntOffset(0, connection.bottomBarOffset) }
                        .onGloballyPositioned { connection.bottomBarMaxHeight = it.size.height }
                ) {
                    bottomBar(navigationBarInsets.only(WindowInsetsSides.Bottom).asPaddingValues())
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
                        .padding(bottom = visibleBottomBarHeight)
                ) {
                    snackbarHost()
                }

                Box(
                    modifier = Modifier
                        .align(fabAlignment)
                        .windowInsetsPadding(WindowInsets.horizontalSafeDrawing)
                        .padding(bottom = visibleBottomBarHeight + FabMargin, start = FabMargin, end = FabMargin)
                ) {
                    floatingActionButton()
                }
            }
        }
    }
}

