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
package com.instructure.horizon.features.account.reportabug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.LoadingButton
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextField
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldInputSize
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldState

@Composable
fun ReportABugScreen(
    uiState: ReportABugUiState,
    navController: NavHostController
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            val result = snackbarHostState.showSnackbar(uiState.snackbarMessage)
            if (result == SnackbarResult.Dismissed) {
                uiState.onSnackbarDismissed()
            }
        }
    }

    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            navController.popBackStack()
        }
    }

    Scaffold (
        topBar = { ReportABugScreen(navController) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = HorizonColors.Surface.cardPrimary()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(24.dp)
            ){
                Text(
                    text = stringResource(R.string.reportAProblemDescriptionMessage),
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body()
                )

                var subjectValue by remember { mutableStateOf(TextFieldValue("")) }
                var descriptionValue by remember { mutableStateOf(TextFieldValue("")) }

                val topicOptions = listOf(
                    stringResource(R.string.reportAProblemTopicSuggestion),
                    stringResource(R.string.reportAProblemTopicGeneralHelp),
                    stringResource(R.string.reportAProblemTopicMinorIssue),
                    stringResource(R.string.reportAProblemTopicUrgentIssue),
                    stringResource(R.string.reportAProblemTopicCriticalError)
                )

                SingleSelect(
                    SingleSelectState(
                        label = stringResource(R.string.reportAProblemTopic),
                        size = SingleSelectInputSize.Medium,
                        options = topicOptions,
                        selectedOption = uiState.selectedTopic,
                        isMenuOpen = uiState.isTopicMenuOpen,
                        onOptionSelected = uiState.onTopicSelected,
                        onMenuOpenChanged = uiState.onTopicMenuOpenChanged,
                        errorText = uiState.topicError,
                        isFocused = false,
                        isFullWidth = true,
                        onFocusChanged = {},
                        enabled = !uiState.isLoading,
                        required = InputLabelRequired.Required
                    )
                )

                TextField(
                    TextFieldState(
                        label = stringResource(R.string.reportAProblemSubject),
                        size = TextFieldInputSize.Medium,
                        value = subjectValue,
                        onValueChange = {
                            subjectValue = it
                            uiState.onSubjectChanged(it.text)
                        },
                        errorText = uiState.subjectError,
                        isFocused = false,
                        onFocusChanged = {},
                        enabled = !uiState.isLoading,
                        required = InputLabelRequired.Required
                    )
                )

                TextArea(
                    TextAreaState(
                        label = stringResource(R.string.reportAProblemDescription),
                        value = descriptionValue,
                        onValueChange = {
                            descriptionValue = it
                            uiState.onDescriptionChanged(it.text)
                        },
                        errorText = uiState.descriptionError,
                        isFocused = false,
                        onFocusChanged = {},
                        enabled = !uiState.isLoading,
                        required = InputLabelRequired.Required
                    ),
                    minLines = 5,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column {
                HorizonDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        label = stringResource(R.string.reportAProblemCancel),
                        color = ButtonColor.Ghost,
                        width = ButtonWidth.RELATIVE,
                        enabled = !uiState.isLoading,
                        onClick = { navController.popBackStack() }
                    )

                    LoadingButton(
                        label = stringResource(R.string.reportAProblemSubmit),
                        color = ButtonColor.Black,
                        width = ButtonWidth.RELATIVE,
                        loading = uiState.isLoading,
                        enabled = !uiState.isLoading,
                        contentAlignment = Alignment.Center,
                        onClick = uiState.onSubmit
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportABugScreen(
    navController: NavHostController,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.reportAProblemTitle),
                    style = HorizonTypography.h3,
                    color = HorizonColors.Text.title(),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            },
            actions = {
                IconButton(
                    iconRes = R.drawable.close,
                    contentDescription = stringResource(R.string.close),
                    color = IconButtonColor.Ghost,
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = HorizonColors.Surface.pageSecondary(),
                titleContentColor = HorizonColors.Text.title(),
                navigationIconContentColor = HorizonColors.Icon.default()
            ),
        )

        HorizonDivider()
    }
}
