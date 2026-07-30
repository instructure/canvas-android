/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.assignments.details.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.pandautils.utils.ThemedColor

@Composable
fun GradeCell(
    viewData: GradeCellViewData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentDescription = viewData.gradeCellContentDescription
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.borderMedium),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(role = Role.Button, onClick = onClick)
            // Merge (not clear) the subtree so screen readers announce the combined description,
            // while individual inner nodes (score/grade/outOf/labels) remain reachable in the
            // unmerged semantics tree for UI tests.
            .semantics(mergeDescendants = true) {
                if (contentDescription.isNotEmpty()) {
                    this.contentDescription = contentDescription
                }
                role = Role.Button
            }
            .padding(16.dp)
            .testTag("gradeCell")
    ) {
        if (viewData.state == GradeCellViewData.State.EMPTY) {
            // No submission yet: just the centered "Submission & Feedback" link.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubmissionAndRubricLabel(viewData)
            }
        } else {
            GradeCellHeader(viewData)

            when (viewData.state) {
                GradeCellViewData.State.UPLOADING -> StatusMessage(
                    title = stringResource(id = R.string.submissionStatusUploadingTitle),
                    subtitle = stringResource(id = R.string.submissionStatusUploadingSubtitle),
                    subtitleColor = colorResource(id = R.color.textInfo)
                )

                GradeCellViewData.State.FAILED -> StatusMessage(
                    title = stringResource(id = R.string.submissionStatusFailedTitle),
                    subtitle = stringResource(id = R.string.submissionStatusFailedSubtitle),
                    subtitleColor = colorResource(id = R.color.textInfo)
                )

                GradeCellViewData.State.SUBMITTED -> SubmittedState()

                GradeCellViewData.State.GRADED -> GradedState(viewData)

                GradeCellViewData.State.EMPTY -> Unit
            }
        }
    }
}

@Composable
private fun GradeCellHeader(viewData: GradeCellViewData) {
    val showGradeLabel = viewData.state == GradeCellViewData.State.SUBMITTED ||
        viewData.state == GradeCellViewData.State.GRADED
    val showSubmissionAndRubricLabel = viewData.state != GradeCellViewData.State.UPLOADING &&
        viewData.state != GradeCellViewData.State.FAILED

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showGradeLabel) {
            Text(
                text = stringResource(id = R.string.grade),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier.testTag("gradeLabel")
            )
        } else {
            Spacer(modifier = Modifier.size(0.dp))
        }

        if (showSubmissionAndRubricLabel) {
            SubmissionAndRubricLabel(viewData)
        }
    }
}

@Composable
private fun SubmissionAndRubricLabel(viewData: GradeCellViewData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(id = R.string.submissionAndFeedback),
            fontSize = 16.sp,
            color = Color(viewData.submissionAndRubricLabelColor),
            modifier = Modifier.testTag("submissionAndRubricLabel")
        )
        Spacer(modifier = Modifier.size(7.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_grade_cell_arrow_right),
            contentDescription = null,
            tint = Color(viewData.submissionAndRubricLabelColor),
        )
    }
}

@Composable
private fun StatusMessage(title: String, subtitle: String, subtitleColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 34.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            color = colorResource(id = R.color.textDarkest),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 16.sp,
            color = subtitleColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SubmittedState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_complete_solid),
                contentDescription = null,
                tint = colorResource(id = R.color.backgroundSuccess),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = stringResource(id = R.string.submissionStatusSuccessTitle),
                fontSize = 22.sp,
                color = colorResource(id = R.color.textDarkest)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.submissionStatusWaitingSubtitle),
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDark),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GradedState(viewData: GradeCellViewData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .padding(end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            DonutChart(
                percent = viewData.chartPercent,
                color = if (viewData.showIncompleteIcon) {
                    colorResource(id = R.color.textDark)
                } else {
                    Color(viewData.courseColor.color())
                },
                trackColor = Color(viewData.backgroundColorWithAlpha),
                modifier = Modifier.size(130.dp)
            )
            when {
                viewData.showCompleteIcon -> Icon(
                    painter = painterResource(id = R.drawable.ic_checkmark_lined),
                    contentDescription = null,
                    tint = Color(viewData.courseColor.color()),
                    modifier = Modifier.size(52.dp)
                )

                viewData.showIncompleteIcon -> Icon(
                    painter = painterResource(id = R.drawable.ic_close_lined),
                    contentDescription = null,
                    tint = colorResource(id = R.color.textDark),
                    modifier = Modifier.size(52.dp)
                )

                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (viewData.score.isNotEmpty()) {
                        Text(
                            text = viewData.score,
                            fontSize = 32.sp,
                            maxLines = 1,
                            color = colorResource(id = R.color.textDarkest),
                            modifier = Modifier.testTag("gradeCellScore")
                        )
                    }
                    if (viewData.showPointsLabel) {
                        Text(
                            text = stringResource(id = R.string.canvasAPI_points),
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.textDark)
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            if (viewData.grade.isNotEmpty()) {
                Text(
                    text = viewData.grade,
                    fontSize = 32.sp,
                    maxLines = 1,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.testTag("gradeCellGrade")
                )
            }
            if (viewData.outOf.isNotEmpty()) {
                Text(
                    text = viewData.outOf,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("gradeCellOutOf")
                )
            }
            if (viewData.yourGrade.isNotEmpty()) {
                Text(
                    text = viewData.yourGrade,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (viewData.latePenalty.isNotEmpty()) {
                Text(
                    text = viewData.latePenalty,
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textWarning),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (viewData.finalGrade.isNotEmpty()) {
                Text(
                    text = viewData.finalGrade,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    viewData.stats?.let { stats ->
        Spacer(modifier = Modifier.height(8.dp))
        GradeStatistics(
            stats = stats,
            accentColor = Color(viewData.courseColor.color()),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stats.minText, fontSize = 14.sp, color = colorResource(id = R.color.textDark))
            Text(text = stats.meanText, fontSize = 14.sp, color = colorResource(id = R.color.textDark))
            Text(text = stats.maxText, fontSize = 14.sp, color = colorResource(id = R.color.textDark))
        }
    }
}

@Composable
fun DonutChart(
    percent: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier
) {
    // Animate the arc sweeping in from 0 to the target whenever the chart (re)appears — e.g. when a
    // submission finishes uploading and the cell switches to the graded state. In previews there is
    // no LaunchedEffect, so start at the target value so the donut renders filled.
    // Guard against NaN/Infinity (e.g. score / 0 points possible), which would crash the animation.
    val safePercent = if (percent.isFinite()) percent.coerceIn(0f, 1f) else 0f
    val inInspection = LocalInspectionMode.current
    var target by remember { mutableFloatStateOf(if (inInspection) safePercent else 0f) }
    LaunchedEffect(safePercent) { target = safePercent }
    val animatedPercent by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 500),
        label = "donutProgress"
    )
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val diameter = size.minDimension - strokeWidth - 1f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            color = trackColor,
            radius = diameter / 2f,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = color,
            startAngle = 270f,
            sweepAngle = 359.9999f * animatedPercent,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
    }
}

@Composable
fun GradeStatistics(
    stats: GradeCellViewState.GradeStats,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val lineColor = colorResource(id = R.color.backgroundMedium)
    val darkLineColor = colorResource(id = R.color.textDark)
    val meanLineColor = colorResource(id = R.color.textDarkest)

    Canvas(modifier = modifier) {
        val maxPossible = stats.outOf
        if (maxPossible <= 0.0) return@Canvas

        val range = 0.0..maxPossible
        val minScore = (stats.min ?: return@Canvas).coerceIn(range)
        val maxScore = (stats.max ?: return@Canvas).coerceIn(range)
        val meanScore = (stats.mean ?: return@Canvas).coerceIn(range)
        val youScore = stats.score.coerceIn(range)

        val sidePadding = 2.dp.toPx()
        val endMarkerHeight = 16.dp.toPx()
        val minMaxHeight = 16.dp.toPx()
        val scoreCircleRadius = 7.dp.toPx()
        val lineStroke = 2.dp.toPx()
        val darkStroke = 3.dp.toPx()
        val centerY = size.height / 2f
        val usableWidth = size.width - 2 * sidePadding

        fun positionFor(score: Double) = ((score / maxPossible) * usableWidth + sidePadding).toFloat()

        // Centerline
        drawLine(lineColor, Offset(sidePadding, centerY), Offset(size.width - sidePadding, centerY), lineStroke)
        // Left and right bounds
        drawLine(
            lineColor,
            Offset(sidePadding, centerY + endMarkerHeight / 2f),
            Offset(sidePadding, centerY - endMarkerHeight / 2f),
            lineStroke
        )
        drawLine(
            lineColor,
            Offset(size.width - sidePadding, centerY + endMarkerHeight / 2f),
            Offset(size.width - sidePadding, centerY - endMarkerHeight / 2f),
            lineStroke
        )

        val minX = positionFor(minScore)
        val maxX = positionFor(maxScore)
        val meanX = positionFor(meanScore)
        val youX = positionFor(youScore)

        // Darker line between min and max
        drawLine(darkLineColor, Offset(minX, centerY), Offset(maxX, centerY), darkStroke)
        drawLine(darkLineColor, Offset(minX, centerY + minMaxHeight / 2f), Offset(minX, centerY - minMaxHeight / 2f), darkStroke)
        drawLine(darkLineColor, Offset(maxX, centerY + minMaxHeight / 2f), Offset(maxX, centerY - minMaxHeight / 2f), darkStroke)
        drawLine(meanLineColor, Offset(meanX, centerY + endMarkerHeight / 2f), Offset(meanX, centerY - endMarkerHeight / 2f), darkStroke)

        // Your score
        drawCircle(accentColor, radius = scoreCircleRadius, center = Offset(youX, centerY))
    }
}

@Suppress("unused")
private val previewThemedColor: ThemedColor
    get() = ThemedColor(android.graphics.Color.RED)

@Preview(showBackground = true)
@Composable
private fun GradeCellGradedPreview() {
    if (LocalInspectionMode.current) {
        GradeCell(
            viewData = GradeCellViewData(
                courseColor = ThemedColor(android.graphics.Color.RED),
                submissionAndRubricLabelColor = android.graphics.Color.BLUE,
                state = GradeCellViewData.State.GRADED,
                chartPercent = 0.85f,
                showPointsLabel = true,
                score = "85",
                outOf = "Out of 100 pts"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeCellSubmittedPreview() {
    if (LocalInspectionMode.current) {
        GradeCell(
            viewData = GradeCellViewData(
                courseColor = ThemedColor(android.graphics.Color.RED),
                submissionAndRubricLabelColor = android.graphics.Color.BLUE,
                state = GradeCellViewData.State.SUBMITTED
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeCellEmptyPreview() {
    if (LocalInspectionMode.current) {
        GradeCell(
            viewData = GradeCellViewData(
                courseColor = ThemedColor(android.graphics.Color.RED),
                submissionAndRubricLabelColor = android.graphics.Color.BLUE,
                state = GradeCellViewData.State.EMPTY
            ),
            onClick = {}
        )
    }
}
