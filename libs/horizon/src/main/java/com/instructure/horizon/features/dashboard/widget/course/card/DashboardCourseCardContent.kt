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
package com.instructure.horizon.features.dashboard.widget.course.card

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardCard
import com.instructure.horizon.features.dashboard.widget.DashboardWidgetPageState
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.isWideLayout
import com.instructure.horizon.horizonui.molecules.ProgressBarSmall
import com.instructure.horizon.horizonui.molecules.ProgressBarStyle
import com.instructure.horizon.horizonui.molecules.StatusChip
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.molecules.StatusChipState
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.utils.localisedFormatMonthDay
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun DashboardCourseCardContent(
    state: DashboardCourseCardState,
    handleOnClickAction: (CardClickAction?) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = state.onClickAction != null) {
                    handleOnClickAction(state.onClickAction)
                }
        ) {
            BoxWithConstraints {
                if (this.isWideLayout) {
                    DashboardCourseCardWideContent(state, isLoading, handleOnClickAction)
                } else {
                    DashboardCourseCardCompactContent(state, isLoading, handleOnClickAction)
                }
            }
        }
    }
}

@Composable
private fun DashboardCourseCardCompactContent(
    state: DashboardCourseCardState,
    isLoading: Boolean,
    handleOnClickAction: (CardClickAction?) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ){
        ImageWithProgramChips(state, isLoading, Modifier.fillMaxWidth())
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
        ) {
            if (!state.title.isNullOrEmpty()) {
                HorizonSpace(SpaceSize.SPACE_16)
                TitleText(state.title, isLoading)
            }
            if (state.progress != null) {
                HorizonSpace(SpaceSize.SPACE_12)
                CourseProgress(state.progress, isLoading)
            }
            if (state.descriptionState != null) {
                HorizonSpace(SpaceSize.SPACE_16)
                DescriptionText(state.descriptionState, isLoading)
            }
            if (state.moduleItem != null) {
                HorizonSpace(SpaceSize.SPACE_16)
                ModuleItemCard(state.moduleItem, isLoading, handleOnClickAction)
            }
            if (state.pageState != DashboardWidgetPageState.Empty) {
                HorizonSpace(SpaceSize.SPACE_16)
                PageIndicator(state.pageState, isLoading)
            }
        }
        HorizonSpace(SpaceSize.SPACE_24)
    }
}

@Composable
private fun DashboardCourseCardWideContent(
    state: DashboardCourseCardState,
    isLoading: Boolean,
    handleOnClickAction: (CardClickAction?) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageWithProgramChips(
                state,
                isLoading,
                Modifier
                    .width(320.dp)
                    .padding(start = 24.dp, top = 24.dp, end = 16.dp)
                    .clip(HorizonCornerRadius.level2)
            )
            Column(
                modifier = Modifier
                    .padding(end = 24.dp, top = 24.dp)
            ) {
                if (!state.title.isNullOrEmpty()) {
                    TitleText(state.title, isLoading)
                }
                if (state.progress != null) {
                    HorizonSpace(SpaceSize.SPACE_12)
                    CourseProgress(state.progress, isLoading)
                }
                if (state.descriptionState != null) {
                    HorizonSpace(SpaceSize.SPACE_16)
                    DescriptionText(state.descriptionState, isLoading)
                }
                if (state.moduleItem != null) {
                    HorizonSpace(SpaceSize.SPACE_16)
                    ModuleItemCard(state.moduleItem, isLoading, handleOnClickAction)
                }
            }
        }
        if (state.pageState != DashboardWidgetPageState.Empty) {
            HorizonSpace(SpaceSize.SPACE_16)
            PageIndicator(state.pageState, isLoading)
        }
        HorizonSpace(SpaceSize.SPACE_24)
    }
}

@Composable
private fun ImageWithProgramChips(
    state: DashboardCourseCardState,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        CourseImage(state.imageState, isLoading)

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(24.dp)
        ) {
            // Display only 3 programs at most
            state.parentPrograms?.take(3)?.forEach { program ->
                ProgramChip(program, isLoading)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CourseImage(
    state: DashboardCourseCardImageState,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var isImageLoading by rememberSaveable { mutableStateOf(true) }
    if (!state.imageUrl.isNullOrEmpty()) {
        GlideImage(
            state.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            requestBuilderTransform = {
                it.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
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
                .shimmerEffect(isLoading || isImageLoading)
        )
    } else {
        if (state.showPlaceholder) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .aspectRatio(1.69f)
                    .background(HorizonColors.Surface.institution().copy(alpha = 0.1f))
                    .shimmerEffect(isLoading)
            ) {
                Icon(
                    painterResource(R.drawable.book_2_filled),
                    contentDescription = null,
                    tint = HorizonColors.Surface.institution(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TitleText(
    title: String,
    isLoading: Boolean,
) {
    Text(
        text = title,
        style = HorizonTypography.labelLargeBold,
        color = HorizonColors.Text.title(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.shimmerEffect(isLoading)
    )
}

@Composable
private fun DescriptionText(
    descriptionState: DashboardCourseCardDescriptionState,
    isLoading: Boolean,
) {
    Column {
        Text(
            text = descriptionState.descriptionTitle,
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.title(),
            modifier = Modifier.shimmerEffect(isLoading)
        )
        HorizonSpace(SpaceSize.SPACE_8)
        Text(
            text = descriptionState.description,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier.shimmerEffect(isLoading)
        )
    }
}

@Composable
private fun CourseProgress(
    progress: Double,
    isLoading: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.shimmerEffect(
            isLoading,
            backgroundColor = HorizonColors.Surface.institution().copy(0.1f),
            shimmerColor = HorizonColors.Surface.institution().copy(0.05f),
        )
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
private fun PageIndicator(
    pageState: DashboardWidgetPageState,
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Text(
            stringResource(
                R.string.dashboardPaginatedWidgetPagerMessage,
                pageState.currentPageNumber,
                pageState.pageCount
            ),
            style = HorizonTypography.p2,
            color = HorizonColors.Text.dataPoint(),
            modifier = Modifier.shimmerEffect(isLoading),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModuleItemCard(
    state: DashboardCourseCardModuleItemState,
    isLoading: Boolean,
    handleOnClickAction: (CardClickAction?) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HorizonColors.Surface
                    .institution()
                    .copy(alpha = 0.1f),
                shape = HorizonCornerRadius.level2
            )
            .clip(HorizonCornerRadius.level2)
            .clickable { handleOnClickAction(state.onClickAction) }
            .shimmerEffect(
                isLoading,
                backgroundColor = HorizonColors.Surface.institution().copy(0.1f),
                shimmerColor = HorizonColors.Surface.institution().copy(0.05f),
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = state.moduleItemTitle,
                style = HorizonTypography.p2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = HorizonColors.Text.body()
            )
            Spacer(Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.estimatedDuration != null) {
                        StatusChip(
                            StatusChipState(
                                label = state.estimatedDuration,
                                color = StatusChipColor.White,
                                fill = true,
                                iconRes = R.drawable.schedule,
                            )
                        )
                    }
                    if (state.dueDate != null) {
                        StatusChip(
                            StatusChipState(
                                label = stringResource(R.string.learningobject_dueDate, state.dueDate.localisedFormatMonthDay()),
                                color = StatusChipColor.White,
                                fill = true,
                                iconRes = R.drawable.calendar_today,
                            )
                        )
                    } else {
                        StatusChip(
                            StatusChipState(
                                label = stringResource(R.string.dashboardCourseCardModuleItemNoDueDateLabel),
                                color = StatusChipColor.White,
                                fill = true,
                                iconRes = R.drawable.calendar_today,
                            )
                        )
                    }
                }
                StatusChip(
                    StatusChipState(
                        label = stringResource(state.moduleItemType.stringRes),
                        color = StatusChipColor.White,
                        fill = true,
                        iconRes = state.moduleItemType.iconRes,
                    )
                )
            }
        }
    }
}

@Composable
private fun ProgramChip(
    program: DashboardCourseCardParentProgramState,
    isLoading: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = HorizonColors.Surface.pageSecondary(),
                shape = HorizonCornerRadius.level1
            )
            .border(1.dp, HorizonColors.LineAndBorder.lineStroke(), HorizonCornerRadius.level1)
            .shimmerEffect(isLoading)
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Text(
            stringResource(R.string.dashboardCourseCardProgramPrefix),
            style = HorizonTypography.labelMediumBold,
            color = HorizonColors.Text.title()
        )
        Text(program.programName,
            style = HorizonTypography.p2,
            color = HorizonColors.Text.title(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview
private fun DashboardCourseCardWithModuleCompactPreview() {
    ContextKeeper.appContext = LocalContext.current

    val state = DashboardCourseCardState(
        parentPrograms = listOf(
            DashboardCourseCardParentProgramState(
                programName = "Program Name",
                programId = "1",
                onClickAction = CardClickAction.Action({})
            ),
            DashboardCourseCardParentProgramState(
                programName = "Program Name to test the overflow behaviour in the chip",
                programId = "2",
                onClickAction = CardClickAction.Action({})
            )
        ),
        imageState = DashboardCourseCardImageState(
            imageUrl = null,
            showPlaceholder = true
        ),
        title = "Course Title That Might Be Really Long and Go On Two Lines",
        progress = 45.0,
        moduleItem = DashboardCourseCardModuleItemState(
            moduleItemTitle = "Module Item Title That Might Be Really Long and Go On Two Lines",
            moduleItemType = LearningObjectType.ASSIGNMENT,
            dueDate = Date(),
            estimatedDuration = "32 Hours 25 Mins",
            onClickAction = CardClickAction.Action({})
        ),
        onClickAction = CardClickAction.Action({})
    )
    DashboardCourseCardContent(state, {}, false)
}

@Composable
@Preview(widthDp = 720)
private fun DashboardCourseCardWithModuleWidePreview() {
    ContextKeeper.appContext = LocalContext.current

    val state = DashboardCourseCardState(
        parentPrograms = listOf(
            DashboardCourseCardParentProgramState(
                programName = "Program Name",
                programId = "1",
                onClickAction = CardClickAction.Action({})
            ),
            DashboardCourseCardParentProgramState(
                programName = "Program Name to test the overflow behaviour in the chip",
                programId = "2",
                onClickAction = CardClickAction.Action({})
            )
        ),
        imageState = DashboardCourseCardImageState(
            imageUrl = null,
            showPlaceholder = true
        ),
        title = "Course Title That Might Be Really Long and Go On Two Lines",
        progress = 45.0,
        moduleItem = DashboardCourseCardModuleItemState(
            moduleItemTitle = "Module Item Title That Might Be Really Long and Go On Two Lines",
            moduleItemType = LearningObjectType.ASSIGNMENT,
            dueDate = Date(),
            estimatedDuration = "32 Hours 25 Mins",
            onClickAction = CardClickAction.Action({})
        ),
        onClickAction = CardClickAction.Action({})
    )
    DashboardCourseCardContent(state, {}, false)
}