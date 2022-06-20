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
package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.loginapi.login.activities.BaseLoginFindSchoolActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_FIND_SCHOOL
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.teacher.R

@ScreenView(SCREEN_VIEW_FIND_SCHOOL)
class FindSchoolActivity : BaseLoginFindSchoolActivity() {
    override fun themeColor() = ContextCompat.getColor(this, R.color.login_teacherAppTheme)

    override fun signInActivityIntent(accountDomain: AccountDomain): Intent {
        return SignInActivity.createIntent(this, accountDomain)
    }

    companion object {
        fun createIntent(context: Context?) = Intent(context, FindSchoolActivity::class.java)
    }
}
