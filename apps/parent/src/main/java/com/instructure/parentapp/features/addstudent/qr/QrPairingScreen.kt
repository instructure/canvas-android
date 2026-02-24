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
package com.instructure.parentapp.features.addstudent.qr

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.AddStudentAction
import com.instructure.parentapp.features.addstudent.AddStudentUiState

@Composable
fun QrPairingScreen(
    uiState: AddStudentUiState,
    onNextClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    CanvasTheme {
        CanvasScaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasAppBar(
                    title = stringResource(
                        id = if (uiState.isError) {
                            R.string.studentPairing
                        } else {
                            R.string.qrPairingTitleCanvas
                        }
                    ),
                    navigationActionClick = onBackClicked,
                    actions = {
                        if (!uiState.isError) {
                            TextButton(onClick = onNextClicked) {
                                Text(
                                    text = stringResource(id = R.string.next),
                                    color = colorResource(id = R.color.textInfo),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when {
                    uiState.isLoading -> {
                        Loading(modifier = Modifier.fillMaxSize())
                    }

                    uiState.isError -> {
                        QrPairingError(uiState.actionHandler, onNextClicked)
                    }

                    else -> {
                        QrPairingContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun QrPairingError(
    actionHandler: (AddStudentAction) -> Unit,
    onRetryClicked: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .heightIn(min = 16.dp)
                .weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.panda_no_pairing_code),
            contentDescription = null
        )
        Spacer(
            modifier = Modifier
                .heightIn(min = 16.dp)
                .weight(1f)
        )
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(id = R.string.qrPairingErrorTitle),
            color = colorResource(id = R.color.textDarkest),
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
        Text(
            text = stringResource(id = R.string.qrPairingErrorDescription),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        OutlinedButton(
            modifier = Modifier.padding(top = 16.dp),
            border = BorderStroke(1.dp, colorResource(id = R.color.textDark)),
            colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.backgroundLightest)),
            onClick = {
                onRetryClicked()
                actionHandler(AddStudentAction.ResetError)
            }
        ) {
            Text(
                text = stringResource(id = R.string.retry),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 18.sp,
            )
        }
        Spacer(
            modifier = Modifier
                .heightIn(min = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
private fun QrPairingContent() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.qrPairingDescriptionCanvas),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 18.sp
        )
        Spacer(
            modifier = Modifier
                .heightIn(min = 16.dp)
                .weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.locate_pairing_qr_tutorial),
            contentDescription = null
        )
        Spacer(
            modifier = Modifier
                .heightIn(min = 16.dp)
                .weight(1f)
        )
    }
}

@Preview
@Composable
fun QrPairingScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    QrPairingScreen(uiState = AddStudentUiState(color = android.graphics.Color.BLUE) {}, {}, {})
}

@Preview
@Composable
fun QrPairingScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    QrPairingScreen(
        uiState = AddStudentUiState(
            color = android.graphics.Color.BLUE,
            isLoading = true
        ) {}, {}, {})
}

@Preview
@Composable
fun QrPairingErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    QrPairingScreen(
        uiState = AddStudentUiState(
            color = android.graphics.Color.BLUE,
            isLoading = false,
            isError = true
        ) {}, {}, {})
}