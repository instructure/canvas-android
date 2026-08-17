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
 */
package com.instructure.pandautils.features.notes.notebook.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronLeft
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R

/**
 * The shared white/black header for both Notebook tray levels (Figma: list `25808-31909`, detail
 * `25808-31607`). Deliberately not course-colored - see the Notebook Tray plan §3a. Paints no
 * background of its own so the Tray's own container color always shows through underneath it and
 * the two can never drift out of sync.
 *
 * The title+subtitle slot is always truly centered in the row, never just centered in whatever space
 * happens to be left: [CenteredHeaderRow] measures the leading back control and trailing Done button
 * first, reserves a gutter on *both* sides equal to the wider of the two, then gives the title
 * whatever's left in the exactly-symmetric middle. That means neither side is ever truncated to force
 * equal thirds (a long [backLabel] like "Page notes" just gets a wider gutter, mirrored on the Done
 * side), while the title never drifts off-center the way it would in a plain `Row` with one weighted
 * slot. InstUI's own [com.instructure.instui.compose.navigation.TopBar] can't center a title at all
 * (its title starts after the navigation slot, with no centered variant), which is why this hand-rolled
 * layout exists instead of reusing it.
 *
 * [onBackClick] null renders the back control inert (see open question 1 in the plan: the "Course"
 * back affordance on the list level has no destination yet) - it stays visually clickable per Figma
 * but is excluded from the accessibility tree so it is never announced as an actionable control.
 */
@Composable
fun NotebookTrayHeader(
    title: String,
    backLabel: String,
    onBackClick: (() -> Unit)?,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    backContentDescription: String? = null,
    backTestTag: String = "notebookTrayBack"
) {
    CenteredHeaderRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm
            ),
        leading = {
            NotebookTrayBackControl(
                label = backLabel,
                contentDescription = backContentDescription ?: backLabel,
                onBackClick = onBackClick,
                testTag = backTestTag
            )
        },
        title = { NotebookTrayHeaderTitle(title = title, subtitle = subtitle) },
        trailing = {
            TextButton(
                text = stringResource(R.string.done),
                onClick = onDoneClick,
                contentColor = InstUISemanticColors.Text.base(),
                modifier = Modifier.testTag("notebookDoneButton")
            )
        }
    )
}

/**
 * A single-row layout with a leading slot, a trailing slot, and a title slot that is always centered
 * in the *full* row width - not just in whatever space is left after the other two. Achieved by
 * measuring [leading] and [trailing] at their own natural width first, then reserving that same width
 * (the wider of the two) as a gutter on both sides, so the middle region - and therefore [title] - is
 * always symmetric regardless of how the two side widths compare. See [NotebookTrayHeader]'s doc for
 * why a plain weighted `Row` can't do this.
 */
@Composable
private fun CenteredHeaderRow(
    leading: @Composable () -> Unit,
    title: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val leadingPlaceables = subcompose("leading", leading).map { it.measure(looseConstraints) }
        val trailingPlaceables = subcompose("trailing", trailing).map { it.measure(looseConstraints) }
        val leadingWidth = leadingPlaceables.maxOfOrNull { it.width } ?: 0
        val trailingWidth = trailingPlaceables.maxOfOrNull { it.width } ?: 0
        val gutterWidth = maxOf(leadingWidth, trailingWidth)

        val titleMaxWidth = (constraints.maxWidth - 2 * gutterWidth).coerceAtLeast(0)
        val titlePlaceables = subcompose("title", title)
            .map { it.measure(looseConstraints.copy(maxWidth = titleMaxWidth)) }

        val rowHeight = maxOf(
            leadingPlaceables.maxOfOrNull { it.height } ?: 0,
            trailingPlaceables.maxOfOrNull { it.height } ?: 0,
            titlePlaceables.maxOfOrNull { it.height } ?: 0
        )

        layout(constraints.maxWidth, rowHeight) {
            // Leading/trailing stay flush to their own edge - only the reserved gutter width (used to
            // size the middle region below) is symmetric, not the placement of the shorter side's
            // content within it.
            leadingPlaceables.forEach { it.place(x = 0, y = (rowHeight - it.height) / 2) }
            trailingPlaceables.forEach { it.place(x = constraints.maxWidth - it.width, y = (rowHeight - it.height) / 2) }
            titlePlaceables.forEach {
                val x = gutterWidth + (titleMaxWidth - it.width) / 2
                it.place(x = x, y = (rowHeight - it.height) / 2)
            }
        }
    }
}

@Composable
private fun NotebookTrayHeaderTitle(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = InstUIHeading.titleCardMini,
            color = InstUISemanticColors.Text.base(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        subtitle?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = InstUIText.legend,
                color = InstUIText.mutedColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotebookTrayBackControl(
    label: String,
    contentDescription: String,
    onBackClick: (() -> Unit)?,
    testTag: String,
    modifier: Modifier = Modifier
) {
    // The same InstUI TextButton component the "Done" button above uses, so the back control's ripple
    // shape and touch-target padding match Done's by construction rather than by hand.
    val semanticsModifier = if (onBackClick != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        // TODO(MBL-xxxxx): route to the Course-level notebook once it exists. Still clickable (matches
        // Figma's affordance) but clearAndSetSemantics() keeps it out of the accessibility tree so it
        // is never announced as an actionable control until it actually does something.
        Modifier.clearAndSetSemantics {}
    }
    TextButton(
        text = label,
        onClick = onBackClick ?: {},
        contentColor = InstUISemanticColors.Text.base(),
        icon = InstUIIcons.chevronLeft,
        iconSize = IconSize.Medium,
        maxLines = 1,
        modifier = modifier.then(semanticsModifier).testTag(testTag)
    )
}
