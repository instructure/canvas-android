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
package com.instructure.pandautils.features.elementary.homeroom

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.help.HelpDialogViewData
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class HomeRoomViewModel @Inject constructor(
    private val apiPrefs: ApiPrefs,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<HomeroomViewData>
        get() = _data
    private val _data = MutableLiveData<HomeroomViewData>()

    init {
        loadData()
    }

    private fun loadData() {
        // TODO Load announcements and courses, separate ticket
        _state.postValue(ViewState.Loading)

        val greetingString = context.getString(R.string.homeroomWelcomeMessage, apiPrefs.user?.name) // TODO Check if we can receive display name from the API

        _data.postValue(HomeroomViewData(greetingString))
        _state.postValue(ViewState.Success)
    }
}