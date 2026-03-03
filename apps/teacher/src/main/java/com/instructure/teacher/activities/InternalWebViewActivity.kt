/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.activities.BasePresenterActivity
import com.instructure.pandautils.utils.Const
import com.instructure.teacher.R
import com.instructure.teacher.factory.InternalWebViewPresenterFactory
import com.instructure.teacher.fragments.InternalWebViewFragment
import com.instructure.teacher.presenters.InternalWebViewPresenter
import com.instructure.teacher.viewinterface.InternalWebView

class InternalWebViewActivity : BasePresenterActivity<InternalWebViewPresenter, InternalWebView>(), InternalWebView {

    private var mInternalWebView: InternalWebViewFragment? = null
    private var mUrl: String? = null
    private var mActionbarTitle: String? = null
    private var mHtml: String? = null
    private var mAuthenticate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internal_webview)
        handleIntentExtras(intent.getBundleExtra(Const.EXTRAS))
    }

    override fun onReadySetGo(presenter: InternalWebViewPresenter) = setupViews()

    override fun onPresenterPrepared(presenter: InternalWebViewPresenter) = Unit

    override fun getPresenterFactory() = InternalWebViewPresenterFactory()

    private fun setupViews() {
        mInternalWebView = InternalWebViewFragment.newInstance(mUrl!!, mHtml!!, mActionbarTitle!!)
        mInternalWebView?.let {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.internalWebViewContainer, it, InternalWebViewFragment::class.java.simpleName)
                .commit()
        }
    }

    private fun handleIntentExtras(extras: Bundle?) {
        if (extras == null) {
            return
        }

        with(extras) {
            mUrl = getString(Const.INTERNAL_URL)
            mActionbarTitle = getString(Const.ACTION_BAR_TITLE, "")
            mAuthenticate = getBoolean(Const.AUTHENTICATE)
            mHtml = getString(Const.HTML, "")
        }
    }

    override fun onBackPressed() {
        mInternalWebView?.let {
            if (it.canGoBack()) {
                it.goBack()
                return
            }
        }
        super.onBackPressed()
    }

    ///////////////////////////////////////////////////////////////////////////
    // ACTION BAR STATUS BAR COLORS
    ///////////////////////////////////////////////////////////////////////////

    override fun unBundle(extras: Bundle) = Unit

    companion object {

        fun createIntent(context: Context, url: String, title: String, authenticate: Boolean): Intent {
            // Assumes no canvasContext
            val extras = createBundle(null, url, title, authenticate)
            return Intent(context, InternalWebViewActivity::class.java).apply {
                putExtra(Const.EXTRAS, extras)
            }
        }

        fun createIntent(context: Context, url: String?, html: String, title: String, authenticate: Boolean): Intent {
            // Assumes no canvasContext
            val extras = createBundle(null, url.orEmpty(), title, authenticate)
            extras.putString(Const.HTML, html)
            return Intent(context, InternalWebViewActivity::class.java).apply {
                putExtra(Const.EXTRAS, extras)
            }
        }

        fun createIntent(context: Context, route: Route, title: String, authenticate: Boolean)
                = createIntent(context, route.uri?.toString() ?: "", title, authenticate)

        fun createBundle(canvasContext: CanvasContext?, route: Route, title: String, authenticate: Boolean): Bundle {
            return Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putString(Const.INTERNAL_URL, route.uri?.toString() ?: "")
                putBoolean(Const.AUTHENTICATE, authenticate)
                putString(Const.ACTION_BAR_TITLE, title)
            }
        }

        fun createBundle(canvasContext: CanvasContext?, url: String, title: String, authenticate: Boolean): Bundle {
            return Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putString(Const.INTERNAL_URL, url)
                putBoolean(Const.AUTHENTICATE, authenticate)
                putString(Const.ACTION_BAR_TITLE, title)
            }
        }
    }
}
