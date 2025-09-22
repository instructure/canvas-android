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
package com.instructure.horizon.features.notebook

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.instructure.horizon.features.notebook.navigation.NotebookDialogNavigation
import com.instructure.horizon.horizonui.foundation.HorizonColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookBottomDialog(
    courseId: Long,
    objectFilter: Pair<String, String>,
    onShowSnackbar: (String?, () -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        containerColor = HorizonColors.Surface.pagePrimary(),
        onDismissRequest = { onDismiss() },
        dragHandle = null,
        sheetState = bottomSheetState,
    ) {
        NotebookDialogNavigation(courseId, objectFilter, onDismiss, onShowSnackbar)
    }
}