/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.horizonui.platform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Spinner

data class LoadingState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isError: Boolean = false,
    val isPullToRefreshEnabled: Boolean = true,
    val errorMessage: String? = null,
    val errorSnackbar: String? = null,
    val onRefresh: () -> Unit = {},
    val onErrorSnackbarDismiss: () -> Unit = {},
)

@ExperimentalMaterial3Api
@Composable
fun LoadingStateWrapper(
    loadingState: LoadingState,
    modifier: Modifier = Modifier,
    containerColor: Color = HorizonColors.Surface.pagePrimary(),
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loadingState.errorSnackbar) {
        if (loadingState.errorSnackbar != null) {
            val result = snackbarHostState.showSnackbar(loadingState.errorSnackbar)
            if (result == SnackbarResult.Dismissed) {
                loadingState.onErrorSnackbarDismiss()
            }
        }
    }

    Scaffold(containerColor = containerColor, snackbarHost = { SnackbarHost(snackbarHostState) }, modifier = modifier) { paddingValues ->
        if (loadingState.isPullToRefreshEnabled) {
            PullToRefreshBox(
                isRefreshing = loadingState.isRefreshing,
                onRefresh = loadingState.onRefresh,
                modifier = Modifier.padding(paddingValues),
                state = state,
                indicator = {
                    Indicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        isRefreshing = loadingState.isRefreshing,
                        containerColor = HorizonColors.Surface.pageSecondary(),
                        color = HorizonColors.Surface.institution(),
                        state = state
                    )
                },
                content = {
                    when {
                        loadingState.isLoading -> LoadingContent()
                        loadingState.isError -> ErrorContent(
                            loadingState.errorMessage
                                ?: stringResource(R.string.loadingStateWrapper_errorOccurred)
                        )

                        else -> content()
                    }
                }
            )
        } else {
            Box {
                when {
                    loadingState.isLoading -> LoadingContent()
                    loadingState.isError -> ErrorContent(
                        loadingState.errorMessage
                            ?: stringResource(R.string.loadingStateWrapper_errorOccurred)
                    )

                    else -> content()
                }
            }
        }
    }
}

@Composable
private fun BoxScope.LoadingContent(modifier: Modifier = Modifier) {
    Spinner(
        modifier
            .fillMaxSize()
            .align(Alignment.Center)
    )
}

@Composable
private fun BoxScope.ErrorContent(errorText: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = errorText, style = HorizonTypography.h3)
    }
}