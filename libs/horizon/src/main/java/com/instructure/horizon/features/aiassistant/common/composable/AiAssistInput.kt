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

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor

@Composable
fun AiAssistInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSubmitPressed: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.aiAssistEnterAPromptLabel),
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        AiAssistTextArea(
            value = value,
            onValueChange = onValueChange,
            label = label,
            modifier = Modifier.weight(1f),
        )

        HorizonSpace(SpaceSize.SPACE_16)

        IconButton(
            iconRes = R.drawable.arrow_upward,
            contentDescription = stringResource(R.string.aiAssistSubmitPrompt),
            color = IconButtonColor.Inverse,
            onClick = onSubmitPressed,
        )
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