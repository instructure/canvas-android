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
package com.instructure.parentapp.features.login.createaccount

import android.text.Annotation
import android.text.SpannedString
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.pandares.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.Loading

@Composable
internal fun CreateAccountScreen(
    uiState: CreateAccountUiState,
    actionHandler: (CreateAccountAction) -> Unit
) {
    CanvasTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val errorMessage = uiState.errorSnackMessage
        LaunchedEffect(uiState.showErrorSnack) {
            if (uiState.showErrorSnack) {
                val result = snackbarHostState.showSnackbar(errorMessage)
                if (result == SnackbarResult.Dismissed) {
                    actionHandler(CreateAccountAction.SnackbarDismissed)
                }
            }
        }

        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .testTag("CreateAccountScreen"),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    CanvasHeader()
                    Spacer(modifier = Modifier.height(48.dp))

                    TextFields(uiState, actionHandler)
                    Spacer(modifier = Modifier.height(12.dp))

                    TermsOrPrivacyText(actionHandler, uiState.termsOfService)
                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.backgroundInfo)),
                        onClick = { actionHandler(CreateAccountAction.CreateAccountTapped) }
                    ) {
                        Text(
                            text = stringResource(R.string.createAccButton),
                            color = colorResource(R.color.textLightest)
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.createAccAlreadyHaveAccount),
                            color = colorResource(R.color.textDark)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            modifier = Modifier.clickable {
                                actionHandler(CreateAccountAction.SignInTapped)
                            },
                            text = stringResource(R.string.createAccSignIn),
                            color = colorResource(R.color.textInfo)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (uiState.isLoading) {
                    Loading(modifier = Modifier
                        .fillMaxSize()
                        .testTag("loading")
                        .pointerInput(Unit) { }
                    )
                }
            })
    }
}

@Composable
private fun CanvasHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_canvas_logo),
            tint = colorResource(R.color.login_parentAppTheme),
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            modifier = Modifier.width(168.dp),
            painter = painterResource(id = R.drawable.canvas_wordmark),
            tint = colorResource(id = R.color.textDarkest),
            contentDescription = null
        )
    }
}

@Composable
private fun TextFields(
    uiState: CreateAccountUiState,
    actionHandler: (CreateAccountAction) -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = colorResource(R.color.textDarkest),
        unfocusedLabelColor = colorResource(R.color.textDark),
        unfocusedBorderColor = colorResource(R.color.textDarkest),
        focusedBorderColor = colorResource(id = R.color.backgroundInfo),
        focusedLabelColor = colorResource(id = R.color.backgroundInfo),
        cursorColor = colorResource(id = R.color.backgroundInfo),
        trailingIconColor = colorResource(R.color.textDarkest),
        errorBorderColor = colorResource(R.color.borderDanger),
        errorCursorColor = colorResource(R.color.textDanger),
        errorLabelColor = colorResource(R.color.textDanger),
        errorTrailingIconColor = colorResource(R.color.borderDanger),
    )

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nameInput"),
        singleLine = true,
        value = uiState.name,
        isError = !uiState.nameError.isNullOrBlank(),
        onValueChange = { actionHandler(CreateAccountAction.UpdateName(it)) },
        label = { Text(stringResource(R.string.createAccFullName)) },
        colors = textFieldColors,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
    if (!uiState.nameError.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("nameError"),
            textAlign = TextAlign.Start,
            text = uiState.nameError,
            color = colorResource(R.color.textDanger)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emailInput"),
        singleLine = true,
        value = uiState.email,
        isError = !uiState.emailError.isNullOrBlank(),
        onValueChange = { actionHandler(CreateAccountAction.UpdateEmail(it)) },
        label = { Text(stringResource(R.string.createAccEmail)) },
        colors = textFieldColors,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
    )
    if (!uiState.emailError.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("emailError"),
            textAlign = TextAlign.Start,
            text = uiState.emailError,
            color = colorResource(R.color.textDanger)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    var focused by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("passwordInput")
            .onFocusChanged { focused = it.isFocused },
        singleLine = true,
        value = uiState.password,
        isError = !uiState.passwordError.isNullOrBlank(),
        onValueChange = { actionHandler(CreateAccountAction.UpdatePassword(it)) },
        label = { Text(stringResource(R.string.createAccPassword)) },
        colors = textFieldColors,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { actionHandler(CreateAccountAction.CreateAccountTapped) }
        ),
        trailingIcon = {
            val icon = if (passwordVisible) {
                painterResource(R.drawable.ic_visibility_off)
            } else {
                painterResource(R.drawable.ic_visibility)
            }

            val description = if (passwordVisible) {
                stringResource(R.string.createAccHidePassword)
            } else {
                stringResource(R.string.createAccShowPassword)
            }

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                if (focused && uiState.passwordError.isNullOrBlank()) {
                    Icon(
                        painter = icon,
                        description,
                        tint = colorResource(id = R.color.backgroundInfo)
                    )
                } else {
                    Icon(painter = icon, description)
                }
            }
        }
    )
    if (!uiState.passwordError.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("passwordError"),
            textAlign = TextAlign.Start,
            text = uiState.passwordError,
            color = colorResource(R.color.textDanger)
        )
    }
}

@Composable
private fun TermsOrPrivacyText(
    actionHandler: (CreateAccountAction) -> Unit,
    termsOfService: TermsOfService?
) {
    val textRes = if (termsOfService == null) {
        R.string.createAccTosAndPrivacy
    } else if (termsOfService.passive) {
        R.string.createAccViewPrivacy
    } else {
        R.string.createAccTosAndPrivacy
    }
    PrivacyTextSpan(textRes, actionHandler)
}

@Composable
private fun PrivacyTextSpan(
    textResource: Int,
    actionHandler: (CreateAccountAction) -> Unit
) {
    val titleText = SpannedString(LocalContext.current.resources.getText(textResource))
    val annotations = titleText.getSpans(0, titleText.length, Annotation::class.java)

    Text(buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = 14.sp,
                color = colorResource(R.color.textDark)
            )
        ) {
            append(titleText)
            for (annotation in annotations) {
                val start = titleText.getSpanStart(annotation)
                val end = titleText.getSpanEnd(annotation)
                addLink(
                    clickable = LinkAnnotation.Clickable(
                        tag = annotation.value,
                        styles = TextLinkStyles(style = SpanStyle(color = colorResource(R.color.textInfo)))
                    ) {
                        textLinkTapped(annotation.value, actionHandler)
                    },
                    start = start,
                    end = end
                )
            }
        }
    })
}

private fun textLinkTapped(
    tag: String,
    actionHandler: (CreateAccountAction) -> Unit
) {
    if (tag == "tos") {
        actionHandler(CreateAccountAction.TosTapped)
    } else if (tag == "privacy") {
        actionHandler(CreateAccountAction.PrivacyTapped)
    }
}

@Preview
@Composable
private fun CreateAccountScreenPreview() {
    CreateAccountScreen(
        CreateAccountUiState(),
        {}
    )
}

@Preview
@Composable
private fun CreateAccountScreenErrorPreview() {
    CreateAccountScreen(
        CreateAccountUiState(
            nameError = "Please enter full name",
            emailError = "Please enter an email address",
            passwordError = "Password is required"
        ),
        {}
    )
}
