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
package com.instructure.parentapp.util.navigation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.navigation.WebViewRouter

class ParentWebViewRouter(val activity: FragmentActivity) : WebViewRouter {

    override fun canRouteInternally(url: String, routeIfPossible: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun routeInternally(url: String, extras: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun openMedia(url: String, mime: String, filename: String, canvasContext: CanvasContext?) {
        TODO("Not yet implemented")
    }

    override fun routeExternally(url: String) {
        TODO("Not yet implemented")
    }

    override fun openLtiScreen(canvasContext: CanvasContext?, url: String) {
        TODO("Not yet implemented")
    }

    override fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?) {
        TODO("Not yet implemented")
    }
}