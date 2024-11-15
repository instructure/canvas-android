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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandares.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.features.reminder.ReminderItem
import com.instructure.pandautils.features.reminder.ReminderViewState
import com.instructure.pandautils.utils.toFormattedString

@Composable
fun ReminderView(
    viewState: ReminderViewState,
    onAddClick: () -> Unit,
    onRemoveClick: (ReminderItem) -> Unit,
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
                color = colorResource(id = R.color.textDark)
            )

            Row(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                IconButton(onClick = { onAddClick() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = stringResource(id = R.string.a11y_addReminder),
                        tint = viewState.themeColor
                    )
                }

                Text(
                    text = stringResource(id = R.string.reminderDescription),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.textDarkest),
                )
            }
            if (viewState.dueDate == null) {
                RemindersGroupView(
                    title = null,
                    reminders = viewState.reminders,
                    themeColor = viewState.themeColor,
                    onRemoveClick = onRemoveClick
                )
            } else {
                val remindersBeforeDueDate =
                    viewState.reminders.filter { it.date <= viewState.dueDate }
                val remindersAfterDueDate =
                    viewState.reminders.filter { it.date > viewState.dueDate }

                RemindersGroupView(
                    title = null,
                    reminders = remindersBeforeDueDate,
                    themeColor = viewState.themeColor,
                    onRemoveClick = onRemoveClick
                )

                RemindersGroupView(
                    title = "After Due Date (${viewState.dueDate?.toFormattedString()})",
                    reminders = remindersAfterDueDate,
                    themeColor = viewState.themeColor,
                    onRemoveClick = onRemoveClick
                )
            }
        }
    }
}

@Composable
private fun RemindersGroupView(title: String?, reminders: List<ReminderItem>, themeColor: Color, onRemoveClick: (ReminderItem) -> Unit) {
    if (reminders.isEmpty()) return

    title?.let {
        Text(
            text = it,
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark),
            modifier = Modifier.padding(all = 8.dp)
        )
    }

    Card(
        backgroundColor = colorResource(id = R.color.backgroundLight),
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            reminders.forEach { reminder ->
                ReminderItemView(reminder, themeColor, onRemoveClick)

                if (reminder != reminders.last()) {
                    CanvasDivider(modifier = Modifier.padding(horizontal = 24.dp))
                }
            }
        }
    }
}

@Composable
private fun ReminderItemView(reminderItem: ReminderItem, themeColor: Color, onRemoveClick: (ReminderItem) -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            painter = painterResource(id = R.drawable.ic_notifications_lined),
            contentDescription = null,
            tint = themeColor
        )

        Text(
            text = reminderItem.title,
            fontSize = 16.sp,
            color = colorResource(id = R.color.textDarkest),
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { onRemoveClick(reminderItem) },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_lined),
                contentDescription = stringResource(id = R.string.a11y_removeReminder),
                tint = colorResource(id = R.color.textDarkest)
            )
        }
    }

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