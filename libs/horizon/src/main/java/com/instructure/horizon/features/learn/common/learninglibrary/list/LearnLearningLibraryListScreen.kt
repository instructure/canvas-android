/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.common.learninglibrary.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonBorderShadow
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.organisms.CollapsableHeaderScreen

@Composable
fun LearnLearningLibraryListScreen(
    state: LearnLearningLibraryListUiState,
    navController: NavHostController
) {
    CollapsableHeaderScreen(
        headerContent = {
            LearnLearningLibraryListFilterRow(state, navController)
        },
        bodyContent = {

        }
    )
}

@Composable
private fun LearnLearningLibraryListFilterRow(
    state: LearnLearningLibraryListUiState,
    navController: NavHostController
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(HorizonColors.Surface.pagePrimary())
            .horizonBorderShadow(HorizonColors.LineAndBorder.lineStroke(), bottom = 1.dp)
            .padding(horizontal = 24.dp)
    ) {
        LearnSearchBar(
            value = state.searchQuery,
            onValueChange = state.updateSearchQuery,
            placeholder = stringResource(R.string.learnLearningLibraryListSearchPlaceholder)
        )
        HorizonSpace(SpaceSize.SPACE_16)
        IconButton(
            iconRes = R.drawable.bookmarks,
            size = IconButtonSize.NORMAL,
            elevation = HorizonElevation.level4,
            contentDescription = stringResource(R.string.a11y_learnLearningLibraryBookmarkContentDescription),
            onClick = {
                // TODO
            }
        )
        HorizonSpace(SpaceSize.SPACE_8)
        IconButton(
            iconRes = R.drawable.history,
            size = IconButtonSize.NORMAL,
            elevation = HorizonElevation.level4,
            contentDescription = stringResource(R.string.a11y_learnLearningLibraryCompletedContentDescription),
            onClick = {
                // TODO
            }
        )
    }
}

@Composable
private fun LearnLearningLibraryListContent(
    state: LearnLearningLibraryListUiState,
    navController: NavHostController
) {

}