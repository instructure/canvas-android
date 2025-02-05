/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.notaparent

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.analytics.SCREEN_VIEW_NOT_A_PARENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.parentapp.util.ParentLogoutTask
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@ScreenView(SCREEN_VIEW_NOT_A_PARENT)
@AndroidEntryPoint
class NotAParentFragment : BaseCanvasFragment() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                NotAParentScreen(
                    returnToLoginClick = {
                        ParentLogoutTask(
                            LogoutTask.Type.LOGOUT,
                            alarmScheduler = alarmScheduler
                            ).execute()
                    },
                    onStudentClick = {
                        openStore("com.instructure.candroid")
                    },
                    onTeacherClick = {
                        openStore("com.instructure.teacher")
                    }
                )
            }
        }
    }

    private fun openStore(packageName: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
}
