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

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
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
    onNode(
        hasParent(hasTestTag(toolbarTag)).and(
            hasContentDescription(contentDescription)
        )
    ).performClick()
}
