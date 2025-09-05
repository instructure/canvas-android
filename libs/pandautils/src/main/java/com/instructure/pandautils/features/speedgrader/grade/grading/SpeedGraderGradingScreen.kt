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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.common.primitives.Floats.max
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.convertScoreToLetterGrade
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.BasicTextFieldWithHintDecoration
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.RadioButtonText
import com.instructure.pandautils.compose.composables.TextDropdown
import com.instructure.pandautils.utils.getCheckpointTagString
import com.instructure.pandautils.utils.orDefault
import java.text.DecimalFormat
import java.util.Date
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt

private val numberFormatter = DecimalFormat("0.##")

@Composable
fun SpeedGraderGradingScreen() {

    val viewModel = hiltViewModel<SpeedGraderGradingViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    SpeedGraderGradingContent(uiState = uiState)
}

@Composable
fun SpeedGraderGradingContent(uiState: SpeedGraderGradingUiState) {
    when {
        uiState.error -> {
            ErrorContent(
                modifier = Modifier.fillMaxWidth(),
                errorMessage = stringResource(R.string.errorOccurred),
                retryClick = uiState.retryAction
            )
        }

        uiState.loading -> {
            Loading(modifier = Modifier.fillMaxWidth())
        }

        else -> {
            Column(
                modifier = Modifier
                    .imePadding()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                GradingContent(uiState)
                FinalScore(uiState)
            }
        }
    }
}

@Composable
private fun GradingContent(uiState: SpeedGraderGradingUiState) {
    if (uiState.checkpoints.isNotEmpty()) {
        uiState.checkpoints.forEach { checkpoint ->
            GradingInputBlock(uiState, GradableAssignment(
                tag = checkpoint.label,
                enteredGrade = checkpoint.enteredGrade,
                enteredScore = checkpoint.enteredScore,
                pointsPossible = checkpoint.pointsPossible,
                excused = checkpoint.excused,
                gradingStatus = checkpoint.gradingStatus,
                daysLate = checkpoint.daysLate
            ))
            CanvasDivider()
        }
    } else {
        GradingInputBlock(uiState, GradableAssignment(
            enteredGrade = uiState.enteredGrade,
            enteredScore = uiState.enteredScore,
            pointsPossible = uiState.pointsPossible,
            excused = uiState.excused,
            gradingStatus = uiState.gradingStatus,
            daysLate = uiState.daysLate
        ))
    }
}

@Composable
private fun GradingInputBlock(uiState: SpeedGraderGradingUiState, gradableAssignment: GradableAssignment) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
    ) {
        if (!gradableAssignment.tag.isNullOrEmpty()) {
            Text(
                text = gradableAssignment.tag.getCheckpointTagString(
                    ContextKeeper.appContext,
                    2 // TODO use actual replies needed when api is fixed
                ).orEmpty(),
                color = colorResource(R.color.textDark),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .testTag("speedGraderCheckpointLabel")
            )
        }

        when (uiState.gradingType) {
            GradingType.letter_grade -> {
                LetterGradeGradingTypeInput(
                    gradableAssignment,
                    uiState.letterGrades,
                    onScoreChange = { score -> uiState.onScoreChange(score, gradableAssignment.tag) },
                    onPercentageChange = { percentage -> uiState.onPercentageChange(percentage, gradableAssignment.tag) }
                )
            }

            GradingType.pass_fail -> {
                CompleteIncompleteGradingTypeInput(gradableAssignment) {
                    uiState.onScoreChange(it, gradableAssignment.tag)
                }
            }

            GradingType.percent -> {
                PercentageGradingTypeInput(gradableAssignment) {
                    uiState.onPercentageChange(it, gradableAssignment.tag)
                }
            }

            GradingType.not_graded -> {}

            GradingType.gpa_scale -> {
                LetterGradeGradingTypeInput(
                    gradableAssignment,
                    uiState.letterGrades,
                    onScoreChange = { score -> uiState.onScoreChange(score, gradableAssignment.tag) },
                    onPercentageChange = { percentage -> uiState.onPercentageChange(percentage, gradableAssignment.tag) }
                )
            }

            else -> {
                PointGradingTypeInput(gradableAssignment) {
                    uiState.onScoreChange(it, gradableAssignment.tag)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            if (uiState.gradingType != GradingType.pass_fail) {
                OutlinedButton(
                    enabled = gradableAssignment.enteredScore != null || gradableAssignment.excused,
                    onClick = { uiState.onScoreChange(null, gradableAssignment.tag) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("speedGraderNoGradeButton"),
                    border = BorderStroke(
                        1.dp,
                        LocalCourseColor.current.copy(alpha = if (gradableAssignment.enteredScore != null || gradableAssignment.excused) 1f else 0.5f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors().copy(
                        contentColor = LocalCourseColor.current,
                        disabledContentColor = LocalCourseColor.current.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(R.string.noGrade),
                        fontSize = 16.sp,
                        lineHeight = 19.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            OutlinedButton(
                enabled = gradableAssignment.excused.not(),
                onClick = { uiState.onExcuse(gradableAssignment.tag) },
                border = BorderStroke(
                    1.dp,
                    LocalCourseColor.current.copy(alpha = if (gradableAssignment.excused) 0.5f else 1f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("speedGraderExcuseButton"),
                colors = ButtonDefaults.outlinedButtonColors().copy(
                    contentColor = LocalCourseColor.current,
                    disabledContentColor = LocalCourseColor.current.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    stringResource(R.string.gradeExcused),
                    fontSize = 16.sp,
                    lineHeight = 19.sp
                )
            }
        }

        TextDropdown(
            modifier = Modifier.padding(top = 16.dp),
            options = uiState.gradingStatuses.map { it.name },
            selectedOption = gradableAssignment.gradingStatus
                ?: stringResource(R.string.gradingStatus_none),
            title = stringResource(R.string.status),
            onSelection = { selected ->
                val status = uiState.gradingStatuses.first { it.name == selected }
                uiState.onStatusChange(status, gradableAssignment.tag)
            },
            testTag = "speedGraderStatusDropdown",
            color = LocalCourseColor.current
        )

        gradableAssignment.daysLate?.let {
            LateHeader(it, uiState.submittedAt, uiState.onLateDaysChange)
        }
    }
}

@Composable
private fun FinalScore(uiState: SpeedGraderGradingUiState) {
    val enteredScore =
        uiState.enteredScore?.let { numberFormatter.format(uiState.enteredScore) } ?: "-"
    val pointsPossible =
        uiState.pointsPossible?.let { numberFormatter.format(uiState.pointsPossible) } ?: "-"
    val finalScore = uiState.score?.let { numberFormatter.format(uiState.score) } ?: "-"

    Card(
        modifier = Modifier.padding(vertical = 16.dp),
        colors = CardDefaults.cardColors().copy(
            containerColor = colorResource(R.color.backgroundLightestElevated)
        ),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.points),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 16.sp,
                    modifier = Modifier.testTag("finalGradePointsLabel")
                )

                Text(
                    text = pluralStringResource(
                        R.plurals.pointTotal,
                        uiState.pointsPossible?.toInt().orDefault(),
                        enteredScore,
                        pointsPossible
                    ),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 16.sp,
                    modifier = Modifier.testTag("finalGradePointsValue")
                )

            }
            CanvasDivider()

            if (uiState.pointsDeducted != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.speedGraderLatePenaltyTitle),
                        color = colorResource(id = R.color.textDanger),
                        fontSize = 16.sp,
                        modifier = Modifier.testTag("speedGraderLatePenaltyLabel")
                    )

                    Text(
                        text = pluralStringResource(
                            R.plurals.quantityPointsAbbreviated,
                            uiState.pointsDeducted.toInt().orDefault(),
                            numberFormatter.format(uiState.pointsDeducted)
                        ),
                        color = colorResource(R.color.textDanger),
                        fontSize = 16.sp,
                        modifier = Modifier.testTag("speedGraderLatePenaltyValue")
                    )
                }
                CanvasDivider()
            }


            val finalGrade = uiState.grade?.let { grade ->
                when (uiState.gradingType) {
                    GradingType.points -> {
                        pluralStringResource(
                            R.plurals.pointTotal,
                            uiState.pointsPossible?.toInt().orDefault(),
                            finalScore,
                            pointsPossible
                        )
                    }

                    GradingType.pass_fail -> {
                        when (grade) {
                            "complete" -> stringResource(R.string.gradeComplete)
                            "incomplete" -> stringResource(R.string.gradeIncomplete)
                            else -> stringResource(R.string.noGrade)
                        }
                    }

                    else -> grade
                }
            } ?: "-"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.finalGrade),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag("finalGradeLabel")
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = finalGrade,
                    color = colorResource(R.color.textDarkest),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag("finalGradeValue")
                )

                if (uiState.gradeHidden) {
                    Icon(
                        modifier = Modifier.padding(start = 8.dp),
                        painter = painterResource(R.drawable.ic_eye_off),
                        tint = colorResource(R.color.textDanger),
                        contentDescription = stringResource(R.string.gradesAreHidden)
                    )
                }
            }
        }
    }
}

@Composable
private fun LateHeader(
    daysLate: Float?,
    submissionDate: Date?,
    onLateDaysChange: (Float?) -> Unit
) {
    var textFieldValue by remember(daysLate) {
        mutableStateOf(
            TextFieldValue(
                text = numberFormatter.format(daysLate),
                selection = TextRange.Zero
            )
        )
    }
    var hasFocusedOnce by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.daysLate),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("speedGraderDaysLateLabel")
            )
            submissionDate?.let {
                Text(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("speedGraderDueDateValue"),
                    text = DateHelper.getDateAtTimeWithYearString(
                        LocalContext.current,
                        R.string.submitted_dateTime,
                        it
                    ).orEmpty(),
                    color = colorResource(R.color.textDark),
                    fontSize = 14.sp
                )
            }
        }

        BasicTextFieldWithHintDecoration(
            modifier = Modifier
                .testTag("speedGraderDaysLateValue")
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !hasFocusedOnce) {
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(0, textFieldValue.text.length)
                        )
                        hasFocusedOnce = true
                    }
                },
            hintColor = colorResource(R.color.textDark),
            textColor = LocalCourseColor.current,
            value = textFieldValue,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            onValueChange = {
                textFieldValue = it
                onLateDaysChange(it.text.toFloatOrNull())
            },
            hint = stringResource(R.string.daysLateHint)
        )
    }
}

@Composable
private fun LetterGradeGradingTypeInput(
    gradableAssignment: GradableAssignment,
    letterGrades: List<GradingSchemeRow>,
    onScoreChange: (Float?) -> Unit,
    onPercentageChange: (Float?) -> Unit
) {
    val options = letterGrades.map { it.name }
    val defaultItem =
        if (gradableAssignment.excused) stringResource(R.string.gradeExcused) else stringResource(R.string.not_graded)

    var textFieldScore by remember(gradableAssignment.enteredScore) {
        mutableStateOf(gradableAssignment.enteredScore?.let {
            numberFormatter.format(
                it
            )
        }.orEmpty())
    }

    var selectedGrade by remember(
        gradableAssignment.enteredScore,
        gradableAssignment.pointsPossible,
        letterGrades
    ) {
        mutableStateOf(
            gradableAssignment.enteredScore?.let {
                convertScoreToLetterGrade(
                    it.toDouble(),
                    gradableAssignment.pointsPossible.orDefault(),
                    letterGrades
                )
            } ?: defaultItem
        )
    }

    LaunchedEffect(textFieldScore) {
        val scoreAsFloat = textFieldScore.toFloatOrNull()
        if (scoreAsFloat != gradableAssignment.enteredScore) {
            onScoreChange(scoreAsFloat)
        }
    }

    LaunchedEffect(selectedGrade) {
        if (selectedGrade != gradableAssignment.enteredGrade) {
            onPercentageChange(
                letterGrades
                    .find { it.name == selectedGrade }
                    ?.value.orDefault()
                    .toFloat() * 100f
            )
        }
    }

    Column {
        if (letterGrades.isNotEmpty()) {
            TextDropdown(
                options = options,
                onSelection = { selectedGrade = it },
                title = stringResource(R.string.letterGrade),
                selectedOption = selectedGrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = LocalCourseColor.current,
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.grade),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.textDarkest),
                fontSize = 16.sp,
                modifier = Modifier.testTag("speedGraderCurrentGradeGradeLabel")
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicTextFieldWithHintDecoration(
                modifier = Modifier.padding(end = 8.dp).testTag("speedGraderCurrentGradeTextField"),
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
                    gradableAssignment.pointsPossible?.toInt().orDefault(),
                    gradableAssignment.pointsPossible.orDefault()
                ),
                fontSize = 16.sp,
                color = colorResource(R.color.textPlaceholder),
                modifier = Modifier.testTag("speedGraderPointsPossibleText")
            )
        }
    }
}

@Composable
private fun CompleteIncompleteGradingTypeInput(
    gradableAssignment: GradableAssignment,
    onScoreChange: (Float?) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var grade by remember { mutableStateOf(gradableAssignment.enteredGrade ?: "") }
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
                fontSize = 16.sp,
                modifier = Modifier.testTag("speedGraderCurrentGradeGradeLabel")
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(
                    R.string.completeIncompleteGradeScore,
                    gradableAssignment.enteredScore ?: 0f,
                    gradableAssignment.pointsPossible.orDefault()
                ),
                fontSize = 16.sp,
                color = colorResource(R.color.textPlaceholder),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("speedGraderCurrentPassFailPointsGradeText")
            )
        }
        RadioButtonText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.gradeComplete),
            selected = grade == "complete",
            color = LocalCourseColor.current,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                grade = "complete"
                onScoreChange(gradableAssignment.pointsPossible?.toFloat() ?: 0f)
            },
            testtag = "speedGraderCompleteRadioButton"
        )
        CanvasDivider()
        RadioButtonText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.gradeIncomplete),
            selected = grade == "incomplete",
            color = LocalCourseColor.current,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                grade = "incomplete"
                onScoreChange(0f)
            },
            testtag = "speedGraderIncompleteRadioButton"
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PercentageGradingTypeInput(gradableAssignment: GradableAssignment, onPercentageChange: (Float?) -> Unit) {
    val grade = gradableAssignment.enteredGrade?.replace("%", "").orEmpty()
    var sliderDrivenScore by remember { mutableFloatStateOf(grade.toFloatOrNull() ?: 0f) }
    var textFieldScore by remember(gradableAssignment.enteredGrade) { mutableStateOf(grade) }

    val maxScore by remember(gradableAssignment.enteredGrade) {
        mutableFloatStateOf(
            max(
                grade.toFloatOrNull() ?: 0f, 100f
            )
        )
    }
    val sliderState = remember(maxScore) {
        SliderState(
            value = sliderDrivenScore.coerceAtLeast(0f),
            valueRange = 0f..maxScore,
        )
    }

    LaunchedEffect(textFieldScore) {
        val scoreAsFloat = textFieldScore.toFloatOrNull()
        if (scoreAsFloat != gradableAssignment.enteredScore) {
            onPercentageChange(scoreAsFloat)
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
            onPercentageChange(sliderDrivenScore)
            textFieldScore = numberFormatter.format(sliderDrivenScore)
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.grade),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.textDarkest),
                fontSize = 16.sp,
                modifier = Modifier.testTag("speedGraderCurrentGradeGradeLabel")
            )
            Text(
                text = pluralStringResource(
                    R.plurals.percentGradeScore,
                    gradableAssignment.pointsPossible.orDefault().roundToInt(),
                    gradableAssignment.pointsPossible.orDefault()
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("speedGraderPercentagePossibleGradeLabel"),
                color = colorResource(R.color.textDark),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicTextFieldWithHintDecoration(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("speedGraderCurrentGradeTextField"),
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
            modifier = Modifier
                .padding(top = 16.dp)
                .testTag("speedGraderSlider"),
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
private fun PointGradingTypeInput(
    gradableAssignment: GradableAssignment,
    onScoreChange: (Float?) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val maxScore by remember(gradableAssignment.enteredScore) {
        mutableFloatStateOf(
            max(
                (gradableAssignment.pointsPossible?.toFloat() ?: 10f),
                gradableAssignment.enteredScore ?: 0f
            )
        )
    }

    val pointScale by remember(maxScore) {
        mutableFloatStateOf(
            when {
                maxScore <= 10.0 -> 4f
                maxScore <= 20.0 -> 2f
                else -> 1f
            }
        )
    }

    var sliderDrivenScore by remember(gradableAssignment.enteredScore) {
        mutableFloatStateOf(
            (gradableAssignment.enteredScore ?: 0f) * pointScale
        )
    }
    var textFieldScore by remember(gradableAssignment.enteredScore) {
        mutableStateOf(gradableAssignment.enteredScore?.let {
            numberFormatter.format(
                it
            )
        }.orEmpty())
    }

    val minScore by remember(gradableAssignment.enteredScore) {
        mutableFloatStateOf(
            min(
                gradableAssignment.enteredScore ?: 0f,
                0f
            )
        )
    }

    val sliderState = remember(uiState.enteredScore, maxScore, minScore) {
        SliderState(
            value = sliderDrivenScore,
            valueRange = minScore * pointScale..maxScore * pointScale,
            steps = ((maxScore - minScore).roundToInt() * pointScale.roundToInt() - 1).coerceAtLeast(
                1
            )
        )
    }

    LaunchedEffect(textFieldScore) {
        val scoreAsFloat = textFieldScore.toFloatOrNull()
        if (scoreAsFloat != gradableAssignment.enteredScore) {
            onScoreChange(scoreAsFloat)
        }
    }

    LaunchedEffect(gradableAssignment.enteredScore) {
        val newScore = gradableAssignment.enteredScore
        if (textFieldScore != newScore?.toString()) {
            textFieldScore = newScore?.let { numberFormatter.format(it) }.orEmpty()
        }
        if (sliderDrivenScore != (newScore ?: 0f)) {
            sliderDrivenScore = (newScore ?: 0f) * pointScale
            sliderState.value = sliderDrivenScore
        }
    }

    LaunchedEffect(sliderState.value) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val newScoreFromSlider = sliderState.value.roundToInt().toFloat() / pointScale
        if (sliderDrivenScore != newScoreFromSlider) {
            sliderDrivenScore = newScoreFromSlider
            onScoreChange(newScoreFromSlider)
            textFieldScore = numberFormatter.format(newScoreFromSlider)
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.grade),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.textDarkest),
                fontSize = 16.sp,
                modifier = Modifier.testTag("speedGraderCurrentGradeGradeLabel")
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicTextFieldWithHintDecoration(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .testTag("speedGraderCurrentGradeTextField"),
                value = textFieldScore,
                onValueChange = {
                    textFieldScore = it
                },
                hint = stringResource(R.string.pointGradeHint),
                hintColor = colorResource(R.color.textPlaceholder),
                textColor = LocalCourseColor.current,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
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
                    gradableAssignment.pointsPossible?.toInt().orDefault(),
                    gradableAssignment.pointsPossible.orDefault()
                ),
                fontSize = 16.sp,
                color = colorResource(R.color.textPlaceholder),
                modifier = Modifier.testTag("speedGraderPointsPossibleText")
            )
        }

        if (gradableAssignment.pointsPossible != null && gradableAssignment.pointsPossible != 0.0 && (gradableAssignment.enteredScore ?: 0f) <= 100.0) {
            Slider(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .testTag("speedGraderSlider"),
                state = sliderState,
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
}

@Preview
@Composable
private fun SpeedGraderGradingContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme(courseColor = Color.Magenta) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                excused = true,
                loading = false,
                pointsPossible = 20.0,
                enteredGrade = "15",
                enteredScore = 15.0f,
                grade = "14",
                score = 14.0,
                pointsDeducted = 1.0,
                onScoreChange = { _, _ -> },
                submittedAt = Date(),
                daysLate = 4f,
                gradingType = GradingType.points,
                onPercentageChange = { _, _ -> },
                onExcuse = {},
                onStatusChange = { _, _ -> },
                onLateDaysChange = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentPercentagePreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme(courseColor = Color.Blue) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                gradeHidden = true,
                loading = false,
                pointsPossible = 20.0,
                enteredGrade = "90%",
                enteredScore = 18f,
                grade = "90%",
                score = 15.0,
                onScoreChange = { _, _ -> },
                submittedAt = Date(),
                daysLate = 4f,
                gradingType = GradingType.percent,
                onPercentageChange = { _, _ -> },
                onExcuse = {},
                onStatusChange = { _, _ -> },
                onLateDaysChange = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentCompleteIncompletePreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme(courseColor = Color.Green) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                loading = false,
                pointsPossible = 20.0,
                enteredGrade = "incomplete",
                enteredScore = 0f,
                grade = "A",
                score = 15.0,
                submittedAt = Date(),
                daysLate = 4f,
                onScoreChange = { _, _ -> },
                gradingType = GradingType.pass_fail,
                onPercentageChange = { _, _ -> },
                onExcuse = {},
                onStatusChange = { _, _ -> },
                onLateDaysChange = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentLetterGraderPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme(courseColor = Color.Red) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                loading = false,
                pointsPossible = 20.5,
                enteredGrade = "A",
                enteredScore = 0f,
                grade = null,
                score = 15.0,
                onScoreChange = { _, _ -> },
                gradingType = GradingType.letter_grade,
                onPercentageChange = { _, _ -> },
                pointsDeducted = 2.0,
                submittedAt = Date(),
                daysLate = 4f,
                letterGrades = listOf(
                    GradingSchemeRow("A", 90.0),
                    GradingSchemeRow("B", 80.0),
                    GradingSchemeRow("C", 70.0),
                    GradingSchemeRow("D", 60.0),
                    GradingSchemeRow("F", 0.0)
                ),
                onExcuse = {},
                onStatusChange = { _, _ -> },
                onLateDaysChange = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme(courseColor = Color.Red) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                error = true,
                loading = false,
                onScoreChange = { _, _ -> },
                onPercentageChange = { _, _ -> },
                onExcuse = {},
                onStatusChange = { _, _ -> },
                onLateDaysChange = {},
                retryAction = {}
            )
        )
    }
}

@Preview
@Composable
private fun SpeedGraderGradingContentLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    CanvasTheme(courseColor = Color.Red) {
        SpeedGraderGradingContent(
            SpeedGraderGradingUiState(
                error = false,
                loading = true,
                onScoreChange = { _, _ -> },
                onPercentageChange = { _, _ -> },
                onExcuse = {},
                onStatusChange = { _, _ -> },
                onLateDaysChange = {}
            )
        )
    }
}