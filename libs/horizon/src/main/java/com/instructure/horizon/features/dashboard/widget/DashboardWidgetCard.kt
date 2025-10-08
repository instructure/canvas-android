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
package com.instructure.horizon.features.dashboard.widget

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.pandautils.compose.modifiers.conditional

@Composable
fun DashboardWidgetCard(
    title: String,
    @DrawableRes iconRes: Int,
    widgetColor: Color,
    modifier: Modifier = Modifier,
    useMinWidth: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    DashboardCard(modifier) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .conditional(useMinWidth) {
                    width(IntrinsicSize.Min)
                }
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = HorizonTypography.labelMediumBold,
                    color = HorizonColors.Text.dataPoint(),
                    modifier = Modifier
                        .padding(end = 8.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(widgetColor)
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = HorizonColors.Icon.default(),
                        modifier = Modifier
                            .size(16.dp)
                    )
                }
            }

            HorizonSpace(SpaceSize.SPACE_8)

            content()
        }
    }
}

@Composable
@Preview
private fun DashboardTimeSpentCardPreview() {
    DashboardWidgetCard(
        title = "Time",
        iconRes = R.drawable.schedule,
        widgetColor = HorizonColors.PrimitivesBlue.blue12()
    ) {
        Text(
            text = "Content",
            style = HorizonTypography.h1,
            color = HorizonColors.Text.body()
        )
    }
}