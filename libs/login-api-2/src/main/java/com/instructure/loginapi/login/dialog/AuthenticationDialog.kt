/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.utils.Analytics.logEvent
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.loginapi.login.R
import com.instructure.pandautils.base.BaseCanvasDialogFragment

class AuthenticationDialog : BaseCanvasDialogFragment() {
    interface OnAuthenticationSet {
        fun onRetrieveCredentials(username: String?, password: String?)
    }

    private lateinit var callback: OnAuthenticationSet
    private lateinit var username: EditText
    private lateinit var password: EditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as? OnAuthenticationSet
            ?: throw IllegalStateException("Context required to implement AuthenticationDialog.OnAuthenticationSet callback")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (arguments != null && requireArguments()[DOMAIN] != null) {
            val bundle = Bundle()
            bundle.putString(AnalyticsParamConstants.DOMAIN_PARAM, requireArguments().getString(DOMAIN))
            logEvent(AnalyticsEventConstants.AUTHENTICATION_DIALOG, bundle)
        }
        val builder = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
        builder.setTitle(R.string.authenticationRequired)
        val root = LayoutInflater.from(context).inflate(R.layout.dialog_auth, null)
        username = root.findViewById(R.id.username)
        password = root.findViewById(R.id.password)
        builder.setView(root)
        builder.setPositiveButton(R.string.done) { dialog, _ ->
            callback.onRetrieveCredentials(username.text.toString(), password.text.toString())
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    companion object {
        private const val DOMAIN = "domain"

        fun newInstance(domain: String?, vararg target: Fragment?): AuthenticationDialog {
            val dialog = AuthenticationDialog()
            val args = Bundle()
            args.putString(DOMAIN, domain)
            dialog.arguments = args
            if (target.isNotEmpty()) {
                dialog.setTargetFragment(target[0], 1)
            }
            return dialog
        }
    }
}
