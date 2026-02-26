/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import java.util.*

class DefaultAppShortcutManager : AppShortcutManager {

    override fun make(context: Context) {
        val manager = context.getSystemService(ShortcutManager::class.java)

        val bookmarksIntent = Intent(context, LoginActivity::class.java)
        bookmarksIntent.action = AppShortcutManager.ACTION_APP_SHORTCUT
        bookmarksIntent.putExtra(AppShortcutManager.APP_SHORTCUT_PLACEMENT, AppShortcutManager.APP_SHORTCUT_BOOKMARKS)
        val shortcutBookmarks = createShortcut(context, AppShortcutManager.APP_SHORTCUT_BOOKMARKS,
            context.getString(R.string.bookmarks),
            context.getString(R.string.bookmarks),
            R.mipmap.ic_shortcut_bookmarks,
            bookmarksIntent)

        val calendarIntent = Intent(context, LoginActivity::class.java)
        calendarIntent.action = AppShortcutManager.ACTION_APP_SHORTCUT
        calendarIntent.putExtra(AppShortcutManager.APP_SHORTCUT_PLACEMENT, AppShortcutManager.APP_SHORTCUT_CALENDAR)
        val shortcutCalendar = createShortcut(context, AppShortcutManager.APP_SHORTCUT_CALENDAR,
            context.getString(R.string.calendar),
            context.getString(R.string.calendar),
            R.mipmap.ic_shortcut_calendar,
            calendarIntent)

        val todoIntent = Intent(context, LoginActivity::class.java)
        todoIntent.action = AppShortcutManager.ACTION_APP_SHORTCUT
        todoIntent.putExtra(AppShortcutManager.APP_SHORTCUT_PLACEMENT, AppShortcutManager.APP_SHORTCUT_TODO)
        val shortcutTodo = createShortcut(context, AppShortcutManager.APP_SHORTCUT_TODO,
            context.getString(R.string.toDoListNew),
            context.getString(R.string.toDoListNew),
            R.mipmap.ic_shortcut_todo,
            todoIntent)

        val notificationsIntent = Intent(context, LoginActivity::class.java)
        notificationsIntent.action = AppShortcutManager.ACTION_APP_SHORTCUT
        notificationsIntent.putExtra(AppShortcutManager.APP_SHORTCUT_PLACEMENT, AppShortcutManager.APP_SHORTCUT_NOTIFICATIONS)
        val shortcutNotifications = createShortcut(context, AppShortcutManager.APP_SHORTCUT_NOTIFICATIONS,
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