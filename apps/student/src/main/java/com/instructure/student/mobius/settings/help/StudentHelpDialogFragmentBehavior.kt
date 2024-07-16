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
package com.instructure.student.mobius.settings.help

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.pandautils.features.help.HelpDialogFragmentBehavior
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Utils
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.dialog.AskInstructorDialogStyled

class StudentHelpDialogFragmentBehavior(private val parentActivity: FragmentActivity) : HelpDialogFragmentBehavior, ErrorReportDialog.ErrorReportDialogResultListener {
    private var errorReportDialog: ErrorReportDialog? = null

    override fun reportProblem() {
        errorReportDialog = ErrorReportDialog(this).apply {
            arguments = ErrorReportDialog.createBundle(parentActivity.getString(R.string.appUserTypeStudent))
            show(parentActivity.supportFragmentManager, ErrorReportDialog.TAG)
        }
    }

    override fun rateTheApp() {
        Utils.goToAppStore(AppType.STUDENT, parentActivity)
    }

    override fun askInstructor() {
        AskInstructorDialogStyled().show(parentActivity.supportFragmentManager, AskInstructorDialogStyled.TAG)
    }

    override fun openWebView(url: String, title: String) {
        parentActivity.startActivity(InternalWebViewActivity.createIntent(parentActivity, url, title, false))
    }

    override fun onTicketPost() {
        errorReportDialog?.dismiss()
        Toast.makeText(parentActivity, R.string.errorReportThankyou, Toast.LENGTH_LONG).show()
    }

    override fun onTicketError() {
        errorReportDialog?.dismiss()
        Toast.makeText(parentActivity, R.string.errorOccurred, Toast.LENGTH_LONG).show()
    }
}