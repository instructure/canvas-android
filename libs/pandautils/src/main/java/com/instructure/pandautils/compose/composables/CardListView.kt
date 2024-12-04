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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandares.R
import com.instructure.pandautils.compose.CanvasTheme

@Composable
fun CardListView(
    items: List<CardListItem>,
    onSelect: ((CardListItem) -> Unit)?,
    onRemove: ((CardListItem) -> Unit)?,
    modifier: Modifier = Modifier
) {
    CanvasTheme {
        Card(
            backgroundColor = colorResource(id = R.color.backgroundLight),
            elevation = 8.dp,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column {
                items.forEach { item ->
                    CardListItemView(item, onSelect, onRemove)

                    if (item != items.last()) {
                        CanvasDivider(modifier = Modifier.padding(horizontal = 24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CardListItemView(
    listItem: CardListItem,
    onSelect: ((CardListItem) -> Unit)?,
    onRemove: ((CardListItem) -> Unit)?
) {
    val clickableModifier = if (onSelect != null) {
        Modifier.clickable { onSelect.invoke(listItem) }
    } else {
        Modifier
    }

    Row(
        modifier = clickableModifier
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listItem.icon?.let { icon ->
            Icon(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = listItem.themeColor
            )
        }

        Column {
            Text(
                text = listItem.title,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            listItem.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.textDark),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        onRemove?.let {
            IconButton(
                onClick = { onRemove.invoke(listItem) },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_lined),
                    contentDescription = stringResource(id = R.string.a11y_remove),
                    tint = colorResource(id = R.color.textDarkest)
                )
            }
        }
    }
}

data class CardListItem(
    val id: Long,
    val title: String,
    val subtitle: String? = null,
    @DrawableRes val icon: Int? = null,
    val themeColor: Color,
)

@Preview
@Composable
fun CardListViewPreview() {
    val items = listOf(
        CardListItem(
            id = 1,
            title = "Item 1",
            subtitle = "Subtitle 1",
            icon = R.drawable.ic_add,
            themeColor = Color.Blue
        ),
        CardListItem(
            id = 2,
            title = "Item 2",
            subtitle = "Subtitle 2",
            icon = R.drawable.ic_add,
            themeColor = Color.Red
        ),
        CardListItem(
            id = 3,
            title = "Item 3",
            subtitle = "Subtitle 3",
            icon = R.drawable.ic_add,
            themeColor = Color.Green
        ),
    )

    CardListView(items = items, onSelect = null, onRemove = { })
}