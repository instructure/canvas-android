/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R

@Composable
fun TextFieldWithHeader(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    enabled: Boolean,
    headerEnabled: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes headerIconResource: Int? = null,
    iconContentDescription: String? = null,
    onIconClick: (() -> Unit)? = null,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    Column(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        TextFieldHeader(
            label = label,
            enabled = headerEnabled,
            headerIconResource = headerIconResource,
            iconContentDescription = iconContentDescription,
            onIconClick = onIconClick,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusRequester.requestFocus()
                }
        )

        CanvasThemedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp)
                .focusRequester(focusRequester)
                .testTag("textFieldWithHeaderTextField")
        )
    }
}

@Composable
private fun TextFieldHeader(
    label: String,
    enabled: Boolean,
    @DrawableRes headerIconResource: Int?,
    iconContentDescription: String?,
    onIconClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = colorResource(id = R.color.textDarkest),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        
        Spacer(Modifier.weight(1f))

        headerIconResource?.let { icon ->
            IconButton(
                enabled = enabled,
                onClick = { onIconClick?.invoke() },
                modifier = Modifier
                    .size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = iconContentDescription,
                    tint = colorResource(id = R.color.textDarkest)
                )
            }
        }
    }
}

@Composable
@Preview
fun TextFieldWithHeaderPreview() {
    ContextKeeper.appContext = LocalContext.current

    TextFieldWithHeader(
        label = "Label",
        value = TextFieldValue("Some text"),
        headerIconResource = R.drawable.ic_attachment,
        enabled = true,
        headerEnabled = true,
        onValueChange = {}
    )
}