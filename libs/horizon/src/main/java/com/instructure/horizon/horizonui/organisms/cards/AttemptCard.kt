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
package com.instructure.horizon.horizonui.organisms.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

data class AttemptCardState(
    val attemptTitle: String,
    val date: String,
    val score: String? = null,
    val onClick: (() -> Unit)? = null,
    val selected: Boolean = false,
)

@Composable
fun AttemptCard(state: AttemptCardState, modifier: Modifier = Modifier) {
    val onClick = state.onClick
    Card(
        shape = HorizonCornerRadius.level2,
        colors = CardDefaults.cardColors().copy(containerColor = HorizonColors.Surface.cardPrimary()),
        border = if (state.selected) HorizonBorder.level2(HorizonColors.Surface.institution()) else HorizonBorder.level1(),
        modifier = modifier
    ) {
        val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
        Column(
            modifier = clickModifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(text = state.attemptTitle, style = HorizonTypography.p2)
            HorizonSpace(SpaceSize.SPACE_2)
            Text(text = state.date, style = HorizonTypography.p2, color = HorizonColors.Text.timestamp())
            state.score?.let {
                HorizonSpace(SpaceSize.SPACE_12)
                Text(text = it, style = HorizonTypography.p2, color = HorizonColors.Text.timestamp())
            }
        }
    }
}