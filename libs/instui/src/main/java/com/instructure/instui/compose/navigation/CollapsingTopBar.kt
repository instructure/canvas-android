/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.instui.compose.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.line.ArrowOpenLeft
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors

private val LeadingSize = InstUILayoutSizes.Size.Interactive.height_lg
private val LeadingGap = InstUILayoutSizes.Spacing.SpaceMd.spaceMd
private val TitleEndPadding = InstUILayoutSizes.Spacing.SpaceLg.spaceLg

/**
 * InstUI collapsing top bar with optional leading content.
 *
 * Wraps Material3 [LargeTopAppBar] with InstUI tokens. When expanded, shows the
 * [leading] content (image, color swatch, avatar, etc.) next to the title. As
 * the user scrolls, the leading content shrinks and fades, the background
 * transitions from base to [accentColor], and text/icon colors transition from
 * base to onColor. Status bar icons toggle light/dark automatically.
 *
 * When the real design system component is ready, only this file's internals change.
 *
 * Usage:
 * ```
 * val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
 *
 * CollapsingTopBar(
 *     title = "Course Name",
 *     scrollBehavior = scrollBehavior,
 *     onNavigateBack = { navController.popBackStack() },
 *     leading = { CourseImage(imageUrl, courseColor) },
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    accentColor: Color = LocalCourseColor.current,
    expandedHeight: Dp = 112.dp,
    onNavigateBack: (() -> Unit)? = null,
) {
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    val expandedFraction = 1f - collapsedFraction
    val contentColor = lerp(
        InstUISemanticColors.Text.base(),
        InstUISemanticColors.Text.onColor(),
        collapsedFraction,
    )

    LargeTopAppBar(
        modifier = modifier,
        expandedHeight = expandedHeight,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leading != null) {
                    Box(
                        modifier = Modifier
                            .width(LeadingSize * expandedFraction)
                            .clipToBounds()
                            .alpha(expandedFraction),
                    ) {
                        // Content stays at full size; outer Box clips as it shrinks
                        Box(modifier = Modifier.requiredWidth(LeadingSize)) {
                            leading()
                        }
                    }
                    Spacer(modifier = Modifier.width(LeadingGap * expandedFraction))
                }
                Text(
                    text = title,
                    style = InstUIHeading.titleCardMini,
                    maxLines = if (collapsedFraction > 0.5f) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = TitleEndPadding),
                )
            }
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = InstUIIcons.Line.ArrowOpenLeft,
                        tint = contentColor,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = InstUISemanticColors.Background.base(),
            scrolledContainerColor = accentColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "CollapsingTopBar — Light", showBackground = true)
@Preview(name = "CollapsingTopBar — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CollapsingTopBarPreview() {
    InstUITheme(courseColor = Color(0xFFBF5811)) {
        CollapsingTopBar(
            title = "Introduction to Space Stations",
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            onNavigateBack = {},
        )
    }
}