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

package com.instructure.parentapp.features.webview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.instructure.pandautils.base.BaseCanvasActivity
import com.instructure.pandautils.fragments.HtmlContentFragment
import com.instructure.pandautils.fragments.HtmlContentFragment.Companion.DARK_TOOLBAR
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import com.instructure.parentapp.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HtmlContentActivity : BaseCanvasActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_html_content)
        EdgeToEdgeHelper.enableEdgeToEdge(this)

        if (savedInstanceState == null) {
            val title = intent.getStringExtra(Const.TITLE).orEmpty()
            val html = intent.getStringExtra(Const.HTML).orEmpty()
            val darkToolbar = intent.getBooleanExtra(DARK_TOOLBAR, false)

            val fragment = HtmlContentFragment.newInstance(
                HtmlContentFragment.makeBundle(title, html, darkToolbar)
            )

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }

    companion object {
        fun createIntent(context: Context, title: String, html: String, darkToolbar: Boolean): Intent {
            return Intent(context, HtmlContentActivity::class.java).apply {
                putExtra(Const.TITLE, title)
                putExtra(Const.HTML, html)
                putExtra(DARK_TOOLBAR, darkToolbar)
            }
        }
    }
}
