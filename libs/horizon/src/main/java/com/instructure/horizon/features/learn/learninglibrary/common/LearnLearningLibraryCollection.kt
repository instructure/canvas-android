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
package com.instructure.horizon.features.learn.learninglibrary.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.pandautils.compose.modifiers.conditional

data class LearnLearningLibraryCollectionState(
    val id: String,
    val name: String,
    val itemCount: Int,
    val items: List<LearnLearningLibraryCollectionItemState>
)

fun LazyListScope.LearnLearningLibraryCollection(
    collections: List<LearnLearningLibraryCollectionState>,
    onBookmarkClick: (itemId: String) -> Unit,
    onEnrollClick: (itemId: String) -> Unit,
    onItemClick: (itemId: String) -> Unit,
    onCollectionDetailsClick: (itemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (collections.isEmpty()) {
        item { EmptyMessage() }
    }

    items(collections) { collectionItem ->
        LearnLearningLibraryCollectionItem(
            collectionItem,
            collections.size > 1,
            onBookmarkClick,
            onEnrollClick,
            onItemClick,
            onCollectionDetailsClick,
            modifier
        )
    }
}

@Composable
private fun LearnLearningLibraryCollectionItem(
    state: LearnLearningLibraryCollectionState,
    isCollapsable: Boolean,
    onBookmarkClick: (itemId: String) -> Unit,
    onEnrollClick: (itemId: String) -> Unit,
    onItemClick: (itemId: String) -> Unit,
    onCollectionDetailsClick: (itemId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier.fillMaxWidth()
    ) {
        LearnLearningLibraryCollectionTitle(
            state.name,
            isCollapsable,
            isExpanded,
            onExpandedChanged = { isExpanded = it }
        )
        HorizonSpace(SpaceSize.SPACE_24)

        state.items.forEach { itemState ->
            LearnLearningLibraryItem(
                state = itemState,
                onClick = { onItemClick(itemState.id) },
                onBookmarkClick = { onBookmarkClick(itemState.id) },
                onEnrollClick = { onEnrollClick(itemState.id) }
            )
            HorizonSpace(SpaceSize.SPACE_24)
        }
        LearnLearningLibraryCollectionDetailsRow(
            state.itemCount,
            onCollectionDetailsClick = { onCollectionDetailsClick(state.id) }
        )
        HorizonSpace(SpaceSize.SPACE_24)

        if (isCollapsable) {
            HorizonDivider()
            HorizonSpace(SpaceSize.SPACE_24)
        }
    }
}

@Composable
private fun LearnLearningLibraryCollectionTitle(
    title: String,
    isCollapsable: Boolean,
    isExpanded: Boolean,
    onExpandedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .conditional(isCollapsable) {
                clickable { onExpandedChanged(!isExpanded) }
            }
    ) {
        Text(
            text = title,
            style = HorizonTypography.h3,
            color = HorizonColors.Text.body(),
            modifier = Modifier.weight(1f)
        )

        if (isCollapsable) {
            val rotationAnimation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
            )
            HorizonSpace(SpaceSize.SPACE_16)
            IconButton(
                iconRes = R.drawable.keyboard_arrow_up,
                contentDescription = if (isExpanded)
                    stringResource(R.string.a11y_learnLearningLibraryCollectionCollapseContentDescription)
                else
                    stringResource(R.string.a11y_learnLearningLibraryCollectionExpandContentDescription),
                color = IconButtonColor.WhiteGreyOutline,
                onClick = { onExpandedChanged(!isExpanded) },
                modifier = Modifier
                    .rotate(rotationAnimation)
            )
        }
    }
}

@Composable
private fun LearnLearningLibraryCollectionDetailsRow(
    itemCount: Int,
    onCollectionDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pluralStringResource(R.plurals.learnLearningLibraryCollectionItemCount, itemCount, itemCount),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.dataPoint(),
            modifier = Modifier.weight(1f)
        )
        Button(
            label = stringResource(R.string.learnLearningLibraryCollectionViewCollectionLabel),
            height = ButtonHeight.SMALL,
            width = ButtonWidth.RELATIVE,
            color = ButtonColor.WhiteWithOutline,
            iconPosition = ButtonIconPosition.End(R.drawable.arrow_forward),
            onClick = { onCollectionDetailsClick() }
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun LearnLearningLibraryCollectionSingleItem() {
    ContextKeeper.appContext = LocalContext.current
    val collections = listOf(
        LearnLearningLibraryCollectionState(
            id = "1",
            name = "Collection 1",
            itemCount = 1,
            items = listOf(
                LearnLearningLibraryCollectionItemState(
                    id = "1",
                    imageUrl = null,
                    name = "Collection Item 1",
                    isBookmarked = true,
                    canEnroll = true,
                    isCompleted = false,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Recommended"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Required"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Test chip"
                        )
                    )
                ),
                LearnLearningLibraryCollectionItemState(
                    id = "2",
                    imageUrl = null,
                    name = "Collection Item 2",
                    isBookmarked = false,
                    canEnroll = false,
                    isCompleted = true,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Recommended"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Required"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Test chip"
                        )
                    )
                ),
            )
        )
    )
    LazyColumn {
        LearnLearningLibraryCollection(
            collections,
            {},
            {},
            {},
            {}
        )
    }
}

@Composable
@Preview(showBackground = true, heightDp = 2000)
private fun LearnLearningLibraryCollectionMultipleItems() {
    ContextKeeper.appContext = LocalContext.current
    val collections = listOf(
        LearnLearningLibraryCollectionState(
            id = "1",
            name = "Collection 1",
            itemCount = 2,
            items = listOf(
                LearnLearningLibraryCollectionItemState(
                    id = "1",
                    imageUrl = null,
                    name = "Collection Item 1",
                    isBookmarked = true,
                    canEnroll = true,
                    isCompleted = false,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Recommended"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Required"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Test chip"
                        )
                    )
                ),
                LearnLearningLibraryCollectionItemState(
                    id = "2",
                    imageUrl = null,
                    name = "Collection Item 2",
                    isBookmarked = false,
                    canEnroll = false,
                    isCompleted = true,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Recommended"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Required"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Test chip"
                        )
                    )
                ),
            )
        ),
        LearnLearningLibraryCollectionState(
            id = "2",
            name = "Collection 2",
            itemCount = 2,
            items = listOf(
                LearnLearningLibraryCollectionItemState(
                    id = "1",
                    imageUrl = null,
                    name = "Collection Item 1",
                    isBookmarked = true,
                    canEnroll = true,
                    isCompleted = false,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Recommended"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Required"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Test chip"
                        )
                    )
                ),
                LearnLearningLibraryCollectionItemState(
                    id = "2",
                    imageUrl = null,
                    name = "Collection Item 2",
                    isBookmarked = false,
                    canEnroll = false,
                    isCompleted = true,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Recommended"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Required"
                        ),
                        LearnLearningLibraryCollectionItemChipState(
                            label = "Test chip"
                        )
                    )
                ),
            )
        )
    )
    LazyColumn {
        LearnLearningLibraryCollection(
            collections,
            {},
            {},
            {},
            {}
        )
    }
}

@Composable
private fun EmptyMessage() {
    Text(
        text = stringResource(R.string.learnLearningLibraryListEmptyMessage),
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}