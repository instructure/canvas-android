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

package com.instructure.pandautils.features.speedgrader.grade.rubric

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.utils.orDefault
import kotlin.math.roundToInt

@Composable
fun SpeedGraderRubricScreen() {

    val viewModel = hiltViewModel<SpeedGraderRubricViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    SpeedGraderRubricContent(
        uiState = uiState
    )
}

@Composable
fun SpeedGraderRubricContent(uiState: SpeedGraderRubricUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.backgroundLight))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 14.dp),
            text = stringResource(R.string.rubricsTitle),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.textDarkest)
        )

        uiState.criterions.forEach {
            if (it.points != null) {
                val selectedId = uiState.assessments[it.id]?.ratingId
                RubricCriterion(
                    rubricCriterion = it,
                    selectedId,
                    uiState.hidePoints,
                    uiState.onRubricClick
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
private fun RubricCriterion(
    rubricCriterion: RubricCriterion,
    selectedId: String?,
    hidePoints: Boolean,
    onRubricSelected: (Double, String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(selectedId == null && hidePoints) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(expanded) {
        rotation.animateTo(
            if (expanded) 180f else 0f,
            animationSpec = tween()
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.cardColors()
            .copy(containerColor = colorResource(R.color.backgroundLightestElevated))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = rubricCriterion.description.orEmpty(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textDarkest)
                )

                Icon(
                    modifier = Modifier.rotate(rotation.value),
                    painter = painterResource(R.drawable.ic_arrow_down),
                    tint = colorResource(R.color.textDarkest),
                    contentDescription = stringResource(R.string.content_description_expand_content)
                )
            }
            SharedTransitionLayout {
                AnimatedContent(expanded) { isExpanded ->
                    if (isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            rubricCriterion.longDescription?.let { longDescription ->
                                Text(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    text = longDescription,
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.textDarkest)
                                )
                            }

                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                Text(
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            LocalCourseColor.current,
                                            RoundedCornerShape(100.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    text = rubricCriterion.id,
                                    fontSize = 16.sp,
                                    color = LocalCourseColor.current,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            rubricCriterion.ratings.forEachIndexed { index, rating ->
                                val selectedIndex = rubricCriterion.ratings.indexOfFirst { it.id == selectedId }
                                val selected = selectedId == rating.id
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            onRubricSelected(
                                                rating.points.orDefault(),
                                                rubricCriterion.id,
                                                rating.id
                                            )
                                        }
                                        .fillMaxWidth()
                                        .background(
                                            if (selected) LocalCourseColor.current else colorResource(
                                                R.color.backgroundLightest
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        if (!hidePoints) {
                                            PointValueBox(
                                                rating.id,
                                                point = rating.points?.roundToInt().toString(),
                                                selected = selected,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@AnimatedContent
                                            )
                                        }
                                        RatingDescription(
                                            rating.description,
                                            rating.longDescription,
                                            selected = selected
                                        )
                                    }
                                    if (!selected && index != rubricCriterion.ratings.lastIndex && index != selectedIndex - 1) {
                                        CanvasDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        Column {
                            if (!hidePoints) {
                                FlowRow(
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 12.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    rubricCriterion.ratings.reversed().forEach { rating ->
                                        val selected = selectedId == rating.id
                                        PointValueBox(
                                            rating.id,
                                            point = rating.points?.roundToInt().toString(),
                                            selected = selected,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent,
                                            modifier = Modifier.clickable {
                                                onRubricSelected(
                                                    rating.points.orDefault(),
                                                    rubricCriterion.id,
                                                    rating.id
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            rubricCriterion.ratings.find { it.id == selectedId }?.let { rating ->
                                RatingDescription(
                                    rating.description,
                                    rating.longDescription,
                                    selected = true,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 14.dp,
                                    ),
                                    innerModifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingDescription(
    description: String,
    longDescription: String?,
    selected: Boolean,
    modifier: Modifier = Modifier,
    innerModifier: Modifier = Modifier
) {
    val backgroundColor =
        if (selected) LocalCourseColor.current else colorResource(R.color.backgroundLightest)
    val textColor =
        if (selected) colorResource(R.color.textLightest) else colorResource(R.color.textDarkest)
    Box(
        modifier = modifier
            .background(
                backgroundColor,
                shape = RoundedCornerShape(24.dp)
            ),
    ) {
        Column(
            modifier = innerModifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 8.dp),
                text = description,
                fontSize = 16.sp,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
            if (!longDescription.isNullOrEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp),
                    text = longDescription,
                    fontSize = 14.sp,
                    color = textColor,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PointValueBox(
    id: String,
    point: String,
    selected: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    with(sharedTransitionScope) {
        Box(
            modifier = modifier
                .sharedElement(
                    rememberSharedContentState(key = id),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(4.dp),
                    color = if (selected) LocalCourseColor.current else colorResource(R.color.textDark)
                )
                .background(
                    if (selected) LocalCourseColor.current else colorResource(
                        R.color.backgroundLightest
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .defaultMinSize(minWidth = 32.dp, minHeight = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = point,
                color = if (selected) colorResource(R.color.textLightest) else colorResource(
                    R.color.textDarkest
                ),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )
        }
    }
}

@Preview
@Composable
private fun SpeedGraderRubricScreenPreview() {
    CanvasTheme(courseColor = Color.Blue) {
        SpeedGraderRubricContent(
            uiState = SpeedGraderRubricUiState(
                assessments = mapOf("2" to RubricCriterionAssessment(
                    ratingId = "3",
                    points = 2.0,
                    comments = "This is a comment for the assessment."
                )),
                loading = false,
                onRubricClick = { _, _, _ -> },
                criterions = listOf(
                    RubricCriterion(
                        id = "1",
                        description = "Criterion 1",
                        longDescription = "This is a long description for criterion 1.",
                        points = 5.0,
                        ratings = listOf(
                            RubricRating(
                                id = "1",
                                description = "Rating 1",
                                points = 2.0,
                                longDescription = "This is a longer description for rating 1. that should be displayed when the criterion is expanded."
                            ),
                            RubricRating(
                                id = "2",
                                description = "Rating 2",
                                points = 3.0,
                                longDescription = "This is a long description for rating 2."
                            )
                        )
                    ),
                    RubricCriterion(
                        id = "2",
                        description = "Criterion 2",
                        longDescription = "This is a long description for criterion 2.",
                        points = 5.0,
                        ratings = listOf(
                            RubricRating(
                                id = "3",
                                description = "Rating 3",
                                points = 1.0,
                                longDescription = "This is a long description for rating 3."
                            ),
                            RubricRating(
                                id = "4",
                                description = "Rating 4",
                                points = 4.0,
                                longDescription = null
                            )
                        )
                    )
                )
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderTextRubricScreenPreview() {
    CanvasTheme(courseColor = Color.Blue) {
        SpeedGraderRubricContent(
            uiState = SpeedGraderRubricUiState(
                assessments = mapOf("2" to RubricCriterionAssessment(
                    ratingId = "3",
                    points = 2.0,
                    comments = "This is a comment for the assessment."
                )),
                loading = false,
                hidePoints = true,
                onRubricClick = { _, _, _ -> },
                criterions = listOf(
                    RubricCriterion(
                        id = "1",
                        description = "Criterion 1",
                        longDescription = "This is a long description for criterion 1.",
                        points = 5.0,
                        ratings = listOf(
                            RubricRating(
                                id = "1",
                                description = "Rating 1",
                                points = 2.0,
                                longDescription = "This is a longer description for rating 1. that should be displayed when the criterion is expanded."
                            ),
                            RubricRating(
                                id = "2",
                                description = "Rating 2",
                                points = 3.0,
                                longDescription = "This is a long description for rating 2."
                            )
                        )
                    ),
                    RubricCriterion(
                        id = "2",
                        description = "Criterion 2",
                        longDescription = "This is a long description for criterion 2.",
                        points = 5.0,
                        ratings = listOf(
                            RubricRating(
                                id = "3",
                                description = "Rating 3",
                                points = 1.0,
                                longDescription = "This is a long description for rating 3."
                            ),
                            RubricRating(
                                id = "4",
                                description = "Rating 4",
                                points = 4.0,
                                longDescription = null
                            )
                        )
                    )
                )
            )
        )
    }
}