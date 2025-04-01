/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.horizonui.molecules

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.pandautils.utils.toDp

/**
 * @param checkmark If this is set to true we won't show the icon, but instead show a checkmark only if the item is selected
 * @param iconRes The icon to show in all the items when checkmark is false
 */
sealed class SegmentedControlIconPosition(open val checkmark: Boolean, @DrawableRes open val iconRes: Int? = null) {
    data class NoIcon(override val checkmark: Boolean = false) : SegmentedControlIconPosition(checkmark)

    data class Start(override val checkmark: Boolean = false, @DrawableRes override val iconRes: Int = R.drawable.check) :
        SegmentedControlIconPosition(checkmark, iconRes)

    data class End(override val checkmark: Boolean = false, @DrawableRes override val iconRes: Int = R.drawable.check) :
        SegmentedControlIconPosition(checkmark, iconRes)
}

@Composable
fun SegmentedControl(
    options: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0,
    iconPosition: SegmentedControlIconPosition = SegmentedControlIconPosition.NoIcon()
) {
    val itemWidth = remember { mutableIntStateOf(0) }
    val indicatorOffset by animateIntAsState(
        targetValue = (selectedIndex * itemWidth.intValue),
        label = "IndicatorOffsetAnimation"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .height(44.dp)
                .fillMaxWidth()
                .background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level6)
                .border(HorizonBorder.level1(), shape = HorizonCornerRadius.level6)
        ) {
            options.forEachIndexed { index, label ->
                Button(
                    onClick = {
                        onItemSelected(index)
                    },
                    shape = HorizonCornerRadius.level6,
                    colors = ButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .onGloballyPositioned {
                            itemWidth.intValue = it.size.width
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val contentColor =
                            if (selectedIndex == index) HorizonColors.Surface.institution() else HorizonColors.Text.placeholder()
                        val showIcon = !iconPosition.checkmark || selectedIndex == index
                        if (iconPosition is SegmentedControlIconPosition.Start && showIcon) {
                            val iconRes = if (iconPosition.checkmark) R.drawable.check else iconPosition.iconRes
                            Icon(painterResource(iconRes), tint = contentColor, contentDescription = null)
                            HorizonSpace(SpaceSize.SPACE_4)
                        }
                        Text(label, style = HorizonTypography.buttonTextLarge, color = contentColor)
                        if (iconPosition is SegmentedControlIconPosition.End && showIcon) {
                            val iconRes = if (iconPosition.checkmark) R.drawable.check else iconPosition.iconRes
                            HorizonSpace(SpaceSize.SPACE_4)
                            Icon(painterResource(iconRes), tint = contentColor, contentDescription = null)
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(indicatorOffset, 0) }
                .width(itemWidth.intValue.toDp.dp)
                .height(44.dp)
                .background(
                    color = HorizonColors.Surface
                        .institution()
                        .copy(alpha = 0.05f), shape = HorizonCornerRadius.level6
                )
                .border(HorizonBorder.level2(color = HorizonColors.Surface.institution()), shape = HorizonCornerRadius.level6)
        )
    }
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_NoIcon_2Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2"),
        iconPosition = SegmentedControlIconPosition.NoIcon(checkmark = false)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_NoIcon_3Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2", "Option 3"),
        iconPosition = SegmentedControlIconPosition.NoIcon(checkmark = false)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_NoIcon_Checkmark_2Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2"),
        iconPosition = SegmentedControlIconPosition.NoIcon(checkmark = true)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_NoIcon_Checkmark_3Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2", "Option 3"),
        iconPosition = SegmentedControlIconPosition.NoIcon(checkmark = true)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_StartIcon_2Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2"),
        iconPosition = SegmentedControlIconPosition.Start(checkmark = false, iconRes = R.drawable.add)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_StartIcon_3Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2", "Option 3"),
        iconPosition = SegmentedControlIconPosition.Start(checkmark = false, iconRes = R.drawable.add)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_StartIcon_Checkmark_2Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2"),
        iconPosition = SegmentedControlIconPosition.Start(checkmark = true)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_StartIcon_Checkmark_3Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2", "Option 3"),
        iconPosition = SegmentedControlIconPosition.Start(checkmark = true)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_EndIcon_2Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2"),
        iconPosition = SegmentedControlIconPosition.End(checkmark = false, iconRes = R.drawable.add)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_EndIcon_3Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2", "Option 3"),
        iconPosition = SegmentedControlIconPosition.End(checkmark = false, iconRes = R.drawable.add)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_EndIcon_Checkmark_2Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2"),
        iconPosition = SegmentedControlIconPosition.End(checkmark = true)
    )
}

@Composable
@Preview(widthDp = 500)
fun SegmentedControlPreview_EndIcon_Checkmark_3Items() {
    ContextKeeper.appContext = LocalContext.current
    SegmentedControl(
        onItemSelected = {},
        options = listOf("Option 1", "Option 2", "Option 3"),
        iconPosition = SegmentedControlIconPosition.End(checkmark = true)
    )
}
