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
package com.instructure.horizon.features.dashboard.course.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun DashboardCourseCardLoading(
    modifier: Modifier = Modifier,
) {
    DashboardCourseCard(modifier.padding(bottom = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.69f)
                    .shimmerEffect(
                        true,
                        shape = HorizonCornerRadius.level0,
                    )
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                HorizonSpace(SpaceSize.SPACE_8)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shimmerEffect(true)
                )

                HorizonSpace(SpaceSize.SPACE_8)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                        .shimmerEffect(
                            true,
                        )
                )

                HorizonSpace(SpaceSize.SPACE_8)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                        .shimmerEffect(
                            true,
                            shape = HorizonCornerRadius.level6,
                            backgroundColor = HorizonColors.Surface.institution().copy(alpha = 0.1f)
                        )
                )

                HorizonSpace(SpaceSize.SPACE_8)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .shimmerEffect(
                            true,
                            shape = HorizonCornerRadius.level2,
                            backgroundColor = HorizonColors.Surface.institution().copy(alpha = 0.1f)
                        )
                )

                HorizonSpace(SpaceSize.SPACE_24)
            }
        }
    }
}

@Composable
@Preview
private fun DashboardCourseCardLoadingPreview() {
    DashboardCourseCardLoading()
}