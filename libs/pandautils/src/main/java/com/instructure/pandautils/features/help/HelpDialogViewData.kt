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
package com.instructure.pandautils.features.help

data class HelpDialogViewData(val helpLinks: List<HelpLinkItemViewModel>)

data class HelpLinkViewData(val title: String, val subtitle: String, val action: HelpDialogAction)

sealed class HelpDialogAction {
    object ReportProblem : HelpDialogAction()
    object AskInstructor : HelpDialogAction()
    object RateTheApp : HelpDialogAction()
    data class Phone(val url: String) : HelpDialogAction()
    data class SendMail(val url: String) : HelpDialogAction()
    data class OpenExternalBrowser(val url: String) : HelpDialogAction()
    data class OpenWebView(val url: String, val title: String) : HelpDialogAction()
}