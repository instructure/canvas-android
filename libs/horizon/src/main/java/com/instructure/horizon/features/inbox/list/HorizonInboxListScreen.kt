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
package com.instructure.horizon.features.inbox.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.utils.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonInboxListScreen(
    state: HorizonInboxListUiState,
    navController: NavHostController
) {
    Scaffold { padding ->
        LoadingStateWrapper(state.loadingState) {
            if (state.items.isEmpty()) {
                Text("Empty")
            } else {
                InboxContent(state, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Spinner(color = HorizonColors.Surface.institution())
    }
}

@Composable
private fun InboxContent(state: HorizonInboxListUiState, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(state.items) { item ->
            InboxContentItem(item)
        }
    }
}

@Composable
private fun InboxContentItem(item: HorizonInboxListItemState) {
    Column {
        Text(
            text = item.date.format("MMM dd, yyyy"),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.timestamp(),
        )

        Text(
            text = item.title,
            style = HorizonTypography.labelMediumBold,
            color = HorizonColors.Text.body(),
        )

        Text(
            text = item.description,
            style = HorizonTypography.labelMediumBold,
            color = HorizonColors.Text.body(),
        )
    }
}