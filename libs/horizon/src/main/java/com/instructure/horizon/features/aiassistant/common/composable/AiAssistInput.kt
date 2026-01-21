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
package com.instructure.horizon.features.aiassistant.common.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize

@Composable
fun AiAssistInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSubmitPressed: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.aiAssistEnterAPromptLabel),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                HorizonColors.Surface.cardPrimary(),
                HorizonCornerRadius.level2
            )
    ) {
        AiAssistTextArea(
            modifier = Modifier
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            onSend = { onSubmitPressed() }
        )

        HorizonDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                iconRes = R.drawable.send_filled,
                contentDescription = stringResource(R.string.aiAssistSubmitPrompt),
                color = IconButtonColor.Black,
                size = IconButtonSize.SMALL,
                enabled = value.text.isNotBlank(),
                onClick = onSubmitPressed,
            )
        }
    }
}

@Composable
@Preview
private fun AiAssistInputPreview() {
    ContextKeeper.appContext = LocalContext.current
    AiAssistInput(
        value = TextFieldValue(""),
        onValueChange = {},
        onSubmitPressed = {},
        modifier = Modifier,
    )
}