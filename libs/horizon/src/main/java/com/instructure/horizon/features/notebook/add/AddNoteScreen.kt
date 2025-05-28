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
package com.instructure.horizon.features.notebook.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.NoteHighlightedDataTextPosition
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
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState

@Composable
fun AddNoteScreen(
    navController: NavHostController,
    state: AddNoteUiState,
) {
    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = { NotebookAppBar(navigateBack = { navController.popBackStack() }) },
    ) { padding ->
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
                    value = state.userComment,
                    onValueChange = state.onUserCommentChanged,
                )
            )

            HorizonSpace(SpaceSize.SPACE_16)

            Button(
                label = stringResource(R.string.addNoteSaveLabel),
                onClick = state.onSaveNote,
                enabled = !state.isLoading && state.type != null,
                color = ButtonColor.Institution,
                width = ButtonWidth.FILL,
                height = ButtonHeight.NORMAL
            )
        }
    }
}

@Composable
@Preview
private fun AddNoteScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = AddNoteUiState(
        type = NotebookType.Important,
        highlightedData = NoteHighlightedData(
            selectedText = "This is a highlighted text",
            textPosition = NoteHighlightedDataTextPosition(start = 0, end = 25)
        ),
        userComment = TextFieldValue("This is an important part"),
        onUserCommentChanged = {},
        onTypeChanged = {},
        onSaveNote = {},
    )

    AddNoteScreen(
        navController = NavHostController(LocalContext.current),
        state = state
    )
}