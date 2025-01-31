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
package com.instructure.teacher.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.analytics.SCREEN_VIEW_NOT_A_TEACHER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.fragments.BaseFragment
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentNotATeacherBinding
import com.instructure.teacher.tasks.TeacherLogoutTask
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_NOT_A_TEACHER)
@AndroidEntryPoint
class NotATeacherFragment : BaseFragment() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val binding by viewBinding(FragmentNotATeacherBinding::bind)

    private val MARKET_URI_PREFIX = "market://details?id="
    private val CANVAS_ID = "com.instructure.candroid"
    private val PARENT_ID = "com.instructure.parentapp"

    override fun layoutResId(): Int = R.layout.fragment_not_a_teacher

    override fun onCreateView(view: View) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        parentLink.setOnClickListener {
            startActivity(playStoreIntent(PARENT_ID))
            TeacherLogoutTask(
                LogoutTask.Type.LOGOUT_NO_LOGIN_FLOW,
                alarmScheduler = alarmScheduler
            ).execute()
        }

        studentLink.setOnClickListener {
            startActivity(playStoreIntent(CANVAS_ID))
            TeacherLogoutTask(
                LogoutTask.Type.LOGOUT_NO_LOGIN_FLOW,
                alarmScheduler = alarmScheduler
            ).execute()
        }

        login.setOnClickListener {
            TeacherLogoutTask(
                LogoutTask.Type.LOGOUT,
                alarmScheduler = alarmScheduler
            ).execute()
        }
    }

    private fun playStoreIntent(s: String): Intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(MARKET_URI_PREFIX + s) }
}
