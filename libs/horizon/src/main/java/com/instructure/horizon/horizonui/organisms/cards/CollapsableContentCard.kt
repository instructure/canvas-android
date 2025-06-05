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
package com.instructure.horizon.horizonui.organisms.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun CollapsableContentCard(
    title: String,
    expandableSubtitle: String,
    expanded: Boolean,
    onExpandChanged: (Boolean) -> Unit,
    expandableContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(HorizonCornerRadius.level2)
            .background(HorizonColors.Surface.cardPrimary())
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.clickable { onExpandChanged(!expanded) }
            ){
                Text(
                    title,
                    style = HorizonTypography.h2,
                    color = HorizonColors.Text.body(),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                HorizonSpace(SpaceSize.SPACE_16)

                val rotationAnimation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "rotationAnimation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_down),
                        contentDescription = null,
                        tint = HorizonColors.Icon.default(),
                        modifier = Modifier
                            .rotate(rotationAnimation)
                    )

                    HorizonSpace(SpaceSize.SPACE_8)

                    Text(
                        expandableSubtitle,
                        style = HorizonTypography.p1,
                        color = HorizonColors.Text.body(),
                    )
                }
            }

            AnimatedVisibility(
                expanded,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                expandableContent()
            }
        }
    }
}

@Composable
@Preview
private fun CollapsableContentCardExpandedPreview() {
    CollapsableContentCard(
        title = "Title",
        expandableSubtitle = "Subtitle",
        expanded = true,
        onExpandChanged = {},
        expandableContent = {
            Text(
                text = "Expandable content",
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    )
}

@Composable
@Preview
private fun CollapsableContentCardCollapsedPreview() {
    CollapsableContentCard(
        title = "Title",
        expandableSubtitle = "Subtitle",
        expanded = false,
        onExpandChanged = {},
        expandableContent = {
            Text(
                text = "Expandable content",
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    )
}