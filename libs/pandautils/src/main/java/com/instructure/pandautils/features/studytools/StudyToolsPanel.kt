/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 *
 */
package com.instructure.pandautils.features.studytools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.indicator.Icon as InstUIIcon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.arrowLeft
import com.instructure.instui.token.icon.circleHelp
import com.instructure.instui.token.icon.sparkles
import com.instructure.instui.token.icon.x
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.features.studytools.composables.StudyToolContent
import com.instructure.pandautils.features.studytools.composables.StudyToolsEmptyState
import com.instructure.pandautils.features.studytools.composables.StudyToolsMenu
import com.instructure.pandautils.features.studytools.composables.labelRes
import com.instructure.pandautils.features.studytools.nutritionfacts.NutritionFactsSheet

/**
 * The Study Tools panel.
 *
 * Phones get a two-stage bottom sheet — it opens partially expanded and drags to full page.
 * Tablets get a trailing-edge drawer instead, since a bottom sheet wastes the horizontal space a
 * tablet has and covers the content the tools are about.
 */
@Composable
fun StudyToolsPanel(
    uiState: StudyToolsUiState,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    InstUITheme {
        if (isTablet) {
            StudyToolsDrawer(uiState = uiState, modifier = modifier)
        } else {
            StudyToolsBottomSheet(uiState = uiState, modifier = modifier)
        }

        // A sibling of the panel rather than a child, so it is not clipped by the sheet or drawer.
        if (uiState.aiDisclosureVisible) {
            NutritionFactsSheet(onDismiss = uiState.actions.onAiDisclosureDismiss)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyToolsBottomSheet(
    uiState: StudyToolsUiState,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // The half-height stage exists so the menu does not bury the page it is about. A tool's content is
    // full-page instead: the sheet is measured at full height, so at the partial stage its lower half —
    // which is where the regenerate link, the feedback icons and any retry button live — sits below the
    // screen with nothing to hint at it.
    val toolOpen = uiState.selectedTool != null
    LaunchedEffect(toolOpen) {
        if (toolOpen) sheetState.expand()
    }

    Tray(
        onDismiss = uiState.actions.onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("studyToolsSheet")
    ) {
        StudyToolsPanelContent(
            uiState = uiState,
            // Filling the height is what gives the sheet a full-page stage to expand into; without
            // it the sheet wraps its content and there is nothing to drag to.
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
private fun StudyToolsDrawer(
    uiState: StudyToolsUiState,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = uiState.actions.onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM_ALPHA))
                // The dialog content fills the whole window, so there is no "outside" region left for
                // Dialog's own touch-outside dismissal to detect - this tap on the scrim replaces it.
                // A raw pointerInput is used instead of clickable() because clickable() adds a
                // semantics node that interferes with the drawer content's own semantics tree.
                .pointerInput(uiState.actions.onDismiss) {
                    detectTapGestures(onTap = { uiState.actions.onDismiss() })
                }
                .testTag("studyToolsDrawerScrim")
        ) {
            Surface(
                color = InstUISemanticColors.Background.container(),
                elevation = 16.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(DRAWER_WIDTH)
                    // Consumes taps so they don't fall through to the scrim's dismiss handler above.
                    .pointerInput(Unit) { detectTapGestures(onTap = {}) }
                    .testTag("studyToolsDrawer")
            ) {
                StudyToolsPanelContent(
                    uiState = uiState,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun StudyToolsPanelContent(
    uiState: StudyToolsUiState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        StudyToolsHeader(uiState = uiState)

        // A selected tool owns the whole area below the header; the menu and its states are what the
        // panel falls back to.
        if (uiState.selectedTool != null) {
            StudyToolContent(
                state = uiState.toolState,
                actions = uiState.actions,
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                when (val state = uiState.screenState) {
                    is StudyToolsScreenState.Loading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = InstUILayoutSizes.Spacing.SpaceXl.spaceXl)
                            .testTag("studyToolsLoading"),
                        contentAlignment = Alignment.Center
                    ) {
                        Spinner()
                    }

                    is StudyToolsScreenState.Content -> StudyToolsMenu(
                        tools = state.tools,
                        onToolSelected = uiState.actions.onToolSelected,
                        modifier = Modifier.testTag("studyToolsMenu")
                    )

                    is StudyToolsScreenState.NoToolsAvailable -> StudyToolsEmptyState(
                        message = stringResource(id = R.string.studyTools_noToolsAvailable),
                        modifier = Modifier.testTag("studyToolsEmpty")
                    )

                    is StudyToolsScreenState.Offline -> StudyToolsEmptyState(
                        message = stringResource(id = R.string.studyTools_offline),
                        modifier = Modifier.testTag("studyToolsOffline")
                    )

                    is StudyToolsScreenState.Error -> StudyToolsEmptyState(
                        title = state.header,
                        message = state.message ?: stringResource(id = R.string.studyTools_genericError),
                        buttonText = if (state.retryable) stringResource(id = R.string.retry) else null,
                        onButtonClick = if (state.retryable) uiState.actions.onRetry else null,
                        modifier = Modifier.testTag("studyToolsError")
                    )
                }
            }
        }
    }
}

/**
 * Sparkle title plus the help / close icon pair. Once a tool is open the title becomes that tool's
 * name and a Back affordance replaces the sparkle, so the header always describes what is on screen.
 */
@Composable
private fun StudyToolsHeader(uiState: StudyToolsUiState) {
    val selected = uiState.selectedTool
    val title = selected?.let { stringResource(id = it.tool.labelRes()) }
        ?: stringResource(id = R.string.studyTools_title)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                end = InstUILayoutSizes.Spacing.SpaceXs.spaceXs,
                bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm
            )
    ) {
        if (selected != null) {
            HeaderIconButton(
                onClick = uiState.actions.onBack,
                contentDescription = stringResource(id = R.string.back),
                imageVector = InstUIIcons.arrowLeft,
                testTag = "studyToolsBack"
            )
        } else {
            InstUIIcon(
                imageVector = InstUIIcons.sparkles,
                contentDescription = null,
                size = IconSize.Medium,
                color = IconColor.Base
            )
        }

        Heading(
            text = title,
            level = HeadingLevel.H3,
            modifier = Modifier
                .weight(1f)
                .padding(start = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
                .semantics { heading() }
                .testTag("studyToolsTitle")
        )

        HeaderIconButton(
            onClick = uiState.actions.onAiDisclosureClick,
            contentDescription = stringResource(id = R.string.studyTools_aiDisclosureContentDescription),
            imageVector = InstUIIcons.circleHelp,
            testTag = "studyToolsAiDisclosure"
        )

        HeaderIconButton(
            onClick = uiState.actions.onDismiss,
            contentDescription = stringResource(id = R.string.close),
            imageVector = InstUIIcons.x,
            testTag = "studyToolsClose"
        )
    }
}

/**
 * InstUI's `IconButton` only accepts a `@DrawableRes` icon, so it cannot host a vector from
 * [InstUIIcons]. Built from [InstUIIcon] plus a clickable modifier instead, matching the pattern
 * [com.instructure.pandautils.features.studytools.composables.StudyToolsFeedbackRow]'s `FeedbackIcon`
 * already uses.
 */
@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    imageVector: ImageVector,
    testTag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(
                minWidth = InstUILayoutSizes.Size.Interactive.height_lg,
                minHeight = InstUILayoutSizes.Size.Interactive.height_lg
            )
            // Clipped to a circle so the ripple matches the round icon instead of the square box.
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .testTag(testTag)
    ) {
        InstUIIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            size = IconSize.Medium,
            color = IconColor.Base
        )
    }
}

private const val SCRIM_ALPHA = 0.5f
private val DRAWER_WIDTH = 420.dp
