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
package com.instructure.horizon.features.learn.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonBorder
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.FilterChip
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.organisms.scaffolds.EdgeToEdgeScaffold

@Composable
fun LearnLearningLibraryFilterScreen(
    state: LearnLearningLibraryFilterUiState,
    navController: NavHostController
) {
    EdgeToEdgeScaffold(
        statusBarColor = HorizonColors.Surface.pageSecondary(),
        navigationBarColor = HorizonColors.Surface.pageSecondary(),
        containerColor = HorizonColors.Surface.pageSecondary(),
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LearnLearningLibraryFilterTopBar { navController.popBackStack() }

            state.sections.forEach { section ->
                LearnLearningLibraryFilterSectionRow(section, Modifier.padding(horizontal = 16.dp))
            }

            Spacer(Modifier.weight(1f))

            LearnLearningLibraryButtonSection(
                { navController.popBackStack() },
                state.onClearFilters
            )
        }
    }
}

@Composable
private fun LearnLearningLibraryFilterTopBar(onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizonBorder(HorizonColors.LineAndBorder.lineStroke(), bottom = 1.dp)
            .background(HorizonColors.Surface.pageSecondary())
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.tune),
            contentDescription = null
        )
        HorizonSpace(SpaceSize.SPACE_8)
        Text(
            text = stringResource(R.string.learnLearningLibraryFilterAndSortTitle),
            style = HorizonTypography.h4,
            color = HorizonColors.Text.title(),
            modifier = Modifier.weight(1f)
        )
        IconButton(
            iconRes = R.drawable.close,
            size = IconButtonSize.SMALL,
            color = IconButtonColor.DarkOutline,
            contentDescription = stringResource(R.string.a11y_close),
            onClick = onClose
        )
    }
}

@Composable
private fun LearnLearningLibraryButtonSection(
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .horizonBorder(HorizonColors.LineAndBorder.lineStroke(), top = 1.dp)
            .background(HorizonColors.Surface.cardPrimary())
            .padding(top = 16.dp)

    ) {
        Button(
            label = stringResource(R.string.learnLearningLibraryFilterApplyFiltersLabel),
            height = ButtonHeight.SMALL,
            width = ButtonWidth.FILL,
            color = ButtonColor.Black,
            onClick = onApplyFilters,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizonSpace(SpaceSize.SPACE_4)

        Button(
            label = stringResource(R.string.learnLearningLibraryFilterClearFiltersLabel),
            height = ButtonHeight.SMALL,
            width = ButtonWidth.FILL,
            color = ButtonColor.BlackOutline,
            onClick = onClearFilters,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LearnLearningLibraryFilterSectionRow(section: LearnLearningLibraryFilterSection, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = section.title,
            style = HorizonTypography.labelMediumBold,
            color = HorizonColors.Text.body()
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            section.items.forEach { item ->
                FilterChip(
                    label = item.label,
                    selected = item.isSelected,
                    onClick = item.onSelected
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun LearnLearningLibraryFilterScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    LearnLearningLibraryFilterScreen(
        state = LearnLearningLibraryFilterUiState(
            sections = listOf(
                LearnLearningLibraryFilterSection(
                    title = "Sort by",
                    items = listOf(
                        LearnLearningLibraryFilterItem("Most recent", isSelected = true, onSelected = {}),
                        LearnLearningLibraryFilterItem("Least recent", isSelected = false, onSelected = {}),
                        LearnLearningLibraryFilterItem("Name: A-Z", isSelected = false, onSelected = {}),
                        LearnLearningLibraryFilterItem("Name: Z-A", isSelected = false, onSelected = {})
                    )
                ),
                LearnLearningLibraryFilterSection(
                    title = "Item type",
                    items = listOf(
                        LearnLearningLibraryFilterItem("All", isSelected = true, onSelected = {}),
                        LearnLearningLibraryFilterItem("Assessments", isSelected = false, onSelected = {}),
                        LearnLearningLibraryFilterItem("Courses", isSelected = false, onSelected = {}),
                        LearnLearningLibraryFilterItem("External links", isSelected = false, onSelected = {}),
                        LearnLearningLibraryFilterItem("External tools", isSelected = false, onSelected = {}),
                        LearnLearningLibraryFilterItem("Files", isSelected = false, onSelected = {}),
                        LearnLearningLibraryFilterItem("Pages", isSelected = false, onSelected = {})
                    )
                ),
            )
        ),
        navController = rememberNavController()
    )
}
