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

import com.instructure.pandautils.mvvm.SubViewModel
import com.instructure.student.R

class HelpLinkSubViewModel(val helpLinkViewData: HelpLinkViewData, private val helpDialogViewModel: HelpDialogViewModel) : SubViewModel {

    override val layoutId: Int = R.layout.view_help_link

    fun onClick() {
        helpDialogViewModel.onLinkClicked(helpLinkViewData.action)
    }
}