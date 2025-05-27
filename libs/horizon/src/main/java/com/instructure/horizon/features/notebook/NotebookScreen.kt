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
package com.instructure.horizon.features.notebook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NotebookAppBar
import com.instructure.horizon.features.notebook.common.composable.NotebookTypeSelect
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize

@Composable
fun NotebookScreen(
    mainNavController: NavHostController,
    state: NotebookUiState,
) {
    Scaffold(
        topBar = { NotebookAppBar(navigateBack = { mainNavController.popBackStack() }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                FilterContent(
                    state.selectedFilter,
                    state.onFilterSelected
                )
            }
        }
    }
}

@Composable
private fun FilterContent(
    selectedFilter: NotebookType?,
    onFilterSelected: (NotebookType?) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.notebookFilterLabel),
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.title()
        )

        HorizonSpace(SpaceSize.SPACE_12)

        Row {
            NotebookTypeSelect(
                type = NotebookType.Important,
                isSelected = selectedFilter == NotebookType.Important,
                onSelect = { onFilterSelected(if (selectedFilter == NotebookType.Important) null else NotebookType.Important) },
                modifier = Modifier.weight(1f)
            )

            HorizonSpace(SpaceSize.SPACE_12)

            NotebookTypeSelect(
                type = NotebookType.Confusing,
                isSelected = selectedFilter == NotebookType.Confusing,
                onSelect = { onFilterSelected(if (selectedFilter == NotebookType.Confusing) null else NotebookType.Confusing) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}