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
package com.instructure.horizon.features.learn.course.list

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.common.LearnSearchBar
import com.instructure.horizon.features.learn.course.list.LearnCourseFilterOption.Companion.getProgressOption
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.animation.shimmerEffect
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
import com.instructure.horizon.horizonui.molecules.DropdownChip
import com.instructure.horizon.horizonui.molecules.DropdownItem
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle
import com.instructure.horizon.horizonui.organisms.CollapsableHeaderScreen
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import kotlin.math.roundToInt

@Composable
fun LearnCourseListScreen(
    state: LearnCourseListUiState,
    navController: NavHostController
) {
    CollapsableHeaderScreen(
        headerContent = {
            Searchbar(state)
        },
        bodyContent = {
            LearnCourseListContent(state, navController)
        }
    )
}

@Composable
private fun Searchbar(state: LearnCourseListUiState) {
    LearnSearchBar(
        value = state.searchQuery,
        onValueChange = { state.updateSearchQuery(it) },
        placeholder = stringResource(R.string.learnCourseListSearchBarPlaceholder),
        modifier = Modifier.padding(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnCourseListContent(
    state: LearnCourseListUiState,
    navController: NavHostController
) {
    LoadingStateWrapper(state.loadingState) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            stickyHeader {
                LearnCourseListFilter(state)
            }

            items(state.coursesToDisplay.take(state.visibleItemCount)) {
                LearnCourseCard(it, Modifier.padding(horizontal = 24.dp)) {
                    navController.navigate(LearnRoute.LearnCourseDetailsScreen.route(it.courseId))
                }
            }

            if (state.coursesToDisplay.size > state.visibleItemCount) {
                item {
                    Button(
                        label = stringResource(R.string.learnCourseListShowMoreLabel),
                        width = ButtonWidth.FILL,
                        height = ButtonHeight.SMALL,
                        color = ButtonColor.BlackOutline,
                        onClick = { state.increaseVisibleItemCount() },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnCourseListFilter(state: LearnCourseListUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(HorizonColors.Surface.pagePrimary())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        DropdownChip(
            items = LearnCourseFilterOption.entries.map {
                DropdownItem(it, stringResource(it.labelRes))
            },
            selectedItem = DropdownItem(
                state.selectedFilterValue,
                stringResource(state.selectedFilterValue.labelRes))
            ,
            onItemSelected = { state.updateFilterValue(it?.value ?: LearnCourseFilterOption.All) },
            dropdownWidth = 180.dp,
            verticalPadding = 6.dp,
            placeholder = ""
        )

        Text(
            text = state.coursesToDisplay.take(state.visibleItemCount).size.toString()
        )
    }
}

@Composable
private fun LearnCourseCard(
    course: LearnCourseState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) = {}
) {
    Box(modifier = modifier
        .horizonShadow(HorizonElevation.level4, shape = HorizonCornerRadius.level4)
        .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level4)
        .clickable(onClick = { onClick() })
    ) {
        Column(Modifier.fillMaxWidth()) {
            CourseImage(course.imageUrl)

            HorizonSpace(SpaceSize.SPACE_16)

            Text(
                text = course.courseName,
                style = HorizonTypography.labelLargeBold,
                color = HorizonColors.Text.title(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            HorizonSpace(SpaceSize.SPACE_12)

            CourseProgress(
                course.progress,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            HorizonSpace(SpaceSize.SPACE_16)

            LearnCourseCardButton(
                course,
                onClick,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            HorizonSpace(SpaceSize.SPACE_24)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CourseImage(
    url: String?,
    modifier: Modifier = Modifier
) {
    var isImageLoading by rememberSaveable { mutableStateOf(true) }
    if (!url.isNullOrEmpty()) {
        GlideImage(
            url,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            requestBuilderTransform = {
                it.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        isImageLoading = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        isImageLoading = false
                        return false
                    }

                })
            },
            modifier = modifier
                .aspectRatio(1.69f)
                .shimmerEffect(isImageLoading)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .aspectRatio(1.69f)
                .background(HorizonColors.Surface.institution().copy(alpha = 0.1f))
        ) {
            Icon(
                painterResource(R.drawable.book_2_filled),
                contentDescription = null,
                tint = HorizonColors.Surface.institution(),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun CourseProgress(
    progress: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressBarSmall(
            progress = progress,
            style = ProgressBarStyle.Institution,
            showLabels = false,
            modifier = Modifier.weight(1f)
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Text(
            text = stringResource(R.string.progressBar_percent, progress.roundToInt()),
            style = HorizonTypography.p2,
            color = HorizonColors.Surface.institution(),
        )
    }
}

@Composable
private fun LearnCourseCardButton(
    course: LearnCourseState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (course.progress.getProgressOption()) {
        LearnCourseFilterOption.NotStarted -> stringResource(R.string.learnCourseListStartLearningLabel)
        LearnCourseFilterOption.InProgress -> stringResource(R.string.learnCourseListResumeLearningLabel)
        else -> stringResource(R.string.learnCourseListViewCourseLabel)
    }

    Button(
        modifier = modifier,
        label = label,
        height = ButtonHeight.NORMAL,
        width = ButtonWidth.FILL,
        color = ButtonColor.WhiteWithOutline,
        onClick = onClick,
    )
}