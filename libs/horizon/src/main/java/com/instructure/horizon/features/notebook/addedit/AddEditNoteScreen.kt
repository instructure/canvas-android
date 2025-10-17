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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NotebookAppBar
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
import com.instructure.horizon.horizonui.organisms.inputs.common.InputLabelRequired
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
        if (activity != null) ViewStyler.setStatusBarColor(activity, ContextCompat.getColor(activity, R.color.surface_pagePrimary))
    }

    LaunchedEffect(state.snackbarMessage) {
        onShowSnackbar(state.snackbarMessage, state.onSnackbarDismiss)
    }

    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = { NotebookAppBar(navigateBack = { navController.popBackStack() }) },
    ) { padding ->
        if (state.isLoading) {
            AddEditNoteLoading(padding)
        } else {
            AddEditNoteContent(
                state = state,
                navController = navController,
                padding = padding
            )
        }
    }
}

@Composable
private fun AddEditNoteContent(state: AddEditNoteUiState, navController: NavHostController, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.addNoteHighlightlabel),
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.body()
        )

        HorizonSpace(SpaceSize.SPACE_8)

        NotebookHighlightedText(
            text = state.highlightedData.selectedText,
            type = state.type
        )

        HorizonSpace(SpaceSize.SPACE_24)

        Text(
            text = stringResource(R.string.addNoteLabelLabel),
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.body()
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Row {
            NotebookTypeSelect(
                type = NotebookType.Important,
                isSelected = state.type == NotebookType.Important,
                onSelect = { state.onTypeChanged(if (state.type == NotebookType.Important) null else NotebookType.Important) },
                modifier = Modifier.weight(1f)
            )

            HorizonSpace(SpaceSize.SPACE_12)

            NotebookTypeSelect(
                type = NotebookType.Confusing,
                isSelected = state.type == NotebookType.Confusing,
                onSelect = { state.onTypeChanged(if (state.type == NotebookType.Confusing) null else NotebookType.Confusing) },
                modifier = Modifier.weight(1f)
            )
        }

        HorizonSpace(SpaceSize.SPACE_24)

        TextArea(
            state = TextAreaState(
                label = stringResource(R.string.addNoteAddANoteLabel),
                required = InputLabelRequired.Optional,
                value = state.userComment,
                onValueChange = state.onUserCommentChanged,
            ),
            minLines = 5
        )

        HorizonSpace(SpaceSize.SPACE_16)

        Button(
            label = stringResource(R.string.addNoteSaveLabel),
            onClick = { state.onSaveNote { navController.popBackStack() } },
            enabled = !state.isLoading && state.type != null,
            color = ButtonColor.Institution,
            width = ButtonWidth.FILL,
            height = ButtonHeight.NORMAL
        )

        if (state.onDeleteNote != null) {
            HorizonSpace(SpaceSize.SPACE_16)
            Button(
                label = stringResource(R.string.addNoteDeleteLabel),
                onClick = { state.onDeleteNote?.invoke { navController.popBackStack() } },
                color = ButtonColor.DangerInverse,
                width = ButtonWidth.FILL,
                height = ButtonHeight.NORMAL,
                iconPosition = ButtonIconPosition.Start(R.drawable.delete),
            )
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
@Preview
private fun AddEditNoteScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = AddEditNoteUiState(
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