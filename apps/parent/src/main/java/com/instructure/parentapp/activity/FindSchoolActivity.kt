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
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.loginapi.login.activities.BaseLoginFindSchoolActivity
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.bind
import com.instructure.parentapp.R

class FindSchoolActivity : BaseLoginFindSchoolActivity() {

    // KAX currently doesn't work across module boundaries, so we manually bind these views here, see: https://youtrack.jetbrains.com/issue/KT-22430
    val toolbar: Toolbar by bind(R.id.toolbar)
    val domainInput: EditText by bind(R.id.domainInput)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(findStudent()) {
            mWhatsYourSchoolName?.setText(R.string.whatsYourStudentsSchoolName)
            toolbar.navigationIcon = null
            domainInput.setHint(R.string.findYourStudentsSchoolHint)
            showLogout(true)
        }
    }

    override fun applyTheme() {
        val color = themeColor()
        val view = LayoutInflater.from(this).inflate(R.layout.login_toolbar_icon, null, false)
        val icon = view.findViewById<ImageView>(com.instructure.loginapi.login.R.id.loginLogo)
        icon.setImageDrawable(ColorUtils.colorIt(color, icon.drawable))

        if (findStudent()) {
            val title = view.findViewById<TextView>(R.id.loginLogoText)
            title.setText(R.string.addStudent)
            title.visibility = View.VISIBLE
        }
        toolbar.addView(view)

        ViewStyler.setStatusBarLight(this)
    }

    override fun themeColor(): Int = ContextCompat.getColor(this, R.color.login_parentAppTheme)

    override fun signInActivityIntent(accountDomain: AccountDomain): Intent {
        val signInAsStudent = intent.extras?.getBoolean(FIND_STUDENT, true)
        return SignInActivity.createIntent(this, accountDomain, signInAsStudent ?: true)
    }

    companion object {
        private const val FIND_STUDENT = "findStudent"
        private const val AS_ACTIVITY_FOR_RESULT = "asActivityForResult"
        fun createIntent(context: Context, findStudent: Boolean): Intent {
            val intent = Intent(context, FindSchoolActivity::class.java)
            intent.putExtra(FIND_STUDENT, findStudent)
            return intent
        }

        fun createIntent(context: Context, findStudent: Boolean, asActivityForResult: Boolean): Intent {
            val intent = Intent(context, FindSchoolActivity::class.java)
            intent.putExtra(FIND_STUDENT, findStudent)
            intent.putExtra(AS_ACTIVITY_FOR_RESULT, asActivityForResult)
            return intent
        }
    }

    private fun findStudent(): Boolean = intent.getBooleanExtra(FIND_STUDENT, true)
}
