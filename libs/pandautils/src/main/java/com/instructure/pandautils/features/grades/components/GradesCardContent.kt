/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.container.Card as InstUICard
import com.instructure.instui.compose.container.Elevation
import com.instructure.instui.compose.list.ListItem
import com.instructure.instui.compose.list.ListItemTrailing
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.Lock
import com.instructure.pandautils.R
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.LocalDesignSystem
import com.instructure.pandautils.features.grades.GradesUiState
import com.instructure.pandautils.utils.drawableId
import androidx.compose.material.Card as MaterialCard

@Composable
fun GradesCardContent(
    uiState: GradesUiState,
    contextColor: Int,
    shouldShowNewText: Boolean,
    modifier: Modifier = Modifier,
) {
    when (LocalDesignSystem.current) {
        DesignSystem.Legacy -> LegacyGradesCard(uiState, contextColor, shouldShowNewText, modifier)
        DesignSystem.InstUI -> InstUIGradesCard(uiState, modifier)
    }
}

@Composable
private fun InstUIGradesCard(
    uiState: GradesUiState,
    modifier: Modifier = Modifier,
) {
    InstUICard(
        elevation = Elevation.Level1,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (uiState.isGradeLocked) {
            ListItem(
                title = stringResource(id = R.string.gradeLockedContentDescription),
                trailing = ListItemTrailing.Icons(
                    icons = listOf(InstUIIcons.Line.Lock),
                ),
            )
        } else {
            ListItem(
                title = stringResource(id = R.string.gradesTotal),
                trailing = ListItemTrailing.TextOnly(
                    value = uiState.gradeText,
                ),
            )
        }
    }
}

@Composable
private fun LegacyGradesCard(
    uiState: GradesUiState,
    contextColor: Int,
    shouldShowNewText: Boolean,
    modifier: Modifier = Modifier,
) {
    MaterialCard(
        modifier = modifier
            .semantics(true) {}
            .fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = if (uiState.showWhatIfScore) {
            Color(color = contextColor)
        } else {
            colorResource(id = R.color.backgroundLightestElevated)
        },
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val whatIf = uiState.showWhatIfScore
            val onlyGraded = uiState.onlyGradedAssignmentsSwitchEnabled
            AnimatedContent(
                targetState = shouldShowNewText && (onlyGraded || whatIf),
                label = "GradeCardTextAnimation",
                transitionSpec = {
                    if (targetState) {
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    } else {
                        slideInVertically { -it } togetherWith slideOutVertically { it }
                    }
                }
            ) {
                Text(
                    text = when {
                        !it -> stringResource(id = R.string.gradesTotal)
                        whatIf && onlyGraded -> stringResource(id = R.string.gradesBasedOnGradedAndWhatIf)
                        whatIf -> stringResource(id = R.string.whatIfScoreLabel)
                        onlyGraded -> stringResource(id = R.string.gradesBasedOnGraded)
                        else -> stringResource(id = R.string.gradesTotal)
                    },
                    fontSize = 14.sp,
                    color = if (uiState.showWhatIfScore) {
                        colorResource(id = R.color.textLightest)
                    } else {
                        colorResource(id = R.color.textDark)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .testTag("gradesCardText")
                )
            }

            if (uiState.isGradeLocked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock_lined),
                    contentDescription = stringResource(id = R.string.gradeLockedContentDescription),
                    tint = if (uiState.showWhatIfScore) {
                        colorResource(id = R.color.textLightest)
                    } else {
                        colorResource(id = R.color.textDarkest)
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { drawableId = R.drawable.ic_lock_lined }
                )
            } else {
                Text(
                    text = uiState.gradeText,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Right,
                    color = if (uiState.showWhatIfScore) {
                        colorResource(id = R.color.textLightest)
                    } else {
                        colorResource(id = R.color.textDarkest)
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag("totalGradeScoreText")
                )
            }
        }
    }
}

private val previewState = GradesUiState(
    isLoading = false,
    gradeText = "89% B+",
    onlyGradedAssignmentsSwitchEnabled = true,
)

@Preview(name = "GradesCardContent Legacy", showBackground = true)
@Composable
private fun GradesCardContentLegacyPreview() {
    CompositionLocalProvider(LocalDesignSystem provides DesignSystem.Legacy) {
        GradesCardContent(
            uiState = previewState,
            contextColor = 0xFF00828E.toInt(),
            shouldShowNewText = false,
        )
    }
}

@Preview(name = "GradesCardContent InstUI — Light", showBackground = true)
@Preview(name = "GradesCardContent InstUI — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradesCardContentInstUIPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            GradesCardContent(
                uiState = previewState,
                contextColor = 0xFF00828E.toInt(),
                shouldShowNewText = false,
            )
        }
    }
}

@Preview(name = "GradesCardContent InstUI Locked", showBackground = true)
@Composable
private fun GradesCardContentInstUILockedPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            GradesCardContent(
                uiState = previewState.copy(isGradeLocked = true),
                contextColor = 0xFF00828E.toInt(),
                shouldShowNewText = false,
            )
        }
    }
}