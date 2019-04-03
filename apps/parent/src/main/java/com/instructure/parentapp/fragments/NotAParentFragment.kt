/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.fragments.BaseFragment
import com.instructure.parentapp.R
import com.instructure.parentapp.tasks.ParentLogoutTask
import kotlinx.android.synthetic.main.fragment_not_a_parent.*

class NotAParentFragment : BaseFragment() {

    override fun layoutResId(): Int = R.layout.fragment_not_a_parent

    override fun onCreateView(view: View?) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teacherLink.setOnClickListener {
            startActivity(playStoreIntent(TEACHER_ID))
            ParentLogoutTask(LogoutTask.Type.LOGOUT_NO_LOGIN_FLOW).execute()
        }

        studentLink.setOnClickListener {
            startActivity(playStoreIntent(CANVAS_ID))
            ParentLogoutTask(LogoutTask.Type.LOGOUT_NO_LOGIN_FLOW).execute()
        }

        login.setOnClickListener {
            ParentLogoutTask(LogoutTask.Type.LOGOUT).execute()
        }
    }

    private fun playStoreIntent(s: String): Intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(MARKET_URI_PREFIX + s)
    }

    companion object {
        private const val MARKET_URI_PREFIX = "market://details?id="
        private const val CANVAS_ID = "com.instructure.candroid"
        private const val TEACHER_ID = "com.instructure.teacher"
    }

}
