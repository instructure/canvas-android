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
package com.instructure.pandautils.features.speedgrader.grade.grading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.common.primitives.Floats.max
import com.instructure.canvasapi2.type.GradingType
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.BasicTextFieldWithHintDecoration
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.Dropdown
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.RadioButtonText
import com.instructure.pandautils.utils.orDefault
import java.text.DecimalFormat
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun SpeedGraderGradingScreen() {

    val viewModel = hiltViewModel<SpeedGraderGradingViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.loading -> {
            Loading()
        }

        else -> {
            SpeedGraderGradingContent(uiState)
        }
    }
}

@Composable
fun SpeedGraderGradingContent(uiState: SpeedGraderGradingUiState) {
    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            when (uiState.gradingType) {
                GradingType.letter_grade -> {
                    LetterGradeGradingTypeInput(uiState)
                }

                GradingType.pass_fail -> {
                    CompleteIncompleteGradingTypeInput(uiState)
                }

                GradingType.percent -> {
                    PercentageGradingTypeInput(uiState)
                }

                GradingType.not_graded -> {}

                else -> {
                    PointGradingTypeInput(uiState)
                }
            }
        }
    }
}

@Composable
private fun LetterGradeGradingTypeInput(uiState: SpeedGraderGradingUiState) {
    val haptic = LocalHapticFeedback.current

    var selectedGrade by remember { mutableStateOf(uiState.enteredGrade ?: "No Grade") }

    Column {
        if (uiState.letterGrades.isNotEmpty()) {
            Dropdown(
                selectedIndex = uiState.letterGrades.indexOfFirst { it.letterGrade == selectedGrade },
                options = uiState.letterGrades.map { it.letterGrade } + "No Grade",
                onOptionSelected = {
                    uiState.onPercentageChange(uiState.letterGrades[it - 1].baseValue.toFloat() * 100f)
                }
            )
        }
        PointGradingTypeInput(uiState)
    }
}


@Composable
private fun CompleteIncompleteGradingTypeInput(uiState: SpeedGraderGradingUiState) {
    val haptic = LocalHapticFeedback.current
    var grade by remember { mutableStateOf(uiState.enteredGrade ?: "") }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                stringResource(R.string.grade),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.textDarkest),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(
                    R.string.completeIncompleteGradeScore,
                    uiState.enteredScore ?: 0f,
                    uiState.pointsPossible.orDefault()
                ),
                fontSize = 16.sp,
                color = colorResource(R.color.textPlaceholder),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        RadioButtonText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.noGrade),
            selected = grade.isBlank(),
            color = LocalCourseColor.current,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        )
        CanvasDivider()
        RadioButtonText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.gradeComplete),
            selected = grade == "complete",
            color = LocalCourseColor.current,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                grade = "complete"
                uiState.onScoreChange(uiState.pointsPossible?.toFloat() ?: 0f)
            })
        CanvasDivider()
        RadioButtonText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.gradeIncomplete),
            selected = grade == "incomplete",
            color = LocalCourseColor.current,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                grade = "incomplete"
                uiState.onScoreChange(0f)
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PercentageGradingTypeInput(uiState: SpeedGraderGradingUiState) {
    val numberFormatter = DecimalFormat("0.#")
    val grade = uiState.enteredGrade?.replace("%", "").orEmpty()
    var sliderDrivenScore by remember { mutableFloatStateOf(grade.toFloatOrNull() ?: 0f) }
    var textFieldScore by remember { mutableStateOf(grade) }

    val maxScore = 100f
    val sliderState = rememberSliderState(
        value = sliderDrivenScore.coerceAtLeast(0f),
        valueRange = 0f..maxScore,
    )

    LaunchedEffect(textFieldScore) {
        val scoreAsFloat = textFieldScore.toFloatOrNull()
        if (scoreAsFloat != uiState.enteredScore) {
            uiState.onPercentageChange(scoreAsFloat)
        }
    }

    LaunchedEffect(grade) {
        val newScore = grade.toFloatOrNull()
        if (textFieldScore != newScore?.toString()) {
            textFieldScore = newScore?.let { numberFormatter.format(it) }.orEmpty()
        }
        if (sliderDrivenScore != (newScore ?: 0f)) {
            sliderDrivenScore = newScore ?: 0f
            sliderState.value = (newScore ?: 0f).coerceAtLeast(0f)
        }
    }

    LaunchedEffect(sliderState.value) {
        val newScoreFromSlider = sliderState.value
        if (sliderDrivenScore != newScoreFromSlider) {
            sliderDrivenScore = round(newScoreFromSlider)
            uiState.onPercentageChange(sliderDrivenScore)
            textFieldScore = numberFormatter.format(sliderDrivenScore)
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.grade),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.textDarkest),
                fontSize = 16.sp
            )
            Text(
                text = pluralStringResource(
                    R.plurals.percentGradeScore,
                    uiState.pointsPossible.orDefault().roundToInt(),
                    uiState.pointsPossible.orDefault()
                ),
                modifier = Modifier.padding(start = 8.dp),
                color = colorResource(R.color.textDark),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicTextFieldWithHintDecoration(
                modifier = Modifier
                    .padding(end = 8.dp),
                value = textFieldScore,
                onValueChange = {
                    textFieldScore = it
                },
                hint = stringResource(R.string.percentageGradeHint),
                hintColor = colorResource(R.color.textPlaceholder),
                textColor = LocalCourseColor.current,
            )
            Text(
                text = "%",
                fontSize = 16.sp,
                color = colorResource(R.color.textPlaceholder)
            )
        }

        Slider(
            modifier = Modifier.padding(top = 16.dp),
            state = sliderState,
            colors = SliderDefaults.colors(
                thumbColor = LocalCourseColor.current,
                activeTrackColor = LocalCourseColor.current,
                inactiveTrackColor = LocalCourseColor.current.copy(alpha = 0.2f),
                inactiveTickColor = LocalCourseColor.current
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PointGradingTypeInput(uiState: SpeedGraderGradingUiState) {
    val haptic = LocalHapticFeedback.current
    var sliderDrivenScore by remember { mutableFloatStateOf(uiState.enteredScore ?: 0f) }
    var textFieldScore by remember { mutableStateOf(uiState.enteredScore?.toString() ?: "") }

    val maxScore by remember {
        mutableFloatStateOf(
            max(
                (uiState.pointsPossible?.toFloat() ?: 10f),
                uiState.enteredScore ?: 0f
            )
        )
    }
    val sliderState = rememberSliderState(
        value = sliderDrivenScore.coerceAtLeast(0f),
        valueRange = 0f..maxScore,
        steps = maxScore.roundToInt() - 1,
    )

    LaunchedEffect(textFieldScore) {
        val scoreAsFloat = textFieldScore.toFloatOrNull()
        if (scoreAsFloat != uiState.enteredScore) {
            uiState.onScoreChange(scoreAsFloat)
        }
    }

    LaunchedEffect(uiState.enteredScore) {
        val newScore = uiState.enteredScore
        if (textFieldScore != newScore?.toString()) {
            textFieldScore = newScore?.toString() ?: ""
        }
        if (sliderDrivenScore != (newScore ?: 0f)) {
            sliderDrivenScore = newScore ?: 0f
            sliderState.value = (newScore ?: 0f).coerceAtLeast(0f)
        }
    }

    LaunchedEffect(sliderState.value) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val newScoreFromSlider = sliderState.value.roundToInt().toFloat()
        if (sliderDrivenScore != newScoreFromSlider) {
            sliderDrivenScore = newScoreFromSlider
            uiState.onScoreChange(newScoreFromSlider)
            textFieldScore = newScoreFromSlider.toString()
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.grade),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.textDarkest),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicTextFieldWithHintDecoration(
                modifier = Modifier
                    .padding(end = 8.dp),
                value = textFieldScore,
                onValueChange = {
                    textFieldScore = it
                },
                hint = stringResource(R.string.pointGradeHint),
                hintColor = colorResource(R.color.textPlaceholder),
                textColor = LocalCourseColor.current,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                decorationText = pluralStringResource(
                    R.plurals.pointsPts,
                    textFieldScore.toFloatOrNull()?.toInt().orDefault()
                )
            )

            Text(
                text = pluralStringResource(
                    R.plurals.pointsPossible,
                    uiState.pointsPossible?.toInt().orDefault(),
                    uiState.pointsPossible.orDefault()
                ),
                fontSize = 16.sp,
                color = colorResource(R.color.textPlaceholder)
            )
        }

        Slider(
            modifier = Modifier.padding(top = 16.dp), state = sliderState,
            colors = SliderDefaults.colors(
                thumbColor = LocalCourseColor.current,
                activeTrackColor = LocalCourseColor.current,
                inactiveTrackColor = LocalCourseColor.current.copy(alpha = 0.2f),
                inactiveTickColor = LocalCourseColor.current,
                activeTickColor = colorResource(R.color.textLightest)
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentPreview() {
    CanvasTheme(courseColor = Color.Magenta) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                pointsPossible = 20.0,
                enteredGrade = "A",
                enteredScore = 15.0f,
                grade = "A",
                score = 15.0,
                onScoreChange = {},
                gradingType = GradingType.points,
                onPercentageChange = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentPercentagePreview() {
    CanvasTheme(courseColor = Color.Blue) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                pointsPossible = 20.0,
                enteredGrade = "95.5%",
                enteredScore = 18f,
                grade = "A",
                score = 15.0,
                onScoreChange = {},
                gradingType = GradingType.percent,
                onPercentageChange = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentCompleteIncompletePreview() {
    CanvasTheme(courseColor = Color.Green) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                pointsPossible = 20.0,
                enteredGrade = "incomplete",
                enteredScore = 0f,
                grade = "A",
                score = 15.0,
                onScoreChange = {},
                gradingType = GradingType.pass_fail,
                onPercentageChange = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentLetterGraderPreview() {
    CanvasTheme(courseColor = Color.Red) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                pointsPossible = 20.0,
                enteredGrade = "incomplete",
                enteredScore = 0f,
                grade = "A",
                score = 15.0,
                onScoreChange = {},
                gradingType = GradingType.letter_grade,
                onPercentageChange = {},
                letterGrades = listOf(
                    LetterGrade(0.0, "F"),
                    LetterGrade(0.6, "D"),
                    LetterGrade(0.7, "C"),
                    LetterGrade(0.8, "B"),
                    LetterGrade(0.9, "A")
                )
            )
        )
    }
}




