/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.loginapi.login.features.acceptableusepolicy

data class AcceptableUsePolicyViewData(
    val policy: String,
    val checked: Boolean = false,
    val loading: Boolean = false
)

sealed class AcceptableUsePolicyAction {
    data class OpenPolicy(val content: String): AcceptableUsePolicyAction()
    object PolicyAccepted : AcceptableUsePolicyAction()
    object AcceptFailure : AcceptableUsePolicyAction()
    object PolicyOpenFailed : AcceptableUsePolicyAction()
}