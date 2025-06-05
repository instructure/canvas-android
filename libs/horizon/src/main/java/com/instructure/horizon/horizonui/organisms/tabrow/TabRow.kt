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
package com.instructure.horizon.horizonui.organisms.tabrow

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors

@Composable
fun<T> TabRow(
    tabs: List<T>,
    onTabSelected: (Int) -> Unit,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    spacing: Dp = 24.dp,
    selectedIndicator: @Composable BoxScope.(Modifier) -> Unit = { SelectedTabIndicator(it) },
    tab: @Composable (T, Boolean, Modifier) -> Unit,
) {
    val localDensity = LocalDensity.current
    val scrollState = rememberScrollState()
    var containerWidth by remember { mutableIntStateOf(0) }
    var tabRowWidth by remember { mutableIntStateOf(0) }
    var sizes by remember { mutableStateOf(tabs.map { 0 }) }
    val spacingPx = with(localDensity) { spacing.toPx().toInt() }
    val currentOffset by animateIntAsState(
        sizes.take(selectedIndex).sumOf { it }
                + (selectedIndex) * spacingPx
                - scrollState.value
                + ((containerWidth - tabRowWidth) / 2).coerceAtLeast(0)
        ,
        label = "IndicatorAnimation"
    )


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                containerWidth = it.size.width
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .horizontalScroll(scrollState)
                .onGloballyPositioned {
                    tabRowWidth = it.size.width
                }
        ) {
            tabs.forEachIndexed { index, tabItem ->
                tab(
                    tabItem,
                    index == selectedIndex,
                    Modifier
                        .onGloballyPositioned { coordinates ->
                            val width = coordinates.size.width
                            sizes = sizes
                                .toMutableList()
                                .apply {
                                    this[index] = width
                                }
                        }
                        .clickable {
                            onTabSelected(index)
                        }
                )
                if (index != tabs.lastIndex) {
                    Spacer(modifier = Modifier.width(spacing))
                }
            }
        }
        val width = with(localDensity) { sizes[selectedIndex].toDp() }
        selectedIndicator(
            Modifier
                .width(width)
                .offset { IntOffset(currentOffset, 0) }
        )
    }
}

@Composable
private fun BoxScope.SelectedTabIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(1.dp)
            .align(Alignment.BottomStart)
            .offset(y = 2.dp)
            .background(HorizonColors.Text.surfaceInverseSecondary())
    )
}

@Composable
@Preview(backgroundColor = 0xFFDDDDDD, showBackground = true)
fun TabRowPreview() {
    TabRow(
        tabs = listOf("Tab 1", "Tab Tab 2", "Very LongTab 3"),
        selectedIndex = 0,
        onTabSelected = {},
        tab = { tab, isSelected, modifier -> Text(tab) },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}