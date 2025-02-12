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
package com.instructure.parentapp.features.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.parentapp.R

@Composable
fun LoginBottomSheetDialogScreen(
    onHaveAccountClick: () -> Unit,
    onNotHaveAccountClick: () -> Unit
) {
    CanvasTheme {
        Column(
            modifier = Modifier
                .background(colorResource(R.color.backgroundLightest))
                .padding(vertical = 24.dp)
        ) {
            Text(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 20.dp),
                text = stringResource(id = R.string.select),
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDark)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHaveAccountClick()
                    }
                    .padding(start = 12.dp, end = 12.dp, bottom = 24.dp, top = 20.dp),
                text = stringResource(R.string.haveCanvasAccount),
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNotHaveAccountClick()
                    }
                    .padding(start = 12.dp, end = 12.dp, bottom = 24.dp, top = 24.dp),
                text = stringResource(R.string.notHaveCanvasAccount),
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest)
            )
        }
    }
}

@Preview
@Composable
fun LoginBottomSheetScreenPreview() {
    LoginBottomSheetDialogScreen(onHaveAccountClick = {}, onNotHaveAccountClick = {})
}
