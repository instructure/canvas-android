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
package com.emeritus.student.util

import android.content.Context

interface AppShortcutManager {

    fun make(context: Context)

    companion object {
        const val ACTION_APP_SHORTCUT = "com.instructure.action.APP_SHORTCUT"
        const val APP_SHORTCUT_PLACEMENT = "com.instructure.APP_SHORTCUT_PLACEMENT"

        const val APP_SHORTCUT_BOOKMARKS = "com.instructure.APP_SHORTCUT_BOOKMARKS"
        const val APP_SHORTCUT_CALENDAR = "com.instructure.APP_SHORTCUT_CALENDAR"
        const val APP_SHORTCUT_TODO = "com.instructure.APP_SHORTCUT_TODO"
        const val APP_SHORTCUT_NOTIFICATIONS = "com.instructure.APP_SHORTCUT_NOTIFICATIONS"
        const val APP_SHORTCUT_INBOX = "com.instructure.APP_SHORTCUT_INBOX"
    }
}