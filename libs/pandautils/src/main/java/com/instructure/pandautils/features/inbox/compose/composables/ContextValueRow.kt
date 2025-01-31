/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color

@Composable
fun ContextValueRow(
    label: String,
    value: CanvasContext?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(52.dp)
            .clickable(enabled = enabled) { onClick() }
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .padding(top = 8.dp, bottom = 8.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .testTag("contextValueRow")
    ) {
        Text(
            text = label,
            color = colorResource(id = R.color.textDarkest),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        Spacer(Modifier.width(12.dp))

        if (value != null) {
            val color = if (value.type == CanvasContext.Type.USER) ThemePrefs.brandColor else value.color

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(color), CircleShape)
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = value.name ?: "",
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = stringResource(R.string.a11y_openCoursePicker),
            tint = colorResource(id = R.color.textDark),
            modifier = Modifier
                .size(16.dp)
        )
    }
}

@Composable
@Preview
fun ContextValueRowPreview() {
    ContextKeeper.appContext = LocalContext.current
    ContextValueRow(
        label = "Course",
        value = Course(
            id = 1,
            name = "Course 1",
            courseColor = "#FF0000"
        ),
        enabled = true,
        onClick = {}
    )
}