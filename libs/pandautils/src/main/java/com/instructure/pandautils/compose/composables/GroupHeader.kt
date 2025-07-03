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

package com.instructure.pandautils.compose.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GroupHeader(
    name: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTopDivider: Boolean = true
) {
    val iconRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "expandedIconRotation")

    val headerContentDescription = stringResource(
        if (expanded) {
            R.string.content_description_collapse_content_with_param
        } else {
            R.string.content_description_expand_content_with_param
        },
        name
    )

    Column(
        modifier = modifier
            .background(colorResource(id = R.color.backgroundLightest))
            .clickable {
                onClick()
            }
            .semantics {
                heading()
                contentDescription = headerContentDescription
                role = Role.Button
            }
    ) {
        if (showTopDivider) {
            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            Text(
                text = name,
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp,
                modifier = Modifier.semantics {
                    invisibleToUser()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                tint = colorResource(id = R.color.textDarkest),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(iconRotation)
            )
        }
        Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
    }
}

@Preview
@Composable
private fun GroupHeaderPreview() {
    GroupHeader(
        name = "Group Header",
        expanded = true,
        onClick = {}
    )
}
