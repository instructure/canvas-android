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
package com.instructure.horizon.features.dashboard.widget.announcement.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState

@Composable
fun DashboardAnnouncementBannerCardError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            StatusChip(
                state = StatusChipState(
                    label = stringResource(R.string.notificationsAnnouncementCategoryLabel),
                    color = StatusChipColor.Sky,
                    fill = true
                ),
            )
            HorizonSpace(SpaceSize.SPACE_16)
            Text(
                text = stringResource(R.string.dashboardAnnouncementBannerErrorMessage),
                style = HorizonTypography.p2,
                color = HorizonColors.Text.timestamp()
            )
            HorizonSpace(SpaceSize.SPACE_8)
            Button(
                label = stringResource(R.string.dashboardAnnouncementRefreshMessage),
                iconPosition = ButtonIconPosition.End(R.drawable.restart_alt),
                height = ButtonHeight.SMALL,
                onClick = onRetry,
                color = ButtonColor.BlackOutline
            )
        }
    }
}

@Composable
@Preview
private fun DashboardAnnouncementBannerCardErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardAnnouncementBannerCardError(onRetry = {})
}
