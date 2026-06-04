/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.cookieconsent

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.instructure.pandautils.R
import com.instructure.pandautils.views.CanvasLoadingView

@Composable
fun CookieConsentContent(
    uiState: CookieConsentUiState,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            uiState.onErrorDismissed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorResource(R.color.backgroundLightest)
    ) { paddingValues ->
        if (!uiState.showDialog) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.loading) {
                    val loadingColorRes = when (uiState.namespace) {
                        CookieConsentNamespace.STUDENT -> R.color.login_studentAppTheme
                        CookieConsentNamespace.TEACHER -> R.color.login_teacherAppTheme
                        CookieConsentNamespace.PARENT -> R.color.login_parentAppTheme
                    }
                    AndroidView(
                        factory = {
                            CanvasLoadingView(it).apply {
                                setOverrideColor(it.getColor(loadingColorRes))
                            }
                        },
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
        Text(
            text = stringResource(R.string.cookieConsentTitle),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.textDarkest)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.cookieConsentDescription),
            fontSize = 16.sp,
            color = colorResource(R.color.textDark)
        )

        Spacer(modifier = Modifier.height(16.dp))

        BulletItem(text = stringResource(R.string.cookieConsentNoProfiles))
        Spacer(modifier = Modifier.height(8.dp))
        BulletItem(text = stringResource(R.string.cookieConsentNoSelling))
        Spacer(modifier = Modifier.height(8.dp))
        BulletItem(text = stringResource(R.string.cookieConsentFullControl))

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = uiState.onAllow,
            enabled = !uiState.saving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.backgroundInfo),
                contentColor = colorResource(R.color.textLightest)
            )
        ) {
            if (uiState.saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colorResource(R.color.white),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.cookieConsentAllow),
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = uiState.onDecline,
            enabled = !uiState.saving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colorResource(R.color.backgroundLightest),
                contentColor = colorResource(R.color.textDarkest),
            )
        ) {
            Text(
                text = stringResource(R.string.cookieConsentDecline),
                fontSize = 16.sp,
            )
        }
    }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "\u2022",
            fontSize = 16.sp,
            color = colorResource(R.color.textDark)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = colorResource(R.color.textDark)
        )
    }
}

@Preview
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, backgroundColor = 0x0000000)
@Composable
fun CookieConsentContentPreview() {
    CookieConsentContent(
        uiState = CookieConsentUiState(
            showDialog = true,
            saving = false,
            loading = false,
            onAllow = {},
            onDecline = {}
        )
    )
}

@Preview
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, backgroundColor = 0x0000000)
@Composable
fun CookieConsentLoadingPreview() {
    CookieConsentContent(
        uiState = CookieConsentUiState(
            showDialog = false,
            saving = false,
            loading = true,
            onAllow = {},
            onDecline = {}
        )
    )
}