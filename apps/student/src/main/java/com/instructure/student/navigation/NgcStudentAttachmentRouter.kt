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
package com.instructure.student.navigation

import androidx.fragment.app.FragmentActivity
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.ngc.navigation.NgcAttachmentRouter
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.student.activity.NgcFileViewerActivity
import com.instructure.student.router.RouteMatcher

/**
 * [NgcAttachmentRouter] for the NGC experience. NGC runs in its own Activity (not inside the main
 * [com.instructure.student.activity.NavigationActivity], which is `singleTask`), so routing through
 * [RouteMatcher] would reset that activity to a root route and surface its bottom navigation bar.
 * Attachment/media previews are opened in their own standalone activities instead.
 */
class NgcStudentAttachmentRouter(
    private val activity: FragmentActivity,
) : NgcAttachmentRouter {

    // Comment attachment URLs are Canvas file-download URLs, served with Content-Disposition:
    // attachment - a plain WebView navigated straight to one never renders anything (its download
    // listener intercepts the load instead), which is why routing every file through
    // InternalWebViewActivity left a blank screen. Preview them the same way the rest of the app does
    // (mirrors the Academic/Teacher viewMedia flow) but in their own standalone Activity: NGC surfaces
    // its comments in a bottom sheet, so a fragment hosted over the NGC content would sit hidden behind
    // that sheet. Images/HTML/other files open in NgcFileViewerActivity; PDFs go through
    // RouteMatcher.openMedia (downloads first, then opens PSPDFKit in its own standalone Activity).
    // None of these surface the NavigationActivity bottom bar.
    override fun openFileAttachment(url: String, contentType: String, fileName: String) {
        when (contentType) {
            "application/pdf" -> RouteMatcher.openMedia(activity, url, contentType)
            else -> activity.startActivity(NgcFileViewerActivity.createIntent(activity, url, contentType, fileName))
        }
    }

    // The submission preview already knows it's playable media, so route straight to the media
    // viewer. RouteMatcher.openMedia would re-classify the URL and, for extension-less media URLs,
    // fall back to downloading / opening externally.
    override fun openMediaFullScreen(url: String, contentType: String, displayName: String?, thumbnailUrl: String?) {
        val bundle = BaseViewMediaActivity.makeBundle(url, thumbnailUrl, contentType, displayName, false)
        RouteMatcher.route(activity, Route(bundle, RouteContext.MEDIA))
    }
}
