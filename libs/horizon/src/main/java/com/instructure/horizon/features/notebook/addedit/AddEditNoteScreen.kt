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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NotebookHighlightedText
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
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull

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

    Scaffold(
        containerColor = HorizonColors.Surface.pageSecondary(),
        topBar = { AddEditNoteAppBar(state, navigateBack = { navController.popBackStack() }) },
    ) { padding ->
        if (state.isLoading) {
            AddEditNoteLoading(padding)
        } else {
            AddEditNoteScreenDeleteConfirmationDialog(state, navController)
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
                color = HorizonColors.Text.title()
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
                height = ButtonHeight.SMALL
            )
        },
        actions = {
            Button(
                label = stringResource(R.string.editNoteSaveButtonLabel),
                onClick = { state.onSaveNote(navigateBack) },
                color = ButtonColor.Black,
                height = ButtonHeight.SMALL,
                enabled = state.hasContentChange
            )
        },
        modifier = Modifier.padding(horizontal = 16.dp)
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

        var isMenuOpen by remember { mutableStateOf(false) }
        SingleSelect(
            SingleSelectState(
                options = NotebookType.entries.map { it.name },
                selectedOption = state.type?.name,
                size = SingleSelectInputSize.Small,
                onOptionSelected = { state.onTypeChanged(NotebookType.valueOf(it)) },
                isMenuOpen = isMenuOpen,
                onMenuOpenChanged = { isMenuOpen = it },
                isFullWidth = false,
            ),
            modifier = Modifier.width(IntrinsicSize.Min)
        )

        HorizonSpace(SpaceSize.SPACE_24)

        NotebookHighlightedText(
            text = state.highlightedData.selectedText,
            type = state.type
        )

        HorizonSpace(SpaceSize.SPACE_24)

        TextArea(
            state = TextAreaState(
                placeHolderText = stringResource(R.string.addNoteAddANoteLabel),
                required = InputLabelRequired.Optional,
                value = state.userComment,
                onValueChange = state.onUserCommentChanged,
            ),
            minLines = 5,
            maxLines = 5
        )

        HorizonSpace(SpaceSize.SPACE_16)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.lastModifiedDate != null) {
                Text(
                    state.lastModifiedDate,
                    style = HorizonTypography.labelSmall,
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
private fun AddEditNoteScreenDeleteConfirmationDialog(
    state: AddEditNoteUiState,
    navController: NavHostController
) {
    if (state.showDeleteConfirmationDialog) {
        Modal(
            ModalDialogState(
                title = stringResource(R.string.deleteNoteConfirmationTitle),
                message = stringResource(R.string.deleteNoteConfirmationMessage),
                primaryButtonTitle = stringResource(R.string.deleteNoteConfirmationDeleteLabel),
                primaryButtonClick = {
                    state.updateDeleteConfirmationDialog(false)
                    state.onDeleteNote?.invoke { navController.popBackStack() }
                },
                secondaryButtonTitle = stringResource(R.string.deleteNoteConfirmationCancelLabel),
                secondaryButtonClick = { state.updateDeleteConfirmationDialog(false) }

            ),
            onDismiss = { state.updateDeleteConfirmationDialog(false) }
        )
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
        navController = NavHostController(LocalContext.current),
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