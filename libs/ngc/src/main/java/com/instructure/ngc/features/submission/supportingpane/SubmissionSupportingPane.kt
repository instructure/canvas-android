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

package com.instructure.ngc.features.submission.supportingpane

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.navigation.SegmentedControl
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneSegment.COMMENTS
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneSegment.INSTRUCTIONS
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneSegment.RUBRIC

@Composable
fun SubmissionSupportingPane(
    uiState: SubmissionSupportingPaneUiState,
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    toggleContentDescription: String? = null,
    onPeekHeightChanged: ((Int) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InstUISemanticColors.Background.base()),
    ) {
        // The header + tabs make up the collapsed peek region; report their measured height so the
        // bottom sheet's peek height can reveal the tabs (not just the title) when collapsed.
        Column(
            modifier = if (onPeekHeightChanged != null) {
                Modifier.onSizeChanged { onPeekHeightChanged(it.height) }
            } else {
                Modifier
            },
        ) {
            SubmissionSupportingPaneHeader(
                expanded = expanded,
                onToggleExpand = onToggleExpand,
                toggleContentDescription = toggleContentDescription,
            )

            SegmentedControl(
                tabs = SubmissionSupportingPaneSegment.entries.map { stringResource(it.labelRes) },
                selectedIndex = selectedIndex,
                onTabSelected = onSegmentSelected,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (SubmissionSupportingPaneSegment.entries[selectedIndex]) {
                INSTRUCTIONS -> SubmissionInstructionsScreen(
                    loading = uiState.loading,
                    html = uiState.instructionsHtml,
                    modifier = Modifier.fillMaxSize(),
                )
                RUBRIC -> SubmissionRubricScreen(modifier = Modifier.fillMaxSize())
                COMMENTS -> SubmissionCommentsScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun SubmissionSupportingPaneHeader(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    toggleContentDescription: String? = null,
) {
    // Rotate a single chevron rather than swapping icons, so the expand/collapse reads as an
    // animated accordion toggle.
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "supportingPaneChevronRotation",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading spacer mirrors the trailing tap target so the weighted title stays centered and
        // can never render under the chevron.
        Spacer(modifier = Modifier.minimumInteractiveComponentSize())
        Heading(
            text = stringResource(R.string.ngc_submissionSupportingPaneTitle),
            level = HeadingLevel.H4,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        if (onToggleExpand != null) {
            Icon(
                imageVector = InstUIIcons.chevronDown,
                contentDescription = toggleContentDescription,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(role = Role.Button, onClick = onToggleExpand)
                    .rotate(chevronRotation),
            )
        } else {
            Spacer(modifier = Modifier.minimumInteractiveComponentSize())
        }
    }
}
