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
package com.instructure.horizon.horizonui.organisms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

@Composable
fun CollapsableHeaderScreen(
    headerContent: @Composable () -> Unit,
    bodyContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {

    val scrollConnectionSaver = Saver<MutableState<CollapsingAppBarNestedScrollConnection>, Pair<Int, Int>>(
        save = { it.value.appBarMaxHeight to it.value.appBarOffset },
        restore = { (mutableStateOf(CollapsingAppBarNestedScrollConnection(it.first).apply { appBarOffset = it.second })) }
    )
    val density = LocalDensity.current
    var moduleHeaderHeight by rememberSaveable { mutableIntStateOf(0) }
    var nestedScrollConnection by rememberSaveable(saver = scrollConnectionSaver) { mutableStateOf(CollapsingAppBarNestedScrollConnection(moduleHeaderHeight)) }

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(0, nestedScrollConnection.appBarOffset) }
                .onGloballyPositioned { coordinates ->
                    if (coordinates.size.height != moduleHeaderHeight) {
                        moduleHeaderHeight = coordinates.size.height
                        val temp = nestedScrollConnection.appBarOffset
                        nestedScrollConnection =
                            CollapsingAppBarNestedScrollConnection(moduleHeaderHeight).apply {
                                appBarOffset = temp
                            }
                    }
                }
        ) {
            headerContent()
        }
        val moduleHeaderHeight = max(0.dp, with(density) { moduleHeaderHeight.toDp() } + with(density) { nestedScrollConnection.appBarOffset.toDp() })

        Box(
            modifier = Modifier
                .padding(top = moduleHeaderHeight)
        ) {
            bodyContent()
        }
    }
}

@Composable
fun CollapsableScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable () -> Unit,
) {

    val scrollConnectionSaver =
        Saver<MutableState<CollapsingBarsNestedScrollConnection>, Pair<Pair<Int, Int>, Pair<Int, Int>>>(
            save = {
                (it.value.topBarMaxHeight to it.value.topBarOffset) to
                (it.value.bottomBarMaxHeight to it.value.bottomBarOffset)
            },
            restore = {
                (mutableStateOf(CollapsingBarsNestedScrollConnection(it.first.first, it.second.first).apply {
                    topBarOffset = it.first.second
                    bottomBarOffset = it.second.second
                }))
            }
        )
    val density = LocalDensity.current

    var topBarHeight by rememberSaveable { mutableIntStateOf(0) }
    var bottomBarHeight by rememberSaveable { mutableIntStateOf(0) }

    var nestedScrollConnection by rememberSaveable(saver = scrollConnectionSaver) {
        mutableStateOf(CollapsingBarsNestedScrollConnection(topBarHeight, bottomBarHeight))
    }

    val collapsedTopBarPadding = max(
        0.dp,
        with(density) { topBarHeight.toDp() } + with(density) { nestedScrollConnection.topBarOffset.toDp() })

    val collapsedBottomBarPadding = max(
        0.dp,
        with(density) { bottomBarHeight.toDp() } - with(density) { nestedScrollConnection.bottomBarOffset.toDp() })

    Scaffold(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .clipToBounds(),
        topBar = {
            Box(
                Modifier.offset { IntOffset(0, nestedScrollConnection.topBarOffset) }
                    .onGloballyPositioned { coordinates ->
                        if (coordinates.size.height != topBarHeight) {
                            topBarHeight = coordinates.size.height
                            val tempTop = nestedScrollConnection.topBarOffset
                            val tempBottom = nestedScrollConnection.bottomBarOffset
                            nestedScrollConnection =
                                CollapsingBarsNestedScrollConnection(topBarHeight, bottomBarHeight).apply {
                                    topBarOffset = tempTop
                                    bottomBarOffset = tempBottom
                                }
                        }
                    }
            ) {
                topBar()
            }
        },
        bottomBar = {
            Box(
                Modifier.offset { IntOffset(0, nestedScrollConnection.bottomBarOffset) }
                    .onGloballyPositioned { coordinates ->
                        if (coordinates.size.height != bottomBarHeight) {
                            bottomBarHeight = coordinates.size.height
                            val tempTop = nestedScrollConnection.topBarOffset
                            val tempBottom = nestedScrollConnection.bottomBarOffset
                            nestedScrollConnection =
                                CollapsingBarsNestedScrollConnection(topBarHeight, bottomBarHeight).apply {
                                    topBarOffset = tempTop
                                    bottomBarOffset = tempBottom
                                }
                        }
                    }
            ) {
                bottomBar()
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = { paddingValues ->
            val layoutDirection = LocalLayoutDirection.current
            Box(
                modifier = Modifier.padding(
                    PaddingValues(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        top = collapsedTopBarPadding,
                        bottom = collapsedBottomBarPadding
                    )
                ).testTag("collapsableContent")
            ) {
                content()
            }
        }
    )
}

private class CollapsingAppBarNestedScrollConnection(
    val appBarMaxHeight: Int
) : NestedScrollConnection {

    var appBarOffset: Int by mutableIntStateOf(0)

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y.toInt()
        val newOffset = appBarOffset + delta
        val previousOffset = appBarOffset
        appBarOffset = newOffset.coerceIn(-appBarMaxHeight, 0)
        val consumed = appBarOffset - previousOffset
        return Offset(0f, consumed.toFloat())
    }
}

private class CollapsingBarsNestedScrollConnection(
    val topBarMaxHeight: Int,
    val bottomBarMaxHeight: Int
) : NestedScrollConnection {

    var topBarOffset: Int by mutableIntStateOf(0)
    var bottomBarOffset: Int by mutableIntStateOf(0)

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y.toInt()

        val topNewOffset = topBarOffset + delta
        val topPreviousOffset = topBarOffset
        topBarOffset = topNewOffset.coerceIn(-topBarMaxHeight, 0)
        val topConsumed = topBarOffset - topPreviousOffset

        val bottomNewOffset = bottomBarOffset - delta
        val bottomPreviousOffset = bottomBarOffset
        bottomBarOffset = bottomNewOffset.coerceIn(0, bottomBarMaxHeight)
        val bottomConsumed = bottomBarOffset - bottomPreviousOffset

        val totalConsumed = topConsumed + bottomConsumed
        return Offset(0f, totalConsumed.toFloat())
    }
}