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

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertEquals

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

/**
 * Asserts the color of a single pixel of the node with the given test tag.
 *
 * Colors are not part of the semantics tree, so the node's surface is captured into a bitmap and one
 * of its pixels is sampled. The node has to be on screen, and the sampled point has to fall on a
 * flat area of it, otherwise the color of whatever is drawn there (text, an icon, an anti-aliased
 * rounded edge) is measured instead of the node's own color.
 *
 * @param tag The test tag of the node to sample
 * @param expectedColor The expected color, as an ARGB int
 * @param relativeX The horizontal position to sample, as a fraction of the node's width
 * @param relativeY The vertical position to sample, as a fraction of the node's height
 */
@OptIn(ExperimentalTestApi::class)
fun ComposeTestRule.assertNodeColor(
    tag: String,
    expectedColor: Int,
    relativeX: Float = 0.5f,
    relativeY: Float = 0.5f
) {
    waitForIdle()
    val pixels = onNodeWithTag(tag).captureToImage().toPixelMap()
    val x = ((pixels.width - 1) * relativeX).toInt()
    val y = ((pixels.height - 1) * relativeY).toInt()
    val actualColor = pixels[x, y].toArgb()

    assertEquals(
        "The node with the '$tag' test tag has the wrong color at ($x, $y). " +
            "Expected: #${Integer.toHexString(expectedColor)}, actual: #${Integer.toHexString(actualColor)}.",
        expectedColor,
        actualColor
    )
}

/**
 * Brings the node with the given test tag into view by swiping up on the screen.
 *
 * Preferred over `performScrollTo()` for nodes at the very bottom of a long scrollable screen:
 * that action retries in an unbounded `do/while` loop until the node fits its scroll container's
 * viewport, animating a scroll on every attempt, so when the node cannot be aligned the test hangs
 * (slowly scrolling forever) instead of failing.
 *
 * @param tag The test tag of the node to bring into view
 * @param maxSwipes The maximum number of swipes to perform before giving up
 * @param useUnmergedTree Whether to query the unmerged semantics tree
 */
fun ComposeTestRule.swipeUpUntilTagDisplayed(
    tag: String,
    maxSwipes: Int = 10,
    useUnmergedTree: Boolean = false
) {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val x = device.displayWidth / 2
    val startY = (device.displayHeight * 0.7).toInt()
    val endY = (device.displayHeight * 0.35).toInt()

    repeat(maxSwipes) {
        if (isTagDisplayed(tag, useUnmergedTree)) return
        device.swipe(x, startY, x, endY, 20)
        waitForIdle()
    }
}

/**
 * Asserts the top-to-bottom order of nodes whose test tag is [tagPrefix] followed by an item id.
 *
 * Only the relative order of [expectedItemIds] is checked, so a partial list can be passed even when
 * more tagged nodes are on screen. Ids that are not present in the semantics tree are ignored, which
 * keeps the assertion usable inside lazy lists where off-screen items are not composed, so absence of
 * an item has to be asserted separately.
 *
 * @param tagPrefix The common test tag prefix, e.g. "dashboardWidget_"
 * @param expectedItemIds The item ids (the part after [tagPrefix]) in the expected top-to-bottom order
 * @param useUnmergedTree Whether to query the unmerged semantics tree
 */
fun ComposeTestRule.assertTagOrder(
    tagPrefix: String,
    expectedItemIds: List<String>,
    useUnmergedTree: Boolean = false
) {
    waitForIdle()
    val actualItemIds = onAllNodes(hasTestTagThatStartsWith(tagPrefix), useUnmergedTree)
        .fetchSemanticsNodes()
        .sortedBy { it.positionInRoot.y }
        .mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag)?.removePrefix(tagPrefix) }

    val actualOrder = actualItemIds.filter { it in expectedItemIds }
    val expectedOrder = expectedItemIds.filter { it in actualItemIds }

    assertEquals(
        "Nodes with the '$tagPrefix' test tag prefix are not in the expected order.",
        expectedOrder,
        actualOrder
    )
}
