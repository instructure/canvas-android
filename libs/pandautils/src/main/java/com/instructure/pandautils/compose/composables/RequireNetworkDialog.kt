/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun rememberWithRequireNetwork(
    action: () -> Unit
): () -> Unit {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        NoInternetConnectionDialog(
            onDismissRequest = { showDialog = false }
        )
    }

    return remember(action) {
        {
            if (APIHelper.hasNetworkConnection()) {
                action()
            } else {
                showDialog = true
            }
        }
    }
}

@Composable
private fun NoInternetConnectionDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.noInternetConnectionTitle),
                color = colorResource(R.color.textDarkest)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.noInternetConnectionMessage),
                color = colorResource(R.color.textDarkest)
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.ok),
                    color = Color(ThemePrefs.textButtonColor)
                )
            }
        },
        backgroundColor = colorResource(R.color.backgroundLightestElevated)
    )
}
