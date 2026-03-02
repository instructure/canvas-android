/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.instructure.interactions.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * A fullscreen activity that handles its own configuration changes (orientation, screen size, keyboard)
 * so the hosted fragment and its views (e.g. WebView) are never recreated on rotation.
 * Use this instead of [FullscreenActivity] for fragments whose content must survive rotation.
 */
@AndroidEntryPoint
class RetainedFullscreenActivity : FullscreenActivity() {

    companion object {
        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, RetainedFullscreenActivity::class.java)
            intent.putExtra(Route.ROUTE, route as Parcelable)
            return intent
        }
    }
}
