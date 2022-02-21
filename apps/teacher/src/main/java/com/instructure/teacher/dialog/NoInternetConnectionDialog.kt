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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.instructure.pandautils.analytics.SCREEN_VIEW_NO_INTERNET_CONNECTION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs

import com.instructure.teacher.R

@ScreenView(SCREEN_VIEW_NO_INTERNET_CONNECTION)
class NoInternetConnectionDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.noInternetConnectionTitle)
        builder.setMessage(R.string.noInternetConnectionMessage)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
        }
        return dialog
    }

    companion object {
        fun show(fragmentManager: FragmentManager) {
            NoInternetConnectionDialog().show(fragmentManager, NoInternetConnectionDialog::class.java.simpleName)
        }
    }
}
