package com.instructure.horizon.features.notebook.common.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.Modal
import com.instructure.horizon.horizonui.organisms.ModalDialogState

@Composable
fun NoteDeleteConfirmationDialog(
    showDialog: Boolean,
    onDeleteSelected: () -> Unit,
    dismissDialog: () -> Unit,
) {
    if (showDialog) {
        Modal(
            ModalDialogState(
                title = stringResource(R.string.deleteNoteConfirmationTitle),
                message = stringResource(R.string.deleteNoteConfirmationMessage),
                primaryButtonTitle = stringResource(R.string.deleteNoteConfirmationDeleteLabel),
                primaryButtonClick = {
                    dismissDialog()
                    onDeleteSelected()
                },
                secondaryButtonTitle = stringResource(R.string.deleteNoteConfirmationCancelLabel),
                secondaryButtonClick = { dismissDialog() }

            ),
            onDismiss = { dismissDialog() }
        )
    }
}