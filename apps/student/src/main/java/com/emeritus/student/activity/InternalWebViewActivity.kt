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
package com.emeritus.student.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.CanvasContext.Companion.emptyCourseContext
import com.instructure.pandautils.activities.BaseActionBarActivity
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.pandautils.utils.toast
import com.emeritus.student.R
import com.emeritus.student.fragment.InternalWebviewFragment
import com.emeritus.student.fragment.InternalWebviewFragment.Companion.makeRoute
import com.emeritus.student.fragment.InternalWebviewFragment.Companion.newInstance

class InternalWebViewActivity : BaseActionBarActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar?.let { ViewStyler.themeToolbarLight(this, it) }
        if (savedInstanceState == null) {
            val bundle = intent.getBundleExtra(Const.EXTRAS)
            bundle?.getString(Const.ACTION_BAR_TITLE)?.let { toolbar?.title = it }
            bundle?.getParcelable<CanvasContext>(Const.CANVAS_CONTEXT)?.let { canvasContext ->
                // Currently we use an empty context when showing the EULA, privacy policy, etc., in which case we
                // want the internalWebViewFragment to hide its toolbar
                if (canvasContext.id == 0L) {
                    bundle.putBoolean(Const.HIDDEN_TOOLBAR, true)
                } else {
                    val color = canvasContext.backgroundColor
                    setActionBarStatusBarColors(color, color)
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

    private fun setActionBarStatusBarColors(actionBarColor: Int, statusBarColor: Int) {
        val colorDrawable = ColorDrawable(actionBarColor)
        supportActionBar?.setBackgroundDrawable(colorDrawable)
        if (statusBarColor != Int.MAX_VALUE) window.statusBarColor = statusBarColor
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
