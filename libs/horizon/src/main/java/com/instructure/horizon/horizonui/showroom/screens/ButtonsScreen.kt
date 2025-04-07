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
package com.instructure.horizon.horizonui.showroom.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth

@Composable
fun ButtonsScreen() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        val colors = listOf(
            ButtonColor.Black,
            ButtonColor.Inverse,
            ButtonColor.Ai,
            ButtonColor.WhiteWithOutline,
            ButtonColor.BlackOutline,
            ButtonColor.WhiteOutline,
            ButtonColor.Danger,
            ButtonColor.Ghost,
            ButtonColor.Institution,
            ButtonColor.Beige
        )

        val heights = ButtonHeight.entries.toTypedArray()
        val widths = ButtonWidth.entries.toTypedArray()
        val iconPositions = listOf(
            ButtonIconPosition.NoIcon,
            ButtonIconPosition.Start(iconRes = R.drawable.add),
            ButtonIconPosition.End(iconRes = R.drawable.add)
        )

        colors.forEach { color ->
            heights.forEach { height ->
                widths.forEach { width ->
                    iconPositions.forEach { iconPosition ->
                        Text(
                            text = "${height.name} ${width.name} ${color.javaClass.simpleName} ${iconPosition::class.simpleName}",
                            style = HorizonTypography.p2
                        )
                        HorizonSpace(SpaceSize.SPACE_2)
                        Button(
                            label = "Button",
                            height = height,
                            width = width,
                            color = color,
                            iconPosition = iconPosition,
                            badge = {
                                if (iconPosition is ButtonIconPosition.NoIcon) Badge(
                                    content = BadgeContent.Text("5"),
                                    type = BadgeType.Primary
                                )
                            }
                        )
                        HorizonSpace(SpaceSize.SPACE_8)
                    }
                }
            }
        }

        HorizonSpace(SpaceSize.SPACE_24)

    }
}