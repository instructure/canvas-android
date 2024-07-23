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
package com.instructure.pandautils.features.inbox.recipientpicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar

@Composable
fun RecipientPickerScreen(
    title: String,
    onNavigateBack: () -> Unit,
    viewModel: RecipientPickerViewModel,
    selectedRecipientsChanged: (List<Recipient>) -> Unit,
) {
    val recipients by viewModel.allRecipient.collectAsState()

    Scaffold (
        topBar = {
            CanvasThemedAppBar(title = title, navigationActionClick = { onNavigateBack() })
         },
        content = { padding ->
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                items(recipients) { recipient ->
                    Text(recipient.name ?: "")
                }
            }

        }

    )
}