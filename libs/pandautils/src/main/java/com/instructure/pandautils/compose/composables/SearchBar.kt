/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.compose.composables

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.pandautils.R

@Composable
fun SearchBar(
    @DrawableRes icon: Int,
    tintColor: Color,
    placeholder: String,
    onSearch: (String) -> Unit,
    onExpand: ((Boolean) -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    collapsable: Boolean = true,
    @DrawableRes hintIcon: Int? = null,
    collapseOnSearch: Boolean = false
) {
    Row(
        modifier = modifier
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var expanded by remember { mutableStateOf(!collapsable) }
        var query by remember { mutableStateOf(searchQuery) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(expanded) {
            if (expanded && collapsable) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }

        if (expanded) {
            if (collapsable) {
                IconButton(
                    modifier = Modifier.testTag("closeButton"),
                    onClick = {
                        expanded = false
                        onExpand?.invoke(false)
                    }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.a11y_searchBarCloseButton),
                        tint = tintColor
                    )
                }
            }

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("searchField")
                    .focusRequester(focusRequester),
                placeholder = { Text(placeholder) },
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        onSearch(query)
                        if (collapseOnSearch) {
                            expanded = false
                            onExpand?.invoke(false)
                        }
                    }
                ),
                textStyle = MaterialTheme.typography.body1,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = tintColor,
                    cursorColor = tintColor,
                    focusedLabelColor = tintColor,
                    leadingIconColor = tintColor,
                    trailingIconColor = tintColor,
                    textColor = tintColor,
                    disabledTextColor = tintColor.copy(alpha = 0.5f),
                    unfocusedLabelColor = tintColor.copy(alpha = 0.5f),
                    unfocusedIndicatorColor = tintColor.copy(alpha = 0.5f),
                    disabledLeadingIconColor = tintColor.copy(alpha = 0.5f),
                    disabledTrailingIconColor = tintColor.copy(alpha = 0.5f),
                    placeholderColor = tintColor.copy(alpha = 0.5f),
                ),
                leadingIcon = {
                    Icon(
                        modifier = Modifier.height(20.dp),
                        painter = painterResource(hintIcon ?: icon),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            modifier = Modifier.testTag("clearButton"),
                            onClick = {
                                query = ""
                                onClear?.invoke()
                            }) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.a11y_searchBarClearButton)
                            )
                        }
                    }
                }
            )
        } else {
            IconButton(
                modifier = Modifier.testTag("searchButton"),
                onClick = {
                    expanded = true
                    onExpand?.invoke(true)
                }) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = stringResource(R.string.a11y_searchBarSearchButton),
                    tint = tintColor
                )
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SearchBarPreview() {
    SearchBar(
        icon = R.drawable.ic_smart_search,
        tintColor = Color.Black,
        placeholder = "Smart Search",
        onExpand = {},
        onSearch = {}
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF121212
)
@Composable
fun SearchBarDarkPreview() {
    SearchBar(
        icon = R.drawable.ic_smart_search,
        tintColor = Color.White,
        placeholder = "Smart Search",
        onExpand = {},
        onSearch = {}
    )
}