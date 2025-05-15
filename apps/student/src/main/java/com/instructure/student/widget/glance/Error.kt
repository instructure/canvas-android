/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.widget.glance

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.instructure.student.R


@Composable
fun Error(
    @DrawableRes imageRes: Int,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(vertical = 24.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(imageRes),
            contentDescription = null,
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.height(16.dp))
        Text(
            text = LocalContext.current.getString(titleRes),
            style = TextStyle(
                color = WidgetColors.textDarkest,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            modifier = GlanceModifier.fillMaxWidth()
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = LocalContext.current.getString(subtitleRes),
            style = TextStyle(
                color = WidgetColors.textDark,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            ),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 250, heightDp = 200)
@Composable
private fun ErrorPreview() {
    Error(
        imageRes = R.drawable.ic_panda_notsupported,
        titleRes = R.string.widgetErrorTitle,
        subtitleRes = R.string.widgetErrorSubtitle
    )
}
