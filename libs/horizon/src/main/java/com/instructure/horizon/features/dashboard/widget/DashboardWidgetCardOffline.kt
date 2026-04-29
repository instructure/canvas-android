/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun DashboardWidgetCardOffline(
    title: String,
    @DrawableRes iconRes: Int,
    widgetColor: Color,
    useMinWidth: Boolean,
    pageState: DashboardWidgetPageState,
    modifier: Modifier = Modifier
) {
    DashboardWidgetCard(
        title = title,
        iconRes = iconRes,
        widgetColor = widgetColor,
        useMinWidth = useMinWidth,
        pageState = pageState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.cloud_off),
                    contentDescription = null,
                    tint = HorizonColors.Text.timestamp(),
                    modifier = Modifier.size(16.dp)
                )
                HorizonSpace(SpaceSize.SPACE_8)
                Text(
                    text = stringResource(R.string.offline_notAvailableTitle),
                    style = HorizonTypography.p2,
                    color = HorizonColors.Text.timestamp()
                )
            }
        }
    }
}

@Composable
@Preview
private fun DashboardWidgetCardOfflinePreview() {
    DashboardWidgetCardOffline(
        title = "My Progress",
        iconRes = R.drawable.trending_up,
        widgetColor = HorizonColors.PrimitivesSky.sky12,
        useMinWidth = false,
        pageState = DashboardWidgetPageState.Empty
    )
}
