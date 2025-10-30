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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun DashboardTimeSpentCardLoading(
    modifier: Modifier = Modifier,
) {
    DashboardCard(modifier.padding(bottom = 8.dp)) {
        Column(
            modifier = Modifier.padding(24.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .shimmerEffect(true)
                )

                Spacer(Modifier.weight(1f))

                Box(
                    Modifier
                        .width(50.dp)
                        .height(20.dp)
                        .shimmerEffect(
                            true,
                            backgroundColor = HorizonColors.PrimitivesHoney.honey12().copy(0.8f),
                            shimmerColor = HorizonColors.PrimitivesHoney.honey12()
                                .copy(alpha = 0.5f)
                        )
                )
            }

            HorizonSpace(SpaceSize.SPACE_8)

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .shimmerEffect(
                        true,
                    )
            )
        }
    }
}

@Composable
@Preview
private fun DashboardTimeSpentCardLoadingPreview() {
    DashboardTimeSpentCardLoading()
}
