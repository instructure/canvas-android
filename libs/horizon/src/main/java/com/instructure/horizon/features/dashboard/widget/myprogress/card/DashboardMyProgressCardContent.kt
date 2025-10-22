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
package com.instructure.horizon.features.dashboard.widget.myprogress.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardMyProgressCardContent(
    state: DashboardMyProgressCardState,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    DashboardWidgetCard(
        stringResource(R.string.dashboardMyProgressTitle),
        R.drawable.trending_up,
        HorizonColors.PrimitivesSky.sky12,
        isLoading = isLoading,
        modifier = modifier
            .widthIn(max = 300.dp)
            .padding(bottom = 8.dp)
    ) {
        if(state.moduleCountCompleted == 0) {
            Text(
                text = stringResource(R.string.dashboardMyProgressEmptyMessage),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp(),
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .shimmerEffect(isLoading)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.moduleCountCompleted.toString(),
                    style = HorizonTypography.h1.copy(fontSize = 38.sp, letterSpacing = 0.sp),
                    color = HorizonColors.Text.body(),
                    modifier = Modifier.shimmerEffect(isLoading)
                )

                Text(
                    text = stringResource(R.string.dashboardMyProgressCompleted),
                    style = HorizonTypography.labelMediumBold,
                    color = HorizonColors.Text.title(),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .shimmerEffect(isLoading)
                )
            }
        }
    }
}

@Composable
@Preview
private fun DashboardMyProgressCardContentPreview() {
    DashboardMyProgressCardContent(
        state = DashboardMyProgressCardState(
            moduleCountCompleted = 24
        ),
        false
    )
}

@Composable
@Preview
private fun DashboardMyProgressCardContentZeroPreview() {
    DashboardMyProgressCardContent(
        state = DashboardMyProgressCardState(
            moduleCountCompleted = 0
        ),
        false
    )
}

@Composable
@Preview
private fun DashboardMyProgressLoadingPreview() {
    DashboardMyProgressCardContent(
        state = DashboardMyProgressCardState(
            moduleCountCompleted = 0
        ),
        isLoading = true
    )
}
