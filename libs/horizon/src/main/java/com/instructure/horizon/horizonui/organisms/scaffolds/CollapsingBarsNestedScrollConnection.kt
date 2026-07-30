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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/**
 * Nested scroll connection that collapses an optional top bar (negative offset, range
 * `-topBarMaxHeight..0`) and an optional bottom bar (positive offset, range `0..bottomBarMaxHeight`).
 *
 * Setting `bottomBarMaxHeight = 0` reduces it to a top-only collapsing bar (used by
 * `CollapsableHeaderScreen` and `ModuleItemSequenceScreen`).
 *
 * Each bar can claim less than its full motion via `topBodyFloorCollapsePx` /
 * `bottomBodyFloorCollapsePx`: while the bar is past the floor the scroll is forwarded to the inner
 * scrollable instead of consumed, so the bar can finish collapsing in lockstep with content scroll
 * (avoids a stuttery handoff when the body's padding is clamped to e.g. the status-bar inset).
 *
 * All four size-like fields are mutable state, so the composable can drive them in
 * `onGloballyPositioned` without rebuilding the connection.
 */
internal class CollapsingBarsNestedScrollConnection : NestedScrollConnection {

    var topBarMaxHeight: Int by mutableIntStateOf(0)
    var bottomBarMaxHeight: Int by mutableIntStateOf(0)
    var topBodyFloorCollapsePx: Int by mutableIntStateOf(0)
    var bottomBodyFloorCollapsePx: Int by mutableIntStateOf(0)
    var topBarOffset: Int by mutableIntStateOf(0)
    var bottomBarOffset: Int by mutableIntStateOf(0)

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y.toInt()

        val topPrev = topBarOffset
        topBarOffset = (topPrev + delta).coerceIn(-topBarMaxHeight, 0)
        val topConsumed = topBarOffset.coerceAtLeast(-topBodyFloorCollapsePx) -
            topPrev.coerceAtLeast(-topBodyFloorCollapsePx)

        val bottomPrev = bottomBarOffset
        bottomBarOffset = (bottomPrev - delta).coerceIn(0, bottomBarMaxHeight)
        // Bottom bar offset moves in the opposite direction (positive = collapsed), so consumption
        // is the *previous* claim minus the new one — opposite sign from the top bar.
        val bottomConsumed = bottomPrev.coerceAtMost(bottomBodyFloorCollapsePx) -
            bottomBarOffset.coerceAtMost(bottomBodyFloorCollapsePx)

        return Offset(0f, (topConsumed + bottomConsumed).toFloat())
    }

    companion object {
        val Saver = listSaver<CollapsingBarsNestedScrollConnection, Int>(
            save = { listOf(it.topBarMaxHeight, it.topBarOffset, it.bottomBarMaxHeight, it.bottomBarOffset) },
            restore = {
                CollapsingBarsNestedScrollConnection().apply {
                    topBarMaxHeight = it[0]
                    topBarOffset = it[1]
                    bottomBarMaxHeight = it[2]
                    bottomBarOffset = it[3]
                }
            }
        )
    }
}
