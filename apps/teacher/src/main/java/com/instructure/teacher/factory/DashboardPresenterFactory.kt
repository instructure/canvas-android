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

package com.instructure.teacher.factory


import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.teacher.presenters.DashboardPresenter
import com.instructure.teacher.viewinterface.CoursesView
import com.instructure.pandautils.blueprint.PresenterFactory

class DashboardPresenterFactory(
    private val userApi: UserAPI.UsersInterface,
    private val networkStateProvider: NetworkStateProvider
) : PresenterFactory<CoursesView, DashboardPresenter> {
    override fun create() = DashboardPresenter(userApi, networkStateProvider)
}
