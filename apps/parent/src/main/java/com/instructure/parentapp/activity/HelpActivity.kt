/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Utils
import com.instructure.parentapp.R
import com.instructure.parentapp.dialogs.LegalDialog
import kotlinx.android.synthetic.main.activity_help.*
import java.text.SimpleDateFormat
import java.util.*

class HelpActivity : AppCompatActivity(), ErrorReportDialog.ErrorReportDialogResultListener {

    private val installDateString: String
        get() {
            try {
                val installed = packageManager
                        .getPackageInfo(packageName, 0)
                        .firstInstallTime
                val format = SimpleDateFormat("dd MMM yyyy")
                return format.format(Date(installed))
            } catch (e: Exception) {
                return ""
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_close_white)
        toolbar.setNavigationContentDescription(R.string.close)
        toolbar.setTitle(R.string.help)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        searchGuides.setOnClickListener {
            //Search guides
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(Const.CANVAS_USER_GUIDES)
            startActivity(intent)
        }

        reportProblem.setOnClickListener {
            val dialog = ErrorReportDialog()

            dialog.arguments = ErrorReportDialog.createBundle(getString(R.string.app_name_parent))
            dialog.show(supportFragmentManager, ErrorReportDialog.TAG)
        }

        requestFeature.setOnClickListener {
            //let the user open their favorite mail client
            val intent = populateMailIntent(getString(R.string.featureSubject), getString(R.string.understandRequest), false)
            startActivity(Intent.createChooser(intent, getString(R.string.sendMail)))
        }

        shareLove.setOnClickListener { Utils.goToAppStore(AppType.PARENT, this@HelpActivity) }

        legal.setOnClickListener { LegalDialog().show(supportFragmentManager, LegalDialog.TAG) }
    }

    //region Error reporting
    override fun onTicketPost() {
        Toast.makeText(this, R.string.errorReportThankyou, Toast.LENGTH_LONG).show()
    }

    override fun onTicketError() {
        Toast.makeText(this, R.string.errorOccurred, Toast.LENGTH_LONG).show()
    }
    //endregion

    /*
        Pass in the subject and first line of the e-mail, all the other data is the same
     */
    private fun populateMailIntent(subject: String, title: String, supportFlag: Boolean): Intent {
        //let the user open their favorite mail client
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        if (supportFlag) {
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.utils_supportEmailAddress)))
        } else {
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.utils_mobileSupportEmailAddress)))
        }
        //try to get the version number and version code
        val pInfo: PackageInfo?
        var versionName = ""
        var versionCode = 0
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0)
            versionName = pInfo.versionName
            versionCode = pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("ParentApp", e.message)
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, "[$subject] Issue with ParentApp [Android] $versionName")

        val parentId = ApiPrefs.user?.id ?: 0
        //populate the email body with information about the user
        var emailBody = ""
        emailBody += title + "\n"
        emailBody += getString(R.string.help_userId) + " " + parentId + "\n"
        emailBody += getString(R.string.help_email) + " " + ApiPrefs.user?.email + "\n"
        emailBody += getString(R.string.help_domain) + " " + ApiPrefs.airwolfDomain + "\n"
        emailBody += getString(R.string.help_versionNum) + " " + versionName + " " + versionCode + "\n"
        emailBody += getString(R.string.help_locale) + " " + Locale.getDefault() + "\n"
        emailBody += getString(R.string.installDate) + " " + installDateString + "\n"
        emailBody += "----------------------------------------------\n"

        intent.putExtra(Intent.EXTRA_TEXT, emailBody)

        return intent
    }

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, HelpActivity::class.java)
        }
    }
}
