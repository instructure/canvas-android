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

package com.instructure.pandautils.features.todo.details.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.features.todo.details.ToDoUiState

@Composable
internal fun ToDoContent(
    toDoUiState: ToDoUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = colorResource(id = R.color.backgroundLightest)
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = toDoUiState.title,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 22.sp
            )
            if (!toDoUiState.contextName.isNullOrEmpty() && toDoUiState.contextColor != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = toDoUiState.contextName,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(toDoUiState.contextColor),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.todoDateLabel),
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = toDoUiState.date,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
            if (toDoUiState.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.todoDescriptionLabel),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = toDoUiState.description,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
