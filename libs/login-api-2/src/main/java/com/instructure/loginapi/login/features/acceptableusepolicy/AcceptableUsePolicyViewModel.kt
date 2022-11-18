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
class AcceptableUsePolicyViewModel @Inject constructor(private val userManager: UserManager) : ViewModel() {

    val policy: LiveData<String>
        get() = _policy
    private val _policy = MutableLiveData("")

    val checked: LiveData<Boolean>
        get() = _checked
    private val _checked = MutableLiveData(false)

    val events: LiveData<Event<AcceptableUsePolicyAction>>
        get() = _events
    private val _events = MutableLiveData<Event<AcceptableUsePolicyAction>>()

    init {
        viewModelScope.launch {
            try {
                val termsOfService = userManager.getTermsOfServiceAsync(true).await().dataOrThrow.content ?: ""
                _policy.value = termsOfService
            } catch (e: Exception) {

            }
        }
    }

    fun checkedChanged(checked: Boolean) {
        _checked.value = checked
    }

    fun openPolicy() {
        _events.value = Event(AcceptableUsePolicyAction.OpenPolicy(_policy.value ?: ""))
    }

    fun acceptPolicy() {
        viewModelScope.launch {
            val result = userManager.acceptUserTermsAsync().await()
            if (result.isSuccess) {
                _events.value = Event(AcceptableUsePolicyAction.PolicyAccepted)
            }
        }
    }
}

sealed class AcceptableUsePolicyAction {
    data class OpenPolicy(val content: String): AcceptableUsePolicyAction()
    object PolicyAccepted: AcceptableUsePolicyAction()
}