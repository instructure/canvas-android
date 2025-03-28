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
import androidx.compose.runtime.remember
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
fun SearchBarLive(
    @DrawableRes icon: Int,
    tintColor: Color,
    placeholder: String,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    query: String,
    queryChanged: (String) -> Unit,
    collapsable: Boolean = true,
    @DrawableRes hintIcon: Int? = null,
    collapseOnSearch: Boolean = false
) {
    Row(
        modifier = modifier
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                        onExpand(false)
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
                onValueChange = { queryChanged(it) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        queryChanged(query)
                        if (collapseOnSearch) {
                            onExpand(false)
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
                            onClick = { queryChanged("") }) {
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
                    onExpand(true)
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
fun SearchBarLivePreview() {
    SearchBarLive(
        icon = R.drawable.ic_smart_search,
        tintColor = Color.Black,
        placeholder = "Smart Search",
        onExpand = {},
        expanded = false,
        query = "",
        queryChanged = {}
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF121212
)
@Composable
fun SearchBarLiveDarkPreview() {
    SearchBarLive(
        icon = R.drawable.ic_smart_search,
        tintColor = Color.Black,
        placeholder = "Smart Search",
        onExpand = {},
        expanded = false,
        query = "",
        queryChanged = {}
    )
}