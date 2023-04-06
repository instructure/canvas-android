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

import android.webkit.CookieManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.pandautils.mvvm.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AcceptableUsePolicyViewModel @Inject constructor(
    private val userManager: UserManager,
    private val cookieManager: CookieManager
) : ViewModel() {

    val data: LiveData<AcceptableUsePolicyViewData>
        get() = _data
    private val _data = MutableLiveData(AcceptableUsePolicyViewData(""))

    val events: LiveData<Event<AcceptableUsePolicyAction>>
        get() = _events
    private val _events = MutableLiveData<Event<AcceptableUsePolicyAction>>()

    init {
        viewModelScope.launch {
            try {
                val termsOfService =
                    userManager.getTermsOfServiceAsync(true).await().dataOrThrow.content.orEmpty()
                _data.value = _data.value?.copy(policy = termsOfService)
            } catch (e: Exception) {
            }
        }
    }

    fun checkedChanged(checked: Boolean) {
        _data.value = _data.value?.copy(checked = checked)
    }

    fun openPolicy() {
        val policy = _data.value?.policy.orEmpty()
        if (policy.isNotEmpty()) {
            _events.value = Event(AcceptableUsePolicyAction.OpenPolicy(_data.value?.policy.orEmpty()))
        } else {
            _data.value = _data.value?.copy(loading = true)
            viewModelScope.launch {
                try {
                    val termsOfService = userManager.getTermsOfServiceAsync(true).await().dataOrThrow.content.orEmpty()
                    _data.value = _data.value?.copy(policy = termsOfService, loading = false)
                    _events.value = Event(AcceptableUsePolicyAction.OpenPolicy(termsOfService))
                } catch (e: Exception) {
                    _data.value = _data.value?.copy(loading = false)
                    _events.value = Event(AcceptableUsePolicyAction.PolicyOpenFailed)
                }
            }
        }
    }

    fun acceptPolicy() {
        _data.value = _data.value?.copy(loading = true)
        viewModelScope.launch {
            try {
                val result = userManager.acceptUserTermsAsync().await()
                _data.value = _data.value?.copy(loading = false)
                if (result.isSuccess) {
                    // Need to clear cookies, because on login, it is stored that the user has not accepted the Use Policy
                    cookieManager.removeAllCookies {}
                    _events.value = Event(AcceptableUsePolicyAction.PolicyAccepted)
                } else {
                    _events.value = Event(AcceptableUsePolicyAction.AcceptFailure)
                }
            } catch (e: Exception) {
                _data.value = _data.value?.copy(loading = false)
                _events.value = Event(AcceptableUsePolicyAction.AcceptFailure)
            }
        }
    }
}
