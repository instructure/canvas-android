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
package com.instructure.pandautils.features.inbox.compose.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.LabelSwitchRow
import com.instructure.pandautils.compose.composables.LabelTextFieldRow
import com.instructure.pandautils.compose.composables.LabelValueRow
import com.instructure.pandautils.compose.composables.TextFieldWithHeader
import com.instructure.pandautils.features.inbox.compose.InboxComposeUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InboxComposeScreen(
    uiState: InboxComposeUiState,
    onDismiss: () -> Unit = {}
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            topBar = {
                CanvasThemedAppBar(
                    title = uiState.title,
                    navigationActionClick = onDismiss
                )
            },
            content = { padding ->
                Column(Modifier.verticalScroll(rememberScrollState()).imePadding().imeNestedScroll()) {
                    LabelValueRow(
                        label = "Course",
                        value = uiState.course,
                        onClick = { /*TODO*/ },
                        modifier = Modifier.padding(padding)
                    )

                    LabelMultipleValuesRow(
                        label = "To",
                        selectedValues = listOf("Person 1", "Person 2", "Person 3", "Person 4", "Person 5"),
                        onSelect = {},
                        addValueClicked = { /*TODO*/ },
                        modifier = Modifier.padding(padding)
                    )

                    LabelSwitchRow(
                        label = "Send individual message to each recipient",
                        checked = uiState.sendIndividual,
                        onCheckedChange = { uiState.sendIndividual = it },
                        modifier = Modifier.padding(padding)
                    )

                    LabelTextFieldRow(
                        label = "Subject",
                        onValueChange = {
                            uiState.subject = it
                        },
                    )
                    
                    TextFieldWithHeader(
                        label = "Message",
                        headerIconResource = R.drawable.ic_attachment,
                        onValueChange = {
                            uiState.body = it
                        },
                    )

                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                    Text("Attachments")
                }
            }
        )
    }
}