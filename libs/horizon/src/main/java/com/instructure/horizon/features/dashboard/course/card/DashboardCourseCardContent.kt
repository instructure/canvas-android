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
package com.instructure.horizon.features.dashboard.course.card

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
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
import com.instructure.horizon.horizonui.animation.shimmerEffect
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.LoadingButton
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillSize
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.horizonui.molecules.ProgressBar
import com.instructure.horizon.horizonui.molecules.ProgressBarNumberStyle
import com.instructure.pandautils.utils.localisedFormatMonthDay
import com.instructure.pandautils.utils.toFormattedString
import java.util.Date

@Composable
fun DashboardCourseCardContent(
    state: DashboardCourseCardState,
    handleOnClickAction: (CardClickAction?) -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCourseCard(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = state.onClickAction != null) {
                    handleOnClickAction(state.onClickAction)
                }
        ) {
            if (!state.imageUrl.isNullOrEmpty()) {
                CourseImage(imageUrl = state.imageUrl)
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            ) {
                if (!state.parentPrograms.isNullOrEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    ProgramsText(state.parentPrograms, handleOnClickAction)
                }
                if (state.title.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    TitleText(state.title)
                }
                if (!state.description.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    DescriptionText(state.description)
                }
                if (state.progress != null) {
                    Spacer(Modifier.height(8.dp))
                    CourseProgress(state.progress)
                }
                if (state.moduleItem != null) {
                    Spacer(Modifier.height(16.dp))
                    ModuleItemCard(state.moduleItem, handleOnClickAction)
                }
                if (state.buttonState != null) {
                    Spacer(Modifier.height(16.dp))
                    DashboardCardButton(state.buttonState, handleOnClickAction)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun CourseImage(imageUrl: String) {
    var isLoading by rememberSaveable { mutableStateOf(true) }
    GlideImage(
        imageUrl,
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        requestBuilderTransform = { it.addListener( object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                isLoading = false
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                isLoading = false
                return false
            }

        }) },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.69f)
            .shimmerEffect(isLoading)
    )
}

@Composable
private fun ProgramsText(
    programs: List<DashboardCourseCardParentProgramState>,
    handleOnClickAction: (CardClickAction?) -> Unit,
) {
    val programsAnnotated = buildAnnotatedString {
        programs.forEachIndexed { i, program ->
            if (i > 0) append(", ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = program.programId,
                    styles = TextLinkStyles(
                        style = SpanStyle(textDecoration = TextDecoration.Underline)
                    ),
                    linkInteractionListener = { _ -> handleOnClickAction(program.onClickAction) }
                )
            ) {
                append(program.programName)
            }
        }
    }

    // String resource can't work with annotated string so we need a temporary placeholder
    val template = stringResource(R.string.learnScreen_partOfProgram, "__PROGRAMS__")

    val fullText = buildAnnotatedString {
        val parts = template.split("__PROGRAMS__")
        append(parts[0])
        append(programsAnnotated)
        if (parts.size > 1) append(parts[1])
    }

    Text(style = HorizonTypography.p1, text = fullText)
}

@Composable
private fun TitleText(title: String) {
    Text(
        text = title,
        style = HorizonTypography.h3,
        color = HorizonColors.Text.title(),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun DescriptionText(description: String) {
    Text(
        text = description,
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
    )
}

@Composable
private fun CourseProgress(progress: Double) {
    Column(Modifier.fillMaxWidth()){
        Text(
            text = "${progress.toFormattedString()}% complete",
            style = HorizonTypography.p1,
            color = HorizonColors.Text.title()
        )
        Spacer(Modifier.height(8.dp))
        ProgressBar(progress = progress, numberStyle = ProgressBarNumberStyle.OFF)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModuleItemCard(
    state: DashboardCourseCardModuleItemState,
    handleOnClickAction: (CardClickAction?) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HorizonColors.Surface
                    .institution()
                    .copy(alpha = 0.1f), shape = HorizonCornerRadius.level2
            )
            .clip(HorizonCornerRadius.level2)
            .clickable { handleOnClickAction(state.onClickAction) }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = state.moduleItemTitle,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body()
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Pill(
                    label = stringResource(state.moduleItemType.stringRes),
                    size = PillSize.SMALL,
                    style = PillStyle.SOLID,
                    type = PillType.INVERSE,
                    case = PillCase.TITLE,
                    iconRes = state.moduleItemType.iconRes,
                )

                if (state.dueDate != null) {
                    Pill(
                        label = stringResource(R.string.learningobject_dueDate, state.dueDate.localisedFormatMonthDay()),
                        size = PillSize.SMALL,
                        style = PillStyle.SOLID,
                        type = PillType.INVERSE,
                        case = PillCase.TITLE,
                        iconRes = R.drawable.calendar_today,
                    )
                }

                if (state.estimatedDuration != null) {
                    Pill(
                        label = state.estimatedDuration,
                        size = PillSize.SMALL,
                        style = PillStyle.SOLID,
                        type = PillType.INVERSE,
                        case = PillCase.TITLE,
                        iconRes = R.drawable.calendar_today,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardCardButton(
    state: DashboardCourseCardButtonState,
    handleOnClickAction: (CardClickAction?) -> Unit
) {
    LoadingButton(
        label = state.label,
        height = ButtonHeight.NORMAL,
        width = ButtonWidth.RELATIVE,
        color = ButtonColor.BlackOutline,
        iconPosition = if (state.iconRes != null) ButtonIconPosition.End(state.iconRes) else ButtonIconPosition.NoIcon,
        onClick = { handleOnClickAction(state.onClickAction) },
        contentAlignment = Alignment.Center,
        loading = state.isLoading,
    )
}

@Composable
@Preview
private fun DashboardCourseCardWithModulePreview() {
    ContextKeeper.appContext = LocalContext.current

    val state = DashboardCourseCardState(
        parentPrograms = listOf(
            DashboardCourseCardParentProgramState(
                programName = "Program Name",
                programId = "1",
                onClickAction = CardClickAction.Action({})
            )
        ),
        imageUrl = null,
        title = "Course Title That Might Be Really Long and Go On Two Lines",
        description = "This is a description of the course. It might be really long and go on multiple lines.",
        progress = 45.0,
        moduleItem = DashboardCourseCardModuleItemState(
            moduleItemTitle = "Module Item Title That Might Be Really Long and Go On Two Lines",
            moduleItemType = com.instructure.horizon.model.LearningObjectType.ASSIGNMENT,
            dueDate = Date(),
            estimatedDuration = "5 mins",
            onClickAction = CardClickAction.Action({})
        ),
        buttonState = DashboardCourseCardButtonState(
            label = "Go to Course",
            iconRes = R.drawable.arrow_forward,
            onClickAction = CardClickAction.Action({})
        ),
        onClickAction = CardClickAction.Action({})
    )
    DashboardCourseCardContent(state, {})
}