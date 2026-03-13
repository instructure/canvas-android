/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
import android.graphics.Color
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContext.Companion.emptyCourseContext
import com.instructure.pandautils.activities.BaseActionBarActivity
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.EdgeToEdgeHelper
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.InternalWebviewFragment.Companion.makeRoute
import com.instructure.student.fragment.InternalWebviewFragment.Companion.newInstance

class InternalWebViewActivity : BaseActionBarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeHelper.enableEdgeToEdge(this)
        toolbar?.let { ViewStyler.themeToolbarLight(this, it) }
        setupWindowInsets()
        if (savedInstanceState == null) {
            val bundle = intent.getBundleExtra(Const.EXTRAS)
            bundle?.getString(Const.ACTION_BAR_TITLE)?.let { toolbar?.title = it }
            bundle?.getParcelable<CanvasContext>(Const.CANVAS_CONTEXT)?.let { canvasContext ->
                // Currently we use an empty context when showing the EULA, privacy policy, etc., in which case we
                // want the internalWebViewFragment to hide its toolbar
                if (canvasContext.id == 0L) {
                    bundle.putBoolean(Const.HIDDEN_TOOLBAR, true)
                } else {
                    val color = canvasContext.color
                    setActionBarStatusBarColor(color)
                    supportActionBar?.title = canvasContext.name
                }
            }

            if (bundle != null) {
                val fragment = newInstance(makeRoute(bundle))
                val ft = supportFragmentManager.beginTransaction()
                ft.add(R.id.container, fragment, InternalWebviewFragment::class.java.name)
                ft.commitAllowingStateLoss()
            } else {
                toast(R.string.somethingWentWrong)
            }
        }
    }

    override fun contentResId(): Int = R.layout.base_layout

    override fun showHomeAsUp(): Boolean = true

    override fun showTitleEnabled(): Boolean = false

    override fun onUpPressed() = finish()

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container) as? InternalWebviewFragment
        if (fragment?.handleBackPressed() != true) super.onBackPressed()
    }

    private fun setupWindowInsets() {
        val container = findViewById<android.view.View>(R.id.container)

        // Setup toolbar insets - handle top (status bar) + horizontal (nav bars + display cutout) in one listener
        toolbar?.let { toolbar ->
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                if (isLandscape) {
                    // In landscape, apply status bar at top and nav bars + display cutout horizontally
                    val leftPadding = maxOf(systemBars.left, displayCutout.left)
                    val rightPadding = maxOf(systemBars.right, displayCutout.right)

                    view.setPadding(
                        leftPadding,
                        systemBars.top,
                        rightPadding,
                        0
                    )
                } else {
                    // In portrait, apply status bar at top and display cutout horizontally
                    view.setPadding(
                        displayCutout.left,
                        systemBars.top,
                        displayCutout.right,
                        0
                    )
                }
                insets
            }
        }

        // Setup container insets
        ViewCompat.setOnApplyWindowInsetsListener(container) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

            if (isLandscape) {
                // In landscape, apply navigation bars + display cutout horizontally and nav bar at bottom
                val leftPadding = maxOf(navigationBars.left, displayCutout.left)
                val rightPadding = maxOf(navigationBars.right, displayCutout.right)

                view.setPadding(
                    leftPadding,
                    0,
                    rightPadding,
                    navigationBars.bottom
                )
            } else {
                // In portrait, only apply display cutout insets horizontally
                view.setPadding(
                    displayCutout.left,
                    0,
                    displayCutout.right,
                    0
                )
            }
            insets
        }
    }

    private fun setActionBarStatusBarColor(color: Int) {
        val contentColor = resources?.getColor(R.color.textLightest) ?: Color.WHITE
        toolbar?.let {
            ViewStyler.themeToolbarColored(this, it, color, contentColor)
        }
        if (color != Int.MAX_VALUE) window.statusBarColor = color
    }

    companion object {

        fun createIntent(context: Context?, url: String?, title: String?, authenticate: Boolean): Intent {
            // Assumes no CanvasContext
            val extras = createBundle(emptyCourseContext(), url, title, authenticate)
            val intent = Intent(context, InternalWebViewActivity::class.java)
            intent.putExtra(Const.EXTRAS, extras)
            return intent
        }

        fun createIntent(context: Context?, url: String?, html: String?, title: String?, authenticate: Boolean): Intent {
            // Assumes no CanvasContext
            val extras = createBundle(emptyCourseContext(), url, title, authenticate, html)
            val intent = Intent(context, InternalWebViewActivity::class.java)
            intent.putExtra(Const.EXTRAS, extras)
            return intent
        }

        private fun createBundle(canvasContext: CanvasContext?): Bundle {
            val extras = Bundle()
            extras.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            return extras
        }

        private fun createBundle(canvasContext: CanvasContext?, url: String?, title: String?, authenticate: Boolean?): Bundle {
            val extras = createBundle(canvasContext)
            extras.putString(Const.INTERNAL_URL, url)
            extras.putBoolean(Const.AUTHENTICATE, authenticate!!)
            extras.putString(Const.ACTION_BAR_TITLE, title)
            return extras
        }

        private fun createBundle(canvasContext: CanvasContext?, url: String?, title: String?, authenticate: Boolean?, html: String?): Bundle {
            val extras = createBundle(canvasContext)
            extras.putString(Const.INTERNAL_URL, url)
            extras.putBoolean(Const.AUTHENTICATE, authenticate!!)
            extras.putString(Const.ACTION_BAR_TITLE, title)
            extras.putString(Const.HTML, html)
            return extras
        }
    }
}
