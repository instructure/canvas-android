/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.fragment.LTIWebViewFragment
import kotlinx.android.synthetic.main.activity_lti.*

/**
 * Intended to handle launch definition endpoints for Gauge and Studio.
 */
class LTIActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lti)

        // Keeping the toolbar here because we can get into a state where Gauge/Studio says we can go back but we cannot.
        // This keeps the user on this screen with no way out. Trapped like a beaver in  a glove box.

        val launchDefinition = intent.extras?.getParcelable<LaunchDefinition>(LAUNCH_DEFINITION)
        var title = ""
        if(launchDefinition != null) {
            title = if(launchDefinition.domain == LaunchDefinition._GAUGE_DOMAIN) getString(R.string.gauge) else getString(R.string.studio)
        }

        toolbar.title = title
        toolbar.setupAsBackButton { finish() }
        ViewStyler.themeToolbar(this, toolbar, Color.WHITE, Color.BLACK, false)


        val user = ApiPrefs.user
        if(launchDefinition != null && user != null) {
            val route = LTIWebViewFragment.makeRoute(
                CanvasContext.currentUserContext(user),
                launchDefinition.placements.globalNavigation.url,
                title,
                true,
                true
            )
            val fragment = LTIWebViewFragment.newInstance(route)
            fragment?.let {supportFragmentManager.beginTransaction().add(R.id.container, it, LTIWebViewFragment::class.java.simpleName).commit() }
        } else {
            Toast.makeText(this, R.string.ltiLaunchFailure, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if(fragment is LTIWebViewFragment && fragment.canGoBack()) {
            // This prevents users from going back to the launch url for LITs, that url shows an error message and is
            // only used to forward the user to the actual LTI.
            val webBackForwardList = fragment.getCanvasWebView()?.copyBackForwardList()
            val historyUrl = webBackForwardList?.getItemAtIndex(webBackForwardList.currentIndex - 1)?.url
            if(historyUrl != null && historyUrl.contains("external_tools/sessionless_launch")) {
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val LAUNCH_DEFINITION = "launchDefinition"

        fun createIntent(context: Context, launchDefinition: LaunchDefinition): Intent {
            val intent =  Intent(context, LTIActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(LAUNCH_DEFINITION, launchDefinition)
            intent.putExtras(extras)
            return intent
        }
    }
}
