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
package com.instructure.pandautils.features.reminder.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandares.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CardListItem
import com.instructure.pandautils.compose.composables.CardListView
import com.instructure.pandautils.features.reminder.ReminderItem
import com.instructure.pandautils.features.reminder.ReminderViewState
import com.instructure.pandautils.utils.toFormattedString

@Composable
fun ReminderView(
    viewState: ReminderViewState,
    onAddClick: (String?) -> Unit,
    onRemoveClick: (Long) -> Unit,
) {
    CanvasTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.reminderTitle),
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.semantics { heading() }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { onAddClick(viewState.tag) }
            ) {
                IconButton(onClick = { onAddClick(viewState.tag) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = stringResource(id = R.string.a11y_addReminder),
                        tint = viewState.getThemeColor(LocalContext.current)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(id = R.string.reminderDescription),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textDarkest),
                )
            }
            if (viewState.dueDate == null) {
                RemindersGroupView(
                    title = null,
                    reminders = viewState.reminders.sortedBy { it.date },
                    themeColor = viewState.getThemeColor(LocalContext.current),
                    onRemoveClick = onRemoveClick
                )
            } else {
                val remindersBeforeDueDate =
                    viewState.reminders.filter { it.date <= viewState.dueDate }
                val remindersAfterDueDate =
                    viewState.reminders.filter { it.date > viewState.dueDate }

                RemindersGroupView(
                    title = null,
                    reminders = remindersBeforeDueDate.sortedBy { it.date },
                    themeColor = viewState.getThemeColor(LocalContext.current),
                    onRemoveClick = onRemoveClick
                )

                RemindersGroupView(
                    title = stringResource(
                        com.instructure.pandautils.R.string.reminderTitleAfterDueDate,
                        viewState.dueDate.toFormattedString()
                    ),
                    reminders = remindersAfterDueDate.sortedBy { it.date },
                    themeColor = viewState.getThemeColor(LocalContext.current),
                    onRemoveClick = onRemoveClick
                )
            }
        }
    }
}

@Composable
private fun RemindersGroupView(title: String?, reminders: List<ReminderItem>, themeColor: Color, onRemoveClick: (Long) -> Unit) {
    if (reminders.isEmpty()) return

    title?.let {
        Text(
            text = it,
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark),
            modifier = Modifier.padding(all = 8.dp)
        )
    }

    val listItems = reminders.map { reminder ->
        CardListItem(
            id = reminder.id,
            title = reminder.title,
            subtitle = null,
            icon = R.drawable.ic_notifications_lined,
            themeColor = themeColor
        )
    }

    CardListView(items = listItems, onSelect = null, onRemove = { onRemoveClick(it.id) })
}



@Composable
@Preview
private fun ReminderViewPreview() {
    ContextKeeper.appContext = LocalContext.current

    ReminderView(
        viewState = ReminderViewState(
            reminders = emptyList(),
            themeColor = Color.Red
        ),
        onAddClick = {},
        onRemoveClick = {}
    )
}