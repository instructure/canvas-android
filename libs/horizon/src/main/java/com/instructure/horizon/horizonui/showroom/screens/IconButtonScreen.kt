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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize

@Composable
fun IconButtonScreen() {
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        HorizonSpace(SpaceSize.SPACE_8)
        val colors = listOf(
            IconButtonColor.Black,
            IconButtonColor.Inverse,
            IconButtonColor.Ai,
            IconButtonColor.WhiteGreyOutline,
            IconButtonColor.InverseDanger,
            IconButtonColor.Ghost,
            IconButtonColor.Institution,
            IconButtonColor.Beige
        )

        colors.forEach {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    iconRes = R.drawable.add,
                    color = it,
                    size = IconButtonSize.NORMAL
                )
                IconButton(
                    iconRes = R.drawable.add,
                    color = it,
                    size = IconButtonSize.SMALL
                )
                IconButton(
                    iconRes = R.drawable.add,
                    color = it,
                    size = IconButtonSize.NORMAL,
                    badge = {
                        Badge(
                            content = BadgeContent.Text("5"),
                            type = BadgeType.Primary
                        )
                    })
                IconButton(
                    iconRes = R.drawable.add,
                    color = it,
                    size = IconButtonSize.NORMAL,
                    enabled = false
                )
            }
        }
    }
}

@Composable
@Preview
private fun IconButtonScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    IconButtonScreen()
}