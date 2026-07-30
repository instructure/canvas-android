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
package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.instructure.pandautils.activities.BaseActionBarActivity
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.ViewHtmlFragment
import com.instructure.student.fragment.ViewImageFragment
import com.instructure.student.fragment.ViewUnsupportedFileFragment

/**
 * Standalone host for previewing an NGC submission-comment file attachment. NGC surfaces its comments in
 * a bottom sheet; hosting a preview fragment over that same Activity's content would leave it hidden
 * behind the sheet, so previews open here in their own Activity instead. Owns the toolbar and hosts the
 * type-appropriate viewer (image / HTML / unsupported-file), suppressing each fragment's own toolbar so
 * there's a single, consistent one. PDFs and audio/video keep their existing dedicated viewers.
 */
class NgcFileViewerActivity : BaseActionBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)
        toolbar?.let { ViewStyler.themeToolbarLight(this, it) }
        setupWindowInsets()

        if (savedInstanceState == null) {
            val url = intent.getStringExtra(EXTRA_URL).orEmpty()
            val contentType = intent.getStringExtra(EXTRA_CONTENT_TYPE).orEmpty()
            val fileName = intent.getStringExtra(EXTRA_FILE_NAME).orEmpty()

            if (url.isEmpty()) {
                toast(R.string.somethingWentWrong)
                finish()
                return
            }

            toolbar?.title = fileName

            val fragment = when {
                contentType.startsWith("image/") ->
                    ViewImageFragment.newInstance(fileName, Uri.parse(url), contentType, showToolbar = false)
                contentType == "text/html" ->
                    ViewHtmlFragment.newInstance(url, fileName).apply { hideToolbar = true }
                else ->
                    ViewUnsupportedFileFragment.newInstance(Uri.parse(url), fileName, contentType, null, R.drawable.ic_attachment)
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment, fragment.javaClass.name)
                .commitAllowingStateLoss()
        }
    }

    override fun contentResId(): Int = R.layout.base_layout

    override fun showHomeAsUp(): Boolean = true

    override fun showTitleEnabled(): Boolean = false

    override fun onUpPressed() = finish()

    override fun onBackPressed() {
        // Let an embedded WebView (HTML preview) consume back for in-page navigation before we finish.
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? InternalWebviewFragment
        if (fragment?.handleBackPressed() != true) super.onBackPressed()
    }

    // Edge-to-edge draws content behind the system bars, so inset the toolbar below the status bar and
    // keep the content clear of the display cutout / navigation bar (mirrors InternalWebViewActivity).
    private fun setupWindowInsets() {
        toolbar?.let { toolbar ->
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                val leftPadding = if (isLandscape) maxOf(systemBars.left, displayCutout.left) else displayCutout.left
                val rightPadding = if (isLandscape) maxOf(systemBars.right, displayCutout.right) else displayCutout.right
                view.setPadding(leftPadding, systemBars.top, rightPadding, 0)
                insets
            }
        }

        findViewById<View>(R.id.container)?.let { container ->
            ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
                val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                if (isLandscape) {
                    val leftPadding = maxOf(navigationBars.left, displayCutout.left)
                    val rightPadding = maxOf(navigationBars.right, displayCutout.right)
                    view.setPadding(leftPadding, 0, rightPadding, navigationBars.bottom)
                } else {
                    view.setPadding(displayCutout.left, 0, displayCutout.right, 0)
                }
                insets
            }
        }
    }

    companion object {
        private const val EXTRA_URL = "url"
        private const val EXTRA_CONTENT_TYPE = "contentType"
        private const val EXTRA_FILE_NAME = "fileName"

        fun createIntent(context: Context, url: String, contentType: String, fileName: String): Intent {
            return Intent(context, NgcFileViewerActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_CONTENT_TYPE, contentType)
                putExtra(EXTRA_FILE_NAME, fileName)
            }
        }
    }
}
