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
package com.instructure.parentapp.features.addstudent

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.parentapp.R

@Composable
fun AddStudentScreen(
    onPairingCodeClick: () -> Unit,
    onQrCodeClick: () -> Unit
) {
    CanvasTheme {
        Column(modifier = Modifier.padding(vertical = 16.dp).testTag("AddStudentOptions")) {
            Text(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                text = stringResource(id = R.string.addStudentTitle),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 14.sp
            )
            AddStudentButton(
                title = R.string.addStudentPairingCodeTitle,
                explanation = R.string.addStudentPairingCodeExplanation,
                icon = R.drawable.ic_keyboard_shortcut,
                onClick = onPairingCodeClick
            )
            AddStudentButton(
                title = R.string.addStudentQrCodeTitle,
                explanation = R.string.addStudentQrCodeExplanation,
                icon = R.drawable.ic_qr_code,
                onClick = onQrCodeClick
            )
        }
    }
}

@Composable
private fun AddStudentButton(
    @StringRes title: Int,
    @StringRes explanation: Int,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(end = 32.dp),
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = colorResource(id = R.color.textDark)
        )
        Column {
            Text(
                text = stringResource(id = title),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(id = explanation),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 14.sp
            )
        }
    }
}

@Preview
@Composable
fun AddStudentScreenPreview() {
    AddStudentScreen(onPairingCodeClick = {}, onQrCodeClick = {})
}
