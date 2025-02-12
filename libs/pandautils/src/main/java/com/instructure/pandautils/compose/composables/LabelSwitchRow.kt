/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.compose.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@Composable
fun LabelSwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(52.dp)
            .padding(start = 16.dp, end = 8.dp)
            .padding(vertical = 8.dp)
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Text(
            text = label,
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
            },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(ThemePrefs.brandColor),
                checkedTrackColor = Color(ThemePrefs.brandColor).copy(alpha = 0.5f),
                uncheckedThumbColor = colorResource(id = R.color.backgroundDark),
                uncheckedTrackColor = colorResource(id = R.color.backgroundMedium),
            ),
            modifier = Modifier
                .testTag("switch")
        )

    }
}

@Preview
@Composable
fun LabelSwitchRowCheckedPreview() {
    ContextKeeper.appContext = LocalContext.current
    LabelSwitchRow(
        label = "Switch row",
        checked = true,
        enabled = true,
        onCheckedChange = {},
    )
}

@Preview
@Composable
fun LabelSwitchRowUncheckedPreview() {
    ContextKeeper.appContext = LocalContext.current
    LabelSwitchRow(
        label = "Switch row",
        checked = false,
        enabled = true,
        onCheckedChange = {},
    )
}