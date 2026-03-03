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
package com.instructure.pandautils.compose.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate

@Composable
fun TodayButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonColor: Color = colorResource(R.color.backgroundInfo),
    textColor: Color = colorResource(R.color.textLightest),
    contentDescription: String = stringResource(R.string.a11y_todoWidget_jumpToToday)
) {

    Box(
        modifier = modifier
            .height(24.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(100.dp),
                clip = false
            )
            .background(
                color = buttonColor,
                shape = RoundedCornerShape(100.dp)
            )
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
            }
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = textColor,
                lineHeight = 14.sp
            )

            Box(
                modifier = Modifier.size(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_calendar_day),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = LocalDate.now().dayOfMonth.toString(),
                    fontSize = 7.sp,
                    modifier = Modifier.padding(top = 2.dp),
                    color = textColor
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF0F1316)
@Composable
private fun CurrentWeekButtonPreview() {
    val context = LocalContext.current
    ContextKeeper.appContext = context
    AndroidThreeTen.init(context)

    TodayButton(title = "Today", onClick = {})
}