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
package com.instructure.pandautils.features.elementary.resources.itemviewmodels

import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.resources.LtiApplicationViewData
import com.instructure.pandautils.features.elementary.resources.ResourcesItemViewType
import com.instructure.pandautils.mvvm.ItemViewModel

class LtiApplicationItemViewModel(
    val data: LtiApplicationViewData,
    val marginBottom: Int = 0) : ItemViewModel {

    override val layoutId: Int = R.layout.item_lti_application

    override val viewType: Int = ResourcesItemViewType.LTI_APPLICATION.viewType
}