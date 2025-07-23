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
package com.instructure.horizon.features.inbox

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor

@Composable
fun HorizonInboxExitConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    title: String = stringResource(R.string.exitConfirmationTitle),
    text: String = stringResource(R.string.exitConfirmationMessage),
    confirmButtonLabel: String = stringResource(R.string.exitConfirmationExitButtonLabel),
    cancelButtonLabel: String = stringResource(R.string.exitConfirmationCancelButtonLabel),
) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(
                label = confirmButtonLabel,
                onClick = onConfirm,
                color = ButtonColor.Institution,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
        },
        dismissButton = {
            Button(
                label = cancelButtonLabel,
                onClick = onCancel,
                color = ButtonColor.Inverse,
            )
        },
        title = {
            Text(
                title,
                style = HorizonTypography.h3,
                color = HorizonColors.Text.title()
            )
        },
        text = {
            Text(
                text,
                style = HorizonTypography.p2,
                color = HorizonColors.Text.body()
            )
        },
        shape = HorizonCornerRadius.level4,
        backgroundColor = HorizonColors.Surface.pagePrimary()
    )
}

@Composable
@Preview
private fun HorizonInboxExitConfirmationDialogPreview() {
    HorizonInboxExitConfirmationDialog( { }, { } )
}