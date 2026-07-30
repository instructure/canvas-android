/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.instui.compose.input

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUILink
import com.instructure.instui.token.component.InstUIRangeInput
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.component.InstUIBaseButton
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * InstUI RangeInput component — a horizontal slider with optional numeric end-value labels.
 *
 * Built on [androidx.compose.material3.Slider] with token-backed colors, a custom tall-thin
 * thumb bar matching the Figma design, and optional start/end labels styled as InstUI links.
 *
 * Usage:
 * ```
 * var progress by remember { mutableFloatStateOf(0.5f) }
 * RangeInput(
 *     value = progress,
 *     onValueChange = { progress = it },
 *     valueRange = 0f..100f,
 *     steps = 9,
 * )
 * ```
 *
 * @param value Current thumb position within [valueRange].
 * @param onValueChange Called continuously as the thumb moves.
 * @param modifier Modifier applied to the outer [Row].
 * @param valueRange The full range of the slider. Defaults to `0f..1f`.
 * @param steps Number of discrete steps between start and end (0 = continuous). Drives tick dots.
 * @param enabled Whether the slider responds to input.
 * @param showEndLabels Whether to render numeric labels at the start and end of the track.
 * @param startLabel Label shown at the left edge. Defaults to the start bound of [valueRange].
 * @param endLabel Label shown at the right edge. Defaults to the end bound of [valueRange].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangeInput(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    showEndLabels: Boolean = true,
    startLabel: String? = null,
    endLabel: String? = null,
) {
    val resolvedStartLabel = startLabel ?: formatBound(valueRange.start)
    val resolvedEndLabel = endLabel ?: formatBound(valueRange.endInclusive)

    val handleColor = InstUIRangeInput.handleBackground
    val trackColor = InstUIRangeInput.trackBackground
    val trackBorderColor = InstUIRangeInput.trackBorderColor

    val labelStyle = InstUIText.contentSmall.copy(
        color = InstUILink.textColor,
        fontSize = InstUILink.fontSizeSm,
        fontWeight = InstUILink.fontWeight,
        fontFamily = InstUILink.fontFamily,
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
    ) {
        if (showEndLabels) {
            Text(
                text = resolvedStartLabel,
                style = labelStyle,
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = handleColor,
                activeTrackColor = handleColor,
                inactiveTrackColor = trackColor,
                activeTickColor = trackBorderColor,
                inactiveTickColor = trackBorderColor,
                disabledThumbColor = trackBorderColor,
                disabledActiveTrackColor = trackBorderColor,
                disabledInactiveTrackColor = trackColor,
            ),
            thumb = {
                RangeInputThumb(enabled = enabled)
            },
            track = { sliderState ->
                RangeInputTrack(
                    sliderState = sliderState,
                    enabled = enabled,
                    activeColor = if (enabled) handleColor else trackBorderColor,
                    inactiveColor = trackColor,
                    borderColor = trackBorderColor,
                    activeTickColor = InstUIBaseButton.primaryColor,
                )
            },
        )

        if (showEndLabels) {
            Text(
                text = resolvedEndLabel,
                style = labelStyle,
            )
        }
    }
}

/**
 * Custom thumb: a tall, thin rounded bar matching the Figma handle design.
 * Width ~4dp, height ~44dp, corner radius [InstUILayoutSizes.BorderRadius.Xs.xs] (2dp).
 */
@Composable
private fun RangeInputThumb(enabled: Boolean) {
    val color = if (enabled) InstUIRangeInput.handleBackground else InstUIRangeInput.trackBorderColor
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(44.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(InstUILayoutSizes.BorderRadius.Xs.xs),
            ),
    )
}

/**
 * Custom track that faithfully reproduces the Figma design:
 * - INACTIVE portion: white fill ([inactiveColor]) with a 1dp [borderColor] stroke and rounded corners.
 * - ACTIVE portion: navy fill ([activeColor]) with rounded corners.
 * - TICK dots (when [sliderState].steps > 0): self-computed fractions (tickFractions is internal
 *   on SliderState), on-primary content-color dots on the active (navy) segment for contrast,
 *   [borderColor] grey dots on the inactive (white) segment.
 *
 * Track height is 16dp (per Figma; the 4dp tick dots sit centered with vertical padding). The
 * thin 44dp thumb extends above and below this track. No token exists for these dimensions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeInputTrack(
    sliderState: SliderState,
    enabled: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    borderColor: Color,
    activeTickColor: Color,
) {
    val trackHeight = 16.dp
    val borderWidth = 1.dp
    val tickRadius = 2.dp
    // Inner (near-thumb) corner radius — the far ends are fully rounded, the ends by the thumb are near-square.
    val innerRadius = InstUILayoutSizes.BorderRadius.Xs.xs
    // Half-gap on each side of the thumb, so the track is interrupted around it (matches Figma spaceSm padding).
    val thumbGap = InstUILayoutSizes.Spacing.SpaceSm.spaceSm
    // tickFractions is internal on SliderState — compute from steps ourselves.
    val tickFractions = remember(sliderState.steps) {
        if (sliderState.steps > 0) {
            (0..sliderState.steps + 1).map { it.toFloat() / (sliderState.steps + 1) }
        } else {
            emptyList()
        }
    }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(trackHeight),
    ) {
        val fullCorner = CornerRadius(size.height / 2f)
        val smallCorner = CornerRadius(innerRadius.toPx())
        val activeFraction = sliderState.coercedValueAsFraction.coerceIn(0f, 1f)
        // Thumb centre; the track is split into an active segment on the left and an inactive
        // segment on the right, with a gap around the thumb (interrupted track, per Figma).
        val thumbX = size.width * activeFraction
        val gap = thumbGap.toPx()
        val activeEnd = thumbX - gap
        val inactiveStart = thumbX + gap

        // 1. Active (navy) segment: fully rounded far (left) end, near-square end by the thumb.
        if (activeEnd > 0f) {
            val activePath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f, top = 0f, right = activeEnd, bottom = size.height,
                        topLeftCornerRadius = fullCorner, bottomLeftCornerRadius = fullCorner,
                        topRightCornerRadius = smallCorner, bottomRightCornerRadius = smallCorner,
                    )
                )
            }
            drawPath(activePath, color = activeColor)
        }

        // 2. Inactive (white) segment: near-square end by the thumb, fully rounded far (right) end.
        if (inactiveStart < size.width) {
            val inactivePath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = inactiveStart, top = 0f, right = size.width, bottom = size.height,
                        topLeftCornerRadius = smallCorner, bottomLeftCornerRadius = smallCorner,
                        topRightCornerRadius = fullCorner, bottomRightCornerRadius = fullCorner,
                    )
                )
            }
            drawPath(inactivePath, color = inactiveColor)
            drawPath(inactivePath, color = borderColor, style = Stroke(width = borderWidth.toPx()))
        }

        // 3. Tick dots at the thumb's snap positions (cx == thumb centre for that fraction, so the
        //    thumb always lands on a dot). The two endpoint ticks are omitted so the rounded track
        //    ends keep some breathing room; any dot inside the thumb gap is skipped.
        if (tickFractions.isNotEmpty()) {
            val dotRadius = tickRadius.toPx()
            val cy = size.height / 2f
            for (fraction in tickFractions) {
                if (fraction <= 0f || fraction >= 1f) continue
                val cx = size.width * fraction
                when {
                    // On-primary content color on the active (navy) segment.
                    cx <= activeEnd -> drawCircle(activeTickColor, dotRadius, Offset(cx, cy))
                    // Border-grey on the inactive (white) segment.
                    cx >= inactiveStart -> drawCircle(borderColor, dotRadius, Offset(cx, cy))
                    // Otherwise the dot falls under the thumb gap — skip it.
                }
            }
        }
    }
}

/**
 * Formats a float bound as an integer string when it has no fractional part,
 * otherwise shows one decimal place. Uses no hard-coded text.
 */
private fun formatBound(value: Float): String {
    return if (value == value.toLong().toFloat()) value.toLong().toString() else "%.1f".format(value)
}

// region Previews

@Preview(name = "RangeInput — Light", showBackground = true)
@Preview(name = "RangeInput — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RangeInputPositionsPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            // 0%
            var v0 by remember { mutableFloatStateOf(0f) }
            RangeInput(
                value = v0,
                onValueChange = { v0 = it },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
            )

            // 25%
            var v25 by remember { mutableFloatStateOf(25f) }
            RangeInput(
                value = v25,
                onValueChange = { v25 = it },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
            )

            // 50%
            var v50 by remember { mutableFloatStateOf(50f) }
            RangeInput(
                value = v50,
                onValueChange = { v50 = it },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
            )

            // 75%
            var v75 by remember { mutableFloatStateOf(75f) }
            RangeInput(
                value = v75,
                onValueChange = { v75 = it },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
            )

            // 100%
            var v100 by remember { mutableFloatStateOf(100f) }
            RangeInput(
                value = v100,
                onValueChange = { v100 = it },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "RangeInput No Labels — Light", showBackground = true)
@Preview(name = "RangeInput No Labels — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RangeInputNoLabelsPreview() {
    InstUITheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            // Continuous, no labels
            var v by remember { mutableFloatStateOf(0.4f) }
            RangeInput(
                value = v,
                onValueChange = { v = it },
                showEndLabels = false,
                modifier = Modifier.fillMaxWidth(),
            )

            // Disabled, with custom labels
            RangeInput(
                value = 0.6f,
                onValueChange = {},
                enabled = false,
                showEndLabels = true,
                startLabel = "Min",
                endLabel = "Max",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// endregion
