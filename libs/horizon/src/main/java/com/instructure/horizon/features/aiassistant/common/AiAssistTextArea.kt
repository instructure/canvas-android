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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun AiAssistTextArea(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.surfaceColored(),
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = HorizonColors.Icon.surfaceColored(),
                    shape = HorizonCornerRadius.level1_5,
                )
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                cursorBrush = SolidColor(HorizonColors.Text.surfaceColored()),
                textStyle = HorizonTypography.buttonTextLarge.copy(color = HorizonColors.Text.surfaceColored()),
                decorationBox = { TextAreaBox { it() } },
            )
        }
    }
}

@Composable
private fun TextAreaBox(innerTextField: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ){
        innerTextField()
    }
}

@Composable
@Preview
private fun AiAssistTextAreaPreview() {
    AiAssistTextArea(
        label = "Label",
        value = TextFieldValue(""),
        onValueChange = {}
    )
}