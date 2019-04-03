/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.parentapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.parentapp.R
import com.instructure.parentapp.fragments.InternalWebViewFragment
import kotlinx.android.synthetic.main.toolbar_layout.*

class InternalWebViewActivity : AppCompatActivity() {

    private lateinit var webView: InternalWebViewFragment
    private var url: String? = null
    private var title: String? = null
    private var html: String? = null
    private var shouldAuthenticate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internal_webview)
        handleIntentExtras(intent.extras)
        setupViews()
    }

    private fun setupViews() {
        webView = InternalWebViewFragment.newInstance(title.orEmpty(), url, html)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.internalWebViewContainer, webView, InternalWebViewFragment::class.java.simpleName)
            .commit()
        toolbar.title = title.validOrNull() ?: url
        toolbar.setupAsBackButton { onBackPressed() }
    }

    private fun handleIntentExtras(extras: Bundle?) = with(extras) {
        this ?: return
        url = getString(Const.INTERNAL_URL)
        title = getString(Const.ACTION_BAR_TITLE, "")
        shouldAuthenticate = getBoolean(Const.AUTHENTICATE)
        html = getString(Const.HTML, "")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    companion object {

        @JvmStatic
        fun createIntent(context: Context, url: String, title: String, authenticate: Boolean): Intent {
            // Assumes no canvasContext
            val extras = createBundle(url, title, authenticate)
            return Intent(context, InternalWebViewActivity::class.java).apply {
                putExtras(extras)
            }
        }

        @JvmStatic
        fun createIntent(context: Context, url: String, html: String, title: String, authenticate: Boolean): Intent {
            // Assumes no canvasContext
            val extras = createBundle(url, title, authenticate)
            extras.putString(Const.HTML, html)
            return Intent(context, InternalWebViewActivity::class.java).apply {
                putExtras(extras)
            }
        }

        @JvmStatic
        fun createIntent(context: Context, route: Route, title: String, authenticate: Boolean) =
            createIntent(context, route.uri?.toString() ?: "", title, authenticate)

        private fun createBundle(url: String, title: String, authenticate: Boolean): Bundle {
            return Bundle().apply {
                putString(Const.INTERNAL_URL, url)
                putBoolean(Const.AUTHENTICATE, authenticate)
                putString(Const.ACTION_BAR_TITLE, title)
            }
        }
    }
}

