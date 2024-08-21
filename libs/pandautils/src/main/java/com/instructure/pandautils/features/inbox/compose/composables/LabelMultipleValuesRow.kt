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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.UserAvatar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> LabelMultipleValuesRow(
    label: String,
    selectedValues: List<T>,
    addValueClicked: () -> Unit,
    itemComposable: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false
) {
    val animationLabel = "LabelMultipleValuesRowTransition"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
            .defaultMinSize(minHeight = 52.dp)
    ) {
        Column {
            Text(
                text = label,
                color = colorResource(id = R.color.textDarkest),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
        if (loading) {
            Spacer(modifier = Modifier.weight(1f))
            Loading(
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(8.dp))

            FlowRow(Modifier.weight(1f)) {
                for (value in selectedValues) {
                    AnimatedContent(
                        label = animationLabel,
                        targetState = value,
                    ){
                        itemComposable(it)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = { addValueClicked() },
            modifier = Modifier
                .size(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_lined),
                contentDescription = null,
                tint = colorResource(id = R.color.textDarkest)
            )
        }
    }
}

@Preview
@Composable
fun LabelMultipleValuesRowPreview() {
    ContextKeeper.appContext = LocalContext.current
    val users = listOf(
        Recipient(name = "Person 1"),
        Recipient(name = "Person 2"),
        Recipient(name = "Person 3"),
    )
    LabelMultipleValuesRow(
        label = "To",
        selectedValues = users,
        itemComposable = { user ->
            UserAvatar(user.avatarURL, user.name ?: "")
        },
        addValueClicked = {},
        loading = false,
        modifier = Modifier
            .fillMaxWidth()
    )
}