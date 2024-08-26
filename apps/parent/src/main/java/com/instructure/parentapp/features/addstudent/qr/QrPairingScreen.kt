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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.parentapp.R
import com.instructure.parentapp.features.addstudent.AddStudentUiState

@Composable
fun QrPairingScreen(
    uiState: AddStudentUiState,
    onNextClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(key1 = uiState.isError) {
        if (uiState.isError) {
            snackbarHostState.showSnackbar(message = context.getString(R.string.pairingCodeDialogError))
            uiState.resetError()
        }
    }
    CanvasTheme {
        Scaffold(
            topBar = {
                CanvasAppBar(
                    title = stringResource(id = R.string.qrPairingTitle),
                    navigationActionClick = onBackClicked,
                    actions = {
                        TextButton(onClick = onNextClicked) {
                            Text(
                                text = stringResource(id = R.string.next),
                                style = TextStyle(
                                    color = colorResource(id = R.color.textInfo),
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when {
                    uiState.isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Loading()
                        }
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
private fun QrPairingContent() {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.qrPairingDescription),
            style = TextStyle(
                color = colorResource(id = R.color.textDarkest),
                fontSize = 18.sp
            )
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
    QrPairingScreen(uiState = AddStudentUiState(resetError = {}, onStartPairing = {}), {}, {})
}

@Preview
@Composable
fun QrPairingScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    QrPairingScreen(
        uiState = AddStudentUiState(
            isLoading = true,
            resetError = {},
            onStartPairing = {}), {}, {})
}