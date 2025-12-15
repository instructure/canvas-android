/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.dashboard.widget.forecast

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R

@Composable
fun ForecastSegmentedControl(
    missingCount: Int,
    dueCount: Int,
    recentGradesCount: Int,
    selectedSection: ForecastSection?,
    onSectionSelected: (ForecastSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = colorResource(R.color.backgroundLightest),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        SegmentButton(
            count = missingCount,
            label = stringResource(R.string.forecastWidgetMissing),
            section = ForecastSection.MISSING,
            isSelected = selectedSection == ForecastSection.MISSING,
            onSelected = onSectionSelected,
            modifier = Modifier.weight(1f)
        )

        SegmentButton(
            count = dueCount,
            label = stringResource(R.string.forecastWidgetDue),
            section = ForecastSection.DUE,
            isSelected = selectedSection == ForecastSection.DUE,
            onSelected = onSectionSelected,
            modifier = Modifier.weight(1f)
        )

        SegmentButton(
            count = recentGradesCount,
            label = stringResource(R.string.forecastWidgetRecentGrades),
            section = ForecastSection.RECENT_GRADES,
            isSelected = selectedSection == ForecastSection.RECENT_GRADES,
            onSelected = onSectionSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentButton(
    count: Int,
    label: String,
    section: ForecastSection,
    isSelected: Boolean,
    onSelected: (ForecastSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onSelected(section) }
            .shadow(
                elevation = if (isSelected) 2.dp else 0.dp,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) {
                    colorResource(R.color.backgroundInfo)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp,
                    color = if (isSelected) Color.White else colorResource(R.color.textDarkest),
                    textAlign = TextAlign.Center
                )

                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down_small),
                    contentDescription = null,
                    tint = if (isSelected) Color.White else colorResource(R.color.textDark),
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .size(16.dp)
                        .then(
                            if (isSelected) Modifier.rotate(180f) else Modifier
                        )
                )
            }

            Text(
                text = label,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = if (isSelected) Color.White else colorResource(R.color.textDark),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun ForecastSegmentedControlPreview() {
    ForecastSegmentedControl(
        missingCount = 2,
        dueCount = 3,
        recentGradesCount = 4,
        selectedSection = ForecastSection.DUE,
        onSectionSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ForecastSegmentedControlNothingSelectedPreview() {
    ForecastSegmentedControl(
        missingCount = 2,
        dueCount = 3,
        recentGradesCount = 4,
        selectedSection = null,
        onSectionSelected = {}
    )
}