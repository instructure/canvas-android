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

package com.instructure.teacher.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.HelpLinksAPI
import com.instructure.canvasapi2.managers.HelpLinksManager
import com.instructure.canvasapi2.models.HelpLink
import com.instructure.canvasapi2.models.HelpLinks
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.utils.onClick
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import kotlinx.android.synthetic.main.dialog_help.view.*
import kotlinx.android.synthetic.main.view_help_link.view.*
import kotlinx.coroutines.Job
import java.util.*


class HelpDialogStyled : DialogFragment() {

    private val helpLinksManager = HelpLinksManager(HelpLinksAPI)

    var helpLinksJob: Job? = null
    private var helpLinks: HelpLinks? = null

    private val installDateString: String
        get() {
            return try {
                val installed = requireActivity().packageManager
                        .getPackageInfo(requireActivity().packageName, 0)
                        .firstInstallTime
                DateHelper.dayMonthYearFormat.format(Date(installed))
            } catch (e: Exception) {
                ""
            }
        }

    @SuppressLint("InflateParams") // Suppress lint warning about passing in null as the parent of a view to be inflated
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity()).setTitle(getString(R.string.help))
        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_help, null)

        builder.setView(view)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

        getHelpLinks(view)

        return dialog
    }

    private fun getHelpLinks(layoutView: View) {
        helpLinksJob = tryWeave {
            helpLinks = awaitApi<HelpLinks> { helpLinksManager.getHelpLinks(it, true) }

            with(layoutView) {
                if (helpLinks?.customHelpLinks?.isNotEmpty() == true) {
                    // We have custom links, let's use those
                    addLinks(container, helpLinks!!.customHelpLinks)
                } else {
                    // Default links
                    addLinks(container, helpLinks!!.defaultHelpLinks)
                }
            }

        } catch {
            Logger.d("Failed to grab help links: ${it.printStackTrace()}")
        }
    }

    // Local function that maps links to views and then adds them to the container
    private fun addLinks(container: ViewGroup, list: List<HelpLink>) {

        // Share love link is specific to Android - Add it to the list returned from the API
        val linksList = list.toMutableList().apply {
            add(HelpLink("", "", listOf("teacher"), "#share_the_love", getString(R.string.shareYourLove), getString(R.string.shareYourLoveDetails)))
        }
        linksList
                // Only want links for teachers
                .filter { link -> link.availableTo.contains("teacher") || link.availableTo.contains("user") }
                .forEach { link ->
                    val view = layoutInflater.inflate(R.layout.view_help_link, null)
                    view.title.text = link.text
                    view.subtitle.text = link.subtext
                    view.onClick { linkClick(link) }
                    container.addView(view)
                }
    }

    private fun linkClick(link: HelpLink) =
            when {
                // Internal routes
                link.url[0] == '#' ->
                    when (link.url) {
                        "#create_ticket" -> {
                            // Report a problem
                            val dialog = ErrorReportDialog()
                            dialog.arguments = ErrorReportDialog.createBundle(getString(R.string.appUserTypeTeacher))
                            dialog.show(requireActivity().supportFragmentManager, ErrorReportDialog.TAG)
                        }

                        "#share_the_love" -> {
                            RatingDialog.showRateDialog(requireActivity(), com.instructure.pandautils.utils.AppType.TEACHER)
                        }
                        else -> { } // Not handling anything else at the moment
                    }
                // External URL, but we handle within the app
                link.id.contains("submit_feature_idea") -> {
                    // Before custom help links, we were handling request a feature ourselves and
                    // we decided to keep that functionality instead of loading up the URL

                    // Let the user open their favorite mail client
                    val intent = populateMailIntent(getString(R.string.featureSubject), getString(R.string.understandRequest), false)
                    startActivity(Intent.createChooser(intent, getString(R.string.sendMail)))
                }
                link.url.startsWith("tel:") -> {
                    // Support phone links: https://community.canvaslms.com/docs/DOC-12664-4214610054
                    val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse(link.url) }
                    startActivity(intent)
                }
                link.url.startsWith("mailto:") -> {
                    // Support mailto links: https://community.canvaslms.com/docs/DOC-12664-4214610054
                    val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse(link.url) }
                    startActivity(intent)
                }
                link.url.contains("cases.canvaslms.com/liveagentchat") -> {
                    // Chat with Canvas Support - Doesn't seem work properly with WebViews, so we kick it out
                    // to the external browser
                    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(link.url) }
                    startActivity(intent)
                }
                // External URL
                else ->
                    startActivity(InternalWebViewActivity.createIntent(requireActivity(), link.url, link.text, false))
            }

    override fun onDestroyView() {
        if (retainInstance)
            dialog?.setDismissMessage(null)

        helpLinksJob?.cancel()

        super.onDestroyView()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    /* Pass in the subject and first line of the e-mail, all the other data is the same */
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
        val pInfo: PackageInfo
        var versionName = ""
        var versionCode = 0
        try {
            pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            versionName = pInfo.versionName
            versionCode = pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.d(e.message)
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, "[" + subject + "] " + getString(R.string.issue_with_canvas) + versionName)

        val user = ApiPrefs.user

        // Populate the email body with information about the user
        var emailBody = ""
        emailBody += title + "\n"
        if (user != null) {
            emailBody += getString(R.string.help_userId) + " " + user.id + "\n"
            emailBody += getString(R.string.help_email) + " " + user.email + "\n"
        } else {
            emailBody += getString(R.string.no_user) + "\n"
        }

        emailBody += getString(R.string.help_domain) + " " + ApiPrefs.domain + "\n"
        emailBody += getString(R.string.help_versionNum) + " " + versionName + " " + versionCode + "\n"
        emailBody += getString(R.string.help_locale) + " " + Locale.getDefault() + "\n"
        emailBody += getString(R.string.installDate) + " " + installDateString + "\n"
        emailBody += "----------------------------------------------\n"

        intent.putExtra(Intent.EXTRA_TEXT, emailBody)

        return intent
    }

    companion object {
        const val TAG = "helpDialog"

        fun show(activity: FragmentActivity): HelpDialogStyled =
                HelpDialogStyled().apply {
                    arguments = Bundle()
                    show(activity.supportFragmentManager, TAG)
                }
    }
}

