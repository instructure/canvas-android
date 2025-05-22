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
package com.instructure.horizon.features.aiassistant.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType

data class AiAssistDetailedFeedbackOption(
    val label: String,
    val onClick: () -> Unit
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAssistDetailedFeedback(
    modifier: Modifier = Modifier,
    options: List<AiAssistDetailedFeedbackOption> = listOf(
        AiAssistDetailedFeedbackOption(stringResource(R.string.aiAssistFeedbackInappropriateOrOffensive)) {},
        AiAssistDetailedFeedbackOption(stringResource(R.string.aiAssistFeedbackinaccurate)) {},
        AiAssistDetailedFeedbackOption(stringResource(R.string.aiAssistFeedbackExposedPersonalInformation)) {},
    ),
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(HorizonCornerRadius.level2)
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.aiAssistFeedbackTitle),
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.surfaceColored(),
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                iconRes = R.drawable.close,
                contentDescription = stringResource(R.string.a11y_dismiss),
                onClick = onDismiss,
                color = IconButtonColor.BLACK_GHOST,
                size = IconButtonSize.SMALL
            )
        }

        Text(
            text = stringResource(R.string.aiAssistFeedbackMessage),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.surfaceColored(),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        FlowRow {
            options.forEach { option ->
                var isSelected by remember { mutableStateOf(false) }
                Pill(
                    label = option.label,
                    style = if (isSelected) PillStyle.SOLID else PillStyle.OUTLINE,
                    type = PillType.INVERSE,
                    case = PillCase.TITLE,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            isSelected = !isSelected
                            option.onClick()
                        },
                )
            }
        }

        var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
        AiAssistTextArea(
            label = stringResource(R.string.aiAssistFeedbackMessageLabel),
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            label = stringResource(R.string.aiAssistFeedbackSubmitLabel),
            onClick = { onSubmit(textFieldValue.text) },
            width = ButtonWidth.FILL,
            color = ButtonColor.Beige,
        )

        Text(
            text = stringResource(R.string.aiAssistFeedbackAcknowledgementMessage),
            style = HorizonTypography.p3,
            color = HorizonColors.Text.surfaceColored(),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
@Preview
private fun AiAssistDetailedFeedbackPreview() {
    ContextKeeper.appContext = LocalContext.current
    AiAssistDetailedFeedback(
        onSubmit = {},
        onDismiss = {}
    )
}