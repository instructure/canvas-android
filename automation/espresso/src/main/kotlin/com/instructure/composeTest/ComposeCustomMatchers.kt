/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.composeTest

import androidx.annotation.DrawableRes
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import com.instructure.pandautils.utils.DrawableId

//This file is the collection of our custom compose matchers

fun hasSiblingWithText(text: String): SemanticsMatcher = hasAnySibling(hasText(text))

fun hasDrawable(@DrawableRes id: Int): SemanticsMatcher = SemanticsMatcher.expectValue(DrawableId, id)

fun hasTestTagThatContains(substring: String): SemanticsMatcher =
    SemanticsMatcher("Has testTag containing \"$substring\"") { node ->
        val tag = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.TestTag)
        tag?.contains(substring) == true
    }
