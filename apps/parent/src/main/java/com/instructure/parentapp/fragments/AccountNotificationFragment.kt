package com.instructure.parentapp.fragments

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

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.util.RouteMatcher
import kotlinx.android.synthetic.main.fragment_account_notification.*

class AccountNotificationFragment : ParentFragment() {

    private var accountNotification: AccountNotification by ParcelableArg(key = Const.ACCOUNT_NOTIFICATION)
    private var student: User by ParcelableArg(key = Const.STUDENT)

    override fun onPause() {
        super.onPause()
        accountNotificationWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        accountNotificationWebView.onResume()
    }

    override val rootLayout: Int
        get() = R.layout.fragment_account_notification

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(rootLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialogToolbar(view)
        setupViews()
    }

    override fun setupDialogToolbar(rootView: View) {
        super.setupDialogToolbar(rootView)

        toolbarTitle.text = accountNotification.subject
    }

    private fun setupViews() {
        accountNotificationWebView.addVideoClient(activity)

        accountNotificationWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
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

        accountNotificationWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {

            }

            override fun onPageStartedCallback(webView: WebView, url: String) {

            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {

            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                val uri = Uri.parse(url)
                return RouteMatcher.canRouteInternally(null, url, student, uri.host, false)
            }

            override fun routeInternallyCallback(url: String) {
                val uri = Uri.parse(url)
                RouteMatcher.canRouteInternally(activity, url, student, uri.host, true)
            }
        }


        accountNotificationWebView.loadHtml(accountNotification.message, accountNotification.subject)

    }

    companion object {

        fun newInstance(notification: AccountNotification, student: User): AccountNotificationFragment {
            val args = Bundle()
            args.putParcelable(Const.ACCOUNT_NOTIFICATION, notification)
            args.putParcelable(Const.STUDENT, student)
            val fragment = AccountNotificationFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
