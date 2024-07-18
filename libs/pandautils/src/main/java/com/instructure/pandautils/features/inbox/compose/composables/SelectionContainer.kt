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
package com.instructure.pandautils.features.inbox.compose.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.ThemePrefs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelMultipleValuesRow(
    label: String,
    selectedValues: List<String>,
    onSelect: (String) -> Unit,
    addValueClicked: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false
) {
    Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Column {
            Text(
                text = label,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
        }
        if (loading) {
            Spacer(modifier = Modifier.weight(1f))
            CircularProgressIndicator(
                color = Color(ThemePrefs.buttonColor),
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
                FlowRow(Modifier.weight(1f)) {
                    for (value in selectedValues) {
                        Text(
                            text = value,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {
                                    onSelect(value)
                                },
                            color = colorResource(id = R.color.textDark),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End
                        )
                    }
                }
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .clickable { addValueClicked() },
            tint = colorResource(id = R.color.textDark)
        )
    }
}

@Preview
@Composable
fun LabelMultipleValuesRowPreview() {
    ContextKeeper.appContext = LocalContext.current
    LabelMultipleValuesRow(
        label = "To",
        selectedValues = listOf("Person 1", "Person 2", "Person 3", "Person 4", "Person 5"),
        onSelect = {},
        addValueClicked = {},
        loading = false
    )
}