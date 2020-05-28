/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.util.RouteMatcher
import kotlinx.android.synthetic.main.fragment_announcement.*

class AnnouncementFragment : ParentFragment() {

    private var name by StringArg(key = Const.NAME)
    private var announcement by ParcelableArg<DiscussionTopicHeader>(key = Const.ANNOUNCEMENT)
    private var student by ParcelableArg<User>(key = Const.STUDENT)

    override val rootLayout: Int
        get() = R.layout.fragment_announcement

    override fun onPause() {
        super.onPause()
        announcementWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        announcementWebView.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(rootLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupDialogToolbar(view)
    }

    private fun setupViews() {
        announcementName.text = announcement.title

        courseName.text = name
        announcementWebView.addVideoClient(activity)

        announcementWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                //create and add the InternalWebviewFragment to deal with the link they clicked
                val internalWebviewFragment = InternalWebViewFragment()
                internalWebviewFragment.arguments = InternalWebViewFragment.createBundle(url, "", null, student)

                val ft = requireFragmentManager().beginTransaction()
                ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom)
                ft.add(R.id.fullscreen, internalWebviewFragment, internalWebviewFragment.javaClass.name)
                ft.addToBackStack(internalWebviewFragment.javaClass.name)
                ft.commitAllowingStateLoss()
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                return true
            }
        }

        announcementWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {

            }

            override fun onPageStartedCallback(webView: WebView, url: String) {

            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {

            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                val uri = Uri.parse(announcement.htmlUrl)
                return RouteMatcher.canRouteInternally(null, url, student, uri.host, false)
            }

            override fun routeInternallyCallback(url: String) {
                val uri = Uri.parse(announcement.htmlUrl)
                RouteMatcher.canRouteInternally(activity, url, student, uri.host, true)
            }
        }

        if (announcement.attachments != null && announcement.attachments.size > 0) {
            formatHTMLWithAttachment()
        } else {
            announcementWebView.loadHtml(announcement.message, announcement.title)
        }


    }

    private fun formatHTMLWithAttachment() {
        val attachment = announcement.attachments[0]
        val html = announcement.message + "<p><a href=\"" + attachment.url + "\" >" + attachment.fileName + "</a></p>"

        announcementWebView.loadHtml(html, announcement.title)
    }

    companion object {

        fun newInstance(announcement: DiscussionTopicHeader, courseName: String, student: User): AnnouncementFragment {
            val args = Bundle()
            args.putParcelable(Const.ANNOUNCEMENT, announcement)
            args.putString(Const.NAME, courseName)
            args.putParcelable(Const.STUDENT, student)
            val fragment = AnnouncementFragment()
            fragment.arguments = args
            return fragment
        }
    }

}
