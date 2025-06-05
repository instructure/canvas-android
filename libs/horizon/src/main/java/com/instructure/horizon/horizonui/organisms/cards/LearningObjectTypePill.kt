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
package com.instructure.horizon.horizonui.organisms.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.model.LearningObjectType

@Composable
fun LearningObjectTypePill(learningObjectType: LearningObjectType, modifier: Modifier = Modifier) {
    Pill(
        label = stringResource(learningObjectType.stringRes),
        style = PillStyle.INLINE,
        type = PillType.LEARNING_OBJECT_TYPE,
        case = PillCase.TITLE,
        iconRes = learningObjectType.iconRes,
        modifier = modifier
    )
}

@Composable
@Preview(showBackground = true)
fun LearningObjectTypePillAssignmentPreview() {
    ContextKeeper.appContext = LocalContext.current
    LearningObjectTypePill(learningObjectType = LearningObjectType.ASSIGNMENT)
}

@Composable
@Preview(showBackground = true)
fun LearningObjectTypePillPagePreview() {
    ContextKeeper.appContext = LocalContext.current
    LearningObjectTypePill(learningObjectType = LearningObjectType.PAGE)
}

@Composable
@Preview(showBackground = true)
fun LearningObjectTypePillFilePreview() {
    ContextKeeper.appContext = LocalContext.current
    LearningObjectTypePill(learningObjectType = LearningObjectType.FILE)
}

@Composable
@Preview(showBackground = true)
fun LearningObjectTypePillExternalToolPreview() {
    ContextKeeper.appContext = LocalContext.current
    LearningObjectTypePill(learningObjectType = LearningObjectType.EXTERNAL_TOOL)
}

@Composable
@Preview(showBackground = true)
fun LearningObjectTypePillExternalUrlPreview() {
    ContextKeeper.appContext = LocalContext.current
    LearningObjectTypePill(learningObjectType = LearningObjectType.EXTERNAL_URL)
}

@Composable
@Preview(showBackground = true)
fun LearningObjectTypePillAssessmentPreview() {
    ContextKeeper.appContext = LocalContext.current
    LearningObjectTypePill(learningObjectType = LearningObjectType.ASSESSMENT)
}