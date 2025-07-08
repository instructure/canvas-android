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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.CanvasDivider
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
                PointCriterion(rubricCriterion = it)
            } else {
                RatingCriterion(rubricCriterion = it)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
private fun PointCriterion(rubricCriterion: RubricCriterion) {
    var expanded by remember { mutableStateOf(false) }
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
                    .padding(vertical = 12.dp, horizontal = 16.dp),
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
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rubricCriterion.longDescription?.let { longDescription ->
                                Text(
                                    text = longDescription,
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.textDarkest)
                                )
                            }

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
                                fontWeight = FontWeight.SemiBold
                            )

                            rubricCriterion.ratings.forEach { rating ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .sharedElement(
                                                    rememberSharedContentState(key = rating.id),
                                                    animatedVisibilityScope = this@AnimatedContent
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = colorResource(R.color.textDark)
                                                )
                                                .defaultMinSize(
                                                    minWidth = 32.dp,
                                                    minHeight = 32.dp
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = rating.points?.roundToInt().toString(),
                                                color = colorResource(R.color.textDarkest),
                                                textAlign = TextAlign.Center,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                )
                                            )
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .padding(start = 8.dp),
                                                text = rating.description,
                                                fontSize = 16.sp,
                                                color = colorResource(R.color.textDarkest),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (!rating.longDescription.isNullOrEmpty()) {
                                                Text(
                                                    modifier = Modifier
                                                        .padding(start = 8.dp),
                                                    text = rating.longDescription,
                                                    fontSize = 14.sp,
                                                    color = colorResource(R.color.textDarkest)
                                                )
                                            }
                                        }
                                    }
                                    CanvasDivider(modifier = Modifier.padding(top = 14.dp))
                                }
                            }
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            rubricCriterion.ratings.reversed().forEach { rating ->
                                Box(
                                    modifier = Modifier
                                        .sharedElement(
                                            rememberSharedContentState(key = rating.id),
                                            animatedVisibilityScope = this@AnimatedContent
                                        )
                                        .border(
                                            width = 1.dp,
                                            shape = RoundedCornerShape(4.dp),
                                            color = colorResource(R.color.textDark)
                                        )
                                        .defaultMinSize(minWidth = 32.dp, minHeight = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rating.points?.roundToInt().toString(),
                                        color = colorResource(R.color.textDarkest),
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
                    }
                }

            }
        }
    }
}

@Composable
private fun RatingCriterion(rubricCriterion: RubricCriterion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {

    }
}

@Preview
@Composable
private fun SpeedGraderRubricScreenPreview() {
    CanvasTheme(courseColor = Color.Blue) {
        SpeedGraderRubricContent(
            uiState = SpeedGraderRubricUiState(
                loading = false,
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
                                longDescription = "This is a long description for rating 1."
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
                        points = null,
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
                                longDescription = "This is a long description for rating 4."
                            )
                        )
                    )
                )
            )
        )
    }
}