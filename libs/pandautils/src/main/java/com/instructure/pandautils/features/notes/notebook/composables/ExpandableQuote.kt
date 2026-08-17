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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.instructure.instui.compose.input.Link
import com.instructure.instui.compose.input.LinkIconPosition
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R

/**
 * A quote excerpt that collapses to [collapsedMaxLines] with a "Show more/less" toggle when it
 * overflows. Renders in plain base text color, matching the detail level's Card in Figma exactly
 * (no note-type accent color/underline there - that treatment is list-row-only, see
 * NotesListContent's HighlightedExcerptText).
 */
@Composable
fun ExpandableQuote(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 1
) {
    var isExpanded by rememberSaveable(text) { mutableStateOf(false) }
    var hasOverflow by remember(text) { mutableStateOf(false) }
    // No verticalArrangement spacing: the toggle Link's own touch padding already supplies the gap to
    // the quote above it (see the comment on it below). Centered children so the toggle sits centered
    // under the quote per Figma - the quote itself takes fillMaxWidth so it stays start-aligned no
    // matter how short it is.
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // animateContentSize() belongs on the quote, NOT on the Column: it is
        // `clipToBounds() then SizeAnimationModifier`, i.e. it lays its child out at the final size
        // from the first frame and only animates the size it *reports* upward, clipping to it. On the
        // Column that meant the toggle was placed at its expanded position immediately and stayed
        // clipped out of view until the animation caught up - it flickered from one place to the other
        // instead of travelling. Here, the Column re-measures against the animated text height every
        // frame, so the quote reveals line by line and the toggle slides along with it. (InstUI's own
        // ToggleDetails can keep it on the Column only because its toggle is the *first* child, so
        // nothing above the animating content ever has to move.)
        Text(
            text = text,
            style = InstUIText.contentQuote,
            color = InstUISemanticColors.Text.base(),
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!isExpanded) hasOverflow = it.hasVisualOverflow },
            modifier = Modifier.fillMaxWidth().animateContentSize()
        )
        if (hasOverflow || isExpanded) {
            val chevronRotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "quoteExpandToggleChevron")
            // InstUI's Link is exactly what Figma specifies here, and being button-backed it brings a
            // rounded *and* padded ripple, a full touch target and button semantics - none of which the
            // hand-rolled clickable Row this replaced had. Its own vertical touch padding is also what
            // supplies the gap to the quote above, hence no verticalArrangement on the Column.
            // A single chevronDown rotated 180deg (rather than swapping to chevronUp) so the direction
            // change is a smooth animated rotation instead of an instant asset swap.
            Link(
                text = stringResource(if (isExpanded) R.string.notebookShowLess else R.string.notebookShowAll),
                onClick = { isExpanded = !isExpanded },
                icon = InstUIIcons.chevronDown,
                iconPosition = LinkIconPosition.Trailing,
                iconModifier = Modifier.rotate(chevronRotation),
                modifier = Modifier.testTag("quoteExpandToggle")
            )
        }
    }
}
