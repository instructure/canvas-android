/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.teacher.features.help

import androidx.fragment.app.FragmentActivity
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.pandautils.features.help.HelpDialogFragmentBehavior
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Utils
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity

class TeacherHelpDialogFragmentBehavior(private val activity: FragmentActivity) : HelpDialogFragmentBehavior {

    override fun reportProblem() {
        val dialog = ErrorReportDialog()
        dialog.arguments = ErrorReportDialog.createBundle(activity.getString(R.string.appUserTypeTeacher))
        dialog.show(activity.supportFragmentManager, ErrorReportDialog.TAG)
    }

    override fun rateTheApp() {
        Utils.goToAppStore(AppType.TEACHER, activity)
    }

    override fun askInstructor() = Unit

    override fun openWebView(url: String, title: String) {
        activity.startActivity(InternalWebViewActivity.createIntent(activity, url, title, false))
    }
}