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
package com.instructure.horizon.features.moduleitemsequence.content

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillStyle
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper

@Composable
fun LockedContentScreen(lockExplanation: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Pill(
            label = stringResource(R.string.learningObject_locked),
            style = PillStyle.INLINE,
            type = PillType.LEARNING_OBJECT_TYPE,
            case = PillCase.TITLE,
            iconRes = R.drawable.lock
        )
        HorizonSpace(SpaceSize.SPACE_16)
        ComposeCanvasWebViewWrapper(html = lockExplanation)
    }
}