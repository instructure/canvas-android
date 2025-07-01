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
package com.instructure.horizon.features.inbox.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.organisms.controls.CheckboxItem
import com.instructure.horizon.horizonui.organisms.controls.CheckboxItemState
import com.instructure.horizon.horizonui.organisms.controls.ControlsContentState
import com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch.MultiSelectSearch
import com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch.MultiSelectSearchInputSize
import com.instructure.horizon.horizonui.organisms.inputs.multiselectsearch.MultiSelectSearchState
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelect
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectInputSize
import com.instructure.horizon.horizonui.organisms.inputs.singleselect.SingleSelectState
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextArea
import com.instructure.horizon.horizonui.organisms.inputs.textarea.TextAreaState
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextField
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldInputSize
import com.instructure.horizon.horizonui.organisms.inputs.textfield.TextFieldState

@Composable
fun HorizonInboxComposeScreen(
    state: HorizonInboxComposeUiState,
    navController: NavHostController
) {
    Scaffold(
        containerColor = HorizonColors.Surface.pageSecondary(),
        topBar = {
            HorizonInboxComposeTopBar(navController)
        }
    ) { innerPadding ->
        HorizonInboxComposeContent(
            state,
            navController,
            Modifier.padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HorizonInboxComposeTopBar(
    navController: NavHostController,
) {
    TopAppBar(
        title = {
            Text(
                stringResource(R.string.inboxComposeTitle),
                style = HorizonTypography.h2,
                color = HorizonColors.Text.title(),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        },
        actions = {
            IconButton(
                iconRes = R.drawable.close,
                contentDescription = null,
                color = IconButtonColor.Inverse,
                elevation = HorizonElevation.level4,
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HorizonColors.Surface.pageSecondary(),
            titleContentColor = HorizonColors.Text.title(),
            navigationIconContentColor = HorizonColors.Icon.default()
        ),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun HorizonInboxComposeContent(
    state: HorizonInboxComposeUiState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HorizonDivider()

        CourseRecipientPickerSection(state)

        CourseRecipientSendIndividuallySection(state)

        HorizonInboxComposeTextSection(state)

        Spacer(modifier = Modifier.weight(1f))

        HorizonInboxComposeControlsSection(state, navController)
    }
}

@Composable
private fun CourseRecipientPickerSection(state: HorizonInboxComposeUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        var isCoursePickerFocused by remember { mutableStateOf(false) }
        var isCoursePickerOpened by remember { mutableStateOf(false) }
        val coursePickerState = SingleSelectState(
            isFocused = isCoursePickerFocused,
            isMenuOpen = isCoursePickerOpened,
            size = SingleSelectInputSize.Medium,
            placeHolderText = stringResource(R.string.inboxComposeCoursePickerLabel),
            options = state.coursePickerOptions.map { it.name },
            selectedOption = state.selectedCourse?.name,
            onOptionSelected = {
                state.coursePickerOptions.firstOrNull()?.let {
                    state.onCourseSelected(it)
                }
            },
            onFocusChanged = { isCoursePickerFocused = it },
            onMenuOpenChanged = { isCoursePickerOpened = it },
        )
        SingleSelect(coursePickerState, Modifier.padding(top = 24.dp))

        HorizonSpace(SpaceSize.SPACE_12)

        var isRecipientPickerFocused by remember { mutableStateOf(false) }
        var isRecipientPickerOpened by remember { mutableStateOf(false) }
        val recipientPickerState = MultiSelectSearchState(
            enabled = state.selectedCourse != null,
            isFocused = isRecipientPickerFocused,
            isMenuOpen = isRecipientPickerOpened,
            size = MultiSelectSearchInputSize.Medium,
            options = state.recipientPickerOptions.mapNotNull { it.name },
            selectedOptions = state.selectedRecipients.mapNotNull { it.name },
            searchPlaceHolder = stringResource(R.string.inboxComposeRecipientPickerLabel),
            searchQuery = state.recipientSearchQuery,
            isOptionListLoading = state.isRecipientPickerLoading,
            onSearchQueryChanged = state.onRecipientSearchQueryChanged,
            onOptionSelected = { recipient ->
                state.recipientPickerOptions.firstOrNull { it.name == recipient }?.let {
                    state.onRecipientSelected(it)
                }
            },
            onOptionRemoved = { recipient ->
                state.selectedRecipients.firstOrNull { it.name == recipient }?.let {
                    state.onRecipientRemoved(it)
                }
            },
            onFocusChanged = { isRecipientPickerFocused = it },
            onMenuOpenChanged = { isRecipientPickerOpened = it },
        )
        MultiSelectSearch(recipientPickerState)

        HorizonSpace(SpaceSize.SPACE_12)
    }
}

@Composable
private fun CourseRecipientSendIndividuallySection(state: HorizonInboxComposeUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        val checkboxState = CheckboxItemState(
            controlsContentState = ControlsContentState(
                title = stringResource(R.string.inboxComposeSendIndividuallyLabel)
            ),
            checked = state.isSendIndividually,
            onCheckedChanged = state.onSendIndividuallyChanged,
        )

        CheckboxItem(checkboxState)

        HorizonSpace(SpaceSize.SPACE_12)
    }
}

@Composable
private fun HorizonInboxComposeTextSection(state: HorizonInboxComposeUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        var isSubjectFieldFocused by remember { mutableStateOf(false) }
        val subjectFieldState = TextFieldState(
            value = state.subject,
            placeHolderText = stringResource(R.string.inboxComposeSubjectLabel),
            size = TextFieldInputSize.Medium,
            isFocused = isSubjectFieldFocused,
            onValueChange = state.onSubjectChanged,
            onFocusChanged = { isSubjectFieldFocused = it },
        )
        TextField(subjectFieldState)

        HorizonSpace(SpaceSize.SPACE_12)

        var isBodyAreaFocused by remember { mutableStateOf(false) }
        val bodyAreaState = TextAreaState(
            value = state.body,
            placeHolderText = stringResource(R.string.inboxComposeMessageLabel),
            isFocused = isBodyAreaFocused,
            onValueChange = state.onBodyChanged,
            onFocusChanged = { isBodyAreaFocused = it },
        )
        TextArea(
            state = bodyAreaState,
            minLines = 5
        )

        HorizonSpace(SpaceSize.SPACE_12)
    }
}

@Composable
private fun HorizonInboxComposeControlsSection(state: HorizonInboxComposeUiState, navController: NavHostController) {
    Column {
        HorizonDivider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                label = stringResource(R.string.inboxComposeCancelLabel),
                color = ButtonColor.Inverse,
                onClick = {
                    navController.popBackStack()
                },
            )

            HorizonSpace(SpaceSize.SPACE_8)

            Button(
                label = stringResource(R.string.inboxComposeSendLabel),
                color = ButtonColor.Institution,
                onClick = {
                    state.onSendConversation()
                }
            )
        }
    }
}