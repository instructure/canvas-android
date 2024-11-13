/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.loginapi.login.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.blueprint.BaseCanvasAppCompatDialogFragment
import androidx.fragment.app.FragmentManager

import com.instructure.loginapi.login.R
import com.instructure.pandautils.utils.ThemePrefs

@RestrictTo(RestrictTo.Scope.LIBRARY)
class NoInternetConnectionDialog : BaseCanvasAppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.login_noInternetConnectionTitle)
        builder.setMessage(R.string.login_noInternetConnectionMessage)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor) }

        return dialog
    }

    companion object {

        fun show(fragmentManager: FragmentManager) {
            NoInternetConnectionDialog().show(fragmentManager, NoInternetConnectionDialog::class.java.simpleName)
        }
    }
}
