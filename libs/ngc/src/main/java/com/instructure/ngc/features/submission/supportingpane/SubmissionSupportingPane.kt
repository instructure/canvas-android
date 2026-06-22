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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.instructure.instui.compose.navigation.SegmentedControl
import com.instructure.instui.compose.text.Text
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneSegment.COMMENTS
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneSegment.INSTRUCTIONS
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPaneSegment.RUBRIC

@Composable
fun SubmissionSupportingPane(
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    headerTrailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = stringResource(R.string.ngc_submissionSupportingPaneTitle))
            if (headerTrailing != null) {
                headerTrailing()
            }
        }

        SegmentedControl(
            tabs = SubmissionSupportingPaneSegment.entries.map { stringResource(it.labelRes) },
            selectedIndex = selectedIndex,
            onTabSelected = onSegmentSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(modifier = Modifier.weight(1f)) {
            when (SubmissionSupportingPaneSegment.entries[selectedIndex]) {
                INSTRUCTIONS -> SubmissionInstructionsScreen(modifier = Modifier.fillMaxSize())
                RUBRIC -> SubmissionRubricScreen(modifier = Modifier.fillMaxSize())
                COMMENTS -> SubmissionCommentsScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
