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
package com.instructure.horizon.horizonui

import android.content.Context
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R

fun SemanticsPropertyReceiver.expandable(context: Context, expanded: Boolean) {
    val expandedStateDesc = context.getString(R.string.a11y_expanded)
    val collapsedStateDesc = context.getString(R.string.a11y_collapsed)
    val expandActionLabel = context.getString(R.string.a11y_expand)
    val collapseActionLabel = context.getString(R.string.a11y_collapse)

    stateDescription = if (expanded) expandedStateDesc else collapsedStateDesc
    liveRegion = LiveRegionMode.Assertive
    onClick(if (expanded) collapseActionLabel else expandActionLabel) { false }
}

fun SemanticsPropertyReceiver.selectable(context: Context, selected: Boolean) {
    val selectedStateDesc = context.getString(R.string.a11y_selected)
    val unselectedStateDesc = context.getString(R.string.a11y_unselected)

    stateDescription = if (selected) selectedStateDesc else unselectedStateDesc
    liveRegion = LiveRegionMode.Assertive
}

val BoxWithConstraintsScope.isWideLayout
    get() = this.maxWidth >= 400.dp