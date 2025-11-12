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
package com.instructure.horizon.features.dashboard.widget.course.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition

@Composable
fun DashboardMoreCourseCard(
    courseCount: Int,
    modifier: Modifier = Modifier,
    onMoreClicked: () -> Unit
) {
    DashboardCard(modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.weight(1f))

            Icon(
                painter = painterResource(R.drawable.book_2_filled),
                contentDescription = null,
                tint = HorizonColors.Surface.institution()
            )

            HorizonSpace(SpaceSize.SPACE_16)

            // No need to use plurals as this card is only displayed when there are 4+ courses.
            Text(
                stringResource(R.string.dashboardCourseCardEnrolledCoursesMessage, courseCount),
                style = HorizonTypography.p1,
                color = HorizonColors.Text.title()
            )

            HorizonSpace(SpaceSize.SPACE_16)

            Button(
                label = stringResource(R.string.dashboardCourseCardEnrolledCoursesSeeAllLabel),
                onClick = onMoreClicked,
                color = ButtonColor.WhiteWithOutline,
                iconPosition = ButtonIconPosition.End(R.drawable.arrow_forward)
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
@Preview
private fun DashboardMoreCourseCardPreview() {
    DashboardMoreCourseCard(10) {}
}