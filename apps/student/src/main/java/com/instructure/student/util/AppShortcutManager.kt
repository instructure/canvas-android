/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.student.util

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import java.util.*

@TargetApi(25)
object AppShortcutManager {

    const val ACTION_APP_SHORTCUT = "com.instructure.action.APP_SHORTCUT"
    const val APP_SHORTCUT_PLACEMENT = "com.instructure.APP_SHORTCUT_PLACEMENT"

    const val APP_SHORTCUT_BOOKMARKS = "com.instructure.APP_SHORTCUT_BOOKMARKS"
    const val APP_SHORTCUT_CALENDAR = "com.instructure.APP_SHORTCUT_CALENDAR"
    const val APP_SHORTCUT_TODO = "com.instructure.APP_SHORTCUT_TODO"
    const val APP_SHORTCUT_NOTIFICATIONS = "com.instructure.APP_SHORTCUT_NOTIFICATIONS"
    const val APP_SHORTCUT_INBOX = "com.instructure.APP_SHORTCUT_INBOX"

    fun make(context: Context) {

        if (Build.VERSION.SDK_INT < 25) return

        val manager = context.getSystemService(ShortcutManager::class.java)

        val bookmarksIntent = Intent(context, LoginActivity::class.java)
        bookmarksIntent.action = ACTION_APP_SHORTCUT
        bookmarksIntent.putExtra(APP_SHORTCUT_PLACEMENT, APP_SHORTCUT_BOOKMARKS)
        val shortcutBookmarks = createShortcut(context, APP_SHORTCUT_BOOKMARKS,
                context.getString(R.string.bookmarks),
                context.getString(R.string.bookmarks),
                R.mipmap.ic_shortcut_bookmarks,
                bookmarksIntent)

        val calendarIntent = Intent(context, LoginActivity::class.java)
        calendarIntent.action = ACTION_APP_SHORTCUT
        calendarIntent.putExtra(APP_SHORTCUT_PLACEMENT, APP_SHORTCUT_CALENDAR)
        val shortcutCalendar = createShortcut(context, APP_SHORTCUT_CALENDAR,
                context.getString(R.string.calendar),
                context.getString(R.string.calendar),
                R.mipmap.ic_shortcut_calendar,
                calendarIntent)

        val todoIntent = Intent(context, LoginActivity::class.java)
        todoIntent.action = ACTION_APP_SHORTCUT
        todoIntent.putExtra(APP_SHORTCUT_PLACEMENT, APP_SHORTCUT_TODO)
        val shortcutTodo = createShortcut(context, APP_SHORTCUT_TODO,
                context.getString(R.string.toDoList),
                context.getString(R.string.toDoList),
                R.mipmap.ic_shortcut_todo,
                todoIntent)

        val notificationsIntent = Intent(context, LoginActivity::class.java)
        notificationsIntent.action = ACTION_APP_SHORTCUT
        notificationsIntent.putExtra(APP_SHORTCUT_PLACEMENT, APP_SHORTCUT_NOTIFICATIONS)
        val shortcutNotifications = createShortcut(context, APP_SHORTCUT_NOTIFICATIONS,
                context.getString(R.string.notifications),
                context.getString(R.string.notifications),
                R.mipmap.ic_shortcut_notifications,
                notificationsIntent)

        manager?.dynamicShortcuts = Arrays.asList(shortcutNotifications, shortcutTodo, shortcutCalendar, shortcutBookmarks)
    }

    private fun createShortcut(context: Context, id: String, label: String, longLabel: String, iconResId: Int, intent: Intent): ShortcutInfo {
        return ShortcutInfo.Builder(context, id)
                .setShortLabel(label)
                .setLongLabel(longLabel)
                .setIcon(Icon.createWithResource(context, iconResId))
                .setIntent(intent)
                .build()
    }
}
