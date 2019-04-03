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
package com.instructure.parentapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.activities.BaseLoginSignInActivity
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Prefs
import com.instructure.parentapp.R
import com.instructure.parentapp.util.Const.CANVAS_PARENT_SP

class SignInActivity : BaseLoginSignInActivity() {

    override fun launchApplicationMainActivityIntent() = SplashActivity.createIntent(this)

    override fun refreshWidgets() {
        // No widgets in Canvas Parent
    }

    override fun userAgent() = "androidParent"

    private fun findStudent() = intent.getBooleanExtra(FIND_STUDENT, true)

    /**
     * Overrides the shouldOverrideUrlLoading to handle Canvas Parents specific use case.
     */
    override fun overrideUrlLoading(view: WebView, url: String): Boolean {
        when {
            url.contains(PARENT_SUCCESS_URL) -> {
                handleLaunchApplicationMainActivityIntent()
                return true
            }

            url.contains(PARENT_CANCEL_URL) -> { return false }
            url.contains(PARENT_ERROR_URL) -> {
                clearCookies()
                if (findStudent()) {
                    Toast.makeText(this@SignInActivity, R.string.unableToAddStudentError, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@SignInActivity, R.string.onlyCanvasObservers, Toast.LENGTH_SHORT).show()
                }
                return true
            }

            url.contains(PARENT_TOKEN_URL) -> {
                // When a parent logs in with observer credentials
                // Get the parent id from the url
                val parentId = "parent_id="
                val token = "token="
                var index = url.indexOf(parentId)
                if (index != -1) {
                    val endIndex = url.indexOf("&", index)
                    val prefs = Prefs(ContextKeeper.appContext, CANVAS_PARENT_SP)
                    prefs.save(Const.ID, url.substring(index + parentId.length, endIndex))
                }
                index = url.indexOf(token)
                if (index != -1) {
                    ApiPrefs.token = url.substring(index + token.length)
                }
                handleLaunchApplicationMainActivityIntent()
                return true
            }
            else -> return false
        }
    }

    companion object {
        private const val PARENT_SUCCESS_URL = "/oauthSuccess"
        private const val PARENT_CANCEL_URL = "/oauth2/deny"
        private const val PARENT_ERROR_URL = "/oauthFailure"
        private const val PARENT_TOKEN_URL = "/canvas/tokenReady"

        private const val FIND_STUDENT = "findStudent"
        private const val AS_ACTIVITY_FOR_RESULT = "asActivityForResult"

        fun createIntent(context: Context, accountDomain: AccountDomain, findStudent: Boolean): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            val extras = Bundle().apply {
                putParcelable(BaseLoginSignInActivity.ACCOUNT_DOMAIN, accountDomain)
                putBoolean(FIND_STUDENT, findStudent)
            }

            intent.putExtras(extras)
            return intent
        }

        fun createIntent(context: Context, accountDomain: AccountDomain, findStudent: Boolean, asActivityForResult: Boolean): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(BaseLoginSignInActivity.ACCOUNT_DOMAIN, accountDomain)
            extras.putBoolean(AS_ACTIVITY_FOR_RESULT, asActivityForResult)
            extras.putBoolean(FIND_STUDENT, findStudent)
            intent.putExtras(extras)
            return intent
        }
    }
}
