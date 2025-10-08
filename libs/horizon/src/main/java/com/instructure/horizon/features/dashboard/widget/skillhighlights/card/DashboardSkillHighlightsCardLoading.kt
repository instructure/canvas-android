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
package com.instructure.horizon.features.dashboard.widget.skillhighlights.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetCard
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun DashboardSkillHighlightsCardLoading(modifier: Modifier = Modifier) {
    DashboardWidgetCard(
        title = stringResource(R.string.dashboardSkillHighlightsTitle),
        iconRes = R.drawable.hub,
        widgetColor = HorizonColors.PrimitivesGreen.green12(),
        useMinWidth = false,
        modifier = modifier
    ) {
        HorizonSpace(SpaceSize.SPACE_8)
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shimmerEffect(
                            true,
                            backgroundColor = HorizonColors.PrimitivesGreen.green12().copy(alpha = 0.5f)
                        )
                )
                if (it < 2) {
                    HorizonSpace(SpaceSize.SPACE_8)
                }
            }
        }
    }
}

@Composable
@Preview
private fun DashboardSkillHighlightsCardLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    DashboardSkillHighlightsCardLoading()
}
