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
package com.instructure.pandautils.features.speedgrader.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.pandautils.R

@Composable
fun SpeedGraderCommentsScreen() {
    // This is a placeholder for the SpeedGrader Comments Screen.
    // The actual implementation will be added later.
    // For now, we can display a simple text or a loading indicator.

    val viewModel: SpeedGraderCommentsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current.applicationContext

    LazyColumn(
        modifier = Modifier.background(colorResource(id = R.color.backgroundLightest))
    ) {
        item {
            Text(
                text = "SpeedGrader Comments Screen",
                modifier = Modifier.padding(16.dp),
                color = colorResource(id = R.color.textDarkest)
            )
        }
        items(uiState.comments) { comment ->
            Text(
                text = comment.content,
                modifier = Modifier.padding(16.dp),
                color = colorResource(id = R.color.textDarkest)
            )
        }
    }
}