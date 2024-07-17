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

package com.instructure.parentapp.features.help

import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.pandautils.features.help.HelpDialogFragmentBehavior
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Utils
import com.instructure.parentapp.R

class ParentHelpDialogFragmentBehavior(private val parentActivity: FragmentActivity) : HelpDialogFragmentBehavior, ErrorReportDialog.ErrorReportDialogResultListener {
    private var errorReportDialog: ErrorReportDialog? = null

    override fun reportProblem() {
        errorReportDialog = ErrorReportDialog(this).apply {
            arguments = ErrorReportDialog.createBundle(parentActivity.getString(R.string.appUserTypeParent))
            show(parentActivity.supportFragmentManager, ErrorReportDialog.TAG)
        }
    }

    override fun rateTheApp() {
        Utils.goToAppStore(AppType.PARENT, parentActivity)
    }

    override fun askInstructor() = Unit

    override fun openWebView(url: String, title: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        parentActivity.startActivity(intent)
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
