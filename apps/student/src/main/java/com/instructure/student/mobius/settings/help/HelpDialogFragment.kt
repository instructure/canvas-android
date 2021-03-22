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

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.pandautils.utils.AppType
import com.instructure.pandautils.utils.Event
import com.instructure.pandautils.utils.Utils
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.databinding.HelpDialogBinding
import com.instructure.student.dialog.AskInstructorDialogStyled
import com.instructure.student.util.LoggingUtility
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class HelpDialogFragment : DialogFragment() {

    private val installDateString: String
        get() {
            return try {
                val installed = requireContext().packageManager
                    .getPackageInfo(requireContext().packageName, 0)
                    .firstInstallTime
                DateHelper.dayMonthYearFormat.format(Date(installed))
            } catch (e: Exception) {
                ""
            }
        }

    private val viewModel: HelpDialogViewModel by viewModels()

    @SuppressLint("InflateParams") // Suppress lint warning about null parent when inflating layout
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext()).setTitle(requireContext().getString(R.string.help))

        val binding = HelpDialogBinding.inflate(LayoutInflater.from(context))
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        viewModel.events.observe(this, Observer { event: Event<HelpDialogAction>? ->
            event?.getContentIfNotHandled()?.let { action: HelpDialogAction ->
                handleAction(action)
            }
        })

        return dialog
    }

    private fun handleAction(action: HelpDialogAction) {
        when (action) {
            is HelpDialogAction.ReportProblem -> {
                // Report a problem
                val dialog = ErrorReportDialog()
                dialog.arguments = ErrorReportDialog.createBundle(getString(R.string.appUserTypeStudent))
                dialog.show(requireActivity().supportFragmentManager, ErrorReportDialog.TAG)
            }
            is HelpDialogAction.AskInstructor -> {
                // Ask instructor a question
                // Open the ask instructor dialog
                AskInstructorDialogStyled().show(requireFragmentManager(), AskInstructorDialogStyled.TAG)
            }
            is HelpDialogAction.RateTheApp -> {
                Utils.goToAppStore(AppType.STUDENT, activity)
            }
            // External URL, but we handle within the app
            is HelpDialogAction.SubmitFeatureIdea -> {
                // Before custom help links, we were handling request a feature ourselves and
                // we decided to keep that functionality instead of loading up the URL

                // Let the user open their favorite mail client
                val intent = populateMailIntent(getString(R.string.featureSubject), getString(R.string.understandRequest), false)
                startActivity(Intent.createChooser(intent, getString(R.string.sendMail)))
            }
            is HelpDialogAction.Phone -> {
                // Support phone links: https://community.canvaslms.com/docs/DOC-12664-4214610054
                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse(action.url) }
                startActivity(intent)
            }
            is HelpDialogAction.SendMail -> {
                // Support mailto links: https://community.canvaslms.com/docs/DOC-12664-4214610054
                val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse(action.url) }
                startActivity(intent)
            }
            is HelpDialogAction.OpenExternalBrowser -> {
                // Chat with Canvas Support - Doesn't seem work properly with WebViews, so we kick it out
                // to the external browser
                val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(action.url) }
                startActivity(intent)
            }
            // External URL
            is HelpDialogAction.OpenWebView ->
                startActivity(InternalWebViewActivity.createIntent(activity, action.url, action.title, false))
        }
    }

    private fun populateMailIntent(subject: String, title: String, supportFlag: Boolean): Intent {
        // Let the user open their favorite mail client
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"

        if (supportFlag) {
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.utils_supportEmailAddress)))
        } else {
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.utils_mobileSupportEmailAddress)))
        }

        // Try to get the version number and version code
        val pInfo: PackageInfo?
        var versionName = ""
        var versionCode = 0
        try {
            pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            versionName = pInfo.versionName
            versionCode = pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            LoggingUtility.logConsole(e.message)
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, "[$subject] Issue with Canvas [Android] $versionName")

        val user = ApiPrefs.user
        // Populate the email body with information about the user
        var emailBody = ""
        emailBody += title + "\n"
        emailBody += getString(R.string.help_userId) + " " + user!!.id + "\n"
        emailBody += getString(R.string.help_email) + " " + user.email + "\n"
        emailBody += getString(R.string.help_domain) + " " + ApiPrefs.domain + "\n"
        emailBody += getString(R.string.help_versionNum) + " " + versionName + " " + versionCode + "\n"
        emailBody += getString(R.string.help_locale) + " " + Locale.getDefault() + "\n"
        emailBody += getString(R.string.installDate) + " " + installDateString + "\n"
        emailBody += "----------------------------------------------\n"

        intent.putExtra(Intent.EXTRA_TEXT, emailBody)

        return intent
    }

    override fun onDestroyView() {
        dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        const val TAG = "helpDialog"

        fun show(activity: FragmentActivity): HelpDialogFragment =
            HelpDialogFragment().apply {
                show(activity.supportFragmentManager, TAG)
            }
    }
}