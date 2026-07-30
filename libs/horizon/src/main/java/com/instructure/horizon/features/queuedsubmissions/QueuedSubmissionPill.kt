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
package com.instructure.horizon.features.queuedsubmissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillSize
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.pandautils.room.studentdb.entities.SubmissionState

/**
 * Inline "Queued — will submit when online" indicator that transitions to "Submitting…" while the
 * worker is running and disappears once the submission completes (the queued row is deleted on
 * success). Shown on the assignment detail screen and the module item card.
 */
@Composable
fun QueuedSubmissionPill(
    state: SubmissionState?,
    modifier: Modifier = Modifier
) {
    val (labelRes, type) = when (state) {
        SubmissionState.QUEUED -> R.string.queuedSubmission_pillQueued to PillType.DEFAULT
        SubmissionState.UPLOADING_FILES,
        SubmissionState.SUBMITTING,
        SubmissionState.VERIFYING,
        SubmissionState.RETRYING -> R.string.queuedSubmission_pillSubmitting to PillType.INSTITUTION
        SubmissionState.FAILED -> R.string.queuedSubmission_pillFailed to PillType.DANGER
        else -> return
    }
    Pill(
        label = stringResource(labelRes),
        style = PillStyle.OUTLINE,
        type = type,
        case = PillCase.TITLE,
        size = PillSize.SMALL,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun QueuedSubmissionPillQueuedPreview() {
    ContextKeeper.appContext = LocalContext.current
    QueuedSubmissionPill(state = SubmissionState.QUEUED)
}

@Preview(showBackground = true)
@Composable
private fun QueuedSubmissionPillSubmittingPreview() {
    ContextKeeper.appContext = LocalContext.current
    QueuedSubmissionPill(state = SubmissionState.SUBMITTING)
}

@Preview(showBackground = true)
@Composable
private fun QueuedSubmissionPillFailedPreview() {
    ContextKeeper.appContext = LocalContext.current
    QueuedSubmissionPill(state = SubmissionState.FAILED)
}
