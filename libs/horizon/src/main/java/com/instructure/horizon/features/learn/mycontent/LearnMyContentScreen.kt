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
package com.instructure.horizon.features.learn.mycontent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryItem
import com.instructure.horizon.features.learn.learninglibrary.common.LearningLibraryRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.FilterChip
import com.instructure.horizon.horizonui.molecules.LoadingImage
import com.instructure.horizon.horizonui.molecules.ProgressBarSmallInline
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.horizon.horizonui.organisms.scaffolds.CollapsableHeaderScreen

@Composable
fun LearnMyContentScreen(
    state: LearnMyContentUiState,
    navController: NavHostController,
) {
    var selectedTab by remember { mutableStateOf(LearnMyContentTab.InProgress) }
    CollapsableHeaderScreen(
        headerContent = { paddingValues ->
            LearnMyContentTabSelector(
                selectedTab,
                { selectedTab = it },
                Modifier.padding(paddingValues)
            )
        },
        bodyContent = { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LearnSearchBar(
                            state.searchQuery,
                            state.updateSearchQuery,
                            placeholder = stringResource(R.string.learnMyContentSearchLabel)
                        )
                    }
                }
                when(selectedTab) {
                    LearnMyContentTab.InProgress -> {
                        items(state.inProgressState.contentCards) { card ->
                            LearnMyContentCard(card, navController)
                        }
                    }

                    LearnMyContentTab.Completed -> {
                        items(state.completedState.contentCards) { card ->
                            LearnMyContentCard(card, navController)
                        }

                    }
                    LearnMyContentTab.Saved -> {
                        items(state.savedState.contentCards) { card ->
                            LearnLearningLibraryItem(
                                card,
                                {
                                    when (card.route) {
                                        is LearningLibraryRoute.ObjectRoute -> { navController.navigate(card.route.route) }
                                        is LearningLibraryRoute.StringRoute -> { navController.navigate(card.route.route) }
                                        null -> {}
                                    }
                                },
                                onBookmarkClick = {},
                                onEnrollClick = {},
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun LearnMyContentTabSelector(
    selectedTab: LearnMyContentTab,
    onTabSelected: (LearnMyContentTab) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LearnMyContentTab.entries.forEach { tab ->
            val tabLabel = when (tab) {
                LearnMyContentTab.InProgress -> stringResource(R.string.LearnMyContentInProgressLabel)
                LearnMyContentTab.Completed -> stringResource(R.string.LearnMyContentCompletedLabel)
                LearnMyContentTab.Saved -> stringResource(R.string.LearnMyContentSavedLabel)
            }
            FilterChip(
                label = tabLabel,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Composable
private fun LearnMyContentCard(
    cardState: LearnContentCardState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier
        .horizonShadow(HorizonElevation.level4, shape = HorizonCornerRadius.level4)
        .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level4)
        .clickable(onClick = { navController.navigate(cardState.route) })
    ) {
        Column(Modifier.fillMaxWidth()) {
            if (cardState.imageUrl != null) {
                LoadingImage(cardState.imageUrl)
                HorizonSpace(SpaceSize.SPACE_16)
            } else {
                HorizonSpace(SpaceSize.SPACE_24)
            }

            Text(
                text = cardState.name,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.title(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            HorizonSpace(SpaceSize.SPACE_12)

            if (cardState.progress != null) {
                ProgressBarSmallInline(cardState.progress)
                HorizonSpace(SpaceSize.SPACE_16)
            }

            if (cardState.cardChips.isNotEmpty()) {
                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cardState.cardChips.forEach {
                        StatusChip(
                            StatusChipState(
                                label = it.label,
                                color = it.color,
                                iconRes = it.iconRes,
                                fill = true,
                            )
                        )
                    }
                }
                HorizonSpace(SpaceSize.SPACE_16)
            }

            if (cardState.buttonLabel != null) {
                Button(
                    modifier = modifier,
                    label = cardState.buttonLabel,
                    height = ButtonHeight.NORMAL,
                    width = ButtonWidth.FILL,
                    color = ButtonColor.WhiteWithOutline,
                    onClick = {
                        navController.navigate(cardState.route)
                    },
                )
            }

            HorizonSpace(SpaceSize.SPACE_24)
        }
    }
}