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
package com.instructure.pandautils.navigation

import android.os.Bundle
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz

interface WebViewRouter {

    fun canRouteInternally(url: String, routeIfPossible: Boolean = false): Boolean

    fun routeInternally(url: String, extras: Bundle? = null)

    fun openMedia(url: String, mime: String = "", filename: String = "", canvasContext: CanvasContext? = null)

    fun routeExternally(url: String)

    fun openLtiScreen(canvasContext: CanvasContext?, url: String)

    fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?)

    /**
     * Launches an assignment-based LTI tool (e.g. a New Quiz). Unlike [openLtiScreen]'s sessionless
     * launch, this uses the pre-fetched [ltiTool] and the assignment-LTI launch flow, which is
     * required for tools that can collide on URL. Defaults to a sessionless launch for routers that
     * don't support the richer flow.
     */
    fun launchAssignmentLtiTool(
        canvasContext: CanvasContext?,
        url: String,
        title: String?,
        ltiTool: LTITool?,
        openInternally: Boolean,
    ) = openLtiScreen(canvasContext, url)

    /**
     * Opens a (classic) quiz. Defaults to routing the URL internally; routers that host their own
     * fragments (e.g. NGC) can override to show the dedicated quiz screen.
     */
    fun launchInternalQuiz(canvasContext: CanvasContext?, quiz: Quiz, url: String) = routeInternally(url)

    /**
     * Launches the Studio submission flow (external tool WebView). Reuses the academic Studio
     * implementation. Routers that host their own fragments (e.g. NGC) override this to show the
     * Studio screen over their content; the default is a no-op for routers that don't support it.
     */
    fun launchStudio(canvasContext: CanvasContext?, assignment: Assignment, studioLTITool: LTITool?) {}
}
