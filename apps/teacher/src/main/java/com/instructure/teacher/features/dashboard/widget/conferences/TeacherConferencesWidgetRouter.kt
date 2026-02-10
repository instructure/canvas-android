/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.features.dashboard.widget.conferences

import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.dashboard.widget.conferences.ConferencesWidgetRouter
import com.instructure.pandautils.utils.color

class TeacherConferencesWidgetRouter : ConferencesWidgetRouter {

    override fun launchConference(activity: FragmentActivity, canvasContext: CanvasContext, url: String) {
        val colorScheme = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(canvasContext.color)
            .build()
        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorScheme)
            .setShowTitle(true)
            .build()
            .launchUrl(activity, url.toUri())
    }
}
