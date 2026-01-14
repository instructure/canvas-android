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
package com.instructure.horizon.features.notebook.addedit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NoteDeleteConfirmationDialog
import com.instructure.horizon.features.notebook.common.composable.NotebookHighlightedText
import com.instructure.horizon.features.notebook.common.composable.NotebookTypeSelect
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.Modal
import com.instructure.horizon.horizonui.organisms.ModalDialogState
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull
import kotlinx.coroutines.delay

@Composable
fun AddEditNoteScreen(
    navController: NavHostController,
    state: AddEditNoteUiState,
    onShowSnackbar: (String?, () -> Unit) -> Unit
) {
    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(Unit) {
        if (activity != null) ViewStyler.setStatusBarColor(activity, ContextCompat.getColor(activity, R.color.surface_pageSecondary))
    }

    LaunchedEffect(state.snackbarMessage) {
        onShowSnackbar(state.snackbarMessage, state.onSnackbarDismiss)
    }

    BackHandler {
        if (state.hasContentChange) {
            state.updateExitConfirmationDialog(true)
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = HorizonColors.Surface.pageSecondary(),
        topBar = { AddEditNoteAppBar(state, navigateBack = { navController.popBackStack() }) },
    ) { padding ->
        if (state.isLoading) {
            AddEditNoteLoading(padding)
        } else {
            NoteDeleteConfirmationDialog(
                showDialog = state.showDeleteConfirmationDialog,
                onDeleteSelected = {
                    state.onDeleteNote?.invoke {
                        navController.popBackStack()
                    }
                },
                dismissDialog = { state.updateDeleteConfirmationDialog(false) }
            )
            AddEditNoteScreenExitConfirmationDialog(state, navController)
            AddEditNoteContent(state, padding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditNoteAppBar(
    state: AddEditNoteUiState,
    navigateBack: () -> Unit
) {
    val requester = FocusRequester()
    var requestFocus by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(50)
        if(requestFocus) {
            requester.requestFocus()
            requestFocus = false
        }
    }
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pageSecondary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        title = {
            Text(
                state.title,
                style = HorizonTypography.h4,
                color = HorizonColors.Text.title(),
                modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                    }
                    .focusRequester(requester)
                    .focusable()
            )
        },
        navigationIcon = {
            Button(
                label = stringResource(R.string.editNoteCancelButtonLabel),
                onClick = {
                    if (state.hasContentChange) {
                        state.updateExitConfirmationDialog(true)
                    } else {
                        navigateBack()
                    }
                },
                color = ButtonColor.WhiteWithOutline,
                height = ButtonHeight.SMALL,
                enabled = !state.isLoading
            )
        },
        actions = {
            Button(
                label = stringResource(R.string.editNoteSaveButtonLabel),
                onClick = { state.onSaveNote(navigateBack) },
                color = ButtonColor.Black,
                height = ButtonHeight.SMALL,
                enabled = state.hasContentChange && !state.isLoading
            )
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .semantics {
                isTraversalGroup = true
                traversalIndex = -1f
            }
    )
}


@Composable
private fun AddEditNoteContent(state: AddEditNoteUiState, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = 16.dp)
    ) {
        HorizonSpace(SpaceSize.SPACE_24)

        NotebookTypeSelect(
            state.type,
            state.onTypeChanged,
            true,
            false,
        )

        HorizonSpace(SpaceSize.SPACE_24)

        NotebookHighlightedText(
            text = state.highlightedData.selectedText,
            type = state.type
        )

        HorizonSpace(SpaceSize.SPACE_24)

        TextArea(
            state = TextAreaState(
                placeHolderText = stringResource(R.string.addNoteAddANoteOptionalLabel),
                required = InputLabelRequired.Optional,
                value = state.userComment,
                onValueChange = state.onUserCommentChanged,
            ),
            minLines = 5,
        )

        HorizonSpace(SpaceSize.SPACE_16)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.lastModifiedDate != null) {
                Text(
                    state.lastModifiedDate,
                    style = HorizonTypography.labelMediumBold,
                    color = HorizonColors.Text.timestamp(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Spacer(Modifier.weight(1f))
            }

            if (state.onDeleteNote != null) {
                Button(
                    label = stringResource(R.string.deleteNoteLabel),
                    width = ButtonWidth.RELATIVE,
                    height = ButtonHeight.SMALL,
                    color = ButtonColor.DangerInverse,
                    iconPosition = ButtonIconPosition.Start(R.drawable.delete),
                    onClick = { state.updateDeleteConfirmationDialog(true) }
                )
            }
        }
    }
}

@Composable
private fun AddEditNoteLoading(padding: PaddingValues) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
            .padding(24.dp)
    ) {
        Spinner(color = HorizonColors.Surface.institution())
    }
}

@Composable
private fun AddEditNoteScreenExitConfirmationDialog(
    state: AddEditNoteUiState,
    navController: NavHostController
) {
    if (state.showExitConfirmationDialog) {
        Modal(
            ModalDialogState(
                title = stringResource(R.string.editNoteExitConfirmationTitle),
                message = stringResource(R.string.editNoteExitConfirmationMessage),
                primaryButtonTitle = stringResource(R.string.editNoteExitConfirmationExitButtonLabel),
                primaryButtonClick = {
                    state.updateExitConfirmationDialog(false)
                    navController.popBackStack()
                },
                secondaryButtonTitle = stringResource(R.string.editNoteExitConfirmationCancelButtonLabel),
                secondaryButtonClick = { state.updateExitConfirmationDialog(false) }
            ),
            onDismiss = { state.updateExitConfirmationDialog(false) }
        )
    }
}

@Composable
@Preview
private fun AddEditNoteScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = AddEditNoteUiState(
        title = "Add note",
        type = NotebookType.Important,
        highlightedData = NoteHighlightedData(
            selectedText = "This is a highlighted text",
            range = NoteHighlightedDataRange(0, 0, "", ""),
            textPosition = NoteHighlightedDataTextPosition(0, 0)
        ),
        userComment = TextFieldValue("This is an important part"),
        onUserCommentChanged = {},
        onTypeChanged = {},
        onSaveNote = {},
        onSnackbarDismiss = {}
    )

    AddEditNoteScreen(
        navController = rememberNavController(),
        state = state,
        onShowSnackbar = { _, _ -> }
    )
}

@Composable
@Preview
private fun AddEditNoteScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = AddEditNoteUiState(
        title = "Add note",
        type = NotebookType.Important,
        highlightedData = NoteHighlightedData(
            selectedText = "This is a highlighted text",
            range = NoteHighlightedDataRange(0, 0, "", ""),
            textPosition = NoteHighlightedDataTextPosition(0, 0)
        ),
        userComment = TextFieldValue("This is an important part"),
        onUserCommentChanged = {},
        onTypeChanged = {},
        onSaveNote = {},
        isLoading = true,
        onSnackbarDismiss = {}
    )

    AddEditNoteScreen(
        navController = NavHostController(LocalContext.current),
        state = state,
        onShowSnackbar = { _, _ -> }
    )
}