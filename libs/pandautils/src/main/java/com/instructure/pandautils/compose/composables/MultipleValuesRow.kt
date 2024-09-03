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
package com.instructure.pandautils.compose.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> MultipleValuesRow(
    label: String,
    uiState: MultipleValuesRowState<T>,
    actionHandler: (MultipleValuesRowAction) -> Unit,
    itemComposable: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    searchResultComposable: (@Composable (T) -> Unit)? = null,
) {
    val animationLabel = "LabelMultipleValuesRowTransition"
    val searchFieldFocusRequester = remember { FocusRequester() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
            .defaultMinSize(minHeight = 52.dp)
    ) {
        Column {
            Text(
                text = label,
                color = colorResource(id = R.color.textDarkest),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.weight(1f))
            Loading(
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
            ){
                FlowRow {
                    for (value in uiState.selectedValues) {
                        AnimatedContent(
                            label = animationLabel,
                            targetState = value,
                        ) {
                            itemComposable(it)
                        }
                    }
                }

                if (uiState.isSearchEnabled) {
                    Spacer(Modifier.height(8.dp))

                    CanvasThemedTextField(
                        value = uiState.searchQuery,
                        onValueChange = {
                            actionHandler(MultipleValuesRowAction.SearchQueryChanges(it))
                        },
                        maxLines = 1,
                        placeholder = stringResource(id = R.string.search),
                        modifier = Modifier
                            .padding(4.dp)
                            .focusRequester(searchFieldFocusRequester)
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = uiState.isShowResults,
                        properties = PopupProperties(focusable = false),
                        onDismissRequest = { actionHandler(MultipleValuesRowAction.HideRecipientResults) }
                    ) {
                        Column {
                            uiState.searchResults.forEach { value ->
                                Row(
                                    Modifier
                                        .defaultMinSize(minWidth = 200.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            actionHandler(
                                                MultipleValuesRowAction.SearchValueSelected(
                                                    value
                                                )
                                            )
                                            actionHandler(
                                                MultipleValuesRowAction.SearchQueryChanges(
                                                    TextFieldValue("")
                                                )
                                            )
                                            actionHandler(MultipleValuesRowAction.HideRecipientResults)
                                        }
                                ){
                                    searchResultComposable?.invoke(value)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { actionHandler(MultipleValuesRowAction.AddValueClicked) },
            modifier = Modifier
                .size(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_lined),
                contentDescription = null,
                tint = colorResource(id = R.color.textDarkest)
            )
        }
    }
}

data class MultipleValuesRowState<T>(
    val selectedValues: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val isSearchEnabled: Boolean = false,
    val isShowResults: Boolean = false,
    val searchQuery: TextFieldValue = TextFieldValue(""),
    val searchResults: List<T> = emptyList(),
)

sealed class MultipleValuesRowAction {
    data object AddValueClicked : MultipleValuesRowAction()
    data object HideRecipientResults : MultipleValuesRowAction()
    data class SearchValueSelected<T>(val value: T) : MultipleValuesRowAction()
    data class SearchQueryChanges(val searchQuery: TextFieldValue) : MultipleValuesRowAction()
}

@Preview
@Composable
fun LabelMultipleValuesRowPreview() {
    ContextKeeper.appContext = LocalContext.current
    val users = listOf(
        Recipient(name = "Person 1"),
        Recipient(name = "Person 2"),
        Recipient(name = "Person 3"),
    )
    val uiState = MultipleValuesRowState(
        selectedValues = users,
        isLoading = false,
        isSearchEnabled = false,
        searchQuery = TextFieldValue(""),
        searchResults = emptyList(),
    )
    MultipleValuesRow(
        label = "To",
        uiState = uiState,
        itemComposable = { user ->
            Text(user.name ?: "")
        },
        actionHandler = {},
        modifier = Modifier
            .fillMaxWidth(),
    )
}