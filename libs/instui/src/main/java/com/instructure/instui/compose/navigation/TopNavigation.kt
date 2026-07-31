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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.arrowLeft
import com.instructure.instui.token.icon.x
import com.instructure.instui.token.semantic.InstUISemanticColors

/**
 * The navigation variant that determines the leading icon and trailing icon cap.
 *
 * - [Page] — leading back arrow (`InstUIIcons.arrowLeft`), up to **2** trailing actions.
 * - [Modal] — leading close X (`InstUIIcons.x`), up to **1** trailing action.
 */
enum class TopNavigationType {
    Page,
    Modal,
}

/**
 * A single icon action shown in the trailing slot of [TopNavigation].
 *
 * @param icon The icon vector drawn from [InstUIIcons].
 * @param contentDescription Accessibility label for the icon; `null` if purely decorative.
 * @param onClick Invoked when the user taps the icon.
 */
data class TopNavAction(
    val icon: ImageVector,
    val contentDescription: String?,
    val onClick: () -> Unit,
)

/**
 * InstUI top navigation bar.
 *
 * A thin wrapper over [TopBar] that pins a theme-aware background/content color pair and wires
 * up the per-variant leading icon and trailing action cap:
 * - [TopNavigationType.Page]: back arrow + up to 2 trailing icons (extras are silently clamped).
 * - [TopNavigationType.Modal]: close X + up to 1 trailing icon (extras are silently clamped).
 *
 * Colors default to InstUI semantic tokens that flip together across themes:
 * - Container: `InstUISemanticColors.Background.brand()` (navy170 in light, navy10 in dark).
 * - Content:   `InstUISemanticColors.Text.inverse()` (white in light, grey180 in dark).
 *
 * Usage:
 * ```
 * TopNavigation(
 *     title = "Assignment",
 *     subtitle = "Biology 101",
 *     type = TopNavigationType.Page,
 *     onNavigationClick = { navController.navigateUp() },
 *     navigationContentDescription = stringResource(R.string.back),
 *     actions = listOf(
 *         TopNavAction(InstUIIcons.edit, "Edit") { },
 *         TopNavAction(InstUIIcons.moreVertical, "More") { },
 *     ),
 * )
 * ```
 *
 * @param title Primary title displayed in bold (`heading/titleCardMini`).
 * @param subtitle Optional secondary line below the title (`text/legend`). Typically the course name.
 * @param type Navigation variant — determines the leading icon and trailing action cap.
 * @param onNavigationClick Invoked when the user taps the leading back/close icon.
 * @param navigationContentDescription Accessibility label for the leading back/close icon, supplied
 *   by the caller so it can be localized.
 * @param actions Trailing icon actions. Capped at 2 for [TopNavigationType.Page] and 1 for
 *   [TopNavigationType.Modal]; any excess entries are silently dropped via `take(n)`.
 * @param modifier Optional modifier applied to the underlying [TopBar].
 * @param containerColor Background color of the bar. Defaults to `Background.brand()`.
 * @param contentColor Color of the title, subtitle, and icons. Defaults to `Text.inverse()`.
 */
@Composable
fun TopNavigation(
    title: String,
    onNavigationClick: () -> Unit,
    navigationContentDescription: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    type: TopNavigationType = TopNavigationType.Page,
    actions: List<TopNavAction> = emptyList(),
    containerColor: Color = InstUISemanticColors.Background.brand(),
    contentColor: Color = InstUISemanticColors.Text.inverse(),
) {
    // Enforce per-variant trailing icon cap: Page ≤ 2, Modal ≤ 1.
    val maxActions = if (type == TopNavigationType.Page) 2 else 1
    val clampedActions = actions.take(maxActions)

    val leadingIcon: ImageVector = when (type) {
        TopNavigationType.Page -> InstUIIcons.arrowLeft
        TopNavigationType.Modal -> InstUIIcons.x
    }

    TopBar(
        title = title,
        subtitle = subtitle,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
        navigationIcon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onNavigationClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = leadingIcon,
                    tint = contentColor,
                    contentDescription = navigationContentDescription,
                    size = IconSize.Large,
                )
            }
        },
        actions = {
            clampedActions.forEach { action ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = action.onClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = action.icon,
                        tint = contentColor,
                        contentDescription = action.contentDescription,
                        size = IconSize.Large,
                    )
                }
            }
        },
    )
}

// region Previews

@Preview(name = "TopNavigation Page — Light", showBackground = true)
@Preview(name = "TopNavigation Page — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopNavigationPagePreview() {
    InstUITheme {
        TopNavigation(
            title = "Assignment Details",
            subtitle = "Biology 101",
            type = TopNavigationType.Page,
            onNavigationClick = {},
            navigationContentDescription = "Navigate back",
            actions = listOf(
                TopNavAction(
                    icon = InstUIIcons.x,
                    contentDescription = "Action one",
                    onClick = {},
                ),
                TopNavAction(
                    icon = InstUIIcons.arrowLeft,
                    contentDescription = "Action two",
                    onClick = {},
                ),
            ),
        )
    }
}

@Preview(name = "TopNavigation Modal — Light", showBackground = true)
@Preview(name = "TopNavigation Modal — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopNavigationModalPreview() {
    InstUITheme {
        TopNavigation(
            title = "Submit Assignment",
            subtitle = "Biology 101",
            type = TopNavigationType.Modal,
            onNavigationClick = {},
            navigationContentDescription = "Close",
            actions = listOf(
                TopNavAction(
                    icon = InstUIIcons.arrowLeft,
                    contentDescription = "Submit",
                    onClick = {},
                ),
            ),
        )
    }
}

// endregion
