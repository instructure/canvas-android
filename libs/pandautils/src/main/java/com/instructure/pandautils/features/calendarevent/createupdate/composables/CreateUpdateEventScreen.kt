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

package com.instructure.pandautils.features.calendarevent.createupdate.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.SelectCalendarScreen
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventAction
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventUiState


@Composable
internal fun CreateUpdateEventScreenWrapper(
    title: String,
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    navigationAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        if (uiState.selectCalendarUiState.show) {
            SelectCalendarScreen(
                uiState = uiState.selectCalendarUiState,
                onCalendarSelected = {

                },
                navigationActionClick = {

                },
                modifier = modifier
            )
        } else {
            CreateUpdateEventScreen(
                title = title,
                uiState = uiState,
                actionHandler = actionHandler,
                navigationAction = navigationAction,
                modifier = modifier
            )
        }
    }
}

@Composable
internal fun CreateUpdateEventScreen(
    title: String,
    uiState: CreateUpdateEventUiState,
    actionHandler: (CreateUpdateEventAction) -> Unit,
    navigationAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    
}
