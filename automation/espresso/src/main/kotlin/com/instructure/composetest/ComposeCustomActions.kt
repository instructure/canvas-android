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
package com.instructure.composetest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick

/**
 * Clicks an IconButton within a TopAppBar by content description.
 *
 * @param contentDescription The content description of the IconButton to click (e.g., "Back", "More options")
 * @param toolbarTag The test tag of the TopAppBar, defaults to "toolbar"
 */
fun ComposeTestRule.clickToolbarIconButton(
    contentDescription: String,
    toolbarTag: String = "toolbar"
) {
    waitForIdle()
    // When Compose screens are stacked (e.g. a sub-screen pushed over another Compose screen), the
    // underlying screen's toolbar stays in the semantics tree, so more than one node can match.
    // Target the topmost (last-attached) screen's toolbar button.
    onAllNodes(
        hasParent(hasTestTag(toolbarTag)).and(
            hasContentDescription(contentDescription)
        )
    ).onLast().performClick()
}

/**
 * Waits until at least one node with the given test tag is present in the semantics tree.
 *
 * @param tag The test tag to wait for
 * @param timeoutMillis The maximum time to wait, defaults to 20000ms
 * @param useUnmergedTree Whether to query the unmerged semantics tree, needed when the tagged node is a
 * descendant of a node that merges its descendants (e.g. a clickable card)
 */
fun ComposeTestRule.waitForTag(tag: String, timeoutMillis: Long = 20000, useUnmergedTree: Boolean = false) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodesWithTag(tag, useUnmergedTree = useUnmergedTree).fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * Returns whether a node with the given test tag is currently displayed.
 *
 * @param tag The test tag to check
 * @param useUnmergedTree Whether to query the unmerged semantics tree, needed when the tagged node is a
 * descendant of a node that merges its descendants (e.g. a clickable card)
 */
fun ComposeTestRule.isTagDisplayed(tag: String, useUnmergedTree: Boolean = false): Boolean {
    return try {
        onNodeWithTag(tag, useUnmergedTree = useUnmergedTree).assertIsDisplayed()
        true
    } catch (_: AssertionError) {
        false
    }
}
