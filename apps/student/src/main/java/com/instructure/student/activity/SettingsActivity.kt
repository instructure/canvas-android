/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.instructure.loginapi.login.dialog.ErrorReportDialog
import com.instructure.pandautils.utils.toast
import com.instructure.student.R

class SettingsActivity : AppCompatActivity(), ErrorReportDialog.ErrorReportDialogResultListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    private val currentFragment: Fragment? get() = supportFragmentManager.fragments.last()

    fun addFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        currentFragment?.let { ft.hide(it) }
        ft.add(R.id.fragmentContainer, fragment, fragment.javaClass.name)
        ft.addToBackStack(fragment.javaClass.name)
        ft.commitAllowingStateLoss()
    }

    override fun onTicketPost() {
        // The message is a little longer than normal, so show it for LENGTH_LONG instead of LENGTH_SHORT
        Toast.makeText(this@SettingsActivity, R.string.errorReportThankyou, Toast.LENGTH_LONG).show()
    }

    override fun onTicketError() {
        toast(R.string.errorOccurred)
    }

    companion object {
        @JvmStatic fun createIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
