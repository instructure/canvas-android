/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.loginapi.login.dialog

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.util.PatternsCompat
import com.instructure.pandautils.blueprint.BaseCanvasDialogFragment
import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.managers.ErrorReportManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.ErrorReport
import com.instructure.canvasapi2.models.ErrorReportPreFill
import com.instructure.canvasapi2.models.ErrorReportResult
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.loginapi.login.R
import com.instructure.loginapi.login.databinding.DialogErrorReportBinding
import com.instructure.loginapi.login.databinding.ErrorReportSverityItemBinding
import com.instructure.loginapi.login.util.Const
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.help.HelpDialogFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ErrorReportDialog : BaseCanvasDialogFragment() {
    private val binding by viewBinding(DialogErrorReportBinding::bind)

    private val severityOptions by lazy {
        listOf(
            ErrorReportAPI.Severity.COMMENT to getString(R.string.errorSeverityCasualQuestion),
            ErrorReportAPI.Severity.NOT_URGENT to getString(R.string.errorSeverityNeedHelp),
            ErrorReportAPI.Severity.WORKAROUND_POSSIBLE to getString(R.string.errorSeveritySomethingsBroken),
            ErrorReportAPI.Severity.BLOCKING to getString(R.string.errorSeverityCantGetThingsDone),
            ErrorReportAPI.Severity.CRITICAL to getString(R.string.errorSeverityExtremelyCritical)
        )
    }

    private val fromLogin: Boolean by BooleanArg(key = Const.FROM_LOGIN)
    private val useDefaultDomain: Boolean by BooleanArg(key = Const.USE_DEFAULT_DOMAIN)
    private val appName: String by StringArg(key = Const.APP_NAME)
    private val preFillData: ErrorReportPreFill by ParcelableArg(key = Const.PRE_FILL_DATA)

    private val installDateString: String
        get() {
            return try {
                val installed = requireContext().packageManager
                    .getPackageInfo(requireContext().packageName, 0)
                    .firstInstallTime
                val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                format.format(Date(installed))
            } catch (e: Exception) {
                ""
            }

        }

    @Suppress("UNCHECKED_CAST")
    private val selectedSeverity: Pair<ErrorReportAPI.Severity, String>
        get() = (binding.severitySpinner.selectedItem as? Pair<ErrorReportAPI.Severity, String>) ?: severityOptions[0]

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_error_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        if (fromLogin) {
            emailAddressEditText.visibility = View.VISIBLE
            emailAddress.visibility = View.VISIBLE
        }

        val adapter = ErrorSeverityAdapter(requireContext(), R.layout.error_report_sverity_item, severityOptions)
        severitySpinner.adapter = adapter

        // Pre-fill data
        preFillData.title?.let { title.text = it }
        preFillData.subject?.let { subjectEditText.setText(it) }
        preFillData.email?.let { emailAddressEditText.setText(it) }
        preFillData.comment?.let { descriptionEditText.setText(it) }
        preFillData.severity?.let {
            // Hide severity options if pre-filled
            severityPrompt.setGone()
            severitySpinner.setGone()
        }

        sendButton.onClick { uploadErrorReport() }
        cancelButton.onClick { dismiss() }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }
    }

    private inner class ErrorSeverityAdapter(
        context: Context,
        resource: Int,
        objects: List<Pair<ErrorReportAPI.Severity, String>>
    ) : ArrayAdapter<Pair<ErrorReportAPI.Severity, String>>(context, resource, objects) {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getViewForText(position, convertView, parent)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getViewForText(position, convertView, parent)
        }

        private fun getViewForText(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.error_report_sverity_item, parent, false)
            val binding = ErrorReportSverityItemBinding.bind(view)
            binding.text.text = getItem(position)?.second
            return view
        }
    }


    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun uploadErrorReport() = with(binding) {
        weave {

            var comment: String = descriptionEditText.text.toString()
            val subject = subjectEditText.text.toString()

            // Description (comment) and subject are required
            if (comment.isBlank() || subject.isBlank()) {
                toast(R.string.empty_feedback)
                return@weave
            }

            // Attempt to get email in the following order: EditText, user email, user login ID, blank
            val email = emailAddressEditText.text.toString().validOrNull()
                    ?: ApiPrefs.user?.let { it.primaryEmail ?: it.email ?: it.loginId }.validOrNull()
                    ?: ""

            // Require a valid email address if we're coming from login
            if (fromLogin && !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
                toast(R.string.errorReportInvalidEmail)
                return@weave
            }

            val url = ApiPrefs.domain.validOrNull() ?: ErrorReportAPI.DEFAULT_DOMAIN

            val (versionName, versionCode) = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).let {
                it.versionName to it.versionCode
            }

            comment = """
            ${getString(R.string.device)}: ${Build.MANUFACTURER} ${Build.MODEL}
            ${getString(R.string.osVersion)}: Android ${Build.VERSION.RELEASE}
            ${getString(R.string.versionNum)}: $appName v$versionName ($versionCode)
            ${getString(R.string.utils_installDate)} $installDateString

            -------------------------

            $comment
            """.trimIndent()

            // Get the enrollments for the user
            val enrollments: List<Enrollment> = try {
                awaitApi { UserManager.getSelfEnrollments(true, it) }
            } catch (e: Throwable) {
                emptyList()
            }

            val userRoles = enrollments.distinctBy { it.type }.joinToString(",") { it.type?.apiRoleString ?: ""}

            var becomeUser = ""
            ApiPrefs.user?.id?.let { becomeUser = "$url?become_user_id=$it" }

            val name = ApiPrefs.user?.name.orEmpty()

            val severity = (preFillData.severity ?: selectedSeverity.first).tag

            val report = ErrorReport(
                    comment = comment,
                    subject = subject,
                    email = email,
                    url = url,
                    userRoles = userRoles,
                    becomeUser = becomeUser,
                    name = name,
                    severity = severity
            )

            cancelButton.setInvisible()
            sendButton.setInvisible()
            progressBar.setVisible()

            try {
                awaitApi<ErrorReportResult> { ErrorReportManager.postErrorReport(report, useDefaultDomain, it) }

                onTicketPost()
            } catch (e: Throwable) {
                onTicketError()
            }
        }
    }

    private fun onTicketPost() {
        dismiss()
        dismissHelpDialog()
        Toast.makeText(activity, R.string.errorReportThankyou, Toast.LENGTH_LONG).show()
    }

    private fun onTicketError() = with(binding) {
        cancelButton.setVisible()
        sendButton.setVisible()
        progressBar.setGone()

        dismiss()
        dismissHelpDialog()
        Toast.makeText(activity, R.string.errorOccurred, Toast.LENGTH_LONG).show()
    }

    private fun dismissHelpDialog() {
        val fragment = activity?.supportFragmentManager?.findFragmentByTag(HelpDialogFragment.TAG)
        if (fragment is HelpDialogFragment) {
            try {
                fragment.dismiss()
            } catch (e: IllegalStateException) {
                Logger.e("Committing a transaction after activities saved state was called: " + e)
            }
        }
    }

    companion object {

        const val TAG = "ErrorReportDialog"

        /**
         * if we're coming from the login screen there won't be any user information (because the user hasn't
         * logged in)
         * @param fromLogin boolean telling if coming from a login page where their may not be a valid user.
         * @return Bundle
         */
        fun createBundle(
            appName: String,
            fromLogin: Boolean = false,
            useDefaultDomain: Boolean = false,
            preFill: ErrorReportPreFill = ErrorReportPreFill()
        ) = Bundle().apply {
            putString(Const.APP_NAME, appName)
            putBoolean(Const.FROM_LOGIN, fromLogin)
            putBoolean(Const.USE_DEFAULT_DOMAIN, useDefaultDomain)
            putParcelable(Const.PRE_FILL_DATA, preFill)
        }

    }
}

