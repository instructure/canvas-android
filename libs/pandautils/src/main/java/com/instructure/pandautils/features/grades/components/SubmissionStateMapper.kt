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

package com.instructure.pandautils.features.grades.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.instructure.instui.compose.indicator.PillVariant
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.circleCheck
import com.instructure.instui.token.icon.circleDashed
import com.instructure.instui.token.icon.circleSlash
import com.instructure.instui.token.icon.clock
import com.instructure.instui.token.icon.triangleAlert
import com.instructure.pandautils.compose.composables.SubmissionStateLabel

data class PillData(val text: String, val variant: PillVariant, val icon: ImageVector? = null)

@Composable
fun mapSubmissionStateToPill(label: SubmissionStateLabel): PillData? {
    if (label == SubmissionStateLabel.None) return null

    val text = when (label) {
        is SubmissionStateLabel.Predefined -> stringResource(id = label.labelRes)
        is SubmissionStateLabel.Custom -> label.label
    }

    val variant = when (label) {
        SubmissionStateLabel.Missing -> PillVariant.Error
        SubmissionStateLabel.Late -> PillVariant.Warning
        SubmissionStateLabel.Submitted -> PillVariant.Success
        SubmissionStateLabel.Graded -> PillVariant.Success
        SubmissionStateLabel.Excused -> PillVariant.Warning
        SubmissionStateLabel.NotSubmitted -> PillVariant.Default
        else -> PillVariant.Default
    }

    val icon = when (label) {
        SubmissionStateLabel.Missing -> InstUIIcons.triangleAlert
        SubmissionStateLabel.Late -> InstUIIcons.clock
        SubmissionStateLabel.Submitted -> InstUIIcons.circleCheck
        SubmissionStateLabel.Graded -> InstUIIcons.circleCheck
        SubmissionStateLabel.Excused -> InstUIIcons.circleSlash
        SubmissionStateLabel.NotSubmitted -> InstUIIcons.circleDashed
        else -> null
    }

    return PillData(text, variant, icon)
}