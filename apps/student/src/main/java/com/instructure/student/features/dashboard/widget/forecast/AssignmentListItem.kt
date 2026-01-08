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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getFragmentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AssignmentListItem(
    assignment: AssignmentItem,
    onAssignmentClick: (FragmentActivity, Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val course = Course(id = assignment.courseId)
    val courseColor = Color(course.color)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onAssignmentClick(context.getFragmentActivity(), assignment.id, assignment.courseId)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(
                if (assignment.iconRes != 0) assignment.iconRes
                else R.drawable.ic_assignment
            ),
            contentDescription = null,
            tint = courseColor,
            modifier = Modifier.size(16.dp)
        )

        Box(
            modifier = Modifier
                .width(0.5.dp)
                .height(16.dp)
                .border(
                    width = 0.5.dp,
                    color = colorResource(R.color.borderMedium)
                )
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.courseName,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = courseColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                assignment.weight?.let { weight ->
                    WeightChip(
                        weight = weight,
                        color = courseColor
                    )
                }
            }

            Text(
                text = assignment.assignmentName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 19.sp,
                color = colorResource(R.color.textDarkest),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            val dateText = assignment.dueDate?.let { formatDate(it) }
                ?: assignment.gradedDate?.let { formatDate(it) }
                ?: ""

            if (dateText.isNotEmpty()) {
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colorResource(R.color.textDark),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WeightChip(
    weight: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                border = BorderStroke(0.5.dp, color),
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(
                R.string.forecastWidgetGradeWeight,
                weight.toInt()
            ),
            fontSize = 12.sp,
            lineHeight = 14.sp,
            color = color
        )
    }
}

private fun formatDate(date: Date): String {
    val now = Date()
    val calendar = java.util.Calendar.getInstance()

    calendar.time = now
    val todayStart = calendar.apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    calendar.time = now
    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    val tomorrowStart = calendar.apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    calendar.time = now
    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
    val tomorrowEnd = calendar.apply {
        set(java.util.Calendar.HOUR_OF_DAY, 23)
        set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59)
        set(java.util.Calendar.MILLISECOND, 999)
    }.timeInMillis

    val dateMillis = date.time
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())

    return when {
        dateMillis >= todayStart && dateMillis < tomorrowStart -> "Today, ${timeFormat.format(date)}"
        dateMillis >= tomorrowStart && dateMillis <= tomorrowEnd -> "Tomorrow, ${timeFormat.format(date)}"
        else -> dateFormat.format(date)
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0x1F2124
)
@Composable
private fun AssignmentListItemPreview() {
    AssignmentListItem(
        assignment = AssignmentItem(
            id = 1,
            courseId = 101,
            courseName = "COGS101",
            assignmentName = "The Mind's Maze: Mapping Cognition",
            dueDate = Date(),
            gradedDate = null,
            pointsPossible = 100.0,
            weight = 10.0,
            iconRes = R.drawable.ic_quiz,
            url = ""
        ),
        onAssignmentClick = { _, _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
private fun AssignmentListItemNoWeightPreview() {
    AssignmentListItem(
        assignment = AssignmentItem(
            id = 2,
            courseId = 204,
            courseName = "POLI204",
            assignmentName = "Fix a hyperdrive motivator using only duct tape and panic before the ship explodes, and everyone gets frig...",
            dueDate = Date(System.currentTimeMillis() + 86400000),
            gradedDate = null,
            pointsPossible = 50.0,
            weight = null,
            iconRes = R.drawable.ic_assignment,
            url = ""
        ),
        onAssignmentClick = { _, _, _ -> }
    )
}