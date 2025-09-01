/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
@file:OptIn(ExperimentalLayoutApi::class)

package com.instructure.horizon.horizonui.organisms.cards

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillSize
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.model.LearningObjectStatus
import com.instructure.horizon.model.LearningObjectType

enum class ModuleStatus(@StringRes val stringRes: Int? = null, val pillStyle: PillStyle, @DrawableRes val pillIcon: Int? = null) {
    OPTIONAL(pillStyle = PillStyle.INLINE),
    NOT_STARTED(R.string.moduleStatus_notStarted, pillStyle = PillStyle.INLINE),
    IN_PROGRESS(R.string.moduleStatus_inProgress, pillStyle = PillStyle.OUTLINE),
    COMPLETED(R.string.moduleStatus_completed, pillStyle = PillStyle.SOLID),
    LOCKED(R.string.moduleStatus_locked, pillStyle = PillStyle.INLINE, pillIcon = R.drawable.lock),
}

data class ModuleHeaderState(
    val title: String,
    val status: ModuleStatus,
    val expanded: Boolean = false,
    val subtitle: String? = null,
    val itemCount: Int = 0,
    val pastDueCount: Int = 0,
    val remainingMinutes: String? = null,
    val onClick: (() -> Unit)? = null
)

// TODO We might need to change how we layout the items when we implement modules because there would be nested lazy lists.
// We will handle this when we implement the module list.
@Composable
fun ModuleContainer(state: ModuleHeaderState, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit = {}) {
    Card(
        shape = HorizonCornerRadius.level2,
        colors = CardDefaults.cardColors().copy(containerColor = HorizonColors.Surface.cardPrimary()),
        modifier = modifier
    ) {
        Column {
            val onClick = state.onClick
            val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

            Column(modifier = clickModifier.padding(16.dp)) {
                ModuleHeader(state = state)
                if (state.subtitle != null && state.expanded) {
                    HorizonSpace(SpaceSize.SPACE_24)
                    Text(text = state.subtitle, style = HorizonTypography.p2)
                }
            }
            AnimatedVisibility(
                state.expanded,
                label = "ModuleContainerContent",
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = HorizonColors.LineAndBorder.lineStroke()
                )
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ModuleHeader(state: ModuleHeaderState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        val iconRotation: Float by animateFloatAsState(targetValue = if (state.expanded) 0f else 180f, label = "expandIconRotation")
        Icon(painterResource(R.drawable.keyboard_arrow_up), modifier = Modifier
            .size(24.dp)
            .rotate(iconRotation), contentDescription = null)
        HorizonSpace(SpaceSize.SPACE_8)
        Column {
            if (state.status.stringRes != null) {
                Pill(
                    label = stringResource(state.status.stringRes),
                    type = PillType.INSTITUTION,
                    style = state.status.pillStyle,
                    iconRes = state.status.pillIcon,
                    case = PillCase.TITLE,
                    size = PillSize.SMALL
                )
                HorizonSpace(SpaceSize.SPACE_8)
            }

            AnimatedContent(
                state.expanded,
                label = "ModuleHeaderTitle",
                transitionSpec = { expandVertically(expandFrom = Alignment.Top, initialHeight = { 100 }) togetherWith shrinkVertically(shrinkTowards = Alignment.Top, targetHeight = {100}) }
            ) { expanded ->
                Text(
                    text = state.title,
                    style = HorizonTypography.labelLargeBold,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HorizonSpace(SpaceSize.SPACE_8)
            FlowRow(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModuleItemText(text = pluralStringResource(R.plurals.moduleHeader_itemCount, state.itemCount, state.itemCount))
                if (state.pastDueCount > 0) {
                    ModuleItemText(
                        text = stringResource(R.string.moduleHeader_pastDue, state.pastDueCount),
                        color = HorizonColors.Text.error()
                    )
                }
                if (state.remainingMinutes != null) {
                    ModuleItemText(text = state.remainingMinutes)
                }
            }
        }
    }
}

@Composable
@Preview
private fun ModuleContainerExpandedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.IN_PROGRESS,
        2,
        true,
        "Long subtitle with multiple lines of text to show how it wraps and truncates"
    )
}

@Composable
@Preview
private fun ModuleContainerWithoutSubtitlePreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.IN_PROGRESS,
        2,
        true,
        null
    )
}

@Composable
@Preview
private fun ModuleContainerCollapsedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.IN_PROGRESS,
        2,
        false,
        "Long subtitle with multiple lines of text to show how it wraps and truncates"
    )
}

@Composable
@Preview
private fun ModuleContainerWithoutPastDuePreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.IN_PROGRESS,
        0,
        false,
        "Long subtitle with multiple lines of text to show how it wraps and truncates"
    )
}

@Composable
@Preview
private fun ModuleContainerOptionalPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.OPTIONAL,
        2,
        false,
        "Long subtitle with multiple lines of text to show how it wraps and truncates"
    )
}

@Composable
@Preview
private fun ModuleContainerNotStartedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.NOT_STARTED,
        2,
        false,
        "Long subtitle with multiple lines of text to show how it wraps and truncates"
    )
}

@Composable
@Preview
private fun ModuleContainerCompletedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.COMPLETED,
        2,
        false,
        "Long subtitle with multiple lines of text to show how it wraps and truncates"
    )
}

@Composable
@Preview
private fun ModuleContainerLockedPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainerPreview(
        ModuleStatus.LOCKED,
        2,
        false,
        "Long subtitle with multiple lines of text to show how it wraps and truncates"
    )
}

@Composable
fun ModuleContainerPreview(status: ModuleStatus, pastDueCount: Int, expanded: Boolean, subtitle: String?, onClick: () -> Unit = {}) {
    ContextKeeper.appContext = LocalContext.current
    ModuleContainer(
        state = ModuleHeaderState(
            title = "Module Title. Let's make this longer as well for a better example. Let this be 3 lines long to preview collapsed state truncation.",
            status = status,
            subtitle = subtitle,
            itemCount = 5,
            pastDueCount = pastDueCount,
            remainingMinutes = "5 mins",
            expanded = expanded,
            onClick = onClick
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(5) { index ->
                ModuleItemCard(
                    ModuleItemCardState(
                        title = "Module Item Title $index",
                        learningObjectType = LearningObjectType.ASSIGNMENT,
                        learningObjectStatus = LearningObjectStatus.REQUIRED
                    )
                )
            }
        }
    }
}