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

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.BasicTextFieldWithHintDecoration
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.stringValueWithoutTrailingZeros
import kotlin.math.roundToInt

@Composable
fun SpeedGraderRubricContent(uiState: SpeedGraderRubricUiState) {
    when {
        uiState.loading -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.loading))
            }
        }

        uiState.error -> {
            //We don't show the rubric if there is an error
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.backgroundLight))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 14.dp).testTag("speedGraderRubricsLabel"),
                    text = stringResource(R.string.rubricsTitle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textDarkest)
                )

                uiState.criterions.forEach {
                    if (it.points != null) {
                        RubricCriterion(
                            rubricCriterion = it,
                            uiState.assessments[it.id],
                            uiState.hidePoints,
                            uiState.onRubricSelected,
                            uiState.onPointChanged,
                            uiState.onCommentChange
                        )
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
private fun RubricCriterion(
    rubricCriterion: RubricCriterion,
    assessment: RubricCriterionAssessment?,
    hidePoints: Boolean,
    onRubricSelected: (Double, String, String) -> Unit,
    onPointChanged: (Double, String) -> Unit,
    onCommentChange: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(assessment?.ratingId == null && hidePoints) }
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
                    color = colorResource(R.color.textDarkest),
                    modifier = Modifier.testTag("rubricCriterionDescription-${rubricCriterion.description.orEmpty()}")
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

                            rubricCriterion.ratings.forEachIndexed { index, rating ->
                                val selectedIndex =
                                    rubricCriterion.ratings.indexOfFirst { it.id == assessment?.ratingId }
                                val selected = assessment?.ratingId == rating.id
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
                                                useRange = rubricCriterion.useRange,
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
                                        val selected = assessment?.ratingId == rating.id
                                        PointValueBox(
                                            rating.id,
                                            point = rating.points?.roundToInt().toString(),
                                            selected = selected,
                                            useRange = rubricCriterion.useRange,
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

                            rubricCriterion.ratings.find { it.id == assessment?.ratingId }
                                ?.let { rating ->
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
        if (!hidePoints) {
            if (expanded) {
                CanvasDivider()
            }

            var enteredPoint by remember(assessment) { mutableStateOf(assessment?.points) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    stringResource(R.string.rubricScore),
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.textDarkest),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                BasicTextFieldWithHintDecoration(
                    modifier = Modifier.padding(end = 8.dp),
                    value = enteredPoint?.stringValueWithoutTrailingZeros,
                    onValueChange = { point ->
                        enteredPoint = point.toDoubleOrNull()
                        onPointChanged(enteredPoint.orDefault(), rubricCriterion.id)
                    },
                    hint = stringResource(R.string.rubricScoreHint),
                    textColor = LocalCourseColor.current,
                    hintColor = colorResource(R.color.textPlaceholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    )
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.pointsPossible,
                        rubricCriterion.points?.roundToInt().orDefault(),
                        rubricCriterion.points.orDefault()
                    ),
                    fontSize = 16.sp,
                    color = colorResource(R.color.textPlaceholder)
                )
            }
        }
        CanvasDivider()
        RubricNote(rubricCriterion.id, assessment, onCommentChange = onCommentChange)
    }
}

@Composable
private fun RubricNote(
    criterionId: String,
    assessment: RubricCriterionAssessment?,
    onCommentChange: (String, String) -> Unit
) {
    var editMode by remember { mutableStateOf(assessment?.comments.isNullOrEmpty()) }
    val haptic = LocalHapticFeedback.current
    Column {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = stringResource(R.string.rubricNote),
            color = colorResource(R.color.textDark)
        )
        AnimatedContent(
            targetState = editMode,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { height -> height } + fadeIn() togetherWith
                            slideOutHorizontally { height -> -height } + fadeOut()
                } else {
                    slideInHorizontally { height -> -height } + fadeIn() togetherWith
                            slideOutHorizontally { height -> height } + fadeOut()
                }
            },
            label = "EditModeAnimation"
        ) { targetEditMode ->
            if (targetEditMode) {
                RubricNoteField(
                    initialValue = assessment?.comments.orEmpty(),
                    onCommentChange = { comment ->
                        onCommentChange(comment, criterionId)
                        if (comment.isNotBlank()) {
                            editMode = false
                        }
                    }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, end = 8.dp)
                            .background(
                                color = colorResource(R.color.backgroundLight),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(16.dp),
                            text = assessment?.comments.orEmpty(),
                            color = colorResource(R.color.textDarkest),
                            lineHeight = 19.sp
                        )
                    }

                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            editMode = true
                        }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = stringResource(R.string.content_description_edit_rubric_comment),
                            tint = LocalCourseColor.current
                        )
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
    useRange: Boolean,
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

@Composable
private fun RubricNoteField(
    modifier: Modifier = Modifier,
    onCommentChange: (String) -> Unit,
    initialValue: String = ""
) {
    var commentText by remember(initialValue) { mutableStateOf(initialValue) }
    val haptic = LocalHapticFeedback.current
    OutlinedTextField(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .fillMaxWidth()
            .background(Color.Transparent),
        label = {
            Text(stringResource(R.string.rubricNoteHint))
        },
        shape = RoundedCornerShape(16.dp),
        value = commentText,
        maxLines = 5,
        onValueChange = {
            commentText = it
        },
        textStyle = TextStyle(
            fontSize = 14.sp,
            lineHeight = 17.sp,
            color = colorResource(id = R.color.textDarkest),
        ),
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = LocalCourseColor.current,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedLabelColor = colorResource(R.color.textDarkest),
            unfocusedLabelColor = colorResource(R.color.textDark),
            disabledLabelColor = colorResource(R.color.textDark),
            errorLabelColor = colorResource(R.color.textDark),
            focusedTrailingIconColor = LocalCourseColor.current,
            unfocusedTrailingIconColor = LocalCourseColor.current,
            focusedBorderColor = colorResource(R.color.textDarkest),
            unfocusedBorderColor = colorResource(R.color.textDark)
        ),
        trailingIcon = {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCommentChange(commentText)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send_outlined),
                    contentDescription = stringResource(R.string.a11y_sendRubricNoteContentDescription)
                )
            }
        }
    )
}

@Preview
@Composable
private fun SpeedGraderRubricScreenPreview() {
    CanvasTheme(courseColor = Color.Blue) {
        SpeedGraderRubricContent(
            uiState = SpeedGraderRubricUiState(
                assessments = mapOf(
                    "2" to RubricCriterionAssessment(
                        ratingId = "3",
                        points = 2.0,
                        comments = "This is a comment for the assessment."
                    )
                ),
                loading = false,
                onRubricSelected = { _, _, _ -> },
                onPointChanged = { _, _ -> },
                onCommentChange = { _, _ -> },
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
                        useRange = true,
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SpeedGraderTextRubricScreenPreview() {
    CanvasTheme(courseColor = Color.Yellow) {
        SpeedGraderRubricContent(
            uiState = SpeedGraderRubricUiState(
                assessments = mapOf(
                    "2" to RubricCriterionAssessment(
                        ratingId = "3",
                        points = 2.0,
                        comments = "This is a comment for the assessment"
                    )
                ),
                loading = false,
                hidePoints = true,
                onRubricSelected = { _, _, _ -> },
                onPointChanged = { _, _ -> },
                onCommentChange = { _, _ -> },
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