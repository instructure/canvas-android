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
package com.instructure.horizon.design.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.design.foundation.Colors

@Composable
fun ProgressBar(progress: Double, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier
                .border(width = 2.dp, shape = RoundedCornerShape(100.dp), color = Colors.Surface.institution())
                .height(28.dp)
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .background(color = Colors.Surface.institution(), shape = RoundedCornerShape(100.dp))
                    .fillMaxWidth((progress.toFloat() / 100f))
                    .height(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.progressBar_percent, progress.toInt()), color = Colors.Surface.institution())
    }
}

@Composable
@Preview
private fun ProgressBarPreview() {
    ContextKeeper.appContext = LocalContext.current
    ProgressBar(progress = 50.0)
}