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
package com.instructure.horizon.features.account.profile

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.horizonui.platform.LoadingState

data class AccountProfileUiState(
    val screenState: LoadingState = LoadingState(isPullToRefreshEnabled = false),
    val fullNameTextValue: TextFieldValue = TextFieldValue(""),
    val fullNameIsFocused: Boolean = false,
    val fullNameErrorMessage: String? = null,
    val displayNameTextValue: TextFieldValue = TextFieldValue(""),
    val displayNameIsFocused: Boolean = false,
    val displayNameErrorMessage: String? = null,
    val emailTextValue: TextFieldValue = TextFieldValue(""),
    val emailIsFocused: Boolean = false,
    val emailErrorMessage: String? = null,
    val updateFullName: (TextFieldValue) -> Unit = {},
    val updateFullNameIsFocused: (Boolean) -> Unit = {},
    val updateFullNameErrorMessage: (String?) -> Unit = {},
    val updateDisplayName: (TextFieldValue) -> Unit = {},
    val updateDisplayNameIsFocused: (Boolean) -> Unit = {},
    val updateDisplayNameErrorMessage: (String?) -> Unit = {},
    val updateEmail: (TextFieldValue) -> Unit = {},
    val updateEmailIsFocused: (Boolean) -> Unit = {},
    val updateEmailErrorMessage: (String?) -> Unit = {},
    val saveChanges: (notifyParent: (String) -> Unit) -> Unit = {},
)