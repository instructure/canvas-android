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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R

@Composable
fun LabelSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    fontWeight: FontWeight? = null,
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 8.dp)
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(52.dp)
                .alpha(if (enabled) 1f else 0.5f)
        ) {
            Text(
                text = label,
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp,
                fontWeight = fontWeight,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            CanvasSwitch(
                checked = checked,
                onCheckedChange = {
                    onCheckedChange(it)
                },
                enabled = enabled,
                modifier = Modifier
                    .testTag("switch")
            )
        }

        if (subtitle != null) {
            Text(
                text = subtitle,
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        }
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

@Preview
@Composable
fun LabelSwitchRowCheckedWithSubtitlePreview() {
    ContextKeeper.appContext = LocalContext.current
    LabelSwitchRow(
        label = "Switch row with a really long label that should be truncated and a subtitle",
        subtitle = "This a short description",
        checked = true,
        enabled = true,
        onCheckedChange = {},
    )
}