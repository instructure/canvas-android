/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.espresso.fakes

import androidx.lifecycle.LiveData
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.orDefault

class FakeNetworkStateProvider(private val fakeLiveData: LiveData<Boolean>) : NetworkStateProvider {

    override val isOnlineLiveData: LiveData<Boolean>
        get() = fakeLiveData

    override fun isOnline(): Boolean {
        return fakeLiveData.value.orDefault()
    }
}