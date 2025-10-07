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
package com.instructure.horizon.features.dashboard.widget.timespent.card

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition

@Composable
fun DashboardTimeSpentCardError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardWidgetCard(
        stringResource(R.string.dashboardTimeSpentTitle),
        R.drawable.schedule,
        HorizonColors.PrimitivesHoney.honey12(),
        modifier.padding(bottom = 8.dp)
    ) {

        Text(
            text = stringResource(R.string.dashboardTimeSpentErrorMessage),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.timestamp()
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Button(
            label = stringResource(R.string.dashboardTimeSpentRetry),
            onClick = onRetry,
            color = ButtonColor.BlackOutline,
            height = ButtonHeight.SMALL,
            iconPosition = ButtonIconPosition.End(R.drawable.restart_alt)
        )
    }
}

@Composable
@Preview
private fun DashboardTimeSpentCardErrorPreview() {
    DashboardTimeSpentCardError(onRetry = {})
}
