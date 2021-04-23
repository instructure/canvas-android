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
package com.instructure.loginapi.login.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.FeatureFlagProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Currently we are using this class to handle the calls to the K5 feature flag.
 * The only reason this is shared (only the code, but both have it's own instance) between different Activities of the login process is that we don't have proper MVVM in the login screens.
 * If we would have all the logic in MVVM we could have 3 separate ViewModels for both 3 Activities.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(private val featureFlagProvider: FeatureFlagProvider) : ViewModel() {

    private val canvasForElementaryResult = MutableLiveData<Event<Boolean>>()

    fun checkCanvasForElementaryFeature(errorFallback: Boolean = false): LiveData<Event<Boolean>> {
        viewModelScope.launch {
            val canvasForElementaryFlag = featureFlagProvider.getCanvasForElementaryFlag(errorFallback)
            canvasForElementaryResult.postValue(Event(canvasForElementaryFlag))
        }

        return canvasForElementaryResult
    }
}