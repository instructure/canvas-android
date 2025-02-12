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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs


@Composable
fun LabelValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false
) {
    Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick, enabled = !loading)
            .semantics {
                role = Role.Button
            }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (loading) {
            Spacer(modifier = Modifier.weight(1f))
            CircularProgressIndicator(
                color = Color(ThemePrefs.buttonColor),
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Text(
                text = value,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .weight(1f),
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End
            )
            Icon(
                painter = painterResource(id = R.drawable.arrow_right),
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
                tint = colorResource(id = R.color.textDark)
            )
        }
    }
}

@Preview
@Composable
fun LabelValueRowPreview() {
    ContextKeeper.appContext = LocalContext.current
    LabelValueRow(
        label = "Label",
        value = "Value",
        onClick = {},
        loading = false
    )
}
